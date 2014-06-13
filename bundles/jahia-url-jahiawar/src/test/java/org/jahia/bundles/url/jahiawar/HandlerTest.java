/**
 * ==========================================================================================
 * =                   JAHIA'S DUAL LICENSING - IMPORTANT INFORMATION                       =
 * ==========================================================================================
 *
 *     Copyright (C) 2002-2014 Jahia Solutions Group SA. All rights reserved.
 *
 *     THIS FILE IS AVAILABLE UNDER TWO DIFFERENT LICENSES:
 *     1/GPL OR 2/JSEL
 *
 *     1/ GPL
 *     ======================================================================================
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
 *     describing the FLOSS exception, also available here:
 *     http://www.jahia.com/license"
 *
 *     2/ JSEL - Commercial and Supported Versions of the program
 *     ======================================================================================
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
 *
 * ==========================================================================================
 * =                                   ABOUT JAHIA                                          =
 * ==========================================================================================
 *
 *     Rooted in Open Source CMS, Jahia’s Digital Industrialization paradigm is about
 *     streamlining Enterprise digital projects across channels to truly control
 *     time-to-market and TCO, project after project.
 *     Putting an end to “the Tunnel effect”, the Jahia Studio enables IT and
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
 */
package org.jahia.bundles.url.jahiawar;

import org.apache.commons.io.IOUtils;
import org.jahia.utils.osgi.BundleUtils;
import org.jahia.utils.osgi.ManifestValueClause;
import org.junit.Assert;
import org.junit.Test;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.text.MessageFormat;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.jar.Attributes;
import java.util.jar.JarEntry;
import java.util.jar.JarInputStream;

/**
 * Unit test for {@link Handler}.
 */
public class HandlerTest {

    /**
     * Protocol handler can be used.
     *
     * @throws java.io.IOException - Unexpected
     */
    @Test
    public void use()
            throws IOException, URISyntaxException {
        // System.setProperty( "java.protocol.handler.pkgs", "org.jahia.bundles.url.jahiawar" );
        System.setProperty("org.jahia.bundles.url.jahiawar.importedPackages", "");
        System.setProperty("org.jahia.bundles.url.jahiawar.excludedImportPackages", "templates-wise=org.jahia.modules.docspace.rules");
        System.setProperty("org.jahia.bundles.url.jahiawar.excludedExportPackages", "templates-wise=org.jahia.modules.social");

        Map<String,String[]> contentChecks = new HashMap<String,String[]>();

        JarInputStream jarInputStream = processWar("https://devtools.jahia.com/nexus/content/groups/public/org/jahia/modules/forum/1.3/forum-1.3.war", contentChecks);
        Attributes mainAttributes = jarInputStream.getManifest().getMainAttributes();

        Assert.assertEquals("Bundle-SymbolicName", "forum", mainAttributes.getValue("Bundle-SymbolicName"));
        Assert.assertEquals("Bundle-Name", "Jahia Forum", mainAttributes.getValue("Bundle-Name"));
        Assert.assertEquals("Bundle-ClassPath header is not valid", ".,WEB-INF/lib/forum-1.3.jar", mainAttributes.getValue("Bundle-ClassPath"));
        Assert.assertEquals("Bundle-Version header is not valid", "1.3", mainAttributes.getValue("Bundle-Version"));

        List<ManifestValueClause> importPackageHeaderClauses = BundleUtils.getHeaderClauses("Import-Package", mainAttributes.getValue("Import-Package"));
        assertPackagesPresent("Missing expected package {0} in Import-Package header clause", importPackageHeaderClauses, new String[]{
                // check for imports coming from rules DRL file
                "org.jahia.services.content",
                "org.jahia.services.content.rules",
                "org.jahia.services.render",
                "org.jahia.services.search",
                "org.apache.commons.lang.time",
                "org.slf4j",
                // check for imports coming from JSP files
                "org.jahia.services.render.scripting"
        });

        URI firstModuleURI = new URI("jahiawar:https://devtools.jahia.com/nexus/content/groups/public/org/jahia/modules/forum/1.3/forum-1.3.war");
        String modulePath = firstModuleURI.getPath();

        // now let's try with another module
        jarInputStream = processWar("https://devtools.jahia.com/nexus/content/groups/public/org/jahia/modules/translateworkflow/1.2/translateworkflow-1.2.war", contentChecks);
        mainAttributes = jarInputStream.getManifest().getMainAttributes();

        importPackageHeaderClauses = BundleUtils.getHeaderClauses("Import-Package", mainAttributes.getValue("Import-Package"));
        assertPackagesPresent("Missing expected package {0} in Import-Package header clause", importPackageHeaderClauses, new String[]{
                // check for imports coming from jPDL workflow definition file
                "org.jahia.services.workflow.jbpm"
        });


        jarInputStream = processWar("https://devtools.jahia.com/nexus/content/groups/public/org/jahia/modules/ldap/1.3/ldap-1.3.war", contentChecks);
        mainAttributes = jarInputStream.getManifest().getMainAttributes();

        jarInputStream = processWar("https://devtools.jahia.com/nexus/content/groups/public/org/jahia/modules/social/1.5/social-1.5.war", contentChecks);
        mainAttributes = jarInputStream.getManifest().getMainAttributes();

        importPackageHeaderClauses = BundleUtils.getHeaderClauses("Import-Package", mainAttributes.getValue("Import-Package"));
        assertPackagesPresent("Missing expected package {0} in Import-Package header clause", importPackageHeaderClauses, new String[]{
                // check for imports coming from Spring module application context file.
                "org.jahia.services.content.rules"
        });

        URL abtestingWarURL = this.getClass().getClassLoader().getResource("abtesting-1.0-SNAPSHOT.war");
        jarInputStream = processWar(abtestingWarURL.toExternalForm(), contentChecks);
        mainAttributes = jarInputStream.getManifest().getMainAttributes();

        URL jahiaOneTemplatesWarURL = this.getClass().getClassLoader().getResource("jahiaone-templates-1.0-SNAPSHOT.war");
        contentChecks.put("META-INF/definitions.cnd", new String[] { "!richtext[ckeditor.customConfig='$context/modules/jahiaone-templates/javascript/ckconfig.js']", "richtext" });
        contentChecks.put("genericnt_navbar/html/navbar.menu.groovy", new String[] { "renderContext.site.home", "!currentNode.resolveSite.home" });
        jarInputStream = processWar(jahiaOneTemplatesWarURL.toExternalForm(), contentChecks);
        mainAttributes = jarInputStream.getManifest().getMainAttributes();

    }

