/**
 * ==========================================================================================
 * =                   JAHIA'S DUAL LICENSING - IMPORTANT INFORMATION                       =
 * ==========================================================================================
 *
 *                                 http://www.jahia.com
 *
 *     Copyright (C) 2002-2016 Jahia Solutions Group SA. All rights reserved.
 *
 *     THIS FILE IS AVAILABLE UNDER TWO DIFFERENT LICENSES:
 *     1/GPL OR 2/JSEL
 *
 *     1/ GPL
 *     ==================================================================================
 *
 *     IF YOU DECIDE TO CHOOSE THE GPL LICENSE, YOU MUST COMPLY WITH THE FOLLOWING TERMS:
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
 *     describing the FLOSS exception, also available here:
 *     http://www.jahia.com/license"
 *
 *     2/ JSEL - Commercial and Supported Versions of the program
 *     ===================================================================================
 *
 *     IF YOU DECIDE TO CHOOSE THE JSEL LICENSE, YOU MUST COMPLY WITH THE FOLLOWING TERMS:
 *
 *     Alternatively, commercial and supported versions of the program - also known as
 *     Enterprise Distributions - must be used in accordance with the terms and conditions
 *     contained in a separate written agreement between you and Jahia Solutions Group SA.
 *
 *     If you are unsure which license is appropriate for your use,
 *     please contact the sales department at sales@jahia.com.
 *
 */
package org.jahia.bundles.extender.jahiamodules;

import org.jahia.services.render.scripting.bundle.BundleScriptResolver;
import org.ops4j.pax.swissbox.extender.BundleObserver;
import org.osgi.framework.Bundle;

import java.net.URL;
import java.util.List;

/**
 * Bundle observer for all scripts (JSP, Velocity, Freemarker, etc...)
 */
public class ScriptBundleObserver implements BundleObserver<URL> {

    private BundleScriptResolver bundleScriptResolver = null;

    public ScriptBundleObserver(BundleScriptResolver bundleScriptResolver) {
        this.bundleScriptResolver = bundleScriptResolver;
    }

    public BundleScriptResolver getBundleScriptResolver() {
        return bundleScriptResolver;
    }

    @Override
    public void addingEntries(Bundle bundle, List<URL> urls) {
        bundleScriptResolver.addBundleScripts(bundle, urls);
    }

    @Override
    public void removingEntries(Bundle bundle, List<URL> urls) {
        bundleScriptResolver.removeBundleScripts(bundle, urls);
    }
}
