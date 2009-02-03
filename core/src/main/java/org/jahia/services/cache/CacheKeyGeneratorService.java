/**
 * 
 * This file is part of Jahia: An integrated WCM, DMS and Portal Solution
 * Copyright (C) 2002-2009 Jahia Limited. All rights reserved.
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
 * in Jahia's FLOSS exception. You should have recieved a copy of the text
 * describing the FLOSS exception, and it is also available here:
 * http://www.jahia.com/license"
 * 
 * Commercial and Supported Versions of the program
 * Alternatively, commercial and supported versions of the program may be used
 * in accordance with the terms contained in a separate written agreement
 * between you and Jahia Limited. If you are unsure which license is appropriate
 * for your use, please contact the sales department at sales@jahia.com.
 */

package org.jahia.services.cache;

import org.jahia.bin.Jahia;
import org.jahia.content.ContentContainerKey;
import org.jahia.content.ContentObjectKey;
import org.jahia.content.ContentPageKey;
import org.jahia.data.containers.JahiaContainer;
import org.jahia.exceptions.JahiaException;
import org.jahia.exceptions.JahiaInitializationException;
import org.jahia.params.AdvPreviewSettings;
import org.jahia.registries.ServicesRegistry;
import org.jahia.services.JahiaService;
import org.jahia.services.acl.JahiaACLManagerService;
import org.jahia.services.pages.JahiaPage;
import org.jahia.services.usermanager.JahiaGroup;
import org.jahia.services.usermanager.JahiaGroupManagerService;
import org.jahia.services.usermanager.JahiaUser;
import org.jahia.services.usermanager.JahiaUserManagerService;
import org.jahia.settings.SettingsBean;

import java.util.*;

/**
 * Created by IntelliJ IDEA. User: rincevent Date: 5 févr. 2008 Time: 11:29:39 To change this template use File |
 * Settings | File Templates.
 */
public class CacheKeyGeneratorService extends JahiaService {
    public static final String USERNAME_PREFIX = "USERNAME-";
    public static final String LANGUAGECODE_PREFIX = "LANGUAGECODE-";
    public static final String WORKFLOWSTATE_PREFIX = "WORKFLOWSTATE-";
    public static final String SITE_PREFIX = "SITE-";
    public static final String USERKEY_CACHE_NAME="UserKeyCache";
    private SortedMap<String, JahiaGroup> groups;
    private SortedSet<String> users;
    private JahiaACLManagerService jahiaACLManagerService;
    private JahiaGroupManagerService groupManagerService;
    private static CacheKeyGeneratorService instance;
    private CacheService cacheService;
    private Cache userKeyCache;

    public void setJahiaACLManagerService(JahiaACLManagerService jahiaACLManagerService) {
        this.jahiaACLManagerService = jahiaACLManagerService;
    }

    public void setGroupManagerService(JahiaGroupManagerService groupManagerService) {
        this.groupManagerService = groupManagerService;
    }

    public void setCacheService(CacheService cacheService) {
        this.cacheService = cacheService;
    }

    public void start() throws JahiaInitializationException {
        groups = new TreeMap<String, JahiaGroup>();
        Collection<String> names = jahiaACLManagerService.getAllGroupsInAcl();
        for (String name : names) {
            JahiaGroup g = groupManagerService.lookupGroup(name);
            groups.put(name, g);
        }

        users = new TreeSet<String>(jahiaACLManagerService.getAllUsersInAcl());
        userKeyCache = cacheService.createCacheInstance(USERKEY_CACHE_NAME);
    }

    public void stop() throws JahiaException {
    }

    public void rightsUpdated() throws JahiaInitializationException {
        SortedSet<String> newusers = new TreeSet<String>(jahiaACLManagerService.getAllUsersInAcl());
        final CacheService cacheService = ServicesRegistry.getInstance().getCacheService();
        ContainerHTMLCache containerHTMLCache = cacheService.getContainerHTMLCacheInstance();
        SkeletonCache skeletonCache = cacheService.getSkeletonCacheInstance();
        if (!newusers.equals(users)) {
            Set<String> removedUsers = new HashSet<String>(users);
            removedUsers.removeAll(newusers);

            users = newusers;

            for (String removedUser : removedUsers) {
                String s = USERNAME_PREFIX + removedUser;
                containerHTMLCache.flushGroup(s);
                skeletonCache.flushGroup(s);
            }
            userKeyCache.flush();
        }

        SortedSet<String> newgroupskeys = new TreeSet<String>(jahiaACLManagerService.getAllGroupsInAcl());
        if (!newgroupskeys.equals(groups.keySet())) {
            SortedMap<String, JahiaGroup> newgroups = new TreeMap<String, JahiaGroup>();
            for (String name : newgroupskeys) {
                JahiaGroup g = groupManagerService.lookupGroup(name);
                newgroups.put(name, g);
            }

            groups = newgroups;

            containerHTMLCache.flush();
            skeletonCache.flush();
            userKeyCache.flush();
        }
    }

