/**
 * ==========================================================================================
 * =                        DIGITAL FACTORY v7.0 - Community Distribution                   =
 * ==========================================================================================
 *
 *     Rooted in Open Source CMS, Jahia's Digital Industrialization paradigm is about
 *     streamlining Enterprise digital projects across channels to truly control
 *     time-to-market and TCO, project after project.
 *     Putting an end to "the Tunnel effect", the Jahia Studio enables IT and
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
 *
 * JAHIA'S DUAL LICENSING - IMPORTANT INFORMATION
 * ============================================
 *
 *     Copyright (C) 2002-2014 Jahia Solutions Group SA. All rights reserved.
 *
 *     THIS FILE IS AVAILABLE UNDER TWO DIFFERENT LICENSES:
 *     1/GPL OR 2/JSEL
 *
 *     1/ GPL
 *     ==========================================================
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
 *     describing the FLOSS exception, and it is also available here:
 *     http://www.jahia.com/license"
 *
 *     2/ JSEL - Commercial and Supported Versions of the program
 *     ==========================================================
 *
 *     IF YOU DECIDE TO CHOOSE THE JSEL LICENSE, YOU MUST COMPLY WITH THE FOLLOWING TERMS:
 *
 *     Alternatively, commercial and supported versions of the program - also known as
 *     Enterprise Distributions - must be used in accordance with the terms and conditions
 *     contained in a separate written agreement between you and Jahia Solutions Group SA.
 *
 *     If you are unsure which license is appropriate for your use,
 *     please contact the sales department at sales@jahia.com.
 */
package org.jahia.services.uicomponents.bean.contentmanager;

import org.jahia.services.uicomponents.bean.editmode.EngineConfiguration;
import org.jahia.services.uicomponents.bean.editmode.EngineTab;
import org.jahia.services.uicomponents.bean.toolbar.Toolbar;

import java.io.Serializable;
import java.util.*;

/**
 * Content manager configuration settings.
 * User: ktlili
 * Date: Apr 12, 2010
 * Time: 2:54:37 PM
 */
public class ManagerConfiguration implements Serializable {

    private static final long serialVersionUID = -8372296247741073414L;
    
    private List<Column> treeColumns;
    private List<Column> tableColumns;
    private List<Repository> repositories;

    private boolean hideLeftPanel = false;

    private List<String> folderTypes = new ArrayList<String>();
    private List<String> nodeTypes = new ArrayList<String>();
    private List<String> filters = new ArrayList<String>();
    private List<String> mimeTypes = new ArrayList<String>();

    private List<String> hiddenTypes = new ArrayList<String>();
    private String hiddenRegex;

    private String defaultView;
    private boolean enableDragAndDrop = true;
    private boolean enableFileDoubleClick = true;
    private boolean allowsMultipleSelection = true;
    private List<String> allowedNodeTypesForDragAndDrop;
    private List<String> forbiddenNodeTypesForDragAndDrop;

    private Map<String, EngineConfiguration> engineConfigurations;
    private List<EngineTab> engineTabs;

    private List<Toolbar> toolbars;
    private Toolbar contextMenu;


    private boolean expandRoot = false;
    
    private boolean allowRootNodeEditing;

    private boolean displaySearch = true;

    private boolean displaySearchInPage = true;
    private boolean displaySearchInTag = true;
    private boolean displaySearchInFile = true;
    private boolean displaySearchInContent = true;
    private boolean displaySearchInDateMeta = true;
    private boolean searchInFile = true;
    private boolean searchInContent = true;    
    private boolean searchInCurrentSiteOnly = false;
    private String searchBasePath = null;

    private String requiredPermission;
    private boolean showOnlyNodesWithTemplates = false;

    private boolean editableGrid;

    private List<String> componentsPaths = Arrays.asList("$site/components/*");

