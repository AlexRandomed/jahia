/**
 * This file is part of Jahia: An integrated WCM, DMS and Portal Solution
 * Copyright (C) 2002-2011 Jahia Solutions Group SA. All rights reserved.
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

package org.jahia.bin;

import javax.jcr.RepositoryException;
import javax.servlet.http.HttpServletRequest;

import org.jahia.exceptions.JahiaBadRequestException;
import org.jahia.exceptions.JahiaForbiddenAccessException;
import org.jahia.exceptions.JahiaUnauthorizedException;
import org.jahia.services.content.JCRSessionFactory;
import org.jahia.services.content.JCRTemplate;
import org.jahia.services.rbac.Permission;
import org.jahia.services.usermanager.JahiaUser;
import org.jahia.services.usermanager.JahiaUserManagerService;
import org.springframework.web.servlet.mvc.multiaction.MultiActionController;

/**
 * Base class for Jahia specific action controllers.
 * 
 * @author Sergiy Shyrkov
 */
public abstract class JahiaMultiActionController extends MultiActionController {

	/**
	 * Simple utility method to retrieve a parameter from a request and throws
	 * an {@link JahiaBadRequestException} (results in a 400 error) in case the
	 * parameter is not found.
	 * 
	 * @param request
	 *            The current HttpServletRequest
	 * @param name
	 *            The parameter name
	 * @return A String containing the value of the given parameter
	 * @throws JahiaBadRequestException
	 *             in case the parameter is not found in the request
	 */
	protected static String getParameter(final HttpServletRequest request, final String name)
	        throws JahiaBadRequestException {
		final String value = request.getParameter(name);
		if (value == null) {
			throw new JahiaBadRequestException("Missing required '" + name
			        + "' parameter in request.");
		}
		return value;
	}

	/**
	 * Retrieves the value of the specified request parameter. If it does not
	 * exist, returns the provided default value.
	 * 
	 * @param request
	 *            The current HttpServletRequest
	 * @param name
	 *            The parameter name
	 * @param defaultValue
	 *            the default parameter value
	 * @return the value of the specified request parameter. If it does not
	 *         exist, returns the provided default value
	 */
	protected static String getParameter(final HttpServletRequest request, final String name,
	        String defaultValue) {
		final String value = request.getParameter(name);
		return value != null ? value : defaultValue;
	}

	protected void checkUserAuthorized() throws JahiaForbiddenAccessException {
		JahiaUser user = JCRTemplate.getInstance().getSessionFactory().getCurrentUser();
        try {
            if (JahiaUserManagerService.isGuest(user)) {
                throw new JahiaUnauthorizedException(
                        "You need to euthenticate yourself to use this service");
            } else if (getRequiredPermission() == null || !JCRSessionFactory.getInstance().getCurrentUserSession().getRootNode().hasPermission(getRequiredPermission())) {
                throw new JahiaForbiddenAccessException(
                        "You have not enough permissions to use this service");
            }
        } catch (RepositoryException e) {
            logger.error("Cannot get permission",e);
            throw new JahiaForbiddenAccessException(
                    "You have not enough permissions to use this service");
        }
    }

	protected abstract String getRequiredPermission();

}
