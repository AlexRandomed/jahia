<%@ page import="org.jahia.services.content.JCRNodeWrapper" %>
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
<c:set var="current" value="${baseline.node}"/>
<c:if test="${not empty currentResource.moduleParams.base}">
  <jcr:node var="current" uuid="${currentResource.moduleParams.base}" />
</c:if>

<c:set var="startLevelValue" value="0"/>
<c:if test="${not empty startLevel}">
    <c:set var="startLevelValue" value="${startLevel.long}"/>
</c:if>

<c:set var="navMenuLevel" value="${empty currentResource.moduleParams.navMenuLevel ? 1 : currentResource.moduleParams.navMenuLevel}"/>
<c:if test="${navMenuLevel == 1}">
    <div id="${styleName.string}">
    <c:if test="${not empty title.string}">
        <span><c:out value="${fn:escapeXml(title.string)}"/></span>
    </c:if>
</c:if>

<c:set var="items" value="${jcr:getChildrenOfType(current,'jmix:navMenuItem')}"/>
        <c:if test="${navMenuLevel eq 1}">
            <div id="navbar">
        </c:if>
        <c:if test="${navMenuLevel > 1}">
            <div class="box-inner">
        </c:if>
        <ul class="navmenu level_${navMenuLevel - startLevelValue}">
            <c:forEach items="${items}" var="menuItem" varStatus="menuStatus">
                <c:set var="inpath" value="${fn:startsWith(renderContext.mainResource.node.path, menuItem.path)}"/>
                <c:set var="selected" value="${renderContext.mainResource.node.path eq menuItem.path}"/>
                <c:set var="correctType" value="false"/>
                <c:forEach items="${menuItem.properties['j:displayInMenu']}" var="display">
                   <c:if test="${display.string eq currentNode.properties['j:menuType'].string}">
                       <c:set var="correctType" value="${display.string eq currentNode.properties['j:menuType'].string}"/>
                   </c:if>
                </c:forEach>

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
                                <template:module node="${menuItem}" template="hidden.menuElement" editable="false"/>
                                    <%--<a href="">${menuItem.name}</a>--%>
                                <c:if test="${hasChildren}">
                                    <template:include template="default">
                                        <template:param name="base" value="${menuItem.identifier}"/>
                                        <template:param name="navMenuLevel" value="${navMenuLevel + 1}"/>
                                        <template:param name="omitFormatting" value="true"/>
                                    </template:include>
                                </c:if>
                            </li>
                        </c:when>
                        <c:otherwise>
                            <c:if test="${hasChildren}">
                                <template:include template="default">
                                    <template:param name="base" value="${menuItem.identifier}"/>
                                    <template:param name="navMenuLevel" value="${navMenuLevel + 1}"/>
                                    <template:param name="omitFormatting" value="true"/>
                                </template:include>
                            </c:if>
                        </c:otherwise>
                    </c:choose>
                </c:if>
            </c:forEach>
            <c:if test="${not notempty and renderContext.editMode}">
	       <li>Empty nav bar</li>
	       </c:if>
        </ul>
        <c:if test="${navMenuLevel > 1}">
            </div>
        </c:if>
        <c:if test="${navMenuLevel eq 1}">
            </div>
        </c:if>
<c:if test="${navMenuLevel == 1}">
    </div>
</c:if>
<%
    String n = ((JCRNodeWrapper)pageContext.findAttribute("current")).getName() ;
    String cn = ((JCRNodeWrapper)pageContext.findAttribute("currentNode")).getName() ;
    System.out.println("Navigation bar for "+ cn + " / " + n + " : " +(System.currentTimeMillis()-t) + "ms");
%>
