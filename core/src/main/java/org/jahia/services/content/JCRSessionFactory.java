/**
 * ==========================================================================================
 * =                        DIGITAL FACTORY v7.0 - Community Distribution                   =
 * ==========================================================================================
 *
 *     Rooted in Open Source CMS, Jahia's Digital Industrialization paradigm is about
 *     streamlining Enterprise digital projects across channels to truly control
 *     time-to-market and TCO, project after project.
 *     Putting an end to "the Tunnel effect", the Jahia Studio enables IT and
 *     marketing teams to collaboratively and iteratively build cutting-edge
 *     online business solutions.
 *     These, in turn, are securely and easily deployed as modules and apps,
 *     reusable across any digital projects, thanks to the Jahia Private App Store Software.
 *     Each solution provided by Jahia stems from this overarching vision:
 *     Digital Factory, Workspace Factory, Portal Factory and eCommerce Factory.
 *     Founded in 2002 and headquartered in Geneva, Switzerland,
 *     Jahia Solutions Group has its North American headquarters in Washington DC,
 *     with offices in Chicago, Toronto and throughout Europe.
 *     Jahia counts hundreds of global brands and governmental organizations
 *     among its loyal customers, in more than 20 countries across the globe.
 *
 *     For more information, please visit http://www.jahia.com
 *
 * JAHIA'S DUAL LICENSING - IMPORTANT INFORMATION
 * ============================================
 *
 *     Copyright (C) 2002-2014 Jahia Solutions Group SA. All rights reserved.
 *
 *     THIS FILE IS AVAILABLE UNDER TWO DIFFERENT LICENSES:
 *     1/GPL OR 2/JSEL
 *
 *     1/ GPL
 *     ==========================================================
 *
 *     IF YOU DECIDE TO CHOSE THE GPL LICENSE, YOU MUST COMPLY WITH THE FOLLOWING TERMS:
 *
 *     "This program is free software; you can redistribute it and/or
 *     modify it under the terms of the GNU General Public License
 *     as published by the Free Software Foundation; either version 2
 *     of the License, or (at your option) any later version.
 *
 *     This program is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 *     GNU General Public License for more details.
 *
 *     You should have received a copy of the GNU General Public License
 *     along with this program; if not, write to the Free Software
 *     Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301, USA.
 *
 *     As a special exception to the terms and conditions of version 2.0 of
 *     the GPL (or any later version), you may redistribute this Program in connection
 *     with Free/Libre and Open Source Software ("FLOSS") applications as described
 *     in Jahia's FLOSS exception. You should have received a copy of the text
 *     describing the FLOSS exception, and it is also available here:
 *     http://www.jahia.com/license"
 *
 *     2/ JSEL - Commercial and Supported Versions of the program
 *     ==========================================================
 *
 *     IF YOU DECIDE TO CHOOSE THE JSEL LICENSE, YOU MUST COMPLY WITH THE FOLLOWING TERMS:
 *
 *     Alternatively, commercial and supported versions of the program - also known as
 *     Enterprise Distributions - must be used in accordance with the terms and conditions
 *     contained in a separate written agreement between you and Jahia Solutions Group SA.
 *
 *     If you are unsure which license is appropriate for your use,
 *     please contact the sales department at sales@jahia.com.
 */
package org.jahia.services.content;

import org.apache.commons.collections.map.UnmodifiableMap;
import org.apache.jackrabbit.core.security.JahiaLoginModule;
import org.jahia.jaas.JahiaPrincipal;
import org.jahia.services.usermanager.JahiaUser;
import org.jahia.services.usermanager.JahiaUserManagerService;
import org.jahia.services.usermanager.jcr.JCRUser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.validation.beanvalidation.LocalValidatorFactoryBean;
import org.springframework.web.context.ServletContextAware;

import javax.jcr.*;
import javax.security.auth.Subject;
import javax.security.auth.callback.*;
import javax.servlet.ServletContext;
import java.io.IOException;
import java.util.*;

