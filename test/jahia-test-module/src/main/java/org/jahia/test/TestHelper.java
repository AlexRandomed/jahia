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
 * JAHIA'S DUAL LICENSING IMPORTANT INFORMATION
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
package org.jahia.test;

import org.apache.commons.lang.StringUtils;
import org.jahia.registries.ServicesRegistry;
import org.jahia.services.content.JCRNodeWrapper;
import org.jahia.services.content.JCRSessionFactory;
import org.jahia.services.content.JCRSessionWrapper;
import org.jahia.services.content.decorator.JCRSiteNode;
import org.jahia.services.importexport.ImportExportBaseService;
import org.jahia.services.sites.JahiaSite;
import org.jahia.services.sites.JahiaSitesService;
import org.jahia.services.usermanager.JahiaUser;
import org.jahia.settings.SettingsBean;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.FileSystemResource;
import org.springframework.util.CollectionUtils;

import javax.jcr.ItemExistsException;
import javax.jcr.Node;
import javax.jcr.NodeIterator;
import javax.jcr.RepositoryException;
import javax.jcr.lock.LockException;
import javax.jcr.nodetype.ConstraintViolationException;
import javax.jcr.nodetype.NoSuchNodeTypeException;
import javax.jcr.version.VersionException;
import java.io.*;
import java.util.Set;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

/**
 * User: toto
 * Date: Feb 12, 2009
 * Time: 4:49:40 PM
 * 
 */
public class TestHelper {

    static Logger logger = LoggerFactory.getLogger(TestHelper.class);
    public static final String TCK_TEMPLATES = "Jahia Test";
    public static final String WEB_TEMPLATES = "templates-web";
    public static final String WEB_BLUE_TEMPLATES = "templates-web-blue";
    public static final String WEB_SPACE_TEMPLATES = "templates-web-space";
    public static final String INTRANET_TEMPLATES = "templates-intranet";

    public static JahiaSite createSite(String name) throws Exception {
        return createSite(name, "localhost" + System.currentTimeMillis(), WEB_TEMPLATES, null, null, null);
    }

    public static JahiaSite createSite(String name, String templateSet) throws Exception {
        return createSite(name, "localhost" + System.currentTimeMillis(), templateSet, null, null, null);
    }

    public static JahiaSite createSite(String name, Set<String> languages, Set<String> mandatoryLanguages, boolean mixLanguagesActive) throws Exception {
        createSite(name, "localhost" + System.currentTimeMillis(), WEB_TEMPLATES, null, null, null);
        final JCRSessionWrapper session = JCRSessionFactory.getInstance().getCurrentUserSession();
        JCRSiteNode site = (JCRSiteNode) session.getNode("/sites/" + name);
        if (!CollectionUtils.isEmpty(languages) && !languages.equals(site.getLanguages())) {
            site.setLanguages(languages);
        }
        if (!CollectionUtils.isEmpty(mandatoryLanguages) && !mandatoryLanguages.equals(site.getMandatoryLanguages())) {
            site.setMandatoryLanguages(mandatoryLanguages);
        }
        if (mixLanguagesActive != site.isMixLanguagesActive()) {
            site.setMixLanguagesActive(mixLanguagesActive);
        }
        session.save();
        return site;
    }

    public static JahiaSite createSite(String name, String serverName, String templateSet, String[] modulesToDeploy) throws Exception {
        return createSite(name, serverName, templateSet, null, null,modulesToDeploy);
    }

    public static JahiaSite createSite(String name, String serverName, String templateSet) throws Exception {
        return createSite(name, serverName, templateSet, null, null,null);
    }

    public static JahiaSite createSite(String name, String serverName, String templateSet,
                                       String prepackedZIPFile, String siteZIPName, String[] modulesToDeploy) throws Exception {
        modulesToDeploy = (modulesToDeploy == null) ? new String[0] : modulesToDeploy;

        JahiaUser admin = JahiaAdminUser.getAdminUser(0);

        JahiaSitesService service = ServicesRegistry.getInstance().getJahiaSitesService();
        JahiaSite site = service.getSiteByKey(name);

        if (site != null) {
            service.removeSite(site);
        }
        File siteZIPFile = null;
        File sharedZIPFile = null;
        try {
            if (!StringUtils.isEmpty(prepackedZIPFile)) {
                ZipInputStream zis = null;
                OutputStream os = null;
                try {
                    zis = new ZipInputStream(new FileInputStream(new File(prepackedZIPFile)));
                    ZipEntry z = null;
                    while ((z = zis.getNextEntry()) != null) {
                        if (siteZIPName.equalsIgnoreCase(z.getName())
                                || "users.zip".equals(z.getName())) {
                            File zipFile = File.createTempFile("import", ".zip");
                            os = new FileOutputStream(zipFile);
                            byte[] buf = new byte[4096];
                            int r;
                            while ((r = zis.read(buf)) > 0) {
                                os.write(buf, 0, r);
                            }
                            os.close();
                            if ("users.zip".equals(z.getName())) {
                                sharedZIPFile = zipFile;
                            } else {
                                siteZIPFile = zipFile;
                            }
                        }
                    }
                } catch (IOException e) {
                    logger.error(e.getMessage(), e);
                } finally {
                    if (os != null) {
                        try {
                            os.close();
                        } catch (IOException e) {
                            logger.error(e.getMessage(), e);
                        }
                    }
                    if (zis != null) {
                        try {
                            zis.close();
                        } catch (IOException e) {
                            logger.error(e.getMessage(), e);
                        }
                    }
                }
            }
            if (sharedZIPFile != null) {
                try {
                    ImportExportBaseService.getInstance().importSiteZip(sharedZIPFile != null ? new FileSystemResource(sharedZIPFile) : null, null, null);
                } catch (RepositoryException e) {
                    logger.warn("shared.zip could not be imported", e);
                }
            }
            site = service.addSite(admin, name, serverName, name, name, SettingsBean.getInstance().getDefaultLocale(),
                    templateSet, modulesToDeploy, siteZIPFile == null ? "noImport" : "fileImport", siteZIPFile != null ? new FileSystemResource(siteZIPFile) : null,
                    null, false, false, null);
            site = service.getSiteByKey(name);
        } finally {
            if (sharedZIPFile != null) {
                sharedZIPFile.delete();
            }
            if (siteZIPFile != null) {
                siteZIPFile.delete();
            }
        }

        return site;
    }

