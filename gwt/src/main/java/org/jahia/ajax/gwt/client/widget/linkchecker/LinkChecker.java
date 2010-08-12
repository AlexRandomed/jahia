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
package org.jahia.ajax.gwt.client.widget.linkchecker;

import com.extjs.gxt.ui.client.widget.ContentPanel;
import com.extjs.gxt.ui.client.widget.Status;
import com.extjs.gxt.ui.client.widget.button.Button;
import com.extjs.gxt.ui.client.widget.toolbar.ToolBar;
import com.extjs.gxt.ui.client.widget.grid.*;
import com.extjs.gxt.ui.client.widget.layout.FitLayout;
import com.extjs.gxt.ui.client.store.ListStore;
import com.extjs.gxt.ui.client.core.XTemplate;
import com.extjs.gxt.ui.client.event.*;
import com.extjs.gxt.ui.client.Style;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.Timer;
import com.google.gwt.user.client.Window;
import com.allen_sauer.gwt.log.client.Log;

import java.util.HashMap;
import java.util.List;
import java.util.ArrayList;
import java.util.Map;

import org.jahia.ajax.gwt.client.core.BaseAsyncCallback;
import org.jahia.ajax.gwt.client.data.linkchecker.GWTJahiaCheckedLink;
import org.jahia.ajax.gwt.client.data.linkchecker.GWTJahiaLinkCheckerStatus;
import org.jahia.ajax.gwt.client.service.linkchecker.LinkCheckerService;
import org.jahia.ajax.gwt.client.util.URL;
import org.jahia.ajax.gwt.client.messages.Messages;

/**
 * User: romain
 * Date: 11 juin 2009
 * Time: 12:01:28
 */
public class LinkChecker extends ContentPanel {

    private static String[] WORKFLOW_STATE = new String[]{Messages.get("lc_versioned"), Messages.get("lc_live"), Messages.get("lc_staging"), Messages.get("lc_notify")};
    private static String[] WORKFLOW_IMAGES = new String[]{"600", "111", "121", "130"};
    private static Map<Integer, String> CODE_ICONS;

    static {
        CODE_ICONS = new HashMap<Integer, String>();
        CODE_ICONS.put(0, "warning.png");
        CODE_ICONS.put(300, "check.png");
        CODE_ICONS.put(301, "arrow_right_green.png");
        CODE_ICONS.put(302, "arrow_right_green.png");
        CODE_ICONS.put(307, "arrow_right_green.png");
        CODE_ICONS.put(400, "warning.png");
        CODE_ICONS.put(401, "lock.png");
        CODE_ICONS.put(403, "lock.png");
        CODE_ICONS.put(404, "unknown.png");
        CODE_ICONS.put(500, "error.png");
    }
    
    private ListStore<GWTJahiaCheckedLink> m_store;
    private Timer m_timer;
    private Button stop;
    private Status m_status;
    private Grid<GWTJahiaCheckedLink> linkTable;

    private static String getCodeIcon(int errorCode) {
        String icon = CODE_ICONS.get(errorCode);
        if (icon == null) {
            icon = CODE_ICONS.get(100 * (errorCode / 100));
        }
        if (icon == null) {
            icon = CODE_ICONS.get(0);
        }
        return icon;
    }

