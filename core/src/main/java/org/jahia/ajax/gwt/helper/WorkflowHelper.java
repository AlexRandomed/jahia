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
package org.jahia.ajax.gwt.helper;

import org.apache.commons.lang.StringUtils;
import org.atmosphere.cpr.Broadcaster;
import org.atmosphere.cpr.BroadcasterFactory;
import org.atmosphere.cpr.DefaultBroadcasterFactory;
import org.jahia.ajax.gwt.client.data.definition.GWTJahiaNodeProperty;
import org.jahia.ajax.gwt.client.data.definition.GWTJahiaNodePropertyValue;
import org.jahia.ajax.gwt.client.data.node.GWTJahiaNode;
import org.jahia.ajax.gwt.client.data.workflow.*;
import org.jahia.ajax.gwt.client.data.workflow.history.GWTJahiaWorkflowHistoryItem;
import org.jahia.ajax.gwt.client.data.workflow.history.GWTJahiaWorkflowHistoryProcess;
import org.jahia.ajax.gwt.client.data.workflow.history.GWTJahiaWorkflowHistoryTask;
import org.jahia.ajax.gwt.client.service.GWTJahiaServiceException;
import org.jahia.ajax.gwt.client.widget.poller.TaskEvent;
import org.jahia.ajax.gwt.client.widget.workflow.CustomWorkflow;
import org.jahia.ajax.gwt.commons.server.ManagedGWTResource;
import org.jahia.registries.ServicesRegistry;
import org.jahia.services.SpringContextSingleton;
import org.jahia.services.content.JCRNodeWrapper;
import org.jahia.services.content.JCRPublicationService;
import org.jahia.services.content.JCRSessionFactory;
import org.jahia.services.content.JCRSessionWrapper;
import org.jahia.services.preferences.user.UserPreferencesHelper;
import org.jahia.services.usermanager.JahiaGroup;
import org.jahia.services.usermanager.JahiaPrincipal;
import org.jahia.services.usermanager.JahiaUser;
import org.jahia.services.workflow.*;
import org.jahia.utils.LanguageCodeConverters;
import org.quartz.SchedulerException;
import org.slf4j.Logger;

import javax.jcr.ItemNotFoundException;
import javax.jcr.NodeIterator;
import javax.jcr.RepositoryException;

import java.security.Principal;
import java.util.*;

/**
 * Workflow operation helper for the GWT backend.
 * User: toto
 * Date: Feb 4, 2010
 * Time: 3:48:28 PM
 */
public class WorkflowHelper {
    private static final transient Logger logger = org.slf4j.LoggerFactory.getLogger(WorkflowHelper.class);

    private WorkflowService service;

    public void setService(WorkflowService service) {
        this.service = service;
    }

    public void start() {
        service.addWorkflowListener(new PollingWorkflowListener());
    }


