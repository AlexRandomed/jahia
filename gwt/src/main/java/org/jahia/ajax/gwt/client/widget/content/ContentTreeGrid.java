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

import com.extjs.gxt.ui.client.Style;
import com.extjs.gxt.ui.client.data.*;
import com.extjs.gxt.ui.client.event.*;
import com.extjs.gxt.ui.client.store.ListStore;
import com.extjs.gxt.ui.client.widget.ContentPanel;
import com.extjs.gxt.ui.client.widget.LayoutContainer;
import com.extjs.gxt.ui.client.widget.Text;
import com.extjs.gxt.ui.client.widget.Component;
import com.extjs.gxt.ui.client.widget.button.Button;
import com.extjs.gxt.ui.client.widget.form.ComboBox;
import com.extjs.gxt.ui.client.widget.form.SimpleComboBox;
import com.extjs.gxt.ui.client.widget.form.SimpleComboValue;
import com.extjs.gxt.ui.client.widget.grid.*;
import com.extjs.gxt.ui.client.widget.layout.BorderLayout;
import com.extjs.gxt.ui.client.widget.layout.BorderLayoutData;
import com.extjs.gxt.ui.client.widget.layout.FitLayout;
import com.extjs.gxt.ui.client.widget.treegrid.TreeGrid;
import com.extjs.gxt.ui.client.widget.treegrid.TreeGridCellRenderer;
import com.google.gwt.i18n.client.DateTimeFormat;
import com.google.gwt.user.client.Element;
import com.google.gwt.user.client.rpc.AsyncCallback;
import org.jahia.ajax.gwt.client.data.toolbar.GWTManagerConfiguration;
import org.jahia.ajax.gwt.client.data.node.GWTJahiaNode;
import org.jahia.ajax.gwt.client.data.node.GWTJahiaNodeVersion;
import org.jahia.ajax.gwt.client.messages.Messages;
import org.jahia.ajax.gwt.client.service.content.JahiaContentManagementService;
import org.jahia.ajax.gwt.client.util.Formatter;
import org.jahia.ajax.gwt.client.util.content.JCRClientUtils;
import org.jahia.ajax.gwt.client.util.icons.ContentModelIconProvider;
import org.jahia.ajax.gwt.client.util.icons.StandardIconsProvider;
import org.jahia.ajax.gwt.client.widget.node.GWTJahiaNodeTreeFactory;
import org.jahia.ajax.gwt.client.widget.tripanel.ManagerLinker;
import org.jahia.ajax.gwt.client.widget.tripanel.TopRightComponent;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * TreeTable file picker for use within classic engines.
 */
public class ContentTreeGrid extends LayoutContainer {
    private ManagerLinker linker;
    private ListStore<GWTJahiaNode> mainListStore;
    private final GWTManagerConfiguration configuration;
    private List<GWTJahiaNode> selectedNodes;
    private boolean multiple;
    private String repositoryType;

    /**
     * Content tree table
     *
     * @param repositoryType
     * @param selectedNodes
     * @param multiple
     * @param configuration
     */
    public ContentTreeGrid(String repositoryType, List<GWTJahiaNode> selectedNodes, boolean multiple, final GWTManagerConfiguration configuration) {
        this.multiple = multiple;
        this.linker = new ManagerLinker(configuration);
        this.repositoryType = repositoryType;
        this.configuration = configuration;
        this.selectedNodes = selectedNodes;

    }

