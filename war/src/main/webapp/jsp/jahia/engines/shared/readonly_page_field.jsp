<%--

    
    This file is part of Jahia: An integrated WCM, DMS and Portal Solution
    Copyright (C) 2002-2009 Jahia Limited. All rights reserved.
    
    This program is free software; you can redistribute it and/or
    modify it under the terms of the GNU General Public License
    as published by the Free Software Foundation; either version 2
    of the License, or (at your option) any later version.
    
    This program is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
    GNU General Public License for more details.
    
    You should have received a copy of the GNU General Public License
    along with this program; if not, write to the Free Software
    Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301, USA.
    
    As a special exception to the terms and conditions of version 2.0 of
    the GPL (or any later version), you may redistribute this Program in connection
    with Free/Libre and Open Source Software ("FLOSS") applications as described
    in Jahia's FLOSS exception. You should have recieved a copy of the text
    describing the FLOSS exception, and it is also available here:
    http://www.jahia.com/license
    
    Commercial and Supported Versions of the program
    Alternatively, commercial and supported versions of the program may be used
    in accordance with the terms contained in a separate written agreement
    between you and Jahia Limited. If you are unsure which license is appropriate
    for your use, please contact the sales department at sales@jahia.com.

--%>

<%@ page language="java" contentType="text/html;charset=UTF-8" %>
<%@ page import="org.apache.commons.lang.StringUtils" %>
<%@ page import="org.jahia.data.beans.JahiaBean" %>
<%@ page import="org.jahia.data.fields.JahiaField" %>
<%@ page import="org.jahia.engines.EngineLanguageHelper" %>
<%@ page import="org.jahia.engines.JahiaEngine" %>
<%@ page import="org.jahia.engines.shared.JahiaPageEngineTempBean" %>
<%@ page import="org.jahia.params.ProcessingContext" %>
<%@ page import="org.jahia.services.pages.ContentPage" %>
<%@ page import="org.jahia.services.pages.JahiaPageBaseService" %>
<%@ page import="org.jahia.services.pages.JahiaPageDefinition" %>
<%@ page import="org.jahia.utils.JahiaTools"%>
<%@ page import="java.util.*"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://www.jahia.org/tags/internalLib" prefix="internal" %>
<%!
    /**
     * utility method
     * @param s the long string
     * @return a List of string
     */
    private List getStringToList(final String s) {
        final List vlist = new ArrayList();
        final StringTokenizer tok = new StringTokenizer(s, ",");
        while (tok.hasMoreTokens()) {
            final String v = tok.nextToken().trim();
            vlist.add(v);
        }
        return vlist;
    }
