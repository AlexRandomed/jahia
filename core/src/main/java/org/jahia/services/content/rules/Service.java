/**
 * This file is part of Jahia: An integrated WCM, DMS and Portal Solution
 * Copyright (C) 2002-2009 Jahia Solutions Group SA. All rights reserved.
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
package org.jahia.services.content.rules;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.drools.spi.KnowledgeHelper;
import org.jahia.api.Constants;
import org.jahia.bin.listeners.JahiaContextLoaderListener;
import org.jahia.content.ContentObject;
import org.jahia.content.ContentObjectKey;
import org.jahia.exceptions.JahiaException;
import org.jahia.exceptions.JahiaInitializationException;
import org.jahia.params.ProcessingContext;
import org.jahia.security.license.LicenseActionChecker;
import org.jahia.services.JahiaService;
import org.jahia.services.cache.CacheService;
import org.jahia.services.content.*;
import org.jahia.services.importexport.ImportAction;
import org.jahia.services.importexport.ImportExportBaseService;
import org.jahia.services.importexport.ImportJob;
import org.jahia.services.scheduler.BackgroundJob;
import org.jahia.services.scheduler.SchedulerService;
import org.jahia.services.sites.JahiaSite;
import org.jahia.services.sites.JahiaSitesService;
import org.jahia.services.tags.TaggingService;
import org.jahia.services.usermanager.JahiaSiteUserManagerService;
import org.jahia.services.usermanager.JahiaUser;
import org.jahia.services.usermanager.JahiaUserManagerService;
import org.jahia.services.workflow.WorkflowService;
import org.jahia.settings.SettingsBean;
import org.jahia.utils.LanguageCodeConverters;
import org.quartz.*;

import javax.jcr.*;
import javax.jcr.query.Query;
import javax.servlet.ServletException;
import java.io.*;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.Calendar;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

/**
 * Helper class for accessing Jahia service in rules.
 * User: toto
 * Date: 8 janv. 2008
 * Time: 12:04:29
 */
public class Service extends JahiaService {
    private static Logger logger = Logger.getLogger(Service.class);
    private static Service instance;

    private TaggingService taggingService;
    private JahiaSitesService sitesService;
    private JahiaSiteUserManagerService siteUserManager;
    private SchedulerService schedulerService;
    private CacheService cacheService;
    private JahiaUserManagerService userManager;

    public static synchronized Service getInstance() {
        if (instance == null) {
            instance = new Service();
        }
        return instance;
    }

    public void setPermissions(AddedNodeFact node, String acl, KnowledgeHelper drools) {
        User user = (User) drools.getWorkingMemory().getGlobal("user");
        StringTokenizer st = new StringTokenizer(acl, "|");
        while (st.hasMoreTokens()) {
            String ace = st.nextToken();
            int colon = ace.lastIndexOf(':');
            String userstring = ace.substring(0, colon);

            if (userstring.equals("self")) {
                userstring = "u:" + user.getName();
            }

            Node jcrNode = node.getNode();
            try {
                JCRNodeWrapperImpl.changePermissions(jcrNode, userstring, ace.substring(colon + 1));
            } catch (RepositoryException e) {
                logger.error(e.getMessage(), e);
            }
        }
    }

    public void revokeAllPermissions(AddedNodeFact node) {
        try {
            JCRNodeWrapperImpl.revokeAllPermissions(node.getNode());
        } catch (RepositoryException e) {
            logger.error(e.getMessage(), e);
        }
    }

    public void setAclInheritanceBreak(AddedNodeFact node, boolean aclInheritanceBreak) {
        try {
            JCRNodeWrapperImpl.setAclInheritanceBreak(node.getNode(), aclInheritanceBreak);
        } catch (RepositoryException e) {
            logger.error(e.getMessage(), e);
        }
    }

