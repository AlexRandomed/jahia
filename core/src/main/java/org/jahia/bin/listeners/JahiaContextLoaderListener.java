/**
 * This file is part of Jahia: An integrated WCM, DMS and Portal Solution
 * Copyright (C) 2002-2009 Jahia Solutions Group SA. All rights reserved.
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
package org.jahia.bin.listeners;

import org.apache.log4j.Logger;
import org.apache.pluto.driver.PortalStartupListener;
import org.jahia.bin.Jahia;
import org.jahia.services.applications.ApplicationsManagerServiceImpl;
import org.jahia.services.content.JCRSessionFactory;
import org.jahia.services.templates.TemplatePackageApplicationContextLoader;
import org.jahia.settings.SettingsBean;
import org.springframework.web.context.ContextLoader;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContext;
import javax.servlet.jsp.jstl.core.Config;

import java.net.MalformedURLException;
import java.util.Locale;

/**
 * Startup listener for the Spring's application context.
 * User: Serge Huber
 * Date: 22 juil. 2008
 * Time: 17:01:22
 */
public class JahiaContextLoaderListener extends PortalStartupListener {
    
    private static final transient Logger logger = Logger
            .getLogger(JahiaContextLoaderListener.class);

    private static ServletContext servletContext;

    public void contextInitialized(ServletContextEvent event) {
        servletContext = event.getServletContext();
        String jahiaWebAppRoot = servletContext.getRealPath("/");
        System.setProperty("jahiaWebAppRoot", jahiaWebAppRoot);
        Jahia.initContextData(servletContext);
        
        try {
            boolean configExists = event.getServletContext().getResource(SettingsBean.JAHIA_PROPERTIES_FILE_PATH) != null;
            if (configExists) {
                super.contextInitialized(event);
                try {
                    ((TemplatePackageApplicationContextLoader)ContextLoader.getCurrentWebApplicationContext().getBean("TemplatePackageApplicationContextLoader")).start();
                } catch (Exception e) {
                    logger.error("Error initializing Jahia modules Spring application context. Cause: " + e.getMessage(), e);
                }
                // register listeners after the portal is started
                ApplicationsManagerServiceImpl.getInstance().registerListeners();
            }
            Config.set(servletContext, Config.FMT_FALLBACK_LOCALE, configExists ? SettingsBean
                    .getInstance().getDefaultLanguageCode() : Locale.ENGLISH.getLanguage());                
        } catch (MalformedURLException e) {
            logger.error(e.getMessage(), e);
        } finally {
            JCRSessionFactory.getInstance().closeAllSessions();
        }
    }

    public void contextDestroyed(ServletContextEvent event) {
        try {
            if (event.getServletContext().getResource(SettingsBean.JAHIA_PROPERTIES_FILE_PATH) != null) {
                try {
                    if (ContextLoader.getCurrentWebApplicationContext() != null && ContextLoader.getCurrentWebApplicationContext().getBean("TemplatePackageApplicationContextLoader") != null) {
                        ((TemplatePackageApplicationContextLoader)ContextLoader.getCurrentWebApplicationContext().getBean("TemplatePackageApplicationContextLoader")).stop();
                    }
                } catch (Exception e) {
                    logger.error("Error shutting down Jahia modules Spring application context. Cause: " + e.getMessage(), e);
                }
                super.contextDestroyed(event);
            }
        } catch (MalformedURLException e) {
            logger.error(e.getMessage(), e);
        }
    }

    public static ServletContext getServletContext() {
        return servletContext;
    }
}
