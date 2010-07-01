/**
 *
 * This file is part of Jahia: An integrated WCM, DMS and Portal Solution
 * Copyright (C) 2002-2010 Jahia Limited. All rights reserved.
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
package org.jahia.services.render.filter.cache;

import net.sf.ehcache.Element;
import net.sf.ehcache.constructs.blocking.BlockingCache;
import net.sf.ehcache.constructs.blocking.LockTimeoutException;
import org.apache.log4j.Logger;
import org.jahia.services.cache.CacheEntry;
import org.jahia.services.content.JCRNodeWrapper;
import org.jahia.services.content.decorator.JCRFrozenNodeAsRegular;
import org.jahia.services.render.RenderContext;
import org.jahia.services.render.Resource;
import org.jahia.services.render.filter.AbstractFilter;
import org.jahia.services.render.filter.RenderChain;
import org.jahia.services.render.scripting.Script;

import java.text.SimpleDateFormat;
import java.util.*;

/**
 * Module content caching filter.
 *
 * @author : rincevent
 * @since : JAHIA 6.1
 *        Created : 8 janv. 2010
 */
public class CacheFilter extends AbstractFilter {
    private transient static Logger logger = Logger.getLogger(CacheFilter.class);
    private ModuleCacheProvider cacheProvider;

    public String prepare(RenderContext renderContext, Resource resource, RenderChain chain) throws Exception {
        if (!(resource.getNode() instanceof JCRFrozenNodeAsRegular)) {
            final Script script = (Script) renderContext.getRequest().getAttribute("script");
            chain.pushAttribute(renderContext.getRequest(), "cache.perUser",
                    Boolean.valueOf(script.getTemplate().getProperties().getProperty("cache.perUser", "false")));
            chain.pushAttribute(renderContext.getRequest(), "cache.mainResource", Boolean.valueOf(
                script.getTemplate().getProperties().getProperty("cache.mainResource", "false")));
            boolean debugEnabled = logger.isDebugEnabled();
            boolean displayCacheInfo = Boolean.valueOf(renderContext.getRequest().getParameter("cacheinfo"));
            String key = cacheProvider.getKeyGenerator().generate(resource, renderContext);
            String perUserKey = key.replaceAll("_perUser_", renderContext.getUser().getUsername()).replaceAll("_mr_",renderContext.getMainResource().getNode().getPath());
            if (debugEnabled) {
                logger.debug("Cache filter for key " + key);
            }
            Element element = null;
            try {
                if (debugEnabled) {
                    logger.debug("Try to get content from cache for node with key: " + perUserKey);
                }
                element = cacheProvider.getCache().get(perUserKey);
            } catch (LockTimeoutException e) {
                logger.warn(e.getMessage(), e);
            }
            if (element != null) {
                if (debugEnabled) {
                    logger.debug("Content retrieved from cache for node with key: " + perUserKey);
                }
                final String cachedContent = (String) ((CacheEntry) element.getValue()).getObject();
                if (displayCacheInfo && !cachedContent.contains("<body") && cachedContent.trim().length() > 0) {
                    return appendDebugInformation(renderContext, key, cachedContent, element);
                } else {
                    return cachedContent;
                }
            } else {
                return null;
            }
        }
        return null;
    }

