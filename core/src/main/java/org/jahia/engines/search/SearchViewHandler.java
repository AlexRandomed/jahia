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

import java.util.List;
import java.util.Map;

import org.jahia.data.search.JahiaSearchResult;
import org.jahia.exceptions.JahiaException;
import org.jahia.params.ProcessingContext;
import org.jahia.services.search.savedsearch.JahiaSavedSearch;

/**
 * Created by IntelliJ IDEA.
 * User: hollis
 * Date: 8 f�vr. 2005
 * Time: 15:02:10
 * To change this template use File | Settings | File Templates.
 */
public interface SearchViewHandler {

    public static final String SEARCH_MODE = "searchMode";
    public static final int SEARCH_MODE_WEBSITE = 1;
    public static final int SEARCH_MODE_WEBDAV = 2;
    public static final int SEARCH_MODE_JCR = 3;

    /**
     * The searchViewHandler name
     *
     * @param name
     */
    public abstract void setName(String name);

    public abstract String getName();

    /**
     * Retrieves params from request
     *
     * @param jParams
     * @param engineMap
     */
    public abstract void init(ProcessingContext jParams, Map<String, Object> engineMap)
    throws JahiaException;

    /**
     * handles search operations like search option update
     *
     * @param jParams
     * @param engineMap
     */
    public abstract void update(ProcessingContext jParams, Map<String, Object> engineMap)
    throws JahiaException;

    /**
     * Returns the search result of the last performed query
     *
     * @return
     */
    public abstract JahiaSearchResult getSearchResult();

    /**
     * Executes search
     *
     * @param jParams
     * @return
     * @throws JahiaException
     */
    public abstract JahiaSearchResult search(ProcessingContext jParams)  throws JahiaException;

    /**
     * Return the full query
     * @return
     */
    public abstract String getQuery();

    public abstract void setQuery(String query);

    /**
     * Return the saved search as string
     *
     * @param jParams
     * @return
     * @throws JahiaException
     */
    public abstract String getSaveSearchDoc(ProcessingContext jParams) throws JahiaException;

    /**
     * Use the given savedSearch as initial state
     *
     * @param jParams
     * @param savedSearch
     * @throws JahiaException
     */
    public abstract void useSavedSearch(ProcessingContext jParams, JahiaSavedSearch savedSearch) throws JahiaException;

    public abstract int getSearchMode();

    public abstract void setSearchMode(int searchMode);

    public abstract boolean isSearchModeChanged();

    public abstract boolean isWebSiteSearch();

    public abstract List<JahiaSavedSearch> getSavedSearches() throws JahiaException;

}
