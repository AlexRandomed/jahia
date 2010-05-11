package org.jahia.ajax.gwt.client.widget.edit.sidepanel;

import com.allen_sauer.gwt.log.client.Log;
import com.extjs.gxt.ui.client.Style;
import com.extjs.gxt.ui.client.data.*;
import com.extjs.gxt.ui.client.event.*;
import com.extjs.gxt.ui.client.store.ListStore;
import com.extjs.gxt.ui.client.util.Margins;
import com.extjs.gxt.ui.client.widget.ContentPanel;
import com.extjs.gxt.ui.client.widget.button.Button;
import com.extjs.gxt.ui.client.widget.form.*;
import com.extjs.gxt.ui.client.widget.grid.*;
import com.extjs.gxt.ui.client.widget.grid.ColumnData;
import com.extjs.gxt.ui.client.widget.layout.*;
import com.extjs.gxt.ui.client.widget.toolbar.PagingToolBar;
import com.google.gwt.user.client.rpc.AsyncCallback;
import org.jahia.ajax.gwt.client.data.GWTJahiaLanguage;
import org.jahia.ajax.gwt.client.data.GWTJahiaSearchQuery;
import org.jahia.ajax.gwt.client.data.node.GWTJahiaNode;
import org.jahia.ajax.gwt.client.data.toolbar.GWTColumn;
import org.jahia.ajax.gwt.client.messages.Messages;
import org.jahia.ajax.gwt.client.service.content.JahiaContentManagementService;
import org.jahia.ajax.gwt.client.util.content.actions.ManagerConfigurationFactory;
import org.jahia.ajax.gwt.client.util.icons.ContentModelIconProvider;
import org.jahia.ajax.gwt.client.util.icons.StandardIconsProvider;
import org.jahia.ajax.gwt.client.widget.NodeColumnConfigList;
import org.jahia.ajax.gwt.client.widget.content.ContentPickerField;
import org.jahia.ajax.gwt.client.widget.edit.EditLinker;
import org.jahia.ajax.gwt.client.widget.edit.EditModeDNDListener;
import org.jahia.ajax.gwt.client.widget.edit.EditModeDragSource;
import org.jahia.ajax.gwt.client.data.toolbar.GWTSidePanelTab;

import java.util.ArrayList;
import java.util.List;

/**
 * Search tab item for the side panel for performing simple queries in the content repository.
 * User: toto
 * Date: Dec 21, 2009
 * Time: 3:14:11 PM
 */
class SearchTabItem extends SidePanelTabItem {
    protected ListStore<GWTJahiaNode> contentStore;
    protected DisplayGridDragSource displayGridSource;
    private TextField<String> searchField;
    private ContentPickerField pagePickerField;
    private ComboBox<GWTJahiaLanguage> langPickerField;
    private CheckBox inNameField;
    private CheckBox inTagField;
    private CheckBox inContentField;
    private CheckBox inFileField;
    private CheckBox inMetadataField;
    private Grid<GWTJahiaNode> grid;
    final PagingLoader<PagingLoadResult<GWTJahiaNode>> loader;

