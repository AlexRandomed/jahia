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
package org.jahia.bundles.url.jahiawar.internal;

import org.jahia.bundles.url.jahiawar.ServiceConstants;
import org.ops4j.lang.NullArgumentException;
import org.ops4j.util.property.PropertyResolver;
import org.ops4j.util.property.PropertyStore;

import java.util.*;

/**
 * Service configuration implementation
 */
public class ConfigurationImpl
    extends PropertyStore
    implements Configuration
{

    /**
     * Property resolver. Cannot be null.
     */
    private final PropertyResolver propertyResolver;

    /**
     * Creates a new service configuration.
     *
     * @param propertyResolver propertyResolver used to resolve properties; mandatory
     */
    public ConfigurationImpl( final PropertyResolver propertyResolver )
    {
        NullArgumentException.validateNotNull(propertyResolver, "Property resolver");
        this.propertyResolver = propertyResolver;
    }

    /**
     * @see Configuration#getCertificateCheck()
     */
    public Boolean getCertificateCheck()
    {
        if( !contains( ServiceConstants.PROPERTY_CERTIFICATE_CHECK ) )
        {
            return set( ServiceConstants.PROPERTY_CERTIFICATE_CHECK,
                        Boolean.valueOf(propertyResolver.get(ServiceConstants.PROPERTY_CERTIFICATE_CHECK))
            );
        }
        return get( ServiceConstants.PROPERTY_CERTIFICATE_CHECK );
    }

    @Override
    public Map<String,Set<String>> getImportedPackages() {
        if( !contains( ServiceConstants.PROPERTY_IMPORTED_PACKAGES) )
        {
            String importedPackagePropValue = propertyResolver.get(ServiceConstants.PROPERTY_IMPORTED_PACKAGES);
            if (importedPackagePropValue != null) {
                Map<String,Set<String>> importedPackages = getBundlePackageMappings(importedPackagePropValue);
                return set(ServiceConstants.PROPERTY_IMPORTED_PACKAGES, importedPackages);
            } else {
                return set(ServiceConstants.PROPERTY_IMPORTED_PACKAGES, Collections.<String,Set<String>>emptyMap());
            }
        }
        return get( ServiceConstants.PROPERTY_IMPORTED_PACKAGES);
    }

    @Override
    public Map<String,Set<String>> getExcludedImportPackages() {
        if( !contains( ServiceConstants.PROPERTY_EXCLUDED_IMPORT_PACKAGES) )
        {
            String excludedImportPackagesPropValue = propertyResolver.get(ServiceConstants.PROPERTY_EXCLUDED_IMPORT_PACKAGES);
            if (excludedImportPackagesPropValue != null) {
                Map<String,Set<String>> excludedImportPackages = getBundlePackageMappings(excludedImportPackagesPropValue);
                return set(ServiceConstants.PROPERTY_EXCLUDED_IMPORT_PACKAGES, excludedImportPackages);
            } else {
                return set(ServiceConstants.PROPERTY_EXCLUDED_IMPORT_PACKAGES, Collections.<String,Set<String>>emptyMap());
            }
        }
        return get( ServiceConstants.PROPERTY_EXCLUDED_IMPORT_PACKAGES);
    }

    @Override
    public Map<String, Set<String>> getExcludedExportPackages() {
        if( !contains( ServiceConstants.PROPERTY_EXCLUDED_EXPORT_PACKAGES) )
        {
            String excludedExportPackagesPropValue = propertyResolver.get(ServiceConstants.PROPERTY_EXCLUDED_EXPORT_PACKAGES);
            if (excludedExportPackagesPropValue != null) {
                Map<String,Set<String>> excludedExportPackages = getBundlePackageMappings(excludedExportPackagesPropValue);
                return set(ServiceConstants.PROPERTY_EXCLUDED_EXPORT_PACKAGES, excludedExportPackages);
            } else {
                return set(ServiceConstants.PROPERTY_EXCLUDED_EXPORT_PACKAGES, Collections.<String,Set<String>>emptyMap());
            }
        }
        return get( ServiceConstants.PROPERTY_EXCLUDED_EXPORT_PACKAGES);
    }

    @Override
    public Set<String> getForbiddenJars() {
        if( !contains( ServiceConstants.PROPERTY_FORBIDDEN_JARS) )
        {
            String forbiddenJarsPropValue = propertyResolver.get(ServiceConstants.PROPERTY_FORBIDDEN_JARS);
            if (forbiddenJarsPropValue != null) {
                Set<String> forbiddenJars = new LinkedHashSet<String>();
                String[] forbiddenJarArray = forbiddenJarsPropValue.split(",");
                for (String forbiddenJar : forbiddenJarArray) {
                    forbiddenJars.add(forbiddenJar.trim());
                }
                return set(ServiceConstants.PROPERTY_FORBIDDEN_JARS, forbiddenJars);
            } else {
                return set(ServiceConstants.PROPERTY_FORBIDDEN_JARS, Collections.<String>emptySet());
            }
        }
        return get( ServiceConstants.PROPERTY_FORBIDDEN_JARS);
    }

    private Map<String,Set<String>> getBundlePackageMappings(String propertyValue) {
        Map<String,Set<String>> excludedImportPackages = new TreeMap<String,Set<String>>();
        String[] excludedImportPackageArray = propertyValue.split(",");
        for (String excludedImportPackage : excludedImportPackageArray) {
            String[] excludedImportPackageMapping = excludedImportPackage.split("=");
            String bundleName = "*";
            String bundleExcludedImportPackage = "";
            if (excludedImportPackageMapping.length == 2) {
                bundleName = excludedImportPackageMapping[0].trim();
                bundleExcludedImportPackage = excludedImportPackageMapping[1].trim();
            } else if (excludedImportPackageMapping.length == 1) {
                bundleExcludedImportPackage = excludedImportPackageMapping[0].trim();
                if (bundleExcludedImportPackage.length() == 0) {
                    // no value for the package name, we skip this entry.
                    continue;
                }
            } else {
                // no valid mapping, skip
                continue;
            }
            Set<String> bundleExcludedImportPackages = excludedImportPackages.get(bundleName);
            if (bundleExcludedImportPackages == null) {
                bundleExcludedImportPackages = new TreeSet<String>();
            }
            bundleExcludedImportPackages.add(bundleExcludedImportPackage);
            excludedImportPackages.put(bundleName, bundleExcludedImportPackages);
        }
        return excludedImportPackages;
    }

}