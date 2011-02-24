<%@ page import="org.jahia.utils.FileUtils" %>
<%@ page import="org.jahia.services.content.JCRNodeWrapper" %>
<%@ taglib prefix="jcr" uri="http://www.jahia.org/tags/jcr" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<%@ taglib prefix="template" uri="http://www.jahia.org/tags/templateLib" %>
<template:addResources type="javascript" resources="jquery.js"/>
<template:addResources type="javascript" resources="jquery.form.js"/>
<c:set var="targetNodePath" value="${renderContext.mainResource.node.path}"/>
<c:if test="${!empty param.targetNodePath}">
    <c:set var="targetNodePath" value="${param.targetNodePath}"/>
</c:if>
<c:if test="${!empty currentNode.properties.folder}">
    <c:set var="targetNodePath" value="${currentNode.properties.folder.node.path}"/>
</c:if>
<div id="fileList${currentNode.identifier}">
    <template:addResources type="css" resources="fileList.css"/>
    <jcr:node var="targetNode" path="${targetNodePath}"/>
    <ul class="filesList">
        <c:forEach items="${targetNode.nodes}" var="subchild">
            <c:if test="${jcr:isNodeType(subchild, 'jnt:file')}">
                <li>

                    <c:choose>
                        <c:when test="${fn:startsWith(subchild.fileContent.contentType,'image/')}">
                            <div onclick="return false;" ondblclick="CKEDITOR.instances.editContent.insertHtml('<img src=\'${subchild.url}\'/>')">
                                <img width="100" src="${subchild.url}"  alt="${fn:escapeXml(subchild.name)}" onmousedown="return false;" />
                                    ${fn:escapeXml(not empty title.string ? title.string : subchild.name)}
                            </div>
                        </c:when>
                        <c:otherwise>
                            <jcr:nodeProperty node="${subchild}" name="jcr:title" var="title"/>
                            <div onclick="return false;" ondblclick="CKEDITOR.instances.editContent.insertHtml('<a href=\'${subchild.url}\' title=\'${fn:escapeXml(not empty title.string ? title.string : subchild.name)}\'>  ${fn:escapeXml(not empty refTitle ? refTitle : not empty title.string ? title.string : subchild.name)} </a>')">
                                <span class="icon <%=FileUtils.getFileIcon( ((JCRNodeWrapper) pageContext.findAttribute("subchild")).getName()) %>"></span>
                                <a href="${subchild.url}" onmousedown="return false;"
                                   title="${fn:escapeXml(not empty title.string ? title.string : subchild.name)}">
                                        ${fn:escapeXml(not empty refTitle ? refTitle : not empty title.string ? title.string : subchild.name)}
                                </a>
                            </div>
                        </c:otherwise>
                    </c:choose>

                    <c:if test="${jcr:hasPermission(subchild,'jcr:removeNode')}">
                        <form action="${url.base}${subchild.path}" method="post"
                              id="jahia-blog-item-delete-${subchild.UUID}">
                            <input type="hidden" name="methodToCall" value="delete"/>
                            <button><fmt:message key="label.delete"/></button>
                            <script type="text/javascript">
                                $(document).ready(function() {
                                    // bind 'myForm' and provide a simple callback function
                                    var options = {
                                        success: function() { $('#fileList${currentNode.identifier}').load('${url.base}${currentNode.path}.html.ajax?targetNodePath=${targetNode.path}'); }
                                    }
                                    $('#jahia-blog-item-delete-${subchild.UUID}').ajaxForm(options);
                                });
                            </script>
                        </form>
                    </c:if>
                </li>
            </c:if>
        </c:forEach>
        <li><fmt:message key="label.dblClickToAdd"/></li>
    </ul>
</div>
<template:addCacheDependency path="${targetNodePath}"/>
