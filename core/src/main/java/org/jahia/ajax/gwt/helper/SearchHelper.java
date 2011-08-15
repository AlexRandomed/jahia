/**
 * This file is part of Jahia, next-generation open source CMS:
 * Jahia's next-generation, open source CMS stems from a widely acknowledged vision
 * of enterprise application convergence - web, search, document, social and portal -
 * unified by the simplicity of web content management.
 *
 * For more information, please visit http://www.jahia.com.
 *
 * Copyright (C) 2002-2011 Jahia Solutions Group SA. All rights reserved.
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
 * Commercial and Supported Versions of the program (dual licensing):
 * alternatively, commercial and supported versions of the program may be used
 * in accordance with the terms and conditions contained in a separate
 * written agreement between you and Jahia Solutions Group SA.
 *
 * If you are unsure which license is appropriate for your use,
 * please contact the sales department at sales@jahia.com.
 */

package org.jahia.ajax.gwt.helper;

import org.slf4j.Logger;
import org.jahia.ajax.gwt.client.data.GWTJahiaSearchQuery;
import org.jahia.ajax.gwt.client.data.node.GWTJahiaNode;
import org.jahia.ajax.gwt.client.service.GWTJahiaServiceException;
import org.jahia.ajax.gwt.client.service.content.ExistingFileException;
import org.jahia.services.content.JCRCallback;
import org.jahia.services.content.JCRNodeWrapper;
import org.jahia.services.content.JCRSessionWrapper;
import org.jahia.services.content.JCRStoreService;
import org.jahia.services.content.JCRTemplate;
import org.jahia.services.content.decorator.JCRSiteNode;
import org.jahia.services.search.SearchCriteria;
import org.jahia.services.search.SearchCriteria.Term.SearchFields;
import org.jahia.services.search.jcr.JahiaJCRSearchProvider;

import javax.jcr.ItemNotFoundException;
import javax.jcr.RepositoryException;
import javax.jcr.query.InvalidQueryException;
import javax.jcr.query.Query;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

/**
 * Search utility class.
 * User: toto
 * Date: Sep 28, 2009
 * Time: 2:23:56 PM
 */
public class SearchHelper {
    private static Logger logger = org.slf4j.LoggerFactory.getLogger(SearchHelper.class);

    private JCRStoreService jcrService;

    private NavigationHelper navigation;
    private ContentManagerHelper contentManager;

    private JahiaJCRSearchProvider jcrSearchProvider;

    public void setJcrService(JCRStoreService jcrService) {
        this.jcrService = jcrService;
    }

    public void setNavigation(NavigationHelper navigation) {
        this.navigation = navigation;
    }

    public void setContentManager(ContentManagerHelper contentManager) {
        this.contentManager = contentManager;
    }

    /**
     * Search for searchString in the name f the node
     *
     * @param searchString
     * @param limit
     * @param currentUserSession
     * @return
     * @throws GWTJahiaServiceException
     */
    public List<GWTJahiaNode> search(String searchString, int limit, JCRSessionWrapper currentUserSession) throws GWTJahiaServiceException {
        try {
            Query q = createQuery(formatQuery(searchString), currentUserSession);
            return navigation.executeQuery(q, null,null,null);
        } catch (RepositoryException e) {
            logger.error(e.getMessage(), e);
        }
        return new ArrayList<GWTJahiaNode>();
    }

    /**
     * Search by Serach bean (used by the advanced search)
     *
     * @param search
     * @param limit
     * @param offset
     * @param currentUserSession
     * @return
     * @throws GWTJahiaServiceException
     */
    public List<GWTJahiaNode> search(GWTJahiaSearchQuery search, int limit, int offset, JCRSessionWrapper currentUserSession) throws GWTJahiaServiceException {
        try {
            Query q = createQuery(search, limit, offset, currentUserSession);
            if (logger.isDebugEnabled()) {
                logger.debug("Executing query: " + q.getStatement());
            }
            return navigation.executeQuery(q, search.getNodeTypes(), search.getMimeTypes(), search.getFilters());
        } catch (RepositoryException e) {
            logger.error(e.getMessage(), e);
        }
        return new ArrayList<GWTJahiaNode>();
    }

    /**
     * Search for searchString and filters in the name f the node
     *
     * @param searchString
     * @param limit
     * @param nodeTypes
     * @param mimeTypes
     * @param filters
     * @param currentUserSession
     * @return
     * @throws GWTJahiaServiceException
     */
    public List<GWTJahiaNode> search(String searchString, int limit, List<String> nodeTypes, List<String> mimeTypes, List<String> filters, JCRSessionWrapper currentUserSession) throws GWTJahiaServiceException {
        try {
            Query q = createQuery(formatQuery(searchString), currentUserSession);
            return navigation.executeQuery(q, nodeTypes, mimeTypes, filters);
        } catch (RepositoryException e) {
            logger.error(e.getMessage(), e);
        }
        return new ArrayList<GWTJahiaNode>();
    }

