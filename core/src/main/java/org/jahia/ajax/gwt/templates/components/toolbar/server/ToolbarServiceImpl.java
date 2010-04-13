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
package org.jahia.ajax.gwt.templates.components.toolbar.server;


import org.jahia.ajax.gwt.client.data.GWTJahiaAjaxActionResult;
import org.jahia.ajax.gwt.client.data.GWTJahiaProperty;
import org.jahia.ajax.gwt.client.data.toolbar.GWTJahiaToolbar;
import org.jahia.ajax.gwt.client.data.toolbar.GWTJahiaToolbarItem;
import org.jahia.ajax.gwt.client.data.toolbar.GWTJahiaToolbarItemsGroup;
import org.jahia.ajax.gwt.client.data.toolbar.GWTJahiaToolbarSet;
import org.jahia.ajax.gwt.client.data.toolbar.monitor.GWTJahiaProcessJobInfo;
import org.jahia.ajax.gwt.client.data.toolbar.monitor.GWTJahiaStateInfo;
import org.jahia.ajax.gwt.client.service.GWTJahiaServiceException;
import org.jahia.ajax.gwt.client.service.toolbar.ToolbarService;
import org.jahia.ajax.gwt.client.util.Constants;
import org.jahia.ajax.gwt.client.widget.toolbar.action.WorkflowActionItem;
import org.jahia.ajax.gwt.commons.server.JahiaRemoteService;
import org.jahia.ajax.gwt.engines.pdisplay.server.ProcessDisplayServiceImpl;
import org.jahia.ajax.gwt.templates.components.toolbar.server.ajaxaction.AjaxAction;
import org.jahia.bin.Jahia;
import org.jahia.data.JahiaData;
import org.jahia.hibernate.manager.SpringContextSingleton;
import org.jahia.registries.ServicesRegistry;
import org.jahia.services.preferences.JahiaPreferencesService;
import org.jahia.services.scheduler.BackgroundJob;
import org.jahia.services.scheduler.SchedulerService;
import org.jahia.services.toolbar.bean.*;
import org.jahia.services.workflow.WorkflowDefinition;
import org.jahia.services.workflow.WorkflowService;
import org.quartz.JobDataMap;
import org.quartz.JobDetail;

import javax.jcr.RepositoryException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


/**
 * User: jahia
 * Date: 4 mars 2008
 * Time: 17:29:58
 */
public class ToolbarServiceImpl extends JahiaRemoteService implements ToolbarService {
    private static final org.apache.log4j.Logger logger = org.apache.log4j.Logger.getLogger(ToolbarServiceImpl.class);
    private static Map<String, Class<?>> CLASS_CACHE = new HashMap<String, Class<?>>();

    private static final ServicesRegistry SERVICES_REGISTRY = ServicesRegistry.getInstance();
    private static transient SchedulerService SCHEDULER_SERVICE;

    private JahiaPreferencesService preferencesService;

    public void setPreferencesService(JahiaPreferencesService preferencesService) {
        this.preferencesService = preferencesService;
    }

    /**
     * Get gwt toolbar for the current user
     *
     * @return
     */
    public GWTJahiaToolbarSet getGWTToolbars(String toolbarGroup) throws GWTJahiaServiceException {
        try {
            // there is no pref or toolbar are hided
            // get all tool bars
            ToolbarSet toolbarSet = (ToolbarSet) SpringContextSingleton.getBean(toolbarGroup);
            Visibility visibility = toolbarSet.getVisibility();
            if ((visibility != null && visibility.getRealValue(getSite(), getRemoteJahiaUser(), getLocale(), getRequest())) || visibility == null) {
                GWTJahiaToolbarSet gwtJahiaToolbarSet = createGWTToolbarSet(toolbarSet);
                return gwtJahiaToolbarSet;
            } else {
                logger.info("Toolbar are not visible.");
                return null;
            }

        } catch (Exception e) {
            logger.error(e, e);
            throw new GWTJahiaServiceException("Error during loading toolbars due to " + e.getMessage());
        }
    }