    public SearchTabItem(GWTSidePanelTab config) {
        super(config);
        final String nbResultsAsStrg = config.getParams() == null ? null :config.getParams().get("numberResults");
        final int nbResults = nbResultsAsStrg == null? 15 : Integer.parseInt(nbResultsAsStrg);
        VBoxLayout l = new VBoxLayout();
        l.setVBoxLayoutAlign(VBoxLayout.VBoxLayoutAlign.STRETCH);
        setLayout(new FitLayout());

        setIcon(StandardIconsProvider.STANDARD_ICONS.query());

        final FormPanel searchForm = new FormPanel();
        searchForm.setHeaderVisible(false);
        searchForm.setBorders(false);
        searchForm.setBodyBorder(false);
        searchField = new TextField<String>();
        searchField.setFieldLabel(Messages.getResource("fm_search"));
        searchField.addListener(KeyboardEvents.Enter, new Listener<ComponentEvent>() {
            public void handleEvent(ComponentEvent be) {
                // grid.mask("Loading", "x-mask-loading");
                contentStore.removeAll();
                loader.load(0,nbResults);
            }
        });
        final Button ok = new Button(Messages.getResource("fm_search"), new SelectionListener<ButtonEvent>() {
            public void componentSelected(ButtonEvent e) {
                //  grid.mask("Loading", "x-mask-loading");
                contentStore.removeAll();
                loader.load(0, nbResults);
            }
        });
        ok.setIconStyle("gwt-toolbar-icon-savedSearch");

        final Button drag = new Button(Messages.getResource("em_drag"));
        EditModeDragSource querySource = new EditModeDragSource(drag) {
            @Override
            protected void onDragStart(DNDEvent e) {
                e.setCancelled(false);
                e.getStatus().update(searchField.getValue());
                e.getStatus().setStatus(true);
                e.setData(searchField);
                e.getStatus().setData(EditModeDNDListener.SOURCE_TYPE, EditModeDNDListener.QUERY_SOURCE_TYPE);
                e.getStatus().setData(EditModeDNDListener.SOURCE_QUERY, getGWTJahiaSearchQuery());
                super.onDragStart(e);
            }
        };

        searchForm.add(searchField);
        FieldSet fieldSet = new FieldSet();
        fieldSet.setHeading(Messages.get("label_advanced", "Advanced"));
        FormLayout layout = new FormLayout();
        layout.setLabelWidth(70);
        fieldSet.setLayout(layout);
        fieldSet.setCollapsible(false);

        // page picker field
        pagePickerField = createPageSelectorField();
        fieldSet.add(pagePickerField);

        // lang picker
        langPickerField = createLanguageSelectorField();
        fieldSet.add(langPickerField);
        searchForm.add(fieldSet);

        final CheckBoxGroup scopeCheckGroup = new CheckBoxGroup();
        scopeCheckGroup.setOrientation(Style.Orientation.VERTICAL);
        scopeCheckGroup.setFieldLabel(Messages.get("label_searchScope", "Search scope"));
        // scope name field
        inNameField = createNameField();
        scopeCheckGroup.add(inNameField);


        // scope tag field
        inTagField = createTagField();
        scopeCheckGroup.add(inTagField);

        // scope metadata field
        inMetadataField = createMetadataField();
        scopeCheckGroup.add(inMetadataField);

        // scope content field
        inContentField = createContentField();
        scopeCheckGroup.add(inContentField);

        // scope file field
        inFileField = createFileField();
        scopeCheckGroup.add(inFileField);

        fieldSet.add(scopeCheckGroup, new FormData("-20"));


        searchForm.addButton(ok);
        searchForm.addButton(drag);


        ContentPanel panel = new ContentPanel();
        panel.setLayout(new RowLayout(Style.Orientation.VERTICAL));
        panel.setWidth("100%");
        panel.setHeight("100%");
        panel.setFrame(true);
        panel.setCollapsible(false);
        panel.setHeaderVisible(false);
        panel.add(searchForm, new RowData(1, -1, new Margins(0)));


        RpcProxy<PagingLoadResult<GWTJahiaNode>> proxy = new RpcProxy<PagingLoadResult<GWTJahiaNode>>() {
            @Override
            public void load(Object loadConfig, AsyncCallback<PagingLoadResult<GWTJahiaNode>> callback) {
                doSearch((PagingLoadConfig) loadConfig, callback);
            }
        };

        // loader
        loader = new BasePagingLoader<PagingLoadResult<GWTJahiaNode>>(proxy);
        loader.setRemoteSort(true);
        final PagingToolBar toolBar = new PagingToolBar(10);
        toolBar.bind(loader);
        contentStore = new ListStore<GWTJahiaNode>(loader);

        List<GWTColumn> columnNames = new ArrayList<GWTColumn>();
        columnNames.add(new GWTColumn("icon","icon",40));
        columnNames.add(new GWTColumn("displayName",Messages.getResource("fm_info_name"),280));
        final NodeColumnConfigList columnConfigList = new NodeColumnConfigList(columnNames);
        columnConfigList.init();

        final Grid<GWTJahiaNode> grid = new Grid<GWTJahiaNode>(contentStore, new ColumnModel(columnConfigList));

        //contentContainer.add(grid);

        //contentVBoxData.setFlex(4);
        ContentPanel gridPanel = new ContentPanel();
        gridPanel.setLayout(new FitLayout());
        gridPanel.setBottomComponent(toolBar);
        gridPanel.setHeaderVisible(false);
        gridPanel.setFrame(false);
        gridPanel.setBodyBorder(false);
        gridPanel.setBorders(false);
        gridPanel.add(grid);

        panel.add(gridPanel, new RowData(1, 1, new Margins(0, 0, 20, 0)));
        add(panel);
        displayGridSource = new DisplayGridDragSource(grid);
    }

    @Override
    public void initWithLinker(EditLinker linker) {
        super.initWithLinker(linker);
        displayGridSource.addDNDListener(editLinker.getDndListener());
    }

    /**
     * Create a new date selector field
     *
     * @return
     */
    private Field createDateSelectorField() {
        return null;
    }

    /**
     * Create new page picker field
     *
     * @return
     */
    private ContentPickerField createPageSelectorField() {
        ContentPickerField field = new ContentPickerField(Messages.get("picker_link_header", "Page picker"),
                Messages.get("picker_link_selection", "Selected page"), null, "/", "", null, "",
                ManagerConfigurationFactory.LINKPICKER, false, false);
        field.setFieldLabel(Messages.get("picker_link_header", "Pages"));
        return field;
    }

