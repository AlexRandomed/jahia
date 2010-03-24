/**
 *
 * This file is part of Jahia: An integrated WCM, DMS and Portal Solution
 * Copyright (C) 2002-2009 Jahia Limited. All rights reserved.
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
 * in Jahia's FLOSS exception. You should have recieved a copy of the text
 * describing the FLOSS exception, and it is also available here:
 * http://www.jahia.com/license"
 *
 * Commercial and Supported Versions of the program
 * Alternatively, commercial and supported versions of the program may be used
 * in accordance with the terms contained in a separate written agreement
 * between you and Jahia Limited. If you are unsure which license is appropriate
 * for your use, please contact the sales department at sales@jahia.com.
 */
package org.jahia.ajax.gwt.client.widget.edit;


import com.allen_sauer.gwt.log.client.Log;
import org.jahia.ajax.gwt.client.data.definition.GWTJahiaNodeType;
import org.jahia.ajax.gwt.client.data.node.GWTJahiaNode;
import org.jahia.ajax.gwt.client.widget.edit.mainarea.MainModule;
import org.jahia.ajax.gwt.client.widget.edit.mainarea.Module;
import org.jahia.ajax.gwt.client.widget.edit.sidepanel.SidePanel;
import org.jahia.ajax.gwt.client.widget.toolbar.ActionToolbarLayoutContainer;
import org.jahia.ajax.gwt.client.widget.Linker;
import org.jahia.ajax.gwt.client.widget.LinkerSelectionContext;

import java.util.List;
import java.util.ArrayList;


/**
 * Created by IntelliJ IDEA.
 *
 * @author : rincevent
 * @since : JAHIA 6.1
 *        Created : 24 août 2009
 */
public class EditLinker implements Linker {
    public static int REFRESH_PAGES = 1;

    private GWTJahiaNode sidePanelSelectedNode;
    private LinkerSelectionContext selectionContext = new LinkerSelectionContext();
    private Module selectedModule;
    private Module previouslySelectedModule;
    private EditModeDNDListener dndListener;
    private ActionToolbarLayoutContainer toolbar;
    private MainModule mainModule;
    private SidePanel sidePanel;


    private String locale;

    public EditLinker(MainModule mainModule, SidePanel sidePanel, ActionToolbarLayoutContainer toolbar) {
        this.dndListener = new EditModeDNDListener(this);
        this.mainModule = mainModule;
        this.sidePanel = sidePanel;
        this.toolbar = toolbar;

        registerLinker();
    }


    public ActionToolbarLayoutContainer getToolbar() {
        return toolbar;
    }

    public SidePanel getSidePanel() {
        return sidePanel;
    }

    public MainModule getMainModule() {
        return mainModule;
    }

    public EditModeDNDListener getDndListener() {
        return dndListener;
    }

    public String getLocale() {
        return locale;
    }

    public void setLocale(String locale) {
        this.locale = locale;
    }

    public Module getSelectedModule() {
        return selectedModule;
    }

    public GWTJahiaNode getSidePanelSelectedNode() {
        return sidePanelSelectedNode;
    }

    public void onDisplayGridSelection(GWTJahiaNode node) {
        sidePanelSelectedNode = node;
        handleNewSidePanelSelection();
    }

    public void onBrowseTreeSelection(GWTJahiaNode node) {
        sidePanelSelectedNode = node;
        handleNewSidePanelSelection();
    }

    public void onCreateGridSelection(GWTJahiaNodeType selected) {
        sidePanelSelectedNode = null;
        handleNewSidePanelSelection();
    }

    public void onModuleSelection(Module selection) {
        selectedModule = selection;
//        if (selectedModule != null) {
//            selection.setDraggable(false);
//            if (previouslySelectedModule != null) {
//                final String path = previouslySelectedModule.getPath();
//                final String s = selectedModule.getPath();
//                if (!path.equals(s) && path.contains(s)) {
//                    previouslySelectedModule = null;
//                    return;
//                }
//            }
//        }

        handleNewModuleSelection();
        if (selectedModule != null) {
            selectedModule.setDraggable(true);
        }
    }

    public void refresh() {
        refresh(Linker.REFRESH_ALL);
    }

    public void refresh(int flag) {
        mainModule.refresh();
        syncSelectionContext();
        toolbar.handleNewLinkerSelection();
        refreshSidePanel(flag);
    }

    private void refreshSidePanel(int flag) {
        if (flag == REFRESH_PAGES) {
            sidePanel.refreshPageTabItem();
        } else {
            sidePanel.refresh();
        }
    }

    public GWTJahiaNode getSelectedNode() {
        if (getSelectedModule() != null) {
            return getSelectedModule().getNode();
        }
        return null;
    }

    public void handleNewModuleSelection() {
        previouslySelectedModule = selectedModule;
        syncSelectionContext();
        toolbar.handleNewLinkerSelection();
        mainModule.handleNewModuleSelection(selectedModule);
        sidePanel.handleNewModuleSelection(selectedModule);
    }

    public void handleNewSidePanelSelection() {
        syncSelectionContext();
        toolbar.handleNewLinkerSelection();
        mainModule.handleNewSidePanelSelection(sidePanelSelectedNode);
        sidePanel.handleNewSidePanelSelection(sidePanelSelectedNode);
    }

    /**
     * Set up linker (callback for each member).
     */
    protected void registerLinker() {
        if (mainModule != null) {
            try {
                mainModule.initWithLinker(this);
            } catch (Exception e) {
                Log.error("Error on init linker",e);
            }
        }
        if (sidePanel != null) {
            try {
                sidePanel.initWithLinker(this);
            } catch (Exception e) {
                Log.error("Error on init linker",e);
            }
        }
        if (toolbar != null) {
            try {
                toolbar.initWithLinker(this);
            } catch (Exception e) {
                Log.error("Error on init linker",e);
            }
        }
    }


    public void select(Object o) {
        if (o == null || o instanceof Module) {
            onModuleSelection((Module) o);
        }
    }

    public void refreshMainComponent() {
        getMainModule().refresh();
    }

    public GWTJahiaNode getMainNode() {
        return getMainModule().getNode();
    }

    public List<GWTJahiaNode> getSelectedNodes() {
        List<GWTJahiaNode> nodes = new ArrayList<GWTJahiaNode>();
        if (getSelectedNode() != null) {
            nodes.add(getSelectedNode());
        }
        return nodes;
    }

    public void loaded() {
        // todo:implements 
    }

    public void loading(String resource) {
        // todo:implements
    }

    public void setSelectPathAfterDataUpdate(String path) {
        // todo:implements
    }

    public LinkerSelectionContext getSelectionContext() {
        return selectionContext;
    }

    public void refreshLeftPanel() {
        sidePanel.refresh();
    }

    public void refreshLeftPanel(int flag) {
        refreshSidePanel(flag);
    }

    public void syncSelectionContext() {
        selectionContext.setMainNode(getMainNode());
        selectionContext.setSelectedNodes(getSelectedNodes());
        selectionContext.refresh();
    }
}