    /**
     * create gwt toolabr set
     *
     * @param toolbarSet
     * @return
     */
    private GWTJahiaToolbarSet createGWTToolbarSet(ToolbarSet toolbarSet) {
        if (toolbarSet.getToolbars() == null || toolbarSet.getToolbars().isEmpty()) {
            logger.debug("toolbar set list is empty");
            return null;
        }

        // create  a gwtJahiaToolbarSet
        GWTJahiaToolbarSet gwtJahiaToolbarSet = new GWTJahiaToolbarSet();
        for (Toolbar toolbar : toolbarSet.getToolbars()) {
            // add only tool bar that the user can view
            Visibility visibility = toolbar.getVisibility();
            if ((visibility != null && visibility.getRealValue(getSite(), getRemoteJahiaUser(), getLocale(), getRequest())) || visibility == null) {
                GWTJahiaToolbar gwtToolbar = createGWTToolbar(toolbar);
                // add toolbar only if not empty
                if (gwtToolbar != null && gwtToolbar.getGwtToolbarItemsGroups() != null && !gwtToolbar.getGwtToolbarItemsGroups().isEmpty()) {
                    gwtJahiaToolbarSet.addGWTToolbar(gwtToolbar);
                } else {
                    logger.debug("[" + (gwtToolbar != null) + "," + (gwtToolbar.getGwtToolbarItemsGroups() != null) + "," + (!gwtToolbar.getGwtToolbarItemsGroups().isEmpty()) + "]" + " toolbar: " + toolbar.getName() + " has no items -->  not visible");
                }
            } else {
                logger.debug("toolbar: " + toolbar.getName() + ":  not visible");
            }
        }
        return gwtJahiaToolbarSet;

    }

    /**
     * Execute ItemAjaxAction
     *
     * @param gwtPropertiesMap
     * @return
     */
    public GWTJahiaAjaxActionResult execute(Map<String, GWTJahiaProperty> gwtPropertiesMap) throws GWTJahiaServiceException {
        final GWTJahiaProperty classActionProperty = gwtPropertiesMap.get(Constants.CLASS_ACTION);
        final GWTJahiaProperty actionProperty = gwtPropertiesMap.get(Constants.ACTION);

        GWTJahiaAjaxActionResult actionResult = new GWTJahiaAjaxActionResult("");

        // execute actionProperty depending on classActin and the actionProperty
        if (classActionProperty != null) {
            String classActionValue = classActionProperty.getValue();
            if (classActionValue != null && classActionValue.length() > 0) {
                String actionValue = null;
                if (actionProperty != null) {
                    actionValue = actionProperty.getValue();
                }

                // execute action
                try {
                    // remove useless properties
                    gwtPropertiesMap.remove(Constants.CLASS_ACTION);
                    gwtPropertiesMap.remove(Constants.ACTION);
                    JahiaData jData = retrieveJahiaData();

                    // execute actionProperty
                    if (logger.isDebugEnabled()) {
                        logger.debug("Execute [" + classActionValue + "," + actionValue + "]");
                    }
                    AjaxAction ajaxAction = (AjaxAction) getClassInstance(classActionValue);
                    return ajaxAction.execute(jData, actionValue, gwtPropertiesMap);
                } catch (Exception e) {
                    logger.error(e, e);
                }


            } else {
                logger.info("Class Action property found but EMPTY");
            }

        } else {
            logger.info("Class Action property not found");
        }
        return actionResult;
    }