    public String getUserCacheKey(JahiaUser user, int siteID) {
        String usercachekey;
        final String s = user.getUserKey() + SITE_PREFIX + siteID;
        if (!AdvPreviewSettings.isInUserAliasingMode() && userKeyCache.containsKey(s)) {
            return (String) userKeyCache.get(s);
        }
        Collection<String> users = getAllUsers();

        if (users.contains(user.getUserKey())) {
            usercachekey = user.getUserKey();
        } else {
            Collection<JahiaGroup> groups = getAllGroups();
            StringBuffer b = new StringBuffer();
            for (JahiaGroup g : groups) {
                if (g != null && (g.getSiteID() == siteID || g.getSiteID() == 0) && g.isMember(user)) {
                    if(b.length()>0)b.append("|");
                    b.append(g.getGroupname());
                }
            }
            usercachekey = b.toString();
            if(usercachekey.equals(JahiaGroupManagerService.GUEST_GROUPNAME) && !user.getUsername().equals(JahiaUserManagerService.GUEST_USERNAME)) {
                usercachekey = b.append("|"+JahiaGroupManagerService.USERS_GROUPNAME).toString();
            }
        }

        // useraliasing mode
        if (AdvPreviewSettings.isInUserAliasingMode()) {
            usercachekey += "_" + AdvPreviewSettings.getThreadLocaleInstance().getAliasedUser().getUserKey();
        } else {
            CacheEntry cacheEntry = new CacheEntry(usercachekey);
            cacheEntry.setExpirationDate(new Date(System.currentTimeMillis() + (300 * 1000)));
            userKeyCache.putCacheEntry(s,cacheEntry, true);
        }
        return usercachekey;
    }


    private Collection<JahiaGroup> getAllGroups() {
        return groups.values();
    }

    private Collection<String> getAllUsers() {
        return users;

    }

    /**
     * <p>Builds the cache key that is used to reference the cache entries in the lookup table.</p>
     *
     * @param container    the container identification number
     * @param cacheKey     the cacheKey
     * @param user         the user name
     * @param languageCode the language code
     * @param mode         the mode
     * @param scheme       the request scheme (http/https)
     * @return the generated cache key
     */
    public GroupCacheKey computeContainerEntryKey(JahiaContainer container,
                                                  String cacheKey,
                                                  JahiaUser user,
                                                  String languageCode,
                                                  String mode,
                                                  String scheme) {
        int id = 0;
        if (container != null)
            id = container.getID();
        String usercachekey = getUserCacheKey(user, Jahia.getThreadParamBean().getSiteID());

        Object key = getKey(id, mode, languageCode, usercachekey, cacheKey, scheme);

        return new GroupCacheKey(key, new HashSet<String>());
    }
    public GroupCacheKey computeContainerEntryKeyWithGroups(JahiaContainer container,
                                                            String group,
                                                            JahiaUser user,
                                                            String languageCode,
                                                            String mode,
                                                            String scheme,
                                                            Set<ContentObjectKey> dependencies) {
        int id = 0;
        if (container != null)
            id = container.getID();
        String containerkey = new ContentContainerKey(id).toString();
        String usercachekey = getUserCacheKey(user, Jahia.getThreadParamBean().getSiteID());

        Object key = getKey(id, mode, languageCode, usercachekey, group, scheme);

        Set<String> groups = new HashSet<String>();
        if (SettingsBean.getInstance().isDevelopmentMode()) {
            groups.add(containerkey + WORKFLOWSTATE_PREFIX + mode + LANGUAGECODE_PREFIX + languageCode);
            groups.add(USERNAME_PREFIX + usercachekey);
            groups.add(SITE_PREFIX + Jahia.getThreadParamBean().getSiteID());
            for (ContentObjectKey objectKey : dependencies) {
                groups.add(objectKey.toString() + WORKFLOWSTATE_PREFIX + mode + LANGUAGECODE_PREFIX + languageCode);
            }
        } else {
            groups.add(Integer.toString((containerkey + WORKFLOWSTATE_PREFIX + mode + LANGUAGECODE_PREFIX + languageCode).hashCode()));
            groups.add(Integer.toString((USERNAME_PREFIX + usercachekey).hashCode()));
            groups.add(Integer.toString((SITE_PREFIX + Jahia.getThreadParamBean().getSiteID()).hashCode()));
            for (ContentObjectKey objectKey : dependencies) {
                groups.add(Integer.toString((objectKey.toString() + WORKFLOWSTATE_PREFIX + mode + LANGUAGECODE_PREFIX + languageCode).hashCode()));
            }
        }
        return new GroupCacheKey(key, groups);
    }