    public LinkChecker() {
        super(new FitLayout());
//        setSize(720, 740);
        setHeight(740);

        // data container initialization
        m_store = new ListStore<GWTJahiaCheckedLink>();
        
        linkTable = new Grid<GWTJahiaCheckedLink>(m_store, getHeaders());
        linkTable.addPlugin((RowExpander)linkTable.getColumnModel().getColumn(0));
        linkTable.getSelectionModel().setSelectionMode(Style.SelectionMode.SINGLE);
        linkTable.addListener(Events.RowDoubleClick, new Listener<GridEvent>() {
            public void handleEvent(GridEvent event) {
                GWTJahiaCheckedLink selection = linkTable.getSelectionModel().getSelectedItem();
                if (selection != null) {
                    Window.open(selection.getPageUrl(), "_blank", "");
                }
            }
        });

        m_timer = new Timer() {
            public void run() {
                Log.debug("polling...");
                LinkCheckerService.App.getInstance().lookForCheckedLinks(new BaseAsyncCallback<GWTJahiaLinkCheckerStatus>() {
                    public void onSuccess(GWTJahiaLinkCheckerStatus status) {
                        List<GWTJahiaCheckedLink> gwtJahiaCheckedLinks = status.getLinks();
                        for (GWTJahiaCheckedLink link: gwtJahiaCheckedLinks) {
                            if (!m_store.contains(link)) {
                                m_store.add(link);
                            }
                        }
                        if (!status.isActive()) {
                            Log.debug("polling over");
                            stop();
                            m_status.setText(Messages.get("lc_processed") + " "
                                            + status.getProcessed() + " "
                                            + Messages.get("lc_linksfound") + " "
                                            + status.getFailed() + " "
                                            + Messages.get("lc_invalid"));
                        } else {
                            m_status.setBusy(Messages.get("lc_checking")+"... "
                                            + Messages.get("lc_processed") + " "
                                            + status.getProcessed() + " " + Messages.get("lc_of") + " "
                                            + status.getTotal() +" "
                                            + Messages.get("lc_linksfound") + " "
                                            + status.getFailed() + " "
                                            + Messages.get("lc_invalid"));
                        }
                    }
                });
            }
        };

        // ui layout
        setHeaderVisible(false);
        ToolBar toolBar = new ToolBar();
        stop = new Button(Messages.get("lc_stop"), new SelectionListener<ButtonEvent>() {
            public void componentSelected(ButtonEvent event) {
                stopPolling();
            }
        });
        stop.setEnabled(false);
        final Button csvExport = new Button(Messages.get("lc_exportcsv"), new SelectionListener<ButtonEvent>() {
            public void componentSelected(ButtonEvent toolBarEvent) {
                new CSVExporter(m_store.getModels()).show();
            }
        });
        csvExport.setEnabled(false);
        Button checkLinks = new Button(Messages.get("lc_checkLinks"), new SelectionListener<ButtonEvent>() {
            public void componentSelected(ButtonEvent event) {
                m_store.removeAll();
                LinkCheckerService.App.getInstance().checkLinks(new BaseAsyncCallback<Boolean>() {
                    public void onSuccess(Boolean o) {
                        if (o != null && o.booleanValue()) {
                            Log.debug("starting polling...");
                            stop.setEnabled(true);
                            csvExport.setEnabled(true);
                            startPolling();
                        } else {
                            Window.alert("Access denied, only admin members are allowed.");
                        }
                    }
                });
            }
        });

        toolBar.add(checkLinks);
        toolBar.add(stop);
        toolBar.add(csvExport);

        setTopComponent(toolBar);

        m_status = new Status();
        m_status.clearState();

        setBottomComponent(m_status);

        add(linkTable);
    }

    private void startPolling() {
        Log.debug("scheduled every 2 seconds");
        m_timer.scheduleRepeating(2000);
        m_status.setBusy(Messages.get("lc_checking"));
    }

    private void stopPolling() {
        LinkCheckerService.App.getInstance().stopCheckingLinks(new BaseAsyncCallback() {
            public void onApplicationFailure(Throwable throwable) {
                Log.error(throwable.toString());
                stop();
            }

            public void onSuccess(Object o) {
                Log.debug("cancelled successfully");
                stop();
            }
        });
    }

    private void stop() {
        m_timer.cancel();
        stop.setEnabled(false);
        m_status.clearState();
    }

