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

package org.jahia.services.render;

import java.util.HashMap;
import java.util.Map;

import javax.jcr.RepositoryException;

import org.apache.commons.collections.Transformer;
import org.apache.commons.collections.map.LazyMap;
import org.apache.commons.lang.StringUtils;
import org.jahia.services.sites.JahiaSitesBaseService;
import org.slf4j.Logger;
import org.jahia.api.Constants;
import org.jahia.bin.Captcha;
import org.jahia.bin.Contribute;
import org.jahia.bin.DocumentConverter;
import org.jahia.bin.Edit;
import org.jahia.bin.Find;
import org.jahia.bin.FindPrincipal;
import org.jahia.bin.Initializers;
import org.jahia.bin.Logout;
import org.jahia.bin.Render;
import org.jahia.bin.Studio;
import org.jahia.services.content.JCRNodeWrapper;
import org.jahia.services.render.scripting.Script;
import org.jahia.settings.SettingsBean;

/**
 * Main URL generation class. This class is exposed to the template developers to make it easy to them to access
 * basic URLs such as <code>${url.edit}</code>, <code>${url.userProfile}</code>.
 * User: toto
 * Date: Sep 14, 2009
 * Time: 11:13:37 AM
 *
 * @todo Ideally instances of this class should be created by a factory that is configured through Spring.
 */
public class URLGenerator {
    private static Logger logger = org.slf4j.LoggerFactory.getLogger(URLGenerator.class);

    private String base;

    private String live;
    private String edit;
    private String preview;
    private String contribute;
    private String studio;
    private String find;
    private String findPrincipal;
    private String logout;
    private String initializers;
    private String captcha;

    private Resource resource;
    private RenderContext context;

    private Map<String, String> languages;

    private Map<String, String> templates;

    private Map<String, String> templateTypes;

    private String templatesPath;

    private String baseLive;
    private String baseContribute;
    private String baseEdit;
    private String basePreview;
    private String ckeditor;
    private String convert;
    private String myProfile;

    public URLGenerator(RenderContext context, Resource resource) {
        this.context = context;
        this.resource = resource;
        initURL();
        if (context.getURLGenerator() == null) {
            context.setURLGenerator(this);
        }
    }

    /**
     * Set workspace url as attribute of the current request
     */
    protected void initURL() {
        base = getContext() + context.getServletPath() + "/" + resource.getWorkspace() + "/" + resource.getLocale();

        final String resourcePath = context.getMainResource().getNode().getPath() + "." + context.getMainResource().getTemplateType();

        baseLive = getContext() + Render.getRenderServletPath() + "/" + Constants.LIVE_WORKSPACE + "/" + resource.getLocale();
        live = baseLive + resourcePath;
        baseEdit = getContext() + Edit.getEditServletPath() + "/" + Constants.EDIT_WORKSPACE + "/" + resource.getLocale();
        edit = baseEdit + resourcePath;
        basePreview = getContext() + Render.getRenderServletPath() + "/" + Constants.EDIT_WORKSPACE + "/" + resource.getLocale();
        preview = basePreview + resourcePath;
        baseContribute = getContext() + Contribute.getContributeServletPath() + "/" + Constants.EDIT_WORKSPACE + "/" + resource.getLocale();
        contribute = baseContribute + resourcePath;
        studio = getContext() + Studio.getStudioServletPath() + "/" + Constants.EDIT_WORKSPACE + "/" + resource.getLocale() + "/templateSets";
        ckeditor = SettingsBean.getInstance().getJahiaCkEditorDiskPath();
        if (context.getSite() != null) {
            try {
                if (context.getSite().hasProperty("j:templatesSet")) {
                    studio += "/" + context.getSite().getProperty("j:templatesSet").getString();
                    if (resource.getNode().hasProperty("j:templateNode")) {
                        try {
                            studio += StringUtils.substringAfter(resource.getNode().getProperty("j:templateNode").getNode().getPath(), context.getSite().getPath());
                            studio += ".html";
                        } catch (RepositoryException e) {
                            studio += "/home.html";
                        }
                    } else {
                        studio += "/home.html";
                    }
                }
            } catch (RepositoryException e) {
                logger.error("Cannot get studio url", e);
            }
        } else {
            studio += ".html";
        }
        find = getContext() + Find.getFindServletPath() + "/" + resource.getWorkspace() + "/" + resource.getLocale();
        findPrincipal = getContext() + FindPrincipal.getFindPrincipalServletPath();
        logout = getContext() + Logout.getLogoutServletPath();
        initializers = getContext() + Initializers.getInitializersServletPath() + "/" + resource.getWorkspace() + "/" + resource.getLocale();
        convert = getContext() + DocumentConverter.getPath() + "/" + resource.getWorkspace();
        captcha = getContext() + Captcha.getCaptchaServletPath();
        templatesPath = getContext() + "/modules";
        myProfile = "/sites/" + JahiaSitesBaseService.SYSTEM_SITE_KEY + "/home/my-profile";
    }

    public String getContext() {
        return context.getRequest().getContextPath();
    }

    public String getFiles() {
        return getContext() + "/files";
    }

    public String getBase() {
        return base;
    }

    public String getLive() {
        return live;
    }

    public String getEdit() {
        return edit;
    }

    public String getPreview() {
        return preview;
    }

    public String getContribute() {
        return contribute;
    }

    public String getStudio() {
        return studio;
    }

    public String getFind() {
        return find;
    }

    public String getFindPrincipal() {
        return findPrincipal;
    }

    public String getLogout() {
        return logout;
    }

