/**
 * This file is part of Jahia, next-generation open source CMS:
 * Jahia's next-generation, open source CMS stems from a widely acknowledged vision
 * of enterprise application convergence - web, search, document, social and portal -
 * unified by the simplicity of web content management.
 *
 * For more information, please visit http://www.jahia.com.
 *
 * Copyright (C) 2002-2012 Jahia Solutions Group SA. All rights reserved.
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
 * Commercial and Supported Versions of the program (dual licensing):
 * alternatively, commercial and supported versions of the program may be used
 * in accordance with the terms and conditions contained in a separate
 * written agreement between you and Jahia Solutions Group SA.
 *
 * If you are unsure which license is appropriate for your use,
 * please contact the sales department at sales@jahia.com.
 */

package org.jahia.services.render.filter.cache;

import net.htmlparser.jericho.*;
import net.sf.ehcache.Cache;
import net.sf.ehcache.Element;
import net.sf.ehcache.constructs.blocking.LockTimeoutException;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.builder.ToStringBuilder;
import org.apache.commons.lang.builder.ToStringStyle;
import org.jahia.services.cache.CacheEntry;
import org.jahia.services.content.*;
import org.jahia.services.render.*;
import org.jahia.services.render.filter.AbstractFilter;
import org.jahia.services.render.filter.RenderChain;
import org.jahia.services.render.scripting.Script;
import org.jahia.services.templates.JahiaTemplateManagerService.TemplatePackageRedeployedEvent;
import org.jahia.settings.SettingsBean;
import org.jahia.tools.jvm.ThreadMonitor;
import org.jahia.utils.LanguageCodeConverters;
import org.jahia.utils.Patterns;
import org.slf4j.Logger;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.context.ApplicationEvent;
import org.springframework.context.ApplicationListener;

import javax.jcr.ItemNotFoundException;
import javax.jcr.NodeIterator;
import javax.jcr.PathNotFoundException;
import javax.jcr.RepositoryException;
import javax.servlet.http.HttpServletRequest;
import java.io.Serializable;
import java.text.ParseException;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.jahia.api.Constants.JAHIAMIX_REFERENCES_IN_FIELD;
import static org.jahia.api.Constants.JAHIA_REFERENCE_IN_FIELD_PREFIX;

/**
 * Module content caching filter.
 *
 * @author rincevent
 * @since JAHIA 6.5
 *        Created : 8 janv. 2010
 */
public class AggregateCacheFilter extends AbstractFilter implements ApplicationListener<ApplicationEvent>, InitializingBean {
    protected transient static Logger logger = org.slf4j.LoggerFactory.getLogger(AggregateCacheFilter.class);
    protected ModuleCacheProvider cacheProvider;
    protected ModuleGeneratorQueue generatorQueue;

    public static final Pattern ESI_INCLUDE_STARTTAG_REGEXP = Pattern.compile(
            "<!-- cache:include src=\\\"(.*)\\\" -->");
    public static final Pattern ESI_INCLUDE_STOPTAG_REGEXP = Pattern.compile("<!-- /cache:include -->");
    protected static final Pattern CLEANUP_REGEXP = Pattern.compile(
            "<!-- cache:include src=\\\"(.*)\\\" -->\n|\n<!-- /cache:include -->");
    protected static final Pattern QUERYSTRING_REGEXP = Pattern.compile("(.*)(_qs\\[([^\\]]+)\\]_)(.*)");

    protected static final Pattern P_REGEXP = Pattern.compile("_p_");

    // We use ConcurrentHashMap instead of Set since we absolutely need the thread safety of this implementation but we don't want reads to lock.
    // @todo when migrating to JDK 1.6 we can replacing this with Collections.newSetFromMap(Map m) calls.
    protected static final Map<String,Boolean> notCacheableFragment = new ConcurrentHashMap<String,Boolean>(512);
    protected static final Map<String,Boolean> guestMainResourceRequestParameters = new ConcurrentHashMap<String,Boolean>();
    protected static final Map<String,Boolean> guestnotCacheablePages = new ConcurrentHashMap<String,Boolean>(512);
    static protected ThreadLocal<Set<CountDownLatch>> processingLatches = new ThreadLocal<Set<CountDownLatch>>();
    static protected ThreadLocal<String> acquiredSemaphore = new ThreadLocal<String>();
    static protected ThreadLocal<LinkedList<String>> userKeys = new ThreadLocal<LinkedList<String>>();
    protected static long lastThreadDumpTime = 0L;
    protected Byte[] threadDumpCheckLock = new Byte[0];
    public static final String FORM_TOKEN = "form_token";
    protected Map<String, String> moduleParamsProperties;
    protected int dependenciesLimit = 1000;