    private ColumnModel getHeaders() {
        final String ctx = URL.getJahiaContext();
        List<ColumnConfig> headerList = new ArrayList<ColumnConfig>();

        headerList
                .add(new RowExpander(
                        XTemplate
                                .create("<p><b>"
                                        + Messages.get("lc_link")
                                        + ":</b> {link}</p><br/>"
                                        + "<p><b>"
                                        + Messages.get("lc_page")
                                        + ":</b> <a href=\"{pageUrl}\" target=\"_blank\">{pageTitle} [{pageId}]</a></p><br/>"
                                        + "<p><b>"
                                        + Messages.get("lc_field")
                                        + ":</b> {fieldType} [{fieldId}]</p><br/>"
                                        + "<p><b>"
                                        + Messages.get("lc_language")
                                        + ":</b> {languageCode}</p><br/>"
                                        + "<p><b>"
                                        + Messages.get("lc_workflow")
                                        + ":</b> {workflowState}</p><br/>"
                                        + "<p><b>"
                                        + Messages.get("lc_code")
                                        + ":</b> {code} {codeText}</p><br/>"
                                        + "<p><b>"
                                        + Messages.get("lc_details")
                                        + ":</b><br/>{errorDetails}</p><br/>"
                                        + "<p><b>"
                                        + Messages.get("lc_edit")
                                        + ":</b> <a href=\"{updateUrl}\" target=\"_blank\"><img src=\""
                                        + ctx
                                        + "/gwt/resources/org/jahia/ajax/gwt/public/images/actions/update.png\""
                                        + " height=\"16\" width=\"16\" alt=\"edit\"/></a></p>")));  
        
        ColumnConfig col = new ColumnConfig("code", "", 30);
        col.setSortable(true);
        col.setResizable(false);
        col.setRenderer(new GridCellRenderer<GWTJahiaCheckedLink>() {
            public String render(GWTJahiaCheckedLink link, String property, ColumnData config, int rowIndex, int colIndex, ListStore<GWTJahiaCheckedLink> store, Grid<GWTJahiaCheckedLink> g) {
                return "<span title=\""
                        + link.getCodeText()
                        + "\">"
                        + "<img src=\""
                        + ctx
                        + "/gwt/resources/org/jahia/ajax/gwt/public/images/icons/"
                        + getCodeIcon(link.getCode()) + "\""
                        + " height=\"16\" width=\"16\" alt=\"" + link.getCode()
                        + "\" title=\"" + link.getCodeText() + "\"/></span>";
            }
        });
        headerList.add(col);

        col = new ColumnConfig("code", Messages.get("lc_code"), 50);
        col.setSortable(true);
        col.setResizable(true);
        col.setRenderer(new GridCellRenderer<GWTJahiaCheckedLink>() {
            public String render(GWTJahiaCheckedLink link, String property, ColumnData config, int rowIndex, int colIndex, ListStore<GWTJahiaCheckedLink> store, Grid<GWTJahiaCheckedLink> g) {
                return "<span title=\"" + link.getCodeText() + "\">"
                        + link.getCode() + "</span>";
            }
        });
        headerList.add(col);

        col = new ColumnConfig("link", Messages.get("lc_link"), 250);
        col.setSortable(true);
        col.setResizable(true);
        headerList.add(col);

        col = new ColumnConfig("pageTitle", Messages.get("lc_pageTitle"), 150);
        col.setSortable(true);
        col.setResizable(true);
        col.setRenderer(new GridCellRenderer<GWTJahiaCheckedLink>(){
            public String render(GWTJahiaCheckedLink link, String property, ColumnData config, int rowIndex, int colIndex, ListStore<GWTJahiaCheckedLink> store, Grid<GWTJahiaCheckedLink> g) {
                return "<a href=\"" + link.getPageUrl()
                        + "\" target=\"_blank\">" + link.getPageTitle() + " ["
                        + link.getPageId() + "]</a>";
            }});
        headerList.add(col);

        col = new ColumnConfig("languageCode", Messages.get("lc_language"), 50);
        col.setSortable(true);
        col.setResizable(true);
        headerList.add(col);

        col = new ColumnConfig("workflowState", Messages.get("lc_workflow"), 90);
        col.setSortable(true);
        col.setResizable(true);
        col.setRenderer(new GridCellRenderer<GWTJahiaCheckedLink>() {
            public String render(GWTJahiaCheckedLink link, String property, ColumnData config, int rowIndex, int colIndex, ListStore<GWTJahiaCheckedLink> store, Grid<GWTJahiaCheckedLink> g) {
                return "<img src=\"../engines/images/icons/workflow/"
                        + WORKFLOW_IMAGES[link.getWorkflowState()]
                        + ".png\" height=\"12\" width=\"12\" alt=\""
                        + link.getWorkflowState() + "\">&nbsp;"
                        + WORKFLOW_STATE[link.getWorkflowState()];
            }
        });

        headerList.add(col);

        col = new ColumnConfig("edit", Messages.get("lc_edit"), 50);
        col.setSortable(false);
        col.setResizable(false);
        col.setRenderer(new GridCellRenderer<GWTJahiaCheckedLink>() {
            public String render(GWTJahiaCheckedLink link, String property, ColumnData config, int rowIndex, int colIndex, ListStore<GWTJahiaCheckedLink> store, Grid<GWTJahiaCheckedLink> g) {
                return "<a href=\""
                        + link.getUpdateUrl()
                        + "\" target=\"_blank\"><img src=\""
                        + ctx
                        + "/gwt/resources/org/jahia/ajax/gwt/public/images/actions/update.png\""
                        + " height=\"16\" width=\"16\" alt=\"edit\"/></a>";
            }
        });
        headerList.add(col);
        
        return new ColumnModel(headerList);
    }
    
}
