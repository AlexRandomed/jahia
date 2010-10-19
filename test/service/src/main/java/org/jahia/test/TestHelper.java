/**
 * This file is part of Jahia: An integrated WCM, DMS and Portal Solution
 * Copyright (C) 2002-2010 Jahia Solutions Group SA. All rights reserved.
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
 * Commercial and Supported Versions of the program
 * Alternatively, commercial and supported versions of the program may be used
 * in accordance with the terms contained in a separate written agreement
 * between you and Jahia Solutions Group SA. If you are unsure which license is appropriate
 * for your use, please contact the sales department at sales@jahia.com.
 */

package org.jahia.test;

import org.apache.axis.utils.StringUtils;
import org.apache.commons.codec.binary.Base64;
import org.jahia.admin.database.DatabaseScripts;
import org.jahia.bin.Jahia;
import org.jahia.exceptions.JahiaException;
import org.jahia.params.ProcessingContext;
import org.jahia.registries.ServicesRegistry;
import org.jahia.services.SpringContextSingleton;
import org.jahia.services.content.JCRNodeWrapper;
import org.jahia.services.importexport.ImportExportBaseService;
import org.jahia.services.sites.JahiaSite;
import org.jahia.services.sites.JahiaSitesService;
import org.jahia.services.usermanager.JahiaAdminUser;
import org.jahia.services.usermanager.JahiaUser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.CollectionUtils;

import javax.jcr.ItemExistsException;
import javax.jcr.Node;
import javax.jcr.NodeIterator;
import javax.jcr.RepositoryException;
import javax.jcr.lock.LockException;
import javax.jcr.nodetype.ConstraintViolationException;
import javax.jcr.nodetype.NoSuchNodeTypeException;
import javax.jcr.version.VersionException;
import javax.sql.DataSource;
import java.io.*;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.Statement;
import java.sql.Timestamp;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

/**
 * Created by IntelliJ IDEA.
 * User: toto
 * Date: Feb 12, 2009
 * Time: 4:49:40 PM
 * To change this template use File | Settings | File Templates.
 */
public class TestHelper {

    static Logger logger = LoggerFactory.getLogger(TestHelper.class);
    public static final String TCK_TEMPLATES = "Jahia Test";
    public static final String ACME_TEMPLATES = "templates-web";
    public static final String INTRANET_TEMPLATES = "templates-intranet";

    public static JahiaSite createSite(String name) throws Exception {
        return createSite(name, "localhost" + System.currentTimeMillis(), ACME_TEMPLATES, null, null);
    }

    public static JahiaSite createSite(String name, Set<String> languages, Set<String> mandatoryLanguages, boolean mixLanguagesActive) throws Exception {
        JahiaSite site = createSite(name, "localhost" + System.currentTimeMillis(), ACME_TEMPLATES, null, null);
        JahiaSitesService service = ServicesRegistry.getInstance().getJahiaSitesService();
        if (!CollectionUtils.isEmpty(languages) && !languages.equals(site.getLanguages())) {
            site.setLanguages(languages);
        }
        if (!CollectionUtils.isEmpty(mandatoryLanguages) && !mandatoryLanguages.equals(site.getMandatoryLanguages())) {
            site.setMandatoryLanguages(mandatoryLanguages);
        }
        if (mixLanguagesActive != site.isMixLanguagesActive()) {
            site.setMixLanguagesActive(mixLanguagesActive);
        }
        service.updateSite(site);
        return site;
    }

    public static JahiaSite createSite(String name, String serverName, String templateSet) throws Exception {
        return createSite(name, serverName, templateSet, null, null);
    }