    /**
     * <p>Builds the cache key that is used to reference the cache entries in the lookup table.</p>
     *
     * @param skeleton     the skeleton identification number
     * @param cacheKey     the cacheKey
     * @param user         the user name
     * @param languageCode the language code
     * @param mode         the mode
     * @param scheme       the request scheme (http/https)
     * @return the generated cache key
     */
    public GroupCacheKey computeSkeletonEntryKey(JahiaPage skeleton,
                                                 String cacheKey,
                                                 JahiaUser user,
                                                 String languageCode,
                                                 String mode,
                                                 String scheme) {
        int id = skeleton.getID();
        String usercachekey = getUserCacheKey(user, skeleton.getSiteID());

        Object key = getKey(id, mode, languageCode, usercachekey, cacheKey, scheme);

        return new GroupCacheKey(key, new HashSet<String>());
    }

    public GroupCacheKey computeSkeletonEntryKeyWithGroups(JahiaPage skeleton,
                                                           String group,
                                                           JahiaUser user,
                                                           String languageCode,
                                                           String mode,
                                                           String scheme,
                                                           Set<ContentObjectKey> dependencies) {
        int id = skeleton.getID();
        String containerkey = new ContentPageKey(id).toString();
        final int siteID = skeleton.getSiteID();
        String usercachekey = getUserCacheKey(user, siteID);

        Object key = getKey(id, mode, languageCode, usercachekey, group, scheme);

        Set<String> groups = new HashSet<String>();
        if (SettingsBean.getInstance().isDevelopmentMode()) {
            groups.add(containerkey + WORKFLOWSTATE_PREFIX + mode + LANGUAGECODE_PREFIX + languageCode);
            groups.add(USERNAME_PREFIX + usercachekey);
            groups.add(SITE_PREFIX + siteID);
            for (ContentObjectKey objectKey : dependencies) {
                groups.add(objectKey.toString() + WORKFLOWSTATE_PREFIX + mode + LANGUAGECODE_PREFIX + languageCode);
            }
        } else {
            groups.add(Integer.toString((containerkey + WORKFLOWSTATE_PREFIX + mode + LANGUAGECODE_PREFIX + languageCode).hashCode()));
            groups.add(Integer.toString((USERNAME_PREFIX + usercachekey).hashCode()));
            groups.add(Integer.toString((SITE_PREFIX + siteID).hashCode()));
            for (ContentObjectKey objectKey : dependencies) {
                groups.add(Integer.toString((objectKey.toString() + WORKFLOWSTATE_PREFIX + mode + LANGUAGECODE_PREFIX + languageCode).hashCode()));
            }
        }
        return new GroupCacheKey(key, groups);
    }

    private Object getKey(int id, String mode, String languageCode, String usercachekey, String group, String scheme) {
        String key = id + "-" + mode + "-" + languageCode + "-" + usercachekey;
        if (group != null) {
            key += "-" + group;
        }
        if (!"http".equals(scheme)) {
            key += "-" + scheme;
        }
        if (SettingsBean.getInstance().isDevelopmentMode()) {
         return key;  
        } else {
        return key.hashCode();
        }
    }

    public String getPageKey(String containerID, String mode, String languageCode) {
        if (SettingsBean.getInstance().isDevelopmentMode()) {
            return containerID + WORKFLOWSTATE_PREFIX + mode + LANGUAGECODE_PREFIX + languageCode;
        } else {
            return Integer.toString((containerID + WORKFLOWSTATE_PREFIX + mode + LANGUAGECODE_PREFIX + languageCode).hashCode());
        }
    }

    /**
     * Returns an instance of the service class
     *
     * @return the unique instance of this class
     * @throws org.jahia.exceptions.JahiaException in case of error
     */
    public static synchronized CacheKeyGeneratorService getInstance()
            throws JahiaException {
        if (instance == null) {
            instance = new CacheKeyGeneratorService();
        }
        return instance;
    }

    public GroupCacheKey computeContainerEntryKey(int ctnid, String group, JahiaUser user, String languageCode,
                                                  String operationMode, String scheme, int siteID) {
        String usercachekey = getUserCacheKey(user, siteID);

        Object key = getKey(ctnid, operationMode, languageCode, usercachekey, group, scheme);

        return new GroupCacheKey(key, new HashSet());
    }
}
