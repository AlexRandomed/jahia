<%--

    This file is part of Jahia: An integrated WCM, DMS and Portal Solution
    Copyright (C) 2002-2009 Jahia Solutions Group SA. All rights reserved.

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
    in Jahia's FLOSS exception. You should have received a copy of the text
    describing the FLOSS exception, and it is also available here:
    http://www.jahia.com/license

    Commercial and Supported Versions of the program
    Alternatively, commercial and supported versions of the program may be used
    in accordance with the terms contained in a separate written agreement
    between you and Jahia Solutions Group SA. If you are unsure which license is appropriate
    for your use, please contact the sales department at sales@jahia.com.

--%>
<%@include file="/admin/include/header.inc" %>
<%@page import="org.jahia.bin.JahiaAdministration" %>
<%@ page import="org.jahia.params.ProcessingContext" %>
<%@ page import="org.jahia.services.pages.ContentPage" %>
<%@ page import="java.text.DateFormat" %>
<%@ page import="java.text.SimpleDateFormat" %>
<%@ page import="java.io.File" %>
<%@ page import="java.util.*" %>
<%@ page import="org.jahia.data.templates.JahiaTemplatesPackage" %>
<%@ page import="org.jahia.utils.i18n.JahiaResourceBundle" %>
<%@ page import="org.jahia.data.JahiaData" %>
<%@ page import="org.jahia.services.content.JCRNodeWrapper" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="functions" uri="http://www.jahia.org/tags/functions" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<%
    List importsInfos = (List) session.getAttribute("importsInfos");
    Map importInfosMap = new HashMap();
    for (Iterator iterator = importsInfos.iterator(); iterator.hasNext();) {
        Map infos = (Map) iterator.next();
        File file = (File) infos.get("importFile");
        importInfosMap.put(file, infos);
    }
    List importsInfosSorted = (List) session.getAttribute("importsInfosSorted");
    List tpls = (List) request.getAttribute("tmplSets");
    ProcessingContext jParams = null;
    if (jData != null) {
        jParams = jData.params();
    }
    final boolean isInstall = session.getAttribute(JahiaAdministration.CLASS_NAME + "redirectToJahia") != null;
    final String readmefilePath = response.encodeURL(new StringBuffer().append(request.getContextPath()).append("/html/startup/readme.html").toString());
    stretcherToOpen   = 0; %>
<script type="text/javascript">
    <!--

function sendForm(){
    <%if(isInstall){%>
          var openrf = document.getElementById('openReadmeFile');
          if(openrf != null && openrf.checked){
              openReadmeFile();
          }
          <%}%>
          setWaitingCursor();
          document.main.submit();
      }


      function openReadmeFile() {
           var params = "width=1100,height=500,left=0,top=0,resizable=yes,scrollbars=yes,status=no";
           window.open('<%=readmefilePath%>', 'Readme', params);
      }

      function setWaitingCursor() {
          if (typeof workInProgressOverlay != 'undefined') {
             workInProgressOverlay.launch();
          }
      }

      -->
</script>
<div id="topTitle">
    <h1>Jahia</h1>
    <h2 class="edit">
        <fmt:message key="org.jahia.admin.site.ManageSites.manageVirtualSites.label"/>
    </h2>
</div>
<div id="main">
<table style="width: 100%;" class="dex-TabPanel" cellpadding="0" cellspacing="0">
<tbody>
<tr>
    <td style="vertical-align: top;" align="left">
        <%@include file="/admin/include/tab_menu.inc"%>
    </td>
</tr>
<tr>
<td style="vertical-align: top;" align="left" height="100%">
<div class="dex-TabPanelBottom">
<div class="tabContent">
<jsp:include page="/admin/include/left_menu.jsp">
    <jsp:param name="mode" value="server"/>
</jsp:include>

