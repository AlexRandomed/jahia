/**
 * This file is part of Jahia: An integrated WCM, DMS and Portal Solution
 * Copyright (C) 2002-2010 Jahia Solutions Group SA. All rights reserved.
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301, USA.
 *
 * As a special exception to the terms and conditions of version 2.0 of
 * the GPL (or any later version), you may redistribute this Program in connection
 * with Free/Libre and Open Source Software ("FLOSS") applications as described
 * in Jahia's FLOSS exception. You should have received a copy of the text
 * describing the FLOSS exception, and it is also available here:
 * http://www.jahia.com/license
 *
 * Commercial and Supported Versions of the program
 * Alternatively, commercial and supported versions of the program may be used
 * in accordance with the terms contained in a separate written agreement
 * between you and Jahia Solutions Group SA. If you are unsure which license is appropriate
 * for your use, please contact the sales department at sales@jahia.com.
 */

package org.jahia.services.usermanager.jcr;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.jahia.api.Constants;
import org.jahia.exceptions.JahiaException;
import org.jahia.exceptions.JahiaInitializationException;
import org.jahia.services.cache.Cache;
import org.jahia.services.cache.CacheService;
import org.jahia.services.content.*;
import org.jahia.services.templates.TemplatePackageApplicationContextLoader.ContextInitializedEvent;
import org.jahia.services.usermanager.JahiaUser;
import org.jahia.services.usermanager.JahiaUserManagerProvider;
import org.springframework.context.ApplicationEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.web.context.ServletContextAware;

import javax.jcr.*;
import javax.jcr.query.Query;
import javax.jcr.query.QueryResult;
import javax.jcr.query.Row;
import javax.jcr.query.RowIterator;
import javax.servlet.ServletContext;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.*;

/**
 * JCR implementation of the user manager provider interface. This class stores and
 * retrieves users from a JCR backend.
 *
 * @author : rincevent
 * @since : JAHIA 6.1
 *        Created : 7 juil. 2009
 */
public class JCRUserManagerProvider extends JahiaUserManagerProvider implements ServletContextAware, ApplicationListener {
	private static final String ROOT_PWD_RESET_FILE = "/WEB-INF/etc/config/root.pwd";
    private transient static Logger logger = org.slf4j.LoggerFactory.getLogger(JCRUserManagerProvider.class);
    private transient JCRTemplate jcrTemplate;
    private static JCRUserManagerProvider mUserManagerService;
    private transient CacheService cacheService;
    private transient Cache<String, JCRUser> cache;
    private static transient Map<String, String> mappingOfProperties;
    private ServletContext servletContext;

    static {
        mappingOfProperties = new HashMap<String, String>(3);
        mappingOfProperties.put("lastname", "j:lastName");
        mappingOfProperties.put("firstname", "j:firstName");
        mappingOfProperties.put("organization", "j:organization");
    }

    /**
     * Create an new instance of the User Manager Service if the instance do not
     * exist, or return the existing instance.
     *
     * @return Return the instance of the User Manager Service.
     */
    public static JCRUserManagerProvider getInstance() {
        if (mUserManagerService == null) {
            mUserManagerService = new JCRUserManagerProvider();
        }
        return mUserManagerService;
    }

    public void setJcrTemplate(JCRTemplate jcrTemplate) {
        this.jcrTemplate = jcrTemplate;
    }

    public void setCacheService(CacheService cacheService) {
        this.cacheService = cacheService;
    }