    public void importNode(AddedNodeFact node, KnowledgeHelper drools) throws RepositoryException {
        User user = (User) drools.getWorkingMemory().getGlobal("user");
        String uri = node.getPath();
        String name = node.getName();

        StringTokenizer st = new StringTokenizer(name, "_");

        String type = st.nextToken();
        if (type.equals("importInto")) {
            try {
                logger.info("Import file " + uri);
                String destKey = st.nextToken() + "_" + st.nextToken();

                ContentObject dest = ContentObject.getContentObjectInstance(ContentObjectKey.getInstance(destKey));
                JahiaSite site = sitesService.getSite(dest.getSiteID());

                Class<ImportJob> jobClass = ImportJob.class;

                JobDetail jobDetail = BackgroundJob.createJahiaJob("Import content to " + destKey, jobClass);

                JobDataMap jobDataMap;
                jobDataMap = jobDetail.getJobDataMap();

                jobDataMap.put(ImportJob.TARGET, destKey);
                jobDataMap.put(ImportJob.JOB_SITEKEY, site.getSiteKey());
                jobDataMap.put(BackgroundJob.JOB_DESTINATION_SITE, site.getID());
                jobDataMap.put(ImportJob.URI, uri);
                if (uri.toLowerCase().endsWith(".zip")) {
                    jobDataMap.put("contentType", "application/zip");
                } else {
                    jobDataMap.put("contentType", "application/xml");
                }
                jobDataMap.put(BackgroundJob.JOB_TYPE, "import");
                jobDataMap.put(ImportJob.DELETE_FILE, true);
                schedulerService.scheduleJobNow(jobDetail);
            } catch (Exception e) {
                logger.error("Error during import of file " + uri, e);
                cacheService.flushAllCaches();
            }
        }
        if (type.equals("siteImport")) {
            try {
                logger.info("Import site " + uri);
                //String sitename = st.nextToken() + "_" + st.nextToken();

                ZipEntry z;
                Node contentNode = node.getNode().getNode(Constants.JCR_CONTENT);
                ZipInputStream zis2 = new ZipInputStream(contentNode.getProperty(Constants.JCR_DATA).getBinary().getStream());

                Properties infos = new Properties();
                while ((z = zis2.getNextEntry()) != null) {
                    if ("site.properties".equals(z.getName())) {
                        infos.load(zis2);
                        zis2.closeEntry();

                        boolean siteKeyEx = sitesService.getSiteByKey((String) infos.get("sitekey")) != null || "".equals(
                                infos.get("sitekey"));
                        boolean serverNameEx = sitesService.getSite((String) infos.get(
                                "siteservername")) != null || "".equals(infos.get("siteservername"));
                        if (!user.getJahiaUser().isAdminMember(0)) {
                            return;
                        }
                        if (!siteKeyEx && !serverNameEx) {
                            if (!LicenseActionChecker.isAuthorizedByLicense(
                                    "org.jahia.actions.server.admin.sites.ManageSites", 0)) {
                                if (sitesService.getNbSites() > 0) {
                                    return;
                                }
                            }
                            // site import
                            String tpl = (String) infos.get("templatePackageName");
                            if ("".equals(tpl)) {
                                tpl = null;
                            }
                            try {
                                Locale locale = null;
                                for (Object obj : infos.keySet()) {
                                    String s = (String) obj;
                                    if (s.startsWith("language.") && s.endsWith(".rank")) {
                                        String code = s.substring(s.indexOf('.') + 1, s.lastIndexOf('.'));
                                        String rank = infos.getProperty(s);
                                        if (rank.equals("1")) {
                                            locale = LanguageCodeConverters.languageCodeToLocale(code);
                                        }
                                    }
                                }
                                ProcessingContext ctx = new ProcessingContext(SettingsBean.getInstance(),
                                        System.currentTimeMillis(), null,
                                        user.getJahiaUser(), null,
                                        ProcessingContext.EDIT);
                                sitesService.addSite(user.getJahiaUser(), infos.getProperty("sitetitle"), infos.getProperty(
                                        "siteservername"), infos.getProperty("sitekey"), infos.getProperty(
                                        "description"), locale, tpl, "importRepositoryFile", null, uri, true,
                                        false, infos.getProperty("originatingJahiaRelease"), ctx);
                            } catch (Exception e) {
                                logger.error("Cannot create site " + infos.get("sitetitle"), e);
                            }
                        }
                    }
                }
            } catch (Exception e) {
                logger.error("Error during import of file " + uri, e);
                cacheService.flushAllCaches();
            }

        } else if (type.endsWith("zip")) {
            try {
                processFileImport(prepareFileImports(node, node.getName()));
            } catch (IOException e) {
                logger.error(e);
            } catch (ServletException e) {
                logger.error(e);
            } catch (JahiaException e) {
                logger.error(e);
            }
        }

    }
    
