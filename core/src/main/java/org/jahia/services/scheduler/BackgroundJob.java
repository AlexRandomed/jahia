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
 package org.jahia.services.scheduler;

import org.apache.commons.id.IdentifierGenerator;
import org.apache.commons.id.IdentifierGeneratorFactory;
import org.apache.log4j.Logger;
import org.jahia.bin.Jahia;
import org.jahia.exceptions.JahiaException;
import org.jahia.hibernate.cache.JahiaBatchingClusterCacheHibernateProvider;
import org.jahia.params.BasicURLGeneratorImpl;
import org.jahia.params.ProcessingContext;
import org.jahia.params.URLGenerator;
import org.jahia.registries.ServicesRegistry;
import org.jahia.services.content.JCRSessionFactory;
import org.jahia.services.pages.ContentPage;
import org.jahia.services.sites.JahiaSite;
import org.jahia.services.usermanager.JahiaUser;
import org.jahia.services.version.EntryLoadRequest;
import org.jahia.utils.LanguageCodeConverters;
import org.quartz.*;

import java.util.Date;
import java.util.Locale;
import java.util.Map;

/**
 * Created by IntelliJ IDEA.
 * Date: 25 oct. 2005 - 16:34:07
 *
 * @author toto
 * @version $Id$
 */
public abstract class BackgroundJob implements StatefulJob {
    private final static Logger logger = Logger.getLogger(BackgroundJob.class);

    private static IdentifierGenerator idGen = IdentifierGeneratorFactory.newInstance().uuidVersionFourGenerator();

    //jobdetails Constants
    public static final String JOB_TYPE = "type";
    public static final String JOB_CREATED = "created";
    public static final String JOB_SCHEDULED = "scheduled";
    public static final String JOB_BEGIN = "begin";
    public static final String JOB_END = "end";
    public static final String JOB_SERVER = "server";
    public static final String JOB_STATUS = "status";
    public static final String JOB_DURATION = "duration";
    public static final String JOB_CURRENT_LOCALE = "currentLocale";
    public static final String JOB_SCHEME = "scheme";
    public static final String JOB_SERVERNAME = "servername";
    public static final String JOB_PARAMETER_MAP = "parameterMap";
    public static final String JOB_SERVERPORT = "serverport";
    public static final String JOB_OPMODE = "opmode";
    public static final String JOB_SITEKEY = "sitekey";
    public static final String JOB_USERKEY = "userkey";
    public static final String JOB_PID = "pid";
    public static final String JOB_DESTINATION_SITE = "sitedest";
    public static final String JOB_LOCKS = "locks";
    public static final String JOB_TITLE = "title";

    public static final String ACTIONS = "actions";
    public static final String RESULT = "result";

    public static final String STATUS_SUCCESSFUL = "successful";
    public static final String STATUS_RUNNING = "executing";
    public static final String STATUS_WAITING = "waiting";
    public static final String STATUS_POOLED = "pooled";
    public static final String STATUS_FAILED = "failed";
    public static final String STATUS_ABORTED = "aborted";
    public static final String STATUS_INTERRUPTED = "interrupted";

    public static JobDetail createJahiaJob(String desc, Class<? extends BackgroundJob> jobClass, ProcessingContext jParams) {
        long now = System.currentTimeMillis();
        // jobdetail is non-volatile,durable,non-recoverable
        JobDetail jobDetail = new JobDetail("BackgroundJob-" + idGen.nextIdentifier(),
                getGroupName(jobClass),
                jobClass,
                false,
                true,
                false);
        jobDetail.setDescription(desc);
        JobDataMap jobDataMap = new JobDataMap();
        jobDataMap.putAsString(JOB_CREATED, now); //creation
        jobDataMap.put(JOB_SITEKEY, jParams.getSiteKey());
        jobDataMap.put(JOB_USERKEY, jParams.getUser().getUserKey());
        jobDataMap.putAsString(JOB_PID, jParams.getPageID());
        if ( jParams.getCurrentLocale() != null ){
            jobDataMap.put(JOB_CURRENT_LOCALE, jParams.getCurrentLocale().toString());
        } else {
            jobDataMap.put(JOB_CURRENT_LOCALE,Locale.ENGLISH.toString());
        }
        jobDataMap.put(JOB_SCHEME, jParams.getScheme());
        jobDataMap.put(JOB_SERVERNAME, jParams.getServerName());
        jobDataMap.put(JOB_PARAMETER_MAP, jParams.getParameterMap());
        jobDataMap.putAsString(JOB_SERVERPORT, jParams.getServerPort());
        jobDataMap.put(JOB_OPMODE, jParams.getOperationMode());
        jobDetail.setJobDataMap(jobDataMap);
        return jobDetail;
    }

    public static String getGroupName(Class<? extends BackgroundJob> c) {
        String name = c.getName();
        return name.substring(name.lastIndexOf('.')+1);
    }

    public static int getMaxExecutionTime() {
        return 3600;
    }

