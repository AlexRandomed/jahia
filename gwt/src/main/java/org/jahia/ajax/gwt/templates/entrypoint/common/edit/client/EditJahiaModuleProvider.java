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

package org.jahia.ajax.gwt.templates.entrypoint.common.edit.client;

import org.jahia.ajax.gwt.templates.commons.client.module.JahiaModule;
import org.jahia.ajax.gwt.templates.commons.client.module.JahiaModuleProvider;
import org.jahia.ajax.gwt.templates.commons.client.module.JahiaType;
import org.jahia.ajax.gwt.templates.components.actionmenus.client.ActionMenu;
import org.jahia.ajax.gwt.templates.components.subscription.client.SubscriptionModule;
import org.jahia.ajax.gwt.templates.components.toolbar.client.ToolbarJahiaModule;
import org.jahia.ajax.gwt.templates.components.datefield.client.DateFieldJahiaModule;
import org.jahia.ajax.gwt.templates.components.mysettings.client.MySettingsJahiaModule;
import org.jahia.ajax.gwt.templates.components.opensearch.client.JahiaOpenSearchModule;
import org.jahia.ajax.gwt.templates.components.rss.client.RSSJahiaModule;
import org.jahia.ajax.gwt.templates.components.form.client.FormModule;
import org.jahia.ajax.gwt.templates.components.portletrender.client.PortletRenderModule;
import org.jahia.ajax.gwt.templates.components.inlineediting.client.InlineEditingModule;
import org.jahia.ajax.gwt.templates.components.calendar.client.CalendarJahiaModule;


/**
 * User: jahia
 * Date: 17 mars 2008
 * Time: 12:21:09
 */
public class EditJahiaModuleProvider extends JahiaModuleProvider {
    public JahiaModule getJahiaModuleByJahiaType(String jahiaType) {
        if (jahiaType != null) {
            if (jahiaType.equalsIgnoreCase(JahiaType.ACTION_MENU)) {
                return new ActionMenu();
            } else if (jahiaType.equalsIgnoreCase(JahiaType.TOOLBARS_MANAGER)) {
                return new ToolbarJahiaModule();
            } else if (jahiaType.equalsIgnoreCase(JahiaType.DATE_FIELD)) {
                return new DateFieldJahiaModule();
            } else if (jahiaType.equalsIgnoreCase(JahiaType.CALENDAR)) {
                return new CalendarJahiaModule();
            } else if (jahiaType.equalsIgnoreCase(JahiaType.RSS)) {
                return new RSSJahiaModule();
            } else if (jahiaType.equalsIgnoreCase(JahiaType.MY_SETTINGS)) {
                return new MySettingsJahiaModule();
            } else if (jahiaType.equalsIgnoreCase(JahiaType.SUBSCRIPTION)) {
                return new SubscriptionModule();
            } else if (jahiaType.equalsIgnoreCase(JahiaType.FORM)) {
                return new FormModule();
            } else if (jahiaType.equalsIgnoreCase(JahiaType.PORTLET_RENDER)) {
                return new PortletRenderModule();
            } else if (jahiaType.equalsIgnoreCase(JahiaType.INLINE_EDITING)) {
                return new InlineEditingModule();
            }
        }
        return null;
    }
}
