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

<%@page language = "java" %>
<%@page import = "java.util.*" %>
<%@page import="org.jahia.bin.*" %>
<%@page import = "org.jahia.data.JahiaData" %>
<%@taglib uri="http://www.jahia.org/tags/internalLib" prefix="internal" %>
<jsp:useBean id="URL" class="java.lang.String" scope="request"/><jsp:useBean id="groupMessage" class="java.lang.String" scope="session"/><%
String groupName = (String)session.getAttribute("selectedGroup");
String newGroup = (String)request.getAttribute("newGroup");
JahiaData jData = (JahiaData)request.getAttribute("org.jahia.data.JahiaData");
int stretcherToOpen   = 1; %>
<script language="javascript">
  function setFocus(){
      document.mainForm.newGroup.focus();
  }
</script>
<div id="topTitle">
  <h1>Jahia</h1>
  <h2 class="edit"><internal:adminResourceBundle resourceName="org.jahia.admin.users.ManageGroups.copyGroup.label"/></h2>
</div>
<div id="main">
  <table style="width: 100%;" class="dex-TabPanel" cellpadding="0" cellspacing="0">
    <tbody>
      <tr>
        <td style="vertical-align: top;" align="left">
          <%@include file="/jsp/jahia/administration/include/tab_menu.inc" %>
        </td>
      </tr>
      <tr>
        <td style="vertical-align: top;" align="left" height="100%">
          <div class="dex-TabPanelBottom">
            <div class="tabContent">
              <%@include file="/jsp/jahia/administration/include/menu_server.inc" %>
              <div id="content" class="fit">
                  <div class="head">
                      <div class="object-title">
                          <internal:adminResourceBundle
                                  resourceName="org.jahia.admin.users.ManageGroups.copySelectedGroup.label"/>
                      </div>
                  </div>
                  <div class="content-item">
                  <%
                if ( groupMessage.length()>0 ){ %>
                <p class="error">
                  <%=groupMessage %>
                </p>
                <% } %>
                <form name="mainForm" action='<%=JahiaAdministration.composeActionURL(request,response,"groups","&sub=processCopy")%>' method="post">
                  <!-- Copy group -->
                  <p>
                    <internal:adminResourceBundle resourceName="org.jahia.admin.users.ManageGroups.createCopyGroup.label"/>&nbsp;: <b><%= groupName %></b>
                    <br>
                    <br>
                    <input type="hidden" name="sourceGroupName" value="<%= groupName%>"><internal:adminResourceBundle resourceName="org.jahia.admin.users.ManageGroups.pleaseCopyGroup.label"/>
                    <br>
                  </p>
                  <table border="0" style="width:100%">
                    <tr>
                      <td align="right">
                        <internal:adminResourceBundle resourceName="org.jahia.admin.users.ManageGroups.groupName.label"/>&nbsp;
                      </td>
                      <td>
                        <input type="text" name="newGroup" class="input" size="40" maxlength="255" value="<%= newGroup%>">&nbsp;
                        <font class="text2">
                          (<internal:adminResourceBundle resourceName="org.jahia.admin.required.label"/>)
                        </font>
                      </td>
                    </tr>
                  </table>
                  <br/>
                </form>
              </div>
            </div>
            </td>
          </tr>
          </tbody>
        </table>
        </div>
        <div id="actionBar">
          <span class="dex-PushButton">
            <span class="first-child">
              <a class="ico-cancel" href="<%=JahiaAdministration.composeActionURL(request,response,"groups","&sub=display")%>
" ><internal:adminResourceBundle resourceName="org.jahia.admin.cancel.label"/></a>
            </span>
          </span>
          <span class="dex-PushButton">
            <span class="first-child">
              <a class="ico-ok" href="javascript:document.mainForm.submit();"><internal:adminResourceBundle resourceName="org.jahia.admin.ok.label"/></a>
            </span>
          </span>
        </div>
        <script language="javascript">
          setFocus();
        </script>
      </div>
