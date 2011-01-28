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

// $Id$
//
//  Jahia
//
//  30.10.2000  EV  added in jahia.
//  17.01.2001  AK  change dispatcher method.
//  19.01.2001  AK  replace methods doGet and doPost by the method service.
//  29.01.2001  AK  change re-init way, remove sets methods.
//  10.02.2001  AK  pseudo-bypass the login by forwarding request attributes.
//  27.03.2001  AK  javadoc and change the access to JahiaPrivateSettings.load().
//  28.03.2001  AK  add some jahia path variables.
//  29.03.2001  AK  rename jahia.basic file in jahia.skeleton.
//  20.04.2001  AK  bugfix request uri.
//  17.05.2001  AK  tomcat users check during init.
//  23.05.2001  NK  bug two same parameter in url resolved by removing pathinfo data from request uri
//


package org.jahia.bin;

import org.apache.commons.io.IOUtils;
import org.jahia.params.*;
import org.jahia.services.JahiaAfterInitializationService;
import org.jahia.services.content.JCRSessionFactory;
import org.jahia.services.usermanager.JahiaUser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.jahia.bin.errors.DefaultErrorHandler;
import org.jahia.data.JahiaData;
import org.jahia.exceptions.JahiaException;
import org.jahia.exceptions.JahiaInitializationException;
import org.jahia.exceptions.JahiaPageNotFoundException;
import org.jahia.exceptions.JahiaSiteNotFoundException;
import org.jahia.registries.ServicesRegistry;
import org.jahia.services.SpringContextSingleton;
import org.jahia.services.cache.CacheService;
import org.jahia.services.sites.JahiaSite;
import org.jahia.services.sites.JahiaSitesService;
import org.jahia.settings.SettingsBean;
import org.jahia.utils.Version;

import javax.servlet.ServletConfig;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.*;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.Map;
import java.util.Properties;

/**
 * This is the main servlet of Jahia.
 *   ----=[  Welcome to the Jahia portal  ]=----
 *
 * Copyright:    Copyright (c) 2002
 * Company:      Jahia Ltd
 *
 * @author  Eric Vassalli
 * @author  Alexandre Kraft
 * @author  Khue N'Guyen
 * @version 1.0
 */
public final class Jahia extends HttpServlet implements JahiaInterface {

    private static final long serialVersionUID = -4811687571425897497L;
    
    private static Logger logger = LoggerFactory.getLogger(Jahia.class);
    private static Logger accessLogger = LoggerFactory.getLogger("accessLogger");

    static private final String INIT_PARAM_SUPPORTED_JDK_VERSIONS =
        "supported_jdk_versions";

    static private final String INIT_PARAM_ADMIN_SERVLET_PATH =
        "admin_servlet_path";

    static private final String CTX_PARAM_DEFAULT_SERVLET_PATH =
        "defaultJahiaServletPath";

    /** ... */
    static public final String COPYRIGHT =
            "&copy; Copyright 2002-2011  <a href=\"http://www.jahia.com\" target=\"newJahia\">Jahia Solutions Group SA</a> -";

    public final static String COPYRIGHT_TXT = "2011 Jahia Solutions Group SA" ;

    static private boolean maintenance = false;

    static protected final String JDK_REQUIRED = "1.5";

    private static SettingsBean jSettings;

    static private ServletConfig staticServletConfig;

    static private boolean mInitiated = false;
    
    static protected String jahiaBasicFileName;
    static protected String jahiaPropertiesPath;
    static protected String jahiaTemplatesScriptsPath;
    static protected String jahiaEtcFilesPath;
    static protected String jahiaVarFilesPath;
    static protected String jahiaBaseFilesPath = "";

    static private String jahiaServletPath;
    static private String jahiaContextPath;
    static private int jahiaHttpPort = -1;

    static private String jahiaInitAdminServletPath;

    static private ThreadLocal<ProcessingContext> paramBeanThreadLocal = new ThreadLocal<ProcessingContext>();
    static private ThreadLocal<HttpServlet> servletThreadLocal = new ThreadLocal<HttpServlet>();

    private static int BUILD_NUMBER = -1;
    
    private static String EDITION;

    /** Jahia server release number */
    private static double RELEASE_NUMBER = 6.5;

    public static final String VERSION = String.valueOf(RELEASE_NUMBER);

    /** Jahia server patch number */
    private static int PATCH_NUMBER = 0;
    