/**
 * The entry point into the content repositories provided by the <code>JCRStoreProvider</code> list.
 *
 * Instead of using this class for creating and using sessions, please rather use the JCRTemplate.
 *
 * @author toto
 * @see JCRTemplate
 */
public class JCRSessionFactory implements Repository, ServletContextAware {

    public static final String DEFAULT_PROVIDER_KEY = "default";

    private static transient Logger logger = LoggerFactory.getLogger(JCRSessionFactory.class);

    protected ThreadLocal<Map<String, Map<String, JCRSessionWrapper>>> userSession = new ThreadLocal<Map<String, Map<String, JCRSessionWrapper>>>();
    private NamespaceRegistryWrapper namespaceRegistry;
    private Map<String, String> descriptors = new HashMap<String, String>();
    private JahiaUserManagerService userService;
    private Map<String, JCRStoreProvider> providers = new HashMap<String, JCRStoreProvider>();
    private List<JCRStoreProvider> providerList = new LinkedList<JCRStoreProvider>();
    private SortedMap<String, JCRStoreProvider> mountPoints;
    private String servletContextAttributeName;
    private ServletContext servletContext;
    private ThreadLocal<JahiaUser> currentUser = new ThreadLocal<JahiaUser>();
    private ThreadLocal<Locale> currentLocale = new ThreadLocal<Locale>();
    private ThreadLocal<Locale> fallbackLocale = new ThreadLocal<Locale>();
    private ThreadLocal<JahiaUser> currentAliasedUser = new ThreadLocal<JahiaUser>();
    private ThreadLocal<String> currentServletPath = new ThreadLocal<String>();
    private ThreadLocal<Calendar> currentPreviewDate = new ThreadLocal<Calendar>();
    private LocalValidatorFactoryBean validatorFactoryBean;

    private JCRSessionFactory() {
    }

    public void start() {
        Comparator<String> invertedStringComparator = new Comparator<String>() {
            public int compare(String s1, String s2) {
                return s2.compareTo(s1);
            }
        };
        synchronized (this) {
            this.mountPoints = new TreeMap<String, JCRStoreProvider>(invertedStringComparator);
        }
        namespaceRegistry = new NamespaceRegistryWrapper();

        if ((servletContextAttributeName != null) &&
                (servletContext != null)) {
            servletContext.setAttribute(servletContextAttributeName, this);
        }
    }

    public void setServletContextAttributeName(String servletContextAttributeName) {
        this.servletContextAttributeName = servletContextAttributeName;
    }

    public void setServletContext(ServletContext servletContext) {
        this.servletContext = servletContext;
    }

    public void setDescriptors(Map<String, String> descriptors) {
        this.descriptors = descriptors;
    }

    public void setUserService(JahiaUserManagerService userService) {
        this.userService = userService;
    }


    public JCRSessionWrapper getCurrentUserSession() throws RepositoryException {
        return getCurrentUserSession(null);
    }

    public JCRSessionWrapper getCurrentUserSession(String workspace) throws RepositoryException {
        return getCurrentUserSession(workspace, null);
    }

    public JCRSessionWrapper getCurrentUserSession(String workspace, Locale locale) throws RepositoryException {
        return getCurrentUserSession(workspace, locale, locale != null ? getFallbackLocale() : null);
    }

