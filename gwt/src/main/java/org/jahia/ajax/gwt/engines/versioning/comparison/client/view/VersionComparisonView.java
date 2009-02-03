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

package org.jahia.ajax.gwt.engines.versioning.comparison.client.view;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.jahia.ajax.gwt.commons.client.beans.GWTVersion;
import org.jahia.ajax.gwt.commons.client.ui.IndexedToggleButton;
import org.jahia.ajax.gwt.commons.client.ui.ToggleButtonsWidget;
import org.jahia.ajax.gwt.engines.versioning.client.model.Field;
import org.jahia.ajax.gwt.engines.versioning.client.model.FieldGroup;
import org.jahia.ajax.gwt.engines.versioning.client.model.VersionComparisonData;
import org.jahia.ajax.gwt.engines.versioning.comparison.client.dao.DataFactory;
import org.jahia.ajax.gwt.engines.versioning.comparison.client.dao.DataListener;

import com.google.gwt.user.client.ui.ClickListener;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.FlexTable;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.HasHorizontalAlignment;
import com.google.gwt.user.client.ui.HasVerticalAlignment;
import com.google.gwt.user.client.ui.SimplePanel;
import com.google.gwt.user.client.ui.TabPanel;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.user.client.ui.Widget;

/**
 * Created by IntelliJ IDEA.
 * User: hollis
 * Date: 22 avr. 2008
 * Time: 16:50:12
 * To change this template use File | Settings | File Templates.
 */
public class VersionComparisonView extends Composite implements DataListener, ClickListener {

    private String versionableUUID;
    private String version1;
    private String version2;
    private String lang;
    private VersionComparisonData data;
    private DataFactory dataFactory;

    private VerticalPanel mainPanel;
    private HTML titleAssertHTML;
    private FlexTable versionsTable;
    private HTML diffLegendHTML;
    private TabPanel comparisonTabPanel;
    private String fieldContentHeight = "40px";
    private String bigTextFieldContentHeight = "300px";

    private Map<String, VerticalPanel> fieldGroupPanels;

    private Map<String, VersionCompareButtons> versionButtons;

    public VersionComparisonView(DataFactory dataFactory,
                                 String versionableUUID,
                                 String version1,
                                 String version2,
                                     String lang) {
        this.dataFactory = dataFactory;
        this.versionableUUID = versionableUUID;
        this.version1 = version1;
        this.version2 = version2;
        this.lang = lang;
        this.fieldGroupPanels = new HashMap<String, VerticalPanel>();
        this.versionButtons = new HashMap<String, VersionCompareButtons>();
        initWidget();
        this.dataFactory.getVersionDAO().getData(versionableUUID,version1,version2,lang,this);
    }

    public void onDataLoaded(VersionComparisonData data) {

        this.data = data;

        if (data == null){
            //dispayError();
        } else {
            // update GUI
            initWidget();
        }
    }

    public String getVersionableUUID() {
        return versionableUUID;
    }

    public void setVersionableUUID(String versionableUUID) {
        this.versionableUUID = versionableUUID;
    }

    public String getVersion1() {
        return version1;
    }

    public void setVersion1(String version1) {
        this.version1 = version1;
    }

    public String getVersion2() {
        return version2;
    }

    public void setVersion2(String version2) {
        this.version2 = version2;
    }

    public String getLang() {
        return lang;
    }

    public void setLang(String lang) {
        this.lang = lang;
    }

    public Map<String, VerticalPanel> getFieldGroupPanels() {
        return fieldGroupPanels;
    }

    public void setFieldGroupPanels(Map<String, VerticalPanel> fieldGroupPanels) {
        this.fieldGroupPanels = fieldGroupPanels;
    }

    public String getFieldContentHeight() {
        return fieldContentHeight;
    }

    public void setFieldContentHeight(String fieldContentHeight) {
        this.fieldContentHeight = fieldContentHeight;
    }

    public String getBigTextFieldContentHeight() {
        return bigTextFieldContentHeight;
    }

    public void setBigTextFieldContentHeight(String bigTextFieldContentHeight) {
        this.bigTextFieldContentHeight = bigTextFieldContentHeight;
    }

