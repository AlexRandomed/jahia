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
package org.jahia.services.toolbar.bean;

import org.springframework.beans.factory.BeanNameAware;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * User: jahia
 * Date: 7 avr. 2008
 * Time: 09:04:48
 */
public class Toolbar implements Serializable, BeanNameAware {

    private String name;
    private String titleKey;
    private String type;
    private boolean displayed  = true;
    private boolean displayTitle = false;
    private List<Item> items = new ArrayList<Item>();
    private Visibility visibility;
    private boolean contextMenu;



    public Toolbar() {
    }

    public String getTitleKey() {
        return titleKey;
    }

    public void setTitleKey(String titleKey) {
        this.titleKey = titleKey;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public boolean isDisplayed() {
        return displayed;
    }

    public void setDisplayed(boolean displayed) {
        this.displayed = displayed;
    }

    public boolean isDisplayTitle() {
        return displayTitle;
    }

    public void setDisplayTitle(boolean displayTitle) {
        this.displayTitle = displayTitle;
    }

    public List<Item> getItems() {
        return items;
    }

    public void setItems(List<Item> items) {
        this.items = items;
    }


    public Visibility getVisibility() {
        return visibility;
    }

    public void setVisibility(Visibility visibility) {
        this.visibility = visibility;
    }

    public boolean isContextMenu() {
        return contextMenu;
    }

    public void setContextMenu(boolean contextMenu) {
        this.contextMenu = contextMenu;
    }

    public void setBeanName(String name) {
        setName(name);
        setType(name);
    }
}