    public static int getBuildNumber() {
        if (BUILD_NUMBER == -1) {
            try {
                URL urlToMavenPom = Jahia.class
                        .getResource("/META-INF/jahia-impl-marker.txt");
                if (urlToMavenPom != null) {
                    InputStream in = Jahia.class
                            .getResourceAsStream("/META-INF/jahia-impl-marker.txt");
                    try {
                        String buildNumber = IOUtils.toString(in);
                        BUILD_NUMBER = Integer.parseInt(buildNumber);
                    } finally {
                        IOUtils.closeQuietly(in);
                    }
                } else {
                    BUILD_NUMBER = 0;
                }
            } catch (IOException ioe) {
                logger.error(ioe.getMessage(), ioe);
                BUILD_NUMBER = 0;
            } catch (NumberFormatException nfe) {
                logger.error(nfe.getMessage(), nfe);
                BUILD_NUMBER = 0;
            }
        }

        return BUILD_NUMBER;
    }

    public static double getReleaseNumber() {
        return RELEASE_NUMBER;
    }

    public static int getPatchNumber() {
        return PATCH_NUMBER;
    }


    //-------------------------------------------------------------------------
    /**
     * Default init inherited from HttpServlet.
     *
     * @param   aConfig  Servlet configuration (inherited).
     */
    public void init (final ServletConfig aConfig)
        throws ServletException {
        super.init(aConfig);
        logger.info("Initializing Jahia...");

        try {
	        startupWithTrust(getBuildNumber());
	
	        // get servlet basic variables, like config and context...
	        staticServletConfig = aConfig;
	        final ServletContext context = aConfig.getServletContext();
	
	        if (jahiaContextPath == null) {
	            initContextData(getServletContext());
	        }
	        // get some value from the web.xml file...
	        jahiaInitAdminServletPath = aConfig.getInitParameter(
	            INIT_PARAM_ADMIN_SERVLET_PATH);
	
	        verifyJavaVersion(aConfig.getInitParameter(INIT_PARAM_SUPPORTED_JDK_VERSIONS));
	
	        if (Jahia.jahiaInitAdminServletPath == null) {
	            jahiaInitAdminServletPath = "/administration/";
	        }
	
	        jahiaEtcFilesPath = context.getRealPath("/WEB-INF/etc");
	        jahiaVarFilesPath = context.getRealPath("/WEB-INF/var");
	
	        // set default paths...
	        jahiaPropertiesPath = context.getRealPath("/WEB-INF/etc/config/");

	        jahiaBaseFilesPath = context.getRealPath("/WEB-INF/var");
	        jahiaTemplatesScriptsPath = jahiaBaseFilesPath + File.separator +
	                                    "templates";
	
	        // check if there is a jahia.properties file...
	        boolean jahiaPropertiesExists = SettingsBean.getInstance() != null && context.getResourceAsStream(SettingsBean.JAHIA_PROPERTIES_FILE_PATH) != null;
	
	        if (!jahiaPropertiesExists) {
	        	logger.error("Cannot find settings file under " + SettingsBean.JAHIA_PROPERTIES_FILE_PATH);
	            throw new JahiaInitializationException("Cannot find settings file under " + SettingsBean.JAHIA_PROPERTIES_FILE_PATH);
	        }
	        
	        Jahia.setMaintenance(SettingsBean.getInstance().isMaintenanceMode());
	        
	        try {
	            // retrieve the jSettings object...
	            Jahia.jSettings = SettingsBean.getInstance();
	            
	            Jahia.jSettings.setBuildNumber(getBuildNumber());
	           
	        } catch (Exception e) {
	        	logger.error("Unable to initialize Jahia settings and build number", e);
	        	throw new JahiaInitializationException("Unable to initialize Jahia settings and build number", e);
	        }
	
	        // Initialize all the registered services.
            if (initServicesRegistry()) {
                ServicesRegistry.getInstance().getSchedulerService().startSchedulers();
            }

            // Activate the JMS synchronization if needed
            final CacheService factory = ServicesRegistry.getInstance().getCacheService();
            if (factory.isClusterCache()) {
                factory.enableClusterSync();
            }

            final JahiaUser jahiaUser = ServicesRegistry.getInstance().getJahiaUserManagerService().lookupUser(
                    "root");
            JCRSessionFactory.getInstance().setCurrentUser(jahiaUser);

            if (SpringContextSingleton.getInstance().isInitialized()) {
	            Map map = SpringContextSingleton.getInstance().getContext().getBeansOfType(
	                    JahiaAfterInitializationService.class);
	            for (Object o : map.values()) {
	                JahiaAfterInitializationService initializationService = (JahiaAfterInitializationService) o;
	                initializationService.initAfterAllServicesAreStarted();
	            }
	            map = SpringContextSingleton.getInstance().getModuleContext().getBeansOfType(
	                    JahiaAfterInitializationService.class);
	            for (Object o : map.values()) {
	                JahiaAfterInitializationService initializationService = (JahiaAfterInitializationService) o;
	                initializationService.initAfterAllServicesAreStarted();
	            }
            }
            mInitiated = true;
        } catch (Exception je) {
            logger.error("Error during initialization of Jahia", je);
            // init error, stop Jahia!
            if (je instanceof ServletException) {
            	throw (ServletException) je;
            } else {
            	throw new ServletException(je);
            }
        } finally {
            JCRSessionFactory.getInstance().setCurrentUser(null);
        }
    } // end init

