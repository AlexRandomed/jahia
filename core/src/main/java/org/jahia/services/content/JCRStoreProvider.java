/**
 * This file is part of Jahia: An integrated WCM, DMS and Portal Solution
 * Copyright (C) 2002-2009 Jahia Solutions Group SA. All rights reserved.
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
package org.jahia.services.content;

import org.apache.jackrabbit.rmi.server.ServerAdapterFactory;
import org.apache.jackrabbit.util.ISO9075;
import org.jahia.api.Constants;
import org.jahia.bin.Jahia;
import org.jahia.exceptions.JahiaInitializationException;
import org.jahia.services.sites.JahiaSite;
import org.jahia.services.sites.JahiaSitesService;
import org.jahia.services.usermanager.JahiaGroupManagerService;
import org.jahia.services.usermanager.JahiaUser;
import org.jahia.services.usermanager.JahiaUserManagerService;
import org.jahia.services.usermanager.jcr.JCRUser;
import org.jahia.services.content.nodetypes.NodeTypeRegistry;
import org.jahia.services.content.nodetypes.ExtendedPropertyDefinition;
import org.jahia.jaas.JahiaLoginModule;
import org.xml.sax.ContentHandler;
import org.xml.sax.SAXException;

import javax.jcr.*;
import javax.jcr.nodetype.NodeType;
import javax.jcr.nodetype.PropertyDefinition;
import javax.jcr.observation.ObservationManager;
import javax.jcr.query.Query;
import javax.jcr.query.QueryManager;
import javax.jcr.query.QueryResult;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.naming.Reference;
import javax.naming.StringRefAddr;
import javax.naming.spi.ObjectFactory;
import javax.servlet.ServletRequest;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.MalformedURLException;
import java.rmi.Naming;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;

/**
 * Created by IntelliJ IDEA.
 * User: toto
 * Date: 20 nov. 2007
 * Time: 18:09:21
 * To change this template use File | Settings | File Templates.
 */
public class JCRStoreProvider {

    private static org.apache.log4j.Logger logger =
        org.apache.log4j.Logger.getLogger(JCRStoreProvider.class);

    private String key;
    private String mountPoint;
    private String webdavPath;
    private String relativeRoot = "";

    private String repositoryName;
    private String factory;
    private String url;
    private String workspace;

    protected String systemUser;
    protected String systemPassword;
    protected String guestUser;
    protected String guestPassword;

    protected String authenticationType = null;

    protected String rmibind;

    private boolean running;
    private Map<String,List<DefaultEventListener>> listeners;

    private JahiaUserManagerService userManagerService;
    private JahiaGroupManagerService groupManagerService;
    private JahiaSitesService sitesService;

    private JCRStoreService service;
    private JCRPublicationService publicationService;

    private JCRSessionFactory sessionFactory;
    protected Repository repo = null;

    private boolean isMainStorage = false;
    private boolean isDynamicallyMounted = false;
    private boolean initialized = false;

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public String getMountPoint() {
        return mountPoint;
    }

    public void setMountPoint(String mountPoint) {
        this.mountPoint = mountPoint;
    }

    public String getWebdavPath() {
        return webdavPath;
    }

    public String getRelativeRoot() {
        return relativeRoot;
    }

    public void setRelativeRoot(String relativeRoot) {
        this.relativeRoot = relativeRoot;
    }

    public int getDepth() {
        if (mountPoint.equals("/")) {
            return 0;
        }
        return mountPoint.split("/").length - 1;
    }

    public void setWebdavPath(String webdavPath) {
        // TODO find better way to handle ROOT context and doubnle slash problem
        this.webdavPath = webdavPath;
        if (webdavPath != null) {
            if ("/".equals(webdavPath)) {
                this.webdavPath = "";
            } else if (webdavPath.startsWith("//")) {
                this.webdavPath = webdavPath.substring(1);
            }
        }
    }

    public String getHttpPath() {
        if ("/".equals(Jahia.getContextPath())) {
            return "/files";
        }
        return Jahia.getContextPath() + "/files";
    }

