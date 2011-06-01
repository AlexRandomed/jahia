<%@ page import="org.jahia.services.content.JCRNodeWrapper" %>
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

<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<%@ taglib prefix="jcr" uri="http://www.jahia.org/tags/jcr" %>
<%@ taglib prefix="template" uri="http://www.jahia.org/tags/templateLib" %>
<%--@elvariable id="currentNode" type="org.jahia.services.content.JCRNodeWrapper"--%>
<%--@elvariable id="propertyDefinition" type="org.jahia.services.content.nodetypes.ExtendedPropertyDefinition"--%>
<%--@elvariable id="type" type="org.jahia.services.content.nodetypes.ExtendedNodeType"--%>
<%--@elvariable id="out" type="java.io.PrintWriter"--%>
<%--@elvariable id="script" type="org.jahia.services.render.scripting.Script"--%>
<%--@elvariable id="scriptInfo" type="java.lang.String"--%>
<%--@elvariable id="workspace" type="java.lang.String"--%>
<%--@elvariable id="renderContext" type="org.jahia.services.render.RenderContext"--%>
<%--@elvariable id="currentResource" type="org.jahia.services.render.Resource"--%>
<%--@elvariable id="url" type="org.jahia.services.render.URLGenerator"--%>

<% long t = System.currentTimeMillis(); %>

<jcr:nodeProperty name="jcr:title" node="${currentNode}" var="title"/>
<jcr:nodeProperty name="j:baselineNode" node="${currentNode}" var="baseline"/>
<jcr:nodeProperty name="j:maxDepth" node="${currentNode}" var="maxDepth"/>
<jcr:nodeProperty name="j:startLevel" node="${currentNode}" var="startLevel"/>
<jcr:nodeProperty name="j:styleName" node="${currentNode}" var="styleName"/>
<jcr:nodeProperty name="j:layoutID" node="${currentNode}" var="layoutID"/>
<jcr:nodeProperty name="j:menuItemView" node="${currentNode}" var="menuItemTemplate"/>
<c:if test="${not empty menuItemTemplate}">
    <c:set var="menuItemTemplate" value="${menuItemTemplate.string}" />
</c:if>
<c:if test="${empty menuItemTemplate}">
    <c:set var="menuItemTemplate" value="menuElement" />
</c:if>

<c:if test="${empty currentResource.moduleParams.base}">
    <c:if test="${empty baseline or baseline.string eq 'home'}">
        <c:set var="current" value="${currentNode.resolveSite.home}"/>
    </c:if>
    <c:if test="${baseline.string eq 'currentPage'}">
        <c:set var="current" value="${jcr:getMeAndParentsOfType(renderContext.mainResource.node, 'jnt:page')[0]}" />
    </c:if>
</c:if>

<%--<c:set var="current" value="${baseline.node}"/>--%>
<c:if test="${not empty currentResource.moduleParams.base}">
    <jcr:node var="current" uuid="${currentResource.moduleParams.base}" />
</c:if>

<c:set var="startLevelValue" value="0"/>
<c:if test="${not empty startLevel}">
    <c:set var="startLevelValue" value="${startLevel.long}"/>
</c:if>

<c:set var="navMenuLevel" value="${empty currentResource.moduleParams.navMenuLevel ? 1 : currentResource.moduleParams.navMenuLevel}"/>
<c:if test="${navMenuLevel == 1}">
    <div class="${styleName.string}">
    <c:if test="${not empty title.string}">
        <span><c:out value="${fn:escapeXml(title.string)}"/></span>
    </c:if>
    <c:if test="${!empty layoutID}">
        <div id="${layoutID.string}">
    </c:if>
</c:if>
<c:if test="${not empty current}">
    <template:addCacheDependency node="${current}"/>
    <c:set var="items" value="${jcr:getChildrenOfType(current,'jmix:navMenuItem')}"/>
    <c:if test="${navMenuLevel eq 1}">
        <div class="navbar">
    </c:if>
    <c:if test="${navMenuLevel > 1}">
        <div class="box-inner">
    </c:if>
    <ul class="navmenu level_${navMenuLevel - startLevelValue}">
        <c:forEach items="${items}" var="menuItem" varStatus="menuStatus">
            <c:set var="inpath" value="${fn:startsWith(renderContext.mainResource.node.path, menuItem.path)}"/>
            <c:set var="selected" value="${renderContext.mainResource.node.path eq menuItem.path}"/>
            <c:set var="correctType" value="true"/>
            <c:if test="${!empty menuItem.properties['j:displayInMenu']}">
                <c:set var="correctType" value="false"/>
                <c:forEach items="${menuItem.properties['j:displayInMenu']}" var="display">
                    <c:if test="${display.node eq currentNode}">
                        <c:set var="correctType" value="${display.node eq currentNode}"/>
                    </c:if>
                </c:forEach>
            </c:if>
            <c:if test="${(startLevelValue < navMenuLevel or inpath) and correctType}">
                <c:set var="notempty" value="true"/>
                <c:set var="hasChildren" value="${navMenuLevel < maxDepth.long && jcr:hasChildrenOfType(menuItem,'jnt:page,jnt:nodeLink,jnt:externalLink')}"/>

                <c:choose>
                    <c:when test="${startLevelValue < navMenuLevel}">
                        <c:set var="listItemCssClass"
                               value="${hasChildren ? 'hasChildren' : 'noChildren'}${inpath ? ' inPath' : ''}${selected ? ' selected' : ''}${menuStatus.first ? ' firstInLevel' : ''}${menuStatus.last ? ' lastInLevel' : ''}"
                               scope="request"/>
                        <li class="${listItemCssClass}">
                            <c:set var="statusNavMenu" value="${menuStatus}" scope="request"/>
                            <template:module node="${menuItem}" view="${menuItemTemplate}" editable="false"/>
                                <%--<a href="">${menuItem.name}</a>--%>
                            <c:if test="${hasChildren}">
                                <template:include view="default">
                                    <template:param name="base" value="${menuItem.identifier}"/>
                                    <template:param name="navMenuLevel" value="${navMenuLevel + 1}"/>
                                    <template:param name="omitFormatting" value="true"/>
                                </template:include>
                            </c:if>
                        </li>
                    </c:when>
                    <c:otherwise>
                        <c:if test="${hasChildren}">
                            <li>
                                <template:include view="default">
                                    <template:param name="base" value="${menuItem.identifier}"/>
                                    <template:param name="navMenuLevel" value="${navMenuLevel + 1}"/>
                                    <template:param name="omitFormatting" value="true"/>
                                </template:include>
                            </li>
                        </c:if>
                    </c:otherwise>
                </c:choose>
            </c:if>
        </c:forEach>
        <c:if test="${not notempty and renderContext.editMode}">
            <li><fmt:message key="label.navbar.empty"/> </li>
        </c:if>
    </ul>
    <c:if test="${navMenuLevel > 1}">
        </div>
    </c:if>
    <c:if test="${navMenuLevel eq 1}">
        </div>
    </c:if>
</c:if>
<c:if test="${navMenuLevel eq 1}">
    <c:if test="${!empty layoutID}">
        </div>
    </c:if>
    </div>
</c:if>
