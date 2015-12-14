/**
 * ==========================================================================================
 * =                   JAHIA'S DUAL LICENSING - IMPORTANT INFORMATION                       =
 * ==========================================================================================
 *
 *                                 http://www.jahia.com
 *
 *     Copyright (C) 2002-2016 Jahia Solutions Group SA. All rights reserved.
 *
 *     THIS FILE IS AVAILABLE UNDER TWO DIFFERENT LICENSES:
 *     1/GPL OR 2/JSEL
 *
 *     1/ GPL
 *     ==================================================================================
 *
 *     IF YOU DECIDE TO CHOOSE THE GPL LICENSE, YOU MUST COMPLY WITH THE FOLLOWING TERMS:
 *
 *     "This program is free software; you can redistribute it and/or
 *     modify it under the terms of the GNU General Public License
 *     as published by the Free Software Foundation; either version 2
 *     of the License, or (at your option) any later version.
 *
 *     This program is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 *     GNU General Public License for more details.
 *
 *     You should have received a copy of the GNU General Public License
 *     along with this program; if not, write to the Free Software
 *     Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301, USA.
 *
 *     As a special exception to the terms and conditions of version 2.0 of
 *     the GPL (or any later version), you may redistribute this Program in connection
 *     with Free/Libre and Open Source Software ("FLOSS") applications as described
 *     in Jahia's FLOSS exception. You should have received a copy of the text
 *     describing the FLOSS exception, also available here:
 *     http://www.jahia.com/license"
 *
 *     2/ JSEL - Commercial and Supported Versions of the program
 *     ===================================================================================
 *
 *     IF YOU DECIDE TO CHOOSE THE JSEL LICENSE, YOU MUST COMPLY WITH THE FOLLOWING TERMS:
 *
 *     Alternatively, commercial and supported versions of the program - also known as
 *     Enterprise Distributions - must be used in accordance with the terms and conditions
 *     contained in a separate written agreement between you and Jahia Solutions Group SA.
 *
 *     If you are unsure which license is appropriate for your use,
 *     please contact the sales department at sales@jahia.com.
 *
 */
package org.apache.jackrabbit.core.query.lucene;

import org.apache.jackrabbit.core.query.PropertyTypeRegistry;
import org.apache.jackrabbit.core.query.lucene.constraint.Constraint;
import org.apache.jackrabbit.core.session.SessionContext;
import org.apache.jackrabbit.spi.commons.query.QueryNodeFactory;
import org.apache.lucene.analysis.Analyzer;
import org.jahia.api.Constants;
import org.jahia.utils.LuceneUtils;

import javax.jcr.query.InvalidQueryException;

public class JahiaQueryImpl extends QueryImpl {
    private Constraint constraint = null;
    private String statement = null;

    public JahiaQueryImpl(SessionContext sessionContext, SearchIndex index,
                          PropertyTypeRegistry propReg, String statement, String language,
                          QueryNodeFactory factory) throws InvalidQueryException {
        super(sessionContext, index, propReg, statement, language, factory);
        this.statement = statement;
    }

    public Constraint getConstraint() {
        return constraint;
    }

    public void setConstraint(Constraint constraint) {
        this.constraint = constraint;
    }

    @Override
    public boolean needsSystemTree() {
        return statement.contains(Constants.JCR_SYSTEM);
    }

    @Override
    protected Analyzer getTextAnalyzer() {
        final String lang = LuceneUtils.extractLanguageOrNullFromStatement(statement);

        if (lang != null) {
            final Analyzer analyzer = index.getAnalyzerRegistry().getAnalyzer(lang);
            return analyzer != null ? analyzer : super.getTextAnalyzer();
        }

        return super.getTextAnalyzer();
    }

}
