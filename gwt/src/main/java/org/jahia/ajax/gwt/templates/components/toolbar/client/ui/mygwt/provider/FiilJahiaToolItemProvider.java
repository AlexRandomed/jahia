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

import com.extjs.gxt.ui.client.widget.toolbar.ToolItem;
import com.extjs.gxt.ui.client.widget.toolbar.FillToolItem;
import com.extjs.gxt.ui.client.widget.menu.MenuItem;
import com.extjs.gxt.ui.client.widget.DataListItem;
import com.extjs.gxt.ui.client.widget.DataList;
import com.extjs.gxt.ui.client.widget.TabItem;
import com.extjs.gxt.ui.client.widget.TabPanel;
import com.google.gwt.user.client.ui.Widget;
import org.jahia.ajax.gwt.templates.components.toolbar.client.bean.GWTToolbarItemsGroup;
import org.jahia.ajax.gwt.templates.components.toolbar.client.bean.GWTToolbarItem;

/**
 * User: ktlili
 * Date: 18 sept. 2008
 * Time: 12:29:05
 */
public class FiilJahiaToolItemProvider extends JahiaToolItemProvider {
    public ToolItem createToolItem(final GWTToolbarItemsGroup gwtToolbarItemsGroup, final GWTToolbarItem gwtToolbarItem) {
        return new FillToolItem();
    }

    public MenuItem createMenuItem(final GWTToolbarItemsGroup gwtToolbarItemsGroup, final GWTToolbarItem gwtToolbarItem) {
        MenuItem menuItem = new MenuItem();
        return menuItem;
    }

    public DataListItem createDataListItem(final DataList list, final GWTToolbarItemsGroup gwtToolbarItemsGroup, final GWTToolbarItem gwtToolbarItem) {
        return null;
    }

    public Widget createWidget(GWTToolbarItemsGroup gwtToolbarItemsGroup, GWTToolbarItem gwtToolbarItem) {
        return new FillToolItem();
    }

    public TabItem createTabItem(TabPanel tabPanel, GWTToolbarItemsGroup gwtToolbarItemsGroup, GWTToolbarItem gwtToolbarItem) {
        return null;
    }
}
