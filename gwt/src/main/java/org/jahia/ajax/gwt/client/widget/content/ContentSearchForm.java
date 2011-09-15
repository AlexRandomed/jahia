/**
 * This file is part of Jahia, next-generation open source CMS:
 * Jahia's next-generation, open source CMS stems from a widely acknowledged vision
 * of enterprise application convergence - web, search, document, social and portal -
 * unified by the simplicity of web content management.
 *
 * For more information, please visit http://www.jahia.com.
 *
 * Copyright (C) 2002-2011 Jahia Solutions Group SA. All rights reserved.
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
 * Commercial and Supported Versions of the program (dual licensing):
 * alternatively, commercial and supported versions of the program may be used
 * in accordance with the terms and conditions contained in a separate
 * written agreement between you and Jahia Solutions Group SA.
 *
 * If you are unsure which license is appropriate for your use,
 * please contact the sales department at sales@jahia.com.
 */

package org.jahia.ajax.gwt.client.widget.content;

import com.allen_sauer.gwt.log.client.Log;
import com.extjs.gxt.ui.client.Style;
import com.extjs.gxt.ui.client.data.*;
import com.extjs.gxt.ui.client.event.*;
import com.extjs.gxt.ui.client.store.ListStore;
import com.extjs.gxt.ui.client.util.Margins;
import com.extjs.gxt.ui.client.widget.ContentPanel;
import com.extjs.gxt.ui.client.widget.HorizontalPanel;
import com.extjs.gxt.ui.client.widget.LayoutContainer;
import com.extjs.gxt.ui.client.widget.button.Button;
import com.extjs.gxt.ui.client.widget.form.*;
import com.extjs.gxt.ui.client.widget.layout.*;
import com.google.gwt.user.client.Window;
import org.jahia.ajax.gwt.client.core.BaseAsyncCallback;
import org.jahia.ajax.gwt.client.core.JahiaGWTParameters;
import org.jahia.ajax.gwt.client.data.GWTJahiaLanguage;
import org.jahia.ajax.gwt.client.data.GWTJahiaSearchQuery;
import org.jahia.ajax.gwt.client.data.toolbar.GWTManagerConfiguration;
import org.jahia.ajax.gwt.client.data.node.GWTJahiaNode;
import org.jahia.ajax.gwt.client.messages.Messages;
import org.jahia.ajax.gwt.client.service.content.ExistingFileException;
import org.jahia.ajax.gwt.client.service.content.JahiaContentManagementService;
import org.jahia.ajax.gwt.client.service.content.JahiaContentManagementServiceAsync;
import org.jahia.ajax.gwt.client.util.content.JCRClientUtils;
import org.jahia.ajax.gwt.client.util.content.actions.ManagerConfigurationFactory;
import org.jahia.ajax.gwt.client.util.icons.StandardIconsProvider;

/**
 * Form for searching repository content.
 * User: ktlili
 * Date: Feb 18, 2010
 * Time: 11:17:49 AM
 */
public class ContentSearchForm extends ContentPanel implements AbstractView.ContentSource {
    private TextField<String> searchField;
    private ContentPickerField pagePickerField;
    private ComboBox<GWTJahiaLanguage> langPickerField;
    private CheckBox inNameField;
    private CheckBox inTagField;
    private CheckBox inContentField;
    private CheckBox inFileField;
    private CheckBox inMetadataField;
    private ManagerLinker linker;
    private GWTManagerConfiguration config;

