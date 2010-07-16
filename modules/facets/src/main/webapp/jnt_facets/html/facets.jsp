<%@ taglib prefix="jcr" uri="http://www.jahia.org/tags/jcr" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib prefix="template" uri="http://www.jahia.org/tags/templateLib" %>
<%@ taglib prefix="query" uri="http://www.jahia.org/tags/queryLib" %>
<%@ taglib prefix="facet" uri="http://www.jahia.org/tags/facetLib" %>
<%@ taglib prefix="functions" uri="http://www.jahia.org/tags/functions" %>
<%@ taglib prefix="uiComponents" uri="http://www.jahia.org/tags/uiComponentsLib" %>
<%--@elvariable id="currentNode" type="org.jahia.services.content.JCRNodeWrapper"--%>
<%--@elvariable id="out" type="java.io.PrintWriter"--%>
<%--@elvariable id="script" type="org.jahia.services.render.scripting.Script"--%>
<%--@elvariable id="scriptInfo" type="java.lang.String"--%>
<%--@elvariable id="workspace" type="java.lang.String"--%>
<%--@elvariable id="renderContext" type="org.jahia.services.render.RenderContext"--%>
<%--@elvariable id="currentResource" type="org.jahia.services.render.Resource"--%>
<%--@elvariable id="url" type="org.jahia.services.render.URLGenerator"--%>
<%--@elvariable id="acl" type="java.lang.String"--%>
<c:set var="bindedComponent" value="${uiComponents:getBindedComponent(currentNode, renderContext, 'j:bindedComponent')}"/>
<c:if test="${not empty bindedComponent}">
    <c:set var="facetParamVarName" value="N-${bindedComponent.name}"/>
    <c:set var="activeFacetMapVarName" value="afm-${bindedComponent.name}"/>    
    <c:if test="${not empty param[facetParamVarName] and empty activeFacetsVars[facetParamVarName]}">
        <c:if test="${activeFacetsVars == null}">
           <jsp:useBean id="activeFacetsVars" class="java.util.HashMap" scope="request"/>
        </c:if>
        <c:set target="${activeFacetsVars}" property="${facetParamVarName}" value="${facet:decodeFacetUrlParam(param[facetParamVarName])}"/>
        <c:set target="${activeFacetsVars}" property="${activeFacetMapVarName}" value="${facet:getAppliedFacetFilters(activeFacetsVars[facetParamVarName])}"/>
    </c:if>
    
    <jsp:useBean id="facetLabels" class="java.util.HashMap" scope="request"/>
    <jsp:useBean id="facetValueLabels" class="java.util.HashMap" scope="request"/>    
    <jsp:useBean id="facetValueFormats" class="java.util.HashMap" scope="request"/>    
    
    <query:definition var="listQuery" scope="request">
        <query:selector nodeTypeName="nt:base"/>
        <query:childNode path="${bindedComponent.path}"/>

        <c:forEach items="${jcr:getNodes(currentNode, 'jnt:facet')}" var="facet">
            <jcr:nodeProperty node="${facet}" name="facet" var="currentFacetGroup"/>        
            <jcr:nodeProperty node="${facet}" name="field" var="currentField"/>
            <c:set var="facetNodeTypeName" value="${fn:substringBefore(currentField.string, ';')}"/>
            <c:set var="facetPropertyName" value="${fn:substringAfter(currentField.string, ';')}"/>
            <jcr:nodeType name="${facetNodeTypeName}" var="facetNodeType"/>
            <jcr:nodeProperty node="${facet}" name="mincount" var="minCount"/>
            <c:set var="minCountParam" value=""/>            
            <c:if test="${not empty minCount.string}">
                <c:set var="minCountParam" value="&mincount=${minCount.string}"/>
            </c:if>            
            
            <jcr:nodeProperty node="${facet}" name="label" var="currentFacetLabel"/>
            <c:if test="${not empty currentFacetLabel.string and not empty facetPropertyName}">
                <c:set target="${facetLabels}" property="${facetPropertyName}" value="${currentFacetLabel.string}"/>
            </c:if>
            
            <c:choose>
                <c:when test="${jcr:isNodeType(facet, 'jnt:fieldFacet') or jcr:isNodeType(facet, 'jnt:dateFacet')}">
                    <c:if test="${not empty currentField and not facet:isFacetApplied(facetPropertyName, activeFacetsVars[activeFacetMapVarName], facetNodeType.propertyDefinitionsAsMap[facetPropertyName])}">
                        <c:set var="facetQuery" value="nodetype=${facetNodeTypeName}&key=${facetPropertyName}${minCountParam}"/>
                        <c:choose>
                            <c:when test="${jcr:isNodeType(facet, 'jnt:dateFacet')}">
                                <jcr:nodeProperty node="${facet}" name="format" var="currentFacetValueFormat"/>
                                <c:if test="${not empty currentFacetValueFormat.string}">
                                    <c:set target="${facetValueFormats}" property="${facetPropertyName}" value="${currentFacetValueFormat.string}"/>
                                </c:if>                                                                            
                                <c:set var="facetPrefix" value="date."/>
                            </c:when>
                            <c:otherwise>
                                <c:set var="facetPrefix" value=""/>
                            </c:otherwise>                  
                        </c:choose>      
                        <c:forEach items="${facet.primaryNodeType.declaredPropertyDefinitions}" var="propertyDefinition">
                            <jcr:nodeProperty node="${facet}" name="${propertyDefinition.name}" var="facetPropValue"/>
                            <c:choose>
                                <c:when test="${functions:isIterable(facetPropValue)}">
                                    <c:forEach items="${facetPropValue}" var="facetPropValueItem">
                                        <c:if test="${not empty facetPropValueItem.string}">
                                            <c:set var="facetQuery" value="${facetQuery}&${facetPrefix}${propertyDefinition.name}=${facetPropValueItem.string}"/>
                                        </c:if>                                
                                    </c:forEach>
                                </c:when>
                                <c:otherwise>
                                    <c:if test="${not empty facetPropValue.string}">
                                        <c:set var="facetQuery" value="${facetQuery}&${facetPrefix}${propertyDefinition.name}=${facetPropValue.string}"/>
                                    </c:if>
                                </c:otherwise>
                            </c:choose>
                        </c:forEach>              
                        <query:column columnName="rep:facet(${facetQuery})" propertyName="${facetPropertyName}"/>
                    </c:if>
                </c:when>
                <c:otherwise>
                    <c:choose>
                        <c:when test="${jcr:isNodeType(facet, 'jnt:rangeFacet')}">
                            <jcr:nodeProperty node="${facet}" name="lowerBound" var="lowerBound"/>            
                            <jcr:nodeProperty node="${facet}" name="upperBound" var="upperBound"/>                    
                            <jcr:nodeProperty node="${facet}" name="includeBounds" var="includeBounds"/>
                            <c:set var="closeBrace">}</c:set>
                            <c:set var="currentFacetQuery" value="${includeBounds.boolean ? '[' : '{'}${lowerBound.string} TO ${upperBound.string}${includeBounds.boolean ? ']' : closeBrace}"/>
                        </c:when>
                        <c:when test="${jcr:isNodeType(facet, 'jnt:queryFacet')}">
                            <jcr:nodeProperty node="${facet}" name="query" var="currentFacetQuery"/>                        
                            <c:set var="currentFacetQuery" value="${currentFacetQuery.string}"/>
                        </c:when>
                    </c:choose>                    
                    <jcr:nodeProperty node="${facet}" name="valueLabel" var="currentFacetValueLabel"/>
                    <c:if test="${not empty currentFacetValueLabel.string and not empty currentFacetQuery}">
                        <c:set target="${facetValueLabels}" property="${currentFacetQuery}" value="${currentFacetValueLabel.string}"/>
                    </c:if>    
                    <c:if test="${not empty currentFacetLabel.string and not empty currentFacetQuery}">
                        <c:set target="${facetLabels}" property="${currentFacetQuery}" value="${currentFacetLabel.string}"/>
                    </c:if>
                    <c:if test="${not empty currentFacetQuery and not facet:isFacetApplied(currentFacetQuery, activeFacetsVars[activeFacetMapVarName], null)}">
                        <query:column columnName="rep:facet(nodetype=${facetNodeTypeName}&key=${facet.name}${minCountParam}&facet.query=${currentFacetQuery})" propertyName="${not empty facetPropertyName ? facetPropertyName : 'rep:facet()'}"/>
                    </c:if>            
                </c:otherwise>
            </c:choose>
        </c:forEach>
        <c:forEach items="${activeFacetsVars[activeFacetMapVarName]}" var="facet">
            <c:forEach items="${facet.value}" var="facetValue">
                <query:fullTextSearch propertyName="rep:filter(${jcr:escapeIllegalJcrChars(facet.key)})" searchExpression="${facetValue.value}"/>
            </c:forEach>
        </c:forEach>
    </query:definition>
    <jcr:jqom var="result" qomBeanName="listQuery" scope="request"/>

    <div class="facets">
        <h3><fmt:message key="facets.facets"/></h3>
        <%@include file="activeFacets.jspf"%>        
        <c:forEach items="${result.facetFields}" var="currentFacet">
            <%@include file="facetDisplay.jspf"%>
        </c:forEach>
        <c:forEach items="${result.facetDates}" var="currentFacet">
            <%@include file="facetDisplay.jspf"%>
        </c:forEach>
        <c:set var="currentFacetLabel" value=""/>
        <c:set var="mappedFacetLabel" value=""/>
        <c:forEach items="${result.facetQuery}" var="facetValue" varStatus="iterationStatus">
            <facet:facetLabel currentActiveFacet="${facetValue}" facetLabels="${facetLabels}" display="false"/>
            <c:if test="${iterationStatus.first or (mappedFacetLabel != currentFacetLabel and not empty mappedFacetLabel)}">
                <c:set var="currentFacetLabel" value="${mappedFacetLabel}"/>
                <c:if test="${not empty currentFacetLabel}">
                    </ul>
                </c:if>
                <h4>${mappedFacetLabel}</h4>
                <ul>        
            </c:if>
            <c:if test="${not facet:isFacetValueApplied(facetValue, activeFacetsVars[activeFacetMapVarName])}">
                <c:url var="facetUrl" value="${url.mainResource}" context="/">
                    <c:param name="${facetParamVarName}" value="${facet:encodeFacetUrlParam(facet:getFacetDrillDownUrl(facetValue, activeFacetsVars[facetParamVarName]))}"/>
                </c:url>
                <li><a href="${facetUrl}"><facet:facetValueLabel currentActiveFacetValue="${facetValue}" facetValueLabels="${facetValueLabels}"/></a> (${facetValue.value})<br/></li>
            </c:if>
        </c:forEach>     
        <c:if test="${not empty currentFacetLabel}">
            </ul>
        </c:if>                     
    </div>
</c:if>
<c:if test="${renderContext.editMode}">
    <fmt:message key="facets.facetsSet"/> :
    <c:forEach items="${jcr:getNodes(currentNode, 'jnt:facet')}" var="facet">
        <template:module node="${facet}"/>
    </c:forEach>
    <template:module path="*"/>
    <template:linker property="j:bindedComponent"/>
</c:if>