    public void importXML(final AddedNodeFact targetNode, final String path, KnowledgeHelper drools)
            throws RepositoryException {
        InputStream is = null;
        try {
            is = JahiaContextLoaderListener.getServletContext().getResourceAsStream(path);
            if (is == null) {
                throw new FileNotFoundException("Unable to locate resource at the specified path: " + path);
            }
            JCRSessionWrapper session = targetNode.getNode().getSession();
            session.importXML(targetNode.getPath(), is, ImportUUIDBehavior.IMPORT_UUID_CREATE_NEW, true);
            session.save();
        } catch (Exception e) {
            logger.error("Error reading content of file " + path, e);
        } finally {
            IOUtils.closeQuietly(is);
        }
        logger.info("Content of the file '" + path + "' for target node " + targetNode + " imported successfully");
    }

    private List<Map<Object, Object>> prepareFileImports(AddedNodeFact node, String name) {
        try {
            Properties exportProps = new Properties();
            Node contentNode = node.getNode().getNode(Constants.JCR_CONTENT);
            ZipInputStream zis = new ZipInputStream(contentNode.getProperty(Constants.JCR_DATA).getBinary().getStream());
            ZipEntry z;
            Map<File, String> imports = new HashMap<File, String>();
            List<File> importList = new ArrayList<File>();
            while ((z = zis.getNextEntry()) != null) {
                File i = File.createTempFile("import", ".zip");
                OutputStream os = new FileOutputStream(i);
                byte[] buf = new byte[4096];
                int r;
                while ((r = zis.read(buf)) > 0) {
                    os.write(buf, 0, r);
                }
                os.close();

                String n = z.getName();
                if (n.equals("export.properties")) {
                    exportProps.load(new FileInputStream(i));
                    i.delete();

                } else if (n.equals("classes.jar")) {
                    i.delete();
                } else if (n.equals("site.properties") || ((n.startsWith("export_") && n.endsWith(".xml")))) {
                    // this is a single site import, stop everything and import
                    i.delete();
                    for (File file : imports.keySet()) {
                        file.delete();
                    }
                    imports.clear();
                    importList.clear();
                    File tempFile = File.createTempFile("import", ".zip");
                    IOUtils.copy(contentNode.getProperty(Constants.JCR_DATA).getBinary().getStream(), new FileOutputStream(
                            tempFile));
                    imports.put(tempFile, name);
                    importList.add(tempFile);
                    break;
                } else {
                    imports.put(i, n);
                    importList.add(i);
                }
            }

            List<Map<Object, Object>> importsInfos = new ArrayList<Map<Object, Object>>();
            Map<String, File> importsInfosSorted = new TreeMap<String, File>();
            File users = null;
            File serverPermissions = null;
            for (Iterator<File> iterator = importList.iterator(); iterator.hasNext();) {
                File i = iterator.next();
                String fileName = imports.get(i);
                Map<Object, Object> value = prepareSiteImport(i, imports.get(i));
                if (value != null) {
                    importsInfos.add(value);
                    if ("users.xml".equals(fileName)) {
                        users = i;
                    } else if ("serverPermissions.xml".equals(fileName)) {
                        serverPermissions = i;
                    } else {
                        importsInfosSorted.put(fileName, i);
                    }
                }
            }

            List<File> sorted = new LinkedList<File>(importsInfosSorted.values());
            if (serverPermissions != null) {
                sorted.add(0, serverPermissions);
            }
            if (users != null) {
                sorted.add(0, users);
            }
            return importsInfos;
        } catch (IOException e) {
            logger.error("Cannot read import file :" + e.getMessage());
        } catch (RepositoryException e) {
            logger.error(e);
        }
        return new ArrayList<Map<Object, Object>>();
    }

