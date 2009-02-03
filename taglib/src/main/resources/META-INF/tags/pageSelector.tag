<%--
/*
 * Copyright 2002-2008 Jahia Ltd
 *
 * Licensed under the JAHIA COMMON DEVELOPMENT AND DISTRIBUTION LICENSE (JCDDL),
 * Version 1.0 (the "License"), or (at your option) any later version; you may
 * not use this file except in compliance with the License. You should have
 * received a copy of the License along with this program; if not, you may obtain
 * a copy of the License at
 *
 *  http://www.jahia.org/license/
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
--%>
<%@ tag body-content="empty" description="Renders the link to the page selection engine (as a popup window)." %>
<%@ tag import="java.util.*,
                org.jahia.engines.selectpage.SelectPage_Engine,
                org.jahia.params.ProcessingContext,
                org.jahia.registries.EnginesRegistry" %>
<%@ attribute name="fieldId" required="true" type="java.lang.String"
              description="The input field name and ID to synchronize the seletcted page value with." %>
<%@ attribute name="fieldIdIncludeChildren" required="false" type="java.lang.String"
              description="The ID and name of the include children input field." %>
<%@ attribute name="displayIncludeChildren" required="false" type="java.lang.Boolean"
              description="Do show the include children checkbox." %>
<%@ attribute name="includeChildren" type="java.lang.Boolean"
              description="The initial value for the include children input field." %>
<%@ attribute name="useUrl" required="false" type="java.lang.Boolean"
              description="If set to true the selected page URL will be used in the field value; otherwise the ID of the selected page will be used (default)." %>
<%@ attribute name="label" required="false" type="java.lang.String"
              description="The select link text." %>
<%@ attribute name="onSelect" required="false" type="java.lang.String"
              description="The JavaScript function to be called after a page is selectd. Two paramaters are passed as arguments: page ID and page URL. If the function retuns true, the value will be also set into the field value. Otherwise nothing will be done by this tag." %>
<%@ tag dynamic-attributes="attributes"
        %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<%@ taglib prefix="h" uri="http://www.jahia.org/tags/functions" %>
<%@ taglib prefix="utility" uri="http://www.jahia.org/tags/utilityLib" %>
<c:set var="fieldIdHash"><%= Math.abs(jspContext.getAttribute("fieldId").hashCode()) %>
</c:set>
<c:set var="displayIncludeChildren" value="${not empty displayIncludeChildren ? displayIncludeChildren : 'true'}"/>
<c:set var="useUrl" value="${not empty useUrl ? useUrl : 'false'}"/>
<c:if test="${empty fieldIdIncludeChildren}"><c:set var="fieldIdIncludeChildren"
                                                    value="${fieldId}_includeChildren"/></c:if>
<%-- by default set includeChildren to 'true' to search in subfolders --%>
<c:set var="includeChildren" value="${not empty includeChildren ? includeChildren : 'true'}"/>
<%-- resolve includeChildren either from request parameter or from the default value (note that the 'false' value is not submitted for checkbox) --%>
<c:set var="includeChildren"
       value="${h:default(param[fieldIdIncludeChildren], empty paramValues[fieldId] ? includeChildren : 'false')}"/>
<jsp:useBean id="engineParams" class="java.util.HashMap"/>
<c:set target="${engineParams}" property="selectPageOperation" value="selectAnyPage"/>
<c:set target="${engineParams}" property="pageID" value="<%= Integer.valueOf(-1) %>"/>
<c:set target="${engineParams}" property="parentPageID" value="<%= Integer.valueOf(-1) %>"/>
<c:set target="${engineParams}" property="contextId" value="${fieldId}"/>
<c:set target="${engineParams}" property="callback" value="setSelectedPage${fieldIdHash}"/>
<c:set var="ctx" value="${jahia.processingContext}"/>
<c:set var="link"><%= ((SelectPage_Engine) EnginesRegistry.getInstance().getEngineByBeanName("selectPageEngine")).renderLink((ProcessingContext) jspContext.getAttribute("ctx"), (Map) jspContext.getAttribute("engineParams")) %></c:set>
<c:set target="${attributes}" property="href" value="#select"/>
<c:set target="${attributes}" property="onclick" value="<%= null %>"/>
<c:if test="${empty attributes.title}"><c:set target="${attributes}" property="title"><utility:resourceBundle resourceBundle="JahiaEnginesResources" resourceName="org.jahia.engines.search.selectPage" defaultValue="Select page"/></c:set></c:if>
<c:if test="${empty label}"><c:set var="label"><utility:resourceBundle resourceBundle="JahiaEnginesResources" resourceName="org.jahia.engines.search.select" defaultValue="select"/></c:set></c:if>
<a ${h:attributes(attributes)} onclick="javascript:{var pageSelector = window.open('${link}', '<%="pageSelector_" + session.getId().replaceAll("[^a-zA-Z0-9]", "_")%>', 'resizable,height=800,width=600'); pageSelector.focus(); return false;}">${fn:escapeXml(label)}</a>
<c:if test="${displayIncludeChildren}">
    &nbsp;<input type="checkbox" id="${fieldIdIncludeChildren}" name="${fieldIdIncludeChildren}" value="true" ${includeChildren ? 'checked="checked"' : ''}/>&nbsp;<label for="${fieldIdIncludeChildren}"><utility:resourceBundle resourceBundle="JahiaEnginesResources"
        resourceName="org.jahia.engines.search.selectPage.includeChildren" defaultValue="include subpages"/></label>
</c:if>
<script type="text/javascript">
    function setSelectedPage${fieldIdHash}(pid, url, title) {
    <c:if test="${not empty onSelect}">
        if ((${onSelect})(pid, url, title)) {
            document.getElementById('${fieldId}').value = ${useUrl} ? url : pid;
        }
    </c:if>
    <c:if test="${empty onSelect}">
        document.getElementById('${fieldId}').value = ${useUrl} ? url : pid;
    </c:if>
    }
</script>