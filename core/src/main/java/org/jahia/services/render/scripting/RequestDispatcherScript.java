/**
 * This file is part of Jahia: An integrated WCM, DMS and Portal Solution
 * Copyright (C) 2002-2010 Jahia Solutions Group SA. All rights reserved.
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

package org.jahia.services.render.scripting;

import org.jahia.utils.StringResponseWrapper;
import org.slf4j.Logger;
import org.jahia.services.render.*;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpServletResponseWrapper;
import java.io.*;
import java.util.Enumeration;

/**
 * This class uses the standard request dispatcher to execute a JSP / Quercus script or any file handled by the
 * application server.
 *
 * The template path will be used as a resource path on which the request will be dispatched.
 *
 * @author toto
 */
public class RequestDispatcherScript implements Script {

    private static final Logger logger = org.slf4j.LoggerFactory.getLogger(RequestDispatcherScript.class);

    private RequestDispatcher rd;
    private HttpServletRequest request;
    private HttpServletResponse response;

    private Template template;

    /**
     * Builds the script object
     *
     */
    public RequestDispatcherScript(Template template) {
        this.template = template;
    }

    /**
     * Execute the script and return the result as a string
     *
     * @param resource resource to display
     * @param context
     * @return the rendered resource
     * @throws org.jahia.services.render.RenderException
     */
    public String execute(Resource resource, RenderContext context) throws RenderException {
        if (template == null) {
            throw new RenderException("Template not found for : " + resource);
        } else {
            if (logger.isDebugEnabled()) {
                logger.debug("Template '" + template + "' resolved for resource: " + resource);
            }
        }

        this.request = context.getRequest();
        this.response = context.getResponse();
        rd = request.getRequestDispatcher(template.getPath());

        Object oldModule = request.getAttribute("currentModule");
        request.setAttribute("currentModule", template.getModule());

        if (logger.isDebugEnabled()) {
            // Let's enumerate request attribute to see what we are exposing.
            Enumeration attributeNamesEnum = request.getAttributeNames();
            while (attributeNamesEnum.hasMoreElements()) {
                String currentAttributeName = (String) attributeNamesEnum.nextElement();
                String currentAttributeValue = request.getAttribute(currentAttributeName).toString();
                if (currentAttributeValue.length() < 80) {
                    logger.debug("Request attribute " + currentAttributeName + "=" + currentAttributeValue);
                } else {
                    logger.debug("Request attribute " + currentAttributeName + "=" + currentAttributeValue.substring(0,
                            80) + " (first 80 chars)");
                }
            }
        }
        StringResponseWrapper wrapper = new StringResponseWrapper(response);
        try {
            rd.include(request, wrapper);
        } catch (ServletException e) {
            throw new RenderException(e.getRootCause() != null ? e.getRootCause() : e);
        } catch (IOException e) {
            throw new RenderException(e);
        } finally {
            request.setAttribute("currentModule", oldModule);
        }
        try {
            return wrapper.getString();
        } catch (IOException e) {
            throw new RenderException(e);
        }
    }

    /**
     * Provides access to the template associated with this script
     *
     * @return the Template instance that will be executed
     */
    public Template getTemplate() {
        return template;
    }

}