<div id="content" class="fit">
            <div class="head">
                <div class="object-title">
                    <fmt:message key="label.virtualSitesManagement.configwizard.variables"/>
                </div>
            </div>
            <table class="evenOddTable" border="0" cellpadding="5" cellspacing="0" width="100%">
                <thead>
                <tr>
                    <th<%if(importsInfos.size()==1){ %> style="display:none;"<%} %> width="5%">&nbsp;</th>
                    <th width="95%">
                        <fmt:message key="label.name"/>
                    </th>

                </tr>
                </thead>
                <form name="main">
                    <input type="hidden" name="do" value="sites"/>
                    <input type="hidden" name="sub" value="processimport"/>
                    <tbody>
                    <%
                        int lineCounter = 0;
                        for (Iterator iterator = importsInfosSorted.iterator(); iterator.hasNext();) {
                            File file = (File) iterator.next();
                            Map infos = (Map) importInfosMap.get(file);
                            String filename = (String) infos.get("importFileName");
                            String fileType = (String) infos.get("type");
                            String siteKey = file.getName();
                            String lineClass = "oddLine";
                            if (lineCounter % 2 == 0) {
                                lineClass = "evenLine";
                            }
                            lineCounter++;
                            if(isConfigWizard){%>


                    <%if(importsInfos.size()==1){ %>
                    style="display:none;"
                    <%} %>

                    <input type="hidden" name="<%=file.getName()%>selected" value="checked"
                        <% if (infos.get("selected")!=null) { %>
                           checked
                        <% } %>
                            >


                    <% if ("site".equals(fileType)) { %>
                    <td>
                        <table border="0" cellpadding="0" width="100%">
                            <tr>
                                <td>
                                    <fmt:message key="org.jahia.admin.site.ManageSites.siteTitle.label"/>*&nbsp;
                                </td>
                                <td>
                                    <input class="input" type="text" name="<%=siteKey+"siteTitle"%>" value="<%=infos.get("sitetitle")%>" size="<%=inputSize%>" maxlength="100">
                                </td>
                            </tr>
                            <tr>
                                <td>
                                    <fmt:message key="org.jahia.admin.site.ManageSites.siteServerName.label"/>*&nbsp;                                
                                </td>
                                <td>
                                    <input class="input" type="text" name="<%=siteKey+"siteServerName"%>" value="<%= infos.get("siteservername") %>" size="<%=inputSize%>" maxlength="200">
                                </td>
                            </tr>
                            <tr>
                                <td>
                                    <fmt:message key="org.jahia.admin.site.ManageSites.siteKey.label"/>*&nbsp;<% if (Boolean.TRUE.equals(infos.get("siteKeyExists")))  { %>
                                    <div class="error">
                                        <fmt:message key="org.jahia.admin.warningMsg.chooseAnotherSiteKey.label"/>
                                    </div><% } %>
                                </td>
                                <td>
                                    <input type="hidden" name="<%=siteKey+"oldSiteKey"%>" value="<%= infos.get("oldsitekey") %>">
                                    <input class="input" type="text" name="<%=siteKey+"siteKey"%>" value="<%= infos.get("sitekey") %>" size="<%=inputSize%>" maxlength="50">
                                </td>
                            </tr>
                            <tr>
                                <td>
                                    <% if (tpls != null){
                                        for (Iterator iterator1 = tpls.iterator(); iterator1.hasNext();) {
                                            JCRNodeWrapper pack = (JCRNodeWrapper) iterator1.next();
                                            if (pack.getName().equals(infos.get("templates"))) {

                                    %>
                                    <input type="hidden" name="<%=siteKey + "templates"%>" value="<%=pack.getName()%>">
                                    <%}
                                    %>
                                    <% }
                                    } %>
                                </td>
                            </tr>
                        </table>
                    </td>
                    <% } %>


                    <% } else { %>

                    <tr class="<%=lineClass%>">
                        <td<%if(importsInfos.size()==1){ %> style="display:none;"<%} %> align="center">
                            <input type="checkbox" name="<%=file.getName()%>selected" value="on"<% if (infos.get("selected")!=null) { %>checked<% } %>>
                        </td>
                        <td>
                            <% if ("site".equals(fileType)) { %>
                            <table border="0" cellpadding="0" width="100%">
                                <tr>
                                    <td>
                                        <fmt:message key="org.jahia.admin.site.ManageSites.siteTitle.label"/>*&nbsp;
                                    </td>
                                    <td>
                                        <input class="input" type="text" name="<%=siteKey+"siteTitle"%>" value="<%=infos.get("sitetitle")%>" size="<%=inputSize%>" maxlength="100">
                                    </td>
                                </tr>
                                <tr>
                                    <td>
                                        <fmt:message key="org.jahia.admin.site.ManageSites.siteServerName.label"/>*&nbsp;
                                    </td>
                                    <td>
                                        <input class="input" type="text" name="<%=siteKey+"siteServerName"%>" value="<%= infos.get("siteservername") %>" size="<%=inputSize%>" maxlength="200">
                                    </td>
                                </tr>
                                <tr>
                                    <td>
                                        <fmt:message key="org.jahia.admin.site.ManageSites.siteKey.label"/>*&nbsp;<% if (Boolean.TRUE.equals(infos.get("siteKeyExists")))  { %>
                                        <div class="error">
                                            <fmt:message key="org.jahia.admin.warningMsg.chooseAnotherSiteKey.label"/>
                                        </div><% } %>
                                    </td>
                                    <td>
                                        <input type="hidden" name="<%=siteKey+"oldSiteKey"%>" value="<%= infos.get("oldsitekey") %>"><input class="input" type="text" name="<%=siteKey+"siteKey"%>" value="<%= infos.get("sitekey") %>" size="<%=inputSize%>" maxlength="50">
                                    </td>
                                </tr>
                                <tr>

                                    <td>
                                        <fmt:message key="org.jahia.admin.site.ManageSites.pleaseChooseTemplateSet.label"/>&nbsp;
                                    </td>
                                    <td>
                                        <select name="<%=siteKey + "templates"%>">

                                            <% if (tpls != null)
                                                for (Iterator iterator1 = tpls.iterator(); iterator1.hasNext();) {
                                                    JCRNodeWrapper pack = (JCRNodeWrapper) iterator1.next();
                                            %>
                                            <option value="<%=pack.getName()%>"<% if (pack.getName().equals(infos.get("templates"))) { %>selected<% } %>><%=pack.getName() %></option>

                                            <%
                                                    } %>
                                        </select>
                                    </td>
                                </tr>
                            </table>
                            <% } else if ("files".equals(fileType))  { %>
                            <fmt:message key="org.jahia.admin.site.ManageSites.multipleimport.shared"/>: <%=filename %>
                            <% } else { %>
                            <fmt:message key='<%="org.jahia.admin.site.ManageSites.fileImport."+filename %>'/><% } %>
                        </td>
                    </tr>
                    <% } %>
                    <%} %>
                    </tbody>
                </form>
            </table>
        </div>
    </div>
</td>
</tr>
</tbody>
</table>
</div>
<div id="actionBar">
    <%
        if (session.getAttribute(JahiaAdministration.CLASS_NAME + "redirectToJahia") == null) { %>
          <span class="dex-PushButton">
            <span class="first-child">
              <a class="ico-back" href='<%=JahiaAdministration.composeActionURL(request,response,"displaymenu","")%>'><fmt:message key="label.backToMenu"/></a>
            </span>
          </span><%} %>
          <span class="dex-PushButton">
            <span class="first-child">
              <a class="ico-ok" href='javascript:sendForm();'><fmt:message key="label.doImport"/></a>
            </span>
          </span>
</div>

</div>
<%@include file="/admin/include/footer.inc" %>