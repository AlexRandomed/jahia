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

package org.jahia.ajax.gwt.engines.workflow.client.components;

import org.jahia.ajax.gwt.tripanelbrowser.client.components.LeftComponent;
import org.jahia.ajax.gwt.tripanelbrowser.client.BrowserLinker;
import org.jahia.ajax.gwt.engines.workflow.client.model.GWTJahiaWorkflowElement;
import org.jahia.ajax.gwt.engines.workflow.client.WorkflowServiceAsync;
import org.jahia.ajax.gwt.engines.workflow.client.WorkflowService;
import org.jahia.ajax.gwt.commons.client.tree.CustomTreeLoader;
import org.jahia.ajax.gwt.commons.client.tree.PreviousPathsOpener;
import org.jahia.ajax.gwt.commons.client.tree.CustomTreeBinder;
import com.extjs.gxt.ui.client.widget.Component;
import com.extjs.gxt.ui.client.widget.ContentPanel;
import com.extjs.gxt.ui.client.widget.layout.FitLayout;
import com.extjs.gxt.ui.client.widget.tree.Tree;
import com.extjs.gxt.ui.client.widget.tree.TreeItem;
import com.extjs.gxt.ui.client.data.*;
import com.extjs.gxt.ui.client.binder.TreeBinder;
import com.extjs.gxt.ui.client.store.TreeStore;
import com.extjs.gxt.ui.client.Style;
import com.extjs.gxt.ui.client.event.SelectionChangedListener;
import com.extjs.gxt.ui.client.event.SelectionChangedEvent;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.allen_sauer.gwt.log.client.Log;

import java.util.List;

/**
 * Created by IntelliJ IDEA.
 *
 * @author rfelden
 * @version 16 juil. 2008 - 16:30:16
 */
public class WorkflowTree extends LeftComponent {

    private ContentPanel m_component ;

    private Tree m_tree ;
    private TreeLoader<GWTJahiaWorkflowElement> loader ;
    private MyTreeBinder<GWTJahiaWorkflowElement> binder ;
    private TreeStore<GWTJahiaWorkflowElement> store ;

    private PreviousPathsOpener<GWTJahiaWorkflowElement> previousPathsOpener = null ;
    private TreeItem lastSelection = null ;
    private String rootPage ;

    public WorkflowTree(String siteKey, String startPage) {
        m_component = new ContentPanel(new FitLayout()) ;
        m_component.setHeading(siteKey);
        m_component.setScrollMode(Style.Scroll.AUTO);

        rootPage = startPage ;
        final WorkflowServiceAsync service = WorkflowService.App.getInstance() ;

        // data proxy
        RpcProxy<GWTJahiaWorkflowElement, List<GWTJahiaWorkflowElement>> proxy = new RpcProxy<GWTJahiaWorkflowElement, List<GWTJahiaWorkflowElement>>() {
            protected void load(GWTJahiaWorkflowElement gwtJahiaFolder, AsyncCallback<List<GWTJahiaWorkflowElement>> listAsyncCallback) {
                service.getSubElements(gwtJahiaFolder, listAsyncCallback);
            }
        };

        // tree loader
        loader = new CustomTreeLoader<GWTJahiaWorkflowElement>(proxy) {
            @Override
            public boolean hasChildren(GWTJahiaWorkflowElement parent) {
                return parent.hasChildren() ;
            }

            protected void expandPreviousPaths() {
                expandWorkflowPreviousPaths() ;
            }
        };

        // tree store
        store = new TreeStore<GWTJahiaWorkflowElement>(loader);

        m_tree = new Tree();
        m_tree.setAnimate(false);

        binder = new MyTreeBinder<GWTJahiaWorkflowElement>(m_tree, store) ;
        binder.init() ;
        binder.setCaching(true);
        binder.setDisplayProperty("title");
        binder.setIconProvider(new ModelStringProvider<GWTJahiaWorkflowElement>() {
            public String getStringValue(GWTJahiaWorkflowElement modelData, String s) {
                return "icon-" + modelData.getObjectType() ;
            }
        });

        binder.addSelectionChangedListener(new SelectionChangedListener<GWTJahiaWorkflowElement>() {
            public void selectionChanged(SelectionChangedEvent<GWTJahiaWorkflowElement> event) {
                TreeItem newSelection = m_tree.getSelectedItem() ;
                if (lastSelection != newSelection) {
                    lastSelection = newSelection ;
                    getLinker().onTreeItemSelected();
                }
            }
        });

        m_component.add(m_tree) ;
    }

    @Override
    public void initWithLinker(BrowserLinker linker) {
        super.initWithLinker(linker);
        loader.load(new GWTJahiaWorkflowElement(rootPage)) ;
    }

    private void expandWorkflowPreviousPaths() {
        if (previousPathsOpener == null) {
            previousPathsOpener = new PreviousPathsOpener<GWTJahiaWorkflowElement>(m_tree, store, binder) ;
        }
        previousPathsOpener.expandPreviousPaths();
        selectCurrentPage();
    }

    private void selectCurrentPage() {
        if (rootPage != null && rootPage.length()>0) {
            for (GWTJahiaWorkflowElement wfEl: store.getAllItems()) {
                if (wfEl.getObjectKey().equals(rootPage)) {
                    TreeItem toSelect = (TreeItem) binder.findItem(wfEl) ;
                    if (toSelect != null) {
                        m_tree.setSelectedItem(toSelect);
                    } else {
                        Log.debug("There was no page foudn corresponding to " + rootPage) ;
                    }
                }
            }
        }
    }

    public void openAndSelectItem(Object item) {
        // not needed here
    }

    public void refresh() {
        List<GWTJahiaWorkflowElement> selection = binder.getSelection() ;
        if (selection != null && selection.size() > 0) {
            loader.loadChildren(selection.get(0)) ;
        }

    }

    public Object getSelectedItem() {
        List<GWTJahiaWorkflowElement> selection = binder.getSelection() ;
        if (selection != null && selection.size() > 0) {
            return selection.get(0) ;
        } else {
            return null ;
        }
    }

    public Component getComponent() {
        return m_component ;
    }

    private class MyTreeBinder<M extends BaseTreeModel> extends TreeBinder<M> implements CustomTreeBinder<M> {

        public MyTreeBinder(Tree t, TreeStore<M> s) {
            super(t, s) ;
        }

        public void renderChildren(M parent, List<M> children) {
            super.renderChildren(parent, children);
        }

    }

}
