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
<%@ tag body-content="empty" description="Renders the link to the path selection engine (as a popup window)." %>
<%@ attribute name="fieldId" required="true" type="java.lang.String"
              description="The input field name and ID to synchronize the selected path value with" %>
<%@ attribute name="useUrl" required="false" type="java.lang.Boolean"
              description="If set to true the selected folder URL will be used in the field value; otherwise the path of the selected folder will be used (default)." %>
<%@ attribute name="rootPath" required="false" type="java.lang.String"
              description="The path to start with. So the selection will be available for subfolders of the specified root directory" %>
<%@ attribute name="startPath" required="false" type="java.lang.String"
              description="The path of the directory that will be expanded by default" %>
<%@ attribute name="filters" required="false" type="java.lang.String"
              description="Comma-separated list of filter patterns to be applied on the displayed resources If both filters and mimeTypes are specified they are applied one after another." %>
<%@ attribute name="mimeTypes" required="false" type="java.lang.String"
              description="Comma-separated list of MIME types for files to be displayed. If both filters and mimeTypes are specified they are applied one after another." %>
<%@ attribute name="onSelect" required="false" type="java.lang.String"
              description="The JavaScript function to be called after a location is selected. The selected path will be passed as an argument to this function. If the function returns true, the value will be also set into the field value. Otherwise nothing will be done by this tag." %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn" %>
<%@ taglib prefix="utility" uri="http://www.jahia.org/tags/utilityLib" %>
<c:set var="displayIncludeChildren" value="${not empty displayIncludeChildren ? displayIncludeChildren : 'true'}"/>
<c:if test="${empty fieldIdIncludeChildren}"><c:set var="fieldIdIncludeChildren"
                                                    value="${fieldId}_includeChildren"/></c:if>
<c:set var="fieldIdHash"><%= Math.abs(jspContext.getAttribute("fieldId").hashCode()) %>
</c:set>
<c:set var="useUrl" value="${not empty useUrl ? useUrl : 'false'}"/>
&nbsp;<a href="#select"
onclick="javascript:{var fileSelector = window.open('${pageContext.request.contextPath}/jsp/jahia/engines/webdav/filePicker.jsp?callback=setSelectedFile${fieldIdHash}&amp;rootPath=${rootPath}&amp;startPath=${startPath}&amp;filters=${filters}&amp;mimeTypes=${mimeTypes}', '<%="fileSelector" + session.getId().replaceAll("[^a-zA-Z0-9]", "_")%>', 'resizable,height=800,width=800'); fileSelector.focus(); return false;}"
title='<utility:resourceBundle resourceBundle="JahiaEnginesResources" resourceName="org.jahia.engines.search.selectFolder"
                                      defaultValue="Select folder"/>'><utility:resourceBundle resourceBundle="JahiaEnginesResources"
        resourceName="org.jahia.engines.search.select" defaultValue="select"/></a>
<script type="text/javascript">
    function setSelectedFile${fieldIdHash}(path, url) {
    <c:if test="${not empty onSelect}">
        if ((${onSelect})(path, url)) {
            document.getElementById('${fieldId}').value =
        ${useUrl} ?
            url : path;
        }
    </c:if>
    <c:if test="${empty onSelect}">
        document.getElementById('${fieldId}').value =
    ${useUrl} ?
        url : path;
    </c:if>
    }
</script>