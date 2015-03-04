<%@ tag body-content="scriptless" description="Renders the HTML form element to wrap up the login controls." %>
<%@ tag dynamic-attributes="attributes"%>
<%@ attribute name="doRedirect" required="false" type="java.lang.Boolean"
              description="Do we need to perform a client-side redirect after the login? Setting it to true, will will prevent browser built-in warning message if the page needs to be reloaded (after POST method). [true]"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions"%>
<%@ taglib prefix="functions" uri="http://www.jahia.org/tags/functions"%>
<c:set var="org.jahia.tags.login.form.class" value="<%= this.getClass() %>" scope="request"/>
<c:if test="${!currentRequest.logged}">
    <c:set var="formId" value="<%= this.toString() %>"/>
    <c:url value="/cms/login" var="loginUrl"/>
    <c:set target="${attributes}" property="action" value="${functions:default(attributes.action, loginUrl)}"/>
    <c:set target="${attributes}" property="name" value="${functions:default(attributes.name, 'loginForm')}"/>
	<c:set target="${attributes}" property="method" value="${functions:default(attributes.method, 'post')}"/>
    <form ${functions:attributes(attributes)}>
        <input type="hidden" name="site" value="${renderContext != null ? renderContext.site.name : urlResolver.siteKey}"/>
        <c:choose>
            <c:when test="${not empty attributes.redirectTo}">
                <input type="hidden" name="redirect" value="${attributes.redirectTo}"/>
            </c:when>
            <c:when test="${not empty requestScope['javax.servlet.error.request_uri']}">
                <c:url var="redirect" value="${requestScope['javax.servlet.error.request_uri']}" context="/">
                    <c:forEach items="${paramValues}" var="paramValueEntry">
                        <c:forEach items="${paramValueEntry.value}" var="paramValue">
                            <c:param name="${paramValueEntry.key}" value="${paramValue}"/>
                        </c:forEach>
                    </c:forEach>
                </c:url>
                <input type="hidden" name="redirect" value="${redirect}"/>
            </c:when>
            <c:when test="${not empty renderContext && not empty renderContext.mainResource}">
                <c:choose>
                    <c:when test="${renderContext.mainResource.template != 'default'}">
                        <c:set var="urlEnd" value=".${renderContext.mainResource.template}.html"/>
                    </c:when>
                    <c:otherwise>
                        <c:set var="urlEnd" value=".html"/>
                    </c:otherwise>
                </c:choose>
                <c:url var="redirect" value="${url.base}${renderContext.mainResource.node.path}${urlEnd}">
                    <c:forEach items="${paramValues}" var="paramValueEntry">
                        <c:forEach items="${paramValueEntry.value}" var="paramValue">
                            <c:param name="${paramValueEntry.key}" value="${paramValue}"/>
                        </c:forEach>
                    </c:forEach>
                </c:url>
                <input type="hidden" name="redirect" value="${redirect}"/>
                <input type="hidden" name="failureRedirect" value="${redirect}"/>
            </c:when>
        </c:choose>
        <jsp:doBody/>
    </form>
</c:if>