    @Override
    public String prepare(RenderContext renderContext, Resource resource, RenderChain chain) throws Exception {
        boolean debugEnabled = logger.isDebugEnabled();
        boolean displayCacheInfo = SettingsBean.getInstance().isDevelopmentMode() && Boolean.valueOf(renderContext.getRequest().getParameter("cacheinfo"));
        @SuppressWarnings("unchecked")
        Set<String> servedFromCache = (Set<String>) renderContext.getRequest().getAttribute("servedFromCache");
        if (servedFromCache == null) {
            servedFromCache = new HashSet<String>();
            renderContext.getRequest().setAttribute("servedFromCache", servedFromCache);
        }

        Properties properties = setAttributesForKey(renderContext, resource, chain);
        String key = cacheProvider.getKeyGenerator().generate(resource, renderContext);

        if (debugEnabled) {
            logger.debug("Cache filter for key {}", key);
        }
        Element element = null;
        final Cache cache = cacheProvider.getCache();
        boolean cacheable = isCacheable(renderContext, resource, key, properties, true);

        String perUserKey = replacePlaceholdersInCacheKey(renderContext, key);
        perUserKey = replaceAclPlaceHolder(renderContext, key, perUserKey);
        LinkedList<String> userKeysLinkedList = userKeys.get();
        if (userKeysLinkedList == null) {
            userKeysLinkedList = new LinkedList<String>();
            userKeys.set(userKeysLinkedList);
        }
        if (userKeysLinkedList.contains(perUserKey)) {
            return null;
        }
        userKeysLinkedList.add(0, perUserKey);
        if (cacheable) {
            try {
                if (debugEnabled) {
                    logger.debug("Try to get content from cache for node with key: {}", perUserKey);
                }
                element = cache.get(perUserKey);
            } catch (LockTimeoutException e) {
                logger.warn("Error while rendering " + renderContext.getMainResource() + e.getMessage(), e);
            }
        }
        if (element != null && element.getValue() != null) {
            return returnFromCache(renderContext, resource, debugEnabled, displayCacheInfo, servedFromCache, key,
                    element, cache, perUserKey);
        } else {
            if (cacheable) {
                // Use CountLatch as not found in cache
                CountDownLatch countDownLatch = avoidParallelProcessingOfSameModule(perUserKey,
                        resource.getContextConfiguration(), renderContext.getRequest());
                if (countDownLatch == null) {
                    element = cache.get(perUserKey);
                    if (element != null && element.getValue() != null) {
                        return returnFromCache(renderContext, resource, debugEnabled, displayCacheInfo, servedFromCache,
                                key, element, cache, perUserKey);
                    }
                } else {
                    Set<CountDownLatch> latches = processingLatches.get();
                    if (latches == null) {
                        latches = new HashSet<CountDownLatch>();
                        processingLatches.set(latches);
                    }
                    latches.add(countDownLatch);
                }
            }
            return null;
        }
    }
    
    /**
     * Is the current fragment cacheable or not
     * 
     * @param renderContext render context
     * @param resource current resource
     * @param key calculated cache key
     * @param properties cache properties
     * @param isInPrepare true if we are in filter prepare mode, and false if we are in filter execute mode
     * @return true if fragments is cacheable, false if not
     * @throws RepositoryException
     */
    protected boolean isCacheable(RenderContext renderContext, Resource resource, String key, Properties properties, boolean isInPrepare) throws RepositoryException {
        boolean cacheable = !notCacheableFragment.containsKey(key);
        if (renderContext.isLoggedIn() && renderContext.getRequest().getParameter("v") != null) {
            cacheable = false;
        }
        if (renderContext.getRequest().getParameter("ec") != null && renderContext.getRequest().getParameter(
                "ec").equals(resource.getNode().getIdentifier())) {
            cacheable = false;
        }
        
        return cacheable;
    }
    
    /**
     * Sets whether dependencies should be stored per cache object for this filter, which is useful for dependent flushes.
     * @return true if filter uses dependencies, false if not
     */
    protected boolean useDependencies() {
        return true;
    }

    @SuppressWarnings("unchecked")
    protected String returnFromCache(RenderContext renderContext, Resource resource, boolean debugEnabled,
                                     boolean displayCacheInfo, Set<String> servedFromCache, String key, Element element,
                                     Cache cache, String perUserKey) {
        if (debugEnabled) {
            logger.debug("Content retrieved from cache for node with key: {}", perUserKey);
        }
        CacheEntry<?> cacheEntry = (CacheEntry<?>) element.getValue();
        String cachedContent = (String) cacheEntry.getObject();
        cachedContent = aggregateContent(cache, cachedContent, renderContext,
                (Map<String, Serializable>) cacheEntry.getProperty("moduleParams"), (String) cacheEntry.getProperty("areaResource"), new Stack<String>());
        setResources(renderContext, cacheEntry);

        if (renderContext.getMainResource() == resource) {
            cachedContent = removeEsiTags(cachedContent);
        }
        servedFromCache.add(key);
        if (displayCacheInfo && !cachedContent.contains("<body") && cachedContent.trim().length() > 0) {
            return appendDebugInformation(renderContext, key, cachedContent, element);
        } else {
            return cachedContent;
        }
    }

