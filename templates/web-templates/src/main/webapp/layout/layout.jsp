<%--

    
    This file is part of Jahia: An integrated WCM, DMS and Portal Solution
    Copyright (C) 2002-2009 Jahia Limited. All rights reserved.
    
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
    in Jahia's FLOSS exception. You should have recieved a copy of the text
    describing the FLOSS exception, and it is also available here:
    http://www.jahia.com/license
    
    Commercial and Supported Versions of the program
    Alternatively, commercial and supported versions of the program may be used
    in accordance with the terms contained in a separate written agreement
    between you and Jahia Limited. If you are unsure which license is appropriate
    for your use, please contact the sales department at sales@jahia.com.

--%>

<%@ page language="java" contentType="text/html;charset=UTF-8" %>

<%@ include file="../common/declarations.jspf" %>
<c:set var="useGWT" value="false"/>
<c:if test="${ !empty param.useGWT }">
    <c:set var="useGWT" value="true"/>
</c:if>
<template:template gwtForGuest="${useGWT}">
    <template:templateHead>
        <template:meta name="keywords" metadata="keywords"/>
        <template:meta name="description" metadata="description"/>
        <!-- All headers and declarations global meta and css-->
        <%@ include file="../common/head.jspf" %>
        <utility:applicationResources/>
        <c:if test="${ !empty param.rssFeed }">
            <link rel="alternate" type="application/rss+xml" title="web templates : news"
                  href="${currentPage.url}/${param.rssFeed}"/>
        </c:if>
    </template:templateHead>
    <template:templateBody gwtScript="${param.gwtScript}">
        <div id="bodywrapper"><!--start bodywrapper-->
            <div id="container">
                <%@ include file="../common/top.jspf" %>
                <div id="page"><!--start page-->
                    <div id="headerPart2"><!--start headerPart2-->
                        <div id="logotop">
                            <template:include page="common/logo.jsp"/>
                        </div>
                        <h1 class="hide">Nom du site</h1>
                        <%@ include file="../common/nav/nav.jspf" %>
                    </div>
                    <c:if test="${ !empty param.illustration }">
                        <div id="illustration"><!--start illustration-->
                            <img src="${pageContext.request.contextPath}/jsp/jahia/templates/web-templates/theme/${requestScope.currentTheme}/img/illustration.png"
                                 alt="illustration"/>
                        </div>
                    </c:if>
                    <!--stop illustration-->
                    <div id="containerdata"><!--start containerdata-->
                        <div id="wrapper"><!--start wrapper-->
                                <%--rssfeed may be need for some template which need it--%>
                            <c:if test="${ !empty param.spaceContent }">
                                <template:include page="${param.spaceContent}">
                                    <template:param name="rssFeed" value="${param.rssFeed}"/>
                                </template:include>
                            </c:if>
                        </div>
                        <!--stop wrapper-->
                        <c:if test="${ !empty param.insetA }">
                            <div id="InsetA"><!--start InsetB-->
                                <div class="space"><!--start space InsetB -->
                                    <template:include page="${param.insetA}"/>
                                </div>
                            </div>
                        </c:if>
                        <c:if test="${ !empty param.insetB }">
                            <div id="InsetB"><!--start InsetB-->
                                <div class="space"><!--start space InsetB -->
                                    <template:include page="${param.insetB}"/>
                                </div>
                                <!--stop space InsetB -->
                            </div>
                            <!-- stop InsetB-->
                        </c:if>
                        <c:if test="${ !empty param.insetC }">
                            <div id="InsetC"><!--start InsetC-->
                                <div class="space"><!--start space InsetC -->
                                    <template:include page="${param.insetC}"/>
                                </div>
                                <!--stop InsetC-->
                            </div>
                            <!--stop space InsetC -->
                        </c:if>
                        <div class="clear"></div>
                    </div>
                    <%@ include file="../common/footer.jspf" %>
                    <div class="clear"></div>
                </div>
                <div class="clear"></div>
            </div>
            <div class="clear"></div>
        </div>
    </template:templateBody>
</template:template>