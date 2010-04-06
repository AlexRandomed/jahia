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
<%@ page language="java" contentType="text/html;charset=UTF-8" %>

<%@ include file="../../common/declarations.jspf" %>
<template:addResources type="javascript" resources="swfobject.js,textsizer.js,collapse.js"/>
<template:addResources type="css" resources="01web.css" insert="true"/>
<div id="bodywrapper">
    <jcr:node var="rootPage" path="/sites/${renderContext.site.siteKey}/home"/>
    <div id="contentArea">
        <div id="toplinks">

            <div id="languages">
                <ui:langBar display="horizontal" linkDisplay="flag" rootPage="${rootPage}"/>
            </div>

            <div id="topshortcuts">
                <ul>
                    <c:if test="${requestScope.currentRequest.logged}">
                        <li class="topshortcuts-login">
                            <a class="loginFormTopLogoutShortcuts"
                               href="<template:composePageURL page="logout"/>"><span><fmt:message
                                    key="web_templates.logout"/></span></a>
                        </li>
                        <li>
                            <span class="currentUser"><utility:userProperty/></span>
                        </li>
                        <li class="topshortcuts-mysettings">
                            <a href="${url.userProfile}"><fmt:message key="web_templates.mySettings.title"/></a>
                        </li>
                        <li class="topshortcuts-edit">
                            <a href="${url.edit}"><fmt:message key="edit"/></a>
                        </li>
                    </c:if>
                    <li class="topshortcuts-print"><a href="base.wrapper.bodywrapper.jsp#"
                                                      onclick="javascript:window.print()">
                        <fmt:message key="label.print"/></a>
                    </li>
                    <li class="topshortcuts-typoincrease">
                        <a href="javascript:ts('body',1)"><fmt:message key="label.font.up"/></a>
                    </li>
                    <li class="topshortcuts-typoreduce">
                        <a href="javascript:ts('body',-1)"><fmt:message key="label.font.down"/></a>
                    </li>
                    <li class="topshortcuts-home">
                        <a href="${url.base}${rootPage.path}.html"><fmt:message key="label.home"/></a>
                    </li>
                    <li class="topshortcuts-sitemap">
                        <a href="${url.base}${rootPage.path}.sitemap.html"><fmt:message
                                key="label.sitemap"/></a>
                    </li>
                </ul>
            </div>
            <div class="clear"></div>


        </div>
        <div id="page">
            <div id="pageHeader">
                <div id="logotop">
                    <%--<jcr:node var="logo" path="${rootPage.path}/logo"/>--%>
                    <template:area path="${rootPage.path}/logo"/>
                </div>
                <h1 class="hide">${renderContext.site.title} : ${renderContext.mainResource.node.properties['jcr:title']}</h1>


                <template:area path="${rootPage.path}/topMenu" />
            </div>

            ${wrappedContent}

            <div id="footer">
                <c:if test="${ !empty param.footerNav}">
                    <div id="footerPart1"><!--start footerPart1-->
                        <div class="columns5">
                            <template:area path="${rootPage.path}/bottomLinks"/>
                            <div class="clear"></div>
                        </div>
                    </div>
                </c:if>
                <!--stop footerPart1-->
                <!--start footerPart2-->
                <div id="footerPart2"><!--start footerPart2content-->
                    <div id="footerPart2content"></div>
                    <div class="clear"></div>
                </div>
                <!--stop footerPart2-->
                <!--start footerPart3-->
                <div id="footerPart3">
                    <!--start 2columns -->
                    <div class="columns2">
                        <!--start column-item -->
                        <div class="column-item1">
                            <div class="spacer">
                                <template:area path="${rootPage.path}/logoFooter"/>
                            </div>
                            <div class="clear"></div>
                        </div>
                        <!--stop column-item -->
                        <!--start column-item -->
                        <div class="column-item2">
                            <div class="spacer"><!--start bottomshortcuts-->
                                <div id="bottomshortcuts">
                                </div>
                                <!--stop bottomshortcuts-->
                                <div class="clear"></div>
                                <!--start copyright-->
                                <div id="copyright">
                                    <p>
                                        <template:area path="${rootPage.path}/footerContainerList"/>
                                    </p>
                                </div>
                                <!--stop copyright-->
                            </div>
                            <div class="clear"></div>
                        </div>
                        <!--stop column-item -->

                        <div class="clear"></div>
                    </div>
                    <!--stop 2 columns -->

                    <div class="clear"></div>
                </div>
                <!--stop footerPart3-->

                <div class="clear"></div>
            </div>

            <div class="clear"></div>
        </div>
        <div class="clear"></div>
    </div>
    <div class="clear"></div>
</div>
