<%--

    This file is part of Jahia: An integrated WCM, DMS and Portal Solution
    Copyright (C) 2002-2009 Jahia Solutions Group SA. All rights reserved.

    This program is free software; you can redistribute it and/or
    modify it under the terms of the GNU General Public License
    as published by the Free Software Foundation; either version 2
    of the License, or (at your option) any later version.

    This program is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
    GNU General Public License for more details.

    You should have received a copy of the GNU General Public License
    along with this program; if not, write to the Free Software
    Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301, USA.

    As a special exception to the terms and conditions of version 2.0 of
    the GPL (or any later version), you may redistribute this Program in connection
    with Free/Libre and Open Source Software ("FLOSS") applications as described
    in Jahia's FLOSS exception. You should have received a copy of the text
    describing the FLOSS exception, and it is also available here:
    http://www.jahia.com/license

    Commercial and Supported Versions of the program
    Alternatively, commercial and supported versions of the program may be used
    in accordance with the terms contained in a separate written agreement
    between you and Jahia Solutions Group SA. If you are unsure which license is appropriate
    for your use, please contact the sales department at sales@jahia.com.

--%>
<%@ tag body-content="scriptless"
        description="Renders a date picker control using corresponding jQuery UI component (http://docs.jquery.com/UI/Datepicker). Datepicker options (see http://docs.jquery.com/UI/Datepicker#options) can be specified as a body of this tag in form {option1: value1, option2: value2 ...}. The template module that will use this tag should have Default Jahia Templates module as a dependency." %>
<%@ attribute name="fieldId" required="true" type="java.lang.String"
              description="The input field ID to bind the date picker to." %>
<%@ attribute name="theme" required="false" type="java.lang.String"
              description="The name of the CSS file with corresponding jQuery theme. [jquery-ui.smoothness.css,jquery-ui.smoothness-jahia.css]" %>
<%@ attribute name="time" required="false" type="java.lang.Boolean"
              description="True if you want the time selector" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="template" uri="http://www.jahia.org/tags/templateLib" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<c:if test="${empty requestScope['org.jahia.tags.dateSelector.resources']}">
    <template:addResources type="css" resources="${not empty theme ? theme : 'jquery-ui.smoothness.css,jquery-ui.smoothness-jahia.css'}"/>
    <template:addResources type="javascript"
                           resources="jquery.min.js,jquery-ui-1.7.3.custom.min.js,jquery-ui.datepicker.min.js,jquery-ui.slider.min.js"/>
    <c:set var="locale" value="${renderContext.mainResource.locale}"/>
    <c:if test="${locale != 'en_US'}">
        <template:addResources type="javascript" resources="i18n/jquery.ui.datepicker-${locale.language}.js"/>
        <c:if test="${not empty locale.country}">
            <template:addResources type="javascript"
                                   resources="i18n/jquery.ui.datepicker-${locale.language}-${locale.country}.js"/>
        </c:if>
    </c:if>
    <c:if test="${not empty time and time eq true}">
        <template:addResources type="javascript" resources="timepicker.js"/>
        <template:addResources type="css" resources="timepicker.css"/>
    </c:if>
    <c:set var="org.jahia.tags.dateSelector.resources" value="true" scope="request"/>
</c:if>
<jsp:doBody var="options"/>
<c:if test="${empty options}">
    <c:set var="options" value="{dateFormat: $.datepicker.ISO_8601, showButtonPanel: true, showOn: 'both'}"/>
</c:if>
<script type="text/javascript">
    /* <![CDATA[ */
    <c:if test="${empty time or time eq false}">
    $(document).ready(function() {
        $('#${fieldId}').datepicker(${options});
    });
    </c:if>
    <c:if test="${not empty time and time eq true}">
    $(document).ready(function() {
        $('#${fieldId}').datetime(${options});
    });
    </c:if>
    /* ]]> */
</script>