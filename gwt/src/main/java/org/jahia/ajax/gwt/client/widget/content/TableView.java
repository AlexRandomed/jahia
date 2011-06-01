/**
 * This file is part of Jahia: An integrated WCM, DMS and Portal Solution
 * Copyright (C) 2002-2010 Jahia Solutions Group SA. All rights reserved.
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
import com.extjs.gxt.ui.client.Style;
import com.extjs.gxt.ui.client.core.El;
import com.extjs.gxt.ui.client.data.*;
import com.extjs.gxt.ui.client.dnd.*;
import com.extjs.gxt.ui.client.event.*;
import com.extjs.gxt.ui.client.util.Rectangle;
import com.extjs.gxt.ui.client.widget.Component;
import com.extjs.gxt.ui.client.widget.grid.*;
import com.extjs.gxt.ui.client.widget.menu.Menu;
import com.google.gwt.user.client.Element;
import org.jahia.ajax.gwt.client.core.BaseAsyncCallback;
import org.jahia.ajax.gwt.client.data.node.GWTJahiaNode;
import org.jahia.ajax.gwt.client.data.toolbar.GWTManagerConfiguration;
import org.jahia.ajax.gwt.client.service.content.JahiaContentManagementService;
import org.jahia.ajax.gwt.client.util.content.actions.ContentActions;
import org.jahia.ajax.gwt.client.widget.NodeColumnConfigList;
import org.jahia.ajax.gwt.client.widget.edit.EditLinker;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Table view component for the content manager widget.
 *
 * @author rfelden
 * @version 20 juin 2008 - 09:53:08
 */
public class TableView extends AbstractView {
    private Grid<GWTJahiaNode> m_grid;

    public TableView(final GWTManagerConfiguration config) {
        super(config);

        NodeColumnConfigList columns = new NodeColumnConfigList(configuration.getTableColumns());
        columns.init();
        CheckBoxSelectionModel<GWTJahiaNode> checkboxSelectionModel = null;
        if (configuration.isAllowsMultipleSelection()) {
            checkboxSelectionModel = new CheckBoxSelectionModel<GWTJahiaNode>();
            columns.add(0, checkboxSelectionModel.getColumn());
        }

        m_grid = new Grid<GWTJahiaNode>(store, new ColumnModel(columns));
        m_grid.setBorders(true);
        if (columns.getAutoExpand() != null) {
            m_grid.setAutoExpandColumn(columns.getAutoExpand());
            m_grid.setAutoExpandMax(1000);
        }

        if (checkboxSelectionModel != null) {
            m_grid.setSelectionModel(checkboxSelectionModel);
            m_grid.addPlugin(checkboxSelectionModel);
        }
        selectionModel = m_grid.getSelectionModel();
        if (configuration.isAllowsMultipleSelection()) {
            selectionModel.setSelectionMode(Style.SelectionMode.MULTI);
        } else {
            selectionModel.setSelectionMode(Style.SelectionMode.SINGLE);
        }

        m_grid.addListener(Events.RowDoubleClick, new Listener<GridEvent>() {
            public void handleEvent(GridEvent event) {
                List<GWTJahiaNode> sel = selectionModel.getSelectedItems();
                if (sel != null && sel.size() == 1) {
                    GWTJahiaNode el = sel.get(0);
                    if (el.isFile()) {
                        if (config.isEnableFileDoubleClick()) {
                            if (el.isDisplayable()) {
                                ImagePopup.popImage(el);
                            } else {
                                ContentActions.download(getLinker());
                            }
                        }
                    } else {
                        if (!config.isHideLeftPanel()) {
                            getLinker().onTableItemDoubleClicked(sel.get(0));
                            setContent(sel.get(0));
                        } else {
                            Log.debug("Double click disable if left panle is hidden");
                        }
                    }
                }
            }
        });

    }

    @Override
    public void initWithLinker(ManagerLinker linker) {
        super.initWithLinker(linker);
        GridDragSource source = new GridDragSource(m_grid) {
            @Override protected void onDragDrop(DNDEvent e) {
            }
        };
        source.addDNDListener(linker.getDndListener());

        GridDropTarget target = new MyGridDropTarget(m_grid);
        target.setAllowSelfAsSource(true);
        target.addDNDListener(linker.getDndListener());
    }

    public void setContextMenu(Menu menu) {
        m_grid.setContextMenu(menu);
    }

