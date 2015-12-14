/**
 * ==========================================================================================
 * =                   JAHIA'S DUAL LICENSING - IMPORTANT INFORMATION                       =
 * ==========================================================================================
 *
 *                                 http://www.jahia.com
 *
 *     Copyright (C) 2002-2016 Jahia Solutions Group SA. All rights reserved.
 *
 *     THIS FILE IS AVAILABLE UNDER TWO DIFFERENT LICENSES:
 *     1/GPL OR 2/JSEL
 *
 *     1/ GPL
 *     ==================================================================================
 *
 *     IF YOU DECIDE TO CHOOSE THE GPL LICENSE, YOU MUST COMPLY WITH THE FOLLOWING TERMS:
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
 *     describing the FLOSS exception, also available here:
 *     http://www.jahia.com/license"
 *
 *     2/ JSEL - Commercial and Supported Versions of the program
 *     ===================================================================================
 *
 *     IF YOU DECIDE TO CHOOSE THE JSEL LICENSE, YOU MUST COMPLY WITH THE FOLLOWING TERMS:
 *
 *     Alternatively, commercial and supported versions of the program - also known as
 *     Enterprise Distributions - must be used in accordance with the terms and conditions
 *     contained in a separate written agreement between you and Jahia Solutions Group SA.
 *
 *     If you are unsure which license is appropriate for your use,
 *     please contact the sales department at sales@jahia.com.
 *
 */
package org.jahia.ajax.gwt.client.widget.definition;

import com.allen_sauer.gwt.log.client.Log;
import com.extjs.gxt.ui.client.Style;
import com.extjs.gxt.ui.client.event.*;
import com.extjs.gxt.ui.client.util.Margins;
import com.extjs.gxt.ui.client.widget.Component;
import com.extjs.gxt.ui.client.widget.Dialog;
import com.extjs.gxt.ui.client.widget.LayoutContainer;
import com.extjs.gxt.ui.client.widget.MessageBox;
import com.extjs.gxt.ui.client.widget.button.Button;
import com.extjs.gxt.ui.client.widget.form.*;
import com.extjs.gxt.ui.client.widget.layout.*;
import com.google.gwt.user.client.Command;
import com.google.gwt.user.client.DeferredCommand;
import org.jahia.ajax.gwt.client.core.BaseAsyncCallback;
import org.jahia.ajax.gwt.client.core.JahiaGWTParameters;
import org.jahia.ajax.gwt.client.data.GWTChoiceListInitializer;
import org.jahia.ajax.gwt.client.data.GWTJahiaValueDisplayBean;
import org.jahia.ajax.gwt.client.data.definition.*;
import org.jahia.ajax.gwt.client.data.node.GWTBitSet;
import org.jahia.ajax.gwt.client.data.node.GWTJahiaNode;
import org.jahia.ajax.gwt.client.messages.Messages;
import org.jahia.ajax.gwt.client.service.content.JahiaContentManagementService;
import org.jahia.ajax.gwt.client.util.definition.FormFieldCreator;
import org.jahia.ajax.gwt.client.widget.content.ContentPickerField;
import org.jahia.ajax.gwt.client.widget.content.MultipleTextField;
import org.jahia.ajax.gwt.client.widget.form.tag.TagField;

import java.util.*;


/**
 * This is a property editor that allows to edit properties of a JCR node.
 */
public class PropertiesEditor extends FormPanel {
    private List<GWTJahiaNodeType> nodeTypes = null;
    private List<GWTJahiaNodeType> mixin = null;
    private Map<String, GWTChoiceListInitializer> choiceListInitializersValues;
    private Map<String, List<GWTJahiaNodePropertyValue>> defaultValues;
    private Map<String, GWTJahiaNodeProperty> currentProperties = null;
    private Map<String, GWTJahiaNodeProperty> originalProperties = null;
    private Map<String, PropertyAdapterField> fields;
    private Map<String, FieldSet> fieldSets;
    private List<FieldSet> orderingListFieldSet = new ArrayList<FieldSet>();
    private Map<String, GWTJahiaItemDefinition> propertyDefinitions = new HashMap<String, GWTJahiaItemDefinition>();
    private boolean isMultipleEdit = false;
    private boolean viewInheritedItems = true;
    private boolean viewCopyToAllLangs = false;
    private List<String> excludedItems;
    private List<String> excludedTypes;
    private List<String> dataType;
    private boolean isWriteable = true;
    private boolean isNonI18NWriteable = true;
    private boolean fieldSetGrouping = false;
    private Set<String> addedTypes = new HashSet<String>();
    private Set<String> removedTypes = new HashSet<String>();
    private Set<String> externalMixin = new HashSet<String>();
    private String locale = "";
    private GWTBitSet permissions;
    private LangPropertiesEditor translationSource;

