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

package org.jahia.ajax.gwt.templates.components.layoutmanager.client;

import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.RootPanel;
import com.allen_sauer.gwt.log.client.Log;
import org.jahia.ajax.gwt.templates.commons.client.module.JahiaModule;
import org.jahia.ajax.gwt.templates.commons.client.module.JahiaType;
import org.jahia.ajax.gwt.config.client.beans.GWTJahiaPageContext;
import org.jahia.ajax.gwt.templates.components.layoutmanager.client.ui.gxt.JahiaPortalManager;
import org.jahia.ajax.gwt.templates.components.layoutmanager.client.bean.GWTLayoutManagerConfig;
import org.jahia.ajax.gwt.templates.components.layoutmanager.client.service.LayoutmanagerService;

import java.util.List;


/**
 * Created by Jahia.
 * User: ktlili
 * Date: 31 oct. 2007
 * Time: 11:35:39
 */
public class JahiaLayoutManager extends JahiaModule {
    public static final String LAYOUT_MANAGER_ID = "layoutManager";
    public static final String LAYOUT_MANAGER_WIDTH = "lm-with";

    public String getJahiaModuleType() {
        return JahiaType.LAYOUT_MANAGER;
    }

    public void onModuleLoad(final GWTJahiaPageContext jahiaPageContext, final List<RootPanel> rootPanels) {
        if (rootPanels != null && rootPanels.size() == 1) {
            // get Root element attributes
            LayoutmanagerService.App.getInstance().getLayoutmanagerConfig(new AsyncCallback() {
                public void onSuccess(Object o) {
                    GWTLayoutManagerConfig gwtLayoutManagerConfig = (GWTLayoutManagerConfig) o;
                    if (o == null || gwtLayoutManagerConfig.getNbColumns() < 1) {
                        gwtLayoutManagerConfig = new GWTLayoutManagerConfig();
                        gwtLayoutManagerConfig.setNbColumns(3);
                        gwtLayoutManagerConfig.setLiveQuickbarVisible(true);
                        gwtLayoutManagerConfig.setLiveDraggable(true);
                    }


                    JahiaPortalManager jahiaPortal = new JahiaPortalManager(gwtLayoutManagerConfig);
                    rootPanels.get(0).add(jahiaPortal);
                }

                public void onFailure(Throwable throwable) {
                    Log.error("Error while loading portlet", throwable);
                    // load a defautl config
                    GWTLayoutManagerConfig gwtLayoutManagerConfig = new GWTLayoutManagerConfig();
                    gwtLayoutManagerConfig.setNbColumns(3);
                    gwtLayoutManagerConfig.setLiveQuickbarVisible(true);
                    gwtLayoutManagerConfig.setLiveDraggable(true);
                    JahiaPortalManager jahiaPortal = new JahiaPortalManager(gwtLayoutManagerConfig);
                    rootPanels.get(0).add(jahiaPortal);
                }
            });
        }
    }


}
