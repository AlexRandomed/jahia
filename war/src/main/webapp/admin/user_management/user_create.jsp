<%@ page language="java" %>
<%--

    This file is part of Jahia: An integrated WCM, DMS and Portal Solution
    Copyright (C) 2002-2010 Jahia Solutions Group SA. All rights reserved.

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

<%@ page import="java.util.*" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://struts.apache.org/tags-logic" prefix="logic" %>
<%@ taglib uri="http://www.jahia.org/tags/internalLib" prefix="internal" %>
<%@ taglib uri="http://www.jahia.org/tags/uiComponentsLib" prefix="ui" %>
<%@ taglib prefix="utility" uri="http://www.jahia.org/tags/utilityLib" %>
<%@ page import="java.util.Locale" %>
<%@ page import="org.jahia.data.JahiaData" %>
<%@ page import="org.jahia.utils.LanguageCodeConverters" %>
<%@ page import="org.jahia.params.ProcessingContext" %>
<%@ page import="org.jahia.bin.*,org.jahia.admin.users.*" %>
<%@page import="org.jahia.services.preferences.user.UserPreferencesHelper"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>

<utility:setBundle basename="JahiaInternalResources" useUILocale="true"/>
<c:set var="noneLabel"><fmt:message key="org.jahia.userMessage.none"/></c:set>
<jsp:useBean id="URL" class="java.lang.String" scope="request"/>
<% // http files path. %>
<jsp:useBean id="userMessage" class="java.lang.String" scope="session"/>

<%
    final Map userProperties = (Map) request.getAttribute("userProperties");
    JahiaData jData = (JahiaData) request.getAttribute("org.jahia.data.JahiaData");
    ProcessingContext jParams = ((JahiaData) request.getAttribute("org.jahia.data.JahiaData")).getProcessingContext();
    int stretcherToOpen = 0;
%>
<%!
    public String getUserProp(final Map userProps, final String propName) {
        final String propValue = (String) userProps.get(propName);
        if (propValue == null) {
            return "";
        } else {
            return propValue;
        }
    }
%>

<script language="javascript" type="text/javascript">
    function setFocus() {
        document.mainForm.username.focus();
    }

    function handleKeyCode(code) {
        if (code == 13) {
            document.mainForm.submit();
        }
    }
</script>

<div id="topTitle">
    <h1>Jahia</h1>

    <h2 class="edit"><fmt:message key="org.jahia.admin.users.ManageUsers.createNewUser.label"/></h2>
</div>
<div id="main">
<table style="width: 100%;" class="dex-TabPanel" cellpadding="0"
       cellspacing="0">
<tbody>
<tr>
    <td style="vertical-align: top;" align="left">
        <%@include file="/admin/include/tab_menu.inc" %>
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
        <fmt:message key="org.jahia.admin.users.ManageUsers.createNewUser.label"/>
    </div>
</div>
<div class="content-item">
<%
    if (userMessage.length() > 0) {
%>
<p class="errorbold">
    <%=userMessage%>
</p>
<% } %>
<logic:present name="engineMessages">
<logic:equal name="engineMessages" property="size" value="1">
        <logic:iterate name="engineMessages" property="messages" id="msg">
            <span class="errorbold"><internal:message name="msg"/></span>
        </logic:iterate>
</logic:equal>
<logic:notEqual name="engineMessages" property="size" value="1">
        <ul>
            <logic:iterate name="engineMessages" property="messages" id="msg">
                <li class="errorbold"><internal:message name="msg"/></li>
            </logic:iterate>
        </ul>
</logic:notEqual>
</logic:present>

<form name="mainForm" action='<%=JahiaAdministration.composeActionURL(request,response,"users","&sub=processCreate")%>'
      method="post" onkeydown="javascript:handleKeyCode(event.keyCode);">
<!-- Create new user -->
<input type="hidden" name="actionType" value="save"/>

<p>&nbsp;&nbsp;<fmt:message key="org.jahia.admin.users.ManageUsers.pleaseOk.label"/></p>

<p>&nbsp;&nbsp;<fmt:message key="org.jahia.admin.users.ManageUsers.noteThat.label"/>&nbsp;:</p>
<ul>
    <li><fmt:message key="org.jahia.admin.users.ManageUsers.userNameUniq.label"/></li>
    <li><fmt:message key="org.jahia.admin.users.ManageUsers.onlyCharacters.label"/></li>
    <li><fmt:message key="org.jahia.admin.users.ManageUsers.inputMaxCharacter.label"/></li>
</ul>

<table border="0" style="width:100%">
<tr>
    <td align="right">
        <fmt:message key="label.username"/>&nbsp;
    </td>
    <td>
        <input class="input" type="text" name="username"
               size="40" maxlength="40" value='<%=userProperties.get("username")%>'>
        &nbsp;<font class="text2">(<fmt:message key="org.jahia.admin.required.label"/>)</font>
    </td>
