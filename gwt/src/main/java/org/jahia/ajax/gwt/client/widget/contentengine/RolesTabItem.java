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

package org.jahia.ajax.gwt.client.widget.contentengine;

import com.extjs.gxt.ui.client.widget.TabItem;
import com.extjs.gxt.ui.client.widget.TabPanel;
import com.extjs.gxt.ui.client.widget.layout.FitLayout;
import org.jahia.ajax.gwt.client.core.JahiaGWTParameters;
import org.jahia.ajax.gwt.client.data.acl.GWTJahiaNodeACE;
import org.jahia.ajax.gwt.client.data.acl.GWTJahiaNodeACL;
import org.jahia.ajax.gwt.client.data.definition.GWTJahiaNodeProperty;
import org.jahia.ajax.gwt.client.data.node.GWTJahiaNode;
import org.jahia.ajax.gwt.client.data.toolbar.GWTEngineTab;
import org.jahia.ajax.gwt.client.messages.Messages;
import org.jahia.ajax.gwt.client.util.acleditor.AclEditor;
import org.jahia.ajax.gwt.client.util.security.PermissionsUtils;
import org.jahia.ajax.gwt.client.widget.AsyncTabItem;

import java.util.*;

/**
 *
 * User: toto
 * Date: Jan 6, 2010
 * Time: 7:30:49 PM
 *
 */
public class RolesTabItem extends EditEngineTabItem {
    private static List<AclEditor> rolesEditors = new ArrayList<AclEditor>();

    private transient AclEditor rolesEditor;

    private Set<String> roles;
    private Set<String> roleGroups;

    private boolean canBreakInheritance = false;

    private String autoAddRole;

    @Override
    public AsyncTabItem create(GWTEngineTab engineTab, NodeHolder engine) {
        rolesEditors.clear();
        return super.create(engineTab, engine);
    }

    @Override
    public void init(NodeHolder engine, AsyncTabItem tab, String locale) {
        if (engine.getAcl() != null) {
            tab.setProcessed(true);

            GWTJahiaNode node;
            if (engine.getNode() != null) {
                node = engine.getNode();
            } else {
                node = engine.getTargetNode();
            }

            rolesEditor = new AclEditor(engine.getAcl(), node.getAclContext(), roles, roleGroups, rolesEditors);
            rolesEditor.setCanBreakInheritance(canBreakInheritance);
            rolesEditor.setAutoAddRole(autoAddRole);

            if (!(node.getProviderKey().equals("default") || node.getProviderKey().equals("jahia"))) {
                rolesEditor.setReadOnly(true);
            } else {
                rolesEditor.setReadOnly(!PermissionsUtils.isPermitted("jcr:modifyAccessControl", node) || node.isLocked());
            }

            tab.setLayout(new FitLayout());
            rolesEditor.addNewAclPanel(tab);
        }
    }

    @Override
    public void setProcessed(boolean processed) {
        if (!processed) {
            rolesEditors.remove(rolesEditor);
            rolesEditor = null;
        }
    }

    public Set<String> getRoles() {
        return roles;
    }

    public void setRoles(Set<String> roles) {
        this.roles = roles;
    }

    public Set<String> getRoleGroups() {
        return roleGroups;
    }

    public void setRoleGroups(Set<String> roleGroups) {
        this.roleGroups = roleGroups;
    }

    public boolean isCanBreakInheritance() {
        return canBreakInheritance;
    }

    public void setCanBreakInheritance(boolean canBreakInheritance) {
        this.canBreakInheritance = canBreakInheritance;
    }

    public String getAutoAddRole() {
        return autoAddRole;
    }

    public void setAutoAddRole(String autoAddRole) {
        this.autoAddRole = autoAddRole;
    }

    @Override
    public void doValidate(List<EngineValidation.ValidateResult> validateResult, NodeHolder engine, TabItem tab, String selectedLanguage, Map<String, List<GWTJahiaNodeProperty>> changedI18NProperties, TabPanel tabs) {
        if (canBreakInheritance && rolesEditor != null && rolesEditor.getBreakAllInheritance()) {
            for (GWTJahiaNodeACE ace : rolesEditor.getAcl().getAce()) {
                if (!ace.getRoles().isEmpty()) {
                    for (Boolean granted : ace.getRoles().values()) {
                        if (granted) {
                            return;
                        }
                    }
                }
            }
            EngineValidation.ValidateResult result = new EngineValidation.ValidateResult();
            result.errorTab = tab;
            result.message = Messages.get("label.breakInheritanceWarning", "You are going to break acl inheritance, and you did not assign any role to users. You may loose access to this content.");
            validateResult.add(result);
        }
    }

    @Override
    public void doSave(GWTJahiaNode node, List<GWTJahiaNodeProperty> changedProperties, Map<String, List<GWTJahiaNodeProperty>> changedI18NProperties, Set<String> addedTypes, Set<String> removedTypes, List<GWTJahiaNode> chidren, GWTJahiaNodeACL acl) {
        if (rolesEditor == null) {
            return;
        }
        Map<String, GWTJahiaNodeACE> aceMap = new HashMap<String, GWTJahiaNodeACE>();

        for (GWTJahiaNodeACE ace : acl.getAce()) {
            aceMap.put(ace.getPrincipalType() + ace.getPrincipalKey(), ace);
        }

        if (acl.getAvailableRoles() == null)  {
            acl.setAvailableRoles(new HashMap<String, List<String>>());
        }

        GWTJahiaNodeACL modifiedAcl  = rolesEditor.getAcl();
        acl.setBreakAllInheritance(modifiedAcl.isBreakAllInheritance());

        for (GWTJahiaNodeACE modifiedAce : modifiedAcl.getAce()) {
            if (!aceMap.containsKey(modifiedAce.getPrincipalType() + modifiedAce.getPrincipalKey())) {
                aceMap.put(modifiedAce.getPrincipalType() + modifiedAce.getPrincipalKey(), modifiedAce);
                acl.getAce().add(modifiedAce);
            } else {
                GWTJahiaNodeACE mergedAce = aceMap.get(modifiedAce.getPrincipalType() + modifiedAce.getPrincipalKey());
                mergedAce.getRoles().keySet().removeAll(rolesEditor.getDisplayedRoles());
                mergedAce.getRoles().putAll(modifiedAce.getRoles());
                mergedAce.getInheritedRoles().keySet().removeAll(rolesEditor.getDisplayedRoles());
                mergedAce.getInheritedRoles().putAll(modifiedAce.getInheritedRoles());
            }
        }
    }
}