    public void setPermissions(GWTBitSet permissions) {
        this.permissions = permissions;
    }

    public boolean isDisplayHiddenProperties() {
        return displayHiddenProperties;
    }

    public void setDisplayHiddenProperties(boolean displayHiddenProperties) {
        this.displayHiddenProperties = displayHiddenProperties;
    }

    private boolean displayHiddenProperties = false;

    /**
     * default constructor of PropertiesEditor
     * call {@link this.renderNewFormPanel} to render the PropertiesEditor.
     * @param nodeTypes List of nodeTypes
     * @param properties List of properties already set
     * @param datatype datatype of content, set by itemtype in definition, filter property fields to be displayed
     */
    public PropertiesEditor(List<GWTJahiaNodeType> nodeTypes, Map<String, GWTJahiaNodeProperty> properties,
                            List<String> datatype) {
        super();
        this.nodeTypes = nodeTypes;
        this.dataType = datatype;
        originalProperties = properties;
        cloneProperties();
    }

    public void setNodeTypes(List<GWTJahiaNodeType> nodeTypes) {
        this.nodeTypes = nodeTypes;
    }

    public void setMixin(List<GWTJahiaNodeType> mixin) {
        this.mixin = mixin;
    }

    public void setChoiceListInitializersValues(Map<String, GWTChoiceListInitializer> initializersValues) {
        this.choiceListInitializersValues = initializersValues;
    }

    public void setDefaultValues(Map<String, List<GWTJahiaNodePropertyValue>> defaultValues) {
        this.defaultValues = defaultValues;
    }

    public void setMultipleEdit(boolean multipleEdit) {
        isMultipleEdit = multipleEdit;
    }

    public void setViewInheritedItems(boolean viewInheritedItems) {
        this.viewInheritedItems = viewInheritedItems;
    }

    public void setViewCopyToAllLangs(boolean viewCopyToAllLangs) {
        this.viewCopyToAllLangs = viewCopyToAllLangs;
    }

    public void setExcludedItems(List<String> excludedItems) {
        this.excludedItems = excludedItems;
    }

    public void setExcludedTypes(List<String> excludedTypes) {
        this.excludedTypes = excludedTypes;
    }

    public void setWriteable(boolean writeable) {
        isWriteable = writeable;
    }

    public void setNonI18NWriteable(boolean nonI18NWriteable) {
        isNonI18NWriteable = nonI18NWriteable;
    }

    public void setFieldSetGrouping(boolean fieldSetGrouping) {
        this.fieldSetGrouping = fieldSetGrouping;
    }

    public void renderNewFormPanel() {
        setLabelWidth(180);
        setLabelAlign(LabelAlign.TOP);
        setPadding(5);
        setCollapsible(false);
        setFrame(false);
        setAnimCollapse(false);
        setBorders(false);
        setBodyBorder(false);
        setHeaderVisible(false);
        setScrollMode(Style.Scroll.AUTO);
        setButtonAlign(Style.HorizontalAlignment.CENTER);
        removeAll();
        fields = new HashMap<String, PropertyAdapterField>();
        fieldSets = new HashMap<String, FieldSet>();
        List<String> supertypes = new ArrayList<String>();
        for (GWTJahiaNodeType nodeType : nodeTypes) {
            supertypes.addAll(nodeType.getSuperTypes());
            if (mixin != null && mixin.contains(nodeType)) {
                continue;
            }

            addItems(nodeType, nodeType.getItems(), false, fieldSetGrouping, null);
            if (viewInheritedItems) {
                addItems(nodeType, nodeType.getInheritedItems(), false, fieldSetGrouping, null);
            }
        }
        if (mixin != null) {
            for (GWTJahiaNodeType mix : mixin) {
                if (supertypes.contains(mix.getName()) || mix.getSuperTypes().contains("jmix:templateMixin")) {
                    continue;
                }

                List<GWTJahiaItemDefinition> list = new ArrayList<GWTJahiaItemDefinition>(mix.getItems());
                if (viewInheritedItems) {
                    list.addAll(mix.getInheritedItems());
                }
                addItems(mix, list, true, fieldSetGrouping, null);
            }
        }
    }