    private Map<Object, Object> prepareSiteImport(File i, String filename) throws IOException {
        Map<Object, Object> importInfos = new HashMap<Object, Object>();
        importInfos.put("importFile", i);
        importInfos.put("importFileName", filename);
        importInfos.put("selected", Boolean.TRUE);
        if (filename.endsWith(".xml")) {
            importInfos.put("type", "xml");
        } else {
            ZipEntry z;
            ZipInputStream zis2 = new ZipInputStream(new FileInputStream(i));
            boolean isSite = false;
            boolean isLegacySite = false;
            while ((z = zis2.getNextEntry()) != null) {
                if ("site.properties".equals(z.getName())) {
                    Properties p = new Properties();
                    p.load(zis2);
                    zis2.closeEntry();
                    importInfos.putAll(p);
                    importInfos.put("templates", importInfos.containsKey("templatePackageName") ? importInfos.get(
                            "templatePackageName") : "");
                    importInfos.put("oldsitekey", importInfos.get("sitekey"));
                    isSite = true;
                } else if (z.getName().startsWith("export_")) {
                    isLegacySite = true;
                }
            }
            importInfos.put("isSite", Boolean.valueOf(isSite));
            // todo import ga parameters
            if (isSite || isLegacySite) {
                importInfos.put("type", "site");
                if (!importInfos.containsKey("sitekey")) {
                    importInfos.put("sitekey", "");
                    importInfos.put("siteservername", "");
                    importInfos.put("sitetitle", "");
                    importInfos.put("description", "");
                    importInfos.put("mixLanguage", "false");
                    importInfos.put("templates", "");
                    importInfos.put("siteKeyExists", Boolean.TRUE);
                    importInfos.put("siteServerNameExists", Boolean.TRUE);
                } else {
                    try {
                        importInfos.put("siteKeyExists", Boolean.valueOf(
                                sitesService.getSiteByKey(
                                        (String) importInfos.get("sitekey")) != null || "".equals(importInfos.get(
                                        "sitekey"))));
                        importInfos.put("siteServerNameExists", Boolean.valueOf(
                                sitesService.getSite((String) importInfos.get(
                                        "siteservername")) != null || "".equals(importInfos.get("siteservername"))));
                    } catch (JahiaException e) {
                        logger.error(e.getMessage(), e);
                    }
                }
            } else {
                importInfos.put("type", "files");
            }

        }
        return importInfos;
    }

    private void processFileImport(List<Map<Object, Object>> importsInfos)
            throws IOException, ServletException, JahiaException {
        final JahiaUser user = userManager.lookupUser("root");
        boolean license = LicenseActionChecker.isAuthorizedByLicense("org.jahia.actions.server.admin.sites.ManageSites",
                0);
        boolean noMoreSite = false;

        boolean authorizedForServerPermissions = LicenseActionChecker.isAuthorizedByLicense(
                "org.jahia.actions.server.admin.permissions.ManageServerPermissions", 0);

        boolean doImportServerPermissions = false;
        if (authorizedForServerPermissions) {
            doImportServerPermissions = true;
        }
        ProcessingContext ctx = new ProcessingContext(SettingsBean.getInstance(), System.currentTimeMillis(), null,
                user, null, ProcessingContext.EDIT);

        for (Map<Object, Object> infos : importsInfos) {
            File file = (File) infos.get("importFile");
            if (infos.get("importFileName").equals("users.xml")) {
                ImportExportBaseService.getInstance().importUsers(file);
                break;
            }
        }

        for (Map<Object, Object> infos : importsInfos) {
            File file = (File) infos.get("importFile");
            if (infos.get("type").equals("files")) {
                try {
                    ImportExportBaseService.getInstance().importSiteZip(file, new ArrayList<ImportAction>(), null, ctx.getSite(), infos);
                } catch (RepositoryException e) {
                    e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
                }
            } else if (infos.get("type").equals("xml") && (infos.get("importFileName").equals(
                    "serverPermissions.xml") || infos.get("importFileName").equals("users.xml"))) {

            } else if (infos.get("type").equals("site")) {
                // site import
                String tpl = (String) infos.get("templates");
                if ("".equals(tpl)) {
                    tpl = null;
                }
                try {
                    if (!noMoreSite) {
                        sitesService.addSite(user, (String) infos.get(
                                "sitetitle"), (String) infos.get("siteservername"), (String) infos.get("sitekey"), "",
                                ctx.getLocale(), tpl,
                                "fileImport", file,
                                (String) infos.get(
                                        "importFileName"), true,
                                false, (String) infos.get("originatingJahiaRelease"), ctx);
                        noMoreSite = !license;
                    }
                } catch (Exception e) {
                    logger.error("Cannot create site " + infos.get("sitetitle"), e);
                }
            }

        }

        // import serverPermissions.xml
        if (doImportServerPermissions) {
            for (Map<Object, Object> infos : importsInfos) {
                File file = (File) infos.get("importFile");
                if (infos.get("importFileName").equals("serverPermissions.xml")) {
                    // pass the old-new site key information for server permissions
                    ImportExportBaseService.getInstance().importServerPermissions(ctx, new FileInputStream(file));
                }

            }
        }

    }

