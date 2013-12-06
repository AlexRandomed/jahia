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
package org.jahia.osgi;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.commons.lang.StringUtils;
import org.jahia.data.templates.JahiaTemplatesPackage;
import org.jahia.services.SpringContextSingleton;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.ConstantException;
import org.springframework.core.Constants;

/**
 * Convenient utilities for Jahia OSGi bundles.
 * 
 * @author Sergiy Shyrkov
 */
public final class BundleUtils {

    private static final Constants BUNDLE_EVENTS = new Constants(BundleEvent.class);

    private static final Logger logger = LoggerFactory.getLogger(BundleUtils.class);

    private static Map<String, String[]> moduleForClass = new ConcurrentHashMap<String, String[]>();

    private static Map<String, Map<String, JahiaTemplatesPackage>> modules = new ConcurrentHashMap<String, Map<String, JahiaTemplatesPackage>>(
            64);

    /**
     * Returns a String representation for the given bundle event.
     * 
     * @param eventType
     *            OSGi <code>BundleEvent</code> given as an int
     * @return String representation for the bundle event
     * @see org.eclipse.gemini.blueprint.util.OsgiStringUtils
     */
    public static String bundleEventToString(int eventType) {
        try {
            return BUNDLE_EVENTS.toCode(Integer.valueOf(eventType), "");
        } catch (ConstantException cex) {
            return "Unknown";
        }

    }

    /**
     * Creates an instance of the {@link BundleDelegatingClassLoader} baked by the provided bundle and having Jahia root Spring context's
     * class loader as a parent.
     * 
     * @param bundle
     *            the bundle to create class loader for
     * @return an instance of the {@link BundleDelegatingClassLoader} baked by the provided bundle and having Jahia root Spring context's
     *         class loader as a parent
     */
    public static ClassLoader createBundleClassLoader(Bundle bundle) {
        return BundleDelegatingClassLoader.createBundleClassLoaderFor(bundle, SpringContextSingleton.getInstance()
                .getContext().getClassLoader());
    }

    /**
     * Find the bundle that is represented by the specified module and version.
     * 
     * @param moduleId
     *            the module Id
     * @param version
     *            the module version
     * @return the bundle for the specified module and version or <code>null</code> if the corresponding bundle is not present
     */
    public static Bundle getBundle(String moduleId, String version) {
        for (Bundle bundle : FrameworkService.getBundleContext().getBundles()) {
            String n = getModuleId(bundle);
            if (StringUtils.equals(n, moduleId)) {
                String v = getModuleVersion(bundle);
                if (StringUtils.equals(v, version)) {
                    return bundle;
                }
            }
        }
        return null;
    }

    /**
     * Returns the bundle display name containing module name (ID) and the version.
     * 
     * @param bundle
     *            the bundle to get display name for
     * @return the bundle display name containing module name (ID) and the version
     */
    public static String getDisplayName(Bundle bundle) {
        return getModuleId(bundle) + " v" + getModuleVersion(bundle);
    }
    
    /**
     * Returns the module instance that corresponds to the provided OSGi bundle. If the instance is not present yet, creates it and stores
     * internally.
     * 
     * @param bundle
     *            the corresponding OSGi bundle
     * @return the module instance that corresponds to the provided OSGi bundle
     */
    public static JahiaTemplatesPackage getModule(Bundle bundle) {
        JahiaTemplatesPackage pkg = null;

        String moduleId = getModuleId(bundle);
        String version = getModuleVersion(bundle);

        Map<String, JahiaTemplatesPackage> moduleVersions = modules.get(moduleId);
        if (moduleVersions == null) {
            moduleVersions = new ConcurrentHashMap<String, JahiaTemplatesPackage>(1);
            modules.put(moduleId, moduleVersions);
        } else {
            pkg = moduleVersions.get(version);
        }

        if (pkg == null) {
            logger.info("Building module instance for bundle {} v{}", moduleId, version);
            pkg = JahiaBundleTemplatesPackageHandler.build(bundle);
            if (pkg != null) {
                moduleVersions.put(version, pkg);
            } else {
                logger.warn(
                        "Bundle {} seems to be not a valid Jahia module. Cannot build JahiaTemplatesPackage instance for it",
                        bundle.getSymbolicName());
                logger.info("The following manifest headers were found in the bundle: \n{}", bundle.getHeaders());
                throw new IllegalArgumentException("Bundle " + bundle.getSymbolicName()
                        + " is not a valid Jahia module");
            }
        }

        return pkg;
    }