    public GWTJahiaWorkflowInfo getWorkflowInfo(String path, boolean includeActiveWorfklows, JCRSessionWrapper session, Locale locale, Locale uiLocale)
            throws GWTJahiaServiceException {
        try {
            GWTJahiaWorkflowInfo info = new GWTJahiaWorkflowInfo();

            Map<GWTJahiaWorkflowType, GWTJahiaWorkflowDefinition> gwtWorkflowDefinitions = new HashMap<GWTJahiaWorkflowType, GWTJahiaWorkflowDefinition>();
            info.setPossibleWorkflows(gwtWorkflowDefinitions);
            JCRNodeWrapper node = session.getNode(path);

            try {
                Map<String, WorkflowDefinition> wfs = service.getPossibleWorkflows(node, true, locale);
                for (Map.Entry<String, WorkflowDefinition> entry : wfs.entrySet()) {
                    gwtWorkflowDefinitions.put(getGWTJahiaWorkflowType(entry.getKey()), getGWTJahiaWorkflowDefinition(entry.getValue()));
                }
            }catch(Exception ex) {
                logger.error("Error to get possible workflows for " +  node.getPath(), ex);
            }

            Map<GWTJahiaWorkflowType, GWTJahiaWorkflow> gwtWorkflows = new HashMap<GWTJahiaWorkflowType, GWTJahiaWorkflow>();
            info.setActiveWorkflows(gwtWorkflows);
            if (includeActiveWorfklows) {
                List<Workflow> actives = service.getActiveWorkflows(node, locale, uiLocale);
                for (Workflow workflow : actives) {
                    GWTJahiaWorkflow gwtWf = getGWTJahiaWorkflow(workflow);
                    gwtWorkflows.put(getGWTJahiaWorkflowType(service.getWorkflowType(workflow.getWorkflowDefinition())), gwtWf);
                    for (WorkflowAction workflowAction : workflow.getAvailableActions()) {
                        if (workflowAction instanceof WorkflowTask) {
                            WorkflowTask workflowTask = (WorkflowTask) workflowAction;
                            List<WorkflowParticipation> participations = workflowTask.getParticipations();
                            if (participations != null) {
                                for (WorkflowParticipation participation : participations) {
                                    JahiaPrincipal principal = participation.getJahiaPrincipal();
                                    if ((principal instanceof JahiaGroup && ((JahiaGroup) principal).isMember(session.getUser())) ||
                                            (principal instanceof JahiaUser && ((JahiaUser) principal).getUserKey().equals(session.getUser().getUserKey()))) {
                                        gwtWf.getAvailableTasks().add(getGWTJahiaWorkflowTask(workflowTask));
                                        break;
                                    }
                                }
                            }
                        }
                    }
                }
            }
            return info;
        } catch (RepositoryException e) {
            logger.error(e.getMessage(), e);
            throw new GWTJahiaServiceException(e.getMessage());
        }
    }

    public GWTJahiaWorkflow getGWTJahiaWorkflow(Workflow wf) {
        GWTJahiaWorkflow gwtWf;
        gwtWf = new GWTJahiaWorkflow();
        gwtWf.setId(wf.getId());
        gwtWf.setProvider(wf.getProvider());
        gwtWf.setDefinition(getGWTJahiaWorkflowDefinition(wf.getWorkflowDefinition()));
        gwtWf.setAvailableTasks(new ArrayList<GWTJahiaWorkflowTask>());
        Map<String, Object> map = wf.getVariables();
        gwtWf.setStartTime(wf.getStartTime());
        gwtWf.setVariables(getPropertiesMap(map));
        if (map.get("customWorkflowInfo") != null) {
            gwtWf.setCustomWorkflowInfo((CustomWorkflow) map.get("customWorkflowInfo"));
        }
        gwtWf.setLocale(map.get("locale").toString());
        gwtWf.setWorkspace(map.get("workspace").toString());
        if (wf.getDuedate() != null) {
            gwtWf.setDuedate(wf.getDuedate());
        }
        return gwtWf;
    }

    public GWTJahiaWorkflowTask getGWTJahiaWorkflowTask(WorkflowTask workflowTask) {
        GWTJahiaWorkflowTask task = new GWTJahiaWorkflowTask();
        List<GWTJahiaWorkflowOutcome> gwtOutcomes = new ArrayList<GWTJahiaWorkflowOutcome>();
        task.setProvider(workflowTask.getProvider());
        task.setOutcomes(gwtOutcomes);
        task.setName(workflowTask.getName());
        task.setDisplayName(workflowTask.getDisplayName());
        task.setId(workflowTask.getId());
        task.setFormResourceName(workflowTask.getFormResourceName());
        task.setCreateTime(workflowTask.getCreateTime());
        task.setProcessId(workflowTask.getProcessId());
        Set<String> outcomes = workflowTask.getOutcomes();
        List<String> display = workflowTask.getDisplayOutcomes();
        List<String> icons = workflowTask.getOutcomeIcons();
        int i = 0;
        for (String outcome : outcomes) {
            GWTJahiaWorkflowOutcome gwtOutcome = new GWTJahiaWorkflowOutcome();
            gwtOutcome.setName(outcome);
            gwtOutcome.setLabel(display.get(i));
            gwtOutcome.setIcon(icons.get(i));
            i++;
            gwtOutcomes.add(gwtOutcome);
        }
        task.setVariables(getPropertiesMap(workflowTask.getVariables()));
        return task;
    }