    /**
     * Add item
     *
     * @param nodeType
     * @param items
     * @param optional
     * @param fieldSetGrouping
     */
    private void addItems(final GWTJahiaNodeType nodeType, List<GWTJahiaItemDefinition> items,
                          boolean optional, boolean fieldSetGrouping, Field remoteField) {

        FieldSet fieldSet = null;
        if (remoteField != null) {
            fieldSet = (FieldSet) remoteField.getParent();
        }
        for (final GWTJahiaItemDefinition definition : items) {

            final FormLayout fl = new FormLayout();
            fl.setLabelWidth(0);

            if ((excludedTypes != null && excludedTypes.contains(definition.getDeclaringNodeType())) ||
                    (excludedItems != null && excludedItems.contains(definition.getName()))) {
                continue;
            }
            if (remoteField == null && dataType != null && !dataType.isEmpty() && !dataType.contains(definition.getDataType())) {
                continue;
            }

            List<String> defaultedProperties = new ArrayList<String>();
            if (!currentProperties.containsKey(definition.getName())) {
                GWTJahiaNodeProperty property = new GWTJahiaNodeProperty();
                property.setName(definition.getName());
                property.setValues(new ArrayList<GWTJahiaNodePropertyValue>());
                if (!definition.isNode()) {
                    property.setMultiple(((GWTJahiaPropertyDefinition) definition).isMultiple());
                }
                currentProperties.put(definition.getName(), property);
            }


            final GWTJahiaNodeProperty gwtJahiaNodeProperty = currentProperties.get(definition.getName());
            String key = definition.getOverrideDeclaringNodeType() + "." + definition.getName();
            GWTChoiceListInitializer choiceListInitializer = choiceListInitializersValues != null ? choiceListInitializersValues.get(key) : null;
            List<GWTJahiaNodePropertyValue> propertyDefaultValues = null;
            if (this.defaultValues != null) {
                if (originalProperties.isEmpty() || originalProperties.containsKey(definition.getName()) || !nodeTypes.contains(nodeType)) {
                    propertyDefaultValues = this.defaultValues.get(key);
                }
            }
            if (propertyDefaultValues != null && gwtJahiaNodeProperty.getValues().size() == 0 &&
                    (originalProperties.isEmpty() || originalProperties.containsKey(definition.getName()))) {
                defaultedProperties.add(definition.getName());
            }
            Field<?> field = FormFieldCreator.createField(definition, gwtJahiaNodeProperty, choiceListInitializer, displayHiddenProperties, permissions, propertyDefaultValues);
            propertyDefinitions.put(gwtJahiaNodeProperty.getName(), definition);
            if (field != null) {
                String declaringNodeTypeLabel = optional ? nodeType.getLabel() : definition.getDeclaringNodeTypeLabel();
                if (fieldSet == null || fieldSetGrouping &&
                        (!fieldSet.getId().equals(declaringNodeTypeLabel))) {
                    fieldSet = new FieldSet();
                    fieldSet.setId(declaringNodeTypeLabel);
                    fieldSet.setHeadingHtml(declaringNodeTypeLabel);
                    fieldSet.setLayout(fl);
                    fieldSets.put(optional ? nodeType.getName() : definition.getDeclaringNodeType(), fieldSet);
                    add(fieldSet);
                }
                if (!isWriteable || (!isNonI18NWriteable && !definition.isNode() && !((GWTJahiaPropertyDefinition)definition).isInternationalized())) {
                    field.setReadOnly(true);
                    if (field instanceof TriggerField) {
                        ((TriggerField) field).setHideTrigger(true);
                    }
                }

                final PropertyAdapterField adapterField = new PropertyAdapterField(field, definition, gwtJahiaNodeProperty);
                adapterField.setWidth("98%");
                adapterField.setStyleAttribute("padding-left", "0");
                fields.put(field.getName(), adapterField);
                FormData fd = new FormData("98%");
                fd.setMargins(new Margins(0));
                if (remoteField != null) {
                    int i = 1;
                    for (Component component : fieldSet.getItems()) {
                        if (component.equals(remoteField)) {
                            fieldSet.insert(adapterField, i, fd);
                            break;
                        }
                        i++;
                    }
                }   else {
                    fieldSet.add(adapterField, fd);
                }
                fieldSet.layout();
                if (optional) {
                    boolean isOrderingList = "jmix:orderedList".equalsIgnoreCase(definition.getDeclaringNodeType());
                    if (isWriteable && isNonI18NWriteable) {
                        fieldSet.setCollapsible(true);
                        if (!isOrderingList) {
                            fieldSet.setCheckboxToggle(true);
                            if ((nodeTypes.contains(nodeType) && !removedTypes.contains(nodeType.getName())) || addedTypes.contains(nodeType.getName())) {
                                fieldSet.setExpanded(true);
                            } else {
                                fieldSet.setExpanded(false);
                            }
                            fieldSet.addListener(Events.Collapse, new Listener<FieldSetEvent>() {
                                public void handleEvent(FieldSetEvent componentEvent) {
                                    removedTypes.add(nodeType.getName());
                                    addedTypes.remove(nodeType.getName());
                                    final FieldSet  fs = (FieldSet) componentEvent.getBoxComponent();
                                    for (Component component : fs.getItems()) {
                                        if (component instanceof PropertyAdapterField) {
                                            PropertyAdapterField adapterField = (PropertyAdapterField) component;
                                            if (adapterField.getField() instanceof ComboBox) {
                                                removeExternalMixin(((ComboBox) adapterField.getField()).getSelection(),adapterField);
                                            }
                                        }
                                        component.setData("addedField", null);
                                    }
                                }
                            });
                            fieldSet.addListener(Events.Expand, new Listener<FieldSetEvent>() {
                                public void handleEvent(FieldSetEvent componentEvent) {
                                    addedTypes.add(nodeType.getName());
                                    removedTypes.remove(nodeType.getName());
                                    final FieldSet fs = (FieldSet) componentEvent.getBoxComponent();
                                    final List<Component> w = new ArrayList<Component>();
                                    w.addAll(fs.getItems());
                                    fs.removeAll();
                                    DeferredCommand.addCommand(new Command() {
                                        public void execute() {
                                            for (Component component : w) {
                                                component.setWidth("98%");
                                                fs.add(component);
                                                component.setData("addedField", "true");
                                            }
                                            fs.layout();
                                        }
                                    });
                                }
                            });
                        }
                    } else {
                        if (!nodeTypes.contains(nodeType)) {
                            fieldSet.setVisible(false);
                        }
                    }
                    if (isOrderingList) {
                        orderingListFieldSet.add(fieldSet);
                    }
                }
                if (field instanceof ComboBox) {
                    final ComboBox<GWTJahiaValueDisplayBean> c = (ComboBox<GWTJahiaValueDisplayBean>) field;
                    final List<GWTJahiaValueDisplayBean> oldSelection = c.getSelection();
                    c.addSelectionChangedListener(new SelectionChangedListener<GWTJahiaValueDisplayBean>() {
                        public void selectionChanged(SelectionChangedEvent<GWTJahiaValueDisplayBean> event) {
                            removeExternalMixin(oldSelection, adapterField);
                            setExternalMixin(adapterField, true);
                            if (oldSelection.size() > 0) {
                                oldSelection.remove(0);
                            }
                            oldSelection.add(c.getSelection().get(0));

                        }
                    });
                    if (c.getValue() != null) {
                        setExternalMixin(adapterField, false);
                    }
                }
                if (defaultedProperties.contains(field.getName())) {
                    field.setData("defaultedField","true");
                }
            }
        }
    }