    /**
     * startupWithTrust
     * AK    20.01.2001
     */
    private void startupWithTrust(int buildNumber) {
        Integer buildNumberInteger = new Integer(buildNumber);
        String buildString = buildNumberInteger.toString();
        StringBuilder buildBuffer = new StringBuilder();

        for (int i = 0; i < buildString.length(); i++) {
            buildBuffer.append(" ");
            buildBuffer.append(buildString.substring(i, i + 1));
        }

        String msg;
        InputStream is = null;
        try {
        	is = this.getClass().getResourceAsStream("/jahia-startup-intro.txt");
            msg = IOUtils.toString(is);
        } catch (Exception e) {
        	logger.warn(e.getMessage(), e);
            msg = 
                    "\n\n\n\n"
                            + "                                     ____.\n"
                            + "                         __/\\ ______|    |__/\\.     _______\n"
                            + "              __   .____|    |       \\   |    +----+       \\\n"
                            + "      _______|  /--|    |    |    -   \\  _    |    :    -   \\_________\n"
                            + "     \\\\______: :---|    :    :           |    :    |         \\________>\n"
                            + "             |__\\---\\_____________:______:    :____|____:_____\\\n"
                            + "                                        /_____|\n"
                            + "\n"
                            + "      . . . s t a r t i n g   j a h i a   b u i l d  @BUILD_NUMBER@"+
                    " . . .\n"
                            + "\n\n"
                            + "   Copyright 2002-2011 - Jahia Solutions Group SA http://www.jahia.com - All Rights Reserved\n"
                            + "\n\n"
                            + " *******************************************************************************\n"
                            + " * The contents of this software, or the files included with this software,    *\n"
                            + " * are subject to the GNU General Public License (GPL).                        *\n"
                            + " * You may not use this software except in compliance with the license. You    *\n"
                            + " * may obtain a copy of the license at http://www.jahia.com/license. See the   *\n"
                            + " * license for the rights, obligations and limitations governing use of the    *\n"
                            + " * contents of the software.                                                   *\n"
                            + " *******************************************************************************\n"
                            + "\n\n";
		} finally {
			IOUtils.closeQuietly(is);
		}
        msg = msg.replace("@BUILD_NUMBER@", buildBuffer.toString());

        System.out.println (msg);
        System.out.flush();
    }


