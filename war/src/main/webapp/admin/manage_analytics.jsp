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
<%@ include file="/admin/include/header.inc" %>
<%@ page import="org.jahia.bin.JahiaAdministration,
                 java.util.Properties" %>
<%@ page import="java.util.Iterator" %>
<%@ page import="java.util.Set" %>
<%@ page import="org.jahia.services.sites.*" %>
<%@ page import="org.jahia.services.analytics.GoogleAnalyticsProfile" %>

<script type="text/javascript">

    function sendForm() {
        document.mainForm.submit();
    }
</script>
<div id="topTitle">
    <h1>Jahia</h1>

    <h2 class="edit"><fmt:message key="label.manageAnalytics"/>
        : <% if (currentSite != null) { %><fmt:message
                key="org.jahia.admin.site.label"/>&nbsp;<%=currentSite.getServerName() %><%} %></h2>
</div>

<div id="main">
    <table style="width: 100%;" class="dex-TabPanel" cellpadding="0" cellspacing="0">
        <tbody>
        <tr>
            <td style="vertical-align: top;" align="left">
                <%@include file="/admin/include/tab_menu.inc" %>
            </td>
        </tr>
        <tr>
            <td style="vertical-align: top;" align="left" height="100%">
                <div class="dex-TabPanelBottom">
                    <div class="tabContent">
                        <jsp:include page="/admin/include/left_menu.jsp">
                            <jsp:param name="mode" value="site"/>
                        </jsp:include>

                        <div id="content" class="fit">
                            <div class="head headtop">
                                <div class="object-title">
                                    <fmt:message key="label.manageAnalytics"/>
                                </div>
                            </div>
                            <!--div class="head headtop">
                                <div class="object-title">Google analytics tracking settings</div>
                            </div-->

                            <div id="operationMenu">
                    <span class="dex-PushButton">
                  <span class="first-child">
                    <a class="ico-delete"
                       href='<%=JahiaAdministration.composeActionURL(request,response,"analytics","&sub=delete&profile=all")%>'><fmt:message
                            key="label.delete"/></a>
                  </span>
                </span>

                            </div>
                            <div class="head headtop">
                                <div class="object-title">Existing profiles</div>
                            </div>
                            <form name="mainForm"
                                  action='<%=JahiaAdministration.composeActionURL(request,response,"analytics","&sub=commit")%>'
                                  method="post">
                                <table class="evenOddTable" border="0" cellpadding="5" cellspacing="0" width="100%">
                                    <thead>
                                    <tr>
                                        <th width="5%">
                                            &nbsp;
                                        </th>
                                        <th width="35%">
                                            <fmt:message
                                                    key="org.jahia.admin.site.ManageAnalytics.jahiaGAprofile.label"/>
                                        </th>
                                        <th width="35%">
                                            <fmt:message
                                                    key="org.jahia.admin.site.ManageAnalytics.gaProfileName.label"/>
                                        </th>
                                        <th width="12%" style="white-space: nowrap">
                                            <fmt:message key="org.jahia.admin.site.ManageAnalytics.gaUserAcc.label"/>
                                        </th>
                                        <th width="12%" style="white-space: nowrap">
                                            <fmt:message
                                                    key="org.jahia.admin.site.ManageAnalytics.realUrlsTracked.label"/>
                                        </th>
                                        <th width="12%" style="white-space: nowrap">
                                            <fmt:message
                                                    key="org.jahia.admin.site.ManageAnalytics.virtualUrlsTracked.label"/>
                                        </th>
                                        <th width="12%" style="white-space: nowrap">
                                            <fmt:message
                                                    key="org.jahia.admin.site.ManageAnalytics.trackingEnabled.label"/>
                                        </th>
                                        <th width="15%" class="lastCol">
                                            <fmt:message key="label.action"/>
                                        </th>
                                    </tr>
                                    </thead>
                                        <%
                                    
                                    Iterator<GoogleAnalyticsProfile> googleAnalyticsProfileIterator = currentSite.getGoogleAnalyticsProfil().iterator();
                                    String myClass = "evenLine";
                                    int cnt = 0;
                                    while(googleAnalyticsProfileIterator.hasNext())
                                    {
                                        GoogleAnalyticsProfile googleAnalyticsProfile = googleAnalyticsProfileIterator.next();


                                        if(cnt%2 == 0){
                                            myClass = "evenLine" ;
                                        }else{
                                            myClass = "oddLine" ;
                                        }
                                        String jahiaGAprofile = googleAnalyticsProfile.getProfile();

                                            %>
                                    <tr class="<%=myClass%>" id="<%=jahiaGAprofile%>">
                                        <td><input type="checkbox"/></td>
                                        <td><%=jahiaGAprofile%>
                                        </td>
                                        <td><%= googleAnalyticsProfile.getProfile()%>
                                        </td>
                                        <td><%=googleAnalyticsProfile.getAccount()%>
                                        </td>
                                        <td><input type="radio" value="real" name="<%=jahiaGAprofile%>TrackedUrls"
                                                   <% if (googleAnalyticsProfile.getTypeUrl().equals("real")) { %>checked<% } %>
                                                   id="<%=jahiaGAprofile%>TrackedUrls"/></td>
                                        <td><input type="radio" value="virtual" name="<%=jahiaGAprofile%>TrackedUrls"
                                                   <% if (googleAnalyticsProfile.getTypeUrl().equals("virtual")) { %>checked<% } %>
                                                   id="<%=jahiaGAprofile%>TrackedUrls"/></td>
                                        <td><input type="checkbox" name="<%=jahiaGAprofile%>TrackingEnabled"
                                                   <% if (googleAnalyticsProfile.isEnabled()) { %>checked<% } %>
                                                   id="<%=jahiaGAprofile%>TrackingEnabled"/></td>
                                        <td class="lastCol">
                                            <a href='<%=JahiaAdministration.composeActionURL(request,response,"analytics","&sub=displayEdit&profile="+jahiaGAprofile )%>'
                                               title="<fmt:message key='label.edit'/>"><img
                                                    src="<%=URL%>images/icons/admin/adromeda/edit.png"
                                                    alt="<fmt:message key='label.edit'/>"
                                                    title="<fmt:message key='label.edit'/>"
                                                    width="16"
                                                    height="16" border="0"/></a>&nbsp;
                                            <a href='<%=JahiaAdministration.composeActionURL(request,response,"analytics","&sub=delete&profile="+jahiaGAprofile )%>'
                                               title="<fmt:message key='label.delete'/>"><img
                                                    src="<%=URL%>images/icons/admin/adromeda/delete.png"
                                                    alt="<fmt:message key='label.delete'/>"
                                                    title="<fmt:message key='label.delete'/>"
                                                    width="16"
                                                    height="16" border="0"/></a>&nbsp;
                                        </td>
                                    </tr>
                                        <%
                                        cnt++;

                                }
                                %>
                            </form>
                            <table>
                                <tbody>

                                <div class="head headtop">
                                    <div class="object-title"><fmt:message
                                            key="org.jahia.admin.site.ManageAnalytics.jahiaAnalyticsProfile.label"/>
                                    </div>
                                </div>
                                <tr>
                                    <td width="88%">
                                        <fmt:message key="label.jahiaGAprofile"/>
                                        <table class="evenOddTable" border="0" cellpadding="5" cellspacing="0"
                                               width="100%">
                                            <tr class="evenLine">
                                                <td><fmt:message
                                                        key="org.jahia.admin.site.ManageAnalytics.jahiaGAprofile.label"/>
                                                </td>
                                                <td>
                                                    <fmt:message key="label.jahiaGAprofileName"/>
                                                </td>
                                            </tr>
                                            <tr class="oddLine">
                                                <td><fmt:message
                                                        key="org.jahia.admin.site.ManageAnalytics.gaUserAcc.label"/>
                                                </td>
                                                <td>
                                                    <fmt:message
                                                            key="org.jahia.admin.site.ManageAnalytics.description.gaUserAcc.label"/>
                                                </td>
                                            </tr>
                                            <tr class="evenLine">
                                                <td><fmt:message
                                                        key="org.jahia.admin.site.ManageAnalytics.gaProfileName.label"/>
                                                </td>
                                                <td><fmt:message
                                                        key="org.jahia.admin.site.ManageAnalytics.description.gaProfileName.label"/>
                                                </td>
                                            </tr>
                                            <tr class="oddLine">
                                                <td><fmt:message
                                                        key="org.jahia.admin.site.ManageAnalytics.gaCredentials.label"/>
                                                </td>
                                                <td>
                                                    <fmt:message
                                                            key="org.jahia.admin.site.ManageAnalytics.description.gaCredentials.label"/>
                                                </td>
                                            </tr>
                                            <tr class="oddLine">
                                                <td><fmt:message
                                                        key="org.jahia.admin.site.ManageAnalytics.trackedUrls.label"/>
                                                </td>
                                                <td>
                                                    <fmt:message
                                                            key="org.jahia.admin.site.ManageAnalytics.description.trackedUrls.label"/>
                                                </td>
                                            </tr>
                                        </table>

                                    <td>
                                        <span class="dex-PushButton">
                                            <span class="first-child">
                                              <a class="ico-add"
                                                 href="<%=JahiaAdministration.composeActionURL(request,response,"analytics","&sub=new")%>"><fmt:message
                                                      key="label.add"/></a>
                                            </span>
                                        </span>
                                    </td>
                                </tr>
                            </table>


                            <div id="actionBar">
                            <span class="dex-PushButton">
                              <span class="first-child">
                                <a class="ico-back"
                                   href='<%=JahiaAdministration.composeActionURL(request,response,"displaymenu","")%>'><fmt:message
                                        key="label.backToMenu"/></a>
                              </span>
                            </span>
                           <span class="dex-PushButton">
                            <span class="first-child">
                              <a class="ico-ok" href="javascript:sendForm();"><fmt:message key="label.save"/></a>
                            </span>
                          </span>

                            </div>
                        </div>
                    </div>
                    <%@include file="/admin/include/footer.inc" %>