    public ContentSearchForm(GWTManagerConfiguration config) {
        this.config = config;
        setLayout(new RowLayout(Style.Orientation.VERTICAL));
        setWidth("100%");
        setHeight("100%");

        final FormPanel searchForm = new FormPanel();
        searchForm.setHeaderVisible(false);
        searchForm.setBorders(false);
        searchForm.setBodyBorder(false);
        searchField = new TextField<String>();
        searchField.setFieldLabel(Messages.get("search.label"));

        final Button ok = new Button("", new SelectionListener<ButtonEvent>() {
            public void componentSelected(ButtonEvent e) {
                doSearch();
            }
        });
        ok.setIcon(StandardIconsProvider.STANDARD_ICONS.search());

        final Button save = new Button("", new SelectionListener<ButtonEvent>() {
            public void componentSelected(ButtonEvent e) {
                saveSearch();
            }
        });
        save.setIcon(StandardIconsProvider.STANDARD_ICONS.savedSearch());
        save.setToolTip(Messages.get("saveSearch.label"));

        // main search field
        HorizontalPanel mainField = new HorizontalPanel();
        mainField.setSpacing(2);
        LayoutContainer formLayoutContainer = new LayoutContainer();
        FormLayout flayout = new FormLayout();
        flayout.setLabelWidth(50);
        formLayoutContainer.setLayout(flayout);
        formLayoutContainer.add(searchField);
        mainField.add(formLayoutContainer);
        mainField.add(ok);
        mainField.add(save);
        add(mainField, new RowData(1, -1, new Margins(0)));

        // advanced part
        FieldSet fieldSet = new FieldSet();
        fieldSet.setHeading(Messages.get("label.detailed", "Advanced"));
        FormLayout layout = new FormLayout();
        layout.setLabelWidth(70);

        fieldSet.setLayout(layout);
        fieldSet.setCollapsible(false);

        // page picker field
        pagePickerField = createPageSelectorField();
        fieldSet.add(pagePickerField);
        if (!config.isDisplaySearchInPage()) {
            pagePickerField.hide();
        }

        // lang picker
        langPickerField = createLanguageSelectorField();
        fieldSet.add(langPickerField);

        searchForm.add(fieldSet);

        final CheckBoxGroup scopeCheckGroup = new CheckBoxGroup();
        scopeCheckGroup.setOrientation(Style.Orientation.VERTICAL);
        scopeCheckGroup.setFieldLabel(Messages.get("label_searchScope","Search scope"));
        // scope name field
        inNameField = createNameField();
        scopeCheckGroup.add(inNameField);

        // scope tag field
        inTagField = createTagField();
        scopeCheckGroup.add(inTagField);

        // scope metadata field
        inMetadataField = createMetadataField();
        scopeCheckGroup.add(inMetadataField);
        inMetadataField.hide();

        // scope content field
        inContentField = createContentField();
        scopeCheckGroup.add(inContentField);

        // scope file field
        inFileField = createFileField();
        scopeCheckGroup.add(inFileField);

        fieldSet.add(scopeCheckGroup,new FormData("-20"));

        setWidth("100%");
        setFrame(true);
        setCollapsible(false);
        setBodyBorder(false);
        setHeaderVisible(false);
        getHeader().setBorders(false);
        add(searchForm, new RowData(1, 1, new Margins(0, 0, 20, 0)));
    }

    /**
     * init with linker
     *
     * @param linker
     */
    public void initWithLinker(ManagerLinker linker) {
        this.linker = linker;
    }

    /**
     * Create new page picker field
     *
     * @return
     */
    private ContentPickerField createPageSelectorField() {
        ContentPickerField field = new ContentPickerField(null, null, null, null, ManagerConfigurationFactory.PAGEPICKER, false);
        field.setFieldLabel(Messages.get("label.pagePicker", "Pages"));
        return field;
    }

    /**
     * Create a new scope fields group selector field
     *
     * @return
     */
    private CheckBox createNameField() {
        CheckBox field = new CheckBox();
        field.setFieldLabel(Messages.get("label.name", "Name & Metadata"));
        field.setBoxLabel(field.getFieldLabel());
        field.setName("name");
        field.setValue(true);
        return field;
    }

    /**
     * Create tag field
     *
     * @return
     */
    private CheckBox createTagField() {
        CheckBox field = new CheckBox();
        field.setFieldLabel(Messages.get("label.tags", "Tags"));
        field.setBoxLabel(field.getFieldLabel());
        field.setName("tag");
        field.setValue(true);
        if (!config.isDisplaySearchInTag()) {
            field.hide();
        }
        return field;
    }

    /**
     * Create metadataFied
     *
     * @return
     */
    private CheckBox createMetadataField() {
        CheckBox field = new CheckBox();
        field.setFieldLabel(Messages.get("label.metadata", "Metadata"));
        field.setBoxLabel(field.getFieldLabel());
        field.setName("metadata");
        field.setValue(true);
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
        field.setFieldLabel(Messages.get("label.content", "Content"));
        field.setBoxLabel(field.getFieldLabel());
        field.setName("content");
        field.setValue(config.isSearchInContent() ? true : false);
        if (!config.isDisplaySearchInContent()) {
            field.hide();
        }
        return field;
    }

    /**
     * Create file field
     *
     * @return
     */
    private CheckBox createFileField() {
        CheckBox field = new CheckBox();
        field.setFieldLabel(Messages.get("fileMenu.label", "File"));
        field.setBoxLabel(field.getFieldLabel());        
        field.setName("file");
        field.setValue(config.isSearchInFile() ? true : false);
        if (!config.isDisplaySearchInFile()) {
            field.hide();
        }
        return field;
    }