    public static JahiaSite createSite(String name, String serverName, String templateSet,
                                       String prepackedZIPFile, String siteZIPName) throws Exception {

        ProcessingContext ctx = Jahia.getThreadParamBean();
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
                                || "shared.zip".equals(z.getName())) {
                            File zipFile = File.createTempFile("import", ".zip");
                            os = new FileOutputStream(zipFile);
                            byte[] buf = new byte[4096];
                            int r;
                            while ((r = zis.read(buf)) > 0) {
                                os.write(buf, 0, r);
                            }
                            os.close();
                            if ("shared.zip".equals(z.getName())) {
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
                    ImportExportBaseService.getInstance().importSiteZip(sharedZIPFile, null, null);
                } catch (RepositoryException e) {
                    logger.warn("shared.zip could not be imported", e);
                }
            }
            site = service.addSite(admin, name, serverName, name, name, ctx.getLocale(),
                    templateSet, siteZIPFile == null ? "noImport" : "fileImport", siteZIPFile,
                    null, false, false, null, ctx);
            ctx.setSite(site);
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

    public static void removeAllSites(JahiaSitesService service) throws JahiaException {
        final Iterator<JahiaSite> sites = service.getSites();
        while (sites.hasNext()) {
            JahiaSite jahiaSite = sites.next();
            service.removeSite(jahiaSite);
        }
    }

    public static void deleteSite(String name) throws Exception {
        JahiaSitesService service = ServicesRegistry.getInstance().getJahiaSitesService();
        JahiaSite site = service.getSiteByKey(name);
        if (site != null)
            service.removeSite(site);
    }

    public static void cleanDatabase() throws Exception {
        createDBTables();
        ServicesRegistry.getInstance().getCacheService().flushAllCaches();
    }

    /**
     * Insert the database tables described in the database script. Before the
     * insertion, since you're sure that the user want overwrite his database,
     * each table is dropped, table after table.
     *
     * @throws Exception an exception occured during the process.
     */
    private static void createDBTables() throws Exception {
        File object;
        List<String> sqlStatements;
        String line;
        final DatabaseScripts scripts = new DatabaseScripts();
        logger.info("Creating database tables...");

// construct script path...
        final StringBuffer script = new StringBuffer().append(Jahia.getThreadParamBean().settings().getJahiaDatabaseScriptsPath());
        script.append(File.separator);
        final DataSource bean = (DataSource) SpringContextSingleton.getInstance().getContext().getBean("dataSource");
        final Connection db = bean.getConnection();
        script.append(File.separator + db.getMetaData().getDatabaseProductName().toLowerCase() + ".script");

// get script runtime...
        try {
            object = new File(script.toString());
            sqlStatements = scripts.getSchemaSQL(object);
        } catch (Exception e) {
            logger.error("Jahia can't read the appropriate database script." + e);
            throw e;
        }

// drop each tables (if present) and (re-)create it after...
        final Statement statement = db.createStatement();
        for (Object sqlStatement : sqlStatements) {
            line = (String) sqlStatement;
            final String lowerCaseLine = line.toLowerCase();
            final int tableNamePos = lowerCaseLine.indexOf("create table");
            if (tableNamePos != -1) {
                final String tableName = line.substring("create table".length() +
                        tableNamePos,
                        line.indexOf("(")).trim();
                logger.debug("Creating table [" + tableName + "] ...");
                try {
                    statement.execute("DROP TABLE " + tableName);
                } catch (Throwable t) {
                    // ignore because if this fails it's ok
                    logger.debug("Drop failed on " + tableName + " but that's acceptable...");
                }
            }
            try {
                statement.execute(line);
                logger.debug("Executed sql : " + line);
            } catch (Exception e) {
                // first let's check if it is a DROP TABLE query, if it is,
                // we will just fail silently.
                String upperCaseLine = line.toUpperCase().trim();
                if (!upperCaseLine.startsWith("DROP") && !upperCaseLine.startsWith("ALTER TABLE")
                        && !upperCaseLine.startsWith("CREATE INDEX") && !upperCaseLine.startsWith("DELETE FROM")) {
                    logger.debug("Error while trying to execute query : " + line + " from script " + script.toString() + e);
// continue to propagate the exception upwards.
                    throw e;
                } else if (upperCaseLine.startsWith("CREATE INDEX")) {
                    logger.debug("Error while trying to execute query : " + line + e);
                }
            }
        }
        statement.close();
        insertDBCustomContent(db);
    }

