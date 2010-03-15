<%@ taglib prefix="jcr" uri="http://www.jahia.org/tags/jcr" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="utility" uri="http://www.jahia.org/tags/utilityLib" %>
<%@ taglib prefix="template" uri="http://www.jahia.org/tags/templateLib" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<%@ taglib prefix="functions" uri="http://www.jahia.org/tags/functions" %>
<%@ taglib prefix="ui" uri="http://www.jahia.org/tags/uiComponentsLib" %>
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
<%--@elvariable id="selectorType" type="org.jahia.services.content.nodetypes.SelectorType"--%>
<template:addResources type="css" resources="960.css"/>
<template:addResources type="css" resources="formbuilder.css"/>
<utility:useConstants var="jcrPropertyTypes" className="org.jahia.services.content.nodetypes.ExtendedPropertyType"
                      scope="application"/>
<utility:useConstants var="selectorType" className="org.jahia.services.content.nodetypes.SelectorType"
                      scope="application"/>
<div class="Form FormBuilder">
    <form action="${url.base}${currentNode.path}/*" method="post" id="${currentNode.name}">
        <jcr:nodeType name="jnt:news" var="type"/>
        <input type="hidden" name="nodeType" value="jnt:news"/>
        <input type="hidden" name="redirectTo" value="${url.base}${renderContext.mainResource.node.path}"/>
        <%-- Define the output format for the newly created node by default html or by redirectTo--%>
        <input type="hidden" name="newNodeOutputFormat" value="html"/>
        <fieldset>
            <legend>${jcr:labelForLocale(type,renderContext.mainResourceLocale)}</legend>
            <c:forEach items="${type.propertyDefinitions}" var="propertyDefinition">
                <c:if test="${!propertyDefinition.multiple and propertyDefinition.contentItem}">
                    <p class="field">
                        <c:choose>
                            <c:when test="${propertyDefinition.requiredType == jcrPropertyTypes.BINARY}">
                                <label class="left">BINARY
                                    : ${jcr:labelForLocale(propertyDefinition,renderContext.mainResourceLocale)}</label>
                            </c:when>
                            <c:when test="${(propertyDefinition.requiredType == jcrPropertyTypes.REFERENCE || propertyDefinition.requiredType == jcrPropertyTypes.WEAKREFERENCE)}">
                                <label class="left">Reference
                                    : ${jcr:labelForLocale(propertyDefinition,renderContext.mainResourceLocale)}</label>
                            </c:when>
                            <c:when test="${propertyDefinition.requiredType == jcrPropertyTypes.DATE}">
                                <label class="left">Date
                                    : ${jcr:labelForLocale(propertyDefinition,renderContext.mainResourceLocale)}</label>
                                <%--<input type="text" id="${fn:replace(propertyDefinition.name,':','_')}" name="${propertyDefinition.name}" readonly="readonly"/>
                                <ui:dateSelector fieldId="${fn:replace(propertyDefinition.name,':','_')}"/>--%>
                            </c:when>
                            <c:when test="${propertyDefinition.selector eq selectorType.CHOICELIST}">
                                <label class="left">${selectorType.valueToName[propertyDefinition.selector]}
                                    : ${jcr:labelForLocale(propertyDefinition,renderContext.mainResourceLocale)}</label>
                                <jcr:propertyInitializers var="options" nodeType="jnt:news"
                                                          name="${propertyDefinition.name}"/>
                                <select name="${propertyDefinition.name}" id="${fn:replace(propertyDefinition.name,':','_')}">
                                    <c:forEach items="${options}" var="option">
                                        <option value="${option.value.string}" style="background:url(${option.properties.image}) no-repeat top left;padding-left:25px">${option.displayName}</option>
                                    </c:forEach>
                                </select>
                            </c:when>
                            <c:otherwise>
                                <label class="left"
                                       for="${fn:replace(propertyDefinition.name,':','_')}">${jcr:labelForLocale(propertyDefinition,renderContext.mainResourceLocale)}</label>
                                <input type="text" id="${fn:replace(propertyDefinition.name,':','_')}"
                                       name="${propertyDefinition.name}"/>
                            </c:otherwise>
                        </c:choose>
                    </p>
                </c:if>
            </c:forEach>
            <input class="button" type="submit" value="Submit"/>
        </fieldset>
    </form>
</div>