    /**
     * Set template
     *
     */
    private void setExternalMixin(PropertyAdapterField c, boolean b) {
        String addMixin = null;
        if (c.getValue() != null) {
            addMixin = ((ComboBox<GWTJahiaValueDisplayBean>)c.getField()).getValue().get("addMixin");
        }
        if (addMixin != null && mixin != null) {
            for (GWTJahiaNodeType mix : mixin) {
                if (mix.getName().equals(addMixin)) {
                    if (!b || !externalMixin.contains(addMixin)) {
                        externalMixin.add(mix.getName());
                        addItems(mix, mix.getItems(), false, false, c);
                    }
                }
            }
        }
    }

    private void removeExternalMixin(List<GWTJahiaValueDisplayBean> oldSelection, PropertyAdapterField c) {
        if (oldSelection != null && oldSelection.size() > 0 && oldSelection.get(0).getValue() != null) {
            String removeMixin = oldSelection.get(0).get("addMixin");
            if (externalMixin.contains(removeMixin)) {
                removedTypes.add(removeMixin);
                externalMixin.remove(removeMixin);
                FieldSet fs = (FieldSet) c.getParent();
                Set<Component> compToRemove = new HashSet<Component>();
                for (Component co : fs.getItems()) {
                    if (co instanceof PropertyAdapterField && ((PropertyAdapterField) co).getDefinition().getDeclaringNodeType().equals(removeMixin)) {
                        compToRemove.add(co);
                    }
                }
                for (Component co : compToRemove) {
                    fields.remove(((PropertyAdapterField) co).getName());
                    co.removeFromParent();
                }
            }
        }
    }

