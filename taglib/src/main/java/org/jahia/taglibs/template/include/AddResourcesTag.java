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

package org.jahia.taglibs.template.include;

import org.apache.log4j.Logger;
import org.jahia.data.templates.JahiaTemplatesPackage;
import org.jahia.services.SpringContextSingleton;
import org.jahia.services.render.RenderContext;
import org.jahia.taglibs.AbstractJahiaTag;

import javax.servlet.jsp.JspException;
import javax.servlet.jsp.PageContext;
import java.net.MalformedURLException;
import java.util.LinkedList;
import java.util.Map;
import java.util.Set;
import java.util.List;

/**
 * Add some resources to the head tag of the HTML.
 *
 * @author rincevent
 * @since JAHIA 6.5
 * Created : 27 oct. 2009
 */
public class AddResourcesTag extends AbstractJahiaTag {
    private static final long serialVersionUID = -552052631291168495L;
    private transient static Logger logger = Logger.getLogger(AddResourcesTag.class);
    private boolean insert;
    private String type;
    private String resources;
    private String title;
    private String key;
    /**
     * Default processing of the end tag returning EVAL_PAGE.
     *
     * @return EVAL_PAGE
     * @throws javax.servlet.jsp.JspException if an error occurred while processing this tag
     * @see javax.servlet.jsp.tagext.Tag#doEndTag
     */
    @Override
    public int doEndTag() throws JspException {
        JahiaTemplatesPackage templatesPackage = (JahiaTemplatesPackage) pageContext.getAttribute("currentModule", PageContext.REQUEST_SCOPE);
        addResources(getRenderContext(), templatesPackage, type, resources);
        resetState();
        return super.doEndTag();
    }

    protected void addResources(RenderContext renderContext, JahiaTemplatesPackage aPackage, String type,
                              String resources) {
        if ("inlinecss".equals(type) || "inlinejavascript".equals(type)) {
            return;
        }
        
        final Map<String, String> mapping = getStaticAssetMapping();
        
        final Set<String> links = renderContext.getStaticAssets(type);
        String[] strings = resources.split(",");

        List<String> lookupPaths = new LinkedList<String>();
        lookupPaths.add(aPackage.getRootFolderPath() + "/" + type + "/");
        for (JahiaTemplatesPackage pack : aPackage.getDependencies()) {
            lookupPaths.add(pack.getRootFolderPath() + "/" + type + "/");
        }

        for (String resource : strings) {
            boolean found = false;
            resource = resource.trim();
            if (resource.startsWith("/") || resource.startsWith("http://") || resource.startsWith("https://")) {
                found = true;
                resource = mapping.containsKey(resource) ? mapping.get(resource) : resource;
                if (links == null || !links.contains(resource)) {
                    renderContext.addStaticAsset(type, resource, insert);
                    if (title != null) {
                        renderContext.getStaticAssetOptions().get(resource).put("title", title);
                    }
                }
            } else {
                for (String lookupPath : lookupPaths){
                    String path = lookupPath + resource;
                    String pathWithContext = renderContext.getRequest().getContextPath() + path;
                    if (links != null && links.contains(pathWithContext)) {
                        // we have it already
                        found = true;
                        break;  
                    }
                    try {
                        if (pageContext.getServletContext().getResource(path) != null) {
                            // we found it

                            // apply mapping
                            if (mapping.containsKey(path)) {
                                path = mapping.get(path);
                                pathWithContext = !path.startsWith("http://") && !path.startsWith("https://") ? renderContext.getRequest().getContextPath() + path : path;
                            }
                            if (links == null || !links.contains(pathWithContext)) {
                                renderContext.addStaticAsset(type, pathWithContext, insert);
                                if (title != null) {
                                    renderContext.getStaticAssetOptions().get(resource).put("title", title);
                                }
                            }
                            found = true;
                            break;
                        }
                    } catch (MalformedURLException e) {
                        logger.warn(e.getMessage(), e);
                    }
                }
            }
            if (!found) {
                logger.warn("Unable to find resource '" + resource + "' in: " + lookupPaths);
            }
        }
    }

    public void setType(String type) {
        this.type = type != null ? type.toLowerCase() : null;
    }

    public void setResources(String resources) {
        this.resources = resources;
    }

    public void setInsert(boolean insert) {
        this.insert = insert;
    }

    @Override
    public int doAfterBody() throws JspException {
        if ("inlinecss".equals(type) || "inlinejavascript".equals(type)) {
            if(key!=null) {
                getRenderContext().addStaticAsset(type, getBodyContent().getString(), insert, key);
            }
            else {
            getRenderContext().addStaticAsset(type, getBodyContent().getString(), insert);
            }
            getBodyContent().clearBody();
        }
        return super.doAfterBody();
    }
    
 
    @Override
    protected void resetState() {
        insert = false;
        resources = null;
        type = null;
        title = null;
        super.resetState();
    }
    
    @SuppressWarnings("unchecked")
    protected Map<String, String> getStaticAssetMapping() {
        return (Map<String, String>) SpringContextSingleton.getBean("org.jahia.services.render.StaticAssetMappingRegistry");
    }

    /**
     * Sets the title to be used for the asset if applicable.
     * @param title the title to set
     */
    public void setTitle(String title) {
        this.title = title;
    }

    public void setKey(String key) {
        this.key = key;
    }
}