    private GWTJahiaWorkflowHistoryProcess getGWTJahiaHistoryProcess(HistoryWorkflow wf) {
        return new GWTJahiaWorkflowHistoryProcess(wf.getName(), wf.getDisplayName(), wf.getProcessId(), wf
                .getProvider(), wf.getWorkflowDefinition().getKey(), wf.isCompleted(), wf.getStartTime(), wf.getEndTime(), wf.getDuration(),
                getUsername(wf.getUser()), wf.getNodeId());
    }

    public GWTJahiaWorkflowDefinition getGWTJahiaWorkflowDefinition(String key, Locale uiLocale) {
        return getGWTJahiaWorkflowDefinition(service.getWorkflowDefinition(StringUtils.substringBefore(key, ":"), StringUtils.substringAfter(key, ":"), uiLocale));
    }

    public GWTJahiaWorkflowDefinition getGWTJahiaWorkflowDefinition(WorkflowDefinition workflow) {
        GWTJahiaWorkflowDefinition w = new GWTJahiaWorkflowDefinition();
        w.setProvider(workflow.getProvider());
        w.setName(workflow.getName());
        w.setId(workflow.getKey());
        w.setFormResourceName(workflow.getFormResourceName());
        w.setDisplayName(workflow.getDisplayName());
        return w;
    }

    public void startWorkflow(String path, GWTJahiaWorkflowDefinition def, JCRSessionWrapper session,
                              List<GWTJahiaNodeProperty> properties, List<String> comments)
            throws GWTJahiaServiceException {
        try {
            JCRNodeWrapper node = session.getNode(path);
            Map<String, Object> map = getVariablesMap(properties);
            service.startProcessAsJob(Arrays.asList(node.getIdentifier()), session, def.getId(), def.getProvider(), map, comments);
        } catch (SchedulerException e) {
            logger.error(e.getMessage(), e);
            throw new GWTJahiaServiceException(e.getMessage());
        } catch (RepositoryException e) {
            logger.error(e.getMessage(), e);
            throw new GWTJahiaServiceException(e.getMessage());
        }
    }

    public void startWorkflow(List<String> uuids, GWTJahiaWorkflowDefinition def, JCRSessionWrapper session,
                              List<GWTJahiaNodeProperty> properties, List<String> comments, Map<String, Object> args)
            throws GWTJahiaServiceException {

        try {
            Map<String, Object> map = getVariablesMap(properties);
            map.putAll(args);
            service.startProcessAsJob(uuids, session, def.getId(), def.getProvider(), map, comments);
        } catch (SchedulerException e) {
            logger.error(e.getMessage(), e);
            throw new GWTJahiaServiceException(e.getMessage());
        } catch (RepositoryException e) {
            logger.error(e.getMessage(), e);
            throw new GWTJahiaServiceException(e.getMessage());
        }
    }

    public void abortWorkflow(String processId, String provider) {
        try {
            Workflow w = service.getWorkflow(provider, processId, null);
            if (w != null && w.getWorkflowDefinition().getWorkflowType().equals("publish")) {
                @SuppressWarnings("unchecked")
                List<String> info = (List<String>) w.getVariables().get("nodeIds");
                String workspace = (String) w.getVariables().get("workspace");
                JCRPublicationService.getInstance().unlockForPublication(info, workspace, "publication-process-" + processId);
            }
        } catch (Exception e) {
            logger.error("Cannot clear workflow locks", e);
        }
        service.abortProcess(processId, provider);
    }

    public void assignAndCompleteTask(GWTJahiaWorkflowTask task, GWTJahiaWorkflowOutcome outcome,
                                      JCRSessionWrapper session, List<GWTJahiaNodeProperty> properties) throws GWTJahiaServiceException {
        try {
            Map<String, Object> map = getVariablesMap(properties);
            service.assignAndCompleteTask(task.getId(), task.getProvider(), outcome.getName(), map, session.getUser());
        } catch (Exception e) {
            logger.error("Exception in task", e);
            throw new GWTJahiaServiceException(e.getMessage());
        }
    }

