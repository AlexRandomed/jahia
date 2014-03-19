/**
 * ==========================================================================================
 * =                        DIGITAL FACTORY v7.0 - Community Distribution                   =
 * ==========================================================================================
 *
 *     Rooted in Open Source CMS, Jahia's Digital Industrialization paradigm is about
 *     streamlining Enterprise digital projects across channels to truly control
 *     time-to-market and TCO, project after project.
 *     Putting an end to "the Tunnel effect", the Jahia Studio enables IT and
 *     marketing teams to collaboratively and iteratively build cutting-edge
 *     online business solutions.
 *     These, in turn, are securely and easily deployed as modules and apps,
 *     reusable across any digital projects, thanks to the Jahia Private App Store Software.
 *     Each solution provided by Jahia stems from this overarching vision:
 *     Digital Factory, Workspace Factory, Portal Factory and eCommerce Factory.
 *     Founded in 2002 and headquartered in Geneva, Switzerland,
 *     Jahia Solutions Group has its North American headquarters in Washington DC,
 *     with offices in Chicago, Toronto and throughout Europe.
 *     Jahia counts hundreds of global brands and governmental organizations
 *     among its loyal customers, in more than 20 countries across the globe.
 *
 *     For more information, please visit http://www.jahia.com
 *
 * JAHIA'S DUAL LICENSING IMPORTANT INFORMATION
 * ============================================
 *
 *     Copyright (C) 2002-2014 Jahia Solutions Group SA. All rights reserved.
 *
 *     THIS FILE IS AVAILABLE UNDER TWO DIFFERENT LICENSES:
 *     1/GPL OR 2/JSEL
 *
 *     1/ GPL
 *     ==========================================================
 *
 *     IF YOU DECIDE TO CHOSE THE GPL LICENSE, YOU MUST COMPLY WITH THE FOLLOWING TERMS:
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
 *     describing the FLOSS exception, and it is also available here:
 *     http://www.jahia.com/license"
 *
 *     2/ JSEL - Commercial and Supported Versions of the program
 *     ==========================================================
 *
 *     IF YOU DECIDE TO CHOOSE THE JSEL LICENSE, YOU MUST COMPLY WITH THE FOLLOWING TERMS:
 *
 *     Alternatively, commercial and supported versions of the program - also known as
 *     Enterprise Distributions - must be used in accordance with the terms and conditions
 *     contained in a separate written agreement between you and Jahia Solutions Group SA.
 *
 *     If you are unsure which license is appropriate for your use,
 *     please contact the sales department at sales@jahia.com.
 */
package org.jahia.ajax.gwt.client.widget.content;

import com.extjs.gxt.ui.client.Style;
import com.extjs.gxt.ui.client.event.Events;
import com.extjs.gxt.ui.client.event.Listener;
import com.extjs.gxt.ui.client.event.SelectionEvent;
import com.extjs.gxt.ui.client.widget.button.Button;
import org.jahia.ajax.gwt.client.core.JahiaGWTParameters;
import org.jahia.ajax.gwt.client.data.toolbar.GWTManagerConfiguration;
import org.jahia.ajax.gwt.client.widget.toolbar.ActionContextMenu;
import org.jahia.ajax.gwt.client.widget.tripanel.*;
import org.jahia.ajax.gwt.client.data.node.GWTJahiaNode;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * File and folder picker control.
 *
 * @author rfelden
 *         Date: 27 ao�t 2008
 *         Time: 17:55:07
 */
public class ContentPicker extends TriPanelBrowserLayout {
    private PickedContentView pickedContent;

    public ContentPicker(Map<String, String> selectorOptions, final List<GWTJahiaNode> selectedNodes,
                         final List<String> types, List<String> filters, List<String> mimeTypes,
                         final GWTManagerConfiguration config, boolean multiple) {
        super(config);

        JahiaGWTParameters.setSiteNode(config.getSiteNode());

        setId("JahiaGxtContentPicker");
        
        //setWidth("714px");
        setHeight("700px");
        if (types != null && types.size() > 0) {
            config.setNodeTypes(types);
        }
        if (mimeTypes != null && mimeTypes.size() > 0) {
            config.getMimeTypes().addAll(mimeTypes);
        }
        if (filters != null && filters.size() > 0) {
            config.getFilters().addAll(filters);
        }

        List<String> selectedPaths = new ArrayList<String>();
        for (GWTJahiaNode node : selectedNodes) {
            final String path = node.getPath();
            selectedPaths.add(path.substring(0, path.lastIndexOf("/")));
        }
        // construction of the UI components
        final LeftComponent tree = new ContentRepositoryTabs(config, selectedPaths);
        final ContentViews contentViews = new ContentViews(config);


        if (multiple) {
            contentViews.setSelectionMode(Style.SelectionMode.MULTI);
        } else {
            contentViews.setSelectionMode(Style.SelectionMode.SINGLE);
        }
        contentViews.addSelectionListener(Events.BeforeSelect, new Listener<SelectionEvent>() {
            public void handleEvent(SelectionEvent be) {
                GWTJahiaNode selection = (GWTJahiaNode) be.getModel();
                if (selection != null) {
                    checkTypes(be, selection, config.getNodeTypes());
                    if (types != null) {
                        checkTypes(be, selection, types);
                    }
                }
            }
        });


        contentViews.selectNodes(selectedNodes);

        BottomRightComponent bottomComponents = new PickedContentView(selectedNodes, multiple, config);

        final TopBar toolbar = new ContentToolbar(config, linker) {

        };

        initWidgets(tree.getComponent(), contentViews.getComponent(), multiple ? bottomComponents.getComponent() : null, toolbar.getComponent(), null);

        // linker initializations
        linker.registerComponents(tree, contentViews, bottomComponents, toolbar, null);
        if (config.getContextMenu() != null) {
            final ActionContextMenu actionContextMenu = new ActionContextMenu(config.getContextMenu(), linker);
            tree.getComponent().setContextMenu(actionContextMenu);
            contentViews.getComponent().setContextMenu(actionContextMenu);
        }
        linker.handleNewSelection();
        pickedContent = (PickedContentView) bottomComponents;
    }

    private void checkTypes(SelectionEvent be, GWTJahiaNode selection, final List<String> nodeTypes) {
        boolean found = false;
        for (String s : nodeTypes) {
            if (selection.getNodeTypes().contains(s) || selection.getInheritedNodeTypes().contains(s)) {
                found = true;
                break;
            }
        }

        if (!found) {
            be.setCancelled(true);
        }
    }

    public List<GWTJahiaNode> getSelectedNodes() {
        return pickedContent.getSelectedContent();
    }

    public void setSaveButton(Button saveButton) {
        pickedContent.setSaveButton(saveButton);
    }


}
