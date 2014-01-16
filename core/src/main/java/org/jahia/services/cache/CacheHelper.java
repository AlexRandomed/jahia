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

package org.jahia.services.cache;

import net.sf.ehcache.Cache;
import net.sf.ehcache.CacheManager;
import net.sf.ehcache.Ehcache;
import net.sf.ehcache.Element;
import net.sf.ehcache.management.ManagementService;
import net.sf.ehcache.statistics.StatisticsGateway;

import org.jahia.registries.ServicesRegistry;
import org.jahia.services.SpringContextSingleton;
import org.jahia.services.cache.ehcache.CacheInfo;
import org.jahia.services.cache.ehcache.CacheManagerInfo;
import org.jahia.services.cache.ehcache.EhCacheProvider;
import org.jahia.utils.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.management.ManagementFactory;
import java.util.Iterator;
import java.util.Map;
import java.util.TreeMap;

/**
 * Cache manager utility.
 *
 * @author Sergiy Shyrkov
 */
public final class CacheHelper {

    private static final Logger logger = LoggerFactory.getLogger(CacheHelper.class);

    /**
     * Flushes all back-end and front-end Jahia caches on the current cluster node
     * only.
     */
    public static void flushAllCaches() {
        flushAllCaches(false);
    }

    /**
     * Flushes all back-end and front-end Jahia caches. If
     * <code>propagateInCluster</code> is set to true also propagates the flush to other cluster nodes.
     *
     * @param propagateInCluster if set to true the flush is propagated to other cluster nodes
     */
    public static void flushAllCaches(boolean propagateInCluster) {
        logger.info("Flushing all caches{}",
                propagateInCluster ? " also propagating it to all cluster members" : "");

        CacheService cacheService = ServicesRegistry.getInstance().getCacheService();

        // legacy caches
        Iterator<String> cacheNames = cacheService.getNames().iterator();
        while (cacheNames.hasNext()) {
            String curCacheName = cacheNames.next();
            org.jahia.services.cache.Cache<Object, Object> cache = cacheService
                    .getCache(curCacheName);
            if (cache != null) {
                cache.flush(propagateInCluster);
            }
        }

        // Ehcaches
        for (CacheManager mgr : CacheManager.ALL_CACHE_MANAGERS) {
            for (String cacheName : mgr.getCacheNames()) {
                Cache cache = mgr.getCache(cacheName);
                if (cache != null) {
                    // flush
                    cache.removeAll(!propagateInCluster);
                }
            }
        }

        logger.info("...done flushing all caches.");
    }

    /**
     * Flushes the caches of the specified cache manager.
     *
     * @param cacheManagerName   the cache manager name to flush caches for
     * @param propagateInCluster if set to true the flush is propagated to other cluster nodes
     */
    public static void flushCachesForManager(String cacheManagerName, boolean propagateInCluster) {
        logger.info("Flushing caches for the cache manager '{}' {}", cacheManagerName, propagateInCluster ? " also propagating it to all cluster members" : "");
        CacheManager ehcacheManager = getCacheManager(cacheManagerName);
        if (ehcacheManager == null) {
            return;
        }
        for (String cacheName : ehcacheManager.getCacheNames()) {
            Cache cache = ehcacheManager.getCache(cacheName);
            if (cache != null) {
                // flush
                cache.removeAll(!propagateInCluster);
            }
        }
        logger.info("...done flushing caches for manager {}", cacheManagerName);
    }

    /**
     * Flushes the specified Ehcache on the current cluster node only.
     *
     * @param cacheName the name of the cache to flush
     */
    public static void flushEhcacheByName(String cacheName) {
        flushEhcacheByName(cacheName, false);
    }

    /**
     * Flushes the specified Ehcache. If <code>propagateInCluster</code> is set to true also propagates the flush to other cluster nodes.
     *
     * @param cacheName          the name of the cache to flush
     * @param propagateInCluster if set to true the flush is propagated to other cluster nodes
     */
    public static void flushEhcacheByName(String cacheName, boolean propagateInCluster) {
        logger.info("Flushing {}", cacheName);
        CacheManager ehcacheManager = getJahiaCacheManager();
        Cache cache = ehcacheManager.getCache(cacheName);
        if (cache != null) {
            // flush
            cache.removeAll(!propagateInCluster);
            logger.info("...done flushing {}", cacheName);
        } else {
            logger.warn("Cache with the name {} not found. Skip flushing.", cacheName);
        }
    }

