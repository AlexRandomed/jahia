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

import com.allen_sauer.gwt.log.client.Log;
import com.google.gwt.user.client.Command;
import com.google.gwt.user.client.DeferredCommand;
import com.google.gwt.user.client.rpc.AsyncCallback;
import org.jahia.ajax.gwt.client.data.node.GWTJahiaNode;
import org.jahia.ajax.gwt.client.service.content.JahiaContentManagementService;
import org.jahia.ajax.gwt.client.widget.tripanel.*;
import org.jahia.ajax.gwt.client.util.content.actions.ManagerConfiguration;


/**
 * Created by IntelliJ IDEA.
 *
 * @author rfelden
 * @version 19 juin 2008 - 15:22:43
 */
public class ContentManager extends TriPanelBrowserViewport {

    public ContentManager(final String rootPath, final String types, final String filters, final String mimeTypes, final ManagerConfiguration config) {
        // superclass constructor (define linker)
        super();
        init(rootPath, types, filters, mimeTypes, config);

    }

    private void init(final String rootPath, String types, String filters, String mimeTypes, final ManagerConfiguration config) {
        if (types != null && types.length() > 0) {
            config.setNodeTypes(types);
        }
        if (mimeTypes != null && mimeTypes.length() > 0) {
            config.setMimeTypes(mimeTypes);
        }
        if (filters != null && filters.length() > 0) {
            config.setFilters(filters);
        }

        // construction of the UI components
        final LeftComponent tree;
        if (!config.isHideLeftPanel()) {
            tree = new ContentRepositoryTabs(config);
        } else {
            tree = null;
        }
        final ContentViews contentViews = new ContentViews(config);
        final BottomRightComponent tabs = new ContentDetails(config);
        final TopBar toolbar = new ContentToolbar(config, linker) {
            protected void setListView() {
                contentViews.switchToListView();
            }

            protected void setThumbView() {
                contentViews.switchToThumbView();
            }

            protected void setDetailedThumbView() {
                contentViews.switchToDetailedThumbView();
            }

            protected void setTemplateView() {
                contentViews.switchToTemplateView();
            }
        };
        BottomBar statusBar = new ContentStatusBar();

        // setup widgets in layout
        if (tree != null) {
            initWidgets(tree.getComponent(),
                    contentViews.getComponent(),
                    tabs.getComponent(),
                    toolbar.getComponent(),
                    statusBar.getComponent());
        } else {
            initWidgets(null,
                    contentViews.getComponent(),
                    tabs.getComponent(),
                    toolbar.getComponent(),
                    statusBar.getComponent());
        }

        // linker initializations
        linker.registerComponents(tree, contentViews, tabs, toolbar, statusBar);
        contentViews.initContextMenu();
        linker.handleNewSelection();
        if (config.isExpandRoot()) {
            DeferredCommand.addCommand(new Command() {
                public void execute() {
                    JahiaContentManagementService.App.getInstance().getNode(rootPath, new AsyncCallback<GWTJahiaNode>() {
                        public void onSuccess(GWTJahiaNode gwtJahiaNode) {
                            linker.setLeftPanelSelectionWhenHidden(gwtJahiaNode);
                            linker.refresh();
                        }

                        public void onFailure(Throwable throwable) {
                            Log.error("Unable to load node with path " + rootPath, throwable);
                        }
                    });
                }
            });
        } else {
            linker.handleNewSelection();
        }
    }
}
