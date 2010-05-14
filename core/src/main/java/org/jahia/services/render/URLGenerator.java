package org.jahia.services.render;

import org.apache.log4j.Logger;
import org.jahia.api.Constants;
import org.jahia.bin.*;
import org.jahia.services.content.JCRNodeWrapper;
import org.jahia.services.render.scripting.Script;
import org.jahia.services.sites.JahiaSite;
import org.jahia.services.usermanager.JahiaUserManagerService;
import org.jahia.services.content.JCRStoreService;
import org.apache.commons.collections.map.LazyMap;
import org.apache.commons.collections.Transformer;
import org.jahia.settings.SettingsBean;

import javax.jcr.RepositoryException;
import java.util.Map;
import java.util.HashMap;

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
    private static Logger logger = Logger.getLogger(URLGenerator.class);

    private String base;

    private String live;
    private String edit;
    private String preview;
    private String contribute;
    private String studio;
    private String find;
    private String logout;
    private String initializers;
    private String captcha;

    private String userProfile;

    private Resource resource;
    private RenderContext context;
    private JCRStoreService jcrStoreService;

    private Map<String, String> languages;

    private Map<String, String> templates;

    private String templatesPath;

    // settings
    private boolean useRelativeSiteURLs = false;
    private int siteURLPortOverride = 0;

    private String baseLive;
    private String baseContribute;
    private String baseEdit;
    private String basePreview;
    private String ckeditor;
    private String toPDF;

    public URLGenerator(RenderContext context, Resource resource, JCRStoreService jcrStoreService) {
        this.context = context;
        this.resource = resource;
        this.jcrStoreService = jcrStoreService;
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

        final String resourcePath = context.getMainResource().getNode().getPath() + ".html";

        baseLive = getContext() + Render.getRenderServletPath() + "/" + Constants.LIVE_WORKSPACE + "/" + resource.getLocale();
        live = baseLive + resourcePath;
        baseEdit = getContext() + Edit.getEditServletPath() + "/" + Constants.EDIT_WORKSPACE + "/" + resource.getLocale();
        edit = baseEdit + resourcePath;
        basePreview = getContext() + Render.getRenderServletPath() + "/" + Constants.EDIT_WORKSPACE + "/" + resource.getLocale();
        preview = basePreview + resourcePath;
        baseContribute = getContext() + Contribute.getContributeServletPath() + "/" + Constants.EDIT_WORKSPACE + "/" + resource.getLocale();
        contribute = baseContribute + resourcePath;
        studio = getContext() + Studio.getStudioServletPath() + "/" + Constants.EDIT_WORKSPACE + "/" + resource.getLocale() + "/templatesSet";
        ckeditor = SettingsBean.getInstance().getJahiaCkEditorDiskPath();
        if (context.getSite() != null) {
            try {
                if (context.getSite().hasProperty("j:sourceTemplate")) {
                    studio += "/" + context.getSite().getProperty("j:sourceTemplate").getNode().getName() + "/";
                    if (resource.getNode().isNodeType("jnt:page") && resource.getNode().hasProperty("j:sourceTemplate")) {
                        try {
                            studio += "templates/" + resource.getNode().getProperty("j:sourceTemplate").getNode().getName() + ".html";
                        } catch (RepositoryException e) {
                            studio += "home.html";
                        }
                    } else {
                        studio += "home.html";
                    }
                }
            } catch (RepositoryException e) {
                logger.error("Cannot get studio url",e);
            }
        } else {
            studio += ".html";
        }
        find = getContext() + Find.getFindServletPath() + "/" + resource.getWorkspace() + "/" + resource.getLocale();
        logout = getContext() + Logout.getLogoutServletPath();
        initializers = getContext() + Initializers.getInitializersServletPath() + "/" + resource.getWorkspace() + "/" + resource.getLocale();
        toPDF = getContext() + ToPDFServlet.getToPDFServletPath() + "/" + resource.getWorkspace();
        captcha = getContext() + Captcha.getCaptchaServletPath();
        templatesPath = getContext() + "/templates";
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

    public String getLive(String versionNumber) {
        if (versionNumber == null || versionNumber.length() == 0) {
            return getLive();
        }
        return getLive() + "?v=" + versionNumber;
    }

    public String getEdit(String versionNumber) {
        if (versionNumber == null || versionNumber.length() == 0) {
            return getEdit();
        }
        return getEdit() + "?v=" + versionNumber;
    }

    public String getPreview(String versionNumber) {
        if (versionNumber == null || versionNumber.length() == 0) {
            return getPreview();
        }
        return getPreview() + "?v=" + versionNumber;
    }


    public String getFind() {
        return find;
    }

    public String getLogout() {
        return logout;
    }

    public String getUserProfile() {
        if (userProfile == null) {
            if (!JahiaUserManagerService.isGuest(context.getUser())) {
                if (context.getSite() != null) {
                    userProfile = base + context.getSite().getPath() + "/users/" + context.getUser().getUsername() + "."+ resource.getTemplateType();
                } else {
                    userProfile = base + "/users/" + context.getUser().getUserKey() + "."+ resource.getTemplateType();
                }
            }
        }
        return userProfile;
    }

    public String getCurrentModule() {
        return getTemplatesPath() + "/" + ((Script) context.getRequest().getAttribute("script")).getTemplate().getModule().getRootFolder();
    }

    public String getCurrent() {
        return buildURL(resource.getNode(), resource.getResolvedTemplate(), resource.getTemplateType());
    }

    @SuppressWarnings("unchecked")
    public Map<String, String> getLanguages() {
        if (languages == null) {
            languages = LazyMap.decorate(new HashMap(), new Transformer() {
                public Object transform(Object lang) {
                    String servletPath;
                    return getContext() + context.getServletPath() + "/" + resource.getWorkspace() + "/" + lang + resource.getNode().getPath() + ".html";
                }
            });
        }

        return languages;
    }

    @SuppressWarnings("unchecked")
    public Map<String, String> getTemplates() {
        if (templates == null) {
            templates = LazyMap.decorate(new HashMap(), new Transformer() {
                public Object transform(Object template) {
                    return buildURL(resource.getNode(), (String) template, resource.getTemplateType());
                }
            });
        }
        return templates;
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
            return getEdit();
        } else {
            return Constants.LIVE_WORKSPACE.equals(resource.getWorkspace()) ? live : preview;
        }
    }

    public String buildURL(JCRNodeWrapper node, String template, String templateType) {
        return base + node.getPath() + (template != null ? "." + template : "") + "."+ templateType;
    }

    /**
     * Generates a complete URL for a site. Uses the site URL serverName to generate the URL *only* it is resolves in a DNS. Otherwise it
     * simply uses the current serverName and generates a URL with a /site/ parameter
     *
     * @param theSite       the site agaisnt we build the url
     * @param withSessionID a boolean that specifies whether we should call the encodeURL method on the generated URL. Most of the time we will
     *                      just want to set this to true, but in the case of URLs sent by email we do not, otherwise we have a security problem
     *                      since we are sending SESSION IDs to people that should not have them.
     * @return String a full URL to the site using the currently set values in the ProcessingContext.
     */
    public String getSiteURL(final JahiaSite theSite, final boolean withSessionID) {

        final String siteServerName = theSite.getServerName();
        String sessionIDStr = null;

        final StringBuilder newSiteURL = new StringBuilder(64);
        if (!useRelativeSiteURLs) {
            newSiteURL.append(context.getRequest().getScheme()).append("://");
        }

        if (!useRelativeSiteURLs) {
            // let's construct an URL by deconstruct our current URL and
            // using the site name as a server name
            newSiteURL.append(siteServerName);
            if (!siteServerName.equals(context.getRequest().getServerName())) {
                // serverName has changed, we must transfer cookie information
                // for sessionID if there is some.
                sessionIDStr = ";jsessionid=" + context.getRequest().getSession(false).getId();
            }

            if (siteURLPortOverride > 0) {
                if (siteURLPortOverride != 80) {
                    newSiteURL.append(":");
                    newSiteURL.append(siteURLPortOverride);
                }
            } else if (context.getRequest().getServerPort() != 80) {
                newSiteURL.append(":");
                newSiteURL.append(context.getRequest().getServerPort());
            }
        }

        newSiteURL.append(base);

        if (withSessionID) {
            String serverURL = context.getResponse().encodeURL(newSiteURL.toString());
            if (sessionIDStr != null) {
                if (serverURL.indexOf("jsessionid") == -1) {
                    serverURL += sessionIDStr;
                }
            }
            return serverURL;
        } else {
            return newSiteURL.toString();
        }
    }

    public boolean isUseRelativeSiteURLs() {
        return useRelativeSiteURLs;
    }

    public void setUseRelativeSiteURLs(boolean useRelativeSiteURLs) {
        this.useRelativeSiteURLs = useRelativeSiteURLs;
    }

    public int getSiteURLPortOverride() {
        return siteURLPortOverride;
    }

    public void setSiteURLPortOverride(int siteURLPortOverride) {
        this.siteURLPortOverride = siteURLPortOverride;
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

    public String getToPDF() {
        return toPDF;
    }
}
