<%@ page contentType="text/html; UTF-8" %>
<%@ taglib prefix="template" uri="http://www.jahia.org/tags/templateLib" %>
<%@ taglib prefix="jcr" uri="http://www.jahia.org/tags/jcr" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>

<c:if test="${currentNode.properties['j:limit'].long gt 0}">
    <jcr:sql var="result"
             sql="select * from [jnt:blogContent] as blogContent  where isdescendantnode(blogContent, ['${currentNode.parent.path}']) order by blogContent.[jcr:lastModified] desc"/>

    <c:set var="forcedSkin" value="none" />
    <c:set var="renderOptions" value="none" />
    <c:set var="currentList" value="${result.nodes}" scope="request"/>
    <c:set var="end" value="${fn:length(result.nodes)}" scope="request"/>
    <c:set var="listTotalSize" value="${end}" scope="request"/>
    <c:set var="forcedTemplate" value="hidden.short" scope="request"/>
</c:if>
${fn:length(result.nodes)} -  ${currentNode.parent.parent.path}