	private void verifyJavaVersion(String supportedJDKVersions) throws JahiaInitializationException {
        if (supportedJDKVersions != null) {
                Version currentJDKVersion;
                try {
                    currentJDKVersion = new Version(System.getProperty("java.version"));
                    if (!isSupportedJDKVersion(currentJDKVersion, supportedJDKVersions)) {
                        StringBuffer jemsg = new StringBuffer();
                        jemsg.append("WARNING<br/>\n");
                        jemsg.append(
                            "You are using an unsupported JDK version\n");
                        jemsg.append("or have an invalid ").append(
                                     INIT_PARAM_SUPPORTED_JDK_VERSIONS).append(
                                     " parameter string in \n");
                        jemsg.append(
                            "the deployment descriptor file web.xml.\n");
                        jemsg.append(
                            "<br/><br/>Here is the range specified in the web.xml file : ").
                                append(supportedJDKVersions).append(".\n");
                        jemsg.append(
                                "<br/>If you want to disable this warning, remove the ");
                        jemsg.append(INIT_PARAM_SUPPORTED_JDK_VERSIONS);
                        jemsg.append("\n");
                        jemsg.append(
                            "<br/>initialization parameter in the tomcat/webapps/jahia/WEB-INF/web.xml<br/>\n");
                        jemsg.append("<br/><br/>Please note that if you deactivate this check or use unsupported versions<br/>\n");
                        jemsg.append("<br/>You might run into serious problems and we cannot offer support for these.<br/>\n");
                        jemsg.append("<br/>You may download a supported JDK from <a href=\"http://java.sun.com\" target=\"_newSunWindow\">http://java.sun.com</a>.");
                        jemsg.append("<br/><br/>&nbsp;\n");
                        JahiaInitializationException e = new JahiaInitializationException(jemsg.toString());
                        logger.error("Invalid JDK version", e);
                        throw e;
                    }
                } catch (NumberFormatException nfe) {
                    logger.warn("Couldn't convert JDK version to internal version testing system, ignoring JDK version test...", nfe);
                }
            }
	}

	public void destroy() {

        logger.info("Shutdown requested");

        try {
            // first we shutdown the scheduler, because it might be running lots of
            // jobs.
            ServicesRegistry.getInstance().getSchedulerService().stop();
        } catch (Exception je) {
            logger.debug("Error while stopping job scheduler", je);
            if (!logger.isDebugEnabled()) {
                logger.info("Unable to stop job scheduler");
            }
        }
        try {
            ServicesRegistry.getInstance().shutdown();
        } catch (Exception je) {
            logger.debug("Error while shutting down services", je);
            if (!logger.isDebugEnabled()) {
                logger.info("Unable to shut down services");
            }
        }
        super.destroy();

        logger.info("...done shutting down");

    }


    /*
     * Default service inherited from HttpServlet.
     * @author  Alexandre Kraft
     * @author  Fulco Houkes
     *
     * @param   request     Servlet request (inherited).
     * @param   response    Servlet response (inherited).
     */
    public void service (final HttpServletRequest request,
                         final HttpServletResponse response)
        throws IOException,
        ServletException {

        if (logger.isDebugEnabled()) {
            logger.debug("--------------------------------------------- NEW "
                    + request.getMethod().toUpperCase() + " REQUEST ---");
            logger.debug("New request, URL=[" + request.getRequestURI()
                    + "], query=[" + request.getQueryString()
                    + "] serverName=[" + request.getServerName() + "]");

        logger.debug("Character encoding set as: "
                     + request.getCharacterEncoding());
        }

        HttpSession session = request.getSession(false);
        if (session == null || !request.isRequestedSessionIdValid()) {
            // Session could be false because of new user, or missing JSESSIONID
            // id for a non-cookie browser, or a false cookie maybe ?
            logger.debug("Session is null");
            session = request.getSession(true);
            logger.debug("New session id=[" + session.getId() + "]");
        }

        // WEB APP ISSUE
        // This session attribute is used to track that targeted application
        // has been already processed or not.
        final String appid = request.getParameter("appid");
        if ( appid != null ){
            if ( "true".equals(request.getParameter("resetAppSession")) ){
                session.removeAttribute("org.jahia.services.applications.wasProcessed." + appid);
            }
        }

        try {

            if (jahiaContextPath == null) {
                jahiaContextPath = request.getContextPath();
            }

            if (Jahia.jahiaHttpPort == -1) {
                Jahia.jahiaHttpPort = request.getServerPort();
            }

            if (ParamBean.getDefaultSite() == null) {
                JahiaSitesService jahiaSitesService = ServicesRegistry.getInstance().getJahiaSitesService();
                if (jahiaSitesService.getNbSites() > 0) {
                    jahiaSitesService.setDefaultSite(jahiaSitesService.getSites().next());
                } else {
                    jahiaLaunch(request, response,
                                Jahia.jahiaInitAdminServletPath+"?do=sites&sub=list");
                    return;
                }
            }

            // create the parambean (jParams)...
            final ParamBean jParams = createParamBean(request,response,session);

            if (jParams == null) {
                logger.warn("ParamBean not available, aborting processing...");
                return;
            }
            servletThreadLocal.set(this);

            request.setAttribute("org.jahia.params.ParamBean",
                    jParams);
            final JahiaData jData = new JahiaData(jParams, false);
            jParams.getRequest().setAttribute("org.jahia.data.JahiaData", jData);

            // display time to fetch object plus other info
            if (jParams.getUser() != null && logger.isInfoEnabled()) {
                if (!"true".equals(jParams.getSessionState().getAttribute("needToRefreshParentPage"))) {
                    StringBuffer sb = new StringBuffer(100);
                    sb.append("Processed [").append(jParams.getRequest().getRequestURI());
                    sb.append("] user=[").append(jParams.getUser().getUsername() )
                            .append("] ip=[" ).append(jParams.getRequest().getRemoteAddr() )
                            .append("] sessionID=[").append(jParams.getSessionID())
                            .append("] in [" ).append(System.currentTimeMillis() - jParams.getStartTime())
                            .append("ms]");
                            
                    logger.info(sb.toString());
                }
            }
            if (accessLogger.isDebugEnabled()) {
                accessLogger.debug(new StringBuilder(255).append(";").append(jParams.getRealRequest().getRemoteAddr()).append(";").append(jParams.getSiteID()).append(";").append(jParams.getPageID()).append(";").append(jParams.getLocale().toString()).append(";").append(jParams.getUser().getUsername()).toString());
            }
            paramBeanThreadLocal.set(null);
            servletThreadLocal.set(null);
            ServicesRegistry.getInstance().getCacheService().syncClusterNow();
        } catch (Exception e) {
            DefaultErrorHandler.getInstance().handle(e, request, response);
        } finally {
            paramBeanThreadLocal.set(null);
            servletThreadLocal.set(null);
        }
    } // end service

