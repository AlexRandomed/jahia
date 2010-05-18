package org.jahia.ajax.gwt.client.widget.node;

import com.allen_sauer.gwt.log.client.Log;
import com.extjs.gxt.ui.client.data.*;
import com.extjs.gxt.ui.client.event.*;
import com.extjs.gxt.ui.client.store.TreeStore;
import com.extjs.gxt.ui.client.store.TreeStoreEvent;
import com.extjs.gxt.ui.client.widget.Component;
import com.extjs.gxt.ui.client.widget.grid.ColumnModel;
import com.extjs.gxt.ui.client.widget.treegrid.TreeGrid;
import com.extjs.gxt.ui.client.widget.treepanel.TreePanel;
import com.google.gwt.user.client.Command;
import com.google.gwt.user.client.DeferredCommand;
import com.google.gwt.user.client.rpc.AsyncCallback;
import org.jahia.ajax.gwt.client.core.BaseAsyncCallback;
import org.jahia.ajax.gwt.client.data.node.GWTJahiaNode;
import org.jahia.ajax.gwt.client.service.content.JahiaContentManagementService;

import java.util.ArrayList;
import java.util.List;

/**
 * Tree factory for loading {@link GWTJahiaNode} items.
 * User: toto
 * Date: Dec 24, 2009
 * Time: 3:14:02 PM
 */
public class GWTJahiaNodeTreeFactory {
    private boolean init = true;
    protected String repository;
    protected String nodeTypes;
    protected String filters;
    protected List<String> fields = GWTJahiaNode.DEFAULT_FIELDS;
    protected String mimeTypes;
    protected List<String> selectedPath = new ArrayList<String>();
    protected List<String> openPath = new ArrayList<String>();
    protected boolean saveOpenPath = false;
    protected boolean autoSelect = true;
    protected GWTJahiaNodeTreeLoader loader;
    protected TreeStore<GWTJahiaNode> store;

    public GWTJahiaNodeTreeFactory(final String repositoryType) {
        this(repositoryType,GWTJahiaNode.DEFAULT_FIELDS);
    }

    public GWTJahiaNodeTreeFactory(final String repositoryType, List<String> fields) {
        repository = repositoryType;
        nodeTypes = "";
        filters = "";
        mimeTypes = "";
        this.fields = fields;
    }

    public GWTJahiaNodeTreeLoader getLoader() {
        if (loader == null) {
            loader = new GWTJahiaNodeTreeLoader(new GWTJahiaNodeProxy());
        }
        return loader;
    }



    public TreeStore<GWTJahiaNode> getStore() {
        if (store == null) {
            store = new TreeStore<GWTJahiaNode>(getLoader());
        }
        return store;
    }

    public GWTJahiaNodeTreeGrid getTreeGrid(ColumnModel cm) {
        GWTJahiaNodeTreeGrid grid = new GWTJahiaNodeTreeGrid(getStore(),cm);

        initOpenPathSaverTreeGrid(grid);

        return grid;
    }

    public GWTJahiaNodeTreePanel getTreePanel() {
        GWTJahiaNodeTreePanel panel = new GWTJahiaNodeTreePanel(getStore());

        initOpenPathSaverTreePanel(panel);

        return panel;
    }

    public void setRepository(String repository) {
        this.repository = repository;
    }

    public void setNodeTypes(String nodeTypes) {
        this.nodeTypes = nodeTypes;
    }

    public void setFilters(String filters) {
        this.filters = filters;
    }

    public void setFields(List<String> fields) {
        this.fields = new ArrayList<String>(fields);
        this.fields.add(GWTJahiaNode.CHILDREN_INFO);
        this.fields.add(GWTJahiaNode.ICON);
    }

    public void setMimeTypes(String mimeTypes) {
        this.mimeTypes = mimeTypes;
    }



    public void setSelectedPath(String selectedPath) {
        this.selectedPath.add(selectedPath);
    }

    public void setSelectedPath(List<String> selectedPath) {
        this.selectedPath = selectedPath;
    }

    public void setOpenPath(String openPath) {
        this.openPath.add(openPath);
    }

    public void setOpenPath(List<String> openPath) {
        this.openPath = openPath;
    }

    public void setSaveOpenPath(boolean saveOpenPath) {
        this.saveOpenPath = saveOpenPath;
    }

    /**
     * init method()
     */
    public void initOpenPathSaverTreePanel(final Component widget) {
        // add listener after rendering
        DeferredCommand.addCommand(new Command() {
            public void execute() {
                widget.addListener(Events.Expand, new Listener<TreePanelEvent>() {
                    public void handleEvent(TreePanelEvent le) {
                        GWTJahiaNode gwtJahiaNode = (GWTJahiaNode) le.getItem();
                        String path = gwtJahiaNode.getPath();
                        if (!openPath.contains(path)) {
                            openPath.add(path);
                        }
                        Log.debug("Save Path on expand " + openPath);
                        gwtJahiaNode.setExpandOnLoad(true);
//                        refresh(gwtJahiaNode);
                        if (saveOpenPath) {
                            savePaths();
                        }
                    }
                });

                widget.addListener(Events.Collapse, new Listener<TreePanelEvent>() {
                    public void handleEvent(TreePanelEvent el) {
                        GWTJahiaNode gwtJahiaNode = (GWTJahiaNode) el.getItem();
                        String path = gwtJahiaNode.getPath();
                        openPath.remove(path);
                        Log.debug("Save Path on collapse " + openPath);
                        gwtJahiaNode.setExpandOnLoad(false);
//                        refresh(gwtJahiaNode);
                        if (saveOpenPath) {
                            savePaths();
                        }
                    }
                });
            }
        });
    }