    /**
     * Create gwt toolbar
     *
     * @param toolbar
     * @return
     */
    private GWTJahiaToolbar createGWTToolbar(Toolbar toolbar) {
        // don't add the tool bar if  has no items group
        if (toolbar.getItems() == null || toolbar.getItems().isEmpty()) {
            logger.debug("toolbar[" + toolbar.getName() + "] itemsgroup list is empty");
            return null;
        }

        // create gwtTollbar
        GWTJahiaToolbar gwtToolbar = new GWTJahiaToolbar();
        gwtToolbar.setName(toolbar.getName());
        gwtToolbar.setTitle(getResources(toolbar.getTitleKey(), getUILocale() != null ? getUILocale() : getLocale(), getSite()));
        gwtToolbar.setType(toolbar.getType());
        gwtToolbar.setDisplayTitle(toolbar.isDisplayTitle());
        gwtToolbar.setContextMenu(toolbar.isContextMenu());

        // load items-group
        List<GWTJahiaToolbarItemsGroup> gwtToolbarItemsGroupList = new ArrayList<GWTJahiaToolbarItemsGroup>();
        int index = 0;
        for (Item item : toolbar.getItems()) {
            ItemsGroup itemsGroup = null;
            if (item instanceof ItemsGroup) {
                itemsGroup = (ItemsGroup) item;
            } else {
                // create a single item group
                itemsGroup = new ItemsGroup();
                itemsGroup.addItem(item);
                itemsGroup.setLayout("button-label");
                itemsGroup.setVisibility(item.getVisibility());
            }

            // add only itemsgroup that the user can view
            Visibility visibility = itemsGroup.getVisibility();
            if ((visibility != null && visibility.getRealValue(getSite(), getRemoteJahiaUser(), getLocale(), getRequest())) || visibility == null) {
                GWTJahiaToolbarItemsGroup gwtItemsGroup = createGWTItemsGroup(gwtToolbar.getName(), index, itemsGroup);

                // add itemsGroup only if not empty
                if (gwtItemsGroup != null && gwtItemsGroup.getGwtToolbarItems() != null && !gwtItemsGroup.getGwtToolbarItems().isEmpty()) {
                    gwtToolbarItemsGroupList.add(gwtItemsGroup);

                }
            } else {
                logger.debug("toolbar[" + gwtToolbar.getName() + "] - itemsGroup [" + itemsGroup.getId() + "," + itemsGroup.getTitleKey() + "]  not visible");
            }

            index++;
        }
        gwtToolbar.setGwtToolbarItemsGroups(gwtToolbarItemsGroupList);

        return gwtToolbar;
    }


    /**
     * Create gwt items group
     *
     * @param toolbarName
     * @param index
     * @param itemsGroup
     * @return
     */
    private GWTJahiaToolbarItemsGroup createGWTItemsGroup(String toolbarName, int index, ItemsGroup itemsGroup) {
        // don't add the items group if  has no items group
        List<Item> list = itemsGroup.getRealItems(getSite(), getRemoteJahiaUser(), getLocale());
        if (list == null || list.isEmpty()) {
            logger.debug("toolbar[" + toolbarName + "] itemlist is empty");
            return null;
        }


        List<GWTJahiaToolbarItem> gwtToolbarItemsList = new ArrayList<GWTJahiaToolbarItem>();
        // create items from definition
        for (Item item : list) {
            addItem(gwtToolbarItemsList, item);
        }

        // don't add the items group if  has no items group
        if (gwtToolbarItemsList == null || gwtToolbarItemsList.isEmpty()) {
            logger.debug("toolbar[" + toolbarName + "] itemlist is empty");
            return null;
        }

        // creat items-group
        GWTJahiaToolbarItemsGroup gwtToolbarItemsGroup = new GWTJahiaToolbarItemsGroup();
        gwtToolbarItemsGroup.setId(toolbarName + "_" + index);
        gwtToolbarItemsGroup.setType(itemsGroup.getId());
        gwtToolbarItemsGroup.setLayout(getLayoutAsInt(itemsGroup.getLayout()));

        gwtToolbarItemsGroup.setNeedSeparator(itemsGroup.isSeparator());
        gwtToolbarItemsGroup.setMediumIconStyle(itemsGroup.getMediumIconStyle());
        gwtToolbarItemsGroup.setMinIconStyle(itemsGroup.getMinIconStyle());
        if (itemsGroup.getTitleKey() != null) {
            gwtToolbarItemsGroup.setItemsGroupTitle(getResources(itemsGroup.getTitleKey(), getUILocale() != null ? getUILocale() : getLocale(), getSite()));
        } else {
            gwtToolbarItemsGroup.setItemsGroupTitle(itemsGroup.getTitle());
        }
        gwtToolbarItemsGroup.setGwtToolbarItems(gwtToolbarItemsList);
        return gwtToolbarItemsGroup;
    }