    /**
     * Clone hashmap in order to have a modifiable one.
     */
    private void cloneProperties() {
        currentProperties = new HashMap<String, GWTJahiaNodeProperty>();
        if (originalProperties != null) {
            for (String aProp : originalProperties.keySet()) {
                currentProperties.put(aProp, originalProperties.get(aProp).cloneObject());
            }
        }
    }

    /**
     * Get properties
     *
     * @return
     */
    public List<GWTJahiaNodeProperty> getProperties() {
        return getProperties(true, true, false);
    }

    /**
     * Get properties
     *
     * @return
     */
    public List<GWTJahiaNodeProperty> getProperties(boolean includeI18N, boolean includeNonI18N, boolean modifiedOnly) {
        return getProperties(includeI18N, includeNonI18N, modifiedOnly, false);
    }

    public List<GWTJahiaNodeProperty> getProperties(boolean includeI18N, boolean includeNonI18N, boolean modifiedOnly,
                                                    boolean doNotCheckFieldsValues) {
        List<GWTJahiaNodeProperty> newProps = new ArrayList<GWTJahiaNodeProperty>();

        List<GWTJahiaNodeType> l = new ArrayList<GWTJahiaNodeType>(nodeTypes);
        if (mixin != null) {
            for (GWTJahiaNodeType type : mixin) {
                if (addedTypes.contains(type.getName())) {
                    l.add(type);
                }
                if (externalMixin.contains(type.getName())) {
                    l.add(type);
                }
                if (removedTypes.contains(type.getName())) {
                    l.remove(type);
                }
            }
        }
        for (GWTJahiaNodeType nodeType : l) {
            List<GWTJahiaItemDefinition> allItems = new ArrayList<GWTJahiaItemDefinition>();
            if (viewInheritedItems) {
                allItems.addAll(nodeType.getInheritedItems());
            }
            allItems.addAll(nodeType.getItems());

            for (GWTJahiaItemDefinition definition : allItems) {
                boolean i18nProp = (definition instanceof GWTJahiaPropertyDefinition &&
                        ((GWTJahiaPropertyDefinition) definition).isInternationalized());
                if ((includeI18N && i18nProp) || (includeNonI18N && !i18nProp)) {
                    if (((definition.isHidden() || !displayHiddenProperties) && originalProperties.get(definition.getName()) != null) ||
                            (dataType == null || dataType.isEmpty() || dataType.contains(definition.getDataType()))) {
                        if (!definition.isProtected()) {
                            PropertyAdapterField f = fields.get(definition.getName());
                            GWTJahiaNodeProperty prop = currentProperties.get(definition.getName());
                            if (!doNotCheckFieldsValues && f != null && (f.getValue() != null || !f.getField().getEmptyText().equals(f.getField().getOriginalValue())) &&
                                    (f.isDirty() || !modifiedOnly || f.getData("addedField") != null || f.getData("defaultedField") != null)) {
                                Log.debug("Set value for " + prop.getName());
                                prop.setValues(getPropertyValues(f, definition));
                                prop.setDirty(f.isDirty());
                                if (f.isDirty()) {
                                    f.setDirty(true);
                                }
                                newProps.add(prop);
                            }
                        }
                    }
                }
            }
        }
        return newProps;
    }

