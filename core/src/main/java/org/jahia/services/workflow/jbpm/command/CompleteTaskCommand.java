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

package org.jahia.services.workflow.jbpm.command;

import org.jahia.registries.ServicesRegistry;
import org.jahia.services.content.JCRCallback;
import org.jahia.services.content.JCRSessionWrapper;
import org.jahia.services.content.JCRTemplate;
import org.jahia.services.usermanager.JahiaUser;
import org.jahia.services.workflow.WorkflowObservationManager;
import org.jahia.services.workflow.jbpm.BaseCommand;
import org.jahia.services.workflow.jbpm.JBPM6WorkflowProvider;
import org.kie.api.task.model.Task;
import org.slf4j.Logger;

import javax.jcr.RepositoryException;
import java.util.HashMap;
import java.util.Map;

/**
* Complete a task previously assigned
*/
public class CompleteTaskCommand extends BaseCommand<Object> {
    private static final Logger logger = org.slf4j.LoggerFactory.getLogger(CompleteTaskCommand.class);

    private JBPM6WorkflowProvider jbpm6WorkflowProvider;
    private final String taskId;
    private final String outcome;
    private final Map<String, Object> args;
    private final JahiaUser jahiaUser;
    private final WorkflowObservationManager observationManager;

    public CompleteTaskCommand(String taskId, String outcome, Map<String, Object> args, JahiaUser jahiaUser, WorkflowObservationManager observationManager) {
        this.taskId = taskId;
        this.outcome = outcome;
        this.args = args;
        this.jahiaUser = jahiaUser;
        this.observationManager = observationManager;
    }

    @Override
    public Object execute() {
        long id = Long.parseLong(taskId);
        Task task = getTaskService().getTaskById(id);
        Map<String, Object> taskInputParameters = getTaskInputParameters(task, getKieSession(), getTaskService());
        Map<String, Object> taskOutputParameters = getTaskOutputParameters(task, taskInputParameters, getKieSession(), getTaskService());
        final String uuid = (String) taskOutputParameters.get("task-" + taskId);
        if (uuid != null) {
            String workspace = (String) taskInputParameters.get("workspace");
            try {
                JCRTemplate.getInstance().doExecuteWithSystemSession(null, workspace, new JCRCallback<Object>() {
                    public Object doInJCR(JCRSessionWrapper session) throws RepositoryException {
                        if (!session.getNodeByUUID(uuid).hasProperty("state") ||
                                !session.getNodeByUUID(uuid).getProperty("state").getString().equals("finished")) {
                            session.getNodeByUUID(uuid).setProperty("finalOutcome", outcome);
                            session.getNodeByUUID(uuid).setProperty("state", "finished");
                            session.save();
                        }
                        return null;
                    }
                });
            } catch (RepositoryException e) {
                logger.error(e.getMessage(), e);
            }
        }

        observationManager.notifyTaskEnded(getKey(), taskId);

        ClassLoader l = null;

        try {
            String module = getWorkflowService().getModuleForWorkflow(task.getTaskData().getProcessId());
            if (module != null) {
                l = Thread.currentThread().getContextClassLoader();
                Thread.currentThread().setContextClassLoader(ServicesRegistry.getInstance().getJahiaTemplateManagerService().getTemplatePackageById(module).getChainedClassLoader());
            }
            Map<String, Object> argsMap = args;
            if (argsMap == null) {
                argsMap = new HashMap<String, Object>();
            }
            argsMap.put("outcome", outcome);
            getTaskService().start(id, jahiaUser.getUserKey());
            getTaskService().complete(id, jahiaUser.getUserKey(), argsMap);
        } finally {
            if (l != null) {
                Thread.currentThread().setContextClassLoader(l);
            }
        }
        return null;
    }
}
