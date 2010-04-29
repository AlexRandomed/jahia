<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<%@ taglib prefix="jcr" uri="http://www.jahia.org/tags/jcr" %>
<c:if test="${jcr:isNodeType(currentNode, 'mix:title')}">
<jcr:nodeProperty node="${currentNode}" name="jcr:title" var="title"/>
<jcr:nodeProperty node="${currentNode}" name="jcr:description" var="description"/>
<c:if test="${not empty description.string}"><c:set var="linkTitle"> title="${fn:escapeXml(description.string)}"</c:set></c:if>
</c:if>
<c:if test="${not omitFormatting && not empty param.cssClass}"><c:set var="class"> class="${param.cssClass}"</c:set></c:if>
<c:url var="urlValue" value="${currentNode.path}.html" context="${url.base}"/>
<c:url var="urlValue" value="${jcr:isNodeType(currentNode, 'nt:file') ? currentNode.url : urlValue}" context="/"/>
<a href="${urlValue}"${class}${linkTitle}>${fn:escapeXml(not param.useNodeNameAsTitle && not empty title.string ? title.string : currentNode.name)}</a>