    public Set<String> getAddedTypes() {
        return addedTypes;
    }

    public Set<String> getRemovedTypes() {
        return removedTypes;
    }

    public Set<String> getExternalMixin() {
        return externalMixin;
    }

    public List<GWTJahiaNodeType> getNodeTypes() {
        return nodeTypes;
    }


    /**
     * Returns a list with property values, populated from the field value depending on its type.
     *
     * @param field   the form field, holding the value
     * @param itemDef the definition of the corresponding node/property
     * @return a list with property values, populated from the field value depending on its type
     */
    public static List<GWTJahiaNodePropertyValue> getPropertyValues(Field<?> field, GWTJahiaItemDefinition itemDef) {
        List<GWTJahiaNodePropertyValue> values = new ArrayList<GWTJahiaNodePropertyValue>();
        Field<?> fld = field;
        if(field instanceof PropertyAdapterField) {
            fld = ((PropertyAdapterField)field).getField();
        }
        if (itemDef.isNode()) {
            // case of a new link node
            if (fld instanceof ContentPickerField) {
                ContentPickerField pck = (ContentPickerField) fld;
                List<GWTJahiaNode> selection = pck.getValue();
                for (GWTJahiaNode node : selection) {
                    values.add(new GWTJahiaNodePropertyValue(node, GWTJahiaNodePropertyType.PAGE_LINK));
                }
            }
            // case of a file upload
            else {
                Object value = fld.getValue();
                if (value != null) {
                    values.add(new GWTJahiaNodePropertyValue(value.toString(), GWTJahiaNodePropertyType.ASYNC_UPLOAD));
                }
            }
        } else {
            GWTJahiaPropertyDefinition propDef = (GWTJahiaPropertyDefinition) itemDef;
            // case of a list property
            if (fld instanceof DualListField) {
                List<GWTJahiaValueDisplayBean> selection =
                        ((DualListField<GWTJahiaValueDisplayBean>) fld).getToList().getStore().getModels();
                for (GWTJahiaValueDisplayBean valueDisplayBean : selection) {
                    GWTJahiaNodePropertyValue propertyValue =
                            getPropertyValue(valueDisplayBean, propDef.getRequiredType());
                    if (propertyValue != null) {
                        values.add(propertyValue);
                    }
                }
            }
            // case of a reference
            else if (fld instanceof ContentPickerField) {
                ContentPickerField pck = (ContentPickerField) fld;
                List<GWTJahiaNode> selection = pck.getValue();
                for (GWTJahiaNode node : selection) {
                    GWTJahiaNodePropertyValue propertyValue =
                            new GWTJahiaNodePropertyValue(node, propDef.getRequiredType());
                    values.add(propertyValue);
                }
            } else if (fld instanceof MultipleTextField || fld instanceof TagField) {
                List l = (List) fld.getValue();
                for (Object s : l) {
                    values.add(getPropertyValue(s, propDef.getRequiredType()));
                }
            } else {
                values.add(getPropertyValue(fld.getValue(), propDef.getRequiredType()));
            }
        }

        return values;
    }

    /**
     * Converts the field value into its string representation, considering
     * field type.
     *
     * @param fieldValue   the form field value
     * @param requiredType the expected field type
     * @return string representation of the field value, converted based on its
     *         type
     */
    private static GWTJahiaNodePropertyValue getPropertyValue(Object fieldValue, int requiredType) {
        String propValueString = null;
        if (fieldValue != null) {
            if (fieldValue instanceof Date) {
                propValueString = String.valueOf(((Date) fieldValue).getTime());
            } else if (fieldValue instanceof GWTJahiaValueDisplayBean) {
                propValueString = ((GWTJahiaValueDisplayBean) fieldValue).getValue();
            } else {
                propValueString = fieldValue.toString();
            }
        }
        return new GWTJahiaNodePropertyValue(propValueString, requiredType);
    }

