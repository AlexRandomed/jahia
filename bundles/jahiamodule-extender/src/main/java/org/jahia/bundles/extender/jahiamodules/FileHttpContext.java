/**
 * ==========================================================================================
 * =                   JAHIA'S DUAL LICENSING - IMPORTANT INFORMATION                       =
 * ==========================================================================================
 *
 *     Copyright (C) 2002-2015 Jahia Solutions Group SA. All rights reserved.
 *
 *     THIS FILE IS AVAILABLE UNDER TWO DIFFERENT LICENSES:
 *     1/GPL OR 2/JSEL
 *
 *     1/ GPL
 *     ======================================================================================
 *
 *     IF YOU DECIDE TO CHOSE THE GPL LICENSE, YOU MUST COMPLY WITH THE FOLLOWING TERMS:
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
 *     ======================================================================================
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
 *
 * ==========================================================================================
 * =                                   ABOUT JAHIA                                          =
 * ==========================================================================================
 *
 *     Rooted in Open Source CMS, Jahia’s Digital Industrialization paradigm is about
 *     streamlining Enterprise digital projects across channels to truly control
 *     time-to-market and TCO, project after project.
 *     Putting an end to “the Tunnel effect”, the Jahia Studio enables IT and
 *     marketing teams to collaboratively and iteratively build cutting-edge
 *     online business solutions.
 *     These, in turn, are securely and easily deployed as modules and apps,
 *     reusable across any digital projects, thanks to the Jahia Private App Store Software.
 *     Each solution provided by Jahia stems from this overarching vision:
 *     Digital Factory, Workspace Factory, Portal Factory and eCommerce Factory.
 *     Founded in 2002 and headquartered in Geneva, Switzerland,
 *     Jahia Solutions Group has its North American headquarters in Washington DC,
 *     with offices in Chicago, Toronto and throughout Europe.
 *     Jahia counts hundreds of global brands and governmental organizations
 *     among its loyal customers, in more than 20 countries across the globe.
 *
 *     For more information, please visit http://www.jahia.com
 */
package org.jahia.bundles.extender.jahiamodules;

import org.apache.commons.lang.StringUtils;
import org.apache.maven.model.Model;
import org.jahia.utils.PomUtils;
import org.osgi.framework.Bundle;
import org.osgi.service.http.HttpContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * HttpContext that can look up resources in files as well as the default OSGi HttpContext
 */
public class FileHttpContext implements HttpContext {

    private static final URL[] EMPTY_URL_ARRAY = new URL[0];

    private static Logger logger = LoggerFactory.getLogger(FileHttpContext.class);

    private static URL NULL_URL;

    static {
        try {
            NULL_URL = new URL("http://");
        } catch (MalformedURLException e) {
            //
        }
    }

    HttpContext parentHttpContext;
    URL[] sourceURLs;

    private Map<String, URL> resourcesCache = new ConcurrentHashMap<String, URL>();

    public FileHttpContext(URL[] sourceURLs, HttpContext parentHttpContext) {
        this.sourceURLs = sourceURLs != null && sourceURLs.length == 0 ? null : sourceURLs;
        this.parentHttpContext = parentHttpContext;
    }

    public static URL[] getSourceURLs(Bundle bundle) {
        URL[] urls = EMPTY_URL_ARRAY;
        String sourceFolder = (String) bundle.getHeaders().get("Jahia-Source-Folders");
        if (StringUtils.isNotEmpty(sourceFolder)) {
            File pomFile = new File(sourceFolder, "pom.xml");
            if (!pomFile.exists()) {
                return null;
            }
            try {
                final Model model = PomUtils.read(pomFile);
                String s = model.getVersion();
                if (s == null) {
                    s = model.getParent().getVersion();
                }
                if (!s.equals(bundle.getHeaders().get("Implementation-Version"))) {
                    return null;
                }
            } catch (Exception e) {
                logger.warn("Invalid source folder " + sourceFolder + ", cannot read pom file", e.getMessage());
                return null;
            }

            List<URL> sourceURLs = new ArrayList<URL>();
            File resourceFolderFile = new File(sourceFolder, "src/main/resources");
            if (resourceFolderFile.exists()) {
                try {
                    sourceURLs.add(resourceFolderFile.toURI().toURL());
                } catch (MalformedURLException e) {
                    logger.warn("Invalid source folder " + sourceFolder + ", cannot convert to URL", e);
                }
            }
            // Legacy sources
            File webappFolderFile = new File(sourceFolder, "src/main/webapp");
            if (webappFolderFile.exists()) {
                try {
                    sourceURLs.add(webappFolderFile.toURI().toURL());
                } catch (MalformedURLException e) {
                    logger.warn("Invalid source folder " + sourceFolder + ", cannot convert to URL", e);
                }
            }

            urls = sourceURLs.toArray(new URL[sourceURLs.size()]);
            logger.debug("Detected {} source folders for bundle {}", sourceURLs.size(), bundle.getSymbolicName());
        }
        return urls;
    }

    @Override
    public boolean handleSecurity(HttpServletRequest request, HttpServletResponse response) throws IOException {
        return parentHttpContext.handleSecurity(request, response);
    }

    @Override
    public URL getResource(String name) {
        if (sourceURLs != null) {
            for (URL sourceURL : sourceURLs) {
                URL resourceURL = null;
                try {
                    resourceURL = new URL(sourceURL, name);
                    if (urlExists(resourceURL)) {
                        // @todo we could add dynamic registration here if the resource has not yet been registered in the activator
                        return resourceURL;
                    }
                } catch (MalformedURLException e) {
                    logger.error("Error in resource URL " + name, e);
                }
            }
        }
        URL url = resourcesCache.get(name);
        if (url == null) {
            if (logger.isDebugEnabled()) {
                logger.debug("Getting resource: " + name);
            }
            url = parentHttpContext.getResource(name);
            resourcesCache.put(name, url != null ? url : NULL_URL);
        }
        return url != NULL_URL ? url : null;
    }

    private boolean urlExists(URL url) {
        // Try a URL connection content-length header...
        try {
            URLConnection con = null;
            con = url.openConnection();
            con.setUseCaches(false);
            HttpURLConnection httpCon =
                    (con instanceof HttpURLConnection ? (HttpURLConnection) con : null);
            if (httpCon != null) {
                httpCon.setRequestMethod("HEAD");
                if (httpCon.getResponseCode() == HttpURLConnection.HTTP_OK) {
                    return true;
                }
            }
            if (con.getContentLength() > 0) {
                return true;
            }
            if (httpCon != null) {
                // no HTTP OK status, and no content-length header: give up
                httpCon.disconnect();
                return false;
            } else {
                // Fall back to stream existence: can we open the stream?
                InputStream is = getInputStream(url);
                is.close();
                return true;
            }
        } catch (IOException e) {
            logger.debug("Testing existence of resource " + url, e);
            return false;
        }
    }

    public InputStream getInputStream(URL url) throws IOException {
        URLConnection con = url.openConnection();
        con.setUseCaches(false);
        try {
            return con.getInputStream();
        } catch (IOException ex) {
            // Close the HTTP connection (if applicable).
            if (con instanceof HttpURLConnection) {
                ((HttpURLConnection) con).disconnect();
            }
            throw ex;
        }
    }


    @Override
    public String getMimeType(String name) {
        return parentHttpContext.getMimeType(name);
    }
}
