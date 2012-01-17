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

package org.jahia.params.valves;

import org.apache.commons.lang.StringUtils;
import org.jahia.utils.LanguageCodeConverters;
import org.slf4j.Logger;
import org.jahia.bin.Login;
import org.jahia.params.ProcessingContext;
import org.jahia.pipelines.PipelineException;
import org.jahia.pipelines.valves.ValveContext;
import org.jahia.registries.ServicesRegistry;
import org.jahia.services.preferences.user.UserPreferencesHelper;
import org.jahia.services.sites.JahiaSite;
import org.jahia.services.usermanager.JahiaUser;
import org.jahia.services.usermanager.JahiaUserManagerService;
import org.jahia.settings.SettingsBean;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import java.security.Principal;
import java.util.List;
import java.util.Locale;
import java.util.Properties;
import java.util.Set;

/**
 * @author Thomas Draier
 */
public class LoginEngineAuthValveImpl extends BaseAuthValve {
    public static final String ACCOUNT_LOCKED = "account_locked";
    public static final String BAD_PASSWORD = "bad_password";
    private static final transient Logger logger = org.slf4j.LoggerFactory.getLogger(LoginEngineAuthValveImpl.class);
    public static final String LOGIN_TAG_PARAMETER = "doLogin";
    public static final String OK = "ok";
    public static final String UNKNOWN_USER = "unknown_user";
    public static final String USE_COOKIE = "useCookie";
    public static final String VALVE_RESULT = "login_valve_result";

    private CookieAuthConfig cookieAuthConfig;

    private void enforcePasswordPolicy(JahiaUser theUser) {
//        PolicyEnforcementResult evalResult = ServicesRegistry.getInstance().getJahiaPasswordPolicyService().
//                enforcePolicyOnLogin(theUser);
//        if (!evalResult.isSuccess()) {
//            EngineMessages policyMsgs = evalResult.getEngineMessages();
//            EngineMessages resultMessages = new EngineMessages();
//            for (Object o : policyMsgs.getMessages()) {
//                resultMessages.add((EngineMessage) o);
//            }
//        }
    }

    public void invoke(Object context, ValveContext valveContext) throws PipelineException {
        if (!isEnabled()) {
            valveContext.invokeNext(context);
            return;
        }
        
        final AuthValveContext authContext = (AuthValveContext) context;
        final HttpServletRequest httpServletRequest = authContext.getRequest();

        JahiaUser theUser = null;
        boolean ok = false;

        if (isLoginRequested(httpServletRequest)) {

            final String username = httpServletRequest.getParameter("username");
            final String password = httpServletRequest.getParameter("password");

            if ((username != null) && (password != null)) {
                final ServicesRegistry theRegistry = ServicesRegistry.getInstance();
                if (theRegistry != null) {
                    JahiaUserManagerService theService = theRegistry.getJahiaUserManagerService();
                    if (theService != null) {
                        // Check if the user has site access ( even though it is not a user of this site )
                        theUser = ServicesRegistry.getInstance().getJahiaUserManagerService().lookupUser(username);
                        if (theUser != null) {
                            if (theUser.verifyPassword(password)) {
                                if (!isAccountLocked(theUser)) {
                                    ok = true;
                                } else {
                                    logger.warn("Login failed: account for user " + theUser.getUsername() + " is locked.");
                                    httpServletRequest.setAttribute(VALVE_RESULT, ACCOUNT_LOCKED);
                                }
                            } else {
                                logger.warn("Login failed: user " + theUser.getUsername() + " provided bad password.");
                                httpServletRequest.setAttribute(VALVE_RESULT, BAD_PASSWORD);
                            }
                        } else {
                            if (logger.isDebugEnabled()) {
                                logger.debug("Login failed. Unknown username " + username + ".");
                            }
                            httpServletRequest.setAttribute(VALVE_RESULT, UNKNOWN_USER);
                        }
                    }
                }
            }
        }
        if (ok) {
            if (logger.isDebugEnabled()) {
                logger.debug("User " + theUser + " logged in.");
            }
            if (httpServletRequest.getSession(false) != null) {
                httpServletRequest.getSession().invalidate();
            }
            httpServletRequest.setAttribute(VALVE_RESULT, OK);
            authContext.getSessionFactory().setCurrentUser(theUser);

            // do a switch to the user's preferred language
            if (SettingsBean.getInstance().isConsiderPreferredLanguageAfterLogin()) {
                Locale preferredUserLocale = UserPreferencesHelper.getPreferredLocale(theUser, LanguageCodeConverters.resolveLocaleForGuest(httpServletRequest));
                JahiaSite site = (JahiaSite) authContext.getRequest().getSession().getAttribute(ProcessingContext.SESSION_SITE);
                if (site != null) {
                    List<Locale> siteLocales = site.getLanguagesAsLocales();
                    if (siteLocales.contains(preferredUserLocale)) {
                        httpServletRequest.getSession()
                                .setAttribute(ProcessingContext.SESSION_LOCALE, preferredUserLocale);
                    }
                }
            }

            String useCookie = httpServletRequest.getParameter(USE_COOKIE);
            if ((useCookie != null) && ("on".equals(useCookie))) {
                // the user has indicated he wants to use cookie authentication
                // now let's create a random identifier to store in the cookie.
                String cookieUserKey = null;
                // now let's look for a free random cookie value key.
                while (cookieUserKey == null) {
                    cookieUserKey = CookieAuthValveImpl.generateRandomString(cookieAuthConfig.getIdLength());
                    Properties searchCriterias = new Properties();
                    searchCriterias.setProperty(cookieAuthConfig.getUserPropertyName(), cookieUserKey);
                    Set<Principal> foundUsers =
                            ServicesRegistry.getInstance().getJahiaUserManagerService().searchUsers(searchCriterias);
                    if (foundUsers.size() > 0) {
                        cookieUserKey = null;
                    }
                }
                // let's save the identifier for the user in the database
                theUser.setProperty(cookieAuthConfig.getUserPropertyName(), cookieUserKey);
                // now let's save the same identifier in the cookie.
                Cookie authCookie = new Cookie(cookieAuthConfig.getCookieName(), cookieUserKey);
                authCookie.setPath(StringUtils.isNotEmpty(httpServletRequest.getContextPath()) ?
                        httpServletRequest.getContextPath() : "/");
                authCookie.setMaxAge(cookieAuthConfig.getMaxAgeInSeconds());
                authContext.getResponse().addCookie(authCookie);
            }

            enforcePasswordPolicy(theUser);
            // The following was deactivated for performance reasons. We should instead look at doing this with Camel
            // or some other asynchronous way.
            //theUser.setProperty(Constants.JCR_LASTLOGINDATE,
            //        String.valueOf(System.currentTimeMillis()));
        } else {
            valveContext.invokeNext(context);
        }
    }

    protected boolean isLoginRequested(HttpServletRequest request) {
        String doLogin = request.getParameter(LOGIN_TAG_PARAMETER);
        if (doLogin != null) {
            return Boolean.valueOf(doLogin) || "1".equals(doLogin);
        } else if ("/cms".equals(request.getServletPath())) {
            return Login.getMapping().equals(request.getPathInfo());
        }

        return false;
    }

    public void setCookieAuthConfig(CookieAuthConfig cookieAuthConfig) {
        this.cookieAuthConfig = cookieAuthConfig;
    }

}