    public String execute(String previousOut, RenderContext renderContext, Resource resource, RenderChain chain)
            throws Exception {
        resource.getDependencies().add(resource.getNode().getCanonicalPath());

        Properties properties = setAttributesForKey(renderContext, resource, chain);
        String key = cacheProvider.getKeyGenerator().generate(resource, renderContext);
        @SuppressWarnings("unchecked")
        Set<String> servedFromCache = (Set<String>) renderContext.getRequest().getAttribute("servedFromCache");
        if (servedFromCache.contains(key)) {
            return previousOut;
        }
        if (key.contains("_mr_")) {
            resource.getDependencies().add(renderContext.getMainResource().getNode().getCanonicalPath());
            if (Boolean.valueOf(properties.getProperty("cache.mainResource.flushParent", "false"))) {
                try {
                    resource.getDependencies().add(renderContext.getMainResource().getNode().getParent().getCanonicalPath());
                } catch (ItemNotFoundException e) {
                }
            }
        }
        boolean cacheable = isCacheable(renderContext, resource, key, properties, false);
        final Cache cache = cacheProvider.getCache();
        boolean debugEnabled = logger.isDebugEnabled();
        boolean displayCacheInfo = SettingsBean.getInstance().isDevelopmentMode() && Boolean.valueOf(renderContext.getRequest().getParameter("cacheinfo"));
        String perUserKey = replacePlaceholdersInCacheKey(renderContext, key);
        /*if (Boolean.TRUE.equals(renderContext.getRequest().getAttribute("cache.dynamicRolesAcls"))) {
            key = cacheProvider.getKeyGenerator().replaceField(key, "acls", "dynamicRolesAcls");
            chain.pushAttribute(renderContext.getRequest(), "cache.dynamicRolesAcls", Boolean.FALSE);
        }*/
        perUserKey = replaceAclPlaceHolder(renderContext, key, perUserKey);
        if (debugEnabled) {
            logger.debug("Generating content for node: {}", perUserKey);
        }
        try {
            if (cacheable) {
                int nbOfDependencies = resource.getDependencies().size();
                addReferencesToDependencies(resource);
                if (resource.getDependencies().size() > nbOfDependencies) {
                    key = cacheProvider.getKeyGenerator().generate(resource, renderContext);
                    perUserKey = replacePlaceholdersInCacheKey(renderContext, key);
                    perUserKey = replaceAclPlaceHolder(renderContext, key, perUserKey);
                }
                String perUser = (String) renderContext.getRequest().getAttribute("perUser");
                if (perUser != null && "true".equals(perUser.toLowerCase())) {
                    // This content must be cached by user as it is defined in the options panel
                    // The value of the node property from the mixin jmix:cache are only checked in the TemplatesAttributesFilter
                    // So we need to recalculate the key as we were not aware that this content needed to be cached by user
                    // We need to store content with the previously calculated cache to avoid lock up.
                    key = cacheProvider.getKeyGenerator().replaceField(key, "acls", DefaultCacheKeyGenerator.PER_USER);
                    perUserKey = replacePlaceholdersInCacheKey(renderContext, key);
                }

                doCache(previousOut, renderContext, resource, properties, cache, key, perUserKey);
            }
            if (displayCacheInfo && !previousOut.contains("<body") && previousOut.trim().length() > 0) {
                return appendDebugInformation(renderContext, key, surroundWithCacheTag(key, previousOut), null);
            }
            if (renderContext.getMainResource() == resource) {
                return removeEsiTags(previousOut);
            } else {
                return surroundWithCacheTag(key, previousOut);
            }
        } catch (Exception e) {
            cache.put(new Element(perUserKey, null));
            throw e;
        }
    }

    protected void doCache(String previousOut, RenderContext renderContext, Resource resource, Properties properties, Cache cache, String key, String perUserKey) throws RepositoryException, ParseException {
        Long expiration = getExpiration(renderContext, resource, properties);
        Set<String> depNodeWrappers = Collections.emptySet();
        if (useDependencies()) {
            final Cache dependenciesCache = cacheProvider.getDependenciesCache();
            depNodeWrappers = resource.getDependencies();
            for (String path : depNodeWrappers) {
                Element element1 = dependenciesCache.get(path);
                Set<String> dependencies = element1 != null ? (Set<String>) element1.getValue() : Collections.<String>emptySet();
                if (!dependencies.contains("ALL")) {
                    Set<String> newDependencies = new LinkedHashSet<String>(dependencies.size() + 1);
                    newDependencies.addAll(dependencies);
                    if ((newDependencies.size() + 1) > dependenciesLimit) {
                        newDependencies.clear();
                        newDependencies.add("ALL");
                        dependenciesCache.put(new Element(path, newDependencies));
                    } else {
                        addDependencies(renderContext, perUserKey, dependenciesCache, path, newDependencies);
                    }
                }
            }
            final Cache regexpDependenciesCache = cacheProvider.getRegexpDependenciesCache();
            Set<String> regexpDepNodeWrappers = resource.getRegexpDependencies();
            for (String regexp : regexpDepNodeWrappers) {
                Element element1 = regexpDependenciesCache.get(regexp);
                Set<String> dependencies = element1 != null ? (Set<String>) element1.getValue() : Collections.<String>emptySet();
                Set<String> newDependencies = new LinkedHashSet<String>(dependencies.size() + 1);
                newDependencies.addAll(dependencies);
                addDependencies(renderContext, perUserKey, regexpDependenciesCache, regexp, newDependencies);
            }
        }
        resource.getDependencies().clear();        
        resource.getRegexpDependencies().clear();
        // append cache:include tag
        CacheEntry<String> cacheEntry = getCacheEntry(previousOut, renderContext, resource, key);
        addPropertiesToCacheEntry(resource, cacheEntry);

        Element cachedElement = new Element(perUserKey, cacheEntry);
        if (expiration > 0) {
            cachedElement.setTimeToLive(expiration.intValue());
            String hiddenKey = cacheProvider.getKeyGenerator().replaceField(perUserKey, "template",
                    "hidden.load");
            Element hiddenElement = cache.isKeyInCache(hiddenKey) ? cache.get(hiddenKey) : null;
            if (hiddenElement != null) {
                hiddenElement.setTimeToLive(expiration.intValue());
                cache.put(hiddenElement);
            }
            hiddenKey = cacheProvider.getKeyGenerator().replaceField(perUserKey, "template",
                    "hidden.footer");
            hiddenElement = cache.isKeyInCache(hiddenKey) ? cache.get(hiddenKey) : null;
            if (hiddenElement != null) {
                hiddenElement.setTimeToLive(expiration.intValue());
                cache.put(hiddenElement);
            }
            hiddenKey = cacheProvider.getKeyGenerator().replaceField(perUserKey, "template",
                    "hidden.header");
            hiddenElement = cache.isKeyInCache(hiddenKey) ? cache.get(hiddenKey) : null;
            if (hiddenElement != null) {
                hiddenElement.setTimeToLive(expiration.intValue());
                cache.put(hiddenElement);
            }
        }
        if (expiration != 0) {
            cache.put(cachedElement);
        } else {
            cachedElement = new Element(perUserKey, null);
            cache.put(cachedElement);
            notCacheableFragment.put(key, Boolean.TRUE);
        }
        if (logger.isDebugEnabled()) {
            logger.debug("Store in cache content of node with key: {}", perUserKey);
            StringBuilder stringBuilder = new StringBuilder();
            for (String path : depNodeWrappers) {
                stringBuilder.append(path).append("\n");
            }
            logger.debug("Dependencies of {}:\n", perUserKey, stringBuilder.toString());
        }
    }