    /**
     * Flushes front-end Jahia caches (module HTML output caches) on the current cluster node only.
     */
    public static void flushOutputCaches() {
        flushOutputCaches(false);
    }

    /**
     * Flushes front-end Jahia caches (module HTML output caches). If <code>propagateInCluster</code> is set to true also propagates the
     * flush to other cluster nodes.
     *
     * @param propagateInCluster if set to true the flush is propagated to other cluster nodes
     */
    public static void flushOutputCaches(boolean propagateInCluster) {
        logger.info("Flushing HTML output caches{}",
                propagateInCluster ? " also propagating it to all cluster members" : "");
        CacheManager ehcacheManager = getJahiaCacheManager();
        for (String cacheName : ehcacheManager.getCacheNames()) {
            if (!cacheName.startsWith("HTML")) {
                continue;
            }
            Cache cache = ehcacheManager.getCache(cacheName);
            if (cache != null) {
                // flush
                cache.removeAll(!propagateInCluster);
                logger.info("...done flushing {}", cacheName);
            }
        }
    }
    
    @SuppressWarnings("deprecation")
    private static CacheInfo getCacheInfo(Cache cache, boolean withConfig, boolean withSizeInBytes) {
        CacheInfo info = new CacheInfo(cache);
        info.setName(cache.getName());
        if (withConfig) {
            info.setConfig(cache.getCacheManager().getActiveConfigurationText(info.getName()));
        }
        StatisticsGateway stats = cache.getStatistics();
        info.setHitCount(stats.cacheHitCount());
        info.setMissCount(stats.cacheMissCount());
        info.setHitRatio(info.getAccessCount() > 0 ? info.getHitCount() * 100 / info.getAccessCount() : 0);

        info.setSize(stats.getSize());
        
        info.setLocalHeapSize(stats.getLocalHeapSize());

        info.setOverflowToDisk(cache.getCacheConfiguration().isOverflowToDisk());
        if (info.isOverflowToDisk()) {
            info.setLocalDiskSize(stats.getLocalDiskSize());
        }

        info.setOverflowToOffHeap(cache.getCacheConfiguration().isOverflowToOffHeap());
        if (info.isOverflowToOffHeap()) {
            info.setLocalOffHeapSize(stats.getLocalOffHeapSize());
        }

        if (withSizeInBytes) {
            info.setLocalHeapSizeInBytes(stats.getLocalHeapSizeInBytes());
            info.setLocalHeapSizeInBytesHumanReadable(FileUtils.humanReadableByteCount(info.getLocalHeapSizeInBytes()));
            if (info.isOverflowToDisk()) {
                info.setLocalDiskSizeInBytes(stats.getLocalDiskSizeInBytes());
                info.setLocalDiskSizeInBytesHumanReadable(FileUtils.humanReadableByteCount(info
                        .getLocalDiskSizeInBytes()));
            }
            if (info.isOverflowToOffHeap()) {
                info.setLocalOffHeapSizeInBytes(stats.getLocalOffHeapSizeInBytes());
                info.setLocalOffHeapSizeInBytesHumanReadable(FileUtils.humanReadableByteCount(info
                        .getLocalOffHeapSizeInBytes()));
            }
        }

        return info;
    }

    public static CacheManager getCacheManager(String cacheManagerName) {
        for (CacheManager mgr : CacheManager.ALL_CACHE_MANAGERS) {
            if (cacheManagerName.equals(mgr.getName())) {
                return mgr;
            }
        }
        return null;
    }