    @Override
    protected void onRender(Element parent, int pos) {
        super.onRender(parent, pos);
        setLayout(new FitLayout());
        final ContentPanel mainContent = new ContentPanel();
        mainContent.setLayout(new BorderLayout());
        mainContent.setHeaderVisible(false);
        mainContent.setBorders(false);
        mainContent.setBodyBorder(false);

        // add toolbar
        final ContentToolbar contentToolbar = new ContentToolbar(configuration, linker);
        final TreeGridTopRightComponent treeGridTopRightComponent = new TreeGridTopRightComponent(repositoryType, configuration, selectedNodes);


        // register component linker
        linker.registerComponents(null, treeGridTopRightComponent, null, contentToolbar, null);

        // add grid
        BorderLayoutData borderLayoutData = new BorderLayoutData(Style.LayoutRegion.WEST, 300);
        borderLayoutData.setSplit(true);
        mainContent.add(treeGridTopRightComponent.getComponent(), borderLayoutData);

        // center
        final LayoutContainer contentContainer = new LayoutContainer();
        ThumbsListView thumbsListView = new ThumbsListView(false);
        BaseListLoader<ListLoadResult<GWTJahiaNode>> listLoader = new BaseListLoader<ListLoadResult<GWTJahiaNode>>(new RpcProxy<ListLoadResult<GWTJahiaNode>>() {
            @Override
            protected void load(Object gwtJahiaFolder, AsyncCallback<ListLoadResult<GWTJahiaNode>> listAsyncCallback) {
                contentContainer.mask(Messages.get("label_loading","Loading"));
                JahiaContentManagementService.App.getInstance()
                        .lsLoad((GWTJahiaNode) gwtJahiaFolder, configuration.getNodeTypes(), configuration.getMimeTypes(), null, listAsyncCallback);

            }
        });
        mainListStore = new ListStore<GWTJahiaNode>(listLoader);

        listLoader.addLoadListener(new LoadListener() {
            @Override
            public void loaderLoad(LoadEvent le) {
                if (!le.isCancelled()) {
                    contentContainer.unmask();
                }
            }
        });

        thumbsListView.setStore(mainListStore);
        thumbsListView.getSelectionModel().addSelectionChangedListener(new SelectionChangedListener<GWTJahiaNode>() {
            @Override
            public void selectionChanged(SelectionChangedEvent<GWTJahiaNode> event) {
                if (event != null && event.getSelectedItem() != null) {
                    onContentPicked(event.getSelectedItem());
                }
            }
        });

        BorderLayoutData centerLayoutData = new BorderLayoutData(Style.LayoutRegion.CENTER);
        centerLayoutData.setSplit(true);

        contentContainer.setId("images-view");
        contentContainer.setBorders(true);
        contentContainer.setScrollMode(Style.Scroll.AUTOY);
        contentContainer.add(thumbsListView);
        mainContent.add(contentContainer, centerLayoutData);

        mainContent.setTopComponent(contentToolbar.getComponent());

        add(mainContent);
        

    }


    /**
     * Override thi method to customize "add" button behaviour
     *
     * @param gwtJahiaNode the Picked Node
     */
    public void onContentPicked(final GWTJahiaNode gwtJahiaNode) {

    }


    /**
     * Get repository type
     *
     * @return String
     */
    protected String getRepoType() {
        return repositoryType;
    }


    /**
     * Tree Grid TopRightComponent wrapper
     */
    private class TreeGridTopRightComponent extends TopRightComponent {
        private String repositoryType;
        private GWTManagerConfiguration configuration;
        private boolean init = true;
        private TreeGrid<GWTJahiaNode> m_treeGrid;
        private TreeLoader<GWTJahiaNode> loader;
        private List<GWTJahiaNode> selectedNodes;

        private TreeGridTopRightComponent(String repositoryType, GWTManagerConfiguration configuration, List<GWTJahiaNode> selectedNodes) {
            this.repositoryType = repositoryType;
            this.configuration = configuration;
            this.selectedNodes = selectedNodes;
            init();
        }