    public Component getComponent() {
        return m_grid;
    }

    private class MyGridDropTarget extends GridDropTarget {
        private boolean before;

        public MyGridDropTarget(Grid grid) {
            super(grid);
        }

        @Override
        protected void showFeedback(DNDEvent event) {
            final GWTJahiaNode node = root;
            if ("index".equals(store.getSortField()) && store.getSortDir() == Style.SortDir.ASC &&
                    Boolean.TRUE.equals(node.get("hasOrderableChildNodes"))) {
                feedback = DND.Feedback.INSERT;
            } else {
                feedback = DND.Feedback.APPEND;
            }
            event.getStatus().setStatus(true);

            Element row = grid.getView().findRow(event.getTarget()).cast();

            if (row != null) {
                if (feedback == DND.Feedback.INSERT) {
                    int height = row.getOffsetHeight();
                    int quarter = height / 4;
                    int top = row.getAbsoluteTop();
                    int y = event.getClientY();

                    before = y < (top + quarter);
                    boolean after = y > (top + 3 * quarter);

                    if (before || after) {
                        int idx = grid.getView().findRowIndex(row);

                        showInsert(event, row);

                        if (after) {
                            activeItem = grid.getStore().getAt(idx+1);
                            before = true;
                        } else {
                            activeItem = grid.getStore().getAt(idx);
                        }

                        insertIndex = adjustIndex(event, idx);
                    } else {
                        activeItem = grid.getStore().getAt(grid.getView().findRowIndex(row));
                        Insert.get().hide();
                    }
                } else {
                    activeItem = grid.getStore().getAt(grid.getView().findRowIndex(row));
                }
            } else {
                activeItem = null;
                before = false;
                insertIndex = 0;
                if (feedback == DND.Feedback.INSERT) {
                    showInsert(event, (Element) grid.getView().getRow(grid.getStore().getCount()-1));
                }
            }
            if (!before) {
                GWTJahiaNode target;
                if (activeItem != null) {
                    target = (GWTJahiaNode) activeItem;
                } else {
                    target = node;
                }
                event.getStatus().setStatus(checkTarget(((List<GWTJahiaNode>) event.getData()).get(0), target));
            }
        }

        @Override
        protected void onDragDrop(DNDEvent dndEvent) {
            if (dndEvent.getStatus().getStatus()) {

                final BaseAsyncCallback callback = new BaseAsyncCallback() {
                    public void onSuccess(Object o) {
                        getLinker().loaded();
                        getLinker().refresh(EditLinker.REFRESH_ALL);
                    }
                };

                final List<GWTJahiaNode> gwtJahiaNodes = (List<GWTJahiaNode>) dndEvent.getData();
                final GWTJahiaNode source = gwtJahiaNodes.get(0);

                if (activeItem != null) {
                    final GWTJahiaNode target = (GWTJahiaNode) activeItem;
                        if (before) {
                            JahiaContentManagementService.App.getInstance().moveOnTopOf(source.getPath(), target.getPath(), callback);
                        } else {
                            JahiaContentManagementService.App.getInstance().moveAtEnd(source.getPath(), target.getPath(), callback);
                        }
                } else {
                    final GWTJahiaNode target = root;
                    JahiaContentManagementService.App.getInstance().moveAtEnd(source.getPath(), target.getPath(), callback);
                }
            }
        }

        private boolean checkTarget(GWTJahiaNode source, GWTJahiaNode target) {
            final Set<String> constraints = new HashSet(target.getChildConstraints());
            constraints.retainAll(source.getInheritedNodeTypes());
            return !constraints.isEmpty();
        }

        private int adjustIndex(DNDEvent event, int index) {
            Object data = event.getData();
            List<ModelData> models = prepareDropData(data, true);
            for (ModelData m : models) {
                int idx = grid.getStore().indexOf(m);
                if (idx > -1 && (before ? idx < index : idx <= index)) {
                    index--;
                }
            }
            return before ? index : index + 1;
        }

        private void showInsert(DNDEvent event, Element row) {
            Insert insert = Insert.get();
            insert.show(row);
            Rectangle rect = El.fly(row).getBounds();
            int y = !before ? (rect.y + rect.height - 4) : rect.y - 2;
            insert.el().setBounds(rect.x, y, rect.width, 6);
        }

    }
}
