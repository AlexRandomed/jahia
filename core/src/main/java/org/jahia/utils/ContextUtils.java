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

package org.jahia.utils;

import javax.servlet.ServletRequest;
import javax.servlet.jsp.PageContext;

import org.jahia.data.JahiaData;
import org.jahia.data.beans.JahiaBean;
import org.jahia.params.ProcessingContext;

/**
 * Utility class for accessing Jahia request data.
 * 
 * @author Sergiy Shyrkov
 */
public final class ContextUtils {

    /**
     * Returns an {@link JahiaBean} instance with current Jahia data.
     * 
     * @param pageContext
     *            current page context object
     * @return an {@link JahiaBean} instance with current Jahia data
     */
    public static JahiaBean getJahiaBean(PageContext pageContext) {
        return getJahiaBean(pageContext.getRequest());
    }

    /**
     * Returns an {@link JahiaBean} instance with current Jahia data.
     * 
     * @param request
     *            current request object
     * @return an {@link JahiaBean} instance with current Jahia data
     */
    public static JahiaBean getJahiaBean(ServletRequest request) {
        JahiaBean jBean = (JahiaBean) request.getAttribute("jahia");
        if (null == jBean) {
            throw new IllegalArgumentException(
                    "JahiaBean is not found in the request scope under name 'jahia'");
        }
        return jBean;
    }

    /**
     * Returns current {@link JahiaData} instance.
     * 
     * @param pageContext
     *            current page context object
     * @return current {@link JahiaData} instance
     */
    public static JahiaData getJahiaData(PageContext pageContext) {
        return getJahiaData(pageContext.getRequest());
    }

    /**
     * Returns current {@link JahiaData} instance.
     * 
     * @param processingContext
     *            current processing context object
     * @return current {@link JahiaData} instance
     */
    public static JahiaData getJahiaData(ProcessingContext processingContext) {
        return (JahiaData) processingContext
                .getAttribute("org.jahia.data.JahiaData");
    }

    /**
     * Returns current {@link JahiaData} instance.
     * 
     * @param request
     *            current request object
     * @return current {@link JahiaData} instance
     */
    public static JahiaData getJahiaData(ServletRequest request) {
        return (JahiaData) request.getAttribute("org.jahia.data.JahiaData");
    }
    
    /**
     * Returns current {@link ProcessingContext} instance.
     * 
     * @param pageContext
     *            current page context object
     * @return current {@link ProcessingContext} instance
     */
    public static ProcessingContext getProcessingContext(PageContext pageContext) {
        return getJahiaBean(pageContext).getProcessingContext();
    }

    /**
     * Returns current {@link ProcessingContext} instance.
     * 
     * @param request
     *            current request object
     * @return current {@link ProcessingContext} instance
     */
    public static ProcessingContext getProcessingContext(ServletRequest request) {
        return getJahiaBean(request).getProcessingContext();
    }


    /**
     * Initializes an instance of this class.
     */
    private ContextUtils() {
        super();
    }
}