    protected Properties setAttributesForKey(RenderContext renderContext, Resource resource, RenderChain chain) throws RepositoryException {
        final Script script = (Script) renderContext.getRequest().getAttribute("script");
        boolean isBound = resource.getNode().isNodeType("jmix:bindedComponent");

        Properties properties = new Properties();

        if (script != null) {
            properties.putAll(script.getView().getDefaultProperties());
            properties.putAll(script.getView().getProperties());
            chain.pushAttribute(renderContext.getRequest(), "cache.perUser", Boolean.valueOf(properties.getProperty("cache.perUser", "false")));
            if (isBound) {
                chain.pushAttribute(renderContext.getRequest(), "cache.mainResource", Boolean.TRUE);
            } else {
                chain.pushAttribute(renderContext.getRequest(), "cache.mainResource", Boolean.valueOf(properties.getProperty("cache.mainResource", "false")));
            }
            String requestParameters = properties.getProperty("cache.requestParameters");
            if (SettingsBean.getInstance().isDevelopmentMode()) {
                StringBuilder stringBuilder = new StringBuilder(requestParameters != null ? requestParameters : "");
                if (stringBuilder.length() == 0) {
                    stringBuilder.append("cacheinfo,moduleinfo");
                } else {
                    stringBuilder.append(",cacheinfo,moduleinfo");
                }
                requestParameters = stringBuilder.toString();
            }
            if (requestParameters != null && !"".equals(requestParameters.trim())) {
                chain.pushAttribute(renderContext.getRequest(), "cache.requestParameters", Patterns.COMMA.split(
                        requestParameters));
            } else {
                chain.pushAttribute(renderContext.getRequest(), "cache.requestParameters", null);
            }
        }
        return properties;
    }

    protected CacheEntry<String> getCacheEntry(String previousOut, RenderContext renderContext, Resource resource, String key) throws RepositoryException {
        String cachedRenderContent = ESI_INCLUDE_STOPTAG_REGEXP.matcher(previousOut).replaceAll("</esi:include>");
        cachedRenderContent = ESI_INCLUDE_STARTTAG_REGEXP.matcher(cachedRenderContent).replaceAll("<esi:include src=\"$1\">");

        logger.debug("Storing for key: {}", key);

        Source source = new Source(cachedRenderContent);
        // This will remove all blank line and drastically reduce data in memory
//                source = new Source((new SourceFormatter(source)).toString());
        List<StartTag> esiIncludeTags = source.getAllStartTags("esi:include");
        /*if (debugEnabled) {
    displaySegments(esiIncludeTags);
}*/
        // We will remove container content here has we do not want to store them twice in memory
        OutputDocument outputDocument = emptyEsiIncludeTagContainer(esiIncludeTags, source);
        String output = outputDocument.toString();
        cachedRenderContent = surroundWithCacheTag(key, output);
        CacheEntry<String> cacheEntry = new CacheEntry<String>(cachedRenderContent);

        return cacheEntry;
    }

    private void addPropertiesToCacheEntry(Resource resource, CacheEntry<String> cacheEntry) throws RepositoryException {
        if (resource.getFormInputs() != null) {
            cacheEntry.setProperty(FORM_TOKEN, resource.getFormInputs());
        }
        LinkedHashMap<String, Object> moduleParams = null;
        for (String property : moduleParamsProperties.keySet()) {
            if (resource.getNode().hasProperty(property)) {
                if (moduleParams == null) {
                    moduleParams = new LinkedHashMap<String, Object>();
                }
                moduleParams.put(moduleParamsProperties.get(property),
                        resource.getNode().getPropertyAsString(property));
            }
        }
        if (moduleParams != null && moduleParams.size() > 0) {
            cacheEntry.setProperty("moduleParams", moduleParams);
        }
        if (resource.getNode().isNodeType("jnt:area") || resource.getNode().isNodeType(
                "jnt:mainResourceDisplay")) {
            cacheEntry.setProperty("areaResource", resource.getNode().getIdentifier());
        }
    }

    protected void addDependencies(RenderContext renderContext, String perUserKey, Cache regexpDependenciesCache, String regexp, Set<String> newDependencies) {
        if (newDependencies.add(KeyCompressor.encodeKey(perUserKey))) {
            regexpDependenciesCache.put(new Element(regexp, newDependencies));
        }
    }

    protected Long getExpiration(RenderContext renderContext, Resource resource, Properties properties) {
        String cacheAttribute = (String) renderContext.getRequest().getAttribute("expiration");
        return cacheAttribute != null ? Long.valueOf(cacheAttribute) : Long.valueOf(properties.getProperty("cache.expiration", "-1"));
    }

    protected String replaceAclPlaceHolder(RenderContext renderContext, String key, String perUserKey)
            throws ParseException, RepositoryException {
        if (!key.contains(DefaultCacheKeyGenerator.PER_USER)) {
            Map<String, String> keyAttrbs = cacheProvider.getKeyGenerator().parse(key);
            String[] split = P_REGEXP.split(keyAttrbs.get("acls"));
            String nodePath = "/" + StringUtils.substringAfter(split[1], "/");
            String acls = ((DefaultCacheKeyGenerator) cacheProvider.getKeyGenerator()).getAclsKeyPart(renderContext,
                    Boolean.parseBoolean(StringUtils.substringBefore(split[1], "/")), nodePath, true, keyAttrbs.get(
                    "acls"));
            perUserKey = cacheProvider.getKeyGenerator().replaceField(perUserKey, "acls", acls);
        }
        return perUserKey;
    }

