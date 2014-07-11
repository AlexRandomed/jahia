/**
 * ==========================================================================================
 * =                   JAHIA'S DUAL LICENSING - IMPORTANT INFORMATION                       =
 * ==========================================================================================
 *
 *     Copyright (C) 2002-2014 Jahia Solutions Group SA. All rights reserved.
 *
 *     THIS FILE IS AVAILABLE UNDER TWO DIFFERENT LICENSES:
 *     1/GPL OR 2/JSEL
 *
 *     1/ GPL
 *     ======================================================================================
 *
 *     IF YOU DECIDE TO CHOSE THE GPL LICENSE, YOU MUST COMPLY WITH THE FOLLOWING TERMS:
 *
 *     "This program is free software; you can redistribute it and/or
 *     modify it under the terms of the GNU General Public License
 *     as published by the Free Software Foundation; either version 2
 *     of the License, or (at your option) any later version.
 *
 *     This program is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 *     GNU General Public License for more details.
 *
 *     You should have received a copy of the GNU General Public License
 *     along with this program; if not, write to the Free Software
 *     Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301, USA.
 *
 *     As a special exception to the terms and conditions of version 2.0 of
 *     the GPL (or any later version), you may redistribute this Program in connection
 *     with Free/Libre and Open Source Software ("FLOSS") applications as described
 *     in Jahia's FLOSS exception. You should have received a copy of the text
 *     describing the FLOSS exception, also available here:
 *     http://www.jahia.com/license"
 *
 *     2/ JSEL - Commercial and Supported Versions of the program
 *     ======================================================================================
 *
 *     IF YOU DECIDE TO CHOOSE THE JSEL LICENSE, YOU MUST COMPLY WITH THE FOLLOWING TERMS:
 *
 *     Alternatively, commercial and supported versions of the program - also known as
 *     Enterprise Distributions - must be used in accordance with the terms and conditions
 *     contained in a separate written agreement between you and Jahia Solutions Group SA.
 *
 *     If you are unsure which license is appropriate for your use,
 *     please contact the sales department at sales@jahia.com.
 *
 *
 * ==========================================================================================
 * =                                   ABOUT JAHIA                                          =
 * ==========================================================================================
 *
 *     Rooted in Open Source CMS, Jahia’s Digital Industrialization paradigm is about
 *     streamlining Enterprise digital projects across channels to truly control
 *     time-to-market and TCO, project after project.
 *     Putting an end to “the Tunnel effect”, the Jahia Studio enables IT and
 *     marketing teams to collaboratively and iteratively build cutting-edge
 *     online business solutions.
 *     These, in turn, are securely and easily deployed as modules and apps,
 *     reusable across any digital projects, thanks to the Jahia Private App Store Software.
 *     Each solution provided by Jahia stems from this overarching vision:
 *     Digital Factory, Workspace Factory, Portal Factory and eCommerce Factory.
 *     Founded in 2002 and headquartered in Geneva, Switzerland,
 *     Jahia Solutions Group has its North American headquarters in Washington DC,
 *     with offices in Chicago, Toronto and throughout Europe.
 *     Jahia counts hundreds of global brands and governmental organizations
 *     among its loyal customers, in more than 20 countries across the globe.
 *
 *     For more information, please visit http://www.jahia.com
 */
package org.jahia.ajax.gwt.client.widget.edit;


import com.allen_sauer.gwt.log.client.Log;
import com.extjs.gxt.ui.client.Style;
import com.extjs.gxt.ui.client.widget.ContentPanel;
import com.extjs.gxt.ui.client.widget.layout.BorderLayoutData;
import com.google.gwt.storage.client.Storage;
import com.google.gwt.user.client.ui.Widget;
import org.jahia.ajax.gwt.client.core.JahiaGWTParameters;
import org.jahia.ajax.gwt.client.data.GWTJahiaChannel;
import org.jahia.ajax.gwt.client.data.GWTJahiaLanguage;
import org.jahia.ajax.gwt.client.data.node.GWTJahiaNode;
import org.jahia.ajax.gwt.client.data.toolbar.GWTEditConfiguration;
import org.jahia.ajax.gwt.client.util.security.PermissionsUtils;
import org.jahia.ajax.gwt.client.widget.edit.mainarea.MainModule;
import org.jahia.ajax.gwt.client.widget.edit.mainarea.Module;
import org.jahia.ajax.gwt.client.widget.edit.sidepanel.SidePanel;
import org.jahia.ajax.gwt.client.widget.toolbar.ActionToolbarLayoutContainer;
import org.jahia.ajax.gwt.client.widget.Linker;
import org.jahia.ajax.gwt.client.widget.LinkerSelectionContext;