    public ManagerConfiguration() {
        tableColumns = new ArrayList<Column>();
        treeColumns = new ArrayList<Column>();
        repositories = new ArrayList<Repository>();
        engineTabs = new ArrayList<EngineTab>();
    }

    public void setHideLeftPanel(boolean hide) {
        this.hideLeftPanel = hide;
    }

    public List<Column> getTableColumns() {
        return tableColumns;
    }

    public void setTableColumns(List<Column> tableColumns) {
        this.tableColumns = tableColumns;
    }

    public List<Column> getTreeColumns() {
        return treeColumns;
    }

    public void setTreeColumns(List<Column> treeColumns) {
        this.treeColumns = treeColumns;
    }

    public List<Repository> getRepositories() {
        return repositories;
    }

    public void setRepositories(List<Repository> repositories) {
        this.repositories = repositories;
    }

    public boolean isHideLeftPanel() {
        return hideLeftPanel;
    }

    public List<String> getFolderTypes() {
        return folderTypes;
    }

    public void setFolderTypes(List<String> folderTypes) {
        this.folderTypes = folderTypes;
    }

    public List<String> getNodeTypes() {
        return nodeTypes;
    }

    public void setNodeTypes(List<String> nodeTypes) {
        this.nodeTypes = nodeTypes;
    }

    public List<String> getFilters() {
        return filters;
    }

    public void setFilters(List<String> filters) {
        this.filters = filters;
    }

    public List<String> getMimeTypes() {
        return mimeTypes;
    }

    public void setMimeTypes(List<String> mimeTypes) {
        this.mimeTypes = mimeTypes;
    }

    public String getDefaultView() {
        return defaultView;
    }

    public void setDefaultView(String defaultView) {
        this.defaultView = defaultView;
    }

    public boolean isEnableDragAndDrop() {
        return enableDragAndDrop;
    }

    public void setEnableDragAndDrop(boolean enableDragAndDrop) {
        this.enableDragAndDrop = enableDragAndDrop;
    }

    public List<String> getAllowedNodeTypesForDragAndDrop() {
        return allowedNodeTypesForDragAndDrop;
    }

    public void setAllowedNodeTypesForDragAndDrop(List<String> allowedNodeTypesForDragAndDrop) {
        this.allowedNodeTypesForDragAndDrop = allowedNodeTypesForDragAndDrop;
    }

    public List<String> getForbiddenNodeTypesForDragAndDrop() {
        return forbiddenNodeTypesForDragAndDrop;
    }

    public void setForbiddenNodeTypesForDragAndDrop(List<String> forbiddenNodeTypesForDragAndDrop) {
        this.forbiddenNodeTypesForDragAndDrop = forbiddenNodeTypesForDragAndDrop;
    }

    public boolean isEnableFileDoubleClick() {
        return enableFileDoubleClick;
    }

    public void setEnableFileDoubleClick(boolean enableFileDoubleClick) {
        this.enableFileDoubleClick = enableFileDoubleClick;
    }

    public List<Toolbar> getToolbars() {
        return toolbars;
    }

    public void setToolbars(List<Toolbar> toolbars) {
        this.toolbars = toolbars;
    }

    public Toolbar getContextMenu() {
        return contextMenu;
    }

    public void setContextMenu(Toolbar contextMenu) {
        this.contextMenu = contextMenu;
    }

    public boolean isAllowsMultipleSelection() {
        return allowsMultipleSelection;
    }

    public void setAllowsMultipleSelection(boolean allowsMultipleSelection) {
        this.allowsMultipleSelection = allowsMultipleSelection;
    }

    public boolean isExpandRoot() {
        return expandRoot;
    }

    public void setExpandRoot(boolean expandRoot) {
        this.expandRoot = expandRoot;
    }

    public boolean isDisplaySearch() {
        return displaySearch;
    }

    public void setDisplaySearch(boolean displaySearch) {
        this.displaySearch = displaySearch;
    }

    public boolean isDisplaySearchInPage() {
        return displaySearchInPage;
    }