    public JCRSessionWrapper getCurrentUserSession(String workspace, Locale locale, Locale fallbackLocale) throws RepositoryException {
        // thread user session might be initialized/closed in an HTTP filter, instead of keeping it
        Map<String, Map<String, JCRSessionWrapper>> smap = userSession.get();
        if (smap == null) {
            smap = new HashMap<String, Map<String, JCRSessionWrapper>>();
        }
        userSession.set(smap);
        String username;

        if (getCurrentUser() == null) {
            logger.error("Null thread user");
            throw new RepositoryException("Null thread user");
        }

        JahiaUser user = getCurrentUser();

        if (JahiaUserManagerService.isGuest(user)) {
            username = JahiaLoginModule.GUEST;
        } else {
            username = user.getUsername();
        }

        Map<String, JCRSessionWrapper> wsMap = smap.get(username);
        if (wsMap == null) {
            wsMap = new HashMap<String, JCRSessionWrapper>();
            smap.put(username, wsMap);
        }

        if (workspace == null) {
            workspace = "default";
        }

        String localeString = "default";
        if (locale != null) {
            localeString = locale.toString();
        }

        final String key = workspace + "-" + localeString + "-" + fallbackLocale;
        JCRSessionWrapper s = wsMap.get(key);

        if (s == null || !s.isLive()) {
            if (!JahiaLoginModule.GUEST.equals(username)) {
                s = login(JahiaLoginModule.getCredentials(username), workspace, locale, fallbackLocale);
                // should be done somewhere else, call can be quite expensive
                if (!(user instanceof JCRUser)) {
                    mountPoints.get("/").deployExternalUser(user);
                }
            } else {
                s = login(JahiaLoginModule.getGuestCredentials(), workspace, locale, fallbackLocale);
            }
            s.setCurrentUserSession(true);
            wsMap.put(key, s);
            //} else {
            // In cluster, this will perform a cluster node sync, which is an expensive operation.
            //    s.refresh(true);
        }
        return s;
    }

    protected JCRSessionWrapper getSystemSession() throws RepositoryException {
        return login(JahiaLoginModule.getSystemCredentials());
    }

    protected JCRSessionWrapper getSystemSession(String username) throws RepositoryException {
        return login(JahiaLoginModule.getSystemCredentials(username));
    }

    protected JCRSessionWrapper getSystemSession(String username, String workspace) throws RepositoryException {
        return login(JahiaLoginModule.getSystemCredentials(username), workspace);
    }

    protected JCRSessionWrapper getSystemSession(String username, String workspace, Locale locale) throws RepositoryException {
        return login(JahiaLoginModule.getSystemCredentials(username), workspace, locale, locale != null ? getFallbackLocale() : null);
    }

    protected JCRSessionWrapper getUserSession(String username, String workspace) throws RepositoryException {
        return login(JahiaLoginModule.getCredentials(username), workspace);
    }

    protected JCRSessionWrapper getUserSession(String username, String workspace, Locale locale) throws RepositoryException {
        return login(JahiaLoginModule.getCredentials(username), workspace, locale, locale != null ? getFallbackLocale() : null);
    }

    public String[] getDescriptorKeys() {
        return descriptors.keySet().toArray(new String[descriptors.size()]);
    }

    public String getDescriptor(String s) {
        return descriptors.get(s);
    }

    public Session findSameSession(JCRStoreProvider provider, String userID, String ws) throws RepositoryException {
        Session s = null;
        if (userSession != null) {
            Map<String, Map<String, JCRSessionWrapper>> smap = userSession.get();
            if (smap != null && smap.containsKey(userID)) {
                Map<String, JCRSessionWrapper> wsMap = smap.get(userID);
                for (String key : wsMap.keySet()) {
                    if (key.startsWith(ws)) {
                        if ((s = wsMap.get(key).getProviderSession(provider, false)) != null) {
                            break;
                        }
                        ;
                    }
                }
            }
        }
        return s;
    }

    public JCRSessionWrapper login(Credentials credentials, String workspace)
            throws LoginException, NoSuchWorkspaceException, RepositoryException {
        return login(credentials, workspace, null, null);
    }