import java.util.List;
import java.util.ArrayList;
import java.util.Map;


/**
 * Helper object that contains edit mode context information. 
 *
 * @author rincevent
 */
public class EditLinker implements Linker {

    private GWTEditConfiguration config;
    private String mainPath;
    private String template;

    private LinkerSelectionContext selectionContext = new LinkerSelectionContext();
    private Module selectedModule;
    private EditModeDNDListener dndListener;
    private ActionToolbarLayoutContainer toolbar;
    private MainModule mainModule;
    private SidePanel sidePanel;
    private ModuleSelectionListener selectionListener;
    private Widget mainAreaComponent;
    private int mainAreaVScrollPosition;
    private String locale;
    private GWTJahiaChannel activeChannel;
    private String activeChannelVariant = null;
    private boolean isInSettingsPage = true;

    /**
     * Initializes an instance of this class.
     * 
     * @param mainModule
     *            the current main module
     * @param sidePanel
     *            reference to the side panel
     * @param toolbar
     *            toolbar container object
     * @param config
     *            the edit mode configuration settings
     */
    public EditLinker(MainModule mainModule, SidePanel sidePanel, ActionToolbarLayoutContainer toolbar,
                      GWTEditConfiguration config) {
        if (config.isEnableDragAndDrop()) {
            this.dndListener = new EditModeDNDListener(this);
        }
        this.mainModule = mainModule;
        this.sidePanel = sidePanel;
        this.toolbar = toolbar;
        this.config = config;
        registerLinker();
    }

    /**
     * Returns a side panel object.
     * 
     * @return a side panel object
     */
    public SidePanel getSidePanel() {
        return sidePanel;
    }

    /**
     * Sets the site panel reference.
     * 
     * @param sidePanel
     *            the site panel reference
     */
    public void setSidePanel(SidePanel sidePanel) {
        this.sidePanel = sidePanel;
        if (sidePanel != null) {
            try {
                sidePanel.initWithLinker(this);
            } catch (Exception e) {
                Log.error("Error on init linker",e);
            }
        }
    }

    /**
     * Returns main module object.
     * 
     * @return main module object
     */
    public MainModule getMainModule() {
        return mainModule;
    }

    /**
     * Returns edit mode configuration settings.
     * 
     * @return edit mode configuration settings
     */
    public GWTEditConfiguration getConfig() {
        return config;
    }

    /**
     * Switch edit mode configuration
     *
     * @param config
     * @param newPath
     * @param updateSidePanel
     * @param updateToolbar
     */
    public void switchConfig(GWTEditConfiguration config, String newPath, boolean updateSidePanel, boolean updateToolbar, String enforcedWorkspace) {
        this.config = config;

        JahiaGWTParameters.setSiteNode(config.getSiteNode());
        JahiaGWTParameters.setSitesMap(config.getSitesMap());
        JahiaGWTParameters.setChannels(config.getChannels());
        if(enforcedWorkspace!=null) {
            JahiaGWTParameters.setWorkspace(enforcedWorkspace);
        }
        PermissionsUtils.loadPermissions(config.getPermissions());

        if (updateSidePanel) {
            this.activeChannel = null;
            this.activeChannelVariant = null;
        }

        mainModule.setConfig(config, newPath);
        if (updateSidePanel) {
            sidePanel.setConfig(config);
        }

        if (updateToolbar) {
            toolbar.setConfig(config);
        }
        EditPanelViewport.getInstance().setViewportStyleName(config.getName());
        registerLinker(true, updateSidePanel, updateToolbar);
    }

    /**
     * Returns edit mode drag&drop listener instance.
     * 
     * @return edit mode drag&drop listener instance
     */
    public EditModeDNDListener getDndListener() {
        return dndListener;
    }

    /**
     * Returns current content locale.
     * 
     * @return current content locale
     */
    public String getLocale() {
        return locale;
    }

