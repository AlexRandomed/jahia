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

//
//
//  FileWatcher
//
//  NK      12.01.2001
//
//


package org.jahia.tools.files;

import org.jahia.registries.ServicesRegistry;
import org.jahia.services.scheduler.SchedulerService;
import org.quartz.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.util.Observable;

/**
 * An Observer/Observable Implementation of a Deamon Thread
 * that looks at any new File created in a given Folder.
 * New files are checked by their last modification date.
 *
 * Build a list of files and pass it to any Registered Observer.<br>
 *
 * <pre>
 * Works in two modes : mode = ALL           -> returns all files in the folder.
 *                      mode = CHECK_DATE    -> returns only new files.
 * </pre>
 *
 * @author Khue ng
 * @version 1.0
 */
public class FileWatcher extends Observable implements Serializable {

    private static final long serialVersionUID = -5173318550711639571L;

    private static Logger logger = LoggerFactory.getLogger(FileWatcher.class);

    /** The Full Real Path to the Folder to Watch **/
    private String m_FolderPath = "";

    /** The Abstract File object of the Folder to watch **/
    private File m_Folder;

    /** Define at what interval the folder must be checked, in millis **/
    private long m_Interval;

    private String triggerName;

    private String jobName;
    
    private int maxJobNameLength = 50;

    /** Both file and directory or only file **/
    private boolean m_FileOnly = true;

    private boolean recursive = false;

    /** Define if the Thread is User or Deamon Thread **/
    private boolean m_IsDeamon = true;

    /** Check files by their last modification date status or not **/
    public boolean mCheckDate = false;

    /** the Last Time the Folder was checked **/
    private long lastCheckTime;

    /**
     * Constructor
     * 
     * @param jobName
     *            the name of the background job to use
     * @param fullFolderPath
     *            the real Path to the folder to watch
     * @param checkDate
     *            check by last modification date or not
     * @param interval
     *            the interval to do the repeat Task
     * @param fileOnly
     *            checks only files if true, not directories
     * @exception IOException
     */
    public FileWatcher(String jobName, String fullFolderPath, boolean checkDate, long interval, boolean fileOnly)
            throws IOException {

        this(jobName, fullFolderPath, checkDate, interval, fileOnly, true, null);
    }
    
    /**
     * Constructor
     *
     * @param fullFolderPath  the real Path to the folder to watch
     * @param checkDate check by last modification date or not
     * @param interval the interval to do the repeat Task
     * @param fileOnly checks only files if true, not directories
     * @exception IOException
     */
    public FileWatcher( String fullFolderPath,
                        boolean checkDate,
                        long interval,
                        boolean fileOnly,
                        SchedulerService schedulerService)
            throws IOException {

        this(fullFolderPath, checkDate, interval, fileOnly, true, null);
    }

    /**
     * Constructor
     * Precise if the Thread to Create is a Deamon or not
     *
     * @param fullFolderPath the real Path to the folder to watch
     * @param checkDate check new files by date changes ?
     * @param interval the interval to do the repeat Task
     * @param isDeamon create a User or Deamon Thread
     * @param fileOnly checks only files if true, not directories
     * @param schedulerService a dependency injection of the service to use to schedule the file watching job.
     * @exception IOException
     */
    public FileWatcher( String fullFolderPath,
                        boolean checkDate,
                        long interval,
                        boolean fileOnly,
                        boolean isDeamon,
                        SchedulerService schedulerService
    )
            throws IOException {

        this(null, fullFolderPath, checkDate, interval, fileOnly, isDeamon, schedulerService);
    }

    /**
     * Constructor
     * Precise if the Thread to Create is a Deamon or not
     *
     * @param jobName the name of the background job to use
     * @param fullFolderPath the real Path to the folder to watch
     * @param checkDate check new files by date changes ?
     * @param interval the interval to do the repeat Task
     * @param isDeamon create a User or Deamon Thread
     * @param fileOnly checks only files if true, not directories
     * @param schedulerService a dependency injection of the service to use to schedule the file watching job.
     * @exception IOException
     */
    public FileWatcher( String jobName,
                        String fullFolderPath,
                        boolean checkDate,
                        long interval,
                        boolean fileOnly,
                        boolean isDeamon,
                        SchedulerService schedulerService
    )
            throws IOException {

        super();
        this.jobName = jobName;
        setFolderPath(fullFolderPath);
        setCheckDate(checkDate);
        setInterval(interval);
        setFileOnly(fileOnly);
        setDeamon(isDeamon);
    }

    /**
     * Creates the Timer Thread and starts it
     *
     * @throws IOException
     */
    public void start ()
            throws IOException {

        initialize();

        logger.debug("Time created, Check Interval=" + getInterval() +
                " (millis) ");

        JobDetail jobDetail = new JobDetail(jobName, Scheduler.DEFAULT_GROUP,
                FileWatcherJob.class);
        JobDataMap jobDataMap = new JobDataMap();
        jobDataMap.put("fileWatcher", this);
        jobDetail.setJobDataMap(jobDataMap);

        Trigger trigger = new SimpleTrigger(jobName + "_Trigger",
                Scheduler.DEFAULT_GROUP,
                SimpleTrigger.REPEAT_INDEFINITELY,
                m_Interval);
        // not persisted Job and trigger
        trigger.setVolatility(true);
        triggerName = trigger.getName(); 
        
        jobDetail.setRequestsRecovery(false);
        jobDetail.setDurability(false);
        jobDetail.setVolatility(true);

        try {
            ServicesRegistry.getInstance().getSchedulerService().getRAMScheduler().deleteJob(jobName, Scheduler.DEFAULT_GROUP);
        } catch (SchedulerException e) {
            logger.warn("Unable to delete the job " + jobName + ". Cause: " + e.getMessage());
        }
        try {
            ServicesRegistry.getInstance().getSchedulerService().getRAMScheduler().scheduleJob(jobDetail, trigger);
        } catch (SchedulerException je) {
            logger.error("Error while scheduling file watch for " + m_FolderPath, je);
        }

    }