    private Map<String, GWTJahiaNodeProperty> getPropertiesMap(Map<String, Object> variables) {
        Map<String, GWTJahiaNodeProperty> properties = new HashMap<String, GWTJahiaNodeProperty>(variables.size());
        for (Map.Entry<String, Object> entry : variables.entrySet()) {
            GWTJahiaNodeProperty property = new GWTJahiaNodeProperty();
            property.setName(entry.getKey());
            Object variable = entry.getValue();
            if (variable instanceof List) {
                List list = (List) variable;
                List<GWTJahiaNodePropertyValue> values = new ArrayList<GWTJahiaNodePropertyValue>();
                for (Object o : list) {
                    if (o instanceof WorkflowVariable) {
                        values.add(new GWTJahiaNodePropertyValue(((WorkflowVariable) o).getValue(), ((WorkflowVariable) o).getType()));
                    }
                }
                if (!values.isEmpty()) {
                    property.setValues(values);
                    properties.put(entry.getKey(), property);
                }
            } else if (variable instanceof WorkflowVariable) {
                property.setValue(new GWTJahiaNodePropertyValue(((WorkflowVariable) variable).getValue(), ((WorkflowVariable) variable).getType()));
                properties.put(entry.getKey(), property);
            }
        }
        return properties;
    }

    private Map<String, Object> getVariablesMap(List<GWTJahiaNodeProperty> properties) {
        Map<String, Object> map = new HashMap<String, Object>();
        for (GWTJahiaNodeProperty property : properties) {
            List<GWTJahiaNodePropertyValue> propertyValues = property.getValues();
            if (property.isMultiple()) {
                List<WorkflowVariable> values = new ArrayList<WorkflowVariable>();
                for (GWTJahiaNodePropertyValue value : propertyValues) {
                    String s = value.getString();
                    if (StringUtils.isNotBlank(s)) {
                        values.add(new WorkflowVariable(s, value.getType()));
                    }
                }
                map.put(property.getName(), values);
            } else if (!propertyValues.isEmpty()) {
                GWTJahiaNodePropertyValue value = propertyValues.get(0);
                String s = value.getString();
                if (StringUtils.isNotBlank(s)) {
                    map.put(property.getName(), new WorkflowVariable(s, value.getType()));
                }
            }
        }
        return map;
    }

    public void addCommentToWorkflow(GWTJahiaWorkflow workflow, JahiaUser user, String comment, Locale locale) {
        service.addComment(workflow.getId(), workflow.getProvider(), comment, user.getUserKey());
    }

    public List<GWTJahiaWorkflowComment> getWorkflowComments(GWTJahiaWorkflow workflow, Locale locale) {
        Workflow wf = service.getWorkflow(workflow.getProvider(), workflow.getId(), locale);
        List<GWTJahiaWorkflowComment> gwtComments = new ArrayList<GWTJahiaWorkflowComment>();
        if (wf == null) {
            return gwtComments;
        }
        List<WorkflowComment> comments = wf.getComments();

        if (comments == null) {
            return gwtComments;
        }

        for (WorkflowComment comment : comments) {
            final GWTJahiaWorkflowComment workflowComment = new GWTJahiaWorkflowComment();
            workflowComment.setComment(comment.getComment());
            workflowComment.setTime(comment.getTime());
            final JahiaUser user =
                    ServicesRegistry.getInstance().getJahiaUserManagerService().lookupUserByKey(comment.getUser());
            if (user != null) {
                workflowComment.setUser(user.getName());
            } else {
                workflowComment.setUser(comment.getUser());
            }
            gwtComments.add(workflowComment);
        }

        return gwtComments;
    }

    public List<GWTJahiaWorkflowHistoryItem> getWorkflowHistoryProcesses(String nodeId, JCRSessionWrapper session, Locale uiLocale) throws GWTJahiaServiceException {
        List<GWTJahiaWorkflowHistoryItem> history = new ArrayList<GWTJahiaWorkflowHistoryItem>();
        try {
            // read all processes
            List<HistoryWorkflow> workflows = service.getHistoryWorkflows(session.getNodeByIdentifier(nodeId), uiLocale);
            for (HistoryWorkflow wf : workflows) {
                history.add(getGWTJahiaHistoryProcess(wf));
            }
        } catch (RepositoryException e) {
            logger.error(e.getMessage(), e);
            throw new GWTJahiaServiceException(e.getMessage());
        }
        return history;
    }