    /**
     * Returns the module name read from the provided bundle.
     * 
     * @param bundle
     *            the bundle to read module name from
     * @return the module name read from the provided bundle
     */
    public static String getModuleId(Bundle bundle) {
        return bundle.getSymbolicName();
    }

    /**
     * Returns a version of the module read from the provided bundle.
     * 
     * @param bundle
     *            the bundle to read the module version from
     * @return a version of the module read from the provided bundle
     */
    public static String getModuleVersion(Bundle bundle) {
        return StringUtils.defaultIfEmpty((String) bundle.getHeaders().get("Implementation-Version"), bundle
                .getVersion().toString());
    }

    /**
     * Returns <code>true</code> if the provided bundle represents Jahia-related bundle (either a module or a service).
     * 
     * @param bundle
     *            the OSGi bundle to check
     * @return <code>true</code> if the provided bundle represents Jahia-related bundle (either a module or a service)
     */
    public static boolean isJahiaBundle(Bundle bundle) {
        return isJahiaModuleBundle(bundle)
                || StringUtils.defaultString((String) bundle.getHeaders().get("Bundle-Category")).toLowerCase()
                        .contains("jahia");
    }

    /**
     * Returns <code>true</code> if the provided bundle represents Jahia module.
     * 
     * @param bundle
     *            the OSGi bundle to check
     * @return <code>true</code> if the provided bundle represents Jahia module
     */
    public static boolean isJahiaModuleBundle(Bundle bundle) {
        return bundle.getHeaders().get("Jahia-Module-Type") != null;
    }

    public static Class<?> loadModuleClass(String className) throws ClassNotFoundException {
        Class<?> clazz = null;
        String[] moduleKey = moduleForClass.get(className); // [moduleId, moduleVersion]
        if (moduleKey != null) {
            ClassLoader cl = null;
            Map<String, JahiaTemplatesPackage> versions = modules.get(moduleKey[0]);
            if (versions != null) {
                JahiaTemplatesPackage pkg = versions.get(moduleKey[1]);
                if (pkg != null) {
                    cl = pkg.getClassLoader();
                }
            }
            if (cl == null) {
                moduleForClass.remove(className);
            } else {
                return cl.loadClass(className);
            }
        }

        for (Map<String, JahiaTemplatesPackage> moduleVersions : modules.values()) {
            for (JahiaTemplatesPackage pkg : moduleVersions.values()) {
                if (pkg.getClassLoader() != null) {
                    try {
                        clazz = pkg.getClassLoader().loadClass(className);
                        moduleForClass
                                .put(className, new String[] { pkg.getId(), pkg.getVersion().toString() });
                        return clazz;
                    } catch (ClassNotFoundException e) {
                        // continue searching class in other modules
                    }
                }
            }
        }

        throw new ClassNotFoundException("Unable to find class '" + className + "' in the class loaders of modules");
    }

    /**
     * Removes the module instance that corresponds to the provided OSGi bundle from internal registry.
     * 
     * @param bundle
     *            the corresponding OSGi bundle
     */
    public static void unregisterModule(Bundle bundle) {
        String moduleId = getModuleId(bundle);
        String version = getModuleVersion(bundle);

        Map<String, JahiaTemplatesPackage> moduleVersions = modules.get(moduleId);
        if (moduleVersions != null) {
            JahiaTemplatesPackage pkg = moduleVersions.remove(version);
            if (moduleVersions.isEmpty()) {
                modules.remove(moduleId);
            }
            pkg.setClassLoader(null);
        }
    }
}