    public void setDisplaySearchInPage(boolean displaySearchInPage) {
        this.displaySearchInPage = displaySearchInPage;
    }

    public boolean isDisplaySearchInTag() {
        return displaySearchInTag;
    }

    public void setDisplaySearchInTag(boolean displaySearchInTag) {
        this.displaySearchInTag = displaySearchInTag;
    }

    public boolean isDisplaySearchInFile() {
        return displaySearchInFile;
    }

    public void setDisplaySearchInFile(boolean displaySearchInFile) {
        this.displaySearchInFile = displaySearchInFile;
    }

    public boolean isDisplaySearchInContent() {
        return displaySearchInContent;
    }

    public void setDisplaySearchInContent(boolean displaySearchInContent) {
        this.displaySearchInContent = displaySearchInContent;
    }

    /**
     * @return Map of engine configurations
     */
    public Map<String, EngineConfiguration> getEngineConfigurations() {
        return engineConfigurations;
    }

    public void setEngineConfigurations(Map<String, EngineConfiguration> engineConfigurations) {
        this.engineConfigurations = engineConfigurations;
    }

    public List<EngineTab> getEngineTabs() {
        return engineTabs;
    }

    public void setEngineTabs(List<EngineTab> engineTabs) {
        this.engineTabs = engineTabs;
    }

    public boolean isSearchInFile() {
        return searchInFile;
    }

    public void setSearchInFile(boolean searchInFile) {
        this.searchInFile = searchInFile;
    }

    public boolean isSearchInContent() {
        return searchInContent;
    }

    public void setSearchInContent(boolean searchInContent) {
        this.searchInContent = searchInContent;
    }

    public boolean isSearchInCurrentSiteOnly() {
        return searchInCurrentSiteOnly;
    }

    public void setSearchInCurrentSiteOnly(boolean searchInCurrentSiteOnly) {
        this.searchInCurrentSiteOnly = searchInCurrentSiteOnly;
    }

    public String getSearchBasePath() {
        return searchBasePath;
    }

    public void setSearchBasePath(String searchBasePath) {
        this.searchBasePath = searchBasePath;
    }

    public String getRequiredPermission() {
        return requiredPermission;
    }

    public void setRequiredPermission(String requiredPermission) {
        this.requiredPermission = requiredPermission;
    }

    public List<String> getHiddenTypes() {
        return hiddenTypes;
    }

    public void setHiddenTypes(List<String> hiddenTypes) {
        this.hiddenTypes = hiddenTypes;
    }

    public String getHiddenRegex() {
        return hiddenRegex;
    }

    public void setHiddenRegex(String hiddenRegex) {
        this.hiddenRegex = hiddenRegex;
    }

    public boolean isShowOnlyNodesWithTemplates() {
        return showOnlyNodesWithTemplates;
    }

    public void setShowOnlyNodesWithTemplates(boolean showOnlyNodesWithTemplates) {
        this.showOnlyNodesWithTemplates = showOnlyNodesWithTemplates;
    }

    public boolean isAllowRootNodeEditing() {
        return allowRootNodeEditing;
    }

    public void setAllowRootNodeEditing(boolean allowRootNodeEditing) {
        this.allowRootNodeEditing = allowRootNodeEditing;
    }

    public boolean isDisplaySearchInDateMeta() {
        return displaySearchInDateMeta;
    }

    public void setDisplaySearchInDateMeta(boolean displaySearchInDateMeta) {
        this.displaySearchInDateMeta = displaySearchInDateMeta;
    }


    public boolean isEditableGrid() {
        return editableGrid;
    }

    public void setEditableGrid(boolean editableGrid) {
        this.editableGrid = editableGrid;
    }

    public List<String> getComponentsPaths() {
        return componentsPaths;
    }

    public void setComponentsPaths(List<String> componentsPaths) {
        this.componentsPaths = componentsPaths;
    }
}