    private JCRSessionWrapper login(Credentials credentials, String workspace, Locale locale, Locale fallbackLocale)
            throws LoginException, NoSuchWorkspaceException, RepositoryException {
        if (!(credentials instanceof SimpleCredentials)) {
            throw new LoginException("Only SimpleCredentials supported in this implementation");
        }

        final SimpleCredentials simpleCreds = (SimpleCredentials) credentials;

        JahiaLoginModule m = new JahiaLoginModule();
        Subject s = new Subject();
        HashMap<String, ?> sharedState = new HashMap<String, Object>();
        HashMap<String, ?> options = new HashMap<String, Object>();
        m.initialize(s, new CallbackHandler() {
            public void handle(Callback[] callbacks) throws IOException, UnsupportedCallbackException {
                for (Callback callback : callbacks) {
                    if (callback instanceof NameCallback) {
                        ((NameCallback) callback).setName(simpleCreds.getUserID());
                    } else if (callback instanceof PasswordCallback) {
                        ((PasswordCallback) callback).setPassword(simpleCreds.getPassword());
                    } else {
                        throw new UnsupportedCallbackException(callback);
                    }
                }
            }
        }, sharedState, options);

        try {
            JahiaLoginModule.Token t = JahiaLoginModule.getToken(simpleCreds.getUserID(), new String(
                    simpleCreds.getPassword()));
            m.login();
            m.commit();
            credentials = JahiaLoginModule.getCredentials(simpleCreds.getUserID(), t != null ? t.deniedPath : null);
        } catch (javax.security.auth.login.LoginException e) {
            throw new LoginException(e);
        }

        Set<JahiaPrincipal> p = s.getPrincipals(JahiaPrincipal.class);
        for (JahiaPrincipal jahiaPrincipal : p) {
            JahiaUser user = null;
            if (!jahiaPrincipal.getName().startsWith(JahiaLoginModule.SYSTEM)) {
                if (jahiaPrincipal.isGuest()) {
                    user = userService.lookupUser(JahiaUserManagerService.GUEST_USERNAME);
                } else {
                    user = userService.lookupUser(jahiaPrincipal.getName());
                }
            }
            return new JCRSessionWrapper(user, credentials, jahiaPrincipal.isSystem(), workspace, locale, this, fallbackLocale);
        }
        throw new LoginException("Can't login");
    }

    public JCRSessionWrapper login(Credentials credentials) throws LoginException, RepositoryException {
        return login(credentials, null);
    }

    public JCRSessionWrapper login(String workspace)
            throws LoginException, NoSuchWorkspaceException, RepositoryException {
        return login(JahiaLoginModule.getGuestCredentials(), workspace);
    }

    public JCRSessionWrapper login() throws LoginException, RepositoryException {
        return login(null, null);
    }

    public boolean isStandardDescriptor(String key) {
        return false;
    }

    public boolean isSingleValueDescriptor(String key) {
        return false;
    }

    public Value getDescriptorValue(String key) {
        return null;
    }

    public Value[] getDescriptorValues(String key) {
        return new Value[0];
    }

    public Map<String, JCRStoreProvider> getMountPoints() {
        return mountPoints;
    }

    @SuppressWarnings("unchecked")
    public Map<String, JCRStoreProvider> getProviders() {
        return providers;
    }

    public JCRStoreProvider getDefaultProvider() {
        return getProviders().get(DEFAULT_PROVIDER_KEY);
    }

    /**
     * Registers the JCR store provider.
     *
     * @param p the provider instance
     */
    public void addProvider(JCRStoreProvider p) {
        String key = p.getKey();
        String mountPoint = p.getMountPoint();

        synchronized (this) {
            List<JCRStoreProvider> newList = new ArrayList<JCRStoreProvider>(providerList);
            newList.add(p);
            Collections.sort(newList);
            providerList = Collections.unmodifiableList(newList);

            initProviders();

            if (mountPoint != null) {
                SortedMap<String, JCRStoreProvider> newMountPoints = new TreeMap<String, JCRStoreProvider>(mountPoints);
                newMountPoints.put(mountPoint, p);
                mountPoints = Collections.unmodifiableSortedMap(newMountPoints);
            }
        }
        logger.info("Added provider " + key + " at mount point " + mountPoint + " using implementation "
                + p.getClass().getName());
    }

    /**
     * Registers the JCR store provider.
     *
     * @param key        the key of the provider
     * @param mountPoint provider's mount point
     * @param p          the provider instance
     * @deprecated use {@link #addProvider(JCRStoreProvider)} instead
     */
    @Deprecated
    public void addProvider(String key, String mountPoint, JCRStoreProvider p) {
        addProvider(p);
    }