    public String getRepositoryName() {
        return repositoryName;
    }

    public void setRepositoryName(String repositoryName) {
        this.repositoryName = repositoryName;
    }

    public String getFactory() {
        return factory;
    }

    public void setFactory(String factory) {
        this.factory = factory;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public void setSystemUser(String user) {
        this.systemUser = user;
        if (authenticationType == null) {
            authenticationType = "shared";
        }
    }

    public void setSystemPassword(String password) {
        this.systemPassword = password;
    }

    public void setGuestUser(String user) {
        this.guestUser = user;
    }

    public void setGuestPassword(String password) {
        this.guestPassword = password;
    }

    public String getAuthenticationType() {
        return authenticationType;
    }

    public void setAuthenticationType(String authenticationType) {
        this.authenticationType = authenticationType;
    }

    public String getRmibind() {
        return rmibind;
    }

    public void setRmibind(String rmibind) {
        this.rmibind = rmibind;
    }

    public Map<String, List<DefaultEventListener>> getListeners() {
        return listeners;
    }

    public void setListeners(Map<String, List<DefaultEventListener>> listeners) {
        this.listeners = listeners;
    }

    public JahiaUserManagerService getUserManagerService() {
        return userManagerService;
    }

    public void setUserManagerService(JahiaUserManagerService userManagerService) {
        this.userManagerService = userManagerService;
    }

    public JahiaGroupManagerService getGroupManagerService() {
        return groupManagerService;
    }

    public void setGroupManagerService(JahiaGroupManagerService groupManagerService) {
        this.groupManagerService = groupManagerService;
    }

    public JahiaSitesService getSitesService() {
        return sitesService;
    }

    public void setSitesService(JahiaSitesService sitesService) {
        this.sitesService = sitesService;
    }

    public JCRStoreService getService() {
        return service;
    }

    public void setService(JCRStoreService service) {
        this.service = service;
    }

    public JCRSessionFactory getSessionFactory() {
        return sessionFactory;
    }

    public void setSessionFactory(JCRSessionFactory sessionFactory) {
        this.sessionFactory = sessionFactory;
    }

    public JCRPublicationService getPublicationService() {
        return publicationService;
    }

    public void setPublicationService(JCRPublicationService publicationService) {
        this.publicationService = publicationService;
    }

    public void start() throws JahiaInitializationException {
        try {
            String tmpAuthenticationType = authenticationType;
            authenticationType = "shared";

            getSessionFactory().addProvider(getKey(), getMountPoint(), this);

            initNodeTypes();
            initObservers();
            initContent();

            if (rmibind != null) {
                try {
                    Naming.rebind(rmibind, new ServerAdapterFactory().getRemoteRepository(repo));
                } catch (MalformedURLException e) {
                } catch (RemoteException e) {
                }
            }

            initialized = true;
            authenticationType = tmpAuthenticationType;
        } catch (Exception e){
            logger.error("Repository init error",e);
            throw  new JahiaInitializationException("Repository init error",e) ;
        }
    }

    protected void initNodeTypes() throws RepositoryException, IOException {
//        JahiaUser root = getGroupManagerService().getAdminUser(0);
        if (canRegisterCustomNodeTypes()) {
            Session session = getSystemSession();
            try {
                Workspace workspace = session.getWorkspace();
                registerCustomNodeTypes(workspace);
                session.save();
            } catch (RepositoryException e) {
                logger.error("Cannot register nodetypes",e);
            } finally {
                session.logout();
            }
        }
    }

    protected void initObservers() throws RepositoryException {
        if (listeners != null) {
            for (String ws : listeners.keySet()) {
                List<DefaultEventListener> l = listeners.get(ws);

                // This session must not be released
                final Session session = getSystemSession(null,ws);
                final Workspace workspace = session.getWorkspace();

                ObservationManager observationManager = workspace.getObservationManager();
                for (DefaultEventListener listener : l) {
                    listener.setProvider(this);
                    listener.setWorkspace(ws);
                    observationManager.addEventListener(listener, listener.getEventTypes(), listener.getPath(), true, null, listener.getNodeTypes(), false);
                }

                // The thread should always checks if the session is still alive and reconnect it if lost
                running = true;
                Thread t = new Thread() {
                    public void run() {
                        while (isRunning() && session.isLive()) {
                            try {
                                session.refresh(false);
                            } catch (RepositoryException e) {
                                if (logger != null) logger.error(e.getMessage(), e);
                            }
                            try {
                                Thread.sleep(5000);
                            } catch (InterruptedException e) {
                                // ignore
                            }
                        }
                        if (logger != null) logger.info("System session closed, deregister listeners");
                    }
                };
                t.start();
            }
        }
    }

    protected void initContent() throws RepositoryException, IOException {
        if ("/".equals(mountPoint)) {
            Session session = getSystemSession();
            FileInputStream stream = null;
            try {
                Node rootNode = session.getRootNode();
                if (!rootNode.hasNode(Constants.CONTENT)) {
                    initializeAcl(session);

                    stream = new FileInputStream(
                            org.jahia.settings.SettingsBean.getInstance().getJahiaEtcDiskPath() + "/repository/root.xml");
                    session.importXML("/", stream,ImportUUIDBehavior.IMPORT_UUID_COLLISION_THROW);
                    Node userNode = (Node) session.getItem("/"+Constants.CONTENT+"/users");
                    NodeIterator nodeIterator = userNode.getNodes();
                    while (nodeIterator.hasNext()) {
                        Node node = (Node) nodeIterator.next();
                        if(!"guest".equals(node.getName())) {
                            JCRNodeWrapperImpl.changePermissions(node, "u:" + node.getName(), "rw");
                        }
                    }
                }
                session.save();
            } finally {
                session.logout();
                if(stream!=null) {
                    stream.close();
                }
            }
        }
    }

    public void stop() {
        running = false;
        getSessionFactory().removeProvider(key);
    }

    public boolean isRunning() {
        return running;
    }

    public void deployDefinitions(String systemId) {
        try {
            repo = getRepository();
            JahiaUser root = getGroupManagerService().getAdminUser(0);
            Session session = getSystemSession(root.getUsername());
            try {
                Workspace workspace = session.getWorkspace();

                try {
                    registerCustomNodeTypes(systemId, workspace);
                } catch (RepositoryException e) {
                    logger.error("Cannot register nodetypes",e);
                }
                session.save();
            } finally {
                session.logout();
            }
        } catch (Exception e){
            logger.error("Repository init error",e);
        }
    }

    public synchronized Repository getRepository(){
        if (repo == null) {
            if (repositoryName != null) {
                repo = getRepositoryByJNDI();
                if (rmibind != null) {
                    try {
                        Naming.rebind(rmibind, new ServerAdapterFactory().getRemoteRepository(repo));
                    } catch (MalformedURLException e) {
                    } catch (RemoteException e) {
                    }
                }
                return repo;
            } else if (factory != null && url != null) {
                repo = getRepositoryByRMI();
            }
        }
        return repo;
    }

    public void setRepository(Repository repo) {
        this.repo = repo;
    }

    protected Repository getRepositoryByJNDI() {
        try {
            Hashtable<String, String> env = new Hashtable<String, String>();
            InitialContext initctx = new InitialContext(env);
             // ((ObjectFactory)Class.forName(((Reference) initctx.lookup(repositoryName)).getFactoryClassName()).newInstance()).getObjectInstance(((Reference) initctx.lookup(repositoryName)), null,null,null)
            repo = (Repository) initctx.lookup(repositoryName);
            logger.info("Repository " + getKey() + " acquired via JNDI");
            return repo;
        } catch (NamingException e) {
            logger.error("Cannot get by JNDI",e);
        }
        return null;
    }

    protected Repository getRepositoryByRMI() {
        try {
            Class<? extends ObjectFactory> factoryClass = Class.forName(factory).asSubclass(ObjectFactory.class);
            ObjectFactory factory = (ObjectFactory) factoryClass.newInstance();
            repo = (Repository) factory.getObjectInstance(new Reference(Repository.class.getName(), new StringRefAddr("url", url)),null,null,null);
            logger.info("Repository " + getKey() + " acquired via RMI");
            return repo;
        } catch (Exception e) {
            logger.error("Cannot get by RMI",e);
        }
        return null;
    }

    public Session getSession(Credentials credentials, String workspace) throws RepositoryException {
        Session s;

        if (credentials instanceof SimpleCredentials) {
            String username = ((SimpleCredentials)credentials).getUserID();

            if ("shared".equals(authenticationType)) {
                if (username.startsWith(" system ") || guestUser == null) {
                    credentials = JahiaLoginModule.getSystemCredentials();
                } else {
                    credentials = JahiaLoginModule.getGuestCredentials();
                }
                username = ((SimpleCredentials)credentials).getUserID();
            }

            if (username.startsWith(" system ") && systemUser != null) {
                if (systemPassword != null) {
                    credentials = new SimpleCredentials(systemUser, systemPassword.toCharArray());
                } else {
                    credentials = JahiaLoginModule.getCredentials(systemUser);
                }
            } else if (username.startsWith(" guest ") && guestUser != null) {
                if (guestPassword != null) {
                    credentials = new SimpleCredentials(guestUser, guestPassword.toCharArray());
                } else {
                    credentials = JahiaLoginModule.getCredentials(guestUser);
                }
            } else if ("storedPasswords".equals(authenticationType)) {
                JahiaUser user = userManagerService.lookupUser(username);
                if (user.getProperty("storedUsername_"+getKey()) != null) {
                    username = user.getProperty("storedUsername_"+getKey());
                }
                String pass = user.getProperty("storedPassword_"+getKey());
                if (pass != null) {                    
                    credentials = new SimpleCredentials(username, pass.toCharArray());
                } else {
                    if (guestPassword != null) {
                        credentials = new SimpleCredentials(guestUser, guestPassword.toCharArray());
                    } else {
                        credentials = JahiaLoginModule.getCredentials(guestUser);
                    }
                }
            }
            logger.debug("Login for "+getKey() + " as " + ((SimpleCredentials)credentials).getUserID());
        }

        s = getRepository().login(credentials,workspace);

        return s;
    }

    public NodeType getNodeType(String name) throws RepositoryException {
        Session session = getSystemSession();
        try {
            return session.getWorkspace().getNodeTypeManager().getNodeType(name);
        } finally {
            session.logout();
        }
    }

    public JCRItemWrapper getItemWrapper(Item item, JCRSessionWrapper session) throws RepositoryException {
        if (item.isNode()) {
            return getNodeWrapper((Node) item, session);
        } else {
            return getPropertyWrapper((Property) item, session);
        }
    }

    public JCRNodeWrapper getNodeWrapper(Node objectNode, JCRSessionWrapper session) {
        return service.decorate(new JCRNodeWrapperImpl(objectNode, session, this));
    }

    public JCRPropertyWrapper getPropertyWrapper(Property prop, JCRSessionWrapper session) throws RepositoryException {
        PropertyDefinition def = prop.getDefinition();
        ExtendedPropertyDefinition epd = NodeTypeRegistry.getInstance().getNodeType(def.getDeclaringNodeType().getName()).getDeclaredPropertyDefinitionsAsMap().get(def.getName());
        return new JCRPropertyWrapperImpl(new JCRNodeWrapperImpl(prop.getNode(), session, this), prop, session, this, epd);
    }

    protected boolean canRegisterCustomNodeTypes() {
        return false;
    }

    protected void registerCustomNodeTypes(Workspace ws) throws IOException, RepositoryException {
        return;
    }

    protected void registerCustomNodeTypes(String systemId, Workspace ws) throws IOException, RepositoryException {
        return;
    }

    protected void initializeAcl(Session session) throws RepositoryException, IOException {
        return;
    }


    public void deployNewSite(JahiaSite site, JahiaUser user) throws RepositoryException {
        Session session = getSystemSession(user.getUsername());
        try {
            if (session.getWorkspace().getQueryManager() != null) {
                Query q = session.getWorkspace().getQueryManager().createQuery("SELECT * FROM [jmix:virtualsitesFolder]", Query.JCR_SQL2);
                QueryResult qr = q.execute();
                NodeIterator ni = qr.getNodes();
                try {
                    while (ni.hasNext()) {
                        Node sitesFolder = ni.nextNode();
                        String options = "";
                        if (sitesFolder.hasProperty("j:virtualsitesFolderConfig")) {
                            options = sitesFolder.getProperty("j:virtualsitesFolderConfig").getString();
                        }

                        Node f = getPathFolder(sitesFolder, site.getSiteKey(), options);
                        try {
                            f.getNode(site.getSiteKey());
                        } catch (PathNotFoundException e) {
                            if (sitesFolder.hasProperty("j:virtualsitesFolderSkeleton")) {
                                session.importXML(f.getPath(), new FileInputStream(org.jahia.settings.SettingsBean.getInstance().getJahiaEtcDiskPath() + "/repository/"+ sitesFolder.getProperty("j:virtualsitesFolderSkeleton").getString()),ImportUUIDBehavior.IMPORT_UUID_CREATE_NEW);
                                session.move(f.getPath()+"/site", f.getPath()+"/"+site.getSiteKey());
                            } else {
                                f.addNode(site.getSiteKey(), Constants.JAHIANT_VIRTUALSITE);
                            }

                            Node siteNode = f.getNode(site.getSiteKey());
                            siteNode.setProperty("j:description", site.getDescr());
                            siteNode.setProperty("j:serverName", site.getServerName());

//                            try {
//                                Node home = siteNode.getNode("home");
//                                home.setProperty("j:defaultTemplate", ServicesRegistry.getInstance().getJahiaTemplateManagerService().getTemplatePackage(site.getTemplatePackageName()).getHomePageName());
//                            } catch (RepositoryException e1) {
//                                e1.printStackTrace();
//                            }

                            session.save();
                            publicationService.publish(siteNode.getPath(), Constants.EDIT_WORKSPACE, Constants.LIVE_WORKSPACE, null, user, false, true);
                        }
                    }
                } catch (IOException e) {
                    logger.error(e.getMessage(), e);
                }
            }
        } finally {
            session.logout();
        }
    }

    public void deployExternalUser(String username, String providerName) throws RepositoryException {
        Session session = getSystemSession(username);
        try {
            if (session.getWorkspace().getQueryManager() != null) {
            Query q = session.getWorkspace().getQueryManager().createQuery("SELECT * FROM [jmix:usersFolder]", Query.JCR_SQL2);
            QueryResult qr = q.execute();
            NodeIterator ni = qr.getNodes();
            try {
                while (ni.hasNext()) {
                    Node usersFolderNode = ni.nextNode();
                    String options = "";
                    if (usersFolderNode.hasProperty("j:usersFolderConfig")) {
                        options = usersFolderNode.getProperty("j:usersFolderConfig").getString();
                    }

                    Node f = getPathFolder(usersFolderNode, username, options);

                    try {
                        f.getNode(username);
                    } catch (PathNotFoundException e) {
                        synchronized (this) {
                            try {
                                f.getNode(username);
                            } catch (PathNotFoundException ee) {
                                try {
                                    Node userNode;
                                    if (usersFolderNode.hasProperty("j:usersFolderSkeleton")) {
                                        session.importXML(f.getPath(), new FileInputStream(org.jahia.settings.SettingsBean.getInstance().getJahiaEtcDiskPath() + "/repository/" + usersFolderNode.getProperty("j:usersFolderSkeleton").getString()),ImportUUIDBehavior.IMPORT_UUID_CREATE_NEW);
                                        session.move(f.getPath()+"/user", f.getPath()+"/"+username);
                                        userNode = f.getNode(username);
                                        userNode.setProperty(JCRUser.J_EXTERNAL,true);
                                        userNode.setProperty(JCRUser.J_EXTERNAL_SOURCE,providerName);
                                        JCRNodeWrapperImpl.changePermissions(userNode, "u:"+username, "rw");
                                    } else {
                                        userNode = f.addNode(username, Constants.JAHIANT_USER);
                                        JCRNodeWrapperImpl.changePermissions(userNode, "u:"+username, "rw");
                                    }
                                    session.save();
                                    publicationService.publish(userNode.getPath(), Constants.EDIT_WORKSPACE, Constants.LIVE_WORKSPACE, null, Jahia.getThreadParamBean().getUser(), true, true);
                                } catch (RepositoryException e1) {
                                    logger.error("Cannot save", e1);
                                }
                            }
                        }
                    }
                }
            } catch (IOException e) {
                logger.error(e.getMessage(), e);
            }
            }
        } finally {
            session.logout();
        }
    }

    private Node getPathFolder(Node root, String name, String options) throws RepositoryException {
        Node result = root;
        if (options.contains("initials")) {
            String s = "" + Character.toUpperCase(name.charAt(0));
            if (!result.hasNode(s)) {
                result = result.addNode(s,Constants.JAHIANT_SYSTEMFOLDER);
            } else {
                result = result.getNode(s);
            }
        }
        return result;
    }

    public List<JCRNodeWrapper> getUserFolders(String site, JahiaUser user) throws RepositoryException {
        String username = ISO9075.encode(encodeInternalName(user.getUsername()));
        String sql = "select * from [jnt:user] as user where user.[j:nodename]= '"+username+"'";

        if (site != null) {
            site = ISO9075.encode(encodeInternalName(site));
            sql = "select user from [jnt:user] as user right outer join [jnt:virtualsite] as site on isdescendantnode(user,site) where user.[j:nodename]= '"+username+ "' and site:[j:nodename] = '"+site+"'";
        }
        List<JCRNodeWrapper> results = queryFolders(sessionFactory.getCurrentUserSession(), sql);
        if (site != null) {
            results.addAll(getUserFolders(null, user));
        }
        return results;
    }

    public List<JCRNodeWrapper> getImportDropBoxes(String site, JahiaUser user) throws RepositoryException {
        String username = ISO9075.encode(encodeInternalName(user.getUsername()));
        String sql = "select imp.* from [jnt:importDropBox] as imp right outer join [jnt:user] as user on ischildnode(imp,user) where user.[j:nodename]= '"+username+"'";

        if (site != null) {
            site = ISO9075.encode(encodeInternalName(site));
            sql = "select imp.* from jnt:importDropBox as imp right outer join [jnt:user] as user on ischildnode(imp,user) right outer join [jnt:virtualsite] as site on isdescendantnode(imp,site) where user.[j:nodename]= '"+username+ "' and site.[j:nodename] = '"+site+"'";
        }

        List<JCRNodeWrapper> results = queryFolders(sessionFactory.getCurrentUserSession(), sql);
        if (site != null) {
            results.addAll(getImportDropBoxes(null, user));
        }
        return results;
    }

    public List<JCRNodeWrapper> getSiteFolders(String site, JahiaUser user) throws RepositoryException {
        site = ISO9075.encode(encodeInternalName(site));
        String xp = "select * from [jnt:virtualsite] as site where site.[j:nodename] = '"+site+"'";

        return queryFolders(sessionFactory.getCurrentUserSession(), xp);
    }

    private List<JCRNodeWrapper> queryFolders(JCRSessionWrapper session, String sql) throws RepositoryException {
        List<JCRNodeWrapper> results = new ArrayList<JCRNodeWrapper>();
        QueryManager queryManager = session.getProviderSession(this).getWorkspace().getQueryManager();
        if (queryManager != null) {
            Query q = queryManager.createQuery(sql, Query.JCR_SQL2);
            QueryResult qr = q.execute();
            NodeIterator ni = qr.getNodes();
            while (ni.hasNext()) {
                Node folder = ni.nextNode();
                results.add(getNodeWrapper(folder, session));
            }
        }
        return results;
    }

    public String getAbsoluteContextPath(ServletRequest request) {
        StringBuffer serverUrlBuffer = new StringBuffer(request.getScheme());
        serverUrlBuffer.append("://");
        serverUrlBuffer.append(request.getServerName());
        serverUrlBuffer.append(":");
        serverUrlBuffer.append(request.getServerPort());
        return serverUrlBuffer.toString();
    }

    public boolean isMainStorage() {
        return isMainStorage;
    }

    public void setMainStorage(boolean mainStorage) {
        isMainStorage = mainStorage;
    }

    public boolean isDynamicallyMounted() {
        return isDynamicallyMounted;
    }

    public void setDynamicallyMounted(boolean dynamicallyMounted) {
        isDynamicallyMounted = dynamicallyMounted;
    }

    public boolean isExportable() {
        return true;
    }
    
    public void export(String path, ContentHandler ch, JahiaUser user) {
        exportDocumentView(path, ch, user, true);
    }

    public void exportDocumentView(String path, ContentHandler ch, JahiaUser user, boolean noRecurse) {
        try {
            getCurrentUserSession().exportDocumentView(path, ch, true, noRecurse);
        } catch (SAXException e) {
            logger.error(e.getMessage(), e);
        } catch (RepositoryException e) {
            logger.error(e.getMessage(), e);
        }
    }

    private void dump (Node n) throws RepositoryException {
        System.out.println(n.getPath());
        PropertyIterator pit=n.getProperties();
        while (pit.hasNext()) {
            Property p=pit.nextProperty();
            System.out.print(p.getPath()+"=");
            if (p.getDefinition().isMultiple()) {
                Value[] values = p.getValues();
                for (int i = 0; i < values.length; i++) {
                    Value value = values[i];
                    System.out.print(value+",");
                }
                System.out.println("");
            } else {
                System.out.println(p.getValue());
            }
        }
        NodeIterator nit=n.getNodes();
        while (nit.hasNext()) {
            Node cn=nit.nextNode();
            if (!cn.getName().startsWith("jcr:")) {
                dump (cn);
            }
        }
    }

    public QueryManager getQueryManager(JCRSessionWrapper session) {
        QueryManager queryManager = null;

        try {
            queryManager = session.getProviderSession(JCRStoreProvider.this).getWorkspace().getQueryManager();
            if (queryManager instanceof org.apache.jackrabbit.core.query.QueryManagerImpl){
                queryManager =
                        new JCRStoreQueryManagerAdapter((org.apache.jackrabbit.core.query.QueryManagerImpl)queryManager);
            }
        } catch (RepositoryException e) {
            logger.error("Repository error", e);
        }
        return queryManager;
    }
    
    public ValueFactory getValueFactory(JahiaUser user) {
        ValueFactory valueFactory = null;
        try {
            Session session = getCurrentUserSession();
            valueFactory = session.getValueFactory();
        } catch (RepositoryException e) {
            logger.error("Repository error", e);
        }
        return valueFactory;
    }

    public String encodeInternalName(String name) {
        return JCRContentUtils.encodeInternalName(name);
    }

    public String decodeInternalName(String name) {
        return JCRContentUtils.decodeInternalName(name);
    }

    public Session getCurrentUserSession() throws RepositoryException {
        return sessionFactory.getCurrentUserSession().getProviderSession(this);
    }

    public Session getCurrentUserSession(String workspace) throws RepositoryException {
        return sessionFactory.getCurrentUserSession(workspace).getProviderSession(this);
    }

    public Session getSystemSession() throws RepositoryException {
        return sessionFactory.getSystemSession().getProviderSession(this);
    }

    public Session getSystemSession(String user) throws RepositoryException {
        return sessionFactory.getSystemSession(user).getProviderSession(this);
    }

    public Session getSystemSession(String user, String workspace) throws RepositoryException {
        return sessionFactory.getSystemSession(user, workspace).getProviderSession(this);
    }

    public boolean isInitialized() {
        return initialized;
    }

}