    public void copyToAllLanguages(GWTJahiaNodeProperty prop) {

    }

    /**
     * Get fields map
     *
     * @return
     */
    public Map<String, PropertyAdapterField> getFieldsMap() {
        return fields;
    }

    public Map<String, FieldSet> getFieldSetsMap() {
        return fieldSets;
    }

    /**
     * retrieve  GWTJahiaItemDefinition
     *
     * @param prop
     * @return
     */
    public GWTJahiaItemDefinition getGWTJahiaItemDefinition(GWTJahiaNodeProperty prop) {
        return getGWTJahiaItemDefinition(prop.getName());
    }

    public GWTJahiaItemDefinition getGWTJahiaItemDefinition(String propName) {
        return propertyDefinitions.get(propName);
    }

    public List<FieldSet> getOrderingListFieldSet() {
        return orderingListFieldSet;
    }

    @Override
    public String toString() {
        return nodeTypes.toString();
    }

    public void setLocale(String locale) {
        this.locale = locale;
    }

    public void setTranslationSource(LangPropertiesEditor translationSource) {
        this.translationSource = translationSource;
    }

    public class PropertyAdapterField extends AdapterField {
        private Field field;
        private boolean dirty = false;
        private GWTJahiaItemDefinition definition;
        private LayoutContainer panel;