%>
<%
    // get engine context and all data we need from it...
    final Map engineMap = (Map) request.getAttribute("org.jahia.engines.EngineHashMap");
    final ProcessingContext jParams = (ProcessingContext) request.getAttribute("org.jahia.params.ParamBean");
    pageContext.setAttribute("jahia", new JahiaBean(jParams));
    EngineLanguageHelper elh = (EngineLanguageHelper) engineMap.get(JahiaEngine.ENGINE_LANGUAGE_HELPER);
    if (elh != null) {
        jParams.setCurrentLocale(elh.getCurrentLocale());
    }
    final String fieldsEditCallingEngineName = (String) engineMap.get("fieldsEditCallingEngineName");
    final JahiaField theField = (JahiaField) engineMap.get(fieldsEditCallingEngineName + "." + "theField");
    Map pageBeans = (Map) session.getAttribute("Page_Field.PageBeans");
    if (pageBeans == null) {
        pageBeans = new HashMap();
    }
    final JahiaPageEngineTempBean pageBean = (JahiaPageEngineTempBean) pageBeans.get(theField.getDefinition().getName());
    final Locale processingLocale = (Locale) engineMap.get(JahiaEngine.PROCESSING_LOCALE);
    final JahiaPageBaseService jahiaPageBaseService = JahiaPageBaseService.getInstance();
    String pageTitle = "";
    if (pageBean != null)
        pageTitle = pageBean.getTitle(processingLocale.toString());

    if ("".equals(pageTitle))
        pageTitle = "N/A";

    final boolean isNewPage = theField.getObject() == null;

    boolean isLinkOnly = false;//default
    // added to support specific behaviors of engine addPage actions
    boolean isExternalLink = true; //default
    boolean isInternalLink = true; //default
    List tVlist = new ArrayList();// List of templates constraint requested
    final Boolean templateNotFound = (Boolean) engineMap.get("templateNotFound");

    final ContentPage contentPage = jahiaPageBaseService.lookupContentPage(theField.getPageID(), false);
    if (contentPage != null) {
        String defaultValue = theField.getDefinition().
                getDefaultValue();

        // field type definition always prevails over the current value.
        // added to support new feature: check of defaultvalue in declarations
        // syntax accepted: move, internal or external, pageonly and linkonly. the 2 last are exclusive
        // the webdesigner can also limit the template choice by a list of templates names surrounded by brackets

        if (defaultValue != null
                && !defaultValue.trim().equalsIgnoreCase("")
                && defaultValue.trim().length() > 1) {

            //check for presence of constraint templates


            if (defaultValue.indexOf("[") != -1) {
                int p1 = defaultValue.indexOf("[");
                int p2 = defaultValue.lastIndexOf("]");
                String tList = defaultValue.substring(p1, p2 + 1);
                defaultValue = StringUtils.replace(defaultValue, tList, "");

                tList = tList.substring(1, tList.length() - 1);
                tVlist = getStringToList(tList);
            }

            String value;

            int end = defaultValue.length();

            //remove <> if needed
            if (defaultValue.substring(0, 1).equalsIgnoreCase("<")
                    && defaultValue.substring(end - 1, end).equalsIgnoreCase(">")) {
                value = defaultValue.substring(1, defaultValue.length() - 1);
            } else {
                value = defaultValue;
            }

            List values = getStringToList(value);
            //StringTokenizer tok = new StringTokenizer(value, ",");
            for (int i = 0; i < values.size(); i++) {
                //while (tok.hasMoreTokens()) {
                //String v = tok.nextToken().trim();
                String v = (String) values.get(i);
                if (v.toLowerCase().indexOf("pageonly") != -1) {
                    isExternalLink = false;
                    isInternalLink = false;
                    isLinkOnly = false;
                    break;

                } else if (v.toLowerCase().indexOf("external") != -1) {

                    isExternalLink = true;
                    isInternalLink = false;
                } else if (v.toLowerCase().indexOf("internal") != -1) {

                    isInternalLink = true;
                    isExternalLink = false;
                } else if (v.toLowerCase().indexOf("linkonly") != -1) {
                    isLinkOnly = true;
                    isInternalLink = true;
                    isExternalLink = true;
                    break;
                }
            }
        }
    }

    final int sourcePageID;
    if (pageBean == null) {
        sourcePageID = -1;
    } else {
       sourcePageID = pageBean.getPageLinkID();
    }
    String sourceTitle = null;
    if (sourcePageID != -1) {
        final ContentPage sourcePage = jahiaPageBaseService.lookupContentPage(sourcePageID, false);
        Map titles = sourcePage.getTitles(ContentPage.LAST_UPDATED_TITLES);
        if (titles != null)
            sourceTitle = (String) titles.get(processingLocale.toString());
    }
    final String remoteURL;
    if (pageBean == null) {
        remoteURL = null;
    } else {
        remoteURL = pageBean.getRemoteURL(processingLocale.toString());
    }

%>


<h4 class="page_type_big">
    <% if (isNewPage) { %>
    <% if (isLinkOnly) { %>
    <internal:engineResourceBundle resourceName="org.jahia.engines.shared.Page_Field.jahiaLinkCreation.label"/>
    <% } else { %>
    <internal:engineResourceBundle resourceName="org.jahia.engines.shared.Page_Field.jahiaPageCreation.label"/>
    <% } %>
    <% } else { %>
    <% if (isLinkOnly) { %>
    <internal:engineResourceBundle resourceName="org.jahia.engines.shared.Page_Field.jahiaLinkEdition.label"/>
    <% } else { %>
    <internal:engineResourceBundle resourceName="org.jahia.engines.shared.Page_Field.jahiaPageEdition.label"/>
    <% } %>
    <% } %>