    /**
     * Unregister a provider
     *
     * @param key the key of the provider
     */
    public void removeProvider(String key) {
        JCRStoreProvider p = getProviders().get(key);
        if (p == null) {
            return;
        }
        synchronized (this) {
            List<JCRStoreProvider> newList = new ArrayList<JCRStoreProvider>(providerList);
            newList.remove(p);
            providerList = Collections.unmodifiableList(newList);

            initProviders();

            if (p.getMountPoint() != null) {
                SortedMap<String, JCRStoreProvider> newMountPoints = new TreeMap<String, JCRStoreProvider>(mountPoints);
                newMountPoints.remove(p.getMountPoint());
                mountPoints = Collections.unmodifiableSortedMap(newMountPoints);
            }
        }
        logger.info("Removed provider " + key + " at mount point " + p.getMountPoint() + " using implementation " + p.getClass().getName());
    }

    private void initProviders() {
        Map<String, JCRStoreProvider> providerMap = new LinkedHashMap<String, JCRStoreProvider>(providerList.size());
        for (JCRStoreProvider p : providerList) {
            providerMap.put(p.getKey(), p);
        }
        providers = UnmodifiableMap.decorate(providerMap);
    }

    /**
     * Returns a list of providers ordered by registration order. This is important because some providers
     * are more "low-level" than others.
     *
     * @return an ORDERED list of providers
     */
    public List<JCRStoreProvider> getProviderList() {
        return providerList;
    }

    // Initialization on demand holder idiom: thread-safe singleton initialization
    private static class Holder {
        static final JCRSessionFactory INSTANCE = new JCRSessionFactory();
    }

    public static JCRSessionFactory getInstance() {
        return Holder.INSTANCE;
    }

    public void closeAllSessions() {
        Map<String, Map<String, JCRSessionWrapper>> smap = userSession.get();
        if (smap != null) {
            for (Map<String, JCRSessionWrapper> wsMap : smap.values()) {
                for (JCRSessionWrapper s : wsMap.values()) {
                    if (s.isLive()) {
                        s.logout();
                    }
                }
            }
            userSession.set(null);
        }
    }

    public JCRStoreProvider getProvider(String path) {
        Map<String, JCRStoreProvider> currentMountPoints = getMountPoints();
        for (String mp : currentMountPoints.keySet()) {
            if (mp.equals("/") || path.equals(mp) || path.startsWith(mp + "/")) {
                return currentMountPoints.get(mp);
            }
        }
        return null;
    }

    public boolean unmount(JCRStoreProvider p) {
        if (p != null && p.isDynamicallyMounted()) {
            p.stop();
            return true;
        }
        return false;
    }

    public NamespaceRegistry getNamespaceRegistry() throws RepositoryException {
        return namespaceRegistry;
    }

    public JahiaUser getCurrentUser() {
        return currentUser.get();
    }

    public void setCurrentUser(JahiaUser user) {
        currentUser.set(user);
    }

    public Locale getCurrentLocale() {
        return currentLocale.get();
    }

    public void setCurrentLocale(Locale locale) {
        currentLocale.set(locale);
    }

    public Locale getFallbackLocale() {
        return fallbackLocale.get();
    }

    public void setFallbackLocale(Locale locale) {
        fallbackLocale.set(locale);
    }

    public JahiaUser getCurrentAliasedUser() {
        return currentAliasedUser.get();
    }

    public void setCurrentAliasedUser(JahiaUser user) {
        currentAliasedUser.set(user);
    }

    public String getCurrentServletPath() {
        return currentServletPath.get();
    }

    public void setCurrentServletPath(String path) {
        currentServletPath.set(path);
    }

    public void setCurrentPreviewDate(Calendar previewDate) {
        currentPreviewDate.set(previewDate);
    }

    public Calendar getCurrentPreviewDate() {
        return currentPreviewDate.get();
    }

    public LocalValidatorFactoryBean getValidatorFactoryBean() {
        return validatorFactoryBean;
    }

    public void setValidatorFactoryBean(LocalValidatorFactoryBean validatorFactoryBean) {
        this.validatorFactoryBean = validatorFactoryBean;
    }

}