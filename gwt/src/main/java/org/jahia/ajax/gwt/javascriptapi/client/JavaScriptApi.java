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

package org.jahia.ajax.gwt.javascriptapi.client;

import com.google.gwt.core.client.JavaScriptObject;
import com.google.gwt.user.client.Element;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.allen_sauer.gwt.log.client.Log;
import com.extjs.gxt.ui.client.widget.Info;
import org.jahia.ajax.gwt.commons.client.rpc.JahiaService;
import org.jahia.ajax.gwt.commons.client.beans.GWTPortletOutputBean;
import org.jahia.ajax.gwt.commons.client.beans.GWTInlineEditingResultBean;
import org.jahia.ajax.gwt.config.client.beans.GWTJahiaPageContext;
import org.jahia.ajax.gwt.config.client.util.URL;
import org.jahia.ajax.gwt.config.client.JahiaGWTParameters;

/**
 * Exposes GWT methods into the page scope to be used from native JavaScript functions.
 *
 * @author ktlili
 *         Date: 30 sept. 2008
 *         Time: 09:38:15
 */
public class JavaScriptApi {


    public JavaScriptApi() {
        initJavaScriptApi();
    }

    public static void request(String url, JavaScriptObject options) {
        AjaxRequest.perfom(url, options);
    }

    public static void onBlurEditableContent(final Element e, String containerID, String fieldID) {
        Log.info("Blurring field [" + containerID + "," + fieldID + "]: " + e.getInnerHTML() + " contentEditable" + e.getAttribute("contentEditable"));
        if ((e.getAttribute("contentEditable") != null) &&
            (e.getAttribute("contentEditable").equals("true"))) {
            String content = e.getInnerHTML();
            e.setAttribute("contentEditable", "false");
            e.setClassName("inlineEditingDeactivated");
            Integer containerIDInt = Integer.parseInt(containerID);
            Integer fieldIDInt = Integer.parseInt(fieldID);
            JahiaService.App.getInstance().inlineUpdateField(containerIDInt, fieldIDInt, content, new AsyncCallback<GWTInlineEditingResultBean>() {
                public void onSuccess(GWTInlineEditingResultBean result) {
                    if (result.isContentModified()) {
                        Info.display("Content updated", "Content saved.");
                        Log.info("Content successfully modified.");
                    }
                }

                public void onFailure(Throwable throwable) {
                    Log.error("Error modifying content", throwable);
                }
            });
        }
    }

    public static void onClickEditableContent(final Element e, String containerID, String fieldID) {
        Log.info("Checking if inline editing is allowed for containerID=" + containerID + " fieldID=" + fieldID);
        Integer containerIDInt = Integer.parseInt(containerID);
        Integer fieldIDInt = Integer.parseInt(fieldID);
        JahiaService.App.getInstance().isInlineEditingAllowed(containerIDInt, fieldIDInt, new AsyncCallback<Boolean>() {
            public void onSuccess(Boolean result) {
                if (result.booleanValue()) {
                    Log.info("Inline editing is allowed.");
                    e.setAttribute("contentEditable", "true");
                    e.setClassName("inlineEditingActivated");
                } else {
                    Log.info("Inline editing is not allowed.");
                }
            }

            public void onFailure(Throwable throwable) {
                Log.error("Error modifying content", throwable);
            }
        });
    }


    public static void renderPortlet(final Element e, String windowID, String entryPointInstanceID, String pathInfo, String queryString) {
        GWTJahiaPageContext page = new GWTJahiaPageContext(URL.getRelativeURL());
        page.setPid(JahiaGWTParameters.getPID());
        page.setMode(JahiaGWTParameters.getOperationMode());
        JahiaService.App.getInstance().drawPortletInstanceOutput(page, windowID, entryPointInstanceID, pathInfo, queryString, new AsyncCallback<GWTPortletOutputBean>() {
            public void onSuccess(GWTPortletOutputBean result) {
                e.setInnerHTML(result.getHtmlOutput());
                Log.info("Portlet successfully loaded.");
            }

            public void onFailure(Throwable throwable) {
                Log.error("Error loading portlet HTML", throwable);
            }
        });
    }


    public static void releaseLock(String lockType) {
            JahiaService.App.getInstance().releaseLock(lockType, new AsyncCallback<Boolean>() {
                public void onSuccess(Boolean result) {
                    Log.info("Lock released successfully.");
                }

                public void onFailure(Throwable throwable) {
                    Log.warn("Error releasing lock", throwable);
                }
            });
    }

    private native void initJavaScriptApi() /*-{
        $wnd.onBlurEditableContent = function (element, containerID, fieldID) {@org.jahia.ajax.gwt.javascriptapi.client.JavaScriptApi::onBlurEditableContent(Lcom/google/gwt/user/client/Element;Ljava/lang/String;Ljava/lang/String;)(element, containerID, fieldID); };
        $wnd.onClickEditableContent = function (element, containerID, fieldID) {@org.jahia.ajax.gwt.javascriptapi.client.JavaScriptApi::onClickEditableContent(Lcom/google/gwt/user/client/Element;Ljava/lang/String;Ljava/lang/String;)(element, containerID, fieldID); };
        $wnd.renderPortlet = function (element, windowID, entryPointInstanceID, pathInfo, queryString) {@org.jahia.ajax.gwt.javascriptapi.client.JavaScriptApi::renderPortlet(Lcom/google/gwt/user/client/Element;Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;)(windowID, entryPointInstanceID, pathInfo, queryString); };

        if (!$wnd.jahia) {
            $wnd.jahia = new Object();
        }
        $wnd.jahia.request = function (url, options) {@org.jahia.ajax.gwt.javascriptapi.client.JavaScriptApi::request(Ljava/lang/String;Lcom/google/gwt/core/client/JavaScriptObject;)(url, options); };
        $wnd.jahia.releaseLock = function (lockType) {@org.jahia.ajax.gwt.javascriptapi.client.JavaScriptApi::releaseLock(Ljava/lang/String;)(lockType); }; 
    }-*/;
}
