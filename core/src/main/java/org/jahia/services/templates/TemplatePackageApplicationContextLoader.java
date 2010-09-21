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

package org.jahia.services.templates;

import javax.servlet.ServletContext;

import org.apache.log4j.Logger;
import org.jahia.services.SpringContextSingleton;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationEvent;
import org.springframework.web.context.ServletContextAware;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.context.support.XmlWebApplicationContext;

/**
 * Loader and manager of the Spring's application context for Jahia modules.
 * 
 * @author Sergiy Shyrkov
 */
public class TemplatePackageApplicationContextLoader implements ServletContextAware {
    
    /**
     * This event is fired when modules application context is initialized.
     * 
     * @author Sergiy Shyrkov
     */
    public static class ContextInitializedEvent extends ApplicationEvent {
        private static final long serialVersionUID = -2367558261328740803L;

        public ContextInitializedEvent(Object source) {
            super(source);
        }
    }

    private static final Logger logger = Logger.getLogger(TemplatePackageApplicationContextLoader.class);

    private XmlWebApplicationContext context;

    private String contextConfigLocation;

    private ServletContext servletContext;

    private XmlWebApplicationContext createWebApplicationContext() throws BeansException {

        XmlWebApplicationContext ctx = new XmlWebApplicationContext();
        ctx.setParent(SpringContextSingleton.getInstance().getContext());
        ctx.setServletContext(servletContext);
        servletContext.setAttribute(WebApplicationContext.class.getName() + ".jahiaTemplates", ctx);
        ctx.setConfigLocation(contextConfigLocation);
        ctx.refresh();

        return ctx;
    }

    public void reload() {
        logger.info("Reloading Spring application context for Jahia modules");
        long startTime = System.currentTimeMillis();

        context.refresh();

        logger.info("Jahia modules application context reload completed in "
                + (System.currentTimeMillis() - startTime) + " ms");

        context.getParent().publishEvent(new ContextInitializedEvent(this));
    }

    public void setContextConfigLocation(String contextConfigLocation) {
        this.contextConfigLocation = contextConfigLocation;
    }

    public void setServletContext(ServletContext servletContext) {
        this.servletContext = servletContext;
    }

    public void start() {
        logger.info("Initializing Spring application context for Jahia modules");
        long startTime = System.currentTimeMillis();

        context = createWebApplicationContext();
        
        logger.info("Jahia modules application context initialization completed in "
                + (System.currentTimeMillis() - startTime) + " ms");

        context.getParent().publishEvent(new ContextInitializedEvent(this));

    }

    public void stop() {
        if (context != null) {
            try {
                context.close();
            } catch (Exception e) {
                logger.error("Error shutting down Jahia modules Spring application context", e);
            }
        }
    }

    public XmlWebApplicationContext getContext() {
        return context;
    }
}