    private static String encryptPassword(String password) {
        if (password == null) {
            return null;
        }

        if (password.length() == 0) {
            return null;
        }

        String result = null;

        try {
            MessageDigest md = MessageDigest.getInstance("SHA-1");
            if (md != null) {
                md.reset();
                md.update(password.getBytes());
                result = new String(Base64.encodeBase64(md.digest()));
            }
        } catch (NoSuchAlgorithmException ex) {

            result = null;
        }

        return result;
    }

    /**
     * Insert database custom data, like root user and properties.
     *
     * @throws Exception an exception occured during the process.
     */
    private static void insertDBCustomContent(Connection con) throws Exception {


// get two keys...
        final String rootName = "root";
        final int siteID0 = 0;
        final String rootKey = rootName + ":" + siteID0;
        final String grpKey0 = "administrators:" + siteID0;
// query insert root user...
        queryPreparedStatement(con, "INSERT INTO jahia_users(id_jahia_users, name_jahia_users, password_jahia_users, key_jahia_users) VALUES(0,?,?,?)",
                new Object[]{rootName, encryptPassword("root1234"), rootKey});

// query insert root first name...
        queryPreparedStatement(con, "INSERT INTO jahia_user_prop(id_jahia_users, name_jahia_user_prop, value_jahia_user_prop, provider_jahia_user_prop, userkey_jahia_user_prop) VALUES(0, 'firstname', ?, 'jahia',?)",
                new Object[]{"", rootKey});

// query insert root last name...
        queryPreparedStatement(con, "INSERT INTO jahia_user_prop(id_jahia_users, name_jahia_user_prop, value_jahia_user_prop, provider_jahia_user_prop, userkey_jahia_user_prop) VALUES(0, 'lastname', ?, 'jahia',?)",
                new Object[]{"", rootKey});

// query insert root e-mail address...
        queryPreparedStatement(con, "INSERT INTO jahia_user_prop(id_jahia_users, name_jahia_user_prop, value_jahia_user_prop, provider_jahia_user_prop, userkey_jahia_user_prop) VALUES(0, 'email', ?, 'jahia',?)",
                new Object[]{"", rootKey});

// query insert administrators group...
        queryPreparedStatement(con, "INSERT INTO jahia_grps(id_jahia_grps, name_jahia_grps, key_jahia_grps, siteid_jahia_grps) VALUES(?,?,?,null)",
                new Object[]{siteID0, "administrators", grpKey0});

// query insert administrators group access...
        queryPreparedStatement(con, "INSERT INTO jahia_grp_access(id_jahia_member, id_jahia_grps, membertype_grp_access) VALUES(?,?,1)",
                new Object[]{rootKey, grpKey0});

// create guest user
        queryPreparedStatement(con, "INSERT INTO jahia_users(id_jahia_users, name_jahia_users, password_jahia_users, key_jahia_users) VALUES(1,?,?,?)",
                new Object[]{"guest", "*", "guest:0"});

        queryPreparedStatement(con, "INSERT INTO jahia_version(install_number, build, release_number, install_date) VALUES(0, ?,?,?)",
                new Object[]{new Integer("12346789"), "132456" + "." + "123456", new Timestamp(System.currentTimeMillis())});


    }

    private static void queryPreparedStatement(Connection theConnection, String sqlCode, Object[] params)
            throws Exception {
        PreparedStatement ps = theConnection.prepareStatement(sqlCode);
        for (int i = 0; i < params.length; i++) {
            ps.setObject(i + 1, params[i]);
        }
        ps.execute();
        ps.close();
    } // end query


    public static int createSubPages(Node currentNode, int level, int nbChildren) throws RepositoryException, LockException, ConstraintViolationException, NoSuchNodeTypeException, ItemExistsException, VersionException {
        int pagesCreated = 0;
        if (level <= 0) return pagesCreated;
        if (!currentNode.isCheckedOut()) {
            currentNode.checkout();
        }
        for (int i = 0; i < nbChildren; i++) {
            Node newSubPage = currentNode.addNode("child" + Integer.toString(i), "jnt:page");
            pagesCreated++;
            pagesCreated += createSubPages(newSubPage, level - 1, nbChildren);
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
