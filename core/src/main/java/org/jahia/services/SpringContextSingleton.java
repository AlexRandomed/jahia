/**
 * ==========================================================================================
 * =                   JAHIA'S DUAL LICENSING - IMPORTANT INFORMATION                       =
 * ==========================================================================================
 *
 *                                 http://www.jahia.com
 *
 *     Copyright (C) 2002-2016 Jahia Solutions Group SA. All rights reserved.
 *
 *     THIS FILE IS AVAILABLE UNDER TWO DIFFERENT LICENSES:
 *     1/GPL OR 2/JSEL
 *
 *     1/ GPL
 *     ==================================================================================
 *
 *     IF YOU DECIDE TO CHOOSE THE GPL LICENSE, YOU MUST COMPLY WITH THE FOLLOWING TERMS:
 *
 *     This program is free software; you can redistribute it and/or
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
 *     2/ JSEL - Commercial and Supported Versions of the program
 *     ===================================================================================
 *
 *     IF YOU DECIDE TO CHOOSE THE JSEL LICENSE, YOU MUST COMPLY WITH THE FOLLOWING TERMS:
 *
 *     Alternatively, commercial and supported versions of the program - also known as
 *     Enterprise Distributions - must be used in accordance with the terms and conditions
 *     contained in a separate written agreement between you and Jahia Solutions Group SA.
 *
 *     If you are unsure which license is appropriate for your use,
 *     please contact the sales department at sales@jahia.com.
 */
/*
 * Copyright (c) 2004 Your Corporation. All Rights Reserved.
 */
package org.jahia.services;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

import org.apache.commons.lang.ArrayUtils;
import org.jahia.data.templates.JahiaTemplatesPackage;
import org.jahia.registries.ServicesRegistry;
import org.jahia.services.templates.JahiaTemplateManagerService.TemplatePackageRedeployedEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.NoSuchBeanDefinitionException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.ApplicationEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.event.ApplicationEventMulticaster;
import org.springframework.context.support.AbstractApplicationContext;
import org.springframework.core.io.Resource;

/**
 * Spring application context holder.
 * 
 * @author Sergiy Shyrkov
 */
public class SpringContextSingleton implements ApplicationContextAware, ApplicationListener<TemplatePackageRedeployedEvent> {

    private transient static Logger logger = LoggerFactory.getLogger(SpringContextSingleton.class);

    private static SpringContextSingleton ourInstance = new SpringContextSingleton();
    
    private Map<String, Resource[]> resourcesCache;

    /**
     * Returns an instance of the requested bean.
     * 
     * @param beanId the requested bean ID
     * @return an instance of the requested bean
     */
    public static Object getBean(String beanId) {
        try {
            return getInstance().getContext().getBean(beanId);
        } catch (BeansException e) {
            return getBeanInModulesContext(beanId);
        }
    }

    public static Object getBeanInModulesContext(String beanId) {
        for (JahiaTemplatesPackage aPackage : ServicesRegistry.getInstance().getJahiaTemplateManagerService().getAvailableTemplatePackages()) {
            if (aPackage.getContext() != null && aPackage.getContext().containsBean(beanId)) {
                return aPackage.getContext().getBean(beanId);
            }
        }
        throw new NoSuchBeanDefinitionException(beanId);
    }
    
    /**
     * Returns a map with beans of the specified type, including beans in modules.
     * 
     * @param type
     *            the bean type to search for
     * @return a map with beans of the specified type, including beans in modules
     * @throws BeansException
     *             in case of a lookup error
     */
    public static <T> Map<String, T> getBeansOfType(Class<T> type) throws BeansException {
        Map<String, T> found = new LinkedHashMap<String, T>();
        found.putAll(SpringContextSingleton.getInstance().getContext().getBeansOfType(type));
        for (JahiaTemplatesPackage aPackage : ServicesRegistry.getInstance().getJahiaTemplateManagerService()
                .getAvailableTemplatePackages()) {
            if (aPackage.getContext() != null) {
                found.putAll(aPackage.getContext().getBeansOfType(type));
            }
        }

        return found;
    }

    public static SpringContextSingleton getInstance() {
        return ourInstance;
    }

    private ApplicationContext context;

    private boolean initialized;

    private SpringContextSingleton() {
        super();
        resourcesCache = new HashMap<String, Resource[]>(2);
    }

