package org.jahia.ajax.gwt.client.widget.contentengine;

import com.extjs.gxt.ui.client.Style;
import com.extjs.gxt.ui.client.data.*;
import com.extjs.gxt.ui.client.event.*;
import com.extjs.gxt.ui.client.store.GroupingStore;
import com.extjs.gxt.ui.client.widget.ContentPanel;
import com.extjs.gxt.ui.client.widget.LayoutContainer;
import com.extjs.gxt.ui.client.widget.form.FormPanel;
import com.extjs.gxt.ui.client.widget.form.TextField;
import com.extjs.gxt.ui.client.widget.grid.ColumnConfig;
import com.extjs.gxt.ui.client.widget.grid.ColumnModel;
import com.extjs.gxt.ui.client.widget.grid.Grid;
import com.extjs.gxt.ui.client.widget.layout.BorderLayoutData;
import com.extjs.gxt.ui.client.widget.layout.FitLayout;
import com.extjs.gxt.ui.client.widget.layout.FormData;
import com.extjs.gxt.ui.client.widget.toolbar.PagingToolBar;
import com.google.gwt.user.client.Element;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.HTML;
import org.jahia.ajax.gwt.client.data.GWTJahiaContentHistoryEntry;
import org.jahia.ajax.gwt.client.data.node.GWTJahiaNode;
import org.jahia.ajax.gwt.client.messages.Messages;
import org.jahia.ajax.gwt.client.service.content.JahiaContentManagementService;
import org.jahia.ajax.gwt.client.service.content.JahiaContentManagementServiceAsync;
import org.jahia.ajax.gwt.client.util.Formatter;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * Created by IntelliJ IDEA.
 * User: loom
 * Date: Oct 5, 2010
 * Time: 5:47:40 PM
 * To change this template use File | Settings | File Templates.
 */
public class HistoryPanel extends LayoutContainer {

    private GWTJahiaNode node;
    private FormPanel detailsPanel;

    private List<GWTJahiaContentHistoryEntry> selectedItems = null;
    private PagingToolBar pagingToolBar;


    public HistoryPanel(GWTJahiaNode node) {
        super(new FitLayout());
        this.node = node;
        init();
    }

    protected void onRender(Element parent, int index) {
        super.onRender(parent, index);
    }