    private JarInputStream processWar(String warUrl, Map<String,String[]> contentChecks) throws IOException {
        URL jahiaWarURL = new URL(null, "jahiawar:" + warUrl, new Handler());
        System.out.println("Processing URL " + jahiaWarURL + "...");
        JarInputStream jarInputStream = new JarInputStream(jahiaWarURL.openStream());
        dumpManifest(jarInputStream);
        dumpJarEntries(jarInputStream, contentChecks);
        return jarInputStream;
    }

    private void dumpManifest(JarInputStream jarInputStream) throws IOException {
        System.out.println("MANIFEST.MF:");
        System.out.println("------------");
        StringWriter stringWriter = new StringWriter();
        BundleUtils.dumpManifestHeaders(jarInputStream, new PrintWriter(stringWriter));
        System.out.println(stringWriter.toString());
    }

    private void dumpJarEntries(JarInputStream jarInputStream, Map<String,String[]> contentChecks) throws IOException {
        JarEntry jarEntry;
        System.out.println("JAR contents:");
        System.out.println("-------------");
        while ((jarEntry = jarInputStream.getNextJarEntry()) != null) {
            displayEntryInfo(null, jarEntry);
            if (contentChecks != null && contentChecks.keySet().contains(jarEntry.getName())) {
                String[] contentsToFind = contentChecks.get(jarEntry.getName());
                String entryContent = IOUtils.toString(jarInputStream);
                for (String contentToFind : contentsToFind) {
                    if (contentToFind.startsWith("!")) {
                        contentToFind = contentToFind.substring(1);
                        Assert.assertTrue("Content " + contentToFind + " found in entry " + jarEntry.getName(), !entryContent.contains(contentToFind));
                    } else {
                        Assert.assertTrue("Content " + contentToFind + " not found in entry " + jarEntry.getName(), entryContent.contains(contentToFind));
                    }
                }
            }
            if (jarEntry.getName().endsWith(".jar")) {
                JarInputStream embeddedJar = new JarInputStream(jarInputStream);
                JarEntry embeddedJarEntry = null;
                while ((embeddedJarEntry = embeddedJar.getNextJarEntry()) != null) {
                    displayEntryInfo("    ",embeddedJarEntry);
                }
            }
        }
    }

    private void displayEntryInfo(String padding, JarEntry jarEntry) {
        if (padding == null) {
            padding = "";
        }
        System.out.println(padding + jarEntry.getName() + " size=" + jarEntry.getSize() + " compressedSize=" + jarEntry.getCompressedSize());
    }

    private boolean clauseListContainsPackage(List<ManifestValueClause> manifestValueClauses, String packageName) {
        for (ManifestValueClause manifestValueClause : manifestValueClauses) {
            if (manifestValueClause.getPaths().contains(packageName)) {
                return true;
            }
        }
        return false;
    }

    private boolean clauseListContainsPackageAndVersion(List<ManifestValueClause> manifestValueClauses, String packageName, String version) {
        for (ManifestValueClause manifestValueClause : manifestValueClauses) {
            if (manifestValueClause.getPaths().contains(packageName)) {
                if (manifestValueClause.getAttributes().get("version").equals(version)) {
                    return true;
                }
            }
        }
        return false;
    }

    private void assertPackagesPresent(String message, List<ManifestValueClause> manifestValueClauses, String[] packageNames) {
        for (String packageName : packageNames) {
            String assertionMessage = MessageFormat.format(message, packageName);
            Assert.assertTrue(assertionMessage, clauseListContainsPackage(manifestValueClauses, packageName));
        }
    }
}