    /**
     * This is the method that creates a new user in the system, with all the
     * specified attributes.
     *
     * @param password User password
     */
    public JCRUser createUser(final String name, final String password, final Properties properties) {
        try {
            return jcrTemplate.doExecuteWithSystemSession(new JCRCallback<JCRUser>() {
                public JCRUser doInJCR(JCRSessionWrapper jcrSessionWrapper) throws RepositoryException {
                    JCRNodeWrapper parentNodeWrapper = jcrSessionWrapper.getNode( "/users");

                    jcrSessionWrapper.checkout(parentNodeWrapper);
                    Node userNode = parentNodeWrapper.addNode(name, Constants.JAHIANT_USER);
                    if (parentNodeWrapper.hasProperty("j:usersFolderSkeleton")) {
                        InputStream is = null;
                        try {
                            is = new FileInputStream(
                                    org.jahia.settings.SettingsBean.getInstance().getJahiaEtcDiskPath() + "/repository/" + parentNodeWrapper.getProperty(
                                    "j:usersFolderSkeleton").getString());
                            jcrSessionWrapper.importXML(parentNodeWrapper.getPath() + "/" + name, is,
                                                      ImportUUIDBehavior.IMPORT_UUID_CREATE_NEW, true);
                        } catch (IOException e) {
                            throw new RepositoryException("Could not create user due to some import issues", e);
                        } finally {
                            IOUtils.closeQuietly(is);
                        }
                        JCRNodeWrapperImpl.changePermissions(userNode, "u:" + name, "rw");
                    } else {
                        JCRNodeWrapperImpl.changePermissions(userNode, "u:" + name, "rw");
                    }
                    String l_password;
                    if (!password.startsWith("SHA-1:")) {
                        // Encrypt the password
                        l_password = encryptPassword(password);
                    } else {
                        l_password = password.substring(6);
                    }

                    userNode.setProperty(JCRUser.J_PASSWORD, l_password);
                    userNode.setProperty(JCRUser.J_EXTERNAL, false);
                    for (Map.Entry<Object, Object> entry : properties.entrySet()) {
                        String key = (String) entry.getKey();
                        if (mappingOfProperties.containsKey(key)) {
                            key = mappingOfProperties.get(key);
                        }
                        userNode.setProperty(key, (String) entry.getValue());
                    }
                    jcrSessionWrapper.save();
                    // Use rules instead to publish the user
                   /* publicationService.publish(userNode.getPath(), Constants.EDIT_WORKSPACE, Constants.LIVE_WORKSPACE, null,
                            true);*/
                    return new JCRUser(userNode.getIdentifier(), jcrTemplate);
                }
            });
        } catch (RepositoryException e) {
            logger.error("Error while creating user " + name, e);
            return null;
        }
    }

    /**
     * This method removes a user from the system. All the user's attributes are
     * remove, and also all the related objects belonging to the user. On success,
     * true is returned and the user parameter is not longer valid. Return false
     * on any failure.
     *
     * @param user reference on the user to be deleted.
     */
    public boolean deleteUser(JahiaUser user) {
        if (user instanceof JCRUser) {
            final JCRUser jcrUser = (JCRUser) user;
            final String name = jcrUser.getName();
            try {
                JCRCallback<Boolean> deleteCallcback = new JCRCallback<Boolean>() {
                    public Boolean doInJCR(JCRSessionWrapper session) throws RepositoryException {
                        Node node = jcrUser.getNode(session);
                        session.checkout(node.getParent());
                        session.checkout(node);                        
                        node.remove();
                        session.save();
                        return true;
                    }
                };
                jcrTemplate.doExecuteWithSystemSession(deleteCallcback);
                // Now let's delete the live workspace user
                jcrTemplate.doExecuteWithSystemSession(null, Constants.LIVE_WORKSPACE, deleteCallcback);
            } catch (RepositoryException e) {
                logger.error("Error while deleting user", e);
                return false;
            } finally {
                updateCache(name);
            }
            return true;
        }
        return false;
    }

    /**
     * Return the number of user in the system.
     *
     * @return Return the number of users in the system.
     */
    public int getNbUsers() {
        // TODO to be implemented or removed.
        return 0;
    }