    protected void initWidget(){

        // main vertical panel
        if (mainPanel == null){

            mainPanel = new VerticalPanel();
            mainPanel.setStyleName("gwt-versionComparison-mainPanel");
            mainPanel.setWidth("100%");
            mainPanel.setSpacing(0);
            mainPanel.setHorizontalAlignment(HasHorizontalAlignment.ALIGN_LEFT);
            mainPanel.setVerticalAlignment(HasVerticalAlignment.ALIGN_TOP);

            // title assert
            titleAssertHTML = new HTML("");
            titleAssertHTML.setStyleName("gwt-versionComparison-titleAssert");
            titleAssertHTML.setWidth("100%");
            titleAssertHTML.setWordWrap(false);
            mainPanel.add(titleAssertHTML);

            initWidget(mainPanel);

            // outer container
            SimplePanel outerPanel = new SimplePanel();
            outerPanel.setStyleName("gwt-versionComparison-versionsOuterContainer");
            outerPanel.setWidth("100%");

            // inner container
            SimplePanel innerPanel = new SimplePanel();
            innerPanel.setStyleName("gwt-versionComparison-versionsInnerContainer");
            innerPanel.setWidth("100%");
            outerPanel.add(innerPanel);

            mainPanel.add(outerPanel);
            mainPanel.setCellWidth(outerPanel,"100%");

            VerticalPanel versionsPanel = new VerticalPanel();
            versionsPanel.setStyleName("gwt-versionComparison-versionsPanel");
            versionsPanel.setWidth("100%");
            versionsPanel.setHorizontalAlignment(HasHorizontalAlignment.ALIGN_LEFT);
            versionsPanel.setVerticalAlignment(HasVerticalAlignment.ALIGN_TOP);
            innerPanel.add(versionsPanel);

            versionsTable = new FlexTable();
            versionsTable.setStyleName("gwt-versionComparison-versionsTable");
            versionsTable.setWidth("100%");
            versionsTable.setBorderWidth(0);
            versionsTable.setCellSpacing(0);
            versionsTable.setCellPadding(3);

            versionsPanel.add(versionsTable);
            versionsPanel.setCellWidth(versionsTable,"100%");

            // diff legend
            diffLegendHTML = new HTML();
            diffLegendHTML.setStyleName("gwt-versionComparison-diffLegend");
            diffLegendHTML.setWidth("100%");
            versionsPanel.add(diffLegendHTML);
            versionsPanel.setCellWidth(diffLegendHTML,"100%");

            // comparison tab panel
            comparisonTabPanel = new TabPanel();
            comparisonTabPanel.addStyleName("gwt-versionComparison-comparisonTabPanel");
            comparisonTabPanel.setWidth("100%");
            mainPanel.add(comparisonTabPanel);
            mainPanel.setCellWidth(comparisonTabPanel,"100%");
        }

        if (data == null){
            // displayLoading();
            return;
        }

        updateTitleAssert();
        updateVersionsTable();
        updateDiffLegend();
        updateComparisonTabPanel();
    }

    private void updateTitleAssert(){
        String titleAssert = data.getTitleAssert();
        this.titleAssertHTML.setHTML(titleAssert);
    }

    private void updateVersionsTable(){
        GWTVersion version1 = data.getVersion1();
        GWTVersion version2 = data.getVersion2();
        String[] versionRowDataHeadLabels = data.getVersionRowDataHeadLabels();
        int nbColumns  = versionRowDataHeadLabels.length;

        this.versionsTable.clear();

        // add table headers
        this.versionsTable.getRowFormatter().setStyleName(0,"gwt-versionComparison-versionsTableHeader");
        for (int i=0; i<nbColumns; i++){
            this.versionsTable.setText(0,i,versionRowDataHeadLabels[i]);
        }

        // add versions rows
        addVersionRow(version1);
        addVersionRow(version2);
    }

    private void addVersionRow(GWTVersion version){
        String[] data = version.getVersionRowData();
        int nbColumns = data.length;
        int rowCount = this.versionsTable.getRowCount()-1;
        
        boolean odd = (rowCount==0 || (((rowCount + 1) % 2) != 0));
        for (int i=0; i<nbColumns; i++){
            this.versionsTable.setText(rowCount+1,i,data[i]);
            if (odd){
                this.versionsTable.getRowFormatter().setStyleName(rowCount+1,"gwt-versionComparison-versionsTableRow-odd");
            } else {
                this.versionsTable.getRowFormatter().setStyleName(rowCount+1,"gwt-versionComparison-versionsTableRow-even");
            }
        }
    }

