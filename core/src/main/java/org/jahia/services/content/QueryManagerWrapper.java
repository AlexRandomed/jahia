/**
 * This file is part of Jahia, next-generation open source CMS:
 * Jahia's next-generation, open source CMS stems from a widely acknowledged vision
 * of enterprise application convergence - web, search, document, social and portal -
 * unified by the simplicity of web content management.
 *
 * For more information, please visit http://www.jahia.com.
 *
 * Copyright (C) 2002-2014 Jahia Solutions Group SA. All rights reserved.
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

package org.jahia.services.content;

import org.jahia.services.query.QueryWrapper;

import javax.jcr.Node;
import javax.jcr.RepositoryException;
import javax.jcr.query.InvalidQueryException;
import javax.jcr.query.QueryManager;
import javax.jcr.query.qom.QueryObjectModelFactory;

/**
 * Extension of the QueryManager interface, with wrapped return types
 */
public interface QueryManagerWrapper extends QueryManager {

    /**
     * Creates a new query by specifying the query <code>statement</code> itself
     * and the <code>language</code> in which the query is stated. The
     * <code>language</code> must be a string from among those returned by
     * QueryManager.getSupportedQueryLanguages().
     *
     * @param statement a <code>String</code>
     * @param language  a <code>String</code>
     * @return a <code>Query</code> object
     * @throws javax.jcr.query.InvalidQueryException if the query statement is syntactically
     *                               invalid or the specified language is not supported.
     * @throws javax.jcr.RepositoryException   if another error occurs.
     */
    public QueryWrapper createQuery(String statement, String language) throws InvalidQueryException, RepositoryException;

    /**
     * Creates a new query by specifying the query <code>statement</code> in xpath and
     * in SQL2. XPath will be used against jackrabbit where SQL2 can be used as a fallback statement
     * in other providers, which may not support xpath.
     *
     * QueryManager.getSupportedQueryLanguages().
     *
     * @param statement a <code>String</code>
     * @param language  a <code>String</code>
     * @param sqlFallbackStatement  a <code>String</code>
     * @return a <code>Query</code> object
     * @throws javax.jcr.query.InvalidQueryException if the query statement is syntactically
     *                               invalid or the specified language is not supported.
     * @throws javax.jcr.RepositoryException   if another error occurs.
     */
    public QueryWrapper createDualQuery(String statement, String language, String sqlFallbackStatement) throws InvalidQueryException, RepositoryException;

    /**
     * Returns a <code>QueryObjectModelFactory</code> with which a JCR-JQOM
     * query can be built programmatically.
     *
     * @return a <code>QueryObjectModelFactory</code> object
     * @since JCR 2.0
     */
    public QueryObjectModelFactory getQOMFactory();

    /**
     * Retrieves an existing persistent query.
     * <p>
     * Persistent queries are created by first using {@link
     * QueryManager#createQuery} to create a <code>Query</code> object and then
     * calling <code>Query.save</code> to persist the query to a location in the
     * workspace.
     *
     * @param node a persisted query (that is, a node of type
     *             <code>nt:query</code>).
     * @return a <code>Query</code> object.
     * @throws InvalidQueryException If <code>node</code> is not a valid
     *                               persisted query (that is, a node of type <code>nt:query</code>).
     * @throws RepositoryException   if another error occurs
     */
    public QueryWrapper getQuery(Node node) throws InvalidQueryException, RepositoryException;



}
