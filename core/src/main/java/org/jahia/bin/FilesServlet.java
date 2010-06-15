/**
 * This file is part of Jahia: An integrated WCM, DMS and Portal Solution
 * Copyright (C) 2002-2009 Jahia Solutions Group SA. All rights reserved.
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
package org.jahia.bin;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.jackrabbit.util.Text;
import org.apache.log4j.Logger;
import org.jahia.exceptions.JahiaInitializationException;
import org.jahia.params.ProcessingContext;
import org.jahia.services.cache.Cache;
import org.jahia.services.cache.CacheFactory;
import org.jahia.services.content.decorator.JCRFileContent;
import org.jahia.services.content.JCRNodeWrapper;
import org.jahia.services.content.JCRSessionFactory;
import org.jahia.services.usermanager.JahiaUser;

import javax.jcr.RepositoryException;
import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Date;

/**
 * Serves resources from the JCR repository.
 *
 * @author Thomas Draier
 *         Date: Oct 13, 2008
 *         Time: 2:08:59 PM
 */
public class FilesServlet extends HttpServlet {

    private static Logger logger = Logger.getLogger(FilesServlet.class);

    public static Cache<String, byte[]> cache;

    private int cacheThreshold = 64 * 1024;

    static {
        try {
            cache = CacheFactory.getInstance().createCacheInstance("WebdavCache");
        } catch (JahiaInitializationException e) {
            e.printStackTrace();
        }
    }

    public void init(ServletConfig config) throws ServletException {
        super.init(config);
        String value = config.getInitParameter("cache-threshold");
        if (value != null) {
            cacheThreshold = new Integer(value);
        }

    }

    protected void doGet(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException {
        String p = req.getRequestURI();
        if (!req.getContextPath().equals("/") && p.startsWith(req.getContextPath())) {
            p = p.substring(req.getContextPath().length());
        }
        if (p.startsWith(req.getServletPath())) {
            p = p.substring(req.getServletPath().length()+1);
        }
        String workspace = StringUtils.substringBefore(p,"/");
        p = Text.unescape("/"+StringUtils.substringAfter(p,"/").replaceAll("___",":"));

        JCRNodeWrapper n;
        try {
            n = JCRSessionFactory.getInstance().getCurrentUserSession(workspace).getNode(p);
        } catch (RepositoryException e) {
            logger.error("Error accesing path : "+p+" for user "+(JahiaUser) req.getSession().getAttribute(ProcessingContext.SESSION_USER),e);
            res.sendError(HttpServletResponse.SC_NOT_FOUND,e.getLocalizedMessage());
            return;
        }

        boolean valid = false;
        String v = req.getParameter("v");
        if (v != null) {
            n = n.getFrozenVersion(v);
            if (n != null) {
                valid = true;
            }
        } else {
            valid = n.isFile();
        }

        if (valid) {
            // check presence of the 'If-Modified-Since' header
            long modifiedSince = req.getDateHeader("If-Modified-Since");
            Date lastModified = n.getLastModifiedAsDate();
            if (lastModified != null && modifiedSince > -1
                    && lastModified.getTime() / 1000 * 1000 <= modifiedSince) {
                res.setStatus(HttpServletResponse.SC_NOT_MODIFIED);
                return;
            }

            JCRFileContent fileContent = n.getFileContent();
            res.setContentType(fileContent.getContentType());
            int contentLength = (int) fileContent.getContentLength();
            res.setContentLength(contentLength);

            ServletOutputStream os = res.getOutputStream();
            if (lastModified != null) {
                // set 'Last-Modified' response header
                res.setDateHeader("Last-Modified", lastModified.getTime());
            }
            InputStream is = null;

            if (contentLength < cacheThreshold) {
                String cacheKey = p + ":" + (v==null ? "0" : v);
                byte[] b = (byte[]) cache.get(cacheKey);
                if (b == null) {
                    is = fileContent.downloadFile();
                    if(is!=null) {
                        try {
                            b = new byte[contentLength];
                            int i = is.read(b);
                            if(i>0) {
                                cache.put(cacheKey,b);
                            }
                        } finally {
                            IOUtils.closeQuietly(is);
                        }
                    } else {
                        res.sendError(HttpServletResponse.SC_NOT_FOUND);
                        return;
                    }
                }
                is = new ByteArrayInputStream(b);
            } else {
                is = fileContent.downloadFile();
                if(is== null) {
                    res.sendError(HttpServletResponse.SC_NOT_FOUND);
                    return;
                }
            }

            try {
                IOUtils.copy(is, os);
            } finally {
                IOUtils.closeQuietly(is);
                IOUtils.closeQuietly(os);
            }

            return;
        }

        res.sendError(HttpServletResponse.SC_NOT_FOUND);
    }

}