    public String getCurrentModule() {
        return getTemplatesPath() + "/" + ((Script) context.getRequest().getAttribute(
                "script")).getTemplate().getModule().getRootFolder();
    }

    public String getCurrent() {
        return buildURL(resource.getNode(), resource.getTemplate(), resource.getTemplateType());
    }

    @SuppressWarnings("unchecked")
    public Map<String, String> getLanguages() {
        if (languages == null) {
            languages = LazyMap.decorate(new HashMap<String, String>(), new Transformer() {
                public Object transform(Object lang) {
                    return getContext() + context.getServletPath() + "/" + resource.getWorkspace() + "/" + lang + resource.getNode().getPath() + ".html";
                }
            });
        }

        return languages;
    }

    @SuppressWarnings("unchecked")
    public Map<String, String> getTemplates() {
        if (templates == null) {
            templates = LazyMap.decorate(new HashMap<String, String>(), new Transformer() {
                public Object transform(Object template) {
                    return buildURL(resource.getNode(), (String) template, resource.getTemplateType());
                }
            });
        }
        return templates;
    }

    @SuppressWarnings("unchecked")
    public Map<String, String> getTemplateTypes() {
        if (templateTypes == null) {
            templateTypes = LazyMap.decorate(new HashMap<String, String>(), new Transformer() {
                public Object transform(Object templateType) {
                    return buildURL(resource.getNode(), resource.getTemplate(), (String) templateType);
                }
            });
        }
        return templateTypes;
    }

    /**
     * Returns the path to the templates folder.
     *
     * @return the path to the templates folder
     */
    public String getTemplatesPath() {
        return templatesPath;
    }

    /**
     * Returns the URL of the main resource (normally, page), depending on the
     * current mode.
     *
     * @return the URL of the main resource (normally, page), depending on the
     *         current mode
     */
    public String getMainResource() {
        if (context.isEditMode()) {
            if (context.getEditModeConfigName().equals(Studio.STUDIO_MODE)) {
                return  getContext() + Studio.getStudioServletPath() + "/" + Constants.EDIT_WORKSPACE + "/" + resource.getLocale() + context.getMainResource().getNode().getPath() + ".html";
            }
            return getEdit();
        } else if (context.isContributionMode()) {
            if(context.getMainResource().getTemplate()!=null) {
                return baseContribute+context.getMainResource().getNode().getPath()+"."+context.getMainResource().getTemplate()+".html";
            }
            return contribute;
        } else {
            return Constants.LIVE_WORKSPACE.equals(resource.getWorkspace()) ? live : preview;
        }
    }

    public String buildURL(JCRNodeWrapper node, String template, String templateType) {
        return base + node.getPath() + (template != null && !"default".equals(template) ? "." + template : "") + "." + templateType;
    }

    public String getInitializers() {
        return initializers;
    }

    public String getCaptcha() {
        return captcha;
    }

    public String getBaseContribute() {
        return baseContribute;
    }

    public String getBaseEdit() {
        return baseEdit;
    }

    public String getBaseLive() {
        return baseLive;
    }

    public String getBasePreview() {
        return basePreview;
    }

    public String getCkEditor() {
        return ckeditor;
    }

    public String getConvert() {
        return convert;
    }

    public String getRealResource() {
        if (context.isAjaxRequest() && context.getAjaxResource() != null) {
            if (context.isEditMode()) {
                return baseEdit + context.getAjaxResource().getNode().getPath() + ".html";
            } else if (context.isContributionMode()) {
                return baseContribute + context.getAjaxResource().getNode().getPath() + ".html";
            } else {
                return (Constants.LIVE_WORKSPACE.equals(
                        context.getAjaxResource().getWorkspace()) ? baseLive : basePreview) + context.getAjaxResource().getNode().getPath() + ".html";
            }
        } else {
            if (context.isEditMode()) {
                if (context.getEditModeConfigName().equals(Studio.STUDIO_MODE)) {
                    return getStudio();
                }
                return getEdit();
            } else {
                return Constants.LIVE_WORKSPACE.equals(resource.getWorkspace()) ? live : preview;
            }
        }
    }
    
    /**
     * Returns the server URL, including scheme, host and port, depending on the
     * current site. The URL is in the form <code><scheme><host>:<port></code>,
     * e.g. <code>http://www.jahia.org:8080</code>. The port is omitted in case
     * of standard HTTP (80) and HTTPS (443) ports.
     * 
     * If the site's server name is configured to be "localhost", then take the
     * servername from the request.
     * 
     * @return the server URL, including scheme, host and port, depending on the
     *         current site
     */
    public String getServer() {
        StringBuilder url = new StringBuilder();
        String scheme = context.getRequest().getScheme();
        String host = context.getSite().getServerName();
        int port = 0;
        if (host !=null && host.contains(":")) {
            // the server name of the site already has
            host = StringUtils.substringBefore(host, ":");
            port = Integer.valueOf(StringUtils.substringAfterLast(host, ":"));
        }
        if ("localhost".equals(host)) {
            host = context.getRequest().getServerName();
        }
        if (port == 0) {
            port = SettingsBean.getInstance().getSiteURLPortOverride();
        }
        if (port == 0) {
            port = context.getRequest().getServerPort();
        }
        if (port == 443) {
            // use HTTPS
            scheme = "https";
        }
        
        url.append(scheme).append("://").append(host);
        
        if (port != 80 && port != 443) {
            url.append(":").append(port);
        }
        
        return url.toString();
    }

    public String getMyProfile() {
        return myProfile;
    }

}
