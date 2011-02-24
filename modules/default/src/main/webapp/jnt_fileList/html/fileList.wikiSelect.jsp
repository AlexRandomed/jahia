<%@ taglib prefix="jcr" uri="http://www.jahia.org/tags/jcr" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<%@ taglib prefix="template" uri="http://www.jahia.org/tags/templateLib" %>
<template:addResources type="javascript" resources="jquery.js"/>
<template:addResources type="javascript" resources="jquery.fancybox.js"/>
<template:addResources type="css" resources="jquery.fancybox.css"/>

<c:set var="targetNodePath" value="${renderContext.mainResource.node.path}"/>
<c:if test="${!empty param.targetNodePath}">
    <c:set var="targetNodePath" value="${param.targetNodePath}"/>
</c:if>
<c:if test="${!empty currentNode.properties.folder}">
    <c:set var="targetNodePath" value="${currentNode.properties.folder.node.path}"/>
</c:if>
<script type="text/javascript">
    function insertImgSyntax(content) {
        document.formWiki.wikiContent.value += content;
    }
</script>
<div id="fileList${currentNode.identifier}">
    <template:addResources type="css" resources="fileList.css"/>
    <jcr:node var="targetNode" path="${targetNodePath}"/>
    <ul class="filesList">
        <c:forEach items="${targetNode.nodes}" var="subchild">
            <c:if test="${jcr:isNodeType(subchild, 'jnt:file')}">
                <li>
                    <c:if test="${fn:startsWith(subchild.fileContent.contentType,'image/')}">
                        <img name="" width="100" src="${subchild.url}"
                             ondblclick="insertImgSyntax('\n [[image:${subchild.url}||width=${subchild.properties["j:width"].string} height=${subchild.properties["j:height"].string}]]')"
                             alt="${fn:escapeXml(subchild.name)}"/>
                    </c:if>
                </li>
            </c:if>
        </c:forEach>
    </ul>
</div>
<template:addCacheDependency node="${currentNode.parent}"/>
