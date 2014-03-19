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
package org.jahia.services.workflow.jbpm;

import org.jahia.registries.ServicesRegistry;
import org.jahia.services.SpringContextSingleton;
import org.jahia.services.content.*;
import org.jahia.services.content.nodetypes.ExtendedNodeType;
import org.jahia.services.content.nodetypes.ExtendedPropertyDefinition;
import org.jahia.services.content.nodetypes.NodeTypeRegistry;
import org.jahia.services.usermanager.JahiaGroup;
import org.jahia.services.usermanager.JahiaPrincipal;
import org.jahia.services.usermanager.JahiaUser;
import org.jahia.services.usermanager.jcr.JCRGroup;
import org.jahia.services.usermanager.jcr.JCRUser;
import org.jahia.services.usermanager.jcr.JCRUserManagerProvider;
import org.jahia.services.workflow.*;
import org.jahia.services.workflow.jbpm.custom.AbstractTaskLifeCycleEventListener;
import org.jahia.utils.Patterns;
import org.jbpm.runtime.manager.impl.task.SynchronizedTaskService;
import org.jbpm.services.task.events.AfterTaskAddedEvent;
import org.jbpm.services.task.impl.model.GroupImpl;
import org.jbpm.services.task.impl.model.UserImpl;
import org.kie.api.task.model.OrganizationalEntity;
import org.kie.api.task.model.Task;

import javax.enterprise.event.Observes;
import javax.enterprise.event.Reception;
import javax.jcr.RepositoryException;
import javax.jcr.Value;
import javax.jcr.ValueFactory;
import java.util.*;


/**
 * JBPM Task lifecycle event listener
 *
 * @author : rincevent
 * @since JAHIA 6.5
 *        Created : 4 févr. 2010
 */
public class JBPMTaskLifeCycleEventListener extends AbstractTaskLifeCycleEventListener {

    private static final long serialVersionUID = 4434614988996316632L;

    @Override
    public void afterTaskReleasedEvent(Task ti) {

    }

    @Override
    public void afterTaskResumedEvent(Task ti) {

    }

    @Override
    public void afterTaskSuspendedEvent(Task ti) {

    }

    @Override
    public void afterTaskForwardedEvent(Task ti) {

    }

    @Override
    public void afterTaskDelegatedEvent(Task ti) {

    }

    @Override
    public void afterTaskActivatedEvent(Task ti) {

    }

    @Override
    public void afterTaskClaimedEvent(Task ti) {

    }

    @Override
    public void afterTaskSkippedEvent(Task ti) {

    }

    @Override
    public void afterTaskStartedEvent(Task ti) {

    }

    @Override
    public void afterTaskStoppedEvent(Task ti) {

    }

    @Override
    public void afterTaskCompletedEvent(Task ti) {

    }

    @Override
    public void afterTaskFailedEvent(Task ti) {

    }

    @Override
    public void afterTaskAddedEvent(@Observes(notifyObserver = Reception.IF_EXISTS) @AfterTaskAddedEvent Task task) {
        Map<String, Object> taskInputParameters = getTaskInputParameters(task);
        Map<String, Object> taskOutputParameters = getTaskOutputParameters(task, taskInputParameters);
        try {
            final List<JahiaPrincipal> principals = new ArrayList<JahiaPrincipal>();
            for (OrganizationalEntity entity : task.getPeopleAssignments().getPotentialOwners()) {
                if (entity instanceof UserImpl) {
                    principals.add(ServicesRegistry.getInstance().getJahiaUserManagerService().lookupUserByKey(entity.getId()));
                } else if (entity instanceof GroupImpl) {
                    principals.add(ServicesRegistry.getInstance().getJahiaGroupManagerService().lookupGroup(entity.getId()));
                }
            }
            createTask(task, taskInputParameters, taskOutputParameters, principals);
            ((SynchronizedTaskService) taskService).addContent(task.getId(), taskOutputParameters);

            observationManager.notifyNewTask("jBPM", Long.toString(task.getId()));
        } catch (RepositoryException e) {
            throw new RuntimeException("Error while setting up task assignees and creating a JCR task", e);
        }
    }


