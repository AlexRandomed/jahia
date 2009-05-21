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
//
//  JahiaApplicationsService
//  EV      29.11.2000
//
//  getAppOutput( fieldID, appID, params )
//

package org.jahia.services.applications;

import org.jahia.exceptions.JahiaException;
import org.jahia.params.ParamBean;
import org.jahia.services.JahiaService;

/**
 * This service generates the dispatching and aggregation on an application.
 * This functionality is central to the portal behavior of Jahia, allowing it
 * to display multiple applications on a web page and interact with them
 * simultaneously.
 *
 * @author Serge Huber
 * @author Eric Vassalli
 * @version 1.0
 */
public abstract class DispatchingService extends JahiaService {

    /**
     * Dispatches processing to an application, and retrieves it's output for
     * Jahia to aggregate
     *
     * @param fieldID     identifier of Jahia's field
     * @param entryPointIDStr application identifier passed as a String (converted
     *                    from an integer)
     * @param jParams     Jahia's ProcessingContext object, containing the standard request /
     *                    response pair, the servlet context, and additional information
     *
     * @throws JahiaException generated if there was a problem dispatching,
     *                        during processing of the application, or when recuperating the application's
     *                        output.
     * @return String containing the output of the application
     */
    public abstract String getAppOutput (int fieldID, String entryPointIDStr, ParamBean jParams)
            throws JahiaException;

} // end JahiaApplicationsService
