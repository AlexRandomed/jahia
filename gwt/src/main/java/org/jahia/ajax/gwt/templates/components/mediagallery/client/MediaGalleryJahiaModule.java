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

package org.jahia.ajax.gwt.templates.components.mediagallery.client;

import org.jahia.ajax.gwt.templates.commons.client.module.JahiaModule;
import org.jahia.ajax.gwt.templates.commons.client.module.JahiaType;
import org.jahia.ajax.gwt.config.client.beans.GWTJahiaPageContext;
import org.jahia.ajax.gwt.filemanagement.client.ui.ThumbView;
import org.jahia.ajax.gwt.filemanagement.client.model.GWTJahiaNode;
import org.jahia.ajax.gwt.filemanagement.client.util.actions.ManagerConfigurationFactory;
import org.jahia.ajax.gwt.tripanelbrowser.client.BrowserLinker;
import com.google.gwt.user.client.ui.RootPanel;
import com.google.gwt.user.client.DOM;
import com.allen_sauer.gwt.log.client.Log;
import com.extjs.gxt.ui.client.GXT;

import java.util.List;

/**
 * @author Xavier Lawrence
 */
public class MediaGalleryJahiaModule extends JahiaModule {

    public String getJahiaModuleType() {
        return JahiaType.MEDIA_GALLERY;
    }

    public void onModuleLoad(GWTJahiaPageContext page, List<RootPanel> rootPanels) {
        GXT.init();
        try {
            for (RootPanel panel : rootPanels) {
                String path = DOM.getElementAttribute(panel.getElement(), "path");
                String conf = DOM.getElementAttribute(panel.getElement(), "config");
                BrowserLinker linker = new BrowserLinker() ;
                final ThumbView view = new ThumbView(ManagerConfigurationFactory.getConfiguration(conf, linker)) ;
                linker.registerComponents(null, view, null, null, null); ;
                final GWTJahiaNode directory = new GWTJahiaNode(null,null, null, path, null, null, null, null, false, false, false, null);
                view.setContent(directory);
                panel.add(view.getComponent());
            }
        } catch (Exception e) {
            Log.error("Error loading MediaGalleryJahiaModule", e);
        }
    }
}