    @Override
    public void afterTaskExitedEvent(Task ti) {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    protected void createTask(final Task task,
                              final Map<String, Object> taskInputParameters,
                              final Map<String, Object> taskOutputParameters,
                              final List<JahiaPrincipal> candidates) throws RepositoryException {
        final Workflow workflow = workflowProvider.getWorkflow(Long.toString(task.getTaskData().getProcessInstanceId()), null);

        String username = (String) taskInputParameters.get("user");
        if (username == null) {
            username = workflow.getStartUser();
        }
        final JahiaUser user = ServicesRegistry.getInstance().getJahiaUserManagerService().lookupUserByKey(username);

        if (user != null) {
            String workspace = (String) taskInputParameters.get("workspace");
            if (workspace == null) {
                workspace = (String) workflow.getVariables().get("workspace");
            }
            JCRTemplate.getInstance().doExecuteWithSystemSession(user.getUsername(), workspace, null, new JCRCallback<Object>() {
                public Object doInJCR(JCRSessionWrapper session) throws RepositoryException {
                    JCRUser jcrUser;
                    if (user instanceof JCRUser) {
                        jcrUser = (JCRUser) user;
                    } else {
                        jcrUser = ((JCRUserManagerProvider) SpringContextSingleton.getBean("JCRUserManagerProvider")).lookupExternalUser(user);
                    }
                    JCRNodeWrapper n = jcrUser.getNode(session);
                    JCRNodeWrapper tasks;

                    if (!n.hasNode("workflowTasks")) {
                        tasks = n.addNode("workflowTasks", "jnt:tasks");
                    } else {
                        tasks = n.getNode("workflowTasks");
                    }
                    final String taskName = task.getNames().get(0).getText();
                    JCRNodeWrapper jcrTask = tasks.addNode(JCRContentUtils.findAvailableNodeName(tasks, taskName), "jnt:workflowTask");
                    String definitionKey = JBPM6WorkflowProvider.getDecodedProcessKey(task.getTaskData().getProcessId());
                    jcrTask.setProperty("taskName", taskName);
                    String bundle = workflow.getWorkflowDefinition().getPackageName() + "." + Patterns.SPACE.matcher(definitionKey).replaceAll("");
                    jcrTask.setProperty("taskBundle", bundle);
                    jcrTask.setProperty("taskId", task.getId());
                    jcrTask.setProperty("provider", "jBPM");

                    String uuid = (String) taskInputParameters.get("nodeId");
                    if (uuid == null) {
                        uuid = (String) workflow.getVariables().get("nodeId");
                    }
                    if (uuid != null) {
                        jcrTask.setProperty("targetNode", uuid);
                    }

                    if (task.getTaskData().getExpirationTime() != null) {
                        Calendar calendar = Calendar.getInstance();
                        calendar.setTime(task.getTaskData().getExpirationTime());
                        jcrTask.setProperty("dueDate", calendar);
                    }
                    List<Value> candidatesArray = new ArrayList<Value>();
                    ValueFactory valueFactory = session.getValueFactory();
                    for (JahiaPrincipal principal : candidates) {
                        if (principal instanceof JahiaGroup) {
                            candidatesArray.add(valueFactory.createValue("g:" + ((JCRGroup) principal).getGroupKey()));
                        } else if (principal instanceof JahiaUser) {
                            candidatesArray.add(valueFactory.createValue("u:" + principal.getName()));
                        }
                    }
                    jcrTask.setProperty("candidates", candidatesArray.toArray(new Value[candidatesArray.size()]));
                    WorkflowTask wfTask = workflowProvider.getWorkflowTask(Long.toString(task.getId()), null);
                    Set<String> outcomes = wfTask.getOutcomes();
                    List<Value> outcomesArray = new ArrayList<Value>();
                    for (String outcome : outcomes) {
                        outcomesArray.add(valueFactory.createValue(outcome));
                    }
                    jcrTask.setProperty("possibleOutcomes", outcomesArray.toArray(new Value[outcomes.size()]));
                    jcrTask.setProperty("state", "active");
                    jcrTask.setProperty("type", "workflow");
                    jcrTask.setProperty("jcr:title", "##resourceBundle(" +
                            Patterns.SPACE.matcher(taskName).replaceAll(".").trim().toLowerCase() +
                            "," +
                            bundle +
                            ")## : " +
                            session.getNodeByIdentifier(uuid).getDisplayableName());

                    if (taskInputParameters.containsKey("jcr:title") && taskInputParameters.get("jcr:title") instanceof WorkflowVariable) {
                        jcrTask.setProperty("description", ((WorkflowVariable) taskInputParameters.get("jcr:title")).getValue());
                    }
                    String form = WorkflowService.getInstance().getFormForAction(definitionKey, taskName);
                    if (form != null && NodeTypeRegistry.getInstance().hasNodeType(form)) {
                        JCRNodeWrapper data = jcrTask.addNode("taskData", form);
                        ExtendedNodeType type = NodeTypeRegistry.getInstance().getNodeType(form);
                        Map<String, ExtendedPropertyDefinition> m = type.getPropertyDefinitionsAsMap();
                        for (String s : m.keySet()) {
                            Object variable = taskInputParameters.get(s);
                            if (variable instanceof WorkflowVariable) {
                                WorkflowVariable workflowVariable = (WorkflowVariable) variable;
                                data.setProperty(s, workflowVariable.getValue(), workflowVariable.getType());
                            } else if (variable instanceof List) {
                                List list = (List) variable;
                                List<Value> v = new ArrayList<Value>();
                                for (Object o : list) {
                                    if (o instanceof WorkflowVariable) {
                                        WorkflowVariable workflowVariable = (WorkflowVariable) o;
                                        v.add(session.getValueFactory().createValue(workflowVariable.getValue(), workflowVariable.getType()));
                                    }
                                }
                                data.setProperty(s, v.toArray(new Value[v.size()]));
                            }
                        }
                    }

                    session.save();

                    taskOutputParameters.put("task-" + task.getId(), jcrTask.getIdentifier());

                    return null;
                }
            });
        }
    }
}