        /**
         * init
         */
        private void init() {

            GWTJahiaNodeTreeFactory factory = new GWTJahiaNodeTreeFactory(repositoryType != null ? repositoryType : JCRClientUtils.GLOBAL_REPOSITORY);
            //factory.setNodeTypes(configuration.getNodeTypes());
            factory.setFolderTypes(configuration.getFolderTypes());
            factory.setMimeTypes(configuration.getMimeTypes());
            factory.setFilters(configuration.getFilters());
            List<String> selectedPath = new ArrayList<String>();
            for (GWTJahiaNode node : selectedNodes) {
                selectedPath.add(node.getPath());
            }
            factory.setSelectedPath(selectedPath);
            loader = factory.getLoader();
            m_treeGrid = factory.getTreeGrid(getHeaders());
            m_treeGrid.addListener(Events.RowClick, new Listener<GridEvent>() {
                public void handleEvent(GridEvent gridEvent) {
                    if (mainListStore != null) {
                        mainListStore.getLoader().load(m_treeGrid.getSelectionModel().getSelectedItem());
                    }
                }
            });
            m_treeGrid.getSelectionModel().addSelectionChangedListener(new SelectionChangedListener<GWTJahiaNode>() {
                public void selectionChanged(SelectionChangedEvent selectionChangedEvent) {
                    getLinker().onTreeItemSelected();
                }
            });

            m_treeGrid.getTreeView().setRowHeight(25);
            m_treeGrid.setIconProvider(ContentModelIconProvider.getInstance());
            /* m_treeGrid.getSelectionModel().addSelectionChangedListener(new SelectionChangedListener<GWTJahiaNode>() {
               @Override
               public void selectionChanged(SelectionChangedEvent<GWTJahiaNode> event) {
                   if (event != null && event.getSelectedItem() != null) {
                       onContentPicked(event.getSelectedItem());
                   }
               }
           }); */
            m_treeGrid.setHideHeaders(true);
            m_treeGrid.setBorders(false);


        }