    /**
     * Returns the Spring application context instance.
     * 
     * @return the Spring application context instance
     */
    public ApplicationContext getContext() {
        if (!initialized) {
            logger.warn("Trying to access Spring context before it is available ! Please refactor code to avoid this !");
        }
        return context;
    }

    public void publishEvent(ApplicationEvent event) {
        publishEvent(event, true);
    }

    /**
     * Publishes the specified event in the core Spring context and if <code>propagateToModules</code> is set to true, publishes that event
     * to each module's context. When publishing the event to a module, the
     * {@link ApplicationEventMulticaster#multicastEvent(ApplicationEvent)} method is used to skip publishing event to the module's parent
     * context (which is our Spring core context).
     * 
     * @param event
     *            the Spring event to be published
     * @param propagateToModules
     *            whether to propagate the event to all modules
     */
    public void publishEvent(ApplicationEvent event, boolean propagateToModules) {
        getContext().publishEvent(event);
        for (JahiaTemplatesPackage aPackage : ServicesRegistry.getInstance().getJahiaTemplateManagerService()
                .getAvailableTemplatePackages()) {
            if (aPackage.getContext() != null) {
                multicastEvent(event, aPackage.getContext());
            }
        }
    }

    private void multicastEvent(ApplicationEvent event, AbstractApplicationContext ctx) {
        if (!ctx.isActive()) {
            return;
        }
        if (ctx.containsBean(AbstractApplicationContext.APPLICATION_EVENT_MULTICASTER_BEAN_NAME)) {
            ((ApplicationEventMulticaster) ctx
                    .getBean(AbstractApplicationContext.APPLICATION_EVENT_MULTICASTER_BEAN_NAME)).multicastEvent(event);
        } else {
            // fall back to publishEvent()
            ctx.publishEvent(event);
        }
    }

    public boolean isInitialized() {
        return initialized;
    }

    public void onApplicationEvent(TemplatePackageRedeployedEvent event) {
        resourcesCache.clear();
    }

    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.context = applicationContext;
        initialized = true;
    }

    /**
     * Searches for Spring resource locations given the specified (pattern-based) location. Multiple locations can be provided separated by
     * comma (or any delimiter, defined in {@link org.springframework.context.ConfigurableApplicationContext#CONFIG_LOCATION_DELIMITERS} ).
     * 
     * @param locationPatterns
     *            (pattern-based) location to search for resources. Multiple locations can be provided separated by comma (or any delimiter,
     *            defined in {@link org.springframework.context.ConfigurableApplicationContext#CONFIG_LOCATION_DELIMITERS} )
     * @return an array of {@link Resource} objects found
     * @throws IOException
     *             in case of a lookup error
     */
    public Resource[] getResources(String locationPatterns) throws IOException {
        return getResources(locationPatterns, true);
    }

    /**
     * Searches for Spring resource locations given the specified (pattern-based) location. Multiple locations can be provided separated by
     * comma (or any delimiter, defined in {@link org.springframework.context.ConfigurableApplicationContext#CONFIG_LOCATION_DELIMITERS} ).
     * 
     * @param locationPatterns
     *            (pattern-based) location to search for resources. Multiple locations can be provided separated by comma (or any delimiter,
     *            defined in {@link org.springframework.context.ConfigurableApplicationContext#CONFIG_LOCATION_DELIMITERS} )
     * @param useCache can we use lookup caches?
     * @return an array of {@link Resource} objects found
     * @throws IOException
     *             in case of a lookup error
     */
    public Resource[] getResources(String locationPatterns, boolean useCache) throws IOException {
        Resource[] allResources = useCache ? resourcesCache.get(locationPatterns) : null;
        if (allResources == null) {
            allResources = new Resource[0];
            for (String location : org.springframework.util.StringUtils.tokenizeToStringArray(
                    locationPatterns, ConfigurableApplicationContext.CONFIG_LOCATION_DELIMITERS)) {
                try {
                    allResources = (Resource[]) ArrayUtils.addAll(allResources,
                            context.getResources(location.trim()));
                } catch (FileNotFoundException e) {
                    // Ignore
                    logger.debug("Cannot find resources",e);
                }
            }
            if (useCache) {
                resourcesCache.put(locationPatterns, allResources);
            }
        }

        return allResources;
    }

}
