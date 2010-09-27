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

/*
 * Copyright (c) 2004 Your Corporation. All Rights Reserved.
 */
package org.jahia.services;

import org.jahia.services.templates.TemplatePackageApplicationContextLoader;
import org.jahia.services.templates.TemplatePackageApplicationContextLoader.ContextInitializedEvent;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.ApplicationEvent;
import org.springframework.context.ApplicationListener;

/**
 * Spring application context holder.
 * 
 * @author Sergiy Shyrkov
 */
public class SpringContextSingleton implements ApplicationContextAware, ApplicationListener {

    private static SpringContextSingleton ourInstance;

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
            return getInstance().getModuleContext().getBean(beanId);
        }
    }

    public static SpringContextSingleton getInstance() {
        if (ourInstance == null) {
            ourInstance = new SpringContextSingleton();
        }
        return ourInstance;
    }

    /**
     * Returns an instance of the requested bean, located in the modules
     * application context.
     * 
     * @param beanId the requested bean ID
     * @return an instance of the requested bean, located in the modules
     *         application context
     */
    public static Object getModuleBean(String beanId) {
        return getInstance().getModuleContext().getBean(beanId);
    }

    private ApplicationContext context;

    private boolean initialized;

    private ApplicationContext moduleContext;

    private SpringContextSingleton() {
        super();
    }

    /**
     * Returns the Spring application context instance.
     * 
     * @return the Spring application context instance
     */
    public ApplicationContext getContext() {
        return context;
    }

    /**
     * Returns the Spring application context instance that corresponds to
     * modules.
     * 
     * @return the Spring application context instance that corresponds to
     *         modules
     */
    public ApplicationContext getModuleContext() {
        return moduleContext;
    }

    public boolean isInitialized() {
        return initialized;
    }

    public void onApplicationEvent(ApplicationEvent event) {
        if (event instanceof ContextInitializedEvent) {
            this.moduleContext = ((TemplatePackageApplicationContextLoader) event.getSource()).getContext();
        }
    }

    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.context = applicationContext;
        initialized = true;
    }
}
