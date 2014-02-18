/**
 * This file is part of Jahia, next-generation open source CMS:
 * Jahia's next-generation, open source CMS stems from a widely acknowledged vision
 * of enterprise application convergence - web, search, document, social and portal -
 * unified by the simplicity of web content management.
 *
 * For more information, please visit http://www.jahia.com.
 *
 * Copyright (C) 2002-2014 Jahia Solutions Group SA. All rights reserved.
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
package org.jahia.services.deamons.filewatcher;

import org.jahia.registries.ServicesRegistry;
import org.quartz.JobDataMap;
import org.quartz.JobDetail;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.quartz.SimpleTrigger;
import org.quartz.StatefulJob;
import org.quartz.Trigger;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Background job that executes the specified {@link FileMonitor}.
 * 
 * @author Sergiy Shyrkov
 */
public class FileMonitorJob implements StatefulJob {

    private static final Logger logger = LoggerFactory.getLogger(FileMonitorJob.class);

    /**
     * Schedules a periodical execution of the specified monitor as a background job.
     * 
     * @param jobName
     *            the background job name to use
     * @param interval
     *            the execution interval in milliseconds
     * @param monitor
     *            the monitor to schedule execution for
     */
    public static void schedule(String jobName, long interval, FileMonitor monitor) {
        JobDetail jobDetail = new JobDetail(jobName, Scheduler.DEFAULT_GROUP, FileMonitorJob.class);
        JobDataMap jobDataMap = new JobDataMap();
        jobDataMap.put("fileMonitor", monitor);
        jobDetail.setJobDataMap(jobDataMap);

        Trigger trigger = new SimpleTrigger(jobName + "_Trigger", Scheduler.DEFAULT_GROUP,
                SimpleTrigger.REPEAT_INDEFINITELY, interval);
        // not persisted Job and trigger
        trigger.setVolatility(true);

        jobDetail.setRequestsRecovery(false);
        jobDetail.setDurability(false);
        jobDetail.setVolatility(true);

        try {
            ServicesRegistry.getInstance().getSchedulerService().getRAMScheduler()
                    .deleteJob(jobName, Scheduler.DEFAULT_GROUP);
        } catch (SchedulerException e) {
            logger.warn("Unable to delete the job " + jobName + ". Cause: " + e.getMessage());
        }
        try {
            ServicesRegistry.getInstance().getSchedulerService().getRAMScheduler().scheduleJob(jobDetail, trigger);
        } catch (SchedulerException je) {
            logger.error("Error while scheduling file monitor job " + jobName, je);
        }

    }

    /**
     * Unschedules the execution of the specified background job.
     * 
     * @param jobName
     *            the background job name to stop and delete
     */
    public static void unschedule(String jobName) {
        try {
            ServicesRegistry.getInstance().getSchedulerService().getRAMScheduler()
                    .deleteJob(jobName, Scheduler.DEFAULT_GROUP);
        } catch (SchedulerException e) {
            logger.warn("Unable to delete the job " + jobName + ". Cause: " + e.getMessage());
        }
    }

    @Override
    public void execute(JobExecutionContext context) throws JobExecutionException {
        JobDataMap jobDataMap = context.getJobDetail().getJobDataMap();
        FileMonitor monitor = (FileMonitor) jobDataMap.get("fileMonitor");
        monitor.run();
    }
}