    /**
     * Create a new scope fields group selector field
     *
     * @return
     */
    private CheckBox createNameField() {
        CheckBox field = new CheckBox();
        field.setValue(true);
        field.setFieldLabel(Messages.get("label_name", "Name & Metadata"));
        field.setBoxLabel(field.getFieldLabel());
        field.setName("name");
        return field;
    }

    /**
     * Create tag field
     *
     * @return
     */
    private CheckBox createTagField() {
        CheckBox field = new CheckBox();
        field.setValue(true);
        field.setFieldLabel(Messages.get("label_tag", "Tags"));
        field.setBoxLabel(field.getFieldLabel());
        field.setName("tag");
        field.hide();
        return field;
    }

    /**
     * Create metadataFied
     *
     * @return
     */
    private CheckBox createMetadataField() {
        CheckBox field = new CheckBox();
        field.setValue(true);
        field.setFieldLabel(Messages.get("label_metadata", "Metadata"));
        field.setBoxLabel(field.getFieldLabel());
        field.setName("metadata");
        field.hide();
        return field;
    }

    /**
     * Create content field
     *
     * @return
     */
    private CheckBox createContentField() {
        CheckBox field = new CheckBox();
        field.setValue(true);
        field.setFieldLabel(Messages.get("label_content", "Content"));
        field.setBoxLabel(field.getFieldLabel());
        field.setName("content");
        return field;
    }

    /**
     * Create file field
     *
     * @return
     */
    private CheckBox createFileField() {
        CheckBox field = new CheckBox();
        field.setValue(true);
        field.setFieldLabel(Messages.get("label_file", "File"));
        field.setBoxLabel(field.getFieldLabel());
        field.setName("file");
        return field;
    }


    /**
     * Create language field
     *
     * @return
     */
    private ComboBox<GWTJahiaLanguage> createLanguageSelectorField() {
        final ComboBox<GWTJahiaLanguage> combo = new ComboBox<GWTJahiaLanguage>();
        combo.setFieldLabel("Language");
        combo.setStore(new ListStore<GWTJahiaLanguage>());
        combo.setDisplayField("displayName");
        combo.setTemplate(getLangSwitchingTemplate());
        combo.setTypeAhead(true);
        combo.setTriggerAction(ComboBox.TriggerAction.ALL);
        combo.setForceSelection(true);
        JahiaContentManagementService.App.getInstance().getSiteLanguages(new AsyncCallback<List<GWTJahiaLanguage>>() {
            public void onSuccess(List<GWTJahiaLanguage> gwtJahiaLanguages) {
                combo.getStore().removeAll();
                combo.getStore().add(gwtJahiaLanguages);
            }

            public void onFailure(Throwable throwable) {
                Log.error("Error while loading languages");
            }
        });
        return combo;
    }


    /**
     * Method used by seach form
     */
    private void doSearch(PagingLoadConfig loadConfig, AsyncCallback<PagingLoadResult<GWTJahiaNode>> callback) {
        GWTJahiaSearchQuery gwtJahiaSearchQuery = getGWTJahiaSearchQuery();
        int limit = 500;
        int offset = 0;
        if (loadConfig != null) {
            limit = loadConfig.getLimit();
            offset = loadConfig.getOffset();
        }

        Log.debug(searchField.getValue() + "," + pagePickerField.getValue() + "," + langPickerField.getValue() + "," +
                inNameField.getValue() + "," + inTagField.getValue());
        JahiaContentManagementService.App.getInstance().search(gwtJahiaSearchQuery, limit, offset, callback);

    }

    /**
     * Get the GWTJahiaSearchQuery that corresponds to what is selected in fields
     *
     * @return
     */
    private GWTJahiaSearchQuery getGWTJahiaSearchQuery() {
        GWTJahiaSearchQuery gwtJahiaSearchQuery = new GWTJahiaSearchQuery();
        gwtJahiaSearchQuery.setQuery(searchField.getValue());
        gwtJahiaSearchQuery.setPages(pagePickerField.getValue());
        gwtJahiaSearchQuery.setLanguage(langPickerField.getValue());
        gwtJahiaSearchQuery.setInName(inNameField.getValue());
        gwtJahiaSearchQuery.setInTags(inTagField.getValue());
        gwtJahiaSearchQuery.setInContents(inContentField.getValue());
        gwtJahiaSearchQuery.setInFiles(inFileField.getValue());
        gwtJahiaSearchQuery.setInMetadatas(inMetadataField.getValue());
        return gwtJahiaSearchQuery;
    }

    /**
     * LangSwithcing template
     *
     * @return
     */
    private static native String getLangSwitchingTemplate()  /*-{
    return  [
    '<tpl for=".">',
    '<div class="x-combo-list-item"><img src="{image}"/> {displayName}</div>',
    '</tpl>'
    ].join("");
  }-*/;

}