    /**
     * This method return all users' keys in the system.
     *
     * @return Return a List of strings holding the user identification key .
     */
    public List<String> getUserList() {
        try {
            return jcrTemplate.doExecuteWithSystemSession(new JCRCallback<List<String>>() {
                public List<String> doInJCR(JCRSessionWrapper session) throws RepositoryException {
                    List<String> users = new ArrayList<String>();
                    if (session.getWorkspace().getQueryManager() != null) {
                        String query = "SELECT [j:nodename] FROM [" + Constants.JAHIANT_USER + "] ORDER BY [j:nodename]";
                        Query q = session.getWorkspace().getQueryManager().createQuery(query, Query.JCR_SQL2);
                        QueryResult qr = q.execute();
                        RowIterator rows = qr.getRows();
                        while (rows.hasNext()) {
                            Row usersFolderNode = rows.nextRow();
                            String userName = "{jcr}" + usersFolderNode.getValue("j:nodename").getString();
                            if (!users.contains(userName)) {
                                users.add(userName);
                            }
                        }
                    }
                    return users;
                }
            });
        } catch (RepositoryException e) {
            logger.error("Error while retrieving user list", e);
            return new ArrayList<String>();
        }
    }

    /**
     * This method returns the list of all the user names registed into the system.
     *
     * @return Return a List of strings holding the user identification names.
     */
    public List<String> getUsernameList() {
        try {
            return jcrTemplate.doExecuteWithSystemSession(new JCRCallback<List<String>>() {
                public List<String> doInJCR(JCRSessionWrapper session) throws RepositoryException {
                    List<String> users = new ArrayList<String>();
                    if (session.getWorkspace().getQueryManager() != null) {
                        String query = "SELECT [j:nodename] FROM [" + Constants.JAHIANT_USER + "] ORDER BY u.[j:nodename]";
                        Query q = session.getWorkspace().getQueryManager().createQuery(query, Query.JCR_SQL2);
                        QueryResult qr = q.execute();
                        RowIterator rows = qr.getRows();
                        while (rows.hasNext()) {
                            Row usersFolderNode = rows.nextRow();
                            String userName = usersFolderNode.getValue("j:nodename").getString();
                            if (!users.contains(userName)) {
                                users.add(userName);
                            }
                        }
                    }
                    return users;
                }
            });
        } catch (RepositoryException e) {
            logger.error("Error while retrieving user name list", e);
            return new ArrayList<String>();
        }
    }

    /**
     * Performs a login of the specified user.
     *
     * @param userKey      the user identifier defined in this service properties
     * @param userPassword the password of the user
     * @return boolean true if the login succeeded, false otherwise
     */
    public boolean login(String userKey, String userPassword) {
        return lookupUser(userKey).verifyPassword(userPassword);
    }

    /**
     * Load all the user data and attributes. On success a reference on the user
     * is returned, otherwise NULL is returned.
     *
     * @return a reference on a new created jahiaUser object.
     */
    public JCRUser lookupUserByKey(String userKey) {
        if (!userKey.startsWith("{")) {
            logger.warn("Expected userKey with provider prefix {jcr}, defaulting to looking up by name instead for parameter=["+userKey+"]... ");
            return lookupUser(userKey);
        }
        return lookupUser(StringUtils.substringAfter(userKey, "}"));
    }

    /**
     * Load all the user data and attributes. On success a reference on the user
     * is returned, otherwise NULL is returned.
     *
     * @return Return a reference on a new created jahiaUser object.
     */
    public JCRUser lookupUser(final String name) {
        if("".equals(name.trim())) {
            logger.error("Should not be looking for empty name user");
            return null;
        }
        try {
            if (cache.containsKey(name)) {
                JCRUser user = cache.get(name);
                return user != null && user.isExternal() ? null : user;
            }
            return jcrTemplate.doExecuteWithSystemSession(new JCRCallback<JCRUser>() {
                public JCRUser doInJCR(JCRSessionWrapper session) throws RepositoryException {
                    Node userNode = session.getNode("/users/" + name.trim());
                    if (!userNode.getProperty(JCRUser.J_EXTERNAL).getBoolean()) {
                        JCRUser user = new JCRUser(userNode.getIdentifier(), jcrTemplate);
                        cache.put(name, user);
                        return user;
                    }
                    return null;
                }
            });
        } catch (PathNotFoundException pnfe) {
            // This is expected in the case the user doesn't exist in the repository. We will simply return null.
        } catch (RepositoryException e) {
            logger.error("Error while looking up user by name " + name, e);
        }
        return null;
    }