    private void updateDiffLegend(){
        StringBuffer buff = new StringBuffer();
        buff.append(this.data.getAddedDiffLegend()).append("&nbsp;|&nbsp;").append(this.data.getRemovedDiffLegend())
                .append("&nbsp;|&nbsp;").append(this.data.getChangedDiffLegend());
        this.diffLegendHTML.setHTML(buff.toString());
    }

    private void updateComparisonTabPanel(){

        this.comparisonTabPanel.clear();
        this.fieldGroupPanels.clear();
        this.versionButtons.clear();
        VersionCompareButtons versionCompareButtons = null;
        List<FieldGroup> fieldsGroup = data.getFieldGroups();
        if (fieldsGroup !=null && !fieldsGroup.isEmpty()){
            for (FieldGroup fieldGroup : fieldsGroup) {
                VerticalPanel vPanel = new VerticalPanel();
                versionCompareButtons = this.versionButtons.get(fieldGroup.getGroupName());
                if (versionCompareButtons == null){
                    versionCompareButtons = new VersionCompareButtons(data);
                    this.versionButtons.put(fieldGroup.getGroupName(),versionCompareButtons);
                }
                ToggleButtonsWidget toggleButtonsWidget = versionCompareButtons.getToggleButtonsWidget();
                vPanel.add(toggleButtonsWidget);
                vPanel.setCellHorizontalAlignment(toggleButtonsWidget, HasHorizontalAlignment.ALIGN_RIGHT);

                vPanel.setHeight("100%");
                this.fieldGroupPanels.put(fieldGroup.getGroupName(), vPanel);
                this.comparisonTabPanel.add(vPanel, fieldGroup.getGroupName());
                List<Field> fields = fieldGroup.getFields();
                if (fields != null) {
                    for (Field f : fields) {
                        FieldView fieldView = new FieldView(f, this);
                        versionCompareButtons.addFieldView(fieldView);
                        vPanel.add(fieldView);
                    }
                }
            }
            this.comparisonTabPanel.selectTab(0);
        }
    }

    public void onClick(Widget sender){

    }

    private class VersionCompareButtons implements ClickListener {

        public static final String OLD_VERSION = "OLD_VERSION";
        public static final String NEW_VERSION = "NEW_VERSION";
        public static final String DIFF_VERSION = "DIFF_VERSION";

        private List<FieldView> fieldViews;

        private ToggleButtonsWidget toggleButtonsWidget;

        public VersionCompareButtons(VersionComparisonData data){
            fieldViews = new ArrayList<FieldView>();
            toggleButtonsWidget = new ToggleButtonsWidget();
            toggleButtonsWidget.addButton(data.getVersion1().getReadableName());
            toggleButtonsWidget.addButton(data.getVersion2().getReadableName());
            toggleButtonsWidget.addButton("difference");
            toggleButtonsWidget.addClickListener(this);
        }

        public void addFieldView(FieldView fieldView){
            fieldViews.add(fieldView);
        }

        public void updateFieldViewContent(String valueType){
            Iterator<FieldView> iterator = fieldViews.iterator();
            FieldView fieldView = null;
            while (iterator.hasNext()){
                fieldView = iterator.next();
                if (OLD_VERSION.equals(valueType)) {
                    fieldView.setFieldViewContent(fieldView.getField().getOriginalValue());
                } else if (NEW_VERSION.equals(valueType)){
                    fieldView.setFieldViewContent(fieldView.getField().getOriginalValue());
                } else if (DIFF_VERSION.equals(valueType)){
                    fieldView.setFieldViewContent(fieldView.getField().getMergedDiffValue());
                }
            }
        }

        public ToggleButtonsWidget getToggleButtonsWidget() {
            return toggleButtonsWidget;
        }

        public void setToggleButtonsWidget(ToggleButtonsWidget toggleButtonsWidget) {
            this.toggleButtonsWidget = toggleButtonsWidget;
        }

        public void onClick(Widget sender){
            if (sender != null && sender instanceof IndexedToggleButton){
                IndexedToggleButton button = (IndexedToggleButton)sender;
                if (button.getIndex()==0){
                    updateFieldViewContent(OLD_VERSION);
                } else if (button.getIndex()==1) {
                    updateFieldViewContent(NEW_VERSION);
                } else if (button.getIndex()==2){
                    updateFieldViewContent(DIFF_VERSION);
                }
            }
        }
    }
}