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
 * in Jahia's FLOSS exception. You should have received a copy of the text
 * describing the FLOSS exception, and it is also available here:
 * http://www.jahia.com/license
 * 
 * Commercial and Supported Versions of the program
 * Alternatively, commercial and supported versions of the program may be used
 * in accordance with the terms contained in a separate written agreement
 * between you and Jahia Limited. If you are unsure which license is appropriate
 * for your use, please contact the sales department at sales@jahia.com.
 */

package org.jahia.ajax.gwt.engines.pdisplay.client;

import com.extjs.gxt.ui.client.data.PagingLoadResult;
import com.google.gwt.core.client.GWT;
import com.google.gwt.user.client.rpc.RemoteService;
import com.google.gwt.user.client.rpc.ServiceDefTarget;
import org.jahia.ajax.gwt.commons.client.beans.GWTJahiaProcessJob;
import org.jahia.ajax.gwt.engines.pdisplay.client.bean.GWTJahiaProcessJobPreference;
import org.jahia.ajax.gwt.engines.pdisplay.client.bean.GWTJahiaProcessJobStat;
import org.jahia.ajax.gwt.config.client.JahiaGWTParameters;
import org.jahia.ajax.gwt.config.client.util.URL;
import org.jahia.ajax.gwt.commons.client.rpc.GWTJahiaServiceException;

/**
 * User: jahia
 * Date: 10 janv. 2008
 * Time: 11:31:13
 */
public interface ProcessDisplayService extends RemoteService {
    /**
     * Utility/Convinience class.
     * Use PdisplayServiceAsync.App.getInstance() to access static instance of MyServiceAsync
     */
    public static class App {
        private static ProcessDisplayServiceAsync ourInstance = null;


        public static synchronized ProcessDisplayServiceAsync getInstance() {
            if (ourInstance == null) {
                String relativeServiceEntryPoint = JahiaGWTParameters.getServiceEntryPoint() + "pdisplay/";
                String serviceEntryPoint = URL.getAbsolutleURL(relativeServiceEntryPoint);
                ourInstance = (ProcessDisplayServiceAsync) GWT.create(ProcessDisplayService.class);
                ((ServiceDefTarget) ourInstance).setServiceEntryPoint(serviceEntryPoint);
            }

            return ourInstance;
        }

    }

    public GWTJahiaProcessJobStat getGWTProcessJobStat(int mode);

    public void savePreferences(GWTJahiaProcessJobPreference gwtJahiaProcessJobPreferences);

    public GWTJahiaProcessJobPreference getPreferences() throws GWTJahiaServiceException ;

    public PagingLoadResult<GWTJahiaProcessJob> findGWTProcessJobs(int offset, String parameter, boolean isAscending) throws GWTJahiaServiceException ;

    public void deleteJob(GWTJahiaProcessJob gwtProcessJob);

    public int getJobListMaxSize();

}
