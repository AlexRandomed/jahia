<%@ taglib prefix="jcr" uri="http://www.jahia.org/tags/jcr" %>
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

<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="utility" uri="http://www.jahia.org/tags/utilityLib" %>
<%@ taglib prefix="template" uri="http://www.jahia.org/tags/templateLib" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<%@ taglib prefix="functions" uri="http://www.jahia.org/tags/functions" %>
<%@ taglib prefix="uiComponents" uri="http://www.jahia.org/tags/uiComponentsLib" %>
<%--@elvariable id="currentNode" type="org.jahia.services.content.JCRNodeWrapper"--%>
<%--@elvariable id="script" type="org.jahia.services.render.scripting.Script"--%>
<%--@elvariable id="renderContext" type="org.jahia.services.render.RenderContext"--%>
<%--@elvariable id="currentResource" type="org.jahia.services.render.Resource"--%>
<%--@elvariable id="url" type="org.jahia.services.render.URLGenerator"--%>

<c:set var="bindedComponent"
       value="${uiComponents:getBindedComponent(currentNode, renderContext, 'j:bindedComponent')}"/>
<jcr:nodeProperty node="${bindedComponent}" name="j:defaultCategory" var="assignedCategories"/>
<c:set var="separator" value="${functions:default(currentResource.moduleParams.separator, ', ')}"/>
<jsp:useBean id="filteredCategories" class="java.util.LinkedHashMap"/>
<c:set var="props" value="${currentNode.properties}"/>
<c:if test="${props.creationdate.boolean}">
    ${bindedComponent.properties['jcr:created'].time}
</c:if>
<c:if test="${props.creator.boolean}">
    ${bindedComponent.properties['jcr:createdBy'].string}
</c:if>
<c:if test="${props.lastmodification.boolean}">
    ${bindedComponent.properties['jcr:lastModified'].time}
</c:if>
<c:if test="${props.lastcontributor.boolean}">
    ${bindedComponent.properties['jcr:lastModifiedBy'].string}
</c:if>
<c:if test="${props.description.boolean}">
    ${bindedComponent.properties['jcr:description'].string}
</c:if>
<c:if test="${props.keywords.boolean}">
    <c:if test="${not empty bindedComponent.properties['j:keywords']}">
        <c:forEach items="${bindedComponent.properties['j:keywords']}" var="keyword">
            ${keyword.string}
        </c:forEach>
    </c:if>
</c:if>
<c:if test="${props.categories.boolean}">
    <c:forEach items="${assignedCategories}" var="category" varStatus="status">
        <c:if test="${not empty category.node}">
            <c:set target="${filteredCategories}" property="${category.node.properties['jcr:title'].string}"
                   value="${category.node.properties['jcr:title'].string}"/>
        </c:if>
    </c:forEach>
    <c:choose>
        <c:when test="${not empty filteredCategories}">
            <c:forEach items="${filteredCategories}" var="category" varStatus="status">
                ${fn:escapeXml(category.value)}${!status.last ? separator : ''}
            </c:forEach>
        </c:when>
        <c:otherwise>
            <fmt:message key="label.categories.noCategory"/>
        </c:otherwise>
    </c:choose>
</c:if>