    public static ParamBean createParamBean(final HttpServletRequest request,
                                            final HttpServletResponse response,
                                            final HttpSession session)
    throws IOException, JahiaSiteNotFoundException, JahiaException {

        // all is okay, let's continue...
        boolean exitAdminMode;
        final Integer I = (Integer) session.getAttribute(ProcessingContext.
             SESSION_JAHIA_RUNNING_MODE);

        ParamBean jParams = null;
        try {
            final ProcessingContextFactory pcf = (ProcessingContextFactory) SpringContextSingleton.
                    getInstance().getContext().getBean(ProcessingContextFactory.class.getName());

            jParams = pcf.getContext(request, response, getStaticServletConfig().getServletContext());

            if (jParams != null) {

                exitAdminMode = ( (I != null) &&
                        (I.intValue() == ADMIN_MODE) &&
                        jParams.getEngine().equals(ProcessingContext.
                        CORE_ENGINE_NAME));

                paramBeanThreadLocal.set(jParams);

                // tell we are in Jahia Core mode
                if (exitAdminMode) {
                    session.setAttribute(ProcessingContext.
                            SESSION_JAHIA_RUNNING_MODE,
                            Integer.valueOf(Jahia.CORE_MODE));
                    logger.debug("Switch to Jahia.CORE_MODE");
                }
            }
        } catch (JahiaPageNotFoundException ex) {
            // PAGE NOT FOUND EXCEPTION
            logger.debug(ex.getJahiaErrorMsg(), ex);
//            logger.error(ex.getJahiaErrorMsg());
            String requestURI = request.getRequestURI();
            JahiaSite site = (JahiaSite) request.getSession().getAttribute("org.jahia.services.sites.jahiasite");
            if (site != null && requestURI.indexOf("/op/edit") > 0) {
                    String redirectURL = requestURI;
                    int pidPos = requestURI.indexOf("/pid/");
                    if (pidPos != -1) {
                        // found PID in URL, let's replace it's value.
                        int nextSlashPos = requestURI.indexOf("/", pidPos + "/pid/".length());
                        if (nextSlashPos == -1) {
                            redirectURL = requestURI.substring(0, pidPos) + "/pid/"+site.getHomePageID();
                        } else {
                            redirectURL = requestURI.substring(0, pidPos) + "/pid/"+site.getHomePageID() + requestURI.substring(nextSlashPos);
                        }
                        response.sendRedirect(redirectURL);                        
                    } else {
                        pidPos = requestURI.substring(0,requestURI.length()-2).lastIndexOf("/");
                        // We are using url key
                        int nextSlashPos = requestURI.indexOf("/", pidPos + "/pid/".length());
                        if (nextSlashPos == -1) {
                            redirectURL = requestURI.substring(0, pidPos) + "/pid/"+site.getHomePageID();
                        } else {
                            redirectURL = requestURI.substring(0, pidPos) + "/pid/"+site.getHomePageID() + requestURI.substring(nextSlashPos);
                        }
                        response.sendRedirect(redirectURL);
                    }
            } else {
                throw ex;
            }
        }
        return jParams;
    }

