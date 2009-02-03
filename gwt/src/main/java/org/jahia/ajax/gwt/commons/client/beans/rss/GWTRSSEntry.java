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

package org.jahia.ajax.gwt.commons.client.beans.rss;

import com.extjs.gxt.ui.client.data.BaseModel;

import java.util.Date;
import java.util.List;

/**
 * User: jahia
 * Date: 13 ao�t 2008
 * Time: 14:55:18
 */
public class GWTRSSEntry extends BaseModel {
    public GWTRSSEntry() {
    }

    public GWTRSSEntry(String author, List<GWTRSSPerson> authors, List<GWTRSSCategory> categories, List<GWTRSSContent> contents,
                       List<GWTRSSPerson> contributors, String description, List<GWTRSSEnclosure> enclosures, String link,
                       List<String> links,
                       Date publishedDate, String title, Date updatedDate) {
        setAuthor(author);
        setAuthors(authors);
        setCategories(categories);
        setContents(contents);
        setContributors(contributors);
        setDescription(description);
        setShortDescription(getDescription());
        setEnclosures(enclosures);
        setLink(link);
        setLinks(links);
        setPublishedDate(publishedDate);
        setTitle(title);
        setUpdatedDate(updatedDate);
        set("authorLabel", "Author");
        set("publishedDateLabel", "Publish date");
        set("updatedDateLabel", "Updated date");
    }

    public String getAuthor() {
        return get("author");
    }

    public void setAuthor(String author) {
        set("author", author);
    }

    public List<GWTRSSPerson> getAuthors() {
        return get("authors");
    }

    public void setAuthors(List<GWTRSSPerson> authors) {
        set("authors", authors);
    }

    public List<GWTRSSCategory> getCategories() {
        return get("categories");
    }

    public void setCategories(List<GWTRSSCategory> categories) {
        set("categories", categories);
    }

    public List<GWTRSSContent> getContents() {
        return get("contents");
    }

    public void setContents(List<GWTRSSContent> contents) {
        this.set("contents", contents);
    }

    public List<GWTRSSPerson> getContributors() {
        return get("contributors");
    }

    public void setContributors(List<GWTRSSPerson> contributors) {
        set("contributors", contributors);
    }

    public String getDescription() {
        return get("description");
    }

    public void setDescription(String description) {
        set("description", description);
    }

    public String getShortDescription() {
        return get("shortDescription");
    }

    public void setShortDescription(String shortDescription) {
        set("shortDescription", shortDescription);
    }

    public List<GWTRSSEnclosure> getEnclosures() {
        return get("enclosures");
    }

    public void setEnclosures(List<GWTRSSEnclosure> enclosures) {
        set("enclosures", enclosures);
    }

    public String getLink() {
        return get("link");
    }

    public void setLink(String link) {
        set("link", link);
    }

    public List<String> getLinks() {
        return get("links");
    }

    public void setLinks(List<String> links) {
        set("links", links);
    }

    public Date getPublishedDate() {
        return get("publishedDate");
    }

    public void setPublishedDate(Date publishedDate) {
        set("publishedDate", publishedDate);
    }

    public String getTitle() {
        return get("title");
    }

    public void setTitle(String title) {
        set("title", title);
    }

    public Date getUpdatedDate() {
        return get("updatedDate");
    }

    public void setUpdatedDate(Date updatedDate) {
        set("updatedDate", updatedDate);
    }
}