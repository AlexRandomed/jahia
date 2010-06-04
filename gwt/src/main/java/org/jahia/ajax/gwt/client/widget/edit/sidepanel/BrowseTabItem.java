package org.jahia.ajax.gwt.client.widget.edit.sidepanel;

import com.extjs.gxt.ui.client.Style;
import com.extjs.gxt.ui.client.dnd.DND;
import com.extjs.gxt.ui.client.dnd.TreeGridDropTarget;
import com.extjs.gxt.ui.client.event.DNDEvent;
import com.extjs.gxt.ui.client.widget.LayoutContainer;
import com.extjs.gxt.ui.client.widget.grid.ColumnConfig;
import com.extjs.gxt.ui.client.widget.grid.ColumnModel;
import com.extjs.gxt.ui.client.widget.layout.FitLayout;
import com.extjs.gxt.ui.client.widget.layout.VBoxLayout;
import com.extjs.gxt.ui.client.widget.layout.VBoxLayoutData;
import com.extjs.gxt.ui.client.widget.treegrid.TreeGrid;
import com.extjs.gxt.ui.client.widget.treegrid.TreeGridCellRenderer;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.rpc.AsyncCallback;
import org.jahia.ajax.gwt.client.core.BaseAsyncCallback;
import org.jahia.ajax.gwt.client.data.node.GWTJahiaNode;
import org.jahia.ajax.gwt.client.util.icons.ContentModelIconProvider;
import org.jahia.ajax.gwt.client.widget.NodeColumnConfigList;
import org.jahia.ajax.gwt.client.widget.edit.EditLinker;
import org.jahia.ajax.gwt.client.widget.edit.EditModeDNDListener;
import org.jahia.ajax.gwt.client.data.toolbar.GWTSidePanelTab;
import org.jahia.ajax.gwt.client.widget.node.GWTJahiaNodeTreeFactory;

import java.util.Arrays;
import java.util.List;

/**
 * Repository browser tab.
 * User: toto
 * Date: Dec 21, 2009
 * Time: 2:22:30 PM
 */
abstract class BrowseTabItem extends SidePanelTabItem {
    protected LayoutContainer treeContainer;
    protected TreeGrid<GWTJahiaNode> tree;
    protected TreeGridDropTarget treeDropTarget;
    protected String repositoryType;
    protected List<String> folderTypes;

    public BrowseTabItem(GWTSidePanelTab config) {
        super(config);
        this.folderTypes = config.getFolderTypes();
        VBoxLayout l = new VBoxLayout();
        l.setVBoxLayoutAlign(VBoxLayout.VBoxLayoutAlign.STRETCH);
        setLayout(l);

        treeContainer = new LayoutContainer();
        treeContainer.setBorders(true);
        treeContainer.setScrollMode(Style.Scroll.AUTO);
        treeContainer.setLayout(new FitLayout());
        GWTJahiaNodeTreeFactory factory = new GWTJahiaNodeTreeFactory(config.getPaths());
        factory.setNodeTypes(this.folderTypes);

        NodeColumnConfigList columns = new NodeColumnConfigList(config.getTreeColumns());
        columns.init();
        columns.get(0).setRenderer(new TreeGridCellRenderer());

        tree = factory.getTreeGrid(new ColumnModel(columns));
        tree.setAutoExpandColumn(columns.getAutoExpand());
        tree.getTreeView().setRowHeight(25);
        tree.getTreeView().setForceFit(true);
        tree.setHeight("100%");
        tree.setIconProvider(ContentModelIconProvider.getInstance());

        treeContainer.add(tree);

        VBoxLayoutData treeVBoxData = new VBoxLayoutData();
        treeVBoxData.setFlex(1);

        add(treeContainer, treeVBoxData);

        treeDropTarget = new BrowseTreeGridDropTarget();

    }

    @Override
    public void initWithLinker(EditLinker linker) {
        super.initWithLinker(linker);
        treeDropTarget.addDNDListener(linker.getDndListener());
        treeDropTarget.setAllowDropOnLeaf(true);
        treeDropTarget.setAllowSelfAsSource(false);
        treeDropTarget.setAutoExpand(true);
        treeDropTarget.setFeedback(DND.Feedback.APPEND);
    }

    class BrowseTreeGridDropTarget extends TreeGridDropTarget {
        public BrowseTreeGridDropTarget() {
            super(BrowseTabItem.this.tree);
        }

        @Override
        protected void onDragEnter(DNDEvent e) {
            if (EditModeDNDListener.SIMPLEMODULE_TYPE.equals(e.getStatus().getData(EditModeDNDListener.SOURCE_TYPE))) {
                List<GWTJahiaNode> nodes = e.getStatus().getData(EditModeDNDListener.SOURCE_NODES);
                if (acceptNode(nodes.get(0))) {
                    e.getStatus().setData(EditModeDNDListener.TARGET_TYPE, EditModeDNDListener.BROWSETREE_TYPE);
                    e.getStatus().setStatus(true);
                }
            } else {
                e.getStatus().setStatus(false);
            }
            e.setCancelled(false);
        }

        @Override
        protected void showFeedback(DNDEvent e) {
            super.showFeedback(e);
            if (activeItem != null) {
                GWTJahiaNode activeNode = (GWTJahiaNode) activeItem.getModel();
                e.getStatus().setData(EditModeDNDListener.TARGET_PATH, activeNode.get("path"));
            } else {
                e.getStatus().setData(EditModeDNDListener.TARGET_PATH, null);
            }
        }

        public AsyncCallback<Object> getCallback() {
            AsyncCallback<Object> callback = new BaseAsyncCallback<Object>() {
                public void onSuccess(Object o) {
                    refresh(0);
                }

            };
            return callback;
        }

    }

    protected abstract boolean acceptNode(GWTJahiaNode node);
}
