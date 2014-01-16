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

package org.jahia.ajax.gwt.client.widget.toolbar.action;

import org.jahia.ajax.gwt.client.core.BaseAsyncCallback;
import org.jahia.ajax.gwt.client.data.definition.GWTJahiaNodeType;
import org.jahia.ajax.gwt.client.data.node.GWTJahiaNode;
import org.jahia.ajax.gwt.client.service.content.JahiaContentManagementService;
import org.jahia.ajax.gwt.client.service.content.JahiaContentManagementServiceAsync;
import org.jahia.ajax.gwt.client.util.URL;
import org.jahia.ajax.gwt.client.util.security.PermissionsUtils;
import org.jahia.ajax.gwt.client.widget.LinkerSelectionContext;
import org.jahia.ajax.gwt.client.widget.contentengine.EngineLoader;
import org.jahia.ajax.gwt.client.widget.edit.mainarea.ModuleHelper;

import java.util.Arrays;
import java.util.List;

/**
 * 
* User: toto
* Date: Sep 25, 2009
* Time: 6:59:03 PM
*/
public class EditContentActionItem extends NodeTypeAwareBaseActionItem {
    
    private static final long serialVersionUID = 1899385924986263120L;
    
    private boolean allowRootNodeEditing;
    private boolean useMainNode = false;
    private String path = null;

    public void onComponentSelection() {
        GWTJahiaNode singleSelection;
        if (path != null) {
            String replacedPath = URL.replacePlaceholders(path, linker.getSelectionContext().getSingleSelection());
            JahiaContentManagementService.App.getInstance().getNodes(Arrays.asList(replacedPath), null, new BaseAsyncCallback<List<GWTJahiaNode>>() {
                public void onSuccess(List<GWTJahiaNode> result) {
                    EngineLoader.showEditEngine(linker, result.get(0));
                }
            });
            return;
        } else if (useMainNode) {
            singleSelection = linker.getSelectionContext().getMainNode();
        }   else {
            singleSelection = linker.getSelectionContext().getSingleSelection();
        }
        EngineLoader.showEditEngine(linker, singleSelection);
    }

    public void handleNewLinkerSelection() {
        LinkerSelectionContext lh = linker.getSelectionContext();
        GWTJahiaNode singleSelection = lh.getSingleSelection();
        setEnabled(singleSelection != null
                && isNodeTypeAllowed(singleSelection)
                && hasPermission(lh.getSelectionPermissions())
                && (allowRootNodeEditing || !lh.isRootNode())
                && PermissionsUtils.isPermitted("jcr:modifyProperties", lh.getSelectionPermissions()));
    }

    public void setAllowRootNodeEditing(boolean allowRootNodeEditing) {
        this.allowRootNodeEditing = allowRootNodeEditing;
    }

    public void setUseMainNode(boolean useMainNode) {
        this.useMainNode = useMainNode;
    }

    public void setPath(String path) {
        this.path = path;
    }

    @Override
    protected boolean isNodeTypeAllowed(GWTJahiaNode selectedNode) {
        GWTJahiaNodeType nodeType = ModuleHelper.getNodeType(selectedNode.getNodeTypes().get(0));
        if (nodeType != null) {
            Boolean canUseComponentForCreate = (Boolean) nodeType.get("canUseComponentForEdit");
            if (canUseComponentForCreate != null && !canUseComponentForCreate) {
                return false;
            }
        }

        return super.isNodeTypeAllowed(selectedNode);
    }
}
