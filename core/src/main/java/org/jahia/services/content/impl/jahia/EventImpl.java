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
 * in Jahia's FLOSS exception. You should have recieved a copy of the text
 * describing the FLOSS exception, and it is also available here:
 * http://www.jahia.com/license"
 * 
 * Commercial and Supported Versions of the program
 * Alternatively, commercial and supported versions of the program may be used
 * in accordance with the terms contained in a separate written agreement
 * between you and Jahia Limited. If you are unsure which license is appropriate
 * for your use, please contact the sales department at sales@jahia.com.
 */

package org.jahia.services.content.impl.jahia;

import javax.jcr.observation.Event;

/**
 * Created by IntelliJ IDEA.
 * User: toto
 * Date: Jul 2, 2008
 * Time: 2:21:09 PM
 * To change this template use File | Settings | File Templates.
 */
public class EventImpl implements Event {

    private int type;
    private String path;
    private String userID;

    public EventImpl(int type, String path, String userID) {
        this.type = type;
        this.path = path;
        this.userID = userID;
    }

    public int getType() {
        return type;
    }

    public String getPath() {
        return path;
    }

    public String getUserID() {
        return userID;
    }

    @Override
    public String toString() {
        return "EventImpl{" +
                "type=" + type +
                ", path='" + path + '\'' +
                ", userID='" + userID + '\'' +
                '}';
    }

    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        EventImpl event = (EventImpl) o;

        if (type != event.type) return false;
        if (path != null ? !path.equals(event.path) : event.path != null) return false;
        if (userID != null ? !userID.equals(event.userID) : event.userID != null) return false;

        return true;
    }

    public int hashCode() {
        int result;
        result = type;
        result = 31 * result + (path != null ? path.hashCode() : 0);
        result = 31 * result + (userID != null ? userID.hashCode() : 0);
        return result;
    }
}