    /**
     * Search for searchString and filters in the name f the node
     *
     * @param searchString
     * @param limit
     * @param nodeTypes
     * @param mimeTypes
     * @param filters
     * @param currentUserSession
     * @return
     * @throws GWTJahiaServiceException
     */
    public List<GWTJahiaNode> searchSQL(String searchString, int limit, List<String> nodeTypes, List<String> mimeTypes,
                                        List<String> filters, List<String> fields, JCRSessionWrapper currentUserSession) throws GWTJahiaServiceException {
        try {
            Query q = currentUserSession.getWorkspace().getQueryManager().createQuery(searchString,Query.JCR_SQL2);
            q.setLimit(limit);
            return navigation.executeQuery(q, nodeTypes, mimeTypes, filters,fields);
        } catch (RepositoryException e) {
            logger.error(e.getMessage(), e);
        }
        return new ArrayList<GWTJahiaNode>();
    }

    /**
     * Get saved search
     *
     * @param currentUserSession
     * @return
     */
    public List<GWTJahiaNode> getSavedSearch(JCRSessionWrapper currentUserSession) {
        List<GWTJahiaNode> result = new ArrayList<GWTJahiaNode>();
        try {
            String s = "select * from [nt:query]";
            Query q = currentUserSession.getWorkspace().getQueryManager().createQuery(s, Query.JCR_SQL2);
            return navigation.executeQuery(q, null,null,null);
        } catch (RepositoryException e) {
            logger.error(e.getMessage(), e);
        }
        return result;
    }


    /**
     * Save search
     *
     * @param searchString
     * @param name
     * @param site
     * @param currentUserSession
     * @return
     * @throws GWTJahiaServiceException
     */
    public GWTJahiaNode saveSearch(String searchString, String name, JCRSiteNode site, JCRSessionWrapper currentUserSession) throws GWTJahiaServiceException {
        try {
            if (name == null) {
                throw new GWTJahiaServiceException("Could not store query with null name");
            }
            Query q = createQuery(searchString, currentUserSession);
            JCRNodeWrapper user;
            try {
                user = jcrService.getUserFolder(currentUserSession.getUser());
            } catch (Exception e) {
                logger.error("no user folder for site " + site.getSiteKey() + " and user " + currentUserSession.getUser().getUsername());
                throw new GWTJahiaServiceException("No user folder to store query");
            }

            JCRNodeWrapper queryStore;
            if (!user.hasNode("savedSearch")) {
                currentUserSession.checkout(user);
                queryStore = user.createCollection("savedSearch");
            } else {
                queryStore = currentUserSession.getNode(user.getPath() + "/savedSearch");
                currentUserSession.checkout(queryStore);
            }
            String path = queryStore.getPath() + "/" + name;
            if (contentManager.checkExistence(path, currentUserSession)) {
                throw new ExistingFileException("The node " + path + " alreadey exists.");
            }
            q.storeAsNode(path);
            user.getSession().save();
            return navigation.getGWTJahiaNode(currentUserSession.getNode(path));
        } catch (RepositoryException e) {
            logger.error(e.getMessage(), e);
            throw new GWTJahiaServiceException("Could not store query");
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            throw new GWTJahiaServiceException("Could not store query");
        }
    }

    /**
     * Save search
     *
     * @param search
     * @param path
     * @param name
     * @param session
     * @return
     * @throws GWTJahiaServiceException
     */
    public GWTJahiaNode saveSearch(GWTJahiaSearchQuery search, String path, String name, JCRSessionWrapper session) throws GWTJahiaServiceException {
        try {
            if (name == null) {
                throw new GWTJahiaServiceException("Could not store query with null name");
            }

            JCRNodeWrapper parent = null;
            if (path == null) {
                try {
                    parent = jcrService.getUserFolder(session.getUser());
                } catch (Exception e) {
                    logger.error("there is no defined user floder.",e);
                }
            } else {
                parent = session.getNode(path);
            }

            final String saveSearchPath = parent.getPath() + "/" + contentManager.findAvailableName(parent, name);
            parent.checkout();
            logger.debug("Save search path: " + saveSearchPath);
            Query q = createQuery(search, session);

            q.storeAsNode(saveSearchPath);

            session.save();

            return navigation.getGWTJahiaNode(session.getNode(saveSearchPath));
        } catch (RepositoryException e) {
            logger.error(e.getMessage(), e);
            throw new GWTJahiaServiceException("Could not store query");
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            throw new GWTJahiaServiceException("Could not store query");
        }
    }