    public void initOpenPathSaverTreeGrid(final Component widget) {
        // add listener after rendering
        DeferredCommand.addCommand(new Command() {
            public void execute() {
                widget.addListener(Events.Expand, new Listener<TreeGridEvent>() {
                    public void handleEvent(TreeGridEvent le) {
                        GWTJahiaNode gwtJahiaNode = (GWTJahiaNode) le.getModel();
                        String path = gwtJahiaNode.getPath();
                        if (!openPath.contains(path)) {
                            openPath.add(path);
                        }
                        Log.debug("Save Path on expand " + openPath);
                        gwtJahiaNode.setExpandOnLoad(true);
//                        refresh(gwtJahiaNode);
                        if (saveOpenPath) {
                            savePaths();
                        }
                    }
                });

                widget.addListener(Events.Collapse, new Listener<TreeGridEvent>() {
                    public void handleEvent(TreeGridEvent el) {
                        GWTJahiaNode gwtJahiaNode = (GWTJahiaNode) el.getModel();
                        String path = gwtJahiaNode.getPath();
                        openPath.remove(path);
                        Log.debug("Save Path on collapse " + openPath);
                        gwtJahiaNode.setExpandOnLoad(false);
//                        refresh(gwtJahiaNode);
                        if (saveOpenPath) {
                            savePaths();
                        }
                    }
                });
            }
        });
    }

    public void savePaths() {
        JahiaContentManagementService.App.getInstance().saveOpenPathsForRepository(repository, openPath, new BaseAsyncCallback() {
            public void onSuccess(Object o) {
                // nothing to do
            }

            public void onApplicationFailure(Throwable throwable) {
                Log.error("Could not save expanded paths into user preferences:\n\n" + throwable.getLocalizedMessage(), throwable);
            }
        });
    }



    class GWTJahiaNodeProxy extends RpcProxy<List<GWTJahiaNode>> {
        public GWTJahiaNodeProxy() {
        }

        @Override
        protected void load(Object currentPage, AsyncCallback<List<GWTJahiaNode>> listAsyncCallback) {
            if (currentPage == null) {
                JahiaContentManagementService.App.getInstance().getRoot(repository, nodeTypes, mimeTypes, filters,
                        fields, selectedPath, openPath, listAsyncCallback);
            } else {
                GWTJahiaNode gwtJahiaNode = (GWTJahiaNode) currentPage;
                if (gwtJahiaNode.isExpandOnLoad()) {
                    List<GWTJahiaNode> list = new ArrayList<GWTJahiaNode>();
                    for (ModelData modelData : gwtJahiaNode.getChildren()) {
                        list.add((GWTJahiaNode) modelData);
                    }
                    listAsyncCallback.onSuccess(list);
                } else {
                    JahiaContentManagementService.App.getInstance().ls(gwtJahiaNode,nodeTypes, mimeTypes, filters,
                            fields, listAsyncCallback);
                }
            }
        }

    }


    class GWTJahiaNodeTreeLoader extends BaseTreeLoader<GWTJahiaNode> {
        GWTJahiaNodeTreeLoader(DataProxy proxy) {
            super(proxy);
        }

        @Override
        public boolean hasChildren(GWTJahiaNode parent) {
            return parent.hasChildren();
        }

        protected void onLoadSuccess(Object gwtJahiaNode, List<GWTJahiaNode> gwtJahiaNodes) {
            super.onLoadSuccess(gwtJahiaNode, gwtJahiaNodes);
            if (init) {
                Log.debug("setting init to false");
                init = false;
            }

        }

    }

    class GWTJahiaNodeTreeGrid extends TreeGrid<GWTJahiaNode> {
        GWTJahiaNodeTreeGrid(TreeStore store, ColumnModel cm) {
            super(store, cm);
        }

        protected void onDataChanged(TreeStoreEvent<GWTJahiaNode> mTreeStoreEvent) {
            super.onDataChanged(mTreeStoreEvent);
            init = false;
            GWTJahiaNode p = mTreeStoreEvent.getParent();
            if (p == null) {
                expandChildren(treeStore.getRootItems());
            } else {
                expandChildren(treeStore.getChildren(p));
            }
        }

        private void expandChildren(List<GWTJahiaNode> children) {
            for (GWTJahiaNode child : children) {
                if (child.isExpandOnLoad()) {
                    setExpanded(child, true);
                }
                if (child.isSelectedOnLoad()) {
                    getSelectionModel().select(true,child);
                }
            }
        }

    }

    class GWTJahiaNodeTreePanel extends TreePanel<GWTJahiaNode> {
        GWTJahiaNodeTreePanel(TreeStore store) {
            super(store);
            setAutoSelect(autoSelect);
        }

        protected void onDataChanged(TreeStoreEvent<GWTJahiaNode> mTreeStoreEvent) {
            super.onDataChanged(mTreeStoreEvent);
            init = false;
            GWTJahiaNode p = mTreeStoreEvent.getParent();
            if (p == null) {
                expandChildren(store.getRootItems());
            } else {
                expandChildren(store.getChildren(p));
            }
        }

        private void expandChildren(List<GWTJahiaNode> children) {
            for (GWTJahiaNode child : children) {
                if (child.isExpandOnLoad()) {
                    setExpanded(child, true);
                }
                if (selectedPath.contains(child.getPath())) {
                    List<GWTJahiaNode> l = new ArrayList<GWTJahiaNode>();
                    l.add(child);
                    getSelectionModel().setSelection(l);
                }
            }
        }

    }

    public void setAutoSelect(boolean autoSelect) {
        this.autoSelect = autoSelect;
    }


}
