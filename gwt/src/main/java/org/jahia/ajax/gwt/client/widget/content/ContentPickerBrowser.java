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
package org.jahia.ajax.gwt.client.widget.content;

import com.extjs.gxt.ui.client.widget.*;
import com.extjs.gxt.ui.client.widget.layout.*;
import org.jahia.ajax.gwt.client.data.toolbar.GWTManagerConfiguration;
import org.jahia.ajax.gwt.client.data.node.GWTJahiaNode;
import org.jahia.ajax.gwt.client.messages.Messages;
import org.jahia.ajax.gwt.client.widget.tripanel.TopRightComponent;

import java.util.List;
import java.util.ArrayList;

/**
 * User: rfelden
 * Date: 21 oct. 2008 - 16:47:12
 */
public class ContentPickerBrowser extends TopRightComponent {

    private GWTManagerConfiguration config;
    private String pickerType;
    private ContentPanel m_component;
    private List<ContentTreeGrid> contentTreeGrids = new ArrayList<ContentTreeGrid>();
    private SearchGrid m_search;
    private TabPanel tabs;
    private TabItem searchTabItem;
    private GWTJahiaNode lastSelection;

    public ContentPickerBrowser(String pickerType, String rootPath, List<GWTJahiaNode> selectedNodes, GWTManagerConfiguration config, boolean multiple) {
        this.pickerType = pickerType;
        this.config = config;


        for (String repoId : config.getAccordionPanels()) {
            ContentTreeGrid treeGrid = new ContentTreeGrid(repoId, selectedNodes, multiple, config) {
                @Override
                public void onContentPicked(GWTJahiaNode gwtJahiaNode) {
                    super.onContentPicked(gwtJahiaNode);
                    pickContent(gwtJahiaNode);
                }
            };
            contentTreeGrids.add(treeGrid);
        }


        m_search = new SearchGrid(config, multiple) {
            @Override
            public void onContentPicked(GWTJahiaNode gwtJahiaNode) {
                super.onContentPicked(gwtJahiaNode);
                pickContent(gwtJahiaNode);
            }
        };

        m_component = new ContentPanel(new FitLayout());
        m_component.setBodyBorder(false);
        m_component.setBorders(false);
        m_component.setHeaderVisible(false);


        tabs = new TabPanel();
        tabs.setHeight(400);
        tabs.setBodyBorder(false);
        tabs.setBorders(false);

        for (ContentTreeGrid treeGrid : contentTreeGrids) {
            TabItem treeTable = new TabItem(Messages.getResource("fm_repository_" + treeGrid.getRepoType()));
            treeTable.setBorders(false);
            treeTable.setLayout(new FitLayout());
            treeTable.add(treeGrid);
            tabs.add(treeTable);
        }

        searchTabItem = new TabItem(Messages.getResource("fm_search"));
        searchTabItem.setBorders(false);


        searchTabItem.setLayout(new FitLayout());
        searchTabItem.add(m_search);
        tabs.add(searchTabItem);

        m_component.add(tabs);
    }


    /**
     * Handle new Selection
     */
    public void handleNewSelection() {
        // TODo: getSelection should alwas return a list of GWTJahiaNode
    }


    /**
     * Handle new selction
     *
     * @param selection
     */
    public void handleNewSelection(GWTJahiaNode selection) {
        lastSelection = selection;

    }

    /**
     * Get selected node
     *
     * @return
     */
    public List<GWTJahiaNode> getSelectedNodes() {
        if (lastSelection == null) {
            return new ArrayList<GWTJahiaNode>();
        }
        List<GWTJahiaNode> l = new ArrayList<GWTJahiaNode>();
        l.add(lastSelection);
        return l;
    }


    /**
     * init contextMenu
     */
    public void initContextMenu() {
    }

    /**
     * Set selected content
     *
     * @param root
     */
    public void setContent(Object root) {
        lastSelection = (GWTJahiaNode) root;
    }

    /**
     * Pick content
     *
     * @param root
     */
    public void pickContent(Object root) {
        lastSelection = (GWTJahiaNode) root;
        getLinker().onTableItemSelected();
    }

    /**
     * Clear
     */
    public void clearTable() {

        m_search.clearTable();
    }

    /**
     * Get selection
     *
     * @return
     */
    public Object getSelection() {
        return getSelectedNodes();
    }

    /**
     * Refresh
     */
    public void refresh() {
        m_search.refresh();
    }

    /**
     * Get main component
     *
     * @return
     */
    public Component getComponent() {
        return m_component;
    }


    /**
     * Clear selection
     */
    public void clearSelection() {
        lastSelection = null;
    }
}