    public static String getServletPath () {
        return jahiaServletPath;
    }

    public static String getContextPath () {
        return jahiaContextPath;
    }

    public static int getJahiaHttpPort() {
        return jahiaHttpPort;
    }

    public static void setJahiaHttpPort(int theJahiaHttpPort) {
        Jahia.jahiaHttpPort = theJahiaHttpPort;
    }

    public static ProcessingContext getThreadParamBean () {
        return paramBeanThreadLocal.get();
    }

    public static void setThreadParamBean(final ProcessingContext processingContext) {
        paramBeanThreadLocal.set(processingContext);
    }

    public static HttpServlet getJahiaServlet () {
        return servletThreadLocal.get();
    }

    //-------------------------------------------------------------------------
    /**
     * Call the initialization of the services registry.
     *
     * @return  Return <code>true</code> on success or <code>false</code> on any failure.
     */
    protected boolean initServicesRegistry ()
        throws JahiaException {

        logger.debug("Start the Services Registry ...");

        try {
            final ServicesRegistry registry = ServicesRegistry.getInstance();
            if (registry != null) {
                registry.init(Jahia.jSettings);
                logger.debug("Services Registry is running...");
                return true;
            }

            logger.debug(
                "  -> ERROR : Could not get the Services Registry instance.");
            return false;
        } catch (JahiaException je) {
            throw new JahiaException(je.getJahiaErrorMsg(),
                "Service Registry Initialization Exception",
                JahiaException.INITIALIZATION_ERROR,
                JahiaException.FATAL_SEVERITY,
                je);
        }
    } // end initServicesRegistry

    //-------------------------------------------------------------------------
    /**
         * Forward the flow to an another servlet. The name of the destination servlet
     * is defined on the <code>destination</code> parameter.
     *
     * @param   request       Servlet request.
     * @param   response      Servlet response.
     * @param   destination   Destination for requestDispatcher.forward().
     */
    private void jahiaLaunch (final HttpServletRequest request,
                              final HttpServletResponse response,
                              final String destination) {
        try {
            getServletContext().getRequestDispatcher(destination).forward(
                request, response);

        } catch (IOException ie) {
            logger.error("IOException on method jahiaLaunch.", ie);
        } catch (ServletException se) {
            logger.error("ServletException on method jahiaLaunch.", se != null
                    && se.getRootCause() != null ? se.getRootCause() : se);
        }
    } // end jahiaLaunch

    //-------------------------------------------------------------------------
    /**
     * Return the private settings
     *
     * @return JahiaPrivateSettings
     */
    public static SettingsBean getSettings () {
        return jSettings;
    }

    /**
     * Return true if this class has been fully initiated
     *
     * @return boolean true if this class has been fully initiated
     */
    public static boolean isInitiated () {
        return mInitiated;
    }


    public static boolean isMaintenance() {
        return maintenance;
    }