    private void init() {
        setBorders(false);
        final JahiaContentManagementServiceAsync service = JahiaContentManagementService.App.getInstance();

        // data proxy
        RpcProxy<BasePagingLoadResult<GWTJahiaContentHistoryEntry>> proxy = new RpcProxy<BasePagingLoadResult<GWTJahiaContentHistoryEntry>>() {
            @Override
            protected void load(Object loadConfig, AsyncCallback<BasePagingLoadResult<GWTJahiaContentHistoryEntry>> callback) {
                if (loadConfig == null) {
                    service.getContentHistory(node.getUUID(), 0, Integer.MAX_VALUE, callback);
                } else if (loadConfig instanceof BasePagingLoadConfig) {
                    BasePagingLoadConfig pagingLoadConfig = (BasePagingLoadConfig) loadConfig;
                    int limit = pagingLoadConfig.getLimit();
                    int offset = pagingLoadConfig.getOffset();
                    Style.SortDir sortDir = pagingLoadConfig.getSortDir();
                    String sortField = pagingLoadConfig.getSortField();
                    service.getContentHistory(node.getUUID(), offset, limit, callback);
                } else {
                    callback.onSuccess(new BasePagingLoadResult<GWTJahiaContentHistoryEntry>(new ArrayList<GWTJahiaContentHistoryEntry>()));
                }
            }
        };

        // tree loader
        final PagingLoader<BasePagingLoadResult<ModelData>> loader = new BasePagingLoader<BasePagingLoadResult<ModelData>>(proxy);
        loader.setRemoteSort(true);

        // trees store
        final GroupingStore<GWTJahiaContentHistoryEntry> store = new GroupingStore<GWTJahiaContentHistoryEntry>(loader);
        store.groupBy("status");

        pagingToolBar = new PagingToolBar(50);
        pagingToolBar.bind(loader);

        List<ColumnConfig> config = new ArrayList<ColumnConfig>();

        ColumnConfig column = new ColumnConfig("date", Messages.get("label.date", "Date"), 100);
        column.setDateTimeFormat(Formatter.DEFAULT_DATETIME_FORMAT);
        column.setSortable(false);
        config.add(column);

        column = new ColumnConfig("action", Messages.get("label.action", "Action"), 100);
        column.setSortable(false);
        config.add(column);

        column = new ColumnConfig("userKey", Messages.get("label.user", "User"), 100);
        column.setSortable(false);
        config.add(column);

        column = new ColumnConfig("propertyName", Messages.get("label.propertyName", "Property name"), 100);
        column.setSortable(false);
        config.add(column);

        column = new ColumnConfig("path", Messages.get("label.path", "Path"), 100);
        column.setSortable(false);
        config.add(column);

        final ColumnModel cm = new ColumnModel(config);

        final Grid<GWTJahiaContentHistoryEntry> grid = new Grid<GWTJahiaContentHistoryEntry>(store, cm);
        grid.setBorders(true);
        grid.setAutoExpandColumn("path");
        grid.setTrackMouseOver(false);
        grid.setStateId("historyPagingGrid");
        grid.setStateful(true);
        grid.addListener(Events.Attach, new Listener<GridEvent<GWTJahiaContentHistoryEntry>>() {
            public void handleEvent(GridEvent<GWTJahiaContentHistoryEntry> be) {
                PagingLoadConfig config = new BasePagingLoadConfig();
                config.setOffset(0);
                config.setLimit(50);

                Map<String, Object> state = grid.getState();
                if (state.containsKey("offset")) {
                    int offset = (Integer) state.get("offset");
                    int limit = (Integer) state.get("limit");
                    config.setOffset(offset);
                    config.setLimit(limit);
                }
                if (state.containsKey("sortField")) {
                    config.setSortField((String) state.get("sortField"));
                    config.setSortDir(Style.SortDir.valueOf((String) state.get("sortDir")));
                }
                loader.load(config);
            }
        });
        grid.setLoadMask(true);
        grid.setBorders(true);
        grid.getSelectionModel().addSelectionChangedListener(new SelectionChangedListener<GWTJahiaContentHistoryEntry>() {

            @Override
            public void selectionChanged(SelectionChangedEvent<GWTJahiaContentHistoryEntry> gwtJahiaJobDetailSelectionChangedEvent) {
                selectedItems = gwtJahiaJobDetailSelectionChangedEvent.getSelection();
                updateDetails();
            }
        });

        ContentPanel listPanel = new ContentPanel();
        listPanel.setFrame(true);
        listPanel.setCollapsible(false);
        listPanel.setAnimCollapse(false);
        // panel.setIcon(Resources.ICONS.table());
        // panel.setHeading("");
        listPanel.setHeaderVisible(false);
        listPanel.setLayout(new FitLayout());
        listPanel.add(grid);
        listPanel.setSize(600, 350);
        listPanel.setBottomComponent(pagingToolBar);
        grid.getAriaSupport().setLabelledBy(listPanel.getId());
        add(listPanel);

        BorderLayoutData centerData = new BorderLayoutData(Style.LayoutRegion.CENTER);
        add(listPanel, centerData);

        FormPanel detailPanel = new FormPanel();
        detailPanel.setBorders(true);
        detailPanel.setBodyBorder(true);
        detailPanel.setHeaderVisible(true);
        detailPanel.setHeading(Messages.get("label.detailed", "Details"));
        detailPanel.setScrollMode(Style.Scroll.AUTOY);
        detailPanel.setLabelWidth(100);
        detailsPanel = detailPanel;

        BorderLayoutData southData = new BorderLayoutData(Style.LayoutRegion.SOUTH, 200);
        southData.setSplit(true);
        southData.setCollapsible(true);
        add(detailPanel, southData);

    }

    public void addDetail(String labelKey, String labelDefaultValue, Object value) {
        if (value != null) {
            TextField textField = new TextField();
            textField.setFieldLabel(Messages.get(labelKey, labelDefaultValue));
            textField.setReadOnly(true);
            if (value instanceof String) {
                textField.setValue(value);
            } else if (value instanceof Date) {
                textField.setValue(org.jahia.ajax.gwt.client.util.Formatter.getFormattedDate((Date) value));
            } else {
                textField.setValue(value.toString());
            }
            detailsPanel.add(textField, new FormData("98%"));
        }
    }

    public void addTimeDetail(String labelKey, String labelDefaultValue, Object value) {
        if (value == null) {
            addDetail(labelKey, labelDefaultValue, value);
        } else if (value instanceof Long) {
            Date date = new Date((Long) value);
            addDetail(labelKey, labelDefaultValue, date);
        } else {
            addDetail(labelKey, labelDefaultValue, value);
        }
    }

    public void updateDetails() {

        if (detailsPanel == null) {
            // maybe we clicked before it was created properly ?
            return;
        }

        if (selectedItems == null || selectedItems.size() == 0) {
            return;
        }

        detailsPanel.removeAll();
        if (selectedItems.size() == 1) {
            GWTJahiaContentHistoryEntry historyEntry = selectedItems.get(0);

            addDetail("label.user", "User key", historyEntry.getUserKey());
            addTimeDetail("label.date", "Date", historyEntry.getAction());
            addDetail("label.propertyName", "Property name", historyEntry.getPropertyName());
            addDetail("label.path", "Path", historyEntry.getPropertyName());
        } else {
            int nbHistoryEntries = 0;

            for (GWTJahiaContentHistoryEntry historyEntry : selectedItems) {
                nbHistoryEntries++;
            }
            detailsPanel.add(new HTML("<b>" + Messages.get("label.selectedHistoryEntryCount", "Number of selected history entries") + " :</b> " + nbHistoryEntries));
        }
        detailsPanel.layout();

    }

}
