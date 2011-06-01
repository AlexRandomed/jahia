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

package org.jahia.bin;

import org.apache.commons.fileupload.disk.DiskFileItem;
import org.apache.commons.lang.StringUtils;
import org.jahia.services.content.*;
import org.jahia.services.logging.MetricsLoggingService;
import org.jahia.services.render.RenderContext;
import org.jahia.services.render.Resource;
import org.jahia.services.render.URLResolver;
import org.jahia.services.usermanager.JahiaUserManagerService;
import org.jahia.tools.files.FileUpload;
import org.json.JSONObject;

import javax.jcr.PathNotFoundException;
import javax.jcr.RepositoryException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.util.*;

/**
 * Performs the default action when data is posted to the render servlet, i.e. creates or modifies a node.
 *
 * @author rincevent
 * @since JAHIA 6.5
 * Created : 11 mars 2010
 */
public class DefaultPostAction extends Action {

    public static final String ACTION_NAME = "default";

    private MetricsLoggingService loggingService;

    public DefaultPostAction() {
        setName(ACTION_NAME);
    }

    public MetricsLoggingService getLoggingService() {
        return loggingService;
    }

    public void setLoggingService(MetricsLoggingService loggingService) {
        this.loggingService = loggingService;
    }

    public ActionResult doExecute(HttpServletRequest req, RenderContext renderContext, Resource resource,
                                  final JCRSessionWrapper session, final Map<String, List<String>> parameters, URLResolver urlResolver) throws Exception {
        JCRNodeWrapper newNode = null;
        String[] subPaths = urlResolver.getPath().split("/");
        String lastPath = subPaths[subPaths.length - 1];
        JCRNodeWrapper node = null;
        StringBuffer realPath = new StringBuffer();
        for (String subPath : subPaths) {
            if (StringUtils.isNotBlank(subPath) && !"*".equals(subPath) && !subPath.equals(lastPath)) {
                realPath.append("/").append(subPath);
                try {
                    node = session.getNode(realPath.toString());
                } catch (PathNotFoundException e) {
                    if (node != null) {
                        if (!node.isCheckedOut()) {
                            node.checkout();
                        }
                         String parentType = "jnt:contentList";
                        if (parameters.containsKey(Render.PARENT_TYPE)) {
                            parentType = parameters.get(Render.PARENT_TYPE).get(0);
                        }
                        node = node.addNode(subPath, parentType);
                    }
                }
            }
        }
        if (node != null) {
            String nodeType = null;
            if (parameters.containsKey(Render.NODE_TYPE)) {
                nodeType = (String) ((List) parameters.get(Render.NODE_TYPE)).get(0);
            }
            if (StringUtils.isBlank(nodeType)) {
//                resp.sendError(HttpServletResponse.SC_BAD_REQUEST, "Missing nodeType Property");
                return ActionResult.BAD_REQUEST;
            }
            String nodeName = null;
            if (parameters.containsKey(Render.NODE_NAME)) {
                nodeName = (String) ((List) parameters.get(Render.NODE_NAME)).get(0);
            }
            boolean forceCreation = false;
            if (!"*".equals(lastPath)) {
                nodeName = lastPath;
            } else {
                forceCreation = true;
            }
            newNode = createNode(req, parameters, node, nodeType, nodeName, forceCreation);
            final FileUpload fileUpload = (FileUpload) req.getAttribute(FileUpload.FILEUPLOAD_ATTRIBUTE);
            if (fileUpload != null && fileUpload.getFileItems() != null && fileUpload.getFileItems().size() > 0) {
                final Map<String, DiskFileItem> stringDiskFileItemMap = fileUpload.getFileItems();
                for (Map.Entry<String, DiskFileItem> itemEntry : stringDiskFileItemMap.entrySet()) {
                    newNode.uploadFile(itemEntry.getValue().getName(), itemEntry.getValue().getInputStream(),
                            itemEntry.getValue().getContentType());
                }
            }
            session.save();

            final String nodeId = newNode.getIdentifier();
            if (parameters.containsKey(Render.AUTO_ASSIGN_ROLE) && JahiaUserManagerService.isNotGuest(session.getUser())) {
                JCRTemplate.getInstance().doExecuteWithSystemSession(session.getUser().getUsername(),session.getWorkspace().getName(),new JCRCallback<Object>() {
                    public Object doInJCR(JCRSessionWrapper rootSession) throws RepositoryException {
                        JCRNodeWrapper createdNode = rootSession.getNodeByUUID(nodeId);
                        List<String> roles = parameters.get(Render.AUTO_ASSIGN_ROLE);
                        createdNode.grantRoles("u:"+session.getUser().getUsername(), new HashSet<String>(roles));
                        rootSession.save();
                        return null;
                    }
                });
            }


            if (parameters.containsKey(Render.AUTO_CHECKIN) && ((String) ((List) parameters.get(Render.AUTO_CHECKIN)).get(
                    0)).length() > 0) {
                newNode.checkpoint();
            }
        }

        String sessionID = "";
        HttpSession httpSession = req.getSession(false);
        if (httpSession != null) {
            sessionID = httpSession.getId();
        }
        String nodeIdentifier = null;
        if (newNode != null) {
            nodeIdentifier = newNode.getIdentifier();
        }
        loggingService.logContentEvent(renderContext.getUser().getName(), req
                .getRemoteAddr(), sessionID, nodeIdentifier, urlResolver.getPath(), (String) ((List) parameters.get(Render.NODE_TYPE)).get(0), "nodeCreated", new JSONObject(parameters).toString());

        if (newNode != null) {
            return new ActionResult(HttpServletResponse.SC_CREATED, newNode.getPath(), Render.serializeNodeToJSON(newNode));
        } else {
            return null;
        }
    }

}
