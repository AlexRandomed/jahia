<%@ page import="org.jahia.utils.FileUtils" %>
<%@ page import="org.jahia.services.content.JCRNodeWrapper" %>
<%@ taglib prefix="jcr" uri="http://www.jahia.org/tags/jcr" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<%@ taglib prefix="template" uri="http://www.jahia.org/tags/templateLib" %>
<template:addResources type="css" resources="files.css"/>

<jcr:nodeProperty node="${currentNode}" name="jcr:created" var="created"/>
<fmt:formatDate value="${created.time}" dateStyle="full" var="displayDate"/>
<c:choose>
    <c:when test="${fn:startsWith(currentNode.fileContent.contentType,'image/')}">
        <img src="${currentNode.url}"
             alt="${fn:escapeXml(currentNode.name)}"/>
    </c:when>
    <c:otherwise>
        <span class="icon <%=FileUtils.getFileIcon( ((JCRNodeWrapper)pageContext.findAttribute("currentNode")).getName()) %>"></span>
        <a href="${pageContext.request.contextPath}/files${currentNode.path}"
             title="${displayDate}">${currentNode.name}</a>
    </c:otherwise>
</c:choose>