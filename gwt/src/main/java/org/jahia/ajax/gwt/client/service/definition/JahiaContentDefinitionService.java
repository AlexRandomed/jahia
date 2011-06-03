/**
 * This file is part of Jahia, next-generation open source CMS:
 * Jahia's next-generation, open source CMS stems from a widely acknowledged vision
 * of enterprise application convergence - web, search, document, social and portal -
 * unified by the simplicity of web content management.
 *
 * For more information, please visit http://www.jahia.com.
 *
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
 * Commercial and Supported Versions of the program (dual licensing):
 * alternatively, commercial and supported versions of the program may be used
 * in accordance with the terms and conditions contained in a separate
 * written agreement between you and Jahia Solutions Group SA.
 *
 * If you are unsure which license is appropriate for your use,
 * please contact the sales department at sales@jahia.com.
 */

package org.jahia.ajax.gwt.client.service.definition;

import com.google.gwt.core.client.GWT;
import com.google.gwt.user.client.rpc.RemoteService;
import com.google.gwt.user.client.rpc.ServiceDefTarget;
import org.jahia.ajax.gwt.client.core.JahiaGWTParameters;
import org.jahia.ajax.gwt.client.data.definition.GWTJahiaNodeType;
import org.jahia.ajax.gwt.client.service.GWTJahiaServiceException;
import org.jahia.ajax.gwt.client.util.URL;

import java.util.List;
import java.util.Map;

/**
 * GWT remote service for retrieving JCR node type information.
 *
 * @author Thomas Draier
 *         Date: Aug 25, 2008
 *         Time: 6:20:26 PM
 */
public interface JahiaContentDefinitionService extends RemoteService {

    public static class App {
        private static JahiaContentDefinitionServiceAsync app = null;

        public static synchronized JahiaContentDefinitionServiceAsync getInstance() {
            if (app == null) {
                String relativeServiceEntryPoint = createEntryPointUrl();
                String serviceEntryPoint = URL.getAbsoluteURL(relativeServiceEntryPoint);
                app = (JahiaContentDefinitionServiceAsync) GWT.create(JahiaContentDefinitionService.class);
                ((ServiceDefTarget) app).setServiceEntryPoint(serviceEntryPoint);

                JahiaGWTParameters.addUpdater(new JahiaGWTParameters.UrlUpdater() {
                    public void updateEntryPointUrl() {
                        String relativeServiceEntryPoint = createEntryPointUrl();
                        String serviceEntryPoint = URL.getAbsoluteURL(relativeServiceEntryPoint);
                        ((ServiceDefTarget) app).setServiceEntryPoint(serviceEntryPoint);
                    }
                });

            }
            return app;
        }

        private static String createEntryPointUrl() {
            return JahiaGWTParameters.getServiceEntryPoint() + "contentDefinition.gwt?lang=" +
                    JahiaGWTParameters.getLanguage() + "&site=" + JahiaGWTParameters.getSiteUUID() + "&workspace=" +
                    JahiaGWTParameters.getWorkspace();
        }
    }

    GWTJahiaNodeType getNodeType(String names) throws GWTJahiaServiceException;

    List<GWTJahiaNodeType> getNodeTypes(List<String> names) throws GWTJahiaServiceException;

    /**
     * Returns a list of node types with name and label populated that are the
     * sub-types of the specified base type.
     *
     *
     * @param baseType   the node type name to find sub-types
     * @param displayStudioElement
     * @return a list of node types with name and label populated that are the
     *         sub-types of the specified base type
     */
    Map<GWTJahiaNodeType, List<GWTJahiaNodeType>> getSubNodetypes(List<String> baseTypes, boolean includeSubTypes, boolean displayStudioElement) throws GWTJahiaServiceException;

    GWTJahiaNodeType getWFFormForNodeAndNodeType(String formResourceName)
            throws GWTJahiaServiceException;

}