    public JCRUser lookupExternalUser(final String name) {
        try {
            if (cache.containsKey(name)) {
                return cache.get(name);
            }
            return jcrTemplate.doExecuteWithSystemSession(new JCRCallback<JCRUser>() {
                public JCRUser doInJCR(JCRSessionWrapper session) throws RepositoryException {
                    Node userNode = session.getNode( "/users/" + name.trim());
                    if (userNode.getProperty(JCRUser.J_EXTERNAL).getBoolean()) {
                        JCRUser user = new JCRUser(userNode.getIdentifier(), jcrTemplate, true);
                        cache.put(name, user);
                        return user;
                    }
                    return null;
                }
            });
        } catch (PathNotFoundException pnfe) {
            // This is expected in the case the user doesn't exist in the repository. We will simply return null.
        } catch (RepositoryException e) {
            logger.error("Error while looking up external user by name " + name, e);
        }
        return null;
    }


    /**
     * Find users according to a table of name=value properties. If the left
     * side value is "*" for a property then it will be tested against all the
     * properties. ie *=test* will match every property that starts with "test"
     *
     * @param searchCriterias a Properties object that contains search criterias
     *                        in the format name,value (for example "*"="*" or "username"="*test*") or
     *                        null to search without criterias
     * @return Set a set of JahiaUser elements that correspond to those
     *         search criterias
     */
    public Set<JahiaUser> searchUsers(final Properties searchCriterias) {
        try {
            return jcrTemplate.doExecuteWithSystemSession(new JCRCallback<Set<JahiaUser>>() {
                public Set<JahiaUser> doInJCR(JCRSessionWrapper session) throws RepositoryException {
                    Set<JahiaUser> users = new HashSet<JahiaUser>();
                    if (session.getWorkspace().getQueryManager() != null) {
                        StringBuilder query = new StringBuilder("SELECT * FROM [" + Constants.JAHIANT_USER + "] as u WHERE u.[" + JCRUser.J_EXTERNAL + "] = 'false'");
                        if (searchCriterias != null && searchCriterias.size() > 0) {
                            // Avoid wildcard attribute
                            if (!(searchCriterias.containsKey(
                                    "*") && searchCriterias.size() == 1 && searchCriterias.getProperty("*").equals(
                                    "*"))) {
                                Iterator<Map.Entry<Object, Object>> objectIterator = searchCriterias.entrySet().iterator();
                                if (objectIterator.hasNext()) {
                                    query.append(" AND ");
                                }
                                while (objectIterator.hasNext()) {
                                    Map.Entry<Object, Object> entry = objectIterator.next();
                                    String propertyKey = (String) entry.getKey();
                                    if ("username".equals(propertyKey)) {
                                        propertyKey = "j:nodename";
                                    }
                                    String propertyValue = (String) entry.getValue();
                                    if ("*".equals(propertyValue)) {
                                        propertyValue = "%";
                                    } else {
                                        if (propertyValue.contains("*")) {
                                            propertyValue = propertyValue.replaceAll("\\*", "%");
                                        } else {
                                            propertyValue = propertyValue + "%";
                                        }
                                    }
                                    if ("*".equals(propertyKey)) {
                                        query.append("(CONTAINS(u.*,'" + propertyValue.replaceAll("%", "")
                                                + "') OR LOWER(u.[j:nodename]) LIKE '")
                                                .append(propertyValue.toLowerCase()).append("') ");
                                    } else {
                                        query.append("LOWER(u.[" + propertyKey + "])").append(
                                                " LIKE '").append(propertyValue.toLowerCase()).append("'");
                                    }
                                    if (objectIterator.hasNext()) {
                                        query.append(" OR ");
                                    }
                                }
                            }
                        }
                        query.append(" ORDER BY u.[j:nodename]");
                        if (logger.isDebugEnabled()) {
                            logger.debug(query.toString());
                        }
                        Query q = session.getWorkspace().getQueryManager().createQuery(query.toString(),
                                                                                       Query.JCR_SQL2);
                        QueryResult qr = q.execute();
                        NodeIterator ni = qr.getNodes();
                        while (ni.hasNext()) {
                            Node usersFolderNode = ni.nextNode();
                            users.add(new JCRUser(usersFolderNode.getIdentifier(), jcrTemplate));
                        }
                    }

                    return users;
                }
            });
        } catch (RepositoryException e) {
            logger.error("Error while searching for users", e);
            return new HashSet<JahiaUser>();
        }
    }