    protected String replacePlaceholdersInCacheKey(RenderContext renderContext, String key) {
        Matcher m = QUERYSTRING_REGEXP.matcher(key);
        if (m.matches()) {
            Map parameterMap = renderContext.getRequest().getParameterMap();
            String qsString = m.group(2);
            String[] params = Patterns.COMMA.split(m.group(3));

            SortedMap<String, String> qs = new TreeMap<String, String>();
            for (String param : params) {
                param = param.trim();
                if (param.endsWith("*")) {
                    param = param.substring(0, param.length() - 1);
                    for (Map.Entry o : (Iterable<? extends Map.Entry>) parameterMap.entrySet()) {
                        String k = (String) o.getKey();
                        if (k.startsWith(param)) {
                            qs.put(k, Arrays.toString((String[]) o.getValue()));
                        }
                    }
                } else if (parameterMap.containsKey(param)) {
                    qs.put(param, Arrays.toString((String[]) parameterMap.get(param)));
                }
            }
            key = key.replace(qsString, qs.toString());
        }

        return DefaultCacheKeyGenerator.mainResourcePattern.matcher(DefaultCacheKeyGenerator.perUserPattern.matcher(key).replaceAll(
                renderContext.getUser().getUsername())).replaceAll(
                renderContext.getMainResource().getNode().getCanonicalPath() +
                        renderContext.getMainResource().getResolvedTemplate());
    }

    /**
     * Checks if the node properties has references to other content items (links in rich text fields) and adds those as dependencies.
     *
     * @param resource the resource to update dependencies on
     * @throws RepositoryException in case of a repository error
     */
    protected void addReferencesToDependencies(final Resource resource) throws RepositoryException {
        if (resource.getNode().isNodeType(JAHIAMIX_REFERENCES_IN_FIELD)) {
            JCRTemplate.getInstance().doExecuteWithSystemSession(null, resource.getNode().getSession().getWorkspace().getName(), null, new JCRCallback<Object>() {
                public Object doInJCR(JCRSessionWrapper session) throws RepositoryException {
                    NodeIterator ni = session.getNodeByIdentifier(resource.getNode().getIdentifier()).getNodes(JAHIA_REFERENCE_IN_FIELD_PREFIX);
                    while (ni.hasNext()) {
                        JCRNodeWrapper ref = (JCRNodeWrapper) ni.nextNode();
                        try {
                            resource.getDependencies().add(ref.getProperty("j:reference").getNode().getPath());
                        } catch (PathNotFoundException e) {
                            if (logger.isDebugEnabled()) {
                                logger.debug("j:reference property is not found on node {}", ref.getCanonicalPath());
                            }
                        } catch (RepositoryException e) {
                            if (logger.isDebugEnabled()) {
                                logger.debug("referenced node does not exist anymore {}", ref.getCanonicalPath());
                            }
                        } catch (Exception e) {
                            logger.warn("Error adding dependency to node " + resource.getNode().getCanonicalPath(), e);
                        }
                    }
                    return null;
                }
            });
        }
    }