    public void stop() {
        try {
            ServicesRegistry.getInstance().getSchedulerService().getRAMScheduler().unscheduleJob(triggerName, Scheduler.DEFAULT_GROUP);
        } catch (SchedulerException e) {
            logger.warn("Unable to unschedule the job with trigger " + triggerName + ". Cause: " + e.getMessage());
        }
        try {
            ServicesRegistry.getInstance().getSchedulerService().getRAMScheduler().deleteJob(jobName, Scheduler.DEFAULT_GROUP);
        } catch (SchedulerException e) {
            logger.warn("Unable to delete the job " + jobName + ". Cause: " + e.getMessage());
        }
    }

    /**
     * Returns The Interval Values
     *
     * @return (long) the interval
     */
    public long getInterval(){
        return m_Interval;
    }

    /**
     * Set The Interval Values
     *
     * @param interval the interval used to do repeatitive task in millis
     */
    protected void setInterval(long interval){
        m_Interval = interval;
    }

    /**
     * Small trick to export the setChanged method that has a protected. This
     * is necessary for compilation with Ant...
     */
    public void externalSetChanged() {
        setChanged();
    }

    /**
     * Returns The Path of the Folder to watch
     *
     * @return (String) the path to the folder to watch
     */
    public String getFolderPath(){
        return m_FolderPath;
    }

    /**
     * Set The FolderPath
     *
     * @param fullFolderPath the path to the folder to watch
     */
    protected void setFolderPath( String fullFolderPath ){
        m_FolderPath = fullFolderPath;
        if (jobName == null) {
            jobName = m_FolderPath;
        }
        int jobNameLength = jobName.length();
        if (jobNameLength > maxJobNameLength) {
            int jobNameHashCode = jobName.hashCode();
            String jobNameHashCodeStr = Integer.toString(jobNameHashCode);
            jobName = "..." +
                    jobName.substring(jobNameLength - maxJobNameLength + 4 + jobNameHashCodeStr.length())
                    + jobNameHashCodeStr;
        }
    }

    /**
     * set file only mode
     *
     * @param fileOnly file only or not
     */
    public void setFileOnly(boolean fileOnly){
        m_FileOnly = fileOnly;
    }

    public boolean getFileOnly() {
        return m_FileOnly;
    }

    public boolean isRecursive() {
        return recursive;
    }

    public void setRecursive(boolean recursive) {
        this.recursive = recursive;
    }

    /**
     * Returns The Check File Mode
     *
     * @return (boolean) if check new file by controlling the last modification date
     */
    public boolean getCheckDate(){
        return mCheckDate;
    }

    /**
     * Set The Check File Mode ( by last modification date or returns all files found
     *
     * @param checkDate check by last modification date or not
     */
    public void setCheckDate( boolean checkDate){
        mCheckDate = checkDate;
    }

    /**
     * Thread is a Deamon or not
     *
     * @return (boolean) true if is Deamon Thread
     */
    public boolean isDeamon(){
        return m_IsDeamon;
    }

    /**
     * Create a Deamon or User Thread
     *
     * @param isDeamon
     */
    protected void setDeamon( boolean isDeamon ){
        m_IsDeamon = isDeamon;
    }

    public File getFolder() {
        return m_Folder;
    }

    public long getLastCheckTime() {
        return lastCheckTime;
    }

    public void setLastCheckTime(long lastCheckTime) {
        this.lastCheckTime = lastCheckTime;
    }

    public int getMaxJobNameLength() {
        return maxJobNameLength;
    }

    public void setMaxJobNameLength(int maxJobNameLength) {
        this.maxJobNameLength = maxJobNameLength;
    }

    /**
     * Verify if the Folder to watch exists.
     * Create the Archive Folder if not exist.
     *
     * @exception IOException
     */
    protected void initialize ()
            throws IOException {

        logger.debug("Initializing file watcher"  );

        /*
           For Test Purpose
           ToChange : restore the last check time from ext. file !
        */
        lastCheckTime = System.currentTimeMillis();
        logger.debug("Watching directory=" + getFolderPath()   );
        File tmpFile = new File(getFolderPath());
        if ( tmpFile.isDirectory() && !tmpFile.canWrite() ){
            logger.debug("No write access to directory " + getFolderPath() +
                    " tmpFile=" + tmpFile.toString());
        } else if ( !tmpFile.exists() ) {
            logger.debug("Directory " + tmpFile.toString() +
                    " does not exist, creating...");
            tmpFile.mkdirs();
            logger.debug("Directory " + tmpFile.toString() +
                    " created successfully.");
        }
        m_Folder = tmpFile;
    }

}