    private String getUsername(String userKey) {
        String username = "";
        if (userKey != null) {
            final JahiaUser jahiaUser =
                    ServicesRegistry.getInstance().getJahiaUserManagerService().lookupUserByKey(userKey);
            if (jahiaUser != null) {
                username = jahiaUser.getName();
            } else {
                username = StringUtils.substringAfter(userKey, "}");
            }
        }
        return username;
    }

    public List<GWTJahiaWorkflowHistoryItem> getWorkflowHistoryTasks(String provider, String processId, Locale uiLocale) throws GWTJahiaServiceException {
        List<GWTJahiaWorkflowHistoryItem> history = new ArrayList<GWTJahiaWorkflowHistoryItem>();
        // read tasks of the process
        List<HistoryWorkflowTask> tasks = service.getHistoryWorkflowTasks(processId, provider, uiLocale);

        for (HistoryWorkflowTask wfTask : tasks) {
            history.add(new GWTJahiaWorkflowHistoryTask(wfTask.getActionId(), wfTask.getName(),
                    wfTask.getDisplayName() + (wfTask.getDisplayOutcome() != null ? " : " + wfTask.getDisplayOutcome() : ""),
                    wfTask.getProcessId(), wfTask
                    .getProvider(), wfTask.isCompleted(), wfTask.getStartTime(), wfTask.getEndTime(), wfTask
                    .getDuration(), wfTask.getOutcome(), getUsername(wfTask.getUser())));
        }

        return history;
    }

    public List<GWTJahiaWorkflowHistoryItem> getWorkflowHistoryForUser(JahiaUser user, Locale locale, Locale uiLocale) throws GWTJahiaServiceException {
        List<GWTJahiaWorkflowHistoryItem> gwtWorkflows = new ArrayList<GWTJahiaWorkflowHistoryItem>();

        Map<String, GWTJahiaWorkflowHistoryProcess> gwtWorkflowsMap = new HashMap<String, GWTJahiaWorkflowHistoryProcess>();

        List<WorkflowTask> tasks = service.getTasksForUser(user, uiLocale);
        for (WorkflowTask task : tasks) {
            GWTJahiaWorkflowHistoryProcess gwtWfHistory = gwtWorkflowsMap.get(task.getProcessId());
            HistoryWorkflow historyWorkflow = service.getHistoryWorkflow(task.getProcessId(), task.getProvider(), uiLocale);
            if (historyWorkflow != null) {
                if (gwtWfHistory == null) {
                    gwtWfHistory = getGWTJahiaHistoryProcess(historyWorkflow);
                    gwtWfHistory.setAvailableTasks(new ArrayList<GWTJahiaWorkflowTask>());
                    gwtWorkflowsMap.put(task.getProcessId(), gwtWfHistory);

                    List<String> nodeIds;
                    final Workflow wf = service.getWorkflow(gwtWfHistory.getProvider(), gwtWfHistory.getProcessId(), uiLocale);
                    if (wf != null) {
                        gwtWfHistory.setRunningWorkflow(getGWTJahiaWorkflow(wf));
                        nodeIds = (List<String>) wf.getVariables().get("nodeIds");
                        if (nodeIds == null) {
                            nodeIds = Collections.singletonList((String) wf.getVariables().get("nodeId"));
                        }
                    } else {
                        nodeIds = Collections.singletonList(gwtWfHistory.getNodeId());
                    }

                    for (String nodeId : nodeIds) {
                        try {
                            JCRNodeWrapper nodeWrapper = JCRSessionFactory.getInstance().getCurrentUserSession(null, locale).getNodeByIdentifier(nodeId);
                            gwtWfHistory.set("nodeWrapper", ((NavigationHelper) SpringContextSingleton.getInstance().getContext().getBeansOfType(NavigationHelper.class).values().iterator().next()).getGWTJahiaNode(nodeWrapper));

                            gwtWorkflows.add(gwtWfHistory);

                            break;
                        } catch (ItemNotFoundException e) {
                            // Node cannot be found
                        } catch (RepositoryException e) {
                            logger.warn(e.getMessage(), e);
                        }
                    }
                }
                gwtWfHistory.getAvailableTasks().add(getGWTJahiaWorkflowTask(task));
            }
        }

        List<Workflow> workflows = service.getWorkflowsForUser(user, uiLocale);
        for (Workflow wf : workflows) {
            GWTJahiaWorkflowHistoryProcess gwtWfHistory = gwtWorkflowsMap.get(wf.getId());
            if (gwtWfHistory == null) {
                gwtWfHistory = getGWTJahiaHistoryProcess(service.getHistoryWorkflow(wf.getId(), wf.getProvider(), uiLocale));
                try {
                    JCRNodeWrapper nodeWrapper = JCRSessionFactory.getInstance().getCurrentUserSession(org.jahia.api.Constants.EDIT_WORKSPACE, locale).getNodeByIdentifier(
                            gwtWfHistory.getNodeId());
                    gwtWfHistory.set("nodeWrapper", ((NavigationHelper) SpringContextSingleton.getInstance().getContext().getBeansOfType(NavigationHelper.class).values().iterator().next()).getGWTJahiaNode(nodeWrapper));
                } catch (RepositoryException e) {
                    logger.warn(e.getMessage(), e);
                    continue;
                }
                gwtWorkflowsMap.put(wf.getId(), gwtWfHistory);
                gwtWorkflows.add(gwtWfHistory);
                gwtWfHistory.setRunningWorkflow(getGWTJahiaWorkflow(wf));
                gwtWfHistory.setAvailableTasks(new ArrayList<GWTJahiaWorkflowTask>());
            }
        }

        return gwtWorkflows;
    }

