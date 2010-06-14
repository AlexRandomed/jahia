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

package org.jahia.ajax.gwt.client.widget.edit.contentengine;

import java.util.HashMap;
import java.util.Map;

import org.jahia.ajax.gwt.client.data.GWTJahiaLanguage;
import org.jahia.ajax.gwt.client.messages.Messages;

/**
 * Represents a dedicated tab for viewing workflow status and history
 * information.
 * 
 * @author Sergiy Shyrkov
 */
public class WorkflowTabItem extends EditEngineTabItem {

    private WorkflowHistoryPanel activePanel;

    private Map<String, WorkflowHistoryPanel> panelsByLanguage;

    /**
     * Initializes an instance of this class.
     * 
     * @param engine reference to the owner
     */
    public WorkflowTabItem(NodeHolder engine) {
        super(Messages.get("label.engineTab.workflow", "Workflow"), engine);
        //setIcon(ContentModelIconProvider.CONTENT_ICONS.workflow());
        panelsByLanguage = new HashMap<String, WorkflowHistoryPanel>(1);
    }

    @Override
    public void create(GWTJahiaLanguage locale) {
        if (engine.getNode() == null) {
            return;
        }

        WorkflowHistoryPanel next = getPanel(locale.getLanguage());
        if (activePanel != null) {
            if (activePanel == next) {
                // same as current --> do nothing
                return;
            }
            activePanel.setVisible(false);
        }
        next.setVisible(true);
        next.layout();
        activePanel = next;

        layout();
    }

    private WorkflowHistoryPanel getPanel(String locale) {
        WorkflowHistoryPanel panel = panelsByLanguage.get(locale);
        if (panel == null) {
            panel = new WorkflowHistoryPanel(engine.getNode().getUUID(), locale);
            panel.setVisible(true);
            panelsByLanguage.put(locale, panel);
            add(panel);
        }
        return panel;
    }

}