    protected String aggregateContent(Cache cache, String cachedContent, RenderContext renderContext, Map<String, Serializable> moduleParams, String areaIdentifier, Stack<String> cacheKeyStack) {
        // aggregate content
        Source htmlContent = new Source(cachedContent);
        List<? extends Tag> esiIncludeTags = htmlContent.getAllStartTags("esi:include");
        if (esiIncludeTags.size() > 0) {
            OutputDocument outputDocument = new OutputDocument(htmlContent);
            for (Tag esiIncludeTag : esiIncludeTags) {
                StartTag segment = (StartTag) esiIncludeTag;
                if (logger.isDebugEnabled()) {
                    logger.debug(segment.toString());
                }
                String cacheKey = segment.getAttributeValue("src");
                CacheKeyGenerator keyGenerator = cacheProvider.getKeyGenerator();
                if (!cacheKey.contains(DefaultCacheKeyGenerator.PER_USER) && keyGenerator instanceof DefaultCacheKeyGenerator) {
                    DefaultCacheKeyGenerator defaultCacheKeyGenerator = (DefaultCacheKeyGenerator) keyGenerator;
                    try {
                        Map<String, String> keyAttrbs = keyGenerator.parse(cacheKey);
                        String[] split = P_REGEXP.split(keyAttrbs.get("acls"));
                        String nodePath = "/" + StringUtils.substringAfter(split[1], "/");
                        String acls = defaultCacheKeyGenerator.getAclsKeyPart(renderContext, Boolean.parseBoolean(StringUtils.substringBefore(split[1], "/")), nodePath, true, keyAttrbs.get("acls"));
                        cacheKey = keyGenerator.replaceField(cacheKey, "acls", acls);
                        if (renderContext.getRequest().getParameter("ec") != null &&
                                renderContext.getRequest().getParameter("ec").equals(keyAttrbs.get("resourceID"))) {
                            cacheKey = keyGenerator.replaceField(cacheKey, "queryString",
                                    renderContext.getRequest().getQueryString());
                        }
                        if (renderContext.isLoggedIn() && renderContext.getRequest().getParameter("v") != null) {
                            cacheKey = keyGenerator.replaceField(cacheKey, "queryString", UUID.randomUUID().toString());
                        }
                    } catch (ParseException e) {
                        logger.error(e.getMessage(), e);
                    } catch (PathNotFoundException e) {
                        try {
                            cacheKey = keyGenerator.replaceField(cacheKey, "acls", "invalid");
                        } catch (ParseException e1) {
                            logger.error(e1.getMessage(), e1);
                        }
                    } catch (RepositoryException e) {
                        logger.error(e.getMessage(), e);
                    }
                }

                String mrCacheKey = replacePlaceholdersInCacheKey(renderContext, cacheKey);
                cacheKey = DefaultCacheKeyGenerator.perUserPattern.matcher(cacheKey).replaceAll(renderContext.getUser().getUsername());

                if (logger.isDebugEnabled()) {
                    logger.debug("Check if {} is in cache", mrCacheKey);
                }
                if (cache.isKeyInCache(mrCacheKey)) {
                    final Element element = cache.get(mrCacheKey);
                    if (element != null && element.getValue() != null) {
                        if (logger.isDebugEnabled()) {
                            logger.debug("{} has been found in cache", mrCacheKey);
                        }
                        @SuppressWarnings("unchecked")
                        final CacheEntry<String> cacheEntry = (CacheEntry<String>) element.getValue();
                        String content = cacheEntry.getObject();
                        /*if (logger.isDebugEnabled()) {
                            logger.debug("Document replace from : " + segment.getStartTagType() + " to " +
                                         segment.getElement().getEndTag().getEndTagType() + " with " + content);
                        }*/

                        if (cacheKeyStack.contains(cacheKey)) {
                            continue;
                        }
                        cacheKeyStack.push(cacheKey);

                        if (!cachedContent.equals(content)) {
                            String aggregatedContent = aggregateContent(cache, content, renderContext, (Map<String, Serializable>) cacheEntry.getProperty("moduleParams"), (String) cacheEntry.getProperty("areaResource"), cacheKeyStack);
                            outputDocument.replace(segment.getBegin(), segment.getElement().getEndTag().getEnd(),
                                    aggregatedContent);
                        } else {
                            outputDocument.replace(segment.getBegin(), segment.getElement().getEndTag().getEnd(),
                                    content);
                        }
                        setResources(renderContext, cacheEntry);

                        cacheKeyStack.pop();
                    } else {
                        cache.put(new Element(mrCacheKey, null));
                        if (logger.isDebugEnabled()) {
                            logger.debug("Missing content: {}", mrCacheKey);
                        }
                        generateContent(renderContext, outputDocument, segment, cacheKey, moduleParams, areaIdentifier);
                    }
                } else {
                    if (logger.isDebugEnabled()) {
                        logger.debug("Missing content: {}", mrCacheKey);
                    }
                    generateContent(renderContext, outputDocument, segment, cacheKey, moduleParams, areaIdentifier);
                }
            }
            return outputDocument.toString();
        }
        return cachedContent;
    }

    @SuppressWarnings("unchecked")
    protected void setResources(RenderContext renderContext, CacheEntry<?> cacheEntry) {
        Object property = cacheEntry.getProperty(FORM_TOKEN);
        if (property != null) {
            Map<String, Map<String, String>> forms = (Map<String, Map<String, String>>) renderContext.getRequest().getAttribute(
                    "form-parameter");
            if (forms == null) {
                forms = new HashMap<String, Map<String, String>>();
                renderContext.getRequest().setAttribute("form-parameter", forms);
            }
            forms.putAll((Map<? extends String, ? extends Map<String, String>>) property);
        }
    }

    protected void generateContent(RenderContext renderContext, OutputDocument outputDocument, StartTag segment,
                                   String cacheKey, Map<String, Serializable> moduleParams, String areaIdentifier) {
        // if missing data call RenderService after creating the right resource
        final CacheKeyGenerator cacheKeyGenerator = cacheProvider.getKeyGenerator();
        try {
            Map<String, String> keyAttrbs = cacheKeyGenerator.parse(cacheKey);
            JCRSessionWrapper currentUserSession = JCRSessionFactory.getInstance().getCurrentUserSession(keyAttrbs.get(
                    "workspace"), LanguageCodeConverters.languageCodeToLocale(keyAttrbs.get("language")),
                    renderContext.getFallbackLocale());
            JCRNodeWrapper node = null;
            try {
                node = currentUserSession.getNode(keyAttrbs.get("path"));
            } catch (PathNotFoundException e) {
                if (logger.isDebugEnabled()) {
                    logger.debug("Node {} is no longer available." + " Replacing output with empty content.",
                            keyAttrbs.get("path"));
                }
                outputDocument.replace(segment.getBegin(), segment.getElement().getEndTag().getEnd(),
                        StringUtils.EMPTY);
                return;
            }
            if (logger.isDebugEnabled()) {
                logger.debug("Calling render service for generating content for key " + cacheKey + " with attributes : " +
                        new ToStringBuilder(keyAttrbs, ToStringStyle.MULTI_LINE_STYLE) + "\nmodule params : " +
                        ToStringBuilder.reflectionToString(moduleParams, ToStringStyle.MULTI_LINE_STYLE) +
                        " areaIdentifier " + areaIdentifier);
            }
            renderContext.getRequest().removeAttribute(
                    "areaNodeTypesRestriction" + renderContext.getRequest().getAttribute("org.jahia.modules.level"));
            Template oldOne = (Template) renderContext.getRequest().getAttribute("previousTemplate");
            String context = keyAttrbs.get("context");
            if (!context.equals("page")) {
                renderContext.getRequest().setAttribute("templateSet", Boolean.TRUE);
            }
            if (!StringUtils.isEmpty(keyAttrbs.get("templateNodes"))) {
                Template templateNodes = new Template(keyAttrbs.get("templateNodes"));
                renderContext.getRequest().setAttribute("previousTemplate", templateNodes);
            } else {
                renderContext.getRequest().removeAttribute("previousTemplate");
            }

            renderContext.getRequest().setAttribute("skipWrapper", Boolean.TRUE);
            Object oldInArea = (Object) renderContext.getRequest().getAttribute("inArea");
            String inArea = keyAttrbs.get("inArea");
            if (inArea == null || "".equals(inArea)) {
                renderContext.getRequest().removeAttribute("inArea");
            } else {
                renderContext.getRequest().setAttribute("inArea", Boolean.valueOf(inArea));
            }
            if (areaIdentifier != null) {
                renderContext.getRequest().setAttribute("areaListResource", currentUserSession.getNodeByIdentifier(areaIdentifier));
            }
            Resource resource = new Resource(node, keyAttrbs.get("templateType"), keyAttrbs.get("template"), context);
            if (moduleParams != null) {
                for (Map.Entry<String, Serializable> entry : moduleParams.entrySet()) {
                    resource.getModuleParams().put(entry.getKey(), entry.getValue());
                }
            }
            String content = RenderService.getInstance().render(resource, renderContext);
            if (content == null || "".equals(content.trim())) {
                logger.error("Empty generated content for key " + cacheKey + " with attributes : " +
                        new ToStringBuilder(keyAttrbs, ToStringStyle.MULTI_LINE_STYLE) + "\nmodule params : " +
                        ToStringBuilder.reflectionToString(moduleParams, ToStringStyle.MULTI_LINE_STYLE) +
                        " areaIdentifier " + areaIdentifier);
            }
            outputDocument.replace(segment.getBegin(), segment.getElement().getEndTag().getEnd(), content);
            if (oldInArea != null) {
                renderContext.getRequest().setAttribute("inArea", oldInArea);
            } else {
                renderContext.getRequest().removeAttribute("inArea");
            }
            if (oldOne != null) {
                renderContext.getRequest().setAttribute("previousTemplate", oldOne);
            } else {
                renderContext.getRequest().removeAttribute("previousTemplate");
            }
        } catch (ParseException e) {
            logger.error(e.getMessage(), e);
        } catch (RenderException e) {
            logger.error(e.getMessage(), e);
        } catch (RepositoryException e) {
            logger.error(e.getMessage(), e);
        }
    }