    public Map<GWTJahiaWorkflowType, List<GWTJahiaWorkflowDefinition>> getWorkflowRules(String path, JCRSessionWrapper session,
                                                                                        Locale uiLocale) throws GWTJahiaServiceException {
        try {
            Map<String, String> rev = new HashMap<String, String>();
            Map<GWTJahiaWorkflowType, List<GWTJahiaWorkflowDefinition>> result = new HashMap<GWTJahiaWorkflowType, List<GWTJahiaWorkflowDefinition>>();
            Map<String, List<GWTJahiaWorkflowDefinition>> keyToMap = new HashMap<String, List<GWTJahiaWorkflowDefinition>>();

            final Set<String> workflowTypes = service.getTypesOfWorkflow();
            for (String workflowType : workflowTypes) {
                List<GWTJahiaWorkflowDefinition> definitions = new ArrayList<GWTJahiaWorkflowDefinition>();
                List<WorkflowDefinition> workflowDefinitions = service.getWorkflowDefinitionsForType(workflowType,
                        uiLocale);
                for (WorkflowDefinition definition : workflowDefinitions) {
                    final GWTJahiaWorkflowDefinition workflowDefinition = getGWTJahiaWorkflowDefinition(definition);
                    definitions.add(workflowDefinition);
                    rev.put(definition.getKey(), workflowType);
                }
                GWTJahiaWorkflowType t = getGWTJahiaWorkflowType(workflowType);
                result.put(t, definitions);
                keyToMap.put(workflowType, definitions);
            }

            JCRNodeWrapper node = session.getNode(path);

            // Get local definitions
            Collection<WorkflowRule> map = service.getWorkflowRules(node);
            for (WorkflowRule rule : map) {
                try {
                    final WorkflowDefinition definition = service.getWorkflowDefinition(rule.getProviderKey(),
                            rule.getWorkflowDefinitionKey(),
                            uiLocale);
                    if (definition != null) {
                        final GWTJahiaWorkflowDefinition workflowDefinition = getGWTJahiaWorkflowDefinition(definition);
                        workflowDefinition.set("active", Boolean.TRUE);
                        workflowDefinition.set("definitionPath", rule.getDefinitionPath());
                        keyToMap.get(rev.get(definition.getKey())).remove(workflowDefinition);
                        keyToMap.get(rev.get(definition.getKey())).add(workflowDefinition);
                    } else {
                        logger.warn("Couldn't find definition for workflow " + rule.getWorkflowDefinitionKey());
                    }
                } catch (Exception e) {
                    logger.error(e.getMessage(), e);
                }
            }
            return result;
        } catch (RepositoryException e) {
            logger.error(e.getMessage(), e);
            throw new GWTJahiaServiceException(e.getMessage());
        }
    }