</tr>
<tr>
    <td align="right">
        <fmt:message key="org.jahia.admin.firstName.label"/>&nbsp;
    </td>
    <td>
        <input class="input" type="text" name='<%=ManageUsers.USER_PROPERTY_PREFIX+"j:firstName"%>'
               size="40" maxlength="255" value='<%=getUserProp(userProperties,"j:firstName")%>'>
    </td>
</tr>
<tr>
    <td align="right">
        <fmt:message key="org.jahia.admin.lastName.label"/>&nbsp;
    </td>
    <td>
        <input class="input" type="text" name='<%=ManageUsers.USER_PROPERTY_PREFIX+"j:lastName"%>'
               size="40" maxlength="255" value='<%=getUserProp(userProperties,"j:lastName")%>'>
    </td>
</tr>
<tr>
    <td align="right">
        <fmt:message key="label.email"/>&nbsp;
    </td>
    <td>
        <input class="input" type="text" name='<%=ManageUsers.USER_PROPERTY_PREFIX+"j:email"%>'
               size="40" maxlength="255" value='<%=getUserProp(userProperties,"j:email")%>'>
    </td>
</tr>
<tr>
    <td align="right" nowrap>
        <fmt:message key="org.jahia.admin.organization.label"/>&nbsp;
    </td>
    <td>
        <input class="input" type="text" name='<%=ManageUsers.USER_PROPERTY_PREFIX+"j:organization"%>'
               size="40" maxlength="255" value='<%=getUserProp(userProperties,"j:organization")%>'>
    </td>
</tr>
<tr>
    <td align="right">
        <label for="emailNotificationsDisabled"><fmt:message key="label.emailNotifications"/>&nbsp;</label>
    </td>
    <td>
        <%
            String propValue = getUserProp(userProperties, "emailNotificationsDisabled");
        %>
        <input type="checkbox" class="input" id="emailNotificationsDisabled"
               name='<%=ManageUsers.USER_PROPERTY_PREFIX + "emailNotificationsDisabled"%>' value="true"
               <% if ("true".equals(propValue)) {%>checked="checked"<% } %> />
    </td>
</tr>
<tr>
    <td align="right">
        <fmt:message key="org.jahia.admin.preferredLanguage.label"/>&nbsp;
    </td>
    <td>
        <%
            propValue = getUserProp(userProperties, "preferredLanguage");
            if (propValue == null || propValue.length() == 0) {
                propValue = UserPreferencesHelper.getPreferredLocale(null, jParams.getSite()).toString();
            }
        %>
        <select name='<%=ManageUsers.USER_PROPERTY_PREFIX + "preferredLanguage"%>'>
            <%
                for (Locale theLocale : LanguageCodeConverters.getAvailableBundleLocalesSorted(jParams.getUILocale())) {%>
            <option value="<%=theLocale %>"
                    <% if (theLocale.toString().equals(propValue)) { %>selected="selected"<% } %>><%= theLocale.getDisplayName(jParams.getUILocale()) %>
            </option>
            <% } %>
        </select>
    </td>
</tr>
<tr>
    <td align="right">
        <label for="accountLocked"><fmt:message key="label.accountLocked"/>&nbsp;</label>
    </td>
    <td>
        <%
            propValue = getUserProp(userProperties, "j:accountLocked");
        %>
        <input type="checkbox" class="input" id="accountLocked"
               name='<%=ManageUsers.USER_PROPERTY_PREFIX + "j:accountLocked"%>' value="true"
               <% if ("true".equals(propValue)) {%>checked="checked"<% } %> />
    </td>
</tr>
<%-- You can add your custom user properties here --%>
<tr>
    <td align="right">
        <fmt:message key="label.password"/>&nbsp;
    </td>
    <td>
        <input class="input" type="password" name="passwd"
               size="40" maxlength="255" value='<%=userProperties.get("passwd")%>'>
        &nbsp;<font class="text2">(<fmt:message key="org.jahia.admin.required.label"/>)</font>
    </td>
</tr>
<tr>
    <td align="right">
        <fmt:message key="label.comfirmPassword"/>&nbsp;
    </td>
    <td>
        <input class="input" type="password" name="passwdconfirm"
               size="40" maxlength="255" value='<%=userProperties.get("passwdconfirm")%>'>
        &nbsp;<font class="text2">(<fmt:message key="org.jahia.admin.required.label"/>)</font>
    </td>
</tr>
</table>
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
      	 <a class="ico-cancel"
              href='<%=JahiaAdministration.composeActionURL(request,response,"users","&sub=display")%>'><fmt:message key="label.cancel"/></a>
      </span>
     </span>
     <span class="dex-PushButton">
      <span class="first-child">
         <a class="ico-restore" href="javascript:document.mainForm.reset();"><fmt:message key="org.jahia.admin.resetChanges.label"/></a>
      </span>
     </span>
     <span class="dex-PushButton">
        <span class="first-child">
        <a class="ico-ok" href="javascript:document.mainForm.submit();"><fmt:message key="label.ok"/></a>
        </span>
      </span>

</div>


<script type="text/javascript" language="javascript">
    setFocus();
</script>

</div>
