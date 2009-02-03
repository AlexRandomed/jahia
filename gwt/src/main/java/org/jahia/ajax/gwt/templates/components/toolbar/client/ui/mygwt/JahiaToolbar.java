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

package org.jahia.ajax.gwt.templates.components.toolbar.client.ui.mygwt;


import com.allen_sauer.gwt.log.client.Log;
import com.extjs.gxt.ui.client.widget.toolbar.ToolItem;
import com.extjs.gxt.ui.client.widget.toolbar.SeparatorToolItem;
import com.extjs.gxt.ui.client.widget.menu.Menu;
import com.extjs.gxt.ui.client.GXT;
import com.google.gwt.user.client.ui.FocusPanel;
import com.google.gwt.user.client.ui.Widget;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.Event;
import com.google.gwt.user.client.Element;
import com.google.gwt.user.client.DOM;
import org.jahia.ajax.gwt.config.client.beans.GWTJahiaPageContext;
import org.jahia.ajax.gwt.templates.components.toolbar.client.bean.GWTToolbar;
import org.jahia.ajax.gwt.templates.components.toolbar.client.bean.GWTToolbarItem;
import org.jahia.ajax.gwt.templates.components.toolbar.client.ui.mygwt.provider.JahiaProviderFactory;
import org.jahia.ajax.gwt.templates.components.toolbar.client.ui.mygwt.provider.ProviderHelper;
import org.jahia.ajax.gwt.templates.components.toolbar.client.ui.ToolbarManager;
import org.jahia.ajax.gwt.templates.components.toolbar.client.ui.dnd.TargetToolbarsPanel;
import org.jahia.ajax.gwt.templates.components.toolbar.client.service.ToolbarService;


/**
 * User: jahia
 * Date: 3 avr. 2008
 * Time: 11:32:56
 */
public abstract class JahiaToolbar extends FocusPanel {
    protected ToolbarManager toolbarManager;
    protected GWTToolbar gwtToolbar;
    protected GWTJahiaPageContext pageContext;
    protected FocusPanel draggableArea;
    protected boolean hasTabs = false;
    protected boolean loaded = false;


    // widget
    private Widget parentContainer;


    public JahiaToolbar(ToolbarManager toolbarManager, GWTToolbar gwtToolbar) {
        this.toolbarManager = toolbarManager;
        this.gwtToolbar = gwtToolbar;
        addStyleName("gwt-toolbar-Panel");
        addStyleName("jahia-admin-gxt");
        addStyleName("toolsbars-gxt");
        toolbarManager.incrementDiplayedToolbar();
        sinkEvents(Event.MOUSEEVENTS | Event.ONMOUSEUP | Event.ONMOUSEDOWN | Event.ONCLICK | Event.ONCONTEXTMENU);
    }

    public JahiaToolbar(GWTToolbar gwtToolbar) {
        super();
        this.gwtToolbar = gwtToolbar;
    }

    public GWTJahiaPageContext getPageContext() {
        return pageContext;
    }

    public void makeOnScrollFixed() {
        Element jEle = getElement();
        DOM.setStyleAttribute(jEle, "z-index", "999");
        if (GXT.isIE6) {
            DOM.setStyleAttribute(jEle, "position", "absolute");
        } else {
            DOM.setStyleAttribute(jEle, "position", "fixed");
        }
    }

    public void setPageContext(GWTJahiaPageContext page) {
        this.pageContext = page;
    }

    public GWTToolbar getGwtToolbar() {
        return gwtToolbar;
    }

    public void setGwtToolbar(GWTToolbar gwtToolbar) {
        this.gwtToolbar = gwtToolbar;
    }


    public Widget getParentContainer() {
        return parentContainer;
    }

    public void setParentContainer(Widget parentContainer) {
        this.parentContainer = parentContainer;
    }

    public FocusPanel getDraggableArea() {
        return draggableArea;
    }

    /**
     * Create north toolbar UI
     */
    public void createToolBarUI() {
        loaded = true;
    }

    /**
     * True if a tab has been added
     *
     * @return
     */
    protected boolean hasTabs() {
        return hasTabs;
    }

    public boolean isLoaded() {
        return loaded;
    }

    /**
     * set tab flag
     *
     * @param hasTabs
     */
    protected void setHasTabs(boolean hasTabs) {
        this.hasTabs = hasTabs;
    }

    public void clearAndRemoveFromParent() {
        removeFromParent();
        loaded = false;
    }