    private GWTJahiaWorkflowType getGWTJahiaWorkflowType(String workflowType) {
        GWTJahiaWorkflowType t = new GWTJahiaWorkflowType();
        t.setDisplayName(workflowType);
        t.setName(workflowType);
        return t;
    }

    public List<GWTJahiaWorkflowDefinition> getWorkflows(Locale locale) throws GWTJahiaServiceException {
        try {
            final Set<String> workflowTypes = service.getTypesOfWorkflow();
            List<GWTJahiaWorkflowDefinition> definitions = new ArrayList<GWTJahiaWorkflowDefinition>();
            for (String workflowType : workflowTypes) {
                final List<WorkflowDefinition> workflowDefinitions = service.getWorkflowDefinitionsForType(workflowType,
                        locale);
                for (WorkflowDefinition definition : workflowDefinitions) {
                    definitions.add(getGWTJahiaWorkflowDefinition(definition));
                }
            }
            return definitions;
        } catch (RepositoryException e) {
            logger.error(e.getMessage(), e);
            throw new GWTJahiaServiceException(e.getMessage());
        }
    }

    public void updateWorkflowRules(GWTJahiaNode gwtNode, Set<GWTJahiaWorkflowDefinition> actives, JCRSessionWrapper session) throws GWTJahiaServiceException {
        try {
            JCRNodeWrapper node = session.getNode(gwtNode.getPath());
            if (!node.isCheckedOut()) {
                session.checkout(node);
            }
            if (!actives.isEmpty()) {
                if (!node.isNodeType("jmix:workflowRulesable")) {
                    node.addMixin("jmix:workflowRulesable");
                }
                session.save();
            }
            if (node.hasNode(WorkflowService.WORKFLOWRULES_NODE_NAME)) {
                JCRNodeWrapper wfRulesNode = node.getNode(WorkflowService.WORKFLOWRULES_NODE_NAME);
                if (!wfRulesNode.isCheckedOut()) {
                    session.checkout(wfRulesNode);
                }
                Set<String> activeKeys = new HashSet<String>();
                for (GWTJahiaWorkflowDefinition definition : actives) {
                    final String defKey = definition.getProvider() + "_" + definition.getId();
                    activeKeys.add(defKey);

                    JCRNodeWrapper wfRuleNode;
                    if (!wfRulesNode.hasNode(defKey)) {
                        wfRuleNode = wfRulesNode.addNode(defKey, "jnt:workflowRule");
                        wfRuleNode.setProperty("j:workflow", definition.getProvider() + ":" + definition.getId());
                    } else {
                        wfRuleNode = wfRulesNode.getNode(defKey);
                    }
                }
                if (actives == null || actives.isEmpty()) {
                    // No more active definitions for this nodes
                    wfRulesNode.remove();
                    // Remove also associated workflows
                    if (node.isNodeType("jmix:workflowRulesable")) {
                        node.removeMixin("jmix:workflowRulesable");
                    }
                } else {
                    NodeIterator ni = wfRulesNode.getNodes();
                    while (ni.hasNext()) {
                        JCRNodeWrapper rule = (JCRNodeWrapper) ni.next();
                        if (!activeKeys.contains(rule.getName())) {
                            rule.remove();
                        }
                    }
                }
            }
            session.save();
        } catch (RepositoryException e) {
            logger.error(e.getMessage(), e);
            throw new GWTJahiaServiceException(e.getMessage());
        }
    }