    protected String surroundWithCacheTag(String key, String output) {
        String cachedRenderContent;
        StringBuilder builder = new StringBuilder();
        builder.append("<!-- cache:include src=\"");
        builder.append(key);
        builder.append("\" -->\n");
        builder.append(output);
        builder.append("\n<!-- /cache:include -->");
        cachedRenderContent = builder.toString();
        return cachedRenderContent;
    }

    public void setCacheProvider(ModuleCacheProvider cacheProvider) {
        this.cacheProvider = cacheProvider;
    }

    public void setGeneratorQueue(ModuleGeneratorQueue generatorQueue) {
        this.generatorQueue = generatorQueue;
    }

    protected String appendDebugInformation(RenderContext renderContext, String key, String renderContent,
                                            Element cachedElement) {
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("<div class=\"cacheDebugInfo\">");
        stringBuilder.append("<span class=\"cacheDebugInfoLabel\">Key: </span><span>");
        stringBuilder.append(key);
        stringBuilder.append("</span><br/>");
        /*if (cachedElement != null && cachedElement.getValue() != null) {
            stringBuilder.append("<span class=\"cacheDebugInfoLabel\">Fragment has been created at: </span><span>");
            stringBuilder.append(SimpleDateFormat.getDateTimeInstance().format(new Date(
                    cachedElement.getCreationTime())));
            stringBuilder.append("</span><br/>");
            stringBuilder.append("<span class=\"cacheDebugInfoLabel\">Fragment will expire at: </span><span>");
            stringBuilder.append(SimpleDateFormat.getDateTimeInstance().format(new Date(
                    cachedElement.getExpirationTime())));
            stringBuilder.append("</span>");
            stringBuilder.append("<form action=\"").append(renderContext.getURLGenerator().getContext()).append(
                    "/tools/ehcache/flushKey.jsp\" method=\"post\"");
            stringBuilder.append("<input type=\"hidden\" name=\"keyToFlush\" value=\"").append(key).append("\"");
            stringBuilder.append("<button type=\"submit\"title=\"Flush it\">Flush It</button>");
            stringBuilder.append("</form>");
        } else {
            stringBuilder.append("<span class=\"cacheDebugInfoLabel\">Fragment Not Cacheable</span><br/>");
        }*/
        stringBuilder.append("</div>");
        stringBuilder.append(renderContent);
        return stringBuilder.toString();
    }

    protected static OutputDocument emptyEsiIncludeTagContainer(Iterable<StartTag> segments, Source source) {
        OutputDocument outputDocument = new OutputDocument(source);
        for (StartTag segment : segments) {
            outputDocument.replace(segment.getElement().getContent(), "");
        }
        return outputDocument;
    }

    public static String removeEsiTags(String content) {
        if (StringUtils.isNotEmpty(content)) {
            String s = CLEANUP_REGEXP.matcher(content).replaceAll("");
            return s;
        } else {
            return content;
        }
    }

    @Override
    public void handleError(RenderContext renderContext, Resource resource, RenderChain chain, Exception e) {
        super.handleError(renderContext, resource, chain, e);
        LinkedList<String> userKeysLinkedList = userKeys.get();
        if (userKeysLinkedList != null && userKeysLinkedList.size() > 0) {
            String perUserKey = userKeysLinkedList.get(0);

            final Cache cache = cacheProvider.getCache();
            cache.put(new Element(perUserKey, null));
        }
    }