    /**
     * This method indicates that any internal cache for a provider should be
     * updated because the value has changed and needs to be transmitted to the
     * other nodes in a clustering environment.
     *
     * @param jahiaUser JahiaUser the user to be updated in the cache.
     */
    public void updateCache(JahiaUser jahiaUser) {
        updateCache(jahiaUser.getName());
    }

    /**
     * This method indicates that any internal cache for a provider should be
     * updated because the value has changed and needs to be transmitted to the
     * other nodes in a clustering environment.
     *
     * @param name the name of the user to be updated in the cache
     */
    public void updateCache(String name) {
        cache.remove(name);
    }

    /**
     * This function checks into the system if the username has already been
     * assigned to another user.
     *
     * @return Return true if the specified username has not been assigned yet,
     *         return false on any failure.
     */
    public boolean userExists(final String name) {
        try {
            return jcrTemplate.doExecuteWithSystemSession(new JCRCallback<Boolean>() {
                public Boolean doInJCR(JCRSessionWrapper session) throws RepositoryException {
                    if (session.getWorkspace().getQueryManager() != null) {
                        String query = "SELECT * FROM [" + Constants.JAHIANT_USER + "] as u WHERE u.[j:nodename] = '" + name + "' AND u.[" + JCRUser.J_EXTERNAL + "] = 'false'";
                        Query q = session.getWorkspace().getQueryManager().createQuery(query, Query.JCR_SQL2);
                        QueryResult qr = q.execute();
                        NodeIterator ni = qr.getNodes();
                        return ni.hasNext();
                    }
                    return false;
                }
            });
        } catch (RepositoryException e) {
            logger.error("Error when testing user existence", e);
            return false;
        }
    }

    public void start() throws JahiaInitializationException {
        if (cacheService != null) {
            cache = cacheService.createCacheInstance("JCR_USER_CACHE");
        }
    }

	private void checkRootUserPwd() {
		try {
			if (servletContext.getResource(ROOT_PWD_RESET_FILE) != null) {
				InputStream is = servletContext.getResourceAsStream(ROOT_PWD_RESET_FILE);
				try {
					String newPwd = IOUtils.toString(is);
					logger.info("Resetting root user password");
					JCRUser root = new JCRUser(JCRUser.ROOT_USER_UUID, jcrTemplate);
					root.setPassword(newPwd);
					logger.info("New root user password set.");
				} finally {
					IOUtils.closeQuietly(is);
					try {
						new File(servletContext.getRealPath(ROOT_PWD_RESET_FILE)).delete();
					} catch (Exception e) {
						logger.warn("Unable to delete " + ROOT_PWD_RESET_FILE
						        + " file after resetting root password", e);
					}
				}
			}
		} catch (Exception e) {
			logger.warn(e.getMessage(), e);
		}
	}

	public void stop() throws JahiaException {
        // do nothing
    }

	public void setServletContext(ServletContext servletContext) {
		this.servletContext = servletContext;
    }

	public void onApplicationEvent(ApplicationEvent event) {
        if (event instanceof ContextInitializedEvent) {
        	checkRootUserPwd();
        }
    }

}
