<%@ tag body-content="empty" description="Renders the search URL built from current search request parameters."%>
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

<%@ attribute name="exclude" required="false" type="java.lang.String"
              description="Comma-separated list of parameter names to exclude from the final search URL. None is excluded by default."
%><%@ attribute name="url" required="false" type="org.jahia.services.render.URLGenerator"
              description="Current URL Generator if available"
%><%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"
%><%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" 
%><c:set var="exclude" value=",${fn:replace(exclude, ' ', '')},"/>
<c:set var="urlBase" value="${pageContext.request.requestURI}"/>
<c:set var="urlContext" value="/"/>
<c:if test="${not empty url}">
    <c:set var="urlBase" value="${url.mainResource}"/>
</c:if>
<c:url value="${urlBase}" context="${urlContext}">
<c:forEach var="aParam" items="${paramValues}">
	<c:set var="paramToCheck" value=",${aParam.key},"/>
	<c:if test="${fn:startsWith(aParam.key, 'src_') && !fn:contains(exclude, paramToCheck)}">
		<c:forEach var="aValue" items="${aParam.value}">
			<c:param name="${aParam.key}" value="${aValue}"/>
		</c:forEach>
	</c:if>
</c:forEach>
</c:url>