/**
 *
 * This file is part of Jahia: An integrated WCM, DMS and Portal Solution
 * Copyright (C) 2002-2009 Jahia Limited. All rights reserved.
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

package org.jahia.admin.themes;

import org.jahia.bin.JahiaAdministration;
import org.jahia.bin.Jahia;
import org.jahia.services.sites.JahiaSite;
import org.jahia.services.sites.JahiaSitesService;
import org.jahia.services.usermanager.JahiaUser;
import org.jahia.registries.ServicesRegistry;
import org.jahia.security.license.License;
import org.jahia.data.JahiaData;
import org.jahia.params.ProcessingContext;
import org.jahia.resourcebundle.JahiaResourceBundle;
import org.jahia.exceptions.JahiaException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import javax.servlet.ServletException;
import java.io.IOException;

import org.jahia.operations.valves.ThemeValve;
import org.jahia.admin.AbstractAdministrationModule;

/**
 * Created by IntelliJ IDEA.
 * User: david
 * Date: 5 janv. 2009
 * Time: 15:23:56
 * To change this template use File | Settings | File Templates.
 */
public class ManageThemes extends AbstractAdministrationModule {
    private static org.apache.log4j.Logger logger =
            org.apache.log4j.Logger.getLogger(ManageThemes.class);

    private static final String JSP_PATH = JahiaAdministration.JSP_PATH;

    private JahiaSite site;
    private JahiaUser user;
    private ServicesRegistry sReg;
    private static JahiaSitesService sMgr;
    private String jahiaThemeSelector = "jahiaThemeSelector";
    private License coreLicense;

    /**
     *
     * @param request  Servlet request.
     * @param response Servlet response.
     */
    public void service(HttpServletRequest request,
                        HttpServletResponse response )
            throws Exception {

        JahiaData jData = (JahiaData) request.getAttribute("org.jahia.data.JahiaData");
        ProcessingContext jParams = null;
        if (jData != null) {
            jParams = jData.getProcessingContext();
        }
        coreLicense = Jahia.getCoreLicense();
        if (coreLicense == null) {
            // set request attributes...
            String dspMsg = JahiaResourceBundle.getAdminResource("org.jahia.admin.JahiaDisplayMessage.invalidLicenseKey.label",
                    jParams, jParams.getLocale());
            request.setAttribute("jahiaDisplayMessage", dspMsg);
            // redirect...
            JahiaAdministration.doRedirect(request, response, request.getSession(), JSP_PATH + "menu.jsp");
            return;
        }
        //mappingManager = (JahiaSiteLanguageMappingManager) SpringContextSingleton.getInstance().getContext().getBean(JahiaSiteLanguageMappingManager.class.getName());
        //listManager = (JahiaSiteLanguageListManager) SpringContextSingleton.getInstance().getContext().getBean(JahiaSiteLanguageListManager.class.getName());
        userRequestDispatcher(request, response, request.getSession());
    } // end constructor

    private void userRequestDispatcher(HttpServletRequest request,
                                       HttpServletResponse response,
                                       HttpSession session)
            throws Exception {
        JahiaData jData = (JahiaData) request.getAttribute("org.jahia.data.JahiaData");
        ProcessingContext jParams = null;
        if (jData != null) {
            jParams = jData.getProcessingContext();
        }

        String operation = request.getParameter("sub");

        sReg = ServicesRegistry.getInstance();

        // check if the user has really admin access to this site...
        user = (JahiaUser) session.getAttribute(ProcessingContext.SESSION_USER);
        site = (JahiaSite) session.getAttribute(ProcessingContext.SESSION_SITE);

        if (site != null && user != null && sReg != null) {

            // set the new site id to administrate...
            request.setAttribute("site", site);

            if (operation.equals("display")) {
                displayThemesParams(request, response, session);
            } else {
                displayThemesParams(request, response, session);
            }

        } else {
            String dspMsg = JahiaResourceBundle.getAdminResource("org.jahia.admin.JahiaDisplayMessage.requestProcessingError.label",
                    jParams, jParams.getLocale());
            request.setAttribute("jahiaDisplayMessage", dspMsg);
            JahiaAdministration.doRedirect(request,
                    response,
                    session, JSP_PATH + "menu.jsp");
        }
    } // userRequestDispatcher

    //-------------------------------------------------------------------------
    private void displayThemesParams(HttpServletRequest request,
                                     HttpServletResponse response,
                                     HttpSession session)
            throws IOException, ServletException {


        logger.info("displayThemesParams");
        JahiaData jData = (JahiaData) request.getAttribute("org.jahia.data.JahiaData");
        ProcessingContext jParams = null;
        if (jData != null) {
            jParams = jData.getProcessingContext();
            String selectedTheme = request.getParameter(jahiaThemeSelector);
            JahiaSite site = jParams.getSite();
            String gaUserAccountCustom = site.getSettings().getProperty("gaUserAccountCustom");
            if (gaUserAccountCustom == null) site.getSettings().setProperty("gaUserAccountCustom", "");
            if (selectedTheme != null) {
                if (request.getParameter("jahiathemeSelectorScope").equals("site") && jParams.getUser().isAdminMember(site.getID())) {
                    site.getSettings().setProperty(ThemeValve.THEME_ATTRIBUTE_NAME, selectedTheme);
                    try {
                        ServicesRegistry.getInstance().getJahiaSitesService().updateSite(site);
                    } catch (JahiaException e) {
                        logger.error(e);
                    }
                }
            }
        }


        JahiaAdministration.doRedirect(request,
                response,
                session,
                JSP_PATH + "manage_themes.jsp");


    }


}