    /**
     * Sets current content locale.
     * 
     * @param locale
     *            current content locale
     */
    public void setLocale(GWTJahiaLanguage locale) {
        if (locale != null) {
            this.locale = locale.getLanguage();
            JahiaGWTParameters.setLanguage(locale);
        } else {
            this.locale = null;
        }
    }

    /**
     * Returns currently selected module.
     * @return currently selected module
     */
    public Module getSelectedModule() {
        return selectedModule;
    }

    /**
     * Registers the module selection listener.
     * 
     * @param selectionListener
     *            the module selection listener
     */
    public void setSelectionListener(ModuleSelectionListener selectionListener) {
        this.selectionListener = selectionListener;
    }

    /**
     * Callback for the module selection event.
     * 
     * @param selection
     *            the currently selected module
     */
    public void onModuleSelection(Module selection) {
        if (this.selectionListener == null) {
            selectedModule = selection;

            handleNewModuleSelection();
            if (selectedModule != null) {
                selectedModule.setDraggable(true);
            }
        } else {
            selectionListener.onModuleSelection(selection);
        }
    }

    /**
     * Callback for the main module selection.
     * 
     * @param mainPath
     *            the main module path
     * @param template
     *            the template
     */
    public void onMainSelection(String mainPath, String template) {
        this.mainPath = mainPath;
        this.selectedModule = null;
        this.template = template;
    }

    /**
     * Performs refresh of the main module and side panel using provided refresh data.
     * 
     * @param data
     *            the refresh data
     */
    public void refresh(Map<String, Object> data) {
        mainModule.refresh(data);
        if (sidePanel != null) {
            sidePanel.refresh(data);
        }
    }

    /**
     * Callback for the module selection event.
     */
    public void handleNewModuleSelection() {
        syncSelectionContext(LinkerSelectionContext.BOTH);
        toolbar.handleNewLinkerSelection();
        mainModule.handleNewModuleSelection(selectedModule);
        if (sidePanel != null) {
            sidePanel.handleNewModuleSelection(selectedModule);
        }
    }

    /**
     * Callback for the new main module selection event.
     */
    public void handleNewMainSelection() {
        syncSelectionContext(LinkerSelectionContext.BOTH);
        mainModule.handleNewMainSelection(mainPath,template);
        mainModule.handleNewModuleSelection(null);
        if (sidePanel != null) {
            sidePanel.handleNewMainSelection(mainPath);
        }
        toolbar.handleNewLinkerSelection();
    }

    /**
     * Callback for the main module node loaded event.
     */
    public void handleNewMainNodeLoaded() {
        syncSelectionContext(LinkerSelectionContext.BOTH);
        toolbar.handleNewMainNodeLoaded(mainModule.getNode());
        if (sidePanel != null) {
            sidePanel.handleNewMainNodeLoaded(mainModule.getNode());
        }
    }

    /**
     * Set up linker (callback for each member).
     */
    protected void registerLinker() {
        registerLinker(true,true,true);
    }

    /**
     * Set up linker (callback for each member).
     */
    protected void registerLinker(boolean doMainModule, boolean doSidePanel, boolean doToolbar) {
        if (mainModule != null && doMainModule) {
            try {
                mainModule.initWithLinker(this);
            } catch (Exception e) {
                Log.error("Error on init linker",e);
            }
        }
        if (sidePanel != null && doSidePanel) {
            try {
                sidePanel.initWithLinker(this);
            } catch (Exception e) {
                Log.error("Error on init linker",e);
            }
        }
        if (toolbar != null && doToolbar) {
            try {
                toolbar.initWithLinker(this);
            } catch (Exception e) {
                Log.error("Error on init linker",e);
            }
        }
    }


    /**
     * Performs the selection of the specified module.
     * 
     * @param o
     *            the module object
     */
    public void select(Object o) {
        if (o == null || o instanceof Module) {
            onModuleSelection((Module) o);
        }
    }

    /**
     * Indicates that a processing action is finished and we can unmask the main module area.
     */
    public void loaded() {
        mainModule.unmask();
    }

    /**
     * Masks the main module area and adds the specified message into the loading indicator.
     * 
     * @param resource
     *            the message text to show in the loading indicator
     */
    public void loading(String resource) {
        mainModule.mask(resource, "x-mask-loading");

    }

    /**
     * Callback for the event of data update.
     *
     * @param paths
     *            the updated items paths
     *
     */
    public void setSelectPathAfterDataUpdate(List<String> paths) {
        // no implementation so far
    }

