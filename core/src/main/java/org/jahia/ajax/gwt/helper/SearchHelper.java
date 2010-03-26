package org.jahia.ajax.gwt.helper;

import org.apache.log4j.Logger;
import org.jahia.ajax.gwt.client.data.GWTJahiaSearchQuery;
import org.jahia.ajax.gwt.client.data.node.GWTJahiaNode;
import org.jahia.ajax.gwt.client.service.GWTJahiaServiceException;
import org.jahia.ajax.gwt.client.service.content.ExistingFileException;
import org.jahia.ajax.gwt.client.util.content.JCRClientUtils;
import org.jahia.services.content.*;
import org.jahia.services.search.SearchCriteria;
import org.jahia.services.search.SearchCriteria.Term.SearchFields;
import org.jahia.services.search.jcr.JahiaJCRSearchProvider;
import org.jahia.services.sites.JahiaSite;

import javax.jcr.RepositoryException;
import javax.jcr.query.InvalidQueryException;
import javax.jcr.query.Query;
import java.util.ArrayList;
import java.util.List;

/**
 * Search utility class.
 * User: toto
 * Date: Sep 28, 2009
 * Time: 2:23:56 PM
 */
public class SearchHelper {
    private static Logger logger = Logger.getLogger(SearchHelper.class);

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
            return navigation.executeQuery(q, new String[0], new String[0], new String[0]);
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
            String[] nodeTypesToApply = navigation.getFiltersToApply(search.getNodeTypes());
            String[] mimeTypesToMatch = navigation.getFiltersToApply(search.getMimeTypes());
            String[] filtersToApply = navigation.getFiltersToApply(search.getFilters());
            if (logger.isDebugEnabled()) {
                logger.debug("Executing query: " + q.getStatement());
            }
            return navigation.executeQuery(q, nodeTypesToApply, mimeTypesToMatch, filtersToApply);
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
    public List<GWTJahiaNode> search(String searchString, int limit, String nodeTypes, String mimeTypes, String filters, JCRSessionWrapper currentUserSession) throws GWTJahiaServiceException {
        if (nodeTypes == null) {
            nodeTypes = JCRClientUtils.FILE_NODETYPES;
        }
        String[] nodeTypesToApply = navigation.getFiltersToApply(nodeTypes);
        String[] mimeTypesToMatch = navigation.getFiltersToApply(mimeTypes);
        String[] filtersToApply = navigation.getFiltersToApply(filters);
        try {
            Query q = createQuery(formatQuery(searchString), currentUserSession);
            return navigation.executeQuery(q, nodeTypesToApply, mimeTypesToMatch, filtersToApply);
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
            return navigation.executeQuery(q, new String[0], new String[0], new String[0]);
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
    public GWTJahiaNode saveSearch(String searchString, String name, JahiaSite site, JCRSessionWrapper currentUserSession) throws GWTJahiaServiceException {
        try {
            if (name == null) {
                throw new GWTJahiaServiceException("Could not store query with null name");
            }
            Query q = createQuery(searchString, currentUserSession);
            List<JCRNodeWrapper> users = jcrService.getUserFolders(site.getSiteKey(), currentUserSession.getUser());
            if (users.isEmpty()) {
                logger.error("no user folder for site " + site.getSiteKey() + " and user " + currentUserSession.getUser().getUsername());
                throw new GWTJahiaServiceException("No user folder to store query");
            }
            JCRNodeWrapper user = users.iterator().next();
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
     * Simple save search
     *
     * @param searchString
     * @param path
     * @param name
     * @param currentUserSession
     * @return
     * @throws GWTJahiaServiceException
     */
    public GWTJahiaNode saveSearch(String searchString, String path, String name, JCRSessionWrapper currentUserSession) throws GWTJahiaServiceException {
        try {
            if (name == null) {
                throw new GWTJahiaServiceException("Could not store query with null name");
            }

            JCRNodeWrapper parent = currentUserSession.getNode(path);
            name = contentManager.findAvailableName(parent, name, currentUserSession);
            Query q = createQuery(searchString, currentUserSession);
            q.storeAsNode(path + "/" + name);
            parent.saveSession();

            return navigation.getGWTJahiaNode(currentUserSession.getNode(path + "/" + name));
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

            JCRNodeWrapper parent = session.getNode(path);
            name = contentManager.findAvailableName(parent, name, session);
            Query q = createQuery(search, session);
            q.storeAsNode(path + "/" + name);
            parent.saveSession();

            return navigation.getGWTJahiaNode(session.getNode(path + "/" + name));
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
    private Query createQuery(GWTJahiaSearchQuery gwtQuery, int limit, int offset, JCRSessionWrapper session) throws InvalidQueryException, RepositoryException {
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

        // language
        if (gwtQuery.getLanguage() != null && gwtQuery.getLanguage().getLanguage() != null) {
            criteria.getLanguages().setValue(gwtQuery.getLanguage().getLanguage());
        }

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
        }

        return jcrSearchProvider.buildQuery(criteria, session);
    }
}