</h4>

<table width="100%">
    <tr>
        <td width="50%">
            <% if (pageBean == null) { %>
            <internal:engineResourceBundle resourceName="org.jahia.engines.shared.Page_Field.noTempPageBean.label"/>.
            <% } %>
            <% if (isLinkOnly) { %>
            <internal:engineResourceBundle resourceName="org.jahia.engines.shared.Page_Field.jahiaLinkTitle.label"/>
            <% } else { %>
            <internal:engineResourceBundle resourceName="org.jahia.engines.shared.Page_Field.jahiaPageTitle.label"/>
            <% } %>
            :
        </td>
        <td><%=pageTitle%></td>
    </tr>
    <% if (! isLinkOnly && (sourcePageID == -1) && (remoteURL == null || remoteURL.length() <= 7) ) { %>
    <tr>
        <td width="50%">
            <% if (isNewPage) { %>
            <internal:engineResourceBundle resourceName="org.jahia.engines.shared.Page_Field.createPageTemplate.label"/>
            <% } else { %>
            <internal:engineResourceBundle resourceName="org.jahia.engines.shared.Page_Field.changePageTemplate.label"/>
            <% } %>
        </td>
        <td>
            <%
                final Iterator templateList;

                if (tVlist.size() > 0) {
                    Iterator tlist = (Iterator) engineMap.get("templateList");
                    List all = new ArrayList();
                    List constraint = new ArrayList();
                    int checkcount = tVlist.size();
                    while (tlist.hasNext()) {
                        JahiaPageDefinition t = (JahiaPageDefinition) tlist.next();
                        all.add(t);
                        String templatename = t.getName();
                        if (tVlist.contains(templatename)) {
                            checkcount --;
                            constraint.add(t);
                        }
                    }
                    if (checkcount == 0) {
                        templateList = constraint.iterator();
                    } else if (checkcount > 0 && constraint.size() > 0) {
                        templateList = constraint.iterator();
                    } else {
                        templateList = all.iterator();
                    }
                } else {
                    templateList = (Iterator) engineMap.get("templateList");
                }
            %>
            <% if (templateList != null) { %>
            <% while (templateList.hasNext()) {
                JahiaPageDefinition theTemplate = (JahiaPageDefinition) templateList.next();
                if (theTemplate.getID() == pageBean.getPageTemplateID()) {
                pageContext.setAttribute("displayName", theTemplate.getDisplayName()); 
                %>
            <c:out value="${jahia.i18n[displayName]}"/>
            <% break; } %>
            <% } %>
            <% } %>
            <% if (templateNotFound == null || templateNotFound.booleanValue()) { %>
            Template not found !!! Deleted ?
            <% } %>
        </td>
    </tr>
    <% } %>
    <% if (isInternalLink && sourcePageID != -1) { %>
    <tr>
        <td width="50%">
            <internal:engineResourceBundle
                    resourceName="org.jahia.engines.shared.Page_Field.linkExistingPage.label"/>
            :
        </td>
        <td>
            <%=sourceTitle%>&nbsp;(<internal:engineResourceBundle
                resourceName="org.jahia.pageId.label"/>:&nbsp;<%=sourcePageID%>)

        </td>
    </tr>
    <% } %>
    <% if (isExternalLink || isLinkOnly) { %>
    <% if (remoteURL != null && remoteURL.length() > 7) { %>
    <tr>
        <td width="50%">
            <internal:engineResourceBundle
                    resourceName="org.jahia.engines.shared.Page_Field.createLinkToExternalSite.label"/>
            :
        </td>
        <td>
            <%=remoteURL%>
        </td>
    </tr>
    <% } %>
    <% } %>
</table>