    public String execute(String previousOut, RenderContext renderContext, Resource resource, RenderChain chain)
            throws Exception {
        if (!(resource.getNode() instanceof JCRFrozenNodeAsRegular)) {
            final Script script = (Script) renderContext.getRequest().getAttribute("script");
            chain.pushAttribute(renderContext.getRequest(), "cache.perUser",
                    Boolean.valueOf(script.getTemplate().getProperties().getProperty("cache.perUser", "false")));
            chain.pushAttribute(renderContext.getRequest(), "cache.mainResource", Boolean.valueOf(
                script.getTemplate().getProperties().getProperty("cache.mainResource", "false")));
            Map<String, Map<String, Integer>> templatesCacheExpiration = renderContext.getTemplatesCacheExpiration();
            boolean debugEnabled = logger.isDebugEnabled();
            boolean displayCacheInfo = Boolean.valueOf(renderContext.getRequest().getParameter("cacheinfo"));
            String key = cacheProvider.getKeyGenerator().generate(resource, renderContext);
            String perUserKey = key.replaceAll("_perUser_", renderContext.getUser().getUsername()).replaceAll("_mr_",renderContext.getMainResource().getNode().getPath());
            if (debugEnabled) {
                logger.debug("Generating content for node : " + perUserKey);
            }

            if (chain.getPreviousValue("currentResource") != null) {
                ((Resource) chain.getPreviousValue("currentResource")).getDependencies()
                        .addAll(resource.getDependencies());
            }

            String cacheAttribute = (String) renderContext.getRequest().getAttribute("expiration");
            Long expiration = cacheAttribute != null ? Long.valueOf(cacheAttribute) :
                    Long.valueOf(script.getTemplate().getProperties().getProperty("cache.expiration", "-1"));
            Set<JCRNodeWrapper> depNodeWrappers = resource.getDependencies();
            for (JCRNodeWrapper nodeWrapper : depNodeWrappers) {
                Long lowestExpiration = 0L;
                String path = nodeWrapper.getPath();
                Map<String, Integer> cachesExpiration = templatesCacheExpiration.get(path);
                if (cachesExpiration != null) {
                    for (long cacheExpiration : cachesExpiration.values()) {
                        lowestExpiration = Math.min(cacheExpiration, lowestExpiration);
                    }
                    expiration = lowestExpiration;
                }
                Element element1 = cacheProvider.getDependenciesCache().get(path);
                Set<String> dependencies;
                if (element1 != null) {
                    dependencies = (Set<String>) element1.getValue();
                } else {
                    dependencies = new LinkedHashSet<String>();
                }
                dependencies.add(perUserKey);
                cacheProvider.getDependenciesCache().put(new Element(path, dependencies));
            }
            CacheEntry<String> cacheEntry = new CacheEntry<String>(previousOut);
            Element cachedElement = new Element(perUserKey, cacheEntry);
            BlockingCache cache = cacheProvider.getCache();
            if (expiration >= 0) {
                cachedElement.setTimeToLive(expiration.intValue() + 1);
                cachedElement.setTimeToIdle(1);
                Map<String, Integer> cachesExpiration = templatesCacheExpiration.get(resource.getNode().getPath());
                if (cachesExpiration == null) {
                    cachesExpiration = new HashMap<String, Integer>();
                }
                cachesExpiration.put(key, expiration.intValue());
                templatesCacheExpiration.put(resource.getNode().getPath(), cachesExpiration);
                String hiddenKey = cacheProvider.getKeyGenerator().replaceField(perUserKey, "template", "hidden.load");
                Element hiddenElement =
                        cache.isKeyInCache(hiddenKey) ? cache.get(hiddenKey) :
                                null;
                if (hiddenElement != null) {
                    hiddenElement.setTimeToIdle(1);
                    hiddenElement.setTimeToLive(expiration.intValue() + 1);
                    cache.put(hiddenElement);
                }
                hiddenKey = cacheProvider.getKeyGenerator().replaceField(perUserKey, "template", "hidden.footer");
                hiddenElement = cache.isKeyInCache(hiddenKey) ? cache.get(hiddenKey) : null;
                if (hiddenElement != null) {
                    hiddenElement.setTimeToLive(expiration.intValue() + 1);
                    cache.put(hiddenElement);
                }
                hiddenKey = cacheProvider.getKeyGenerator().replaceField(perUserKey, "template", "hidden.header");
                hiddenElement = cache.isKeyInCache(hiddenKey) ? cache.get(hiddenKey) : null;
                if (hiddenElement != null) {
                    hiddenElement.setTimeToLive(expiration.intValue() + 1);
                    cache.put(hiddenElement);
                }
            }
            cache.put(cachedElement);

            if (debugEnabled) {
                logger.debug("Store in cache content of node with key: " + perUserKey);
                StringBuilder stringBuilder = new StringBuilder();
                for (JCRNodeWrapper nodeWrapper : depNodeWrappers) {
                    stringBuilder.append(nodeWrapper.getPath()).append("\n");
                }
                logger.debug("Dependencies of " + perUserKey + " : \n" + stringBuilder.toString());
            }
            if (displayCacheInfo && !previousOut.contains("<body") && previousOut.trim().length() > 0) {
                return appendDebugInformation(renderContext, key, previousOut, cachedElement);
            } else {
                return previousOut;
            }
        }
        return previousOut;
    }

    public void setCacheProvider(ModuleCacheProvider cacheProvider) {
        this.cacheProvider = cacheProvider;
    }

    private String appendDebugInformation(RenderContext renderContext, String key, String renderContent,
                                          Element cachedElement) {
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("<div class=\"cacheDebugInfo\">");
        stringBuilder.append("<span class=\"cacheDebugInfoLabel\">Key: </span><span>");
        stringBuilder.append(key);
        stringBuilder.append("</span><br/>");
        stringBuilder.append("<span class=\"cacheDebugInfoLabel\">Fragment has been created at: </span><span>");
        stringBuilder.append(SimpleDateFormat.getDateTimeInstance().format(new Date(cachedElement.getCreationTime())));
        stringBuilder.append("</span><br/>");
        stringBuilder.append("<span class=\"cacheDebugInfoLabel\">Fragment will expire at: </span><span>");
        stringBuilder
                .append(SimpleDateFormat.getDateTimeInstance().format(new Date(cachedElement.getExpirationTime())));
        stringBuilder.append("</span>");
        stringBuilder.append("<form action=\"").append(renderContext.getURLGenerator().getContext())
                .append("/flushKey.jsp\" method=\"post\"");
        stringBuilder.append("<input type=\"hidden\" name=\"keyToFlush\" value=\"").append(key).append("\"");
        stringBuilder.append("<button type=\"submit\"title=\"Flush it\">Flush It</button>");
        stringBuilder.append("</form>");
        stringBuilder.append("</div>");
        stringBuilder.append(renderContent);
        return stringBuilder.toString();
    }
}