    public void incrementProperty(AddedNodeFact node, String propertyName,
                                  KnowledgeHelper drools) {
        final Node jcrNode = node.getNode();
        try {
            long aLong = 0;
            try {
                final Property property = jcrNode.getProperty(propertyName);
                aLong = property.getLong();
            } catch (PathNotFoundException e) {
                logger.debug("The property to increment " + propertyName + " does not exist yet", e);
            }
            jcrNode.setProperty(propertyName, aLong + 1);
        } catch (RepositoryException e) {
            logger.error("Error during increment of property " + propertyName + " for node " + node, e);
        }
    }

    public void addToProperty(AddedNodeFact node, String propertyName, List<?> value,
                              KnowledgeHelper drools) {
        final Node jcrNode = node.getNode();
        try {
            long aLong = 0;
            try {
                final Property property = jcrNode.getProperty(propertyName);
                aLong = property.getLong();
            } catch (PathNotFoundException e) {
                logger.debug("The property to increment " + propertyName + " does not exist yet", e);
            }
            jcrNode.setProperty(propertyName, aLong + Long.valueOf((String) value.get(0)));
        } catch (RepositoryException e) {
            logger.error("Error while adding " + value + " to property " + propertyName + " for node " + node, e);
        }
    }

    public void addNewTag(AddedNodeFact node, final String value, KnowledgeHelper drools) throws RepositoryException {
        String siteKey = node.getPath().startsWith("/sites/")? StringUtils.substringBefore(node.getPath().substring(7),"/"):null;
        if (siteKey == null) {
            logger.warn("Current site cannot be detected. Skip adding new tag for the node " + node.getPath());
            return;
        }
        taggingService.tag(node.getNode(), value, siteKey, true);
    }

    public void executeRuleLater(AddedNodeFact node, final String propertyName, final String ruleToExecute, KnowledgeHelper drools)
            throws JahiaException, RepositoryException {
        final String uuid = node.getNode().getIdentifier();
        final String jobName = "RULE_JOB_" + uuid + ruleToExecute;
        final JobDetail jobDetail = BackgroundJob.createJahiaJob(jobName, RuleJob.class);
        final JobDataMap map = jobDetail.getJobDataMap();
        map.put("ruleToExecute", ruleToExecute);
        map.put("node", uuid);
        map.put("user", ((User) drools.getWorkingMemory().getGlobal("user")).getName());
        map.put("workspace", ((String) drools.getWorkingMemory().getGlobal("workspace")));
        schedulerService.deleteJob(jobName, "RULES_JOBS");
        try {
            schedulerService.scheduleJob(jobDetail, getTrigger(node, propertyName, jobName));
        } catch (ParseException e) {
            logger.error(e.getMessage(), e);
        }
    }

    public void executeActionLater(AddedNodeFact node, final String propertyName, final String actionToExecute, KnowledgeHelper drools)
            throws JahiaException, RepositoryException {
        final String uuid = node.getNode().getIdentifier();
        final String jobName = "ACTION_JOB_" + uuid + actionToExecute;
        final JobDetail jobDetail = BackgroundJob.createJahiaJob(jobName, ActionJob.class);
        final JobDataMap map = jobDetail.getJobDataMap();
        map.put("actionToExecute", actionToExecute);
        map.put("node", uuid);
        map.put("workspace", ((String) drools.getWorkingMemory().getGlobal("workspace")));
        schedulerService.deleteJob(jobName, "ACTIONS_JOBS");
        try {
            schedulerService.scheduleJob(jobDetail, getTrigger(node, propertyName, jobName));
        } catch (ParseException e) {
            logger.error(e.getMessage(), e);
        }
    }

    private Trigger getTrigger(AddedNodeFact node, String propertyName, String jobName)
            throws ParseException, RepositoryException {
        final Property property = node.getNode().getProperty(propertyName);
        if (property.getType() == PropertyType.DATE) {
            return new SimpleTrigger(jobName + "TRIGGER", "RULES_JOBS", property.getDate().getTime());
        } else {
            return new CronTrigger(jobName + "TRIGGER", "ACTIONS_JOBS", property.getString());
        }
    }

