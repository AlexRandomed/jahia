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
 * in Jahia's FLOSS exception. You should have received a copy of the text
 * describing the FLOSS exception, and it is also available here:
 * http://www.jahia.com/license
 *
 * Commercial and Supported Versions of the program
 * Alternatively, commercial and supported versions of the program may be used
 * in accordance with the terms contained in a separate written agreement
 * between you and Jahia Limited. If you are unsure which license is appropriate
 * for your use, please contact the sales department at sales@jahia.com.
 */

package org.jahia.ajax.gwt.templates.components.layoutmanager.client.ui.gxt.portlet;

import org.jahia.ajax.gwt.templates.components.layoutmanager.client.bean.GWTLayoutItem;
import org.jahia.ajax.gwt.templates.components.layoutmanager.client.ui.gxt.listener.OnPortletRemoved;
import org.jahia.ajax.gwt.templates.components.layoutmanager.client.ui.gxt.listener.OnPortletStatusChanged;
import org.jahia.ajax.gwt.templates.components.layoutmanager.client.ui.gxt.JahiaPortalManager;
import org.jahia.ajax.gwt.templates.components.layoutmanager.client.util.JahiaPropertyHelper;

import com.extjs.gxt.ui.client.Events;
import com.extjs.gxt.ui.client.event.ComponentEvent;
import com.extjs.gxt.ui.client.event.SelectionListener;
import com.extjs.gxt.ui.client.widget.Layout;
import com.extjs.gxt.ui.client.widget.LayoutContainer;
import com.extjs.gxt.ui.client.widget.button.ToolButton;
import com.extjs.gxt.ui.client.widget.custom.Portlet;
import com.google.gwt.user.client.ui.Widget;


/**
 * User: ktlili
 * Date: 28 ao�t 2008
 * Time: 17:11:29
 */
public class JahiaPortlet extends Portlet {
    private GWTLayoutItem porletConfig = new GWTLayoutItem();
    private LayoutContainer column;
    protected Widget viewContainer = null;
    protected Widget editContainer = null;
    protected Widget helpContainer = null;

    protected static int MODE_VIEW = 0;
    protected static int MODE_EDIT = 1;
    protected static int MODE_HELP = 2;
    private int mode = 0;
    private ToolButton viewEditButton = new ToolButton("x-tool-gear");
    private ToolButton helpButton = new ToolButton("x-tool-plus");
    private ToolButton maxButton = new ToolButton("x-tool-maximize");
    private ToolButton minButton = new ToolButton("x-tool-minimize");
    private ToolButton removeButton = new ToolButton("x-tool-close");

    public JahiaPortlet() {
        configPanel();
    }

    public JahiaPortlet(GWTLayoutItem porletConfig) {
        this.porletConfig = porletConfig;
        configPanel();
    }

    public JahiaPortlet(Layout layout) {
        super(layout);
        configPanel();
    }

    public JahiaPortlet(Layout layout, GWTLayoutItem porletConfig) {
        super(layout);
        this.porletConfig = porletConfig;
        configPanel();
    }

    public void refreshStatus() {
        // no yet implemented
        if (JahiaPropertyHelper.isStatusFullScreen(getStatus())) {
            maxButton.setVisible(false);
            minButton.setVisible(true);
            expand();
            JahiaPortalManager.getInstance().switchToFullScreenView(this);
        }
    }

    public boolean isInstanciable() {
        return true;
    }

    public void doView() {
        mode = MODE_VIEW;
        // to implement
        removeAll();
        layout();
    }

    public void doEdit() {
        mode = MODE_EDIT;
        // to implement
        removeAll();
        layout();
    }

    public void doHelp() {
        mode = MODE_HELP;
        // to implement
        removeAll();
        layout();
    }

    protected boolean isViewMode() {
        return mode == MODE_VIEW;
    }

    protected boolean isEditMode() {
        return mode == MODE_EDIT;
    }

    protected boolean isHelpMode() {
        return mode == MODE_HELP;
    }


