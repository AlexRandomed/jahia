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

package org.jahia.ajax.gwt.engines.versioning.client.page;

import com.extjs.gxt.ui.client.Style;
import com.extjs.gxt.ui.client.binder.TableBinder;
import com.extjs.gxt.ui.client.data.*;
import com.extjs.gxt.ui.client.event.*;
import com.extjs.gxt.ui.client.store.ListStore;
import com.extjs.gxt.ui.client.widget.Component;
import com.extjs.gxt.ui.client.widget.ContentPanel;
import com.extjs.gxt.ui.client.widget.PagingToolBar;
import com.extjs.gxt.ui.client.widget.layout.FitLayout;
import com.extjs.gxt.ui.client.widget.table.*;
import com.google.gwt.user.client.Command;
import com.google.gwt.user.client.DeferredCommand;
import com.google.gwt.user.client.rpc.AsyncCallback;
import org.jahia.ajax.gwt.engines.versioning.client.RPCVersioningServiceAsync;
import org.jahia.ajax.gwt.engines.versioning.client.RPCVersioningService;
import org.jahia.ajax.gwt.commons.client.beans.GWTVersion;
import org.jahia.ajax.gwt.tripanelbrowser.client.components.TopRightComponent;
import org.jahia.ajax.gwt.config.client.beans.GWTJahiaPageContext;
import org.jahia.ajax.gwt.config.client.JahiaGWTParameters;
import org.jahia.ajax.gwt.commons.client.util.ResourceBundle;

import java.util.ArrayList;
import java.util.List;

/**
 * User: jahia
 * Date: 14 juil. 2008
 * Time: 16:35:49
 */
public class VersionsTable extends TopRightComponent {

    private String versionableUUID;
    private ContentPanel m_component;
    private Table m_table;
    private PagingToolBar pagingToolBar;
    private ListStore<GWTVersion> store = new ListStore<GWTVersion>();
    private TableBinder<GWTVersion> binder;
    private BasePagingLoader loader;
    private int nbItemsPerPage = 20;
    private int maxVersions = Integer.MAX_VALUE;
    public VersionsTable(String versionableUUID) {
        this.versionableUUID = versionableUUID;
    }

    /**
     * Create UI
     */
    public void createUI() {
        m_component = new ContentPanel(new FitLayout());
        m_table = new Table(getHeaders());
        m_table.setHorizontalScroll(true);
        m_table.setWidth(400);
        m_table.setHeight(400);
        m_table.setVerticalLines(true);
        //m_table.setAutoWidth(true);
        final RPCVersioningServiceAsync service = RPCVersioningService.App.getInstance();

        final RpcProxy proxy = new RpcProxy<PagingLoadConfig, PagingLoadResult<GWTVersion>>() {
            @Override
            public void load(PagingLoadConfig pageConfig, AsyncCallback<PagingLoadResult<GWTVersion>> callback) {
                int offset = pageConfig.getOffset();
                String sortParameter = pageConfig.getSortInfo().getSortField();
                boolean isAscending = pageConfig.getSortInfo().getSortDir().equals(Style.SortDir.ASC);

                if (sortParameter == null) {
                    sortParameter = GWTVersion.VERSION_LABEL;
                }

                GWTJahiaPageContext page = new GWTJahiaPageContext();
                page.setPid(JahiaGWTParameters.getPID());
                page.setMode(JahiaGWTParameters.getOperationMode());

                // make the ajax call
                service.getRevisions(page,versionableUUID,JahiaGWTParameters.getLanguage(), true, false, true, true, 
                        offset, sortParameter, isAscending, nbItemsPerPage, callback);
            }
        };

        // loader
        loader = new BasePagingLoader<PagingLoadConfig, BasePagingLoadResult>(proxy);
        loader.setRemoteSort(true);

        // store
        store = new ListStore<GWTVersion>(loader);

        // binder
        binder = new TableBinder<GWTVersion>(m_table, store);


        ContentPanel contentPanel = new ContentPanel();
        contentPanel.setFrame(true);
        contentPanel.setCollapsible(false);
        contentPanel.setAnimCollapse(false);
        contentPanel.setHeaderVisible(false);
        contentPanel.setButtonAlign(Style.HorizontalAlignment.CENTER);
        contentPanel.setLayout(new FitLayout());
        //contentPanel.setAutoWidth(true);
        contentPanel.setWidth(500);
        //contentPanel.setAutoHeight(true);
        // add process job table
        contentPanel.add(m_table);

        // add table to main contentPanel
        m_component.add(contentPanel);

        // bottom component
        pagingToolBar = new PagingToolBar(nbItemsPerPage);
        pagingToolBar.bind(loader);
        contentPanel.setBottomComponent(pagingToolBar);

        //add table item selected item
        binder.addSelectionChangedListener(new SelectionChangedListener<GWTVersion>() {
            public void selectionChanged(SelectionChangedEvent<GWTVersion> event) {
                if (getLinker()!=null){
                    getLinker().onTableItemSelected();
                }
            }
        });

        // load after rendering
        DeferredCommand.addCommand(new Command() {
            public void execute() {
                refresh();
            }
        });
    }

    public void setContent(Object root) {
        refresh();
    }


    public void clearTable() {
        binder.getStore().removeAll();
    }

    /**
     * Get selected item
     *
     * @return
     */
    public Object getSelection() {
        List<GWTVersion> elts = binder.getSelection();
        if (elts != null && elts.size() > 0) {
            return elts.get(0);
        } else {
            return null;
        }
    }

    /**
     * Refresh selection
     */
    public void refresh() {
        pagingToolBar.setPageSize(this.nbItemsPerPage);
        loader.load(0, this.maxVersions);
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
     * Get headers
     *
     * @return
     */
    private static TableColumnModel getHeaders() {
        final List<TableColumn> headerList = new ArrayList<TableColumn>();

        //version label
        String columnName = GWTVersion.VERSION_LABEL;
        TableColumn col = new TableColumn(columnName, ResourceBundle.getNotEmptyResource("versioning_"+ GWTVersion.VERSION_LABEL,
                "Version label"), .2f);
        col.setMinWidth(100);
        headerList.add(col);

        //workflow state
        //columnName = Version.WORKFLOW_STATE;
        //col = new TableColumn(Version.WORKFLOW_STATE, columnName, .2f);
        //col.setMinWidth(80);
        //headerList.add(col);

        //author
        columnName = GWTVersion.AUTHOR;
        col = new TableColumn(columnName, ResourceBundle.getNotEmptyResource("versioning_"+ GWTVersion.AUTHOR,"Author"), .2f);
        col.setMinWidth(100);
        headerList.add(col);

        //date
        columnName = GWTVersion.DATE;
        col = new TableColumn(columnName, ResourceBundle.getNotEmptyResource("versioning_"+ GWTVersion.DATE,"Version date"), .25f);
        col.setMinWidth(150);
        headerList.add(col);

        //lang
        columnName = GWTVersion.LANG;
        col = new TableColumn(columnName, ResourceBundle.getNotEmptyResource("versioning_"+ GWTVersion.LANG,"Language"), .15f);
        col.setMinWidth(80);
        headerList.add(col);

        return new TableColumnModel(headerList);
    }

}