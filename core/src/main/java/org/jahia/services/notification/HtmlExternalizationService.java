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
package org.jahia.services.notification;

import java.util.List;
import java.util.regex.Pattern;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import net.htmlparser.jericho.Attribute;
import net.htmlparser.jericho.Attributes;
import net.htmlparser.jericho.Element;
import net.htmlparser.jericho.HTMLElementName;
import net.htmlparser.jericho.OutputDocument;
import net.htmlparser.jericho.Source;
import net.htmlparser.jericho.StartTag;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.jahia.data.templates.JahiaTemplatesPackage;
import org.jahia.registries.ServicesRegistry;
import org.jahia.services.render.RenderContext;
import org.jahia.services.render.Resource;
import org.jahia.services.render.filter.HtmlTagAttributeTraverser;
import org.jahia.services.render.filter.HtmlTagAttributeTraverser.HtmlTagAttributeVisitor;

/**
 * Service used to "externalizes" the HTML document content by converting all
 * local URLs into absolute, inlining external CSS styles, and rewriting URLs in
 * CSS.
 * 
 * @author Sergiy Shyrkov
 */
public class HtmlExternalizationService {

    protected static final Pattern CSS_URL_PATTERN = Pattern.compile("url *\\( *\"?([^\\:\" )]*)\"? *\\)");

    private static final Logger logger = org.slf4j.LoggerFactory.getLogger(HtmlExternalizationService.class);

    private HttpClientService httpClientService;

    private boolean inlineCss = true;

    private boolean removeExternalScripts = true;

    private boolean removeInlinedScripts = true;

    private boolean rewriteUrls = true;

    private boolean rewriteUrlsInCss = true;

    private HtmlTagAttributeTraverser urlTraverser;

    private boolean useServletContextResources = true;

    public String externalize(String sourceContent, RenderContext renderContext) {
        return externalize(sourceContent, renderContext.getURLGenerator().getServer(), renderContext.getRequest(),
                renderContext.getResponse());
    }

    public String externalize(String sourceContent, String serverUrl) {
        return externalize(sourceContent, serverUrl, null, null);
    }

    public String externalize(String sourceContent, String serverUrl, HttpServletRequest request,
            HttpServletResponse response) {
        String out = sourceContent;
        long timer = System.currentTimeMillis();
        if (removeExternalScripts || removeInlinedScripts) {
            out = removeJavaScript(out);
        }

        if (inlineCss) {
            // process CSS
            out = processCss(out, serverUrl, request, response);
        }

        if (rewriteUrls) {
            // process URLs
            out = rewriteUrls(out, serverUrl);
            if (logger.isDebugEnabled()) {
                logger.debug("...done processing URLs in " + (System.currentTimeMillis() - timer) + " ms");
            }
        }

        if (logger.isDebugEnabled()) {
            logger.info("...done externalizing output content in " + (System.currentTimeMillis() - timer) + " ms");
        }

        return out;
    }

    protected String processCss(String previousOut, String serverUrl, HttpServletRequest request,
            HttpServletResponse response) {

        Source source = new Source(previousOut);
        OutputDocument document = new OutputDocument(source);
        StringBuilder sb = new StringBuilder();
        List<StartTag> linkStartTags = source.getAllStartTags(HTMLElementName.LINK);
        for (StartTag linkTag : linkStartTags) {
            Attributes attributes = linkTag.getAttributes();
            String rel = attributes.getValue("rel");
            if (rel == null || !"stylesheet".equalsIgnoreCase(rel)) {
                continue;
            }
            String href = attributes.getValue("href");
            if (href == null) {
                continue;
            }
            String styleSheetContent = null;
            try {
                if (useServletContextResources || request == null || response == null) {
                    if (request != null && StringUtils.startsWith(href, request.getContextPath())) {
                        href = StringUtils.substringAfter(href, request.getContextPath());
                    }
                    if (href.startsWith("/modules/")) {
                        String after = StringUtils.substringAfter(href, "/modules/");
                        String module = StringUtils.substringBefore(after, "/");
                        JahiaTemplatesPackage pack = ServicesRegistry.getInstance().getJahiaTemplateManagerService()
                                .getTemplatePackageById(module);
                        if (pack != null) {
                            String prefix = "/modules/" + module ;
                            if (!href.startsWith(prefix)) {
                                href = prefix + "/" + StringUtils.substringAfter(after, "/");
                            }
                            styleSheetContent = StringUtils.join(IOUtils.readLines(pack.getResource(StringUtils.substringAfter(after, "/")).getInputStream()),'\n');
                        }
                    } else {
                        styleSheetContent = httpClientService.getResourceAsString(href);
                    }
                } else {
                    styleSheetContent = httpClientService.getResourceAsString(href, request, response);
                }
            } catch (Exception e) {
                logger.warn("Unable to retrieve resource content for " + href + ".Cause: " + e.getMessage(), e);
            }

            if (StringUtils.isNotEmpty(styleSheetContent)) {
                sb.setLength(0);
                sb.append("<style");
                Attribute typeAttribute = attributes.get("type");
                if (typeAttribute != null) {
                    sb.append(' ').append(typeAttribute);
                }
                if (rewriteUrlsInCss) {
                    String baseUrl = HttpClientService.isAbsoluteUrl(href) ? href : serverUrl + href;
                    baseUrl = StringUtils.substringBeforeLast(baseUrl, "/") + "/";

                    styleSheetContent = rewriteCssUrls(styleSheetContent, baseUrl);
                }

                sb.append(">\n");
                if (request!=null && Boolean.valueOf(request.getParameter("debug"))) {
                	sb.append("/* ").append(href).append(" */\n");
                }
                sb.append(styleSheetContent)
                        .append("\n</style>");

                document.replace(linkTag, sb.toString());
            }
        }

        return document.toString();
    }