    /**
     * Create language field
     *
     * @return
     */
    private ComboBox<GWTJahiaLanguage> createLanguageSelectorField() {
        final ComboBox<GWTJahiaLanguage> combo = new ComboBox<GWTJahiaLanguage>();
        combo.setFieldLabel(Messages.get("label.language", "Language"));
        combo.setAllowBlank(true);
        combo.setStore(new ListStore<GWTJahiaLanguage>());
        combo.setDisplayField("displayName");
        combo.setTemplate(getLangSwitchingTemplate());
        combo.setTypeAhead(true);
        combo.setTriggerAction(ComboBox.TriggerAction.ALL);
        combo.setForceSelection(true);
        combo.getStore().removeAll();
        combo.getStore().add(JahiaGWTParameters.getSiteLanguages());

        return combo;
    }


    /**
     * Method used by seach form
     */
    public void doSearch() {
        GWTJahiaSearchQuery gwtJahiaSearchQuery = getCurrentQuery();

        int limit = 500;
        int offset = 0;
        Log.debug(searchField.getValue() + "," + pagePickerField.getValue() + "," + langPickerField.getValue() + "," + inNameField.getValue() + "," + inTagField.getValue());
        JahiaContentManagementService.App.getInstance().search(gwtJahiaSearchQuery, limit, offset, config.isShowOnlyNodesWithTemplates(), new BaseAsyncCallback<PagingLoadResult<GWTJahiaNode>>() {
            public void onSuccess(PagingLoadResult<GWTJahiaNode> gwtJahiaNodePagingLoadResult) {
                linker.getTopRightObject().setProcessedContent(gwtJahiaNodePagingLoadResult.getData(), ContentSearchForm.this);
                linker.loaded();
            }

            public void onApplicationFailure(Throwable throwable) {
                Log.debug("error while searching nodes due to:", throwable);
                linker.getTopRightObject().setProcessedContent(null, ContentSearchForm.this);
                linker.loaded();
            }
        });

    }

    public void refreshTable() {
        doSearch();
    }

    /**
     * Get current query
     *
     * @return
     */
    private GWTJahiaSearchQuery getCurrentQuery() {
        GWTJahiaSearchQuery gwtJahiaSearchQuery = new GWTJahiaSearchQuery();
        gwtJahiaSearchQuery.setQuery(searchField.getValue());
        gwtJahiaSearchQuery.setPages(pagePickerField.getValue());
        gwtJahiaSearchQuery.setLanguage(langPickerField.getValue());
        gwtJahiaSearchQuery.setInName(inNameField.getValue());
        gwtJahiaSearchQuery.setInTags(inTagField.getValue());
        gwtJahiaSearchQuery.setInContents(inContentField.getValue());
        gwtJahiaSearchQuery.setInFiles(inFileField.getValue());
        gwtJahiaSearchQuery.setInMetadatas(inMetadataField.getValue());
        gwtJahiaSearchQuery.setFilters(config.getFilters());
        gwtJahiaSearchQuery.setNodeTypes(config.getNodeTypes());
        gwtJahiaSearchQuery.setFolderTypes(config.getFolderTypes());
        gwtJahiaSearchQuery.setOriginSiteUuid(JahiaGWTParameters.getSiteUUID());
        return gwtJahiaSearchQuery;
    }

    /**
     * Save search
     */
    public void saveSearch() {
        GWTJahiaSearchQuery query = getCurrentQuery();
        if (query != null && query.getQuery().length() > 0) {
            String name = Window.prompt(Messages.get("saveSearchName.label", "Please enter a name for this search"), JCRClientUtils.cleanUpFilename(query.getQuery()));
            if (name != null && name.length() > 0) {
                name = JCRClientUtils.cleanUpFilename(name);
                final JahiaContentManagementServiceAsync service = JahiaContentManagementService.App.getInstance();
                service.saveSearch(query,null, name, false, new BaseAsyncCallback<GWTJahiaNode>() {
                    public void onSuccess(GWTJahiaNode o) {
                        Log.debug("saved.");
                    }

                    public void onApplicationFailure(Throwable throwable) {
                        if (throwable instanceof ExistingFileException) {
                            Window.alert(Messages.get("fm_inUseSaveSearch", "The entered name is already in use."));
                        } else {
                            Log.error("error", throwable);
                        }
                    }


                });
            }
        }

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
