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

package org.jahia.ajax.gwt.templates.components.toolbar.client.ui.mygwt.provider;

import com.extjs.gxt.ui.client.widget.DataList;
import com.extjs.gxt.ui.client.widget.DataListItem;
import com.extjs.gxt.ui.client.widget.TabItem;
import com.extjs.gxt.ui.client.widget.TabPanel;
import com.extjs.gxt.ui.client.widget.menu.Item;
import com.extjs.gxt.ui.client.widget.toolbar.AdapterToolItem;
import com.extjs.gxt.ui.client.widget.toolbar.ToolItem;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Widget;
import org.jahia.ajax.gwt.commons.client.rpc.JahiaService;
import org.jahia.ajax.gwt.commons.client.rpc.JahiaServiceAsync;
import org.jahia.ajax.gwt.commons.client.ui.language.LanguageSelectedListener;
import org.jahia.ajax.gwt.commons.client.ui.language.LanguageSwitcher;
import org.jahia.ajax.gwt.config.client.JahiaGWTParameters;
import org.jahia.ajax.gwt.templates.components.toolbar.client.bean.GWTToolbarItem;
import org.jahia.ajax.gwt.templates.components.toolbar.client.bean.GWTToolbarItemsGroup;

/**
 * Created by IntelliJ IDEA.
 * User: rincevent
 * Date: 27 nov. 2008
 * Time: 11:35:12
 * To change this template use File | Settings | File Templates.
 */
public class LanguageSwitcherProvider extends JahiaToolItemProvider {
    public Widget createWidget (GWTToolbarItemsGroup gwtToolbarItemsGroup, GWTToolbarItem gwtToolbarItem) {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    public ToolItem createToolItem (GWTToolbarItemsGroup gwtToolbarItemsGroup, GWTToolbarItem gwtToolbarItem) {
        return new AdapterToolItem(new LanguageSwitcher(true, true, false,true, JahiaGWTParameters.getLanguage(), false, new LanguageSelectedListener() {
            private final JahiaServiceAsync jahiaServiceAsync = JahiaService.App.getInstance();
            public void onLanguageSelected (String languageSelected) {
//                Info.display("Language Selected Event", "The '{0}' button was clicked.", languageSelected);
                jahiaServiceAsync.getLanguageURL(languageSelected,new AsyncCallback<String>() {
                    public void onFailure (Throwable throwable) {
                        //To change body of implemented methods use File | Settings | File Templates.
                    }

                    public void onSuccess (String s) {
                        com.google.gwt.user.client.Window.Location.assign(s);
                    }
                });
            }

            public void onWorkflowSelected (String text) {}
        }));
    }

    public Item createMenuItem (GWTToolbarItemsGroup gwtToolbarItemsGroup, GWTToolbarItem gwtToolbarItem) {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    public DataListItem createDataListItem (DataList itemsList, GWTToolbarItemsGroup gwtToolbarItemsGroup, GWTToolbarItem gwtToolbarItem) {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    public TabItem createTabItem (TabPanel tabPanel, GWTToolbarItemsGroup gwtToolbarItemsGroup, GWTToolbarItem gwtToolbarItem) {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }
}
