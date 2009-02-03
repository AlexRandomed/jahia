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

import com.allen_sauer.gwt.log.client.Log;
import com.extjs.gxt.ui.client.event.ComponentEvent;
import com.extjs.gxt.ui.client.event.SelectionListener;
import com.extjs.gxt.ui.client.event.BaseEvent;
import com.extjs.gxt.ui.client.event.Listener;
import com.extjs.gxt.ui.client.widget.Info;
import com.extjs.gxt.ui.client.widget.InfoConfig;
import com.extjs.gxt.ui.client.widget.toolbar.TextToolItem;
import com.extjs.gxt.ui.client.widget.toolbar.ToolItem;
import com.extjs.gxt.ui.client.GXT;
import com.google.gwt.user.client.Timer;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.DOM;
import com.google.gwt.user.client.rpc.AsyncCallback;
import org.jahia.ajax.gwt.commons.client.beans.GWTProperty;
import org.jahia.ajax.gwt.templates.components.toolbar.client.bean.GWTToolbarItem;
import org.jahia.ajax.gwt.templates.components.toolbar.client.bean.monitor.GWTJahiaStateInfo;
import org.jahia.ajax.gwt.templates.components.toolbar.client.service.ToolbarService;
import org.jahia.ajax.gwt.templates.components.toolbar.client.ui.Constants;

import java.util.Map;

/**
 * User: jahia
 * Date: 7 ao�t 2008
 * Time: 14:09:47
 */
public class NotificationJahiaToolItemProvider extends AbstractJahiaToolItemProvider {
    private GWTJahiaStateInfo gwtJahiaStateInfo;
    private final static int MAX_AJAX_CALL = 50;
    private int attempts = 0;


    public ToolItem createNewToolItem(final GWTToolbarItem gwtToolbarItem) {
        final TextToolItem toolbarItem = new TextToolItem();
        toolbarItem.setEnabled(false);
        final Map preferences = gwtToolbarItem.getProperties();

        // get refresh time
        final GWTProperty refreshTimeProp = (GWTProperty) preferences.get(Constants.NOTIFICATION_REFRESH_TIME);
        int refreshTime = 5000;
        if (refreshTimeProp != null) {
            try {
                refreshTime = Integer.parseInt(refreshTimeProp.getValue());
            } catch (NumberFormatException e) {
                Log.error("Refresh time value[" + refreshTime + "] is not an integer.");
            }
        }

        // timer
        final Timer timer = createStateInfoTimer(toolbarItem);

        // Schedule the timer to run each "timerRefresh/1000" seconds.
        timer.scheduleRepeating(refreshTime);

        return toolbarItem;
    }

    private Timer createStateInfoTimer(final TextToolItem toolbarItem) {
        Log.debug("create notification Info timer");
        final Timer timer = new Timer() {
            public void run() {
                ToolbarService.App.getInstance().updateGWTJahiaStateInfo(getJahiaGWTPageContext(), gwtJahiaStateInfo, new AsyncCallback<GWTJahiaStateInfo>() {
                    public void onFailure(Throwable throwable) {
                        Log.error("Unable to update pdisplay info timer", throwable);
                        toolbarItem.setIconStyle("gwt-toolbar-ItemsGroup-icons-notification-error");
                        attempts++;
                        if (attempts > 5) {
                            cancel();
                            Log.debug("5 attempds without success --> notification timer stopped.", throwable);
                        }
                    }

                    public void onSuccess(final GWTJahiaStateInfo currentGWTJahiaStateInfo) {
                        if (currentGWTJahiaStateInfo != null) {
                            toolbarItem.setIconStyle(currentGWTJahiaStateInfo.getIconStyle());
                            toolbarItem.setToolTip(currentGWTJahiaStateInfo.getText());

                            // current user job ended
                            if (currentGWTJahiaStateInfo.isCurrentUserJobEnded()) {
                                String alertMessage = "<a href=\"#\" onclick=\"window.open('" + currentGWTJahiaStateInfo.getGwtProcessJobInfo().getJobReportUrl() + "','report','width=700,height=500')\">" + currentGWTJahiaStateInfo.getAlertMessage() + "</a>";
                                InfoConfig infoConfig = new InfoConfig(currentGWTJahiaStateInfo.getGwtProcessJobInfo().getJobType(), alertMessage);
                                infoConfig.display = 9000;
                                infoConfig.height = 75;
                                infoConfig.listener = new Listener() {
                                    public void handleEvent(BaseEvent event) {
                                        Window.open(currentGWTJahiaStateInfo.getGwtProcessJobInfo().getJobReportUrl(), "report", "width=700,height=500");
                                    }
                                };
                                Info info = new Info();
                                if (GXT.isIE6) {
                                    info.setStyleAttribute("position", "absolute");
                                } else {
                                    info.setStyleAttribute("position", "fixed");
                                }
                                info.display(infoConfig);
                            }

                            // need refreah
                            if (currentGWTJahiaStateInfo.isNeedRefresh()) {
                                toolbarItem.setEnabled(true);
                                String message = "<a href=\"#\" onclick=\"location.reload() ;\">" + currentGWTJahiaStateInfo.getRefreshMessage() + "</a>";
                                InfoConfig infoConfig = new InfoConfig("", message);
                                infoConfig.display = 20000;
                                infoConfig.height = 75;
                                Info info = new Info();
                                if (GXT.isIE6) {
                                    info.setStyleAttribute("position", "absolute");
                                } else {
                                    info.setStyleAttribute("position", "fixed");
                                }
                                info.display(infoConfig);
                                // know that the user need to do a refresh
                                cancel();
                            } else {
                                toolbarItem.setEnabled(false);
                            }

                            gwtJahiaStateInfo = currentGWTJahiaStateInfo;
                            attempts = 0;

                        }


                    }
                });
            }
        };
        return timer;
    }

    /**
     * Executed when the item is clicked
     *
     * @param gwtToolbarItem
     * @return
     */
    public SelectionListener<ComponentEvent> getSelectListener(GWTToolbarItem gwtToolbarItem) {
        return new SelectionListener<ComponentEvent>() {
            public void componentSelected(ComponentEvent event) {
                ToolbarService.App.getInstance().updateGWTJahiaStateInfo(getJahiaGWTPageContext(), gwtJahiaStateInfo, new AsyncCallback<GWTJahiaStateInfo>() {
                    public void onFailure(Throwable throwable) {
                        Log.error("Unable to update pdisplay info timer", throwable);
                    }

                    public void onSuccess(GWTJahiaStateInfo currentGWTJahiaStateInfo) {
                        if (currentGWTJahiaStateInfo != null) {
                            if (currentGWTJahiaStateInfo.isNeedRefresh()) {
                                Window.Location.reload();
                            }
                        }
                    }
                });
            }
        };
    }
}