    public void moveSubnodesToSplitFolder(AddedNodeFact n, KnowledgeHelper drools) throws RepositoryException {
        NodeIterator ni = n.getNode().getNodes();
        while (ni.hasNext()) {
            Node node = (Node) ni.next();
            moveToSplitFolder((JCRNodeWrapper) node);
        }
    }

    public void moveToSplitFolder(AddedNodeFact n, KnowledgeHelper drools) throws RepositoryException  {
        JCRNodeWrapper newNode = moveToSplitFolder((JCRNodeWrapper) n.getNode());
        if (newNode != null) {
            drools.retract(n);
            drools.insert(new AddedNodeFact(newNode));
        }
    }

    private JCRNodeWrapper moveToSplitFolder(JCRNodeWrapper node) throws RepositoryException  {
        try {
            Node parent = node.getParent();
            String splitConfig = parent.getProperty(Constants.SPLIT_CONFIG).getString();
            String splitType= parent.getProperty(Constants.SPLIT_NODETYPE).getString();
            String[] config = splitConfig.split(";");
            for (String s : config) {
                String[] folderConfig = s.split(",");

                String type = folderConfig[0];
                String propertyName = folderConfig[1];

                String key = null;

                try {
                    if (type.equals("firstChars")) {
                        if (propertyName.equals("j:nodename")) {
                            key = node.getName();
                            key = StringUtils.substringAfter(key,":");
                        } else if (node.hasProperty(propertyName)) {
                            key = node.getProperty(propertyName).getString();
                        }
                        final int index = Integer.parseInt(folderConfig[2]);
                        if (key != null && key.length() > index) {
                            key = key.substring(0, index);
                        }
                    } else if (type.equals("date")) {
                        if (node.hasProperty(propertyName)) {
                            Calendar calendar = node.getProperty(propertyName).getDate();
                            SimpleDateFormat sdf = new SimpleDateFormat(folderConfig[2]);
                            key = sdf.format(calendar.getTime());
                        }
                    }
                } catch (Exception e) {
                    logger.error("Cannot split folder",e);
                    key = null;
                }

                if (key != null) {
                    if (!parent.hasNode(key)) {
                        parent = parent.addNode(key, splitType);
                    } else {
                        parent = parent.getNode(key);
                    }
                }
            }
            if (!parent.getPath().equals(node.getParent().getPath())) {
                node.getSession().move(node.getPath(), parent.getPath() + "/"+ node.getName());
                return node.getSession().getNode(parent.getPath() + "/"+ node.getName());
            }
            return null;
        } catch (RepositoryException e) {
            e.printStackTrace();
            return null;
        }
    }

    public void publishNode(AddedNodeFact node,KnowledgeHelper drools) throws RepositoryException {
        JCRNodeWrapper nodeWrapper = (JCRNodeWrapper) node.getNode();
        final JCRSessionWrapper jcrSessionWrapper = nodeWrapper.getSession();
        jcrSessionWrapper.save();
        JCRPublicationService.getInstance().publish(nodeWrapper.getPath(), jcrSessionWrapper.getWorkspace().getName(),
                                                    Constants.LIVE_WORKSPACE,
                                                    Collections.singleton(jcrSessionWrapper.getLocale().toString()),
                                                    true, false);
    }

    public void startWorkflowOnNode(AddedNodeFact node,String processKey, String provider,KnowledgeHelper drools) throws RepositoryException {
        JCRNodeWrapper nodeWrapper = (JCRNodeWrapper) node.getNode();
        final JCRSessionWrapper jcrSessionWrapper = nodeWrapper.getSession();
        WorkflowService.getInstance().startProcess(nodeWrapper, processKey, provider, new HashMap<String, Object>());
    }

    public void setTaggingService(TaggingService taggingService) {
        this.taggingService = taggingService;
    }

    public void setSitesService(JahiaSitesService sitesService) {
        this.sitesService = sitesService;
    }

    public void setSiteUserManager(JahiaSiteUserManagerService siteUserManager) {
        this.siteUserManager = siteUserManager;
    }

    public void setSchedulerService(SchedulerService schedulerService) {
        this.schedulerService = schedulerService;
    }

    public void setCacheService(CacheService cacheService) {
        this.cacheService = cacheService;
    }

    public void setUserManager(JahiaUserManagerService userManager) {
        this.userManager = userManager;
    }

    @Override
    public void start() throws JahiaInitializationException {
    }

    @Override
    public void stop() throws JahiaException {
    }
}