    private static CacheManagerInfo getCacheManagerInfo(CacheManager manager, boolean withConfig,
            boolean withSizeInBytes) {
        CacheManagerInfo info = new CacheManagerInfo(manager);
        info.setName(manager.getName());
        if (withConfig) {
            info.setConfig(manager.getActiveConfigurationText());
        }

        for (String name : manager.getCacheNames()) {
            Cache cache = manager.getCache(name);
            if (cache != null) {
                CacheInfo cacheInfo = getCacheInfo(cache, withConfig, withSizeInBytes);
                info.getCaches().put(name, cacheInfo);
                info.setHitCount(info.getHitCount() + cacheInfo.getHitCount());
                info.setMissCount(info.getMissCount() + cacheInfo.getMissCount());

                info.setOverflowToDisk(info.isOverflowToDisk() || cacheInfo.isOverflowToDisk());
                info.setOverflowToOffHeap(info.isOverflowToOffHeap() || cacheInfo.isOverflowToOffHeap());

                info.setSize(info.getSize() + cacheInfo.getSize());
                info.setLocalHeapSize(info.getLocalHeapSize() + cacheInfo.getLocalHeapSize());
                if (info.isOverflowToDisk()) {
                    info.setLocalDiskSize(info.getLocalDiskSize() + cacheInfo.getLocalDiskSize());
                }
                if (info.isOverflowToOffHeap()) {
                    info.setLocalOffHeapSize(info.getLocalOffHeapSize() + cacheInfo.getLocalOffHeapSize());
                }
                if (withSizeInBytes) {
                    info.setLocalDiskSizeInBytes(info.getLocalDiskSizeInBytes() + cacheInfo.getLocalDiskSizeInBytes());
                    info.setLocalHeapSizeInBytes(info.getLocalHeapSizeInBytes() + cacheInfo.getLocalHeapSizeInBytes());
                    info.setLocalOffHeapSizeInBytes(info.getLocalOffHeapSizeInBytes()
                            + cacheInfo.getLocalOffHeapSizeInBytes());
                }
            }
        }

        if (withSizeInBytes) {
            info.setLocalDiskSizeInBytesHumanReadable(FileUtils.humanReadableByteCount(info.getLocalDiskSizeInBytes()));
            info.setLocalHeapSizeInBytesHumanReadable(FileUtils.humanReadableByteCount(info.getLocalHeapSizeInBytes()));
            if (info.isOverflowToOffHeap()) {
                info.setLocalOffHeapSizeInBytesHumanReadable(FileUtils.humanReadableByteCount(info
                        .getLocalOffHeapSizeInBytes()));
            }
        }

        return info;
    }

    /**
     * Returns the configuration and statistics information for all the available cache managers.
     * 
     * @param withConfig
     *            if <code>true</code> the configuration information will be also available
     * @param withSizeInBytes
     *            if <code>true</code> the calculation of the cache sizes (bytes) will be also available
     * @return the configuration and statistics information for all the available cache managers
     */
    public static Map<String, CacheManagerInfo> getCacheManagerInfos(boolean withConfig, boolean withSizeInBytes) {
        Map<String, CacheManagerInfo> infos = new TreeMap<String, CacheManagerInfo>();
        for (CacheManager manager : CacheManager.ALL_CACHE_MANAGERS) {
            infos.put(manager.getName(), getCacheManagerInfo(manager, withConfig, withSizeInBytes));
        }

        return infos;
    }

    private static CacheManager getJahiaCacheManager() {
        return ((EhCacheProvider) SpringContextSingleton.getBean("ehCacheProvider"))
                .getCacheManager();
    }

    /**
     * Returns a value of the specified cache element (by key), considering also classloader-aware desirialization if required.
     * 
     * @param cache
     *            the target cache
     * @param key
     *            the element key
     * @return a value of the specified cache element (by key), considering also classloader-aware desirialization if required
     */
    public static Object getObjectValue(Ehcache cache, String key) {
        return getObjectValue(cache.get(key));
    }

    /**
     * Returns a value of the specified cache element, considering also classloader-aware deserialization if required.
     * 
     * @param cacheElement
     *            the cache element
     * @return a value of the specified cache element (by key), considering also classloader-aware deserialization if required
     */
    public static Object getObjectValue(Element cacheElement) {
        Object value = null;
        if (cacheElement != null) {
            value = cacheElement.getObjectValue();
            if (value != null && value instanceof ClassLoaderAwareCacheEntry) {
                value = ((ClassLoaderAwareCacheEntry) value).getValue();
            }
        }

        return value;
    }

    public static void registerMBeans(String cacheManagerName) {
        CacheManager mgr = getCacheManager(cacheManagerName);
        if (mgr == null) {
            logger.warn("Cannot find Ehcache manager for name {}. Skip registering managed beans in JMX", cacheManagerName);
            return;
        }
        ManagementService.registerMBeans(mgr, ManagementFactory.getPlatformMBeanServer(), true,
                true, true, true, true);
    }

}
