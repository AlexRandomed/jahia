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
package org.jahia.services.importexport;

import org.apache.commons.io.IOUtils;
import org.jahia.content.ContentObject;
import org.jahia.content.ObjectKey;
import org.jahia.content.TreeOperationResult;
import org.jahia.registries.ServicesRegistry;
import org.jahia.services.content.JCRNodeWrapper;
import org.jahia.services.content.JCRSessionWrapper;
import org.jahia.services.scheduler.BackgroundJob;
import org.jahia.services.sites.JahiaSite;
import org.quartz.JobDataMap;
import org.quartz.JobDetail;
import org.quartz.JobExecutionContext;

import java.io.File;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by IntelliJ IDEA.
 * Date: 25 oct. 2005 - 16:34:07
 *
 * @author toto
 * @version $Id$
 */
public class ImportJob extends BackgroundJob {
    public static final String IMPORT_TYPE = "import";

    public static final String TARGET = "target";
    public static final String CONTENT_TYPE = "contentType";
    public static final String PUBLISH_ALL_AT_END = "publishAllAtEnd";
    public static final String URI = "uri";
    public static final String FILENAME = "filename";
    public static final String DELETE_FILE = "delete";

    public static final String COPY_TO_JCR = "copyToJCR";

    public void executeJahiaJob(JobExecutionContext jobExecutionContext) throws Exception {
        JobDetail jobDetail = jobExecutionContext.getJobDetail();
        JobDataMap jobDataMap = jobDetail.getJobDataMap();

        String contentType = (String) jobDataMap.get(CONTENT_TYPE);
        ContentObject target = null;
        String key = (String) jobDataMap.get(TARGET);
        if (key != null) {
            target = ContentObject.getContentObjectInstance(ObjectKey.getInstance(key));
        }

        JahiaSite site = ServicesRegistry.getInstance().getJahiaSitesService().getSiteByKey((String) jobDataMap.get(JOB_SITEKEY));

        String uri = (String) jobDataMap.get(URI);
        JCRSessionWrapper session = ServicesRegistry.getInstance().getJCRStoreService().getSessionFactory().getCurrentUserSession();
        JCRNodeWrapper f = session.getNode(uri);

        List<ImportAction> actions = new ArrayList<ImportAction>();

        ExtendedImportResult result = new ExtendedImportResult();

        if (f != null) {
            File file = File.createTempFile("import", "zip");
            FileOutputStream fileOutputStream = new FileOutputStream(file);
            IOUtils.copy(f.getFileContent().downloadFile(), fileOutputStream);
            fileOutputStream.close();
            ServicesRegistry.getInstance().getImportExportService().importSiteZip(file, actions, result, site);

//            if (Boolean.TRUE.equals(jobDataMap.get(PUBLISH_ALL_AT_END))) {
//                if (result.getErrors().isEmpty()) {
//                    Class<? extends BackgroundJob> jobClass = PublishAllJob.class;
//                    JobDetail publishjobDetail = BackgroundJob.createJahiaJob("ActivatingAll", jobClass, context);
//                    JobDataMap publishjobDataMap = publishjobDetail.getJobDataMap();
//                    publishjobDataMap.put(BackgroundJob.JOB_DESTINATION_SITE, context.getSiteKey());
//                    publishjobDataMap.put(BackgroundJob.JOB_TYPE, AbstractActivationJob.WORKFLOW_TYPE);
//                    publishjobDataMap.put(AbstractActivationJob.COMMENTS_INPUT, "Auto publish " + uri);
//                    final SchedulerService schedulerServ = ServicesRegistry.getInstance().getSchedulerService();
//                    schedulerServ.scheduleJobAtEndOfRequest(publishjobDetail);
//                } else {
//                    MailService mailService = ServicesRegistry.getInstance().getMailService();
//                    GroovyScriptEngine groovyScriptEngine = (GroovyScriptEngine) SpringContextSingleton.getInstance().getContext().getBean("groovyScriptEngine");
//                    GroovyMimeMessagePreparator messageMimePreparator = new GroovyMimeMessagePreparator();
//                    messageMimePreparator.setGroovyScriptEngine(groovyScriptEngine);
//                    String senderEmail = mailService.defaultSender();
//
//                    JahiaGroup adminGroup = ServicesRegistry.getInstance().getJahiaGroupManagerService()
//                            .lookupGroup(context.getSiteID(), JahiaGroupManagerService.ADMINISTRATORS_GROUPNAME);
//                    Set<Principal> members = adminGroup.getRecursiveUserMembers();
//
//                    String recipientEmail = mailService.defaultRecipient();
//                    if (members.iterator().hasNext()) {
//                        JahiaUser user = (JahiaUser) members.iterator().next();
//                        UserProperty userProperty = user.getUserProperty("email");
//                        if (userProperty != null) {
//                            String s = userProperty.getValue();
//                            if (s != null && s.trim().length() > 0) {
//                                recipientEmail += ";" + s;
//                            }
//                        }
//                    }
//                    Binding binding = new Binding();
//                    // Bind all necessary variables for groovy script
//                    binding.setVariable("processingContext", context);
//                    binding.setVariable("from", senderEmail);
//                    binding.setVariable("to", recipientEmail);
//                    binding.setVariable("locale", context.getLocale());
//                    binding.setVariable("results", result);
//
//                    messageMimePreparator.setBinding(binding);
//                    messageMimePreparator.setTemplatePath("autoexport_notvalidated.groovy");
//                    mailService.sendTemplateMessage(messageMimePreparator);
//                }
//            }

//                if (Boolean.TRUE.equals(jobDataMap.get(COPY_TO_JCR)) ) {
//                    ServicesRegistry.getInstance().getJahiaEventService().fireAggregatedEvents();
//                    try {
//                        JCRNodeWrapper source = imported.getJCRNode(context);
//                        Node parent = source.getParent();
//                        if (parent.isNodeType(Constants.JAHIANT_VIRTUALSITE)) {
//                            Node dest = JCRSessionFactory.getInstance().getCurrentUserSession().getNode("/sites/"+parent.getName());
//                            source.copyFile(dest.getPath());
//                            if (source.hasNode("j:acl")) {
//                                dest.addMixin("jmix:accessControlled");
//                                ((JCRNodeWrapper)source.getNode("j:acl")).copyFile(dest.getPath());
//                            }
//                            dest.save();
//                        }
//                    } catch (JahiaException e) {
//                        e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
//                    } catch (RepositoryException e) {
//                        e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
//                    }
//                }
        }

//        try {
//            if (imported != null) {
//                LockKey lock = LockKey.composeLockKey(LockKey.IMPORT_ACTION + "_" + imported.getObjectKey().getType(), imported.getID());
//                ((Set<LockKey>)jobDataMap.get(JOB_LOCKS)).add(lock);
//            }
//        } catch (Exception e) {
//        }
        if (jobDataMap.get(DELETE_FILE) != null) {
            if (result.getStatus() == TreeOperationResult.COMPLETED_OPERATION_STATUS) {
                f.remove();
                session.save();
            }
        }
        jobDataMap.put(ACTIONS, actions);
        jobDataMap.put(RESULT, result);
    }
}
/**
 *$Log $
 */