<%@ tag body-content="scriptless" description="Renders the HTML form element to wrap up the login controls." %>
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

<%@ tag dynamic-attributes="attributes"%>
<%@ attribute name="doRedirect" required="false" type="java.lang.Boolean"
              description="Do we need to perform a client-side redirect after the login? Setting it to true, will will prevent browser built-in warning message if the page needs to be reloaded (after POST method). [true]"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@ taglib prefix="functions" uri="http://www.jahia.org/tags/functions"%>
<%@tag import="org.jahia.params.valves.LoginEngineAuthValveImpl"%>
<c:set var="org.jahia.tags.login.form.class" value="<%= this.getClass() %>" scope="request"/>
<c:if test="${!currentRequest.logged}">
  <c:set var="formId" value="<%= this.toString() %>"/>
  <c:set target="${attributes}" property="action" value="${functions:default(attributes.action, jahia.page.url)}"/>
  <c:set target="${attributes}" property="name" value="${functions:default(attributes.name, 'loginForm')}"/>
  <c:set target="${attributes}" property="method" value="${functions:default(attributes.method, 'post')}"/>
  <form ${functions:attributes(attributes)}>
      <c:choose>
          <c:when test="${not empty requestScope['javax.servlet.error.request_uri']}">
              <input type="hidden" name="redirect" value="${requestScope['javax.servlet.error.request_uri']}"/>
          </c:when>
          <c:otherwise>
              <input type="hidden" name="redirect" value="${url.base}${renderContext.mainResource.node.path}.html"/>
          </c:otherwise>
      </c:choose>
    <input type="hidden" name="<%=LoginEngineAuthValveImpl.LOGIN_TAG_PARAMETER%>" value="true"/>
    <c:if test="${doRedirect}">
        <input type="hidden" name="<%=LoginEngineAuthValveImpl.DO_REDIRECT%>" value="true"/>
    </c:if>
    <jsp:doBody/>
  </form>
</c:if>