    /**
     * Get layout as int
     *
     * @param layout
     * @return
     */
    private int getLayoutAsInt(String layout) {
        int layoutInt = -1;
        if (layout != null) {
            if (layout.equalsIgnoreCase("button")) {
                layoutInt = Constants.LAYOUT_BUTTON;
            } else if (layout.equalsIgnoreCase("label")) {
                layoutInt = Constants.LAYOUT_ONLY_LABEL;
            } else if (layout.equalsIgnoreCase("button-label")) {
                layoutInt = Constants.LAYOUT_BUTTON_LABEL;
            } else if (layout.equalsIgnoreCase("menu")) {
                layoutInt = Constants.LAYOUT_ITEMSGROUP_MENU;
            } else if (layout.equalsIgnoreCase("menu-radio")) {
                layoutInt = Constants.LAYOUT_ITEMSGROUP_MENU_RADIO;
            } else if (layout.equalsIgnoreCase("menu-checkbox")) {
                layoutInt = Constants.LAYOUT_ITEMSGROUP_MENU_CHECKBOX;
            } else {
                logger.debug("Warning: layout " + layout + " unknown.");
            }
        }
        return layoutInt;
    }

    /**
     * Add item
     *
     * @param gwtToolbarItemsList
     * @param item
     */
    private void addItem(List<GWTJahiaToolbarItem> gwtToolbarItemsList, Item item) {
        if (item instanceof ItemsGroup) {
            for (Item subItem : ((ItemsGroup) item).getRealItems(getSite(), getRemoteJahiaUser(), getLocale())) {
                addItem(gwtToolbarItemsList, subItem);
            }
        } else {
            // add only item that the user can view
            logger.debug("Item: " + item.getId());
            Visibility visibility = item.getVisibility();

            // add only visible items
            if ((visibility != null && visibility.getRealValue(getSite(), getRemoteJahiaUser(), getLocale(), getRequest())) || visibility == null) {
                GWTJahiaToolbarItem gwtToolbarItem = createGWTItem(item);
                if (gwtToolbarItem != null) {
                    gwtToolbarItemsList.add(gwtToolbarItem);
                }
            } else {
                logger.debug("Item: " + item.getTitleKey() + ":  not visible");
            }
        }
    }

    /**
     * Create object from the given className
     *
     * @param className
     * @return
     */
    private Object getClassInstance(String className) {
        Class<?> clazz = CLASS_CACHE.get(className);
        if (null == clazz) {
            synchronized (ToolbarServiceImpl.class) {
                if (null == clazz) {
                    try {
                        clazz = Class.forName(className);
                        CLASS_CACHE.put(className, clazz);
                    } catch (ClassNotFoundException e) {
                        throw new IllegalArgumentException(e);
                    }
                }
            }
        }

        Object classInstance = null;
        try {
            classInstance = clazz.newInstance();
        } catch (InstantiationException e) {
            throw new IllegalArgumentException(e);
        } catch (IllegalAccessException e) {
            throw new IllegalArgumentException(e);
        }
        return classInstance;
    }