        /**
         * Get header from configuration
         *
         * @return ColumnModel
         */
        private ColumnModel getHeaders() {
            List<ColumnConfig> headerList = new ArrayList<ColumnConfig>();
            List<String> columnIds = configuration.getTableColumns();
            if (columnIds == null || columnIds.size() == 0) {
                columnIds = new ArrayList<String>();
                columnIds.add("name");
                // columnIds.add("size");
                // columnIds.add("date");
                // columnIds.add("version");
                // columnIds.add("picker");
            }
            for (String s1 : columnIds) {
                if (s1.equals("name")) {
                    ColumnConfig col = new ColumnConfig("displayName", Messages.getResource("fm_column_name"), 300);
                    col.setRenderer(new TreeGridCellRenderer<GWTJahiaNode>());
                    headerList.add(col);
                } else if (s1.equals("size")) {
                    ColumnConfig col = new ColumnConfig("size", Messages.getResource("fm_column_size"), 70);
                    col.setAlignment(Style.HorizontalAlignment.CENTER);
                    col.setRenderer(new GridCellRenderer<GWTJahiaNode>() {
                        public Object render(GWTJahiaNode gwtJahiaNode, String s, ColumnData columnData, int i, int i1, ListStore<GWTJahiaNode> gwtJahiaNodeListStore, Grid<GWTJahiaNode> gwtJahiaNodeGrid) {
                            if (gwtJahiaNode != null && gwtJahiaNode.getSize() != null) {
                                long size = gwtJahiaNode.getSize();
                                return Formatter.getFormattedSize(size);
                            } else {
                                return "-";
                            }
                        }
                    });
                    headerList.add(col);
                } else if (s1.equals("date")) {
                    ColumnConfig col = new ColumnConfig("date", Messages.getResource("fm_column_date"), 80);
                    col.setAlignment(Style.HorizontalAlignment.CENTER);
                    col.setRenderer(new GridCellRenderer<GWTJahiaNode>() {
                        public Object render(GWTJahiaNode gwtJahiaNode, String s, ColumnData columnData, int i, int i1, ListStore<GWTJahiaNode> gwtJahiaNodeListStore, Grid<GWTJahiaNode> gwtJahiaNodeGrid) {
                            Date d = gwtJahiaNode.getLastModified();
                            if (d != null) {
                                return DateTimeFormat.getFormat("d/MM/y").format(d);
                            } else {
                                return "-";
                            }
                        }
                    });
                    headerList.add(col);
                } else if (s1.equals("version")) {
                    ColumnConfig col = new ColumnConfig("version", Messages.getResource("versioning_versionLabel"), 250);
                    col.setAlignment(Style.HorizontalAlignment.CENTER);
                    col.setRenderer(new GridCellRenderer<GWTJahiaNode>() {
                        public Object render(final GWTJahiaNode gwtJahiaNode, String s, ColumnData columnData, int i, int i1, ListStore<GWTJahiaNode> gwtJahiaNodeListStore, Grid<GWTJahiaNode> gwtJahiaNodeGrid) {
                            List<GWTJahiaNodeVersion> versions = gwtJahiaNode.getVersions();
                            if (versions != null) {
                                SimpleComboBox<String> combo = new SimpleComboBox<String>();
                                combo.setForceSelection(true);
                                combo.setTriggerAction(ComboBox.TriggerAction.ALL);
                                for (GWTJahiaNodeVersion version : versions) {
                                    combo.add(version.getVersionNumber() + " (" + DateTimeFormat.getFormat("d/MM/y hh:mm").format(version.getDate()) + ")");
                                }
                                final String s2 = "Always Latest Version";
                                combo.add(s2);
                                combo.setSimpleValue(s2);
                                combo.addSelectionChangedListener(new SelectionChangedListener<SimpleComboValue<String>>() {
                                    @Override
                                    public void selectionChanged(SelectionChangedEvent<SimpleComboValue<String>> simpleComboValueSelectionChangedEvent) {
                                        SimpleComboValue<String> value = simpleComboValueSelectionChangedEvent.getSelectedItem();
                                        String value1 = value.getValue();
                                        if (!s2.equals(value1))
                                            gwtJahiaNode.setSelectedVersion(value1.split("\\(")[0].trim());
                                    }
                                });
                                combo.setDeferHeight(true);
                                return combo;
                            } else {
                                SimpleComboBox<String> combo = new SimpleComboBox<String>();
                                combo.setForceSelection(false);
                                combo.setTriggerAction(ComboBox.TriggerAction.ALL);
                                combo.add("No version");
                                combo.setSimpleValue("No version");
                                combo.setEnabled(false);
                                combo.setDeferHeight(true);
                                return combo;
                            }
                        }
                    });
                    headerList.add(col);
                } else if (s1.equals("picker") && multiple) {
                    ColumnConfig col = new ColumnConfig("action", "action", 100);

                    col.setAlignment(Style.HorizontalAlignment.RIGHT);
                    col.setRenderer(new GridCellRenderer<GWTJahiaNode>() {
                        public Object render(final GWTJahiaNode gwtJahiaNode, String s, ColumnData columnData, int i, int i1, ListStore<GWTJahiaNode> gwtJahiaNodeListStore, Grid<GWTJahiaNode> gwtJahiaNodeGrid) {
                            if (gwtJahiaNode.isMatchFilters()) {
                                final Button pickContentButton = new Button(Messages.get("label_add", "Add"));
                                pickContentButton.setIcon(StandardIconsProvider.STANDARD_ICONS.plusRound());
                                pickContentButton.setEnabled(true);
                                pickContentButton.addSelectionListener(new SelectionListener<ButtonEvent>() {
                                    public void componentSelected(ButtonEvent buttonEvent) {
                                        onContentPicked(gwtJahiaNode);
                                    }
                                });
                                return pickContentButton;
                            } else {
                                return new Text("");
                            }
                        }

                    });
                    col.setFixed(true);
                    headerList.add(col);
                }
            }
            return new ColumnModel(headerList);
        }

        public void setContent(Object root) {
            // not implemented
        }

        public void clearTable() {
            // not implemebented
        }

        public Object getSelection() {
            if (m_treeGrid == null) {
                return null;
            }
            return m_treeGrid.getSelectionModel().getSelectedItems();
        }

        public void refresh() {
            m_treeGrid.getStore().removeAll();
            init = true;
            loader.load(null);
        }

        public Component getComponent() {
            return m_treeGrid;
        }
    }

}
