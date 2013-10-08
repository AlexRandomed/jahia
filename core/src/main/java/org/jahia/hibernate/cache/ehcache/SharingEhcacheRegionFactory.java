/**
 * This file is part of Jahia, next-generation open source CMS:
 * Jahia's next-generation, open source CMS stems from a widely acknowledged vision
 * of enterprise application convergence - web, search, document, social and portal -
 * unified by the simplicity of web content management.
 *
 * For more information, please visit http://www.jahia.com.
 *
 * Copyright (C) 2002-2013 Jahia Solutions Group SA. All rights reserved.
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

package org.jahia.hibernate.cache.ehcache;

import net.sf.ehcache.CacheManager;

import org.hibernate.cache.CacheException;
import org.hibernate.cache.ehcache.EhCacheRegionFactory;
import org.hibernate.cfg.Settings;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Properties;

/**
 * A sharing Hibernate Ehcache provider that can re-use a CacheManager provided by Jahia.
 * This is useful for using Hibernate inside modules.
 */
public class SharingEhcacheRegionFactory extends EhCacheRegionFactory {

    private static final String EXISTING_CACHE_MANAGER_NAME = "org.jahia.hibernate.ehcache.existingCacheManagerName";

    private static final Logger LOG = LoggerFactory.getLogger(SharingEhcacheRegionFactory.class.getName());

    private static final long serialVersionUID = 827915208448076343L;

    @Override
    public void start(Settings settings, Properties properties) throws CacheException {
        if (manager != null) {
            LOG.warn("Attempt to restart an already started EhCacheProvider. Use sessionFactory.close() "
                    + " between repeated calls to buildSessionFactory. Using previously created EhCacheProvider."
                    + " If this behaviour is required, consider using SingletonEhCacheProvider.");
            return;
        }
        try {
            String existingCacheManagerName = null;
            if (properties != null) {
                existingCacheManagerName = (String) properties.get(EXISTING_CACHE_MANAGER_NAME);
            }
            if (existingCacheManagerName == null || existingCacheManagerName.length() == 0) {
                throw new CacheException("No existing cache manager name specified in configuration, property "
                        + EXISTING_CACHE_MANAGER_NAME + " was not set properly.");
            } else {
                manager = CacheManager.getCacheManager(existingCacheManagerName);
            }
        } catch (net.sf.ehcache.CacheException e) {
            if (e.getMessage().startsWith(
                    "Cannot parseConfiguration CacheManager. Attempt to create a new instance of "
                            + "CacheManager using the diskStorePath")) {
                throw new CacheException(
                        "Attempt to restart an already started EhCacheProvider. Use sessionFactory.close() "
                                + " between repeated calls to buildSessionFactory. Consider using SingletonEhCacheProvider. Error from "
                                + " ehcache was: " + e.getMessage());
            } else {
                throw e;
            }
        }

    }

    @Override
    public void stop() {
        // Do nothing; we let the real creator of the cache manager do the shutdown.
    }
}