    /**
     * Create gwt item
     *
     * @param item
     * @return
     */
    private GWTJahiaToolbarItem createGWTItem(Item item) {
        // GWTJahiaToolbarItem
        GWTJahiaToolbarItem gwtToolbarItem = new GWTJahiaToolbarItem();
        if (item.getTitleKey() != null) {
            gwtToolbarItem.setTitle(getResources(item.getTitleKey(), getUILocale() != null ? getUILocale() : getLocale(), getSite()));
        } else {
            gwtToolbarItem.setTitle(item.getTitle());
        }
        gwtToolbarItem.setType(item.getId());
        gwtToolbarItem.setDisplayTitle(item.isDisplayTitle());
        if (item.getDescriptionKey() != null) {
            gwtToolbarItem.setDescription(getResources(item.getDescriptionKey(), getUILocale() != null ? getUILocale() : getLocale(), getSite()));
        } else {
            gwtToolbarItem.setDescription(gwtToolbarItem.getTitle());
        }
        gwtToolbarItem.setMediumIconStyle(item.getMediumIconStyle());
        gwtToolbarItem.setMinIconStyle(item.getMinIconStyle());
        if (item.getSelected() != null) {
            gwtToolbarItem.setSelected(item.getSelected().getRealValue(getSite(),  getRemoteJahiaUser(), getLocale()));
        } else {
            gwtToolbarItem.setSelected(false);
        }
        Map<String, GWTJahiaProperty> pMap = new HashMap<String, GWTJahiaProperty>();
        for (Property currentProperty : item.getProperties()) {
            GWTJahiaProperty gwtProperty = new GWTJahiaProperty();
            gwtProperty.setName(currentProperty.getName());
            gwtProperty.setValue(currentProperty.getRealValue(getSite(),  getRemoteJahiaUser(), getLocale()));
            pMap.put(gwtProperty.getName(), gwtProperty);
        }
        gwtToolbarItem.setLayout(getLayoutAsInt(item.getLayout()));
        gwtToolbarItem.setProperties(pMap);


        if (item.getWorkflowAction() != null) {
            try {
                List<WorkflowDefinition> def = WorkflowService.getInstance().getWorkflowsForAction(item.getWorkflowAction());
                List<String> processes = new ArrayList<String>();
                for (WorkflowDefinition workflowDefinition : def) {
                    processes.add(workflowDefinition.getKey());
                }
                gwtToolbarItem.setProcesses(processes);
                // todo : use the role assigned to the action for bypassing workflow ?
                final WorkflowActionItem workflowActionItem = new WorkflowActionItem(processes, getRemoteJahiaUser().isAdminMember(0), item.getActionItem());
                gwtToolbarItem.setActionItem(workflowActionItem);
            } catch (RepositoryException e) {
                logger.error("Cannot get workflows",e);
            }
        } else {
            gwtToolbarItem.setActionItem(item.getActionItem());
        }

        return gwtToolbarItem;
    }


    /**
     * Update GWT Jahia State Info
     *
     * @param gwtJahiaStateInfo
     * @return
     */
    public GWTJahiaStateInfo updateGWTJahiaStateInfo(GWTJahiaStateInfo gwtJahiaStateInfo) throws GWTJahiaServiceException {
        try {
            if (gwtJahiaStateInfo == null) {
                gwtJahiaStateInfo = new GWTJahiaStateInfo();
                gwtJahiaStateInfo.setLastViewTime(System.currentTimeMillis());
                if (gwtJahiaStateInfo.isNeedRefresh()) {
                    gwtJahiaStateInfo.setIconStyle("gwt-toolbar-icon-notification-refresh");
                }
            } else {
                if (gwtJahiaStateInfo.isNeedRefresh()) {
                    return gwtJahiaStateInfo;
                }
            }

            // remove last alert message
            gwtJahiaStateInfo.setAlertMessage(null);

            // check pdisplay
            if (gwtJahiaStateInfo.isCheckProcessInfo()) {
                GWTJahiaProcessJobInfo gwtProcessJobInfo = updateGWTProcessJobInfo(gwtJahiaStateInfo.getGwtProcessJobInfo(), -1);
                gwtJahiaStateInfo.setGwtProcessJobInfo(gwtProcessJobInfo);
                if (gwtProcessJobInfo.isJobExecuting()) {
                    gwtJahiaStateInfo.setIconStyle("gwt-toolbar-icon-wait-min");
                    gwtJahiaStateInfo.setText("Job is running (" + gwtProcessJobInfo.getNumberWaitingJobs() + ") waiting");
                } else if (gwtProcessJobInfo.getNumberWaitingJobs() > 0) {
                    gwtJahiaStateInfo.setIconStyle("gwt-toolbar-icon-notification-information");
                    gwtJahiaStateInfo.setText(gwtProcessJobInfo.getNumberWaitingJobs() + " waiting jobs");
                } else {
                    gwtJahiaStateInfo.setIconStyle("gwt-toolbar-icon-notification-ok");
                }

                // pdisplay need refresh need refresh
                if (gwtProcessJobInfo.isJobFinished()) {
                    gwtJahiaStateInfo.setAlertMessage(getLocaleJahiaAdminResource("label.processManagering.jobfinished"));

                    // current user job ended
                    if (gwtProcessJobInfo.isCurrentUserJob() && !gwtProcessJobInfo.isSystemJob()) {
                        gwtJahiaStateInfo.setCurrentUserJobEnded(true);
                    } else {
                        gwtJahiaStateInfo.setCurrentUserJobEnded(false);
                    }
                    // do we need to refresh ?
                    if (gwtJahiaStateInfo.isNeedRefresh() || gwtProcessJobInfo.isCurrentPageValidated()) {
                        gwtJahiaStateInfo.setNeedRefresh(true);
                        gwtJahiaStateInfo.setIconStyle("gwt-toolbar-icon-notification-refresh");
                        gwtJahiaStateInfo.setText(getLocaleJahiaAdminResource("label.processManagering.reloadPage"));
                        gwtJahiaStateInfo.setRefreshMessage(getLocaleJahiaAdminResource("label.processManagering.reloadPage"));
                    }

                } else {
                    gwtJahiaStateInfo.setCurrentUserJobEnded(false);
                    gwtJahiaStateInfo.setNeedRefresh(false);
                }
            }


            return gwtJahiaStateInfo;
        } catch (Exception e) {
            logger.error("Error when triing to load Jahia state info due to", e);
            throw new GWTJahiaServiceException("Error when triing to load Jahia state.");
        }
    }

