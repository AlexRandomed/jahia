<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%--

    This file is part of Jahia: An integrated WCM, DMS and Portal Solution
    Copyright (C) 2002-2011 Jahia Solutions Group SA. All rights reserved.

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

<%@ taglib prefix="jcr" uri="http://www.jahia.org/tags/jcr" %>
<%@ taglib prefix="functions" uri="http://www.jahia.org/tags/functions" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib prefix="template" uri="http://www.jahia.org/tags/templateLib" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn" %>
<%--@elvariable id="currentNode" type="org.jahia.services.content.JCRNodeWrapper"--%>
<%--@elvariable id="out" type="java.io.PrintWriter"--%>
<%--@elvariable id="script" type="org.jahia.services.render.scripting.Script"--%>
<%--@elvariable id="scriptInfo" type="java.lang.String"--%>
<%--@elvariable id="workspace" type="java.lang.String"--%>
<%--@elvariable id="renderContext" type="org.jahia.services.render.RenderContext"--%>
<%--@elvariable id="currentResource" type="org.jahia.services.render.Resource"--%>
<%--@elvariable id="url" type="org.jahia.services.render.URLGenerator"--%>
<c:set var="pageNodes" value="${jcr:getParentsOfType(currentNode, 'jnt:page')}"/>
<jcr:nodeProperty node="${currentNode}" name="displayHome" var="displayHome"/>
<jcr:nodeProperty node="${currentNode}" name="displayCurrentPage" var="displayCurrentPage"/>
<jcr:nodeProperty node="${currentNode}" name="displayLinkOnCurrentPage" var="displayLinkOnCurrentPage"/>
<jcr:nodeProperty node="${currentNode}" name="displayOnFirstLevel" var="displayOnFirstLevel"/>
<c:if test="${empty pageNodes}">
    <c:choose>
        <c:when test="${jcr:isNodeType(renderContext.mainResource.node, 'jnt:page')}">
            <c:set var="pageNodes" value="${jcr:getMeAndParentsOfType(renderContext.mainResource.node,'jnt:page')}"/>
        </c:when>
        <c:otherwise>
            <c:set var="pageNodes" value="${jcr:getParentsOfType(renderContext.mainResource.node, 'jnt:page')}"/>
        </c:otherwise>
    </c:choose>
</c:if>
<c:if test="${displayOnFirstLevel.boolean || fn:length(pageNodes) > 1}">
    <ul class="breadcrumb">
        <c:forEach items="${functions:reverse(pageNodes)}"
                   var="pageNode" varStatus="status">
            <c:set var="displayPage" value="true"/>
            <c:choose>
                <c:when test="${status.first && !displayHome.boolean}">
                    <c:set var="displayPage" value="false"/>
                </c:when>
                <c:when
                        test="${status.last && !displayCurrentPage.boolean}">
                    <c:set var="displayPage" value="false"/>
                </c:when>
            </c:choose>
            <c:if test="${displayPage}">
                <li>
                    <c:if
                            test="${renderContext.mainResource.node.path ne pageNode.path || displayLinkOnCurrentPage.boolean}">
                    <a href="<c:url value='${url.base}${pageNode.path}.html'/>">
                        </c:if> ${pageNode.properties['jcr:title'].string} <c:if
                            test="${renderContext.mainResource.node.path ne pageNode.path || displayLinkOnCurrentPage.boolean}">
                    </a>
                    </c:if></li>
            </c:if>
        </c:forEach>
        <c:if test="${not jcr:isNodeType(renderContext.mainResource.node, 'jnt:page')}">
            <c:set var="pageNode" value="${renderContext.mainResource.node}"/>
            <li>
                <c:if test="${displayLinkOnCurrentPage.boolean}">
                <a href="${url.base}${pageNode.path}.html">
                    </c:if> ${functions:abbreviate(renderContext.mainResource.node.displayableName,15,30,'...')} <c:if test="${displayLinkOnCurrentPage.boolean}">
                </a>
                </c:if></li>
        </c:if>
    </ul>
</c:if>