    /**
     * Add hide button depends on the tpye of the portlet
     */
    private void configPanel() {
        setStyleAttribute("padding", "5px 0px 0px 5px");
        setCollapsible(true);
        setAnimCollapse(false);


        // change mode
        viewEditButton.addSelectionListener(new SelectionListener<ComponentEvent>() {
            @Override
            public void componentSelected(ComponentEvent ce) {
                if (isViewMode()) {
                    doEdit();
                } else if (isEditMode()) {
                    doView();
                } else {
                    doView();
                }
            }

        });
        /*getHeader().addTool(viewEditButton);*/

        // help mpde

        helpButton.addSelectionListener(new SelectionListener<ComponentEvent>() {
            @Override
            public void componentSelected(ComponentEvent ce) {
                doHelp();
            }

        });

        // help mpde

        maxButton.addSelectionListener(new SelectionListener<ComponentEvent>() {
            @Override
            public void componentSelected(ComponentEvent ce) {
                OnPortletStatusChanged onPortletStatusChanged = new OnPortletStatusChanged(JahiaPortlet.this, JahiaPropertyHelper.getStatusFullscreenValue());
                onPortletStatusChanged.handleEvent(ce);
            }

        });
        getHeader().addTool(maxButton);
        // minimizebutton
        minButton.addSelectionListener(new SelectionListener<ComponentEvent>() {
            @Override
            public void componentSelected(ComponentEvent ce) {
                OnPortletStatusChanged onPortletStatusChanged = new OnPortletStatusChanged(JahiaPortlet.this, JahiaPropertyHelper.getStatusNormaleValue());
                onPortletStatusChanged.handleEvent(ce);
            }

        });
        getHeader().addTool(minButton);

        // close portlet
        removeButton.addSelectionListener(new OnPortletRemoved(this));
        getHeader().addTool(removeButton);

        if (JahiaPropertyHelper.isStatusFullScreen(getStatus())) {
            maxButton.setVisible(false);
            minButton.setVisible(true);
        } else if (JahiaPropertyHelper.isStatusMinimized(getStatus())) {
            collapse();
            maxButton.setVisible(true);
            minButton.setVisible(false);
        } else {
            expand();
            maxButton.setVisible(true);
            minButton.setVisible(false);
        }

        addListener(Events.Expand, new OnPortletStatusChanged(this, JahiaPropertyHelper.getStatusNormaleValue()));
        addListener(Events.Collapse, new OnPortletStatusChanged(this, JahiaPropertyHelper.getStatusMinimizedValue()));
        layout();

    }

    @Override
    public void setEnabled(boolean b) {
        super.setEnabled(b);
        int toolCount = getHeader().getToolCount();
        for (int i = 0; i < toolCount; i++) {
            getHeader().getTool(i).setVisible(b);
        }
        setPinned(b);
    }

    public LayoutContainer getColumn() {
        return column;
    }

    public void setColumn(LayoutContainer column) {
        this.column = column;
    }

    public GWTLayoutItem getPorletConfig() {
        return porletConfig;
    }

    public void setPorletConfig(GWTLayoutItem porletConfig) {
        this.porletConfig = porletConfig;
    }

    public int getColumnIndex() {
        return porletConfig.getColumn();
    }

    public String getStatus() {
        return this.porletConfig.getStatus();
    }

    public void setColumnIndex(int columnIndex) {
        porletConfig.setColumn(columnIndex);
    }

    public int getRowIndex() {
        return porletConfig.getRow();
    }

    public void setRowIndex(int rowIndex) {
        porletConfig.setRow(rowIndex);
    }

    public void setStatus(String status) {
        porletConfig.setStatus(status);
    }

    public String getEntryPointInstanceId() {
        return porletConfig.getEntryPointInstanceID();
    }

    public String getWindowId() {
        return porletConfig.getWindowId();
    }

    public Widget getViewContainer() {
        return viewContainer;
    }

    public void setViewContainer(Widget viewContainer) {
        this.viewContainer = viewContainer;
    }

    public Widget getEditContainer() {
        return editContainer;
    }

    public void setEditContainer(Widget editContainer) {
        this.editContainer = editContainer;
    }

    public Widget getHelpContainer() {
        return helpContainer;
    }

    public void setHelpContainer(Widget helpContainer) {
        this.helpContainer = helpContainer;
    }

    public boolean isFullScreen() {
        return JahiaPropertyHelper.isStatusFullScreen(getStatus());
    }

}