    public int getNumberOfTasksForUser(JahiaUser user) throws GWTJahiaServiceException {
        int total = 0;
        List<WorkflowTask> tasks = service.getTasksForUser(user, null);
        for (WorkflowTask task : tasks) {
            Workflow workflow = service.getWorkflow(task.getProvider(), task.getProcessId(), null);
            if (workflow != null) {
                List<String> uuids = (List<String>) workflow.getVariables().get("nodeIds");
                if (uuids == null) {
                    uuids = Collections.singletonList((String) workflow.getVariables().get("nodeId"));
                }
                for (String uuid : uuids) {
                    try {
                        JCRSessionFactory.getInstance().getCurrentUserSession(null,null).getNodeByIdentifier(uuid);
                        total++;
                        break;
                    } catch (ItemNotFoundException e) {
                        //
                    } catch (RepositoryException e) {
                        logger.warn(e.getMessage(), e);
                    }
                }
            }
        }
        return total;
    }

    class PollingWorkflowListener extends WorkflowListener {

        @Override
        public void workflowEnded(HistoryWorkflow workflow) {
            JahiaUser user = ServicesRegistry.getInstance().getJahiaUserManagerService().lookupUserByKey(workflow.getUser());
            final BroadcasterFactory broadcasterFactory = DefaultBroadcasterFactory.getDefault();
            Broadcaster broadcaster = broadcasterFactory.lookup(ManagedGWTResource.GWT_BROADCASTER_ID + user.getName());
            if (broadcaster != null) {
                TaskEvent taskEvent = new TaskEvent();
                Locale preferredLocale = UserPreferencesHelper.getPreferredLocale(user);
                if (preferredLocale == null) {
                    preferredLocale = LanguageCodeConverters.languageCodeToLocale(ServicesRegistry.getInstance().getJahiaSitesService().getDefaultSite().getDefaultLanguage());
                }
                workflow = service.getHistoryWorkflow(workflow.getProcessId(), workflow.getProvider(), preferredLocale);
                taskEvent.setEndedWorkflow(getGWTJahiaHistoryProcess(workflow));

                broadcaster.broadcast(taskEvent);
            }
        }

        @Override
        public void taskEnded(WorkflowTask task) {
            update(task, false);
        }

        @Override
        public void newTaskCreated(WorkflowTask task) {
            update(task, true);
        }

        private void update(WorkflowTask task, boolean newTask) {
            final BroadcasterFactory broadcasterFactory = BroadcasterFactory.getDefault();
            if (broadcasterFactory != null) {

                Set<Principal> users = new HashSet<Principal>();
                for (WorkflowParticipation workflowParticipation : task.getParticipations()) {
                    JahiaPrincipal p = workflowParticipation.getJahiaPrincipal();
                    if (p instanceof JahiaUser) {
                        users.add(p);
                    } else if (p instanceof JahiaGroup) {
                        users.addAll(((JahiaGroup) p).getRecursiveUserMembers());
                    }
                }
                for (Principal user : users) {
                    if (user != null) {
                        Broadcaster broadcaster = broadcasterFactory.lookup(ManagedGWTResource.GWT_BROADCASTER_ID + user.getName());
                        if (broadcaster != null) {
                            TaskEvent taskEvent = new TaskEvent();
                            try {
                                JahiaUser jahiaUser = (JahiaUser) user;
                                if (newTask) {
                                    Locale preferredLocale = UserPreferencesHelper.getPreferredLocale(jahiaUser);
                                    if (preferredLocale == null) {
                                        preferredLocale = LanguageCodeConverters.languageCodeToLocale(ServicesRegistry.getInstance().getJahiaSitesService().getDefaultSite().getDefaultLanguage());
                                    }
                                    task = service.getWorkflowTask(task.getId(), task.getProvider(), preferredLocale);
                                    taskEvent.setNewTask(getGWTJahiaWorkflowTask(task));
                                }
                                taskEvent.setNumberOfTasks(getNumberOfTasksForUser(jahiaUser));
                                if (!newTask && user.equals(task.getAssignee())) {
                                    taskEvent.setNumberOfTasks(taskEvent.getNumberOfTasks() - 1);
                                }
                            } catch (GWTJahiaServiceException e) {
                            }

                            broadcaster.broadcast(taskEvent);
                        }
                    }
                }
            }
        }

    }

}