    public static JahiaSite createSite(String name, String serverName, String templateSet,
                                       String prepackedZIPFile, String siteZIPName) throws Exception {
            return createSite(name, serverName, templateSet, prepackedZIPFile, siteZIPName, null);
    }

    public static void deleteSite(String name) throws Exception {
        JahiaSitesService service = ServicesRegistry.getInstance().getJahiaSitesService();
        JahiaSite site = service.getSiteByKey(name);
        if (site != null)
            service.removeSite(site);
    }

    public static int createSubPages(Node currentNode, int level, int nbChildren) throws RepositoryException, LockException, ConstraintViolationException, NoSuchNodeTypeException, ItemExistsException, VersionException {
       return createSubPages(currentNode, level, nbChildren, null);
    }
    
    public static int createSubPages(Node currentNode, int level, int nbChildren, String titlePrefix) throws RepositoryException, LockException, ConstraintViolationException, NoSuchNodeTypeException, ItemExistsException, VersionException {
        int pagesCreated = 0;
        if (!currentNode.isCheckedOut()) {
            currentNode.checkout();
        }
        for (int i = 0; i < nbChildren; i++) {
            Node newSubPage = currentNode.addNode("child" + Integer.toString(i), "jnt:page");
            newSubPage.setProperty("j:templateName", "simple");
            if (titlePrefix != null) {
                newSubPage.setProperty("jcr:title",
                        titlePrefix + Integer.toString(i));
            }
            pagesCreated++;
        }
        return pagesCreated;
    }

    /**
     * Little utility method to easily create lists of content.
     *
     * @param parentNode
     * @param listName
     * @param elementCount
     * @param textPrefix
     * @throws RepositoryException
     * @throws LockException
     * @throws ConstraintViolationException
     * @throws NoSuchNodeTypeException
     * @throws ItemExistsException
     * @throws VersionException
     */
    public static JCRNodeWrapper createList(JCRNodeWrapper parentNode, String listName, int elementCount, String textPrefix) throws RepositoryException, LockException, ConstraintViolationException, NoSuchNodeTypeException, ItemExistsException, VersionException {
        JCRNodeWrapper contentList = parentNode.addNode(listName, "jnt:contentList");

        for (int i = 0; i < elementCount; i++) {
            JCRNodeWrapper textNode = contentList.addNode(listName + "_text" + Integer.toString(i), "jnt:mainContent");
            textNode.setProperty("jcr:title", textPrefix + Integer.toString(i));
            textNode.setProperty("body", textPrefix + Integer.toString(i));
        }
        return contentList;
    }

    /**
     * Utility method to dump a part of a content tree into a String.
     *
     * @param stringBuilder
     * @param startNode
     * @param depth         usually 0 when called initially, it is incremented to mark the current depth in the tree.
     * @param logAsError
     * @return
     * @throws RepositoryException
     */
    public static StringBuilder dumpTree(StringBuilder stringBuilder, Node startNode, int depth, boolean logAsError) throws RepositoryException {
        for (int i = 0; i < depth; i++) {
            if (i == 0) {
                stringBuilder.append("+-");
            } else {
                stringBuilder.append("--");
            }
        }
        stringBuilder.append(startNode.getName());
        stringBuilder.append(" = ");
        stringBuilder.append(startNode.getIdentifier());
        stringBuilder.append("\n");
        NodeIterator childNodeIter = startNode.getNodes();
        while (childNodeIter.hasNext()) {
            Node currentChild = childNodeIter.nextNode();
            stringBuilder = dumpTree(stringBuilder, currentChild, depth + 1, logAsError);
        }
        return stringBuilder;
    }


}