    /**
     * Create a Drag Separator
     *
     * @return
     */
    protected ToolItem createDragSeparator() {
        ToolItem separator = new SeparatorToolItem();
        separator.addStyleName("gwt-draggable");
        return separator;
    }


    /**
     * return true if it's a Separator
     *
     * @param gwtToolbarItem
     * @return
     */
    protected boolean isSeparator(GWTToolbarItem gwtToolbarItem) {
        return gwtToolbarItem.getType() != null && gwtToolbarItem.getType().equalsIgnoreCase(JahiaProviderFactory.ORG_JAHIA_TOOLBAR_ITEM_SEPARATOR);
    }

    /**
     * return true if it's a fill
     *
     * @param gwtToolbarItem
     * @return
     */
    protected boolean isFill(GWTToolbarItem gwtToolbarItem) {
        return gwtToolbarItem.getType() != null && gwtToolbarItem.getType().equalsIgnoreCase(JahiaProviderFactory.ORG_JAHIA_TOOLBAR_ITEM_FILL);
    }

    /**
     * refreah ui
     */
    public void refreshUI() {
        setParentContainer(getParent());
    }

    /**
     * Get parent absolute Top
     *
     * @return
     */
    public int getParentAbsoluteTop() {
        if (parentContainer == null) {
            return 0;
        }
        return parentContainer.getAbsoluteTop();
    }

    /**
     * Get parent absolute left
     *
     * @return
     */
    public int getParentAbsoluteLeft() {
        if (parentContainer == null) {
            return 0;
        }
        return parentContainer.getAbsoluteLeft();
    }

    /**
     * Set auto with
     *
     * @param autoWidth
     */
    public abstract void setAutoWidth(boolean autoWidth);

    /**
     * Add a context menu
     *
     * @param menu
     */
    public abstract void setContextMenu(Menu menu);

    public void setVisible(Widget parent, boolean visible) {
        this.setParentContainer(parent);
        setVisible(visible);
    }

    @Override
    public void setVisible(boolean visible) {
        super.setVisible(visible);
        if (visible) {
            toolbarManager.incrementDiplayedToolbar();
            Widget parent = getParentContainer();
            if (parent instanceof TargetToolbarsPanel) {
                ((TargetToolbarsPanel) parent).addToolbar(this);
            } else {
                toolbarManager.addFloatingToolbar(this);
            }
        } else {
            toolbarManager.decrementDisplayedToolbar();
            removeFromParent();
        }

    }

    /**
     * Set display.
     *
     * @param display
     */
    public void setDisplay(final boolean display) {
        gwtToolbar.getState().setDisplay(display);
        if (loaded || !display) {
            // update then display
            ToolbarService.App.getInstance().updateToolbar(getPageContext(), gwtToolbar, new AsyncCallback() {
                public void onSuccess(Object result) {
                    setVisible(display);
                }

                public void onFailure(Throwable throwable) {
                    Log.error("Unable to load toolbar.", throwable);
                }
            });
        } else {
             Log.error("Unable huuihh "+getParentContainer());
            // load then display
            ToolbarService.App.getInstance().loadGWTToolbar(getPageContext(), gwtToolbar, new AsyncCallback<GWTToolbar>() {
                public void onSuccess(GWTToolbar gwtToolbar) {
                    setGwtToolbar(gwtToolbar);
                    setVisible(display);
                    boolean display = gwtToolbar.getState().isDisplay();
                    if (display) {
                        createToolBarUI();
                        toolbarManager.handleDraggable(JahiaToolbar.this);
                    }
                }

                public void onFailure(Throwable throwable) {
                    Log.error("Unable to load toolbar.", throwable);
                }
            });
        }

    }

    /**
     * Get Item builder
     *
     * @return
     */
    protected ProviderHelper getProviderHelper() {
        ProviderHelper jahiaProviderHelper = new JahiaProviderFactory();
        return jahiaProviderHelper;
    }


    /**
     * ToolItem type unknown
     *
     * @param gwtToolbarItem
     */
    protected void printProviderNotFoundError(GWTToolbarItem gwtToolbarItem) {
        /* Window.alert("toolbar item widget " + gwtToolbarItem.getType() + " unknown. " +
       "\nPlease register it first in" +
       "\n'org.jahia.ajax.gwt.toolbar.client.util.reflection.CustomToolbarItemWidgetInstanceProvider' or" +
       "\n'org.jahia.ajax.gwt.toolbar.client.util.reflection.JahiaToolbarItemWidgetInstanceProvider'."); */
    }


}
