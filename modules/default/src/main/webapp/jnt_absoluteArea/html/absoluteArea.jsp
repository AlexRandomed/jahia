<%@ page language="java" contentType="text/html;charset=UTF-8" %>
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

<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="template" uri="http://www.jahia.org/tags/templateLib" %>
<%@ taglib prefix="jcr" uri="http://www.jahia.org/tags/jcr" %>
<%--@elvariable id="currentNode" type="org.jahia.services.content.JCRNodeWrapper"--%>
<%--@elvariable id="out" type="java.io.PrintWriter"--%>
<%--@elvariable id="script" type="org.jahia.services.render.scripting.Script"--%>
<%--@elvariable id="scriptInfo" type="java.lang.String"--%>
<%--@elvariable id="workspace" type="java.lang.String"--%>
<%--@elvariable id="renderContext" type="org.jahia.services.render.RenderContext"--%>
<%--@elvariable id="currentResource" type="org.jahia.services.render.Resource"--%>
<%--@elvariable id="url" type="org.jahia.services.render.URLGenerator"--%>
<c:set var="level" value="${currentNode.properties['j:level'].long}" />
<c:choose>
    <c:when test="${not empty inWrapper and inWrapper eq false}">
        <div class="absoluteArea<c:if test="${not empty currentNode.properties['j:mockupStyle']}"> ${currentNode.properties['j:mockupStyle'].string}</c:if>">
            <div class="absoluteAreaTemplate">
                <c:if test="${empty level}" >
                    <span>Absolute Area : ${currentNode.resolveSite.home.name}</span>
                </c:if>
                <c:if test="${not empty level}" >
                    <span>Absolute Area : ${currentNode.name} - Level ${level}</span>
                </c:if>
            </div>
        </div>
    </c:when>
    <c:otherwise>
        <jcr:nodeProperty node="${currentNode}" name="j:allowedTypes" var="restrictions"/>
        <c:if test="${not empty restrictions}">
            <c:forEach items="${restrictions}" var="value">
                <c:if test="${not empty nodeTypes}">
                    <c:set var="nodeTypes" value="${nodeTypes} ${value.string}"/>
                </c:if>
                <c:if test="${empty nodeTypes}">
                    <c:set var="nodeTypes" value="${value.string}"/>
                </c:if>
            </c:forEach>
        </c:if>
        <c:set var="listLimit" value="${currentNode.properties['j:numberOfItems'].long}"/>
        <c:if test="${empty listLimit}">
            <c:set var="listLimit" value="${-1}"/>
        </c:if>

        <c:if test="${empty level}" >
            <template:area view="${currentNode.properties['j:referenceView'].string}"
                           path="${currentNode.name}"
                           nodeTypes="${nodeTypes}" listLimit="${listLimit}" moduleType="absoluteArea">
                <c:if test="${not empty currentNode.properties['j:subNodesView'].string}">
                    <template:param name="subNodesView"
                                    value="${currentNode.properties['j:subNodesView'].string}"/>
                </c:if>
                <c:if test="${not empty currentNode.properties['j:mockupStyle'].string}">
                    <template:param name="mockupStyle" value="${currentNode.properties['j:mockupStyle'].string}"/>
                </c:if>
            </template:area>
        </c:if>
        <c:if test="${not empty level}" >
            <template:area level="${level}" view="${currentNode.properties['j:referenceView'].string}"
                           path="${currentNode.name}"
                           nodeTypes="${nodeTypes}" listLimit="${listLimit}" moduleType="absoluteArea">
                <c:if test="${not empty currentNode.properties['j:subNodesView'].string}">
                    <template:param name="subNodesView"
                                    value="${currentNode.properties['j:subNodesView'].string}"/>
                </c:if>
                <c:if test="${not empty currentNode.properties['j:mockupStyle'].string}">
                    <template:param name="mockupStyle" value="${currentNode.properties['j:mockupStyle'].string}"/>
                </c:if>
            </template:area>
        </c:if>
    </c:otherwise>
</c:choose>

