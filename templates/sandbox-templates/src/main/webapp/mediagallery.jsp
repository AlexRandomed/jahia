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
<%@ include file="common/declarations.jspf" %>
<template:template>
    <template:templateHead>
        <!-- All headers and declarations global meta and css-->
        <%@ include file="common/head_externals.jspf" %>
    </template:templateHead>
    <template:templateBody gwtScript="mediagallery">
        <div id="bodywrapper">
            <div id="container"><!--start container-->
                <!-- Head page -->
                <template:include page="common/header.jsp"/>
            </div>
            <!--stop container-->
            <div id="container2"><!--start container2-->
                <div id="container3"><!--start container3-->
                    <div id="wrapper"><!--start wrapper-->
                        <div id="content4"><!--start content-->
                            <div class="spaceContent"><!--start spaceContent -->
                                <template:include page="common/breadcrumb.jsp"/>
                                <div class="box">
                                    <h2><c:out value="${requestScope.currentPage.highLightDiffTitle}"/></h2>

                                    <ui:mediaGallery name="myGallery"
                                                     inputName="selectedPath"
                                                     cssClassName="mediaGallery"/>
                                </div>
                            </div>
                            <!--stop space content-->
                        </div>
                        <!--stopContent-->
                    </div>
                    <!--stop wrapper-->
                    <div class="clear"></div>
                </div>
                <!--stop container2-->
                <!-- footer -->
                <template:include page="common/footer.jsp"/>
                <div class="clear"></div>
            </div>
            <!--stop container3-->
        </div>
    </template:templateBody>
</template:template>