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

package org.jahia.services.cache.ehcache;

import net.sf.ehcache.Cache;
import net.sf.ehcache.Ehcache;
import net.sf.ehcache.ObjectExistsException;
import net.sf.ehcache.config.CacheConfiguration;
import net.sf.ehcache.config.Configuration;
import net.sf.ehcache.config.PinningConfiguration;
import net.sf.ehcache.constructs.blocking.CacheEntryFactory;
import net.sf.ehcache.constructs.blocking.SelfPopulatingCache;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.Resource;
import org.apache.tika.io.IOUtils;
import org.jahia.services.cache.CacheProvider;
import org.jahia.services.cache.CacheService;
import org.jahia.services.cache.CacheImplementation;
import org.jahia.settings.SettingsBean;
import org.jahia.exceptions.JahiaInitializationException;

import net.sf.ehcache.CacheManager;
import net.sf.ehcache.management.ManagementService;

import java.io.IOException;
import java.io.InputStream;
import java.lang.management.ManagementFactory;
import java.util.Map;

/**
 * EHCache based cache provider implementation.
 * @author Serge Huber
 */
public class EhCacheProvider implements CacheProvider {

    final private static Logger logger = LoggerFactory.getLogger(EhCacheProvider.class);

    private CacheManager cacheManager = null;
    private int groupsSizeLimit = 100;
    private Resource configurationResource;
    private boolean statisticsEnabled;
    private boolean jmxActivated = true;
    private boolean initialized = false;

    public void init(SettingsBean settingsBean, CacheService cacheService) throws JahiaInitializationException {
        if (initialized) {
            return;
        }
        InputStream is = null;
        try {
            is = configurationResource.getInputStream();
            cacheManager = CacheManager.create(is);
        } catch (IOException e) {
            throw new JahiaInitializationException(e.getMessage(), e);
        } finally {
            IOUtils.closeQuietly(is);
        }
        if (jmxActivated) {
            ManagementService.registerMBeans(cacheManager, ManagementFactory.getPlatformMBeanServer(), true, true,
                    true, true, true);
        }
        initialized = true;
    }

    public void shutdown() {
        if (initialized) {
            logger.info("Shutting down cache provider, serializing to disk if active. Please wait...");
            long startTime = System.currentTimeMillis();
            cacheManager.shutdown();
            long totalTime = System.currentTimeMillis() - startTime;
            logger.info("Cache provider shutdown completed in " + totalTime + "[ms]");
            initialized = false;
        }
    }

    public CacheImplementation<?, ?> newCacheImplementation(String name) {
        return new EhCacheImpl(name, cacheManager, this);
    }

    public CacheManager getCacheManager() {
        return cacheManager;
    }

    public int getGroupsSizeLimit() {
        return groupsSizeLimit;
    }

    public void setGroupsSizeLimit(int groupsSizeLimit) {
        this.groupsSizeLimit = groupsSizeLimit;
    }

    public void setConfigurationResource(Resource configurationResource) {
        this.configurationResource = configurationResource;
    }

    public void setJmxActivated(boolean jmxActivated) {
        this.jmxActivated = jmxActivated;
    }

    public void setStatisticsEnabled(boolean statisticsEnabled) {
        this.statisticsEnabled = statisticsEnabled;
    }

    public boolean isStatisticsEnabled() {
        return statisticsEnabled;
    }

    /**
     * This method register a SelfPopulatingCache in the CacheManager.
     * @param cacheName the name of the cache to be registered
     * @param factory the CacheFactory to be used to fill the CacheEntry
     * @return teh instance of the registered cache
     */
    public synchronized SelfPopulatingCache registerSelfPopulatingCache(String cacheName, CacheEntryFactory factory) {
        // Call getEhCache to be sure to have the decorated cache. We manipulate only EhCache not Cache object
        if (cacheManager.getEhcache(cacheName) == null) {
            // get the default configuration four the self populating Caches
            Configuration configuration = cacheManager.getConfiguration();
            Map<String,CacheConfiguration> cacheConfigurations = configuration.getCacheConfigurations();
            CacheConfiguration cacheConfiguration = cacheConfigurations.get("org.jahia.selfPopulatingReplicatedCache");
            PinningConfiguration pinningConfiguration = new PinningConfiguration();
            pinningConfiguration.setStore("INCACHE");
            cacheConfiguration.addPinning(pinningConfiguration);
            // Create a new cache with the configuration
            Ehcache cache = new Cache(cacheConfiguration);
            cache.setName(cacheName);
            // Cache name has been set now we can initialize it by putting it in the manager.
            // Only Cache manager is initializing caches.
            cache = cacheManager.addCacheIfAbsent(cache);
            // Create a decorated cache from an initialized cache.
            SelfPopulatingCache selfPopulatingCache = new SelfPopulatingCache(cache, factory);
            // replace the cache in the manager to be sure that everybody is using the decorated instance.
            cacheManager.replaceCacheWithDecoratedCache(cache, selfPopulatingCache);
            return selfPopulatingCache;
        } else {
            return (SelfPopulatingCache) cacheManager.getEhcache(cacheName);
        }
    }
}