/**
 * Jahia Enterprise Edition v6
 *
 * Copyright (C) 2002-2009 Jahia Solutions Group. All rights reserved.
 *
 * Jahia delivers the first Open Source Web Content Integration Software by combining Enterprise Web Content Management
 * with Document Management and Portal features.
 *
 * The Jahia Enterprise Edition is delivered ON AN "AS IS" BASIS, WITHOUT WARRANTY OF ANY KIND, EITHER EXPRESSED OR
 * IMPLIED.
 *
 * Jahia Enterprise Edition must be used in accordance with the terms contained in a separate license agreement between
 * you and Jahia (Jahia Sustainable Enterprise License - JSEL).
 *
 * If you are unsure which license is appropriate for your use, please contact the sales department at sales@jahia.com.
 */
package org.jahia.ajax.gwt.client.widget.node.portlet;

import org.jahia.ajax.gwt.client.util.acleditor.AclEditor;
import org.jahia.ajax.gwt.client.util.nodes.JCRClientUtils;
import org.jahia.ajax.gwt.client.messages.Messages;
import org.jahia.ajax.gwt.client.service.node.JahiaNodeService;
import org.jahia.ajax.gwt.client.data.acl.GWTJahiaNodeACE;
import org.jahia.ajax.gwt.client.data.acl.GWTJahiaNodeACL;
import com.extjs.gxt.ui.client.widget.toolbar.TextToolItem;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Label;
import com.allen_sauer.gwt.log.client.Log;

import java.util.List;
import java.util.ArrayList;

/**
 * User: ktlili
 * Date: 2 d�c. 2008
 * Time: 17:21:22
 */
public class PortletModesCard extends MashupWizardCard {
    private AclEditor modeMappingEditor;


    public PortletModesCard() {
        super(Messages.getNotEmptyResource("mw_modes_permissions", "Modes permissions"));
        setHtmlText(getText());

    }

    public String getText() {
        return Messages.getNotEmptyResource("mw_modes_permissions_description", "Set modes permissions");
    }

    public void next() {
        if (modeMappingEditor != null) {
            getGwtJahiaNewPortletInstance().setModes(modeMappingEditor.getAcl());
        }
    }

    public void createUI() {
        removeAll();
        super.createUI();
        // update
        final GWTJahiaNodeACL acl = getPortletWizardWindow().getGwtJahiaNewPortletInstance().getGwtJahiaPortletDefinition().getBaseAcl();
        List<String> permissions = acl.getAvailablePermissions().get(JCRClientUtils.MODES_ACL);
        if (permissions != null && permissions.size() > 0) {
            JahiaNodeService.App.getInstance().createDefaultUsersGroupACE(permissions, true, new AsyncCallback<GWTJahiaNodeACE>() {
                public void onSuccess(GWTJahiaNodeACE gwtJahiaNodeACE) {
                    Log.debug("Add group ACE");
                    List<GWTJahiaNodeACE> aces = acl.getAce();
                    if (aces == null) {
                        aces = new ArrayList<GWTJahiaNodeACE>();
                    }
                    aces.add(gwtJahiaNodeACE);
                    acl.setAce(aces);
                    initModeMappingEditor();
                    add(modeMappingEditor.renderNewAclPanel());
                }

                public void onFailure(Throwable throwable) {
                    Log.error("Unable to Add group ACE");
                    initModeMappingEditor();
                    add(modeMappingEditor.renderNewAclPanel());
                }
            });
        } else {
            add(new Label(Messages.getNotEmptyResource("mw_only_view_mode", "The selected portlets contains only view mode.")));
        }
    }

    private void initModeMappingEditor() {
        modeMappingEditor = new AclEditor(getPortletWizardWindow().getGwtJahiaNewPortletInstance().getGwtJahiaPortletDefinition().getBaseAcl(), getPortletWizardWindow().getParentNode().getAclContext());
        modeMappingEditor.setAclGroup(JCRClientUtils.MODES_ACL);
        modeMappingEditor.setAddUsersLabel(Messages.getNotEmptyResource("mw_modes_adduser", "Add mode-user permission"));
        modeMappingEditor.setAddGroupsLabel(Messages.getNotEmptyResource("mw_modes_addgroup", "Add mode-group permission"));
        TextToolItem saveButton = modeMappingEditor.getSaveButton();
        saveButton.setVisible(false);


        TextToolItem restoreButton = modeMappingEditor.getRestoreButton();
        restoreButton.setVisible(false);
    }

    @Override
    public void refreshLayout() {
        // do nothing
    }
}