    @Override
    public void finalize(RenderContext renderContext, Resource resource, RenderChain chain) {
        LinkedList<String> userKeysLinkedList = userKeys.get();
        if (userKeysLinkedList != null && userKeysLinkedList.size() > 0) {

            String perUserKey = userKeysLinkedList.remove(0);
            if (perUserKey.equals(acquiredSemaphore.get())) {
                generatorQueue.getAvailableProcessings().release();
                acquiredSemaphore.set(null);
            }

            Set<CountDownLatch> latches = processingLatches.get();
            Map<String, CountDownLatch> countDownLatchMap = generatorQueue.getGeneratingModules();
            CountDownLatch latch = countDownLatchMap.get(perUserKey);
            if (latches != null && latches.contains(latch)) {
                latch.countDown();
                synchronized (countDownLatchMap) {
                    latches.remove(countDownLatchMap.remove(perUserKey));
                }
            }
        }
    }

    protected CountDownLatch avoidParallelProcessingOfSameModule(String key, String contextConfiguration,
                                                                 HttpServletRequest request) throws Exception {
        CountDownLatch latch = null;
        boolean mustWait = true;
        boolean semaphoreAcquired = false;
        Map<String, CountDownLatch> generatingModules = generatorQueue.getGeneratingModules();
        if (generatingModules.get(key) == null && acquiredSemaphore.get() == null) {
            if (!generatorQueue.getAvailableProcessings().tryAcquire(generatorQueue.getModuleGenerationWaitTime(),
                    TimeUnit.MILLISECONDS)) {
                manageThreadDump();
                throw new Exception("Module generation takes too long due to maximum parallel processings reached (" +
                        generatorQueue.getMaxModulesToGenerateInParallel() + ") - " + key + " - " +
                        request.getRequestURI());
            } else {
                acquiredSemaphore.set(key);
                semaphoreAcquired = true;
            }
        }
        if (!generatorQueue.isUseLatchOnlyForPages() || "page".equals(contextConfiguration)) {
            synchronized (generatingModules) {
                latch = (CountDownLatch) generatingModules.get(key);
                if (latch == null) {
                    latch = new CountDownLatch(1);
                    generatingModules.put(key, latch);
                    mustWait = false;
                }
            }
        } else {
            mustWait = false;
        }
        if (mustWait) {
            if (semaphoreAcquired) {
                // another thread wanted the same module and got the latch first, so release the semaphore immediately as we must wait 
                generatorQueue.getAvailableProcessings().release();
                acquiredSemaphore.set(null);
            }
            try {
                if (!latch.await(generatorQueue.getModuleGenerationWaitTime(), TimeUnit.MILLISECONDS)) {
                    manageThreadDump();
                    throw new Exception("Module generation takes too long due to module not generated fast enough (>" +
                            generatorQueue.getModuleGenerationWaitTime() + " ms)- " + key + " - " +
                            request.getRequestURI());
                }
                latch = null;
            } catch (InterruptedException ie) {
                if (logger.isDebugEnabled()) {
                    logger.debug("The waiting thread has been interrupted :", ie);
                }
                throw new Exception(ie);
            }
        }
        return latch;
    }

    protected void manageThreadDump() {
        boolean createDump = false;
        long minInterval = generatorQueue.getMinimumIntervalAfterLastAutoThreadDump();
        if (minInterval > -1 && (generatorQueue.isThreadDumpToSystemOut() || generatorQueue.isThreadDumpToFile())) {
            long now = System.currentTimeMillis();
            synchronized (threadDumpCheckLock) {
                if (now > (lastThreadDumpTime + minInterval)) {
                    createDump = true;
                    lastThreadDumpTime = now;
                }
            }
        }
        if (createDump) {
            ThreadMonitor tm = ThreadMonitor.getInstance();
            tm.dumpThreadInfo(generatorQueue.isThreadDumpToSystemOut(), generatorQueue.isThreadDumpToFile());
            tm = null;
        }
    }

    public void onApplicationEvent(ApplicationEvent event) {
        if (event instanceof TemplatePackageRedeployedEvent) {
            notCacheableFragment.clear();
        }
    }

    public void setModuleParamsProperties(Map<String, String> moduleParamsProperties) {
        this.moduleParamsProperties = moduleParamsProperties;
    }


    /**
     * Invoked by a BeanFactory after it has set all bean properties supplied
     * (and satisfied BeanFactoryAware and ApplicationContextAware).
     * <p>This method allows the bean instance to perform initialization only
     * possible when all bean properties have been set and to throw an
     * exception in the event of misconfiguration.
     *
     * @throws Exception in the event of misconfiguration (such
     *                   as failure to set an essential property) or if initialization fails.
     */
    public void afterPropertiesSet() throws Exception {
        Config.LoggerProvider = LoggerProvider.DISABLED;
    }

    public void setDependenciesLimit(int dependenciesLimit) {
        this.dependenciesLimit = dependenciesLimit;
    }

    public void removeNotCacheableFragment(String key) {
        try {
            CacheKeyGenerator keyGenerator = cacheProvider.getKeyGenerator();
            if (keyGenerator instanceof DefaultCacheKeyGenerator) {
                DefaultCacheKeyGenerator defaultCacheKeyGenerator = (DefaultCacheKeyGenerator) keyGenerator;
                Map<String, String> keyAttrbs = defaultCacheKeyGenerator.parse(KeyCompressor.decodeKey(key));
                String path = keyAttrbs.get("path");
                List<String> removableKeys = new ArrayList<String>();
                for (String notCacheableKey : notCacheableFragment.keySet()) {
                    if (notCacheableKey.contains(path)) {
                        removableKeys.add(notCacheableKey);
                    }
                }
                for (String removableKey : removableKeys) {
                    notCacheableFragment.remove(removableKey);
                }
            }
        } catch (ParseException e) {
            logger.error(e.getMessage(), e);
        }
    }

    public static void flushNotCacheableFragment() {
        notCacheableFragment.clear();
    }
}
