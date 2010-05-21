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
package org.jahia.ajax.gwt.client.widget.content;

import org.jahia.ajax.gwt.client.data.toolbar.GWTManagerConfiguration;
import org.jahia.ajax.gwt.client.util.content.actions.ManagerConfigurationFactory;
import org.jahia.ajax.gwt.client.widget.tripanel.*;
import org.jahia.ajax.gwt.client.data.node.GWTJahiaNode;

import java.util.List;
import java.util.Map;

import com.extjs.gxt.ui.client.widget.layout.BorderLayoutData;
import com.extjs.gxt.ui.client.Style;

/**
 * File and folder picker control.
 *
 * @author rfelden
 *         Date: 27 ao�t 2008
 *         Time: 17:55:07
 */
public class ContentPicker extends TriPanelBrowserLayout {
    private PickedContent pickedContent;

    public ContentPicker(String selectionLabel, final String rootPath, Map<String, String> selectorOptions, final List<GWTJahiaNode> selectedNodes,
                         List<String> filters, List<String> mimeTypes, GWTManagerConfiguration config, boolean multiple) {
        super(config);
        //setWidth("714px");
        setHeight("700px");

        if (mimeTypes != null && mimeTypes.size() > 0) {
            config.getMimeTypes().addAll(mimeTypes);
        }
        if (filters != null && filters.size() > 0) {
            config.getFilters().addAll(filters);
        }

        // construction of the UI components
        BottomRightComponent bottomComponents;
        if (config.getName().equalsIgnoreCase(ManagerConfigurationFactory.LINKPICKER)) {
            boolean externalAllowed = true;
            boolean internalAllowed = true;
            if (selectorOptions != null) {
                String type = selectorOptions.get("type");
                if (type != null) {
                    externalAllowed = type.indexOf("external") != 0;
                    internalAllowed = type.indexOf("internal") != 0;
                }
            } else {
                // allow only internal if no selector specified
                externalAllowed = false;
                internalAllowed = true;
            }
            bottomComponents = new PickedPageView(externalAllowed, internalAllowed, selectedNodes, multiple, config, false);
        } else {
            bottomComponents = new PickedContentView(selectionLabel, selectedNodes, multiple, config);
        }
        TopRightComponent contentPicker = new ContentPickerBrowser(config.getName(), rootPath, selectedNodes, config, multiple);

        MyStatusBar statusBar = new FilterStatusBar(config.getFilters(), config.getMimeTypes(), config.getNodeTypes());

        // setup widgets in layout

        if (config.getName().equalsIgnoreCase(ManagerConfigurationFactory.LINKPICKER)) {
            setCenterData(new BorderLayoutData(Style.LayoutRegion.SOUTH, 300));
            initWidgets(null, bottomComponents.getComponent(), contentPicker.getComponent(), null, statusBar);
        } else {
            setCenterData(new BorderLayoutData(Style.LayoutRegion.SOUTH, 150));
            initWidgets(null, contentPicker.getComponent(), bottomComponents.getComponent(), null, statusBar);
        }

        // linker initializations
        linker.registerComponents(null, contentPicker, bottomComponents, null, null);
        contentPicker.initContextMenu();
        linker.handleNewSelection();

        pickedContent = (PickedContent) bottomComponents;
    }

    public List<GWTJahiaNode> getSelectedNodes() {
        return pickedContent.getSelectedContent();
    }


}
