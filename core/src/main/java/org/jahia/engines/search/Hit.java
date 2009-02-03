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

package org.jahia.engines.search;

import java.util.Date;

/**
 * Describes basic properties of an abstract search hit item.
 * 
 * @author Sergiy Shyrkov
 */
public interface Hit {

    /**
     * Possible search hit types.
     * 
     * @author Sergiy Shyrkov
     */
    public enum Type {
        CONTAINER, FILE, FOLDER, PAGE;
    }

    /**
     * Returns the MIME type of the hit content, if applicable.
     * 
     * @return the MIME type of the hit content, if applicable
     */
    String getContentType();

    /**
     * Returns the content creation date.
     * 
     * @return the content creation date
     */
    Date getCreated();

    /**
     * Returns the resource author (creator).
     * 
     * @return the resource author (creator)
     */
    String getCreatedBy();

    /**
     * Returns the last modification date.
     * 
     * @return the last modification date
     */
    Date getLastModified();

    /**
     * Returns the last contributor.
     * 
     * @return the last contributor
     */
    String getLastModifiedBy();

    /**
     * Returns the URL to the hit page.
     * 
     * @return the URL to the hit page
     */
    String getLink();

    /**
     * Returns the raw hit object.
     * 
     * @return the raw hit object
     */
    Object getRawHit();

    /**
     * Returns the hit score.
     * 
     * @return the hit score
     */
    float getScore();

    /**
     * Returns the short description, abstract or excerpt of the hit's content.
     * 
     * @return the short description, abstract or excerpt of the hit's content
     */
    String getSummary();

    /**
     * Returns the file text content.
     *
     * @return the file content
     */
    String getContent();

    /**
     * Returns the title text.
     * 
     * @return the title text
     */
    String getTitle();

    /**
     * Returns the hit type.
     * 
     * @return the hit type
     */
    Type getType();

}