        public PropertyAdapterField(final Field field, final GWTJahiaItemDefinition definition, final GWTJahiaNodeProperty property) {
            super(new LayoutContainer());
            panel = (LayoutContainer) getWidget();

            if (isMultipleEdit && !definition.isProtected()) {
                field.setEnabled(false);
                final CheckBox checkbox = new CheckBox();
                final Field f = field;
                checkbox.addListener(Events.Change, new Listener<ComponentEvent>() {
                    public void handleEvent(ComponentEvent event) {
                        if (checkbox.getValue()) {
                            Log.debug("add ");
                            f.setEnabled(true);
                        } else {
                            Log.debug("remove ");
                            f.setEnabled(false);
                        }
                    }

                });
                checkbox.setHideLabel(true);
                final HBoxLayout hBoxLayout = new HBoxLayout();
                hBoxLayout.setHBoxLayoutAlign(HBoxLayout.HBoxLayoutAlign.MIDDLE);
                panel.setLayout(hBoxLayout);
                panel.add(checkbox, new HBoxLayoutData());
                panel.add(field, new HBoxLayoutData());
            } else {
                panel.addStyleName("mypanel");
                final FlowLayout hBoxLayout = new FlowLayout();
                panel.setLayout(hBoxLayout);
                panel.add(field);
                field.setWidth("98%");
                if (isWriteable && viewCopyToAllLangs && JahiaGWTParameters.getSiteLanguages() != null && JahiaGWTParameters.getSiteLanguages().size() > 1 && definition.isInternationalized()) {
                    final Button button = new Button(Messages.get("label.translate.copyall","Copy to all languages"));
                    button.addSelectionListener(new SelectionListener<ButtonEvent>() {
                        @Override
                        public void componentSelected(ButtonEvent ce) {
                            GWTJahiaNodeProperty prop = currentProperties.get(definition.getName()).cloneObject();
                            prop.setValues(getPropertyValues(field, definition));
                            copyToAllLanguages(prop);
                            MessageBox.info(Messages.get("label.translate.copyall", "Copy to all languages"),
                                    Messages.get("label.translate.copyall.done",
                                            "Content has been copied to other languages"),
                                    null);

                        }
                    });
                    panel.add(button);
                }
                if (isWriteable && translationSource != null && JahiaGWTParameters.getSiteLanguages().size() > 1
                        && ((GWTJahiaPropertyDefinition)definition).getRequiredType() == GWTJahiaNodePropertyType.STRING
                        && definition.isInternationalized() && !definition.isHidden() && !definition.isProtected()
                        && !((GWTJahiaPropertyDefinition) definition).isConstrained()) {
                    PropertiesEditor sourcePropertiesEditor = translationSource.getPropertiesEditorByLang(translationSource.getDisplayedLocale().getLanguage());
                    GWTJahiaNodeProperty sourceProperty = null;
                    for (GWTJahiaNodeProperty prop : sourcePropertiesEditor.getProperties(true, false, false)) {
                        if (definition.getName().equals(prop.getName())) {
                            sourceProperty = prop;
                            break;
                        }
                    }
                    if (sourceProperty != null) {
                        Button suggestButton = new Button(Messages.get("label.translate.suggest", "Suggest translation"));
                        final GWTJahiaNodeProperty finalSourceProperty = sourceProperty;
                        suggestButton.addSelectionListener(new SelectionListener<ButtonEvent>() {
                            @Override
                            public void componentSelected(ButtonEvent ce) {
                                MessageBox.confirm(
                                        Messages.get("label.translate.suggest", "Suggest translation"),
                                        Messages.get("label.translate.suggest.confirm", "Do you want to replace the content by an automatic translation of it?"),
                                        new Listener<MessageBoxEvent>() {
                                            public void handleEvent(MessageBoxEvent be) {
                                                if(Dialog.YES.equalsIgnoreCase(be.getButtonClicked().getItemId())) {
                                                    String srcLanguage = translationSource.getDisplayedLocale().getLanguage();
                                                    int i = srcLanguage.indexOf("_");
                                                    if (i > -1) {
                                                        srcLanguage = srcLanguage.substring(0, i);
                                                    }
                                                    String destLanguage = locale;
                                                    i = destLanguage.indexOf("_");
                                                    if (i > -1) {
                                                        destLanguage = destLanguage.substring(0, i);
                                                    }
                                                    JahiaContentManagementService.App.getInstance().translate(finalSourceProperty, definition, srcLanguage, destLanguage, JahiaGWTParameters.getSiteUUID(), new BaseAsyncCallback<GWTJahiaNodeProperty>() {
                                                        public void onApplicationFailure(Throwable throwable) {
                                                            com.google.gwt.user.client.Window.alert(Messages.get("failure.property.translation", "Property translation failed") + "\n\n"
                                                                    + throwable.getMessage());
                                                            Log.error("Failed to translate property", throwable);
                                                        }

                                                        public void onSuccess(GWTJahiaNodeProperty newProp) {
                                                            Field f = getFieldsMap().get(definition.getName()).getField();
                                                            FormFieldCreator.copyValue(newProp, f);
                                                        }
                                                    });
                                                }
                                            }}
                                );
                            }
                        });
                        panel.add(suggestButton);
                    }
                }
            }

            this.field = field;
            this.definition = definition;
            setName(field.getName());
            setFieldLabel(field.getFieldLabel());
            setLabelSeparator(field.getLabelSeparator());
        }

        public Field getField() {
            return field;
        }

        public GWTJahiaItemDefinition getDefinition() {
            return definition;
        }

        public Object getValue() {
            return field.getValue();
        }

        public boolean isDirty() {
            return dirty || field.isDirty();
        }

        public void setDirty(boolean dirty) {
            this.dirty = dirty;
        }

        public void setVisible(boolean visible) {
            super.setVisible(visible);
            field.setVisible(visible);
        }

        public void setEnabled(boolean enabled) {
            super.setEnabled(enabled);
            field.setEnabled(enabled);
        }

        @Override
        public void setWidth(final String width) {
            super.setWidth(width);
            DeferredCommand.addCommand(new Command() {
                public void execute() {
                    field.setWidth(width);
                }
            });
        }

        @Override
        public void addListener(EventType eventType, Listener<? extends BaseEvent> listener) {
            if (eventType == Events.Change) {
                field.addListener(eventType, listener);
            } else {
                super.addListener(eventType, listener);
            }
        }

        @Override
        @SuppressWarnings({"unchecked"})
        public <X> X getData(String key) {
            return (X) field.getData(key);
        }

        @Override
        public void setData(String key, Object data) {
            field.setData(key, data);
        }
    }
}