    public static void setMaintenance(boolean maintenance) {
        Jahia.maintenance = maintenance;
    }

    
    /**
     * Check if the current JDK we are running Jahia on is supported. The
     * supported JDK string is a specially encoded String that checks only
     * the versions.
     *
     * The accepted format is the following :
     *      version <= x <= version
     * or
     *      version < x < version
     * The "x" character is mandatory !
     *
     * @param currentJDKVersion the current JDK version we are using, this is
     * a valid version object.
     * @param supportedJDKString
     */
    private boolean isSupportedJDKVersion (final Version currentJDKVersion,
                                           final String supportedJDKString) {
        if (supportedJDKString == null) {
            // we deactivate the check if we specify no supported JDKs
            return true;
        }

        final String workString = supportedJDKString.toLowerCase();
        int xPos = workString.indexOf("x");

        if (xPos == -1) {
            logger.debug("Invalid supported_jdk_versions initialization " +
                         " parameter in web.xml, it MUST be in the " +
                         " following format : 1.2 < x <= 1.3 (the 'x' " +
                         "character is mandatory and was missing in " +
                         "this case : [" + supportedJDKString + "] )");
            return false;
        }
        final String leftArg = workString.substring(0, xPos).trim();
        final String rightArg = workString.substring(xPos + 1).trim();

        if (leftArg.endsWith("<=")) {
            final String leftVersionStr = leftArg.substring(0, leftArg.length() - 2).
                                    trim();
            Version lowerVersion;
            try {
                lowerVersion = new Version(leftVersionStr);
            } catch (NumberFormatException nfe) {
                logger.error("Error in lower version number conversion", nfe);
                return false;
            }
            if (lowerVersion.compareTo(currentJDKVersion) > 0) {
                return false;
            }
        } else if (leftArg.endsWith("<")) {
            final String leftVersionStr = leftArg.substring(0, leftArg.length() - 1).
                                    trim();
            Version lowerVersion;
            try {
                lowerVersion = new Version(leftVersionStr);
            } catch (NumberFormatException nfe) {
                logger.error("Error in lower number conversion", nfe);
                return false;
            }
            if (lowerVersion.compareTo(currentJDKVersion) >= 0) {
                return false;
            }
        } else {
            logger.error("Invalid supported_jdk_versions initialization " +
                         " parameter in web.xml, it MUST be in the " +
                " following format : 1.2 < x <= 1.3. Current string : [" +
                supportedJDKString + "] )");
            return false;
        }

        if (rightArg.startsWith("<=")) {
            final String rightVersionStr = rightArg.substring(2).trim();
            Version upperVersion;
            try {
                upperVersion = new Version(rightVersionStr);
            } catch (NumberFormatException nfe) {
                logger.error("Error in upper number conversion", nfe);
                return false;
            }
            if (upperVersion.compareTo(currentJDKVersion) < 0) {
                return false;
            }
        } else if (rightArg.startsWith("<")) {
            final String rightVersionStr = rightArg.substring(1).trim();
            Version upperVersion;
            try {
                upperVersion = new Version(rightVersionStr);
            } catch (NumberFormatException nfe) {
                logger.error("Error in upper number conversion", nfe);
                return false;
            }
            if (upperVersion.compareTo(currentJDKVersion) <= 0) {
                return false;
            }
        } else {
            logger.error("Invalid supported_jdk_versions initialization " +
                         " parameter in web.xml, it MUST be in the " +
                " following format : 1.2 < x <= 1.3. Current string : [" +
                supportedJDKString + "] )");
            return false;
        }

        return true;
    }

    static public ServletConfig getStaticServletConfig() {
        return staticServletConfig;
    }

    static String getDefaultServletPath(ServletContext ctx) {
        String path = ctx.getInitParameter(CTX_PARAM_DEFAULT_SERVLET_PATH);
        if (null == path) {
            // should we alternatively default it to '/Jahia'?
            throw new RuntimeException("Missing required context-param '"
                    + CTX_PARAM_DEFAULT_SERVLET_PATH + "'"
                    + " in the web.xml. Initialization failed.");
        }
        return path;
    }
    
    public static void initContextData(ServletContext servletContext) {

        String ctxPath = ""; // assume ROOT context by default
        InputStream is = servletContext.getResourceAsStream(SettingsBean.JAHIA_PROPERTIES_FILE_PATH);
        if (is != null) {
            Properties settings = new Properties();
            try {
                settings.load(is);
            } catch (Exception e) {
                logger.warn("Unable to read " + SettingsBean.JAHIA_PROPERTIES_FILE_PATH + " resource", e);
            } finally {
                IOUtils.closeQuietly(is);
            }
            ctxPath = (String) settings.get("jahia.contextPath");
            if (ctxPath == null || ctxPath.length() > 0 && !ctxPath.startsWith("/")) {
                logger.error("Invalid value for the jahia.contextPath in the "
                        + SettingsBean.JAHIA_PROPERTIES_FILE_PATH + " resource. Unable to initialize Web application.");
                throw new IllegalArgumentException("Invalid value for the jahia.contextPath in the "
                        + SettingsBean.JAHIA_PROPERTIES_FILE_PATH + " resource. Unable to initialize Web application.");
            }

        }
        Jahia.jahiaContextPath = ctxPath.equals("/") ? "" : ctxPath;
        Jahia.jahiaServletPath = getDefaultServletPath(servletContext);
    }

	public static String getEdition() {
		if (EDITION == null) {
			EDITION = Jahia.class.getResource("/META-INF/jahia-ee-impl-marker.txt") != null ? "EE"
			        : "CE";
		}

		return EDITION;
	}

}