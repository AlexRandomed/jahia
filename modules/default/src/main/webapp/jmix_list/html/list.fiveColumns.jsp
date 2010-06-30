<%@ taglib prefix="jcr" uri="http://www.jahia.org/tags/jcr" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="utility" uri="http://www.jahia.org/tags/utilityLib" %>
<%@ taglib prefix="template" uri="http://www.jahia.org/tags/templateLib" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<%--@elvariable id="currentNode" type="org.jahia.services.content.JCRNodeWrapper"--%>
<%--@elvariable id="out" type="java.io.PrintWriter"--%>
<%--@elvariable id="script" type="org.jahia.services.render.scripting.Script"--%>
<%--@elvariable id="scriptInfo" type="java.lang.String"--%>
<%--@elvariable id="workspace" type="java.lang.String"--%>
<%--@elvariable id="renderContext" type="org.jahia.services.render.RenderContext"--%>
<%--@elvariable id="currentResource" type="org.jahia.services.render.Resource"--%>
<%--@elvariable id="url" type="org.jahia.services.render.URLGenerator"--%>
<template:addResources type="css" resources="Columns.css"/>

<template:include template="hidden.header"/>

<div class="columns5"><!--start 4columns -->
    <c:forEach items="${moduleMap.currentList}" var="subchild" begin="${moduleMap.begin}" end="${moduleMap.end}" varStatus="status">
        <c:if test="${status.index > 0 and (status.index mod 5 eq 0)}">
            <div class="clear"></div>
        </c:if>
        <div class="column-item">
            <div class="spacer">
                <template:module node="${subchild}" template="${moduleMap.subNodesTemplate}"  editable="${moduleMap.editable}"/>
            </div>
        </div>
    </c:forEach>
    <c:if test="${moduleMap.editable and renderContext.editMode}">
        <div class="column-item">
            <div class="spacer">
                <template:module path="*"/>
            </div>
        </div>
    </c:if>
    <div class="clear"></div>
</div><!--stop 5columns -->
<template:include template="hidden.footer"/>