    /**
     * Returns the current selection context.
     * 
     * @return the current selection context
     */
    public LinkerSelectionContext getSelectionContext() {
        return selectionContext;
    }

    /**
     * Performs the synchronization with the current selection as a result of (main) module change or main module node loaded event.
     * 
     * @param context
     *            the type of the context; see {@link LinkerSelectionContext}
     */
    public void syncSelectionContext(int context) {
        selectionContext.setMainNode(getMainModule().getNode());
        List<GWTJahiaNode> nodes = new ArrayList<GWTJahiaNode>();
        if (mainModule.isCtrlActive()) {
            nodes.addAll(selectionContext.getSelectedNodes());
        }
        if (getSelectedModule() != null && getSelectedModule().getNode() != null && !(getSelectedModule() instanceof MainModule)) {
            nodes.add(getSelectedModule().getNode());
        }
        selectionContext.setSelectedNodes(nodes);
        selectionContext.refresh(context);
    }

    /**
     * Replaces the content of the main area with the specified widget (usually content edit engine).
     * 
     * @param w
     *            the widget to replace the main module area with
     */
    public void replaceMainAreaComponent(Widget w) {
        ContentPanel m;
        if (mainAreaComponent == null) {
            m = (ContentPanel) mainModule.getParent();
            mainAreaVScrollPosition = mainModule.getContainer().getVScrollPosition();
            mainModule.setStyleAttribute("display","none");
        } else {
            m = (ContentPanel) mainAreaComponent.getParent();
            m.remove(mainAreaComponent);
        }
        mainAreaComponent = w;
        m.insert(mainAreaComponent, 0, new BorderLayoutData(Style.LayoutRegion.CENTER));
        m.layout();
    }

    /**
     * Restores the view of the main module area.
     */
    public void restoreMainArea() {
        ContentPanel m = (ContentPanel) mainAreaComponent.getParent();
        m.remove(mainAreaComponent);
        mainAreaComponent = null;
        mainModule.setStyleAttribute("display","block");
        m.layout();
        mainModule.getContainer().setVScrollPosition(mainAreaVScrollPosition);
    }

    /**
     * Indicates if we need to display hidden properties.
     * 
     * @return <code>true</code> if teh hidden properties should be shown
     */
    public boolean isDisplayHiddenProperties() {
        return false;
    }

    /**
     * Returns an active channel instance.
     * 
     * @return an active channel instance
     */
    public GWTJahiaChannel getActiveChannel() {
        return activeChannel;
    }

    /**
     * Sets currently active channel instance.
     * 
     * @param activeChannel
     *            currently active channel instance
     */
    public void setActiveChannel(GWTJahiaChannel activeChannel) {
        this.activeChannel = activeChannel;
    }

    /**
     * Returns an identifier of the currently active channel instance if any.
     * 
     * @return an identifier of the currently active channel instance if any; <code>null</code> if there is no currently active channel
     */
    public String getActiveChannelIdentifier() {
        return activeChannel != null ? activeChannel.getValue() : null;
    }

    /**
     * Returns active channel variant.
     * 
     * @return active channel variant
     */
    public String getActiveChannelVariant() {
        return activeChannelVariant;
    }

    /**
     * Sets active channel variant.
     * 
     * @param activeChannelVariant
     *            active channel variant
     */
    public void setActiveChannelVariant(String activeChannelVariant) {
        this.activeChannelVariant = activeChannelVariant;
    }

    public boolean isInSettingsPage() {
        return isInSettingsPage;
    }

    public void setInSettingsPage(boolean inSettingsPage) {
        this.isInSettingsPage = inSettingsPage;
    }

    /**
     * Returns an index of the currently active channel variant.
     * 
     * @return an index of the currently active channel variant
     */
    public int getActiveChannelVariantIndex() {
        int result = 0;
        if (getActiveChannel() != null && getActiveChannelVariant() != null) {
            String[] variantValueArray = getActiveChannel().getCapability("variants").split(",");
            for (int i = 0; i < variantValueArray.length; i++) {
                if (variantValueArray[i].equals(getActiveChannelVariant())) {
                    // we found the active variant !
                    result = i;
                    break;
                }
            }
        }
        return result;
    }

    public Widget getMainAreaComponent() {
        return mainAreaComponent;
    }
}