    protected String removeJavaScript(String out) {
        Source source = new Source(out);
        OutputDocument document = new OutputDocument(source);
        List<Element> scriptTags = source.getAllElements(HTMLElementName.SCRIPT);
        for (Element scriptElement : scriptTags) {
            boolean doRemove = false;
            if (removeExternalScripts && removeInlinedScripts) {
                doRemove = true;
            } else {
                String srcAttr = scriptElement.getAttributeValue("src");
                doRemove = removeExternalScripts && StringUtils.isNotEmpty(srcAttr) || removeInlinedScripts
                        && StringUtils.isEmpty(srcAttr);
            }
            if (doRemove) {
                document.remove(scriptElement);
            }
        }

        return document.toString();
    }

    /**
     * Replaces the relative URLs in CSS content into absolute form.
     * 
     * @param cssContent the original CSS content
     * @param urlBase the base URL to use for rewriting
     * @return the rewritten CSS content
     */
    protected String rewriteCssUrls(String cssContent, String urlBase) {
        return CSS_URL_PATTERN.matcher(cssContent).replaceAll("url(\"" + urlBase + "$1\")");
    }

    protected String rewriteUrls(String source, final String serverUrl) {
        return urlTraverser.traverse(source, new HtmlTagAttributeVisitor() {
            public String visit(String url, RenderContext context, String tagName, String attrName, Resource resource) {
                return StringUtils.isNotEmpty(url) && url.startsWith("/") ? serverUrl + url : url;
            }
        });
    }

    /**
     * @param httpClientService the httpClientService to set
     */
    public void setHttpClientService(HttpClientService httpClientService) {
        this.httpClientService = httpClientService;
    }

    /**
     * @param inlineCss the inlineCss to set
     */
    public void setInlineCss(boolean inlineCss) {
        this.inlineCss = inlineCss;
    }

    /**
     * @param removeExternalScripts the removeExternalScripts to set
     */
    public void setRemoveExternalScripts(boolean removeExternalScripts) {
        this.removeExternalScripts = removeExternalScripts;
    }

    /**
     * @param removeInlinedScripts the removeInlinedScripts to set
     */
    public void setRemoveInlinedScripts(boolean removeInlinedScripts) {
        this.removeInlinedScripts = removeInlinedScripts;
    }

    /**
     * @param rewriteUrls the rewriteUrls to set
     */
    public void setRewriteUrls(boolean rewriteUrls) {
        this.rewriteUrls = rewriteUrls;
    }

    /**
     * @param rewriteUrlsInCss the rewriteUrlsInCss to set
     */
    public void setRewriteUrlsInCss(boolean rewriteUrlsInCss) {
        this.rewriteUrlsInCss = rewriteUrlsInCss;
    }

    /**
     * @param urlTraverser the urlTraverser to set
     */
    public void setUrlTraverser(HtmlTagAttributeTraverser urlTraverser) {
        this.urlTraverser = urlTraverser;
    }

    /**
     * @param useServletContextResources the useServletContextResources to set
     */
    public void setUseServletContextResources(boolean useServletContextResources) {
        this.useServletContextResources = useServletContextResources;
    }
}