    public void execute(JobExecutionContext jobExecutionContext) throws JobExecutionException {
        JobDetail jobDetail = jobExecutionContext.getJobDetail();
        JobDataMap data = jobDetail.getJobDataMap();
        long now = System.currentTimeMillis();
        updateAllLocks(jobDetail);
        logger.info("execute Background job " + jobDetail.getName() + "\n started @ " + new Date(now));
        ProcessingContext context = null;
        String status = STATUS_FAILED;
        try {
            context = getProcessingContextFromBackgroundJobDataMap(data);
            executeJahiaJob(jobExecutionContext, context);
            status = data.getString(BackgroundJob.JOB_STATUS);
            if ( !(BackgroundJob.STATUS_ABORTED.equals(status) ||
                    BackgroundJob.STATUS_FAILED.equals(status) ||
                    BackgroundJob.STATUS_INTERRUPTED.equals(status)) ){
                status = STATUS_SUCCESSFUL;
            }
        } catch (Exception e) {
            logger.error("Cannot execute job", e);
            data.put("message", e.toString());
            throw new JobExecutionException(e);
        } finally {

            try {
                releaseAllLocks(context, jobDetail);
            } catch (Exception e) {
                logger.error("Cannot release locks",e);
            }
                                         
            ServicesRegistry.getInstance().getCacheService().syncClusterNow();
            JahiaBatchingClusterCacheHibernateProvider.syncClusterNow();

            data.putAsString(JOB_END, System.currentTimeMillis());
            int duration=(int)((Long.parseLong((String)data.get(JOB_END))-Long.parseLong((String)data.get(JOB_BEGIN)))/1000);
            data.putAsString(JOB_DURATION, duration);//duration

            String t=(String)data.get(JOB_TYPE);
            logger.info("Background job (of type "+t+") ended with status "+status+" executed in " + duration + "s");

            Date nextFireTime = jobExecutionContext.getNextFireTime();
            try {
                // Look for other triggers for next fire time
                Trigger[] trigs = jobExecutionContext.getScheduler().getTriggersOfJob(jobDetail.getName(),jobDetail.getGroup()) ;
                for (int i = 0; i < trigs.length; i++) {
                    Trigger trig = trigs[i];
                    Date thisTriggerNextFireTime = trig.getNextFireTime();
                    if (thisTriggerNextFireTime != null && (nextFireTime == null || thisTriggerNextFireTime.before(nextFireTime))) {
                        nextFireTime = thisTriggerNextFireTime;
                    }
                }
            } catch (SchedulerException e) {
                logger.error("Cannot get triggers for job",e);
            }

            if (status == STATUS_FAILED) {
                try {
                    boolean ramScheduler = this instanceof RamJob;
                    ServicesRegistry.getInstance().getSchedulerService().unscheduleJob(jobDetail, ramScheduler);
                } catch (JahiaException e) {
                    logger.error("Cannot unschedule job",e);
                }
            } else {
                if (nextFireTime != null) {
                    status = STATUS_POOLED;
                    data.putAsString(JOB_SCHEDULED,nextFireTime.getTime());
                }
            }
            data.put(JOB_STATUS, status);
            this.postExecution(jobExecutionContext, context);
            JCRSessionFactory.getInstance().closeAllSessions();
        }
    }

    private ProcessingContext getProcessingContextFromBackgroundJobDataMap(JobDataMap data) throws JahiaException {
        JahiaSite site = ServicesRegistry.getInstance().getJahiaSitesService().getSiteByKey((String) data.get(JOB_SITEKEY));
        JahiaUser user = ServicesRegistry.getInstance().getJahiaUserManagerService().lookupUserByKey((String) data.get(JOB_USERKEY));
        // not all background job requires page
        int pageId = Integer.parseInt((String) data.get(JOB_PID));
        ContentPage page = null;
        if ( pageId != -1 ){
            try {
                page = ContentPage.getPage(pageId);
            } catch (JahiaException e) {
                logger.warn("Page "+pageId + " does not exist anymore");
            }
        }

        Locale locale = LanguageCodeConverters.languageCodeToLocale(data.getString(JOB_CURRENT_LOCALE));
        EntryLoadRequest elr = new EntryLoadRequest(EntryLoadRequest.STAGED);
        elr.setFirstLocale(locale.toString());
        String opMode = data.getString(JOB_OPMODE);
        if (opMode == null || opMode.length() == 0) {
            opMode = ProcessingContext.NORMAL;
        }
        ProcessingContext context = new ProcessingContext(org.jahia.settings.SettingsBean.getInstance(), System.currentTimeMillis(), site, user, page, elr, opMode);
        final URLGenerator urlGenerator = new BasicURLGeneratorImpl();
        context.setUrlGenerator(urlGenerator);
        context.getSessionState().setAttribute(ProcessingContext.SESSION_LOCALE, locale);
        context.setCurrentLocale(locale);
        context.setScheme(data.getString(JOB_SCHEME));
        context.setServerName(data.getString(JOB_SERVERNAME));
        context.setServerPort(Integer.parseInt(data.getString(JOB_SERVERPORT)));
        context.setContextPath(Jahia.getContextPath());
        context.setParameterMap((Map) data.get(JOB_PARAMETER_MAP));
        return context;
    }

    public abstract void executeJahiaJob(JobExecutionContext jobExecutionContext, ProcessingContext processingContext)
            throws Exception;

    /**
     * Sub class can perform specific post execution task hehe
     *
     * @param jobExecutionContext
     * @param processingContext
     */
    protected void postExecution(JobExecutionContext jobExecutionContext, ProcessingContext processingContext) {
        // by default do nothing
    }

    private void updateAllLocks(JobDetail jobDetail) {
    }

    private void releaseAllLocks(ProcessingContext processingContext, JobDetail jobDetail) {
    }
}