    /**
     * Add "*" at beginning and end of query if not present in original search string.
     * Ex: *query   -->   *query
     * query*   -->   query*
     * query    -->   *query*
     *
     * @param rawQuery the raw query string
     * @return formatted query string
     */
    public static String formatQuery(String rawQuery) {
        if (rawQuery == null || rawQuery.length() == 0) {
            return "";
        } else if (rawQuery.startsWith("*") || rawQuery.endsWith("*")) {
            return rawQuery;
        } else {
            return new StringBuilder("*").append(rawQuery).append("*").toString();
        }
    }

    /**
     * @param jcrSearchProvider the jcrSearchProvider to set
     */
    public void setJcrSearchProvider(JahiaJCRSearchProvider jcrSearchProvider) {
        this.jcrSearchProvider = jcrSearchProvider;
    }

    /**
     * Creates the {@link Query} instance from the provided search criteria.
     *
     * @param searchString
     * @param session
     * @return
     * @throws RepositoryException
     */
    public Query createQuery(String searchString, JCRSessionWrapper session) throws RepositoryException {
        SearchCriteria criteria = new SearchCriteria();
        criteria.getTerms().get(0).setTerm(searchString);
        return jcrSearchProvider.buildQuery(criteria, session);
    }

    /**
     * Create JCR query
     *
     * @param gwtQuery
     * @param session
     * @return
     * @throws InvalidQueryException
     * @throws RepositoryException
     */
    private Query createQuery(GWTJahiaSearchQuery gwtQuery, JCRSessionWrapper session) throws InvalidQueryException, RepositoryException {
        return createQuery(gwtQuery, 0, 0, session);
    }

    /**
     * Creates the {@link Query} instance from the provided search criteria.
     *
     * @param gwtQuery the search criteria bean
     * @param session  current JCR session
     * @return the {@link Query} instance, created from the provided search criteria
     * @throws RepositoryException
     * @throws InvalidQueryException
     */
    private Query createQuery(final GWTJahiaSearchQuery gwtQuery, int limit, int offset, JCRSessionWrapper session) throws InvalidQueryException, RepositoryException {
        SearchCriteria criteria = new SearchCriteria();
        if (offset > 0) {
            criteria.setOffset(offset);
        }
        if (limit > 0) {
            criteria.setLimit(limit);
        }

        // page path
        if (gwtQuery.getPages() != null && !gwtQuery.getPages().isEmpty()) {
            criteria.getPagePath().setValue(gwtQuery.getPages().get(0).getPath());
            criteria.getPagePath().setIncludeChildren(true);
        }

        // nodeType
        if (gwtQuery.getNodeTypes() != null && gwtQuery.getNodeTypes().size() == 1) {
            criteria.setNodeType(gwtQuery.getNodeTypes().get(0));
            gwtQuery.setNodeTypes(new LinkedList<String>());
        }        
        
        // language
        if (gwtQuery.getLanguage() != null && gwtQuery.getLanguage().getLanguage() != null) {
            criteria.getLanguages().setValue(gwtQuery.getLanguage().getLanguage());
        }

//        // category
//        if (gwtQuery.getCategories() != null && !gwtQuery.getCategories().isEmpty()) {
//            criteria.getLanguages().setValue(gwtQuery.getLanguage().getLanguage());
//        }
//
        // query string
        if (gwtQuery.getQuery() != null && gwtQuery.getQuery().length() > 0) {
            criteria.getTerms().get(0).setTerm(gwtQuery.getQuery());
            SearchFields fields = criteria.getTerms().get(0).getFields();
            fields.setSiteContent(gwtQuery.isInContents());
            fields.setFilename(gwtQuery.isInName());
            fields.setFileContent(gwtQuery.isInFiles());
            fields.setTitle(gwtQuery.isInMetadatas());
            fields.setDescription(gwtQuery.isInMetadatas());
            fields.setKeywords(gwtQuery.isInMetadatas());
            fields.setTags(gwtQuery.isInTags());
        }
        
        if (gwtQuery.getOriginSiteUuid() != null) {
            String siteKey = JCRTemplate.getInstance().doExecuteWithSystemSession(new JCRCallback<String>() {
                public String doInJCR(JCRSessionWrapper session) throws RepositoryException {
                    try {
                        JCRNodeWrapper nodeWrapper = session.getNodeByIdentifier(gwtQuery.getOriginSiteUuid());
                        return nodeWrapper.getName();
                    } catch (ItemNotFoundException e) {
                        logger.error("Unable for find site node by UUID: " + gwtQuery.getOriginSiteUuid(), e);
                    }
                    return null;
                }
            });
            if (siteKey != null) {
                criteria.setOriginSiteKey(siteKey);
            }
            
        }

        return jcrSearchProvider.buildQuery(criteria, session);
    }
}