    /**
     * Get Process Job stat
     *
     * @return
     */
    private GWTJahiaProcessJobInfo updateGWTProcessJobInfo(GWTJahiaProcessJobInfo gwtProcessJobInfo, int currentPageId) throws GWTJahiaServiceException {
        long lastExecutedJob = getSchedulerService().getLastJobCompletedTime();
        if (gwtProcessJobInfo == null) {
            gwtProcessJobInfo = new GWTJahiaProcessJobInfo();
            gwtProcessJobInfo.setLastViewTime(lastExecutedJob);
        }
        boolean isCurrentUser = false;
        boolean isSystemJob = true;
        boolean isCurrentPageValided = false;
        String lastExecutedJobLabel = "";
        String lastExecutionJobTitle = "";
        String link = null;
        JobDetail lastExecutedJobDetail = getSchedulerService().getLastCompletedJobDetail();
        if (lastExecutedJobDetail != null) {
            link = Jahia.getContextPath() + "/processing/jobreport.jsp?name=" + lastExecutedJobDetail.getName() + "&groupName=" + lastExecutedJobDetail.getGroup();
            JobDataMap lastExecutedJobDataMap = lastExecutedJobDetail.getJobDataMap();
            if (lastExecutedJobDataMap != null) {

                // set 'is current user' flag
                String lastExecutedJobUserKey = lastExecutedJobDataMap.getString(BackgroundJob.JOB_USERKEY);
                if (lastExecutedJobUserKey != null) {
                    isCurrentUser = lastExecutedJobUserKey.equalsIgnoreCase(getRemoteUser());
                }

                // set title if any
                lastExecutionJobTitle = lastExecutedJobDataMap.getString(BackgroundJob.JOB_TITLE);

                // set 'is System Job'
                String lastExecutedJobType = lastExecutedJobDataMap.getString(BackgroundJob.JOB_TYPE);
                if (lastExecutedJobType != null) {
                    // is system job
//                    isSystemJob = !lastExecutedJobType.equalsIgnoreCase(AbstractActivationJob.WORKFLOW_TYPE);

                    // workflow
//                    if (lastExecutedJobType.equalsIgnoreCase(AbstractActivationJob.WORKFLOW_TYPE)) {
//                        lastExecutedJobLabel = getLocaleJahiaEnginesResource("org.jahia.engines.processDisplay.op.workflow.label");
//                    }
                }
            }
        }


        // current executing jobs
        try {

            List currentlyExecutingJobs = getSchedulerService().getCurrentlyExecutingJobs();
            if (currentlyExecutingJobs != null && currentlyExecutingJobs.size() > 0) {
                gwtProcessJobInfo.setJobExecuting(true);
            } else {
                gwtProcessJobInfo.setJobExecuting(false);
            }
            // get all job list
            List jobList = ProcessDisplayServiceImpl.getAllActiveJobsDetails();
            int waitingJobNumber = 0;
            int nextJobCurrentUserIndex = -1;
            String nextJobCurrentUserType = "-";
            int maxIndex = jobList.size();
            gwtProcessJobInfo.setNumberJobs(maxIndex);
            for (int jobIndex = 0; jobIndex < maxIndex; jobIndex++) {
                JobDetail currentJobDetail = (JobDetail) jobList.get(jobIndex);
                JobDataMap currentJobDataMap = currentJobDetail.getJobDataMap();
                // job: type
                String type = currentJobDataMap.getString(BackgroundJob.JOB_TYPE);

                // job: status
                String currentJobStatus = currentJobDataMap.getString(BackgroundJob.JOB_STATUS);

                // job: user key
                String currentJobUserKey = currentJobDataMap.getString(BackgroundJob.JOB_USERKEY);
                if (currentJobStatus.equalsIgnoreCase(BackgroundJob.STATUS_WAITING)) {
                    waitingJobNumber++;
                    if (currentJobUserKey != null && getRemoteUser() != null) {
                        if (currentJobUserKey.equalsIgnoreCase(getRemoteUser())) {
                            if (nextJobCurrentUserIndex == -1) {
                                nextJobCurrentUserType = type;
                            }
                        } else {
                            // update nex job index
                            nextJobCurrentUserIndex++;
                        }
                    }
                }
                if (currentJobStatus.equalsIgnoreCase(BackgroundJob.STATUS_RUNNING)) {
                    // update nex job index
                    nextJobCurrentUserIndex++;
                    nextJobCurrentUserType = type;

                }
            }

            // set need to refresh flag
            gwtProcessJobInfo.setJobFinished(gwtProcessJobInfo.getLastViewTime() < lastExecutedJob);

            boolean pageRefresh = false;
            String value = preferencesService.getGenericPreferenceValue(ProcessDisplayServiceImpl.PREF_PAGE_REFRESH, getRemoteJahiaUser());
            if (value != null && value.length() > 0) {
                try {
                    pageRefresh = Boolean.parseBoolean(value);
                } catch (Exception e) {
                    logger.error(e.getMessage(), e);
                }

            }


            // set values
            gwtProcessJobInfo.setCurrentUserJob(isCurrentUser);
            gwtProcessJobInfo.setCurrentPageValidated(false);
            gwtProcessJobInfo.setSystemJob(isSystemJob);
            gwtProcessJobInfo.setJobReportUrl(link);
            if (lastExecutionJobTitle == null) {
                lastExecutionJobTitle = "";
            }
            gwtProcessJobInfo.setJobType(lastExecutedJobLabel);
            gwtProcessJobInfo.setLastTitle(lastExecutionJobTitle);
            gwtProcessJobInfo.setAutoRefresh(pageRefresh);
            gwtProcessJobInfo.setLastViewTime(lastExecutedJob);
            gwtProcessJobInfo.setNumberWaitingJobs(waitingJobNumber);
            gwtProcessJobInfo.setNextJobCurrentUserIndex(nextJobCurrentUserIndex);
            gwtProcessJobInfo.setNextJobCurrentUserType(nextJobCurrentUserType);
        } catch (Exception e) {
            logger.error("Unable to get number of running job.", e);
            throw new GWTJahiaServiceException(e.getMessage());
        }
        return gwtProcessJobInfo;
    }


    /**
     * Get Scheduler Service
     *
     * @return
     */
    private SchedulerService getSchedulerService() {
        if (SCHEDULER_SERVICE == null) {
            SCHEDULER_SERVICE = ServicesRegistry.getInstance().getSchedulerService();
        }
        return SCHEDULER_SERVICE;
    }

}
