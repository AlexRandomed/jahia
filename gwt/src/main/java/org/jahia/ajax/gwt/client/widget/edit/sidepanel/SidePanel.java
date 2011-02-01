/**
 * This file is part of Jahia: An integrated WCM, DMS and Portal Solution
 * Copyright (C) 2002-2011 Jahia Solutions Group SA. All rights reserved.
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

package org.jahia.ajax.gwt.client.widget.edit.sidepanel;

import com.extjs.gxt.ui.client.event.IconButtonEvent;
import com.extjs.gxt.ui.client.event.SelectionListener;
import com.extjs.gxt.ui.client.widget.ContentPanel;
import com.extjs.gxt.ui.client.widget.LayoutContainer;
import com.extjs.gxt.ui.client.widget.TabItem;
import com.extjs.gxt.ui.client.widget.TabPanel;
import com.extjs.gxt.ui.client.widget.button.ToolButton;
import com.extjs.gxt.ui.client.widget.layout.FitLayout;
import com.extjs.gxt.ui.client.widget.layout.FlowLayout;
import com.extjs.gxt.ui.client.widget.toolbar.SeparatorToolItem;
import org.jahia.ajax.gwt.client.data.node.GWTJahiaNode;
import org.jahia.ajax.gwt.client.data.toolbar.GWTEditConfiguration;
import org.jahia.ajax.gwt.client.data.toolbar.GWTJahiaToolbarItem;
import org.jahia.ajax.gwt.client.widget.edit.EditLinker;
import org.jahia.ajax.gwt.client.data.toolbar.GWTSidePanelTab;
import org.jahia.ajax.gwt.client.widget.edit.ToolbarHeader;
import org.jahia.ajax.gwt.client.widget.edit.mainarea.Module;
import org.jahia.ajax.gwt.client.widget.toolbar.action.ActionItem;
import org.jahia.ajax.gwt.client.widget.toolbar.action.SiteSwitcherActionItem;

import java.util.ArrayList;
import java.util.List;

/**
 * Side panel widget that allows creation of new content using drag and drop from different sources
 * (new content panel, file repository, image repository, page tree, portlets, etc.).
 * User: toto
 * Date: Dec 18, 2009
 * Time: 5:27:33 PM
 */
public class SidePanel extends ContentPanel {
    private final List<SidePanelTabItem> tabs;
    private SidePanelTabItem templatesTabItem;
    private GWTEditConfiguration config;

    public SidePanel(GWTEditConfiguration config) {
        super(new FitLayout());
        this.head = new ToolbarHeader();

        this.config = config;

        tabs = new ArrayList<SidePanelTabItem>();

        TabPanel tabPanel = new TabPanel();
        tabPanel.setBorders(false);
        tabPanel.setBodyBorder(false);
        
        for (GWTSidePanelTab tabConfig : config.getTabs()) {
            SidePanelTabItem tabItem = tabConfig.getTabItem();
            tabs.add(tabItem);
            if (tabItem instanceof TemplatesTabItem) {
                templatesTabItem = tabItem;
            }

            tabPanel.add(tabItem.create(tabConfig));
        }

        add(tabPanel);
    }

    public void initWithLinker(EditLinker editLinker) {
        for (GWTJahiaToolbarItem item : config.getSidePanelToolbar().getGwtToolbarItems()) {
            ((ToolbarHeader)head).addItem(editLinker, item);
        }

        head.addTool(new ToolButton("x-tool-refresh", new SelectionListener<IconButtonEvent>() {
            public void componentSelected(IconButtonEvent event) {
                refresh(EditLinker.REFRESH_ALL);
            }
        }));

        for (SidePanelTabItem tab : tabs) {
            tab.initWithLinker(editLinker);
        }
    }

    public void handleNewModuleSelection(Module selectedModule) {
        for (SidePanelTabItem tab : tabs) {
            tab.handleNewModuleSelection(selectedModule);
        }
        ((ToolbarHeader)head).handleNewModuleSelection(selectedModule);
    }

    public void handleNewMainNodeLoaded(GWTJahiaNode node) {
        for (SidePanelTabItem tab : tabs) {
            tab.handleNewMainNodeLoaded(node);
        }
        ((ToolbarHeader)head).handleNewMainNodeLoaded(node);
    }

    public void handleNewMainSelection(String path) {
        for (SidePanelTabItem tab : tabs) {
            tab.handleNewMainSelection(path);
        }
    }

    public void refresh(int flag) {
        for (SidePanelTabItem tab : tabs) {
            tab.refresh(flag);
        }
    }
}
