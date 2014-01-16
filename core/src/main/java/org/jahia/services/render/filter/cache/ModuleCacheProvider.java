/**
 * This file is part of Jahia, next-generation open source CMS:
 * Jahia's next-generation, open source CMS stems from a widely acknowledged vision
 * of enterprise application convergence - web, search, document, social and portal -
 * unified by the simplicity of web content management.
 *
 * For more information, please visit http://www.jahia.com.
 *
 * Copyright (C) 2002-2014 Jahia Solutions Group SA. All rights reserved.
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
 * Commercial and Supported Versions of the program (dual licensing):
 * alternatively, commercial and supported versions of the program may be used
 * in accordance with the terms and conditions contained in a separate
 * written agreement between you and Jahia Solutions Group SA.
 *
 * If you are unsure which license is appropriate for your use,
 * please contact the sales department at sales@jahia.com.
 */

package org.jahia.services.render.filter.cache;

import net.sf.ehcache.Cache;
import net.sf.ehcache.CacheManager;
import net.sf.ehcache.Element;
import org.jahia.services.SpringContextSingleton;
import org.jahia.services.cache.ehcache.EhCacheProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;

import java.text.ParseException;
import java.util.Set;
import java.util.UUID;

/**
 * Instantiates and provides access to the module output and dependency caches.
 *
 * @author rincevent
 * @author Sergiy Shyrkov
 */
public class ModuleCacheProvider implements InitializingBean {

    private static final String CACHE_NAME = "HTMLCache";
    private static final String CACHE_SYNC_NAME = "HTMLCacheEventSync";
    private static final String DEPS_CACHE_NAME = "HTMLDependenciesCache";
    private static final String REGEXPDEPS_CACHE_NAME = "HTMLREGEXPDependenciesCache";

    private static Logger logger = LoggerFactory.getLogger(ModuleCacheProvider.class);
    private Cache regexpDependenciesCache;

    /**
     * Returns an instance of this class
     *
     * @return an instance of this class
     */
    public static ModuleCacheProvider getInstance() {
        return (ModuleCacheProvider) SpringContextSingleton.getBean("ModuleCacheProvider");
    }

    private Cache htmlCache;
    private EhCacheProvider cacheProvider;
    private Cache dependenciesCache;
    private Cache syncCache;

    private CacheKeyGenerator keyGenerator;

    /**
     * Invoked by a BeanFactory after it has set all bean properties supplied
     * (and satisfied BeanFactoryAware and ApplicationContextAware).
     * <p/>
     * This method allows the bean instance to perform initialization only
     * possible when all bean properties have been set and to throw an exception
     * in the event of misconfiguration.
     *
     * @throws Exception in the event of misconfiguration (such as failure to
     *                   set an essential property) or if initialization fails.
     */
    public void afterPropertiesSet() throws Exception {
        CacheManager cacheManager = cacheProvider.getCacheManager();
        htmlCache = cacheManager.getCache(CACHE_NAME);
        if (htmlCache == null) {
            cacheManager.addCache(CACHE_NAME);
            htmlCache = cacheManager.getCache(CACHE_NAME);
        }
        dependenciesCache = cacheManager.getCache(DEPS_CACHE_NAME);
        if (dependenciesCache == null) {
            cacheManager.addCache(DEPS_CACHE_NAME);
            dependenciesCache = cacheManager.getCache(DEPS_CACHE_NAME);
        }
        regexpDependenciesCache = cacheManager.getCache(REGEXPDEPS_CACHE_NAME);
        if (regexpDependenciesCache == null) {
            cacheManager.addCache(REGEXPDEPS_CACHE_NAME);
            regexpDependenciesCache = cacheManager.getCache(REGEXPDEPS_CACHE_NAME);
        }

        if (Boolean.getBoolean("cluster.activated")) {
            // only create syncCache in cluster
            syncCache = cacheManager.getCache(CACHE_SYNC_NAME);
            if (syncCache == null) {
                cacheManager.addCache(CACHE_SYNC_NAME);
                syncCache = cacheManager.getCache(CACHE_SYNC_NAME);
            }
        }
    }

    /**
     * Flushes all the cache entries, related to the specified node.
     *
     * @param nodePath the node path to be invalidated.
     * @throws ParseException in case of a malformed key
     */
    public void invalidate(String nodePath) {
        invalidate(nodePath, true);
    }

    /**
     * Flushes all the cache entries, related to the specified node.
     * @param nodePath the node path to be invalidated.
     * @param propageToOtherClusterNodes do notify replicators of this event
     * @throws ParseException in case of a malformed key
     */
    @SuppressWarnings("unchecked")
    public void invalidate(String nodePath, boolean propagateToOtherClusterNodes) {
        Element element = dependenciesCache.get(nodePath);
        if (element != null) {
            Set<String> deps = (Set<String>) element.getObjectValue();
            if (deps.contains("ALL")) {
                // do not propagate
                htmlCache.removeAll(true);
            } else {
                invalidateDependencies(deps);
            }
        }
        if(propagateToOtherClusterNodes && syncCache != null) {
            if (logger.isDebugEnabled()) {
                logger.debug("Sending flush of {} across cluster", nodePath);
            }
            syncCache.put(new Element("FLUSH_PATH-" + UUID.randomUUID(), nodePath));
        }
    }

    private void invalidateDependencies(Set<String> deps) {
        for (String dep : deps) {
            if (dep != null) {
                boolean removed = htmlCache.remove(dep);
                if (!removed && logger.isDebugEnabled()) {
                    logger.debug("Failed to remove {} from cache", dep);
                }
            }
        }
    }

    public Cache getCache() {
        return htmlCache;
    }

    public Cache getDependenciesCache() {
        return dependenciesCache;
    }

    public CacheKeyGenerator getKeyGenerator() {
        return keyGenerator;
    }

    public void setCacheProvider(EhCacheProvider cacheProvider) {
        this.cacheProvider = cacheProvider;
    }

    /**
     * Injects the cache key generator implementation.
     *
     * @param keyGenerator the cache key generator implementation to use
     */
    public void setKeyGenerator(CacheKeyGenerator keyGenerator) {
        this.keyGenerator = keyGenerator;
    }

    public void flushCaches() {
        htmlCache.removeAll();
        htmlCache.flush();
        dependenciesCache.removeAll();
        dependenciesCache.flush();
        regexpDependenciesCache.removeAll();
        regexpDependenciesCache.flush();
    }

    public Cache getRegexpDependenciesCache() {
        return regexpDependenciesCache;
    }

    public void invalidateRegexp(String key) {
        invalidateRegexp(key, true);
    }

    public void invalidateRegexp(String key, boolean propagateToOtherClusterNodes) {
        Element element = regexpDependenciesCache.get(key);
        if (element != null) {
            @SuppressWarnings("unchecked")
            Set<String> deps = (Set<String>) element.getObjectValue();
            invalidateDependencies(deps);
        }
        if(propagateToOtherClusterNodes && syncCache != null) {
            if (logger.isDebugEnabled()) {
                logger.debug("Sending flush of regexp {} across cluster", key);
            }
            syncCache.put(new Element("FLUSH_REGEXP-" + UUID.randomUUID(), key));
        }
    }
}
