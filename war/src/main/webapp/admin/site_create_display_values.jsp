<%@ page import="org.jahia.params.ParamBean" %>
<%--

    This file is part of Jahia: An integrated WCM, DMS and Portal Solution
    Copyright (C) 2002-2011 Jahia Solutions Group SA. All rights reserved.

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

<%@ page import="org.jahia.registries.ServicesRegistry" %>
<%@ include file="/admin/include/header.inc" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%!
    private String getRequestAttr(HttpServletRequest request, String name) {
        String value = (String) request.getAttribute(name);
        if (value == null || value.length() == 0) {
            value = "--";
        }
        return value;
    }
%>
<%
    final ParamBean jParams = (ParamBean) request.getAttribute("org.jahia.params.ParamBean");
    final String readmefilePath = response.encodeURL(new StringBuffer().append(request.getContextPath()).append("/html/startup/readme.html").toString());
    
    // site
    String siteTitle = getRequestAttr(request, "siteTitle");
    String siteServerName = getRequestAttr(request, "siteServerName");
    String siteDescr = getRequestAttr(request, "siteDescr");
    String siteKey = getRequestAttr(request, "siteKey");
    String templateName = getRequestAttr(request, "templateName");
    Locale selectedLocale = (Locale) request.getAttribute("selectedLocale");
    String selectedTmplSet = getRequestAttr(request, "selectedTmplSet");
    String selectedTheme = getRequestAttr(request, "selectedTheme");
    String selectedThemeName = getRequestAttr(request, "selectedThemeName");
    Boolean defaultSite = (Boolean) request.getAttribute("defaultSite");

    // admin user
    String siteAdminOption = getRequestAttr(request, "siteAdminOption");
    String adminUsername = getRequestAttr(request, "adminUsername");
    String adminFirstName = getRequestAttr(request, "adminFirstName");
    String adminLastName = getRequestAttr(request, "adminLastName");
    String adminEmail = getRequestAttr(request, "adminEmail");
    String adminOrganization = getRequestAttr(request, "adminOrganization");



    String gaUserAccount = getRequestAttr(request,"gaUserAccountCustom");
    String gaProfile = getRequestAttr(request,"gaProfileCustom");
    String gaUserAccountDefConf = getRequestAttr(request,"gaUserAccountDefault");
    String gaProfileDefConf = getRequestAttr(request,"gaProfileDefault");
    String gmailAccount = getRequestAttr(request,"gmailAccount");
    String gmailPassword = getRequestAttr(request,"gmailPassword");
    Boolean trackingEnabled = (Boolean) request.getAttribute("trackingEnabled");



    stretcherToOpen   = 0;

%>
<script type="text/javascript">
    <!--

    function sendForm(){
        document.jahiaAdmin.submit();
    }

    function setWaitingCursor() {
                workInProgressOverlay.launch();
           }

        function openReadmeFile() {
                var params = "width=1100,height=500,left=0,top=0,resizable=yes,scrollbars=yes,status=no";
                window.open('<%=readmefilePath%>', 'Readme', params);
        }

    -->
</script>
<div id="topTitle">
<h1>Jahia</h1>
<h2 class="edit">
      <fmt:message key="org.jahia.admin.site.ManageSites.manageVirtualSites.label"/>
</h2>
</div>

<div id="main">
<table style="width: 100%;" class="dex-TabPanel" cellpadding="0"
	cellspacing="0">
	<tbody>
        <tr>
			<td style="vertical-align: top;" align="left">
				<%@include file="/admin/include/tab_menu.inc"%>
			</td>
		</tr>
		<tr>
			<td style="vertical-align: top;" align="left" height="100%">
            <div class="dex-TabPanelBottom">
			<div class="tabContent">
                <jsp:include page="/admin/include/left_menu.jsp">
                    <jsp:param name="mode" value="server"/>
                </jsp:include>
			
			<div id="content" class="fit">
            <div class="head headtop">
                <div class="object-title">
                     <fmt:message key="org.jahia.createSite.button"/>
                </div>
    <div  class="object-shared">
        <fmt:message key="label.step"/> 3 / 3
    </div>
             </div>

      <div class="content-body">
      
<form name="jahiaAdmin" action='<%=JahiaAdministration.composeActionURL(request,response,"sites","&sub=createsite")%>'
      method="post" enctype="multipart/form-data">
<table class="evenOddTable" border="0" cellpadding="5" cellspacing="0" style="width:100%">
    <tr>
        <th colspan="2"class="lastCol" align="left"><fmt:message key="org.jahia.admin.site.ManageSites.newsite.properties.values"/></th>
    </tr>
    <tr class="evenLine">
        <td class="t3" style="width: 40%; ">
            <fmt:message key="org.jahia.admin.site.ManageSites.siteKey.label"/>&nbsp;
        </td>
        <td headers="t2" class="lastCol">
            &nbsp;<%=siteKey%>
        </td>
    </tr>
    <tr class="oddLine">
        <td class="t3" >
            <fmt:message key="org.jahia.admin.site.ManageSites.siteTitle.label"/>&nbsp;
        </td>
        <td headers="t2" class="lastCol">
            &nbsp;<%=siteTitle%>
        </td>
    </tr>
    <tr class="evenLine">
        <td class="t3" >
            <fmt:message key="org.jahia.admin.site.ManageSites.siteServerName.label"/>&nbsp;
        </td>
        <td headers="t2" class="lastCol">
            &nbsp;<%=siteServerName%>
        </td>
    </tr>
    <tr class="oddLine">
        <td class="t3">
            <fmt:message key="org.jahia.admin.site.ManageSites.setAsDefaultSite.label"/>&nbsp;
        </td>
        <td headers="t2" class="lastCol">
            &nbsp;<c:if test="${defaultSite}"><fmt:message key="org.jahia.admin.yes.label"/></c:if><c:if test="${!defaultSite}"><fmt:message key="label.no"/></c:if>
        </td>
    </tr>
    <tr class="evenLine">
        <td class="t3" >
            <fmt:message key="org.jahia.admin.site.ManageSites.siteDesc.label"/>&nbsp;
        </td>
        <td class="lastCol">
            &nbsp;<%=siteDescr%>
        </td>
    </tr>
    <tr class="oddLine">
        <td class="t3" >
            <fmt:message key="org.jahia.admin.site.ManageSites.templateSet.label"/>&nbsp;
        </td>
        <td class="lastCol">
            &nbsp;<%=templateName%>
            <input type="hidden" name="selectTmplSet" value="<%=selectedTmplSet%>"/>
        </td>
    </tr>
    <tr class="evenLine">
        <td class="t3" >
            <fmt:message key="org.jahia.admin.site.ManageSites.theme.label"/>&nbsp;
        </td>
        <td class="lastCol">
            &nbsp;<%=selectedThemeName%>
            <input type="hidden" name="selectTheme" value="<%=selectedTheme%>"/>
        </td>
    </tr>
    <tr class="oddLine">
        <td class="t3" >
            <fmt:message key="label.language"/>&nbsp;
        </td>
        <td class="lastCol">
            &nbsp;<%=selectedLocale.getDisplayName(jParams.getUILocale())%>
            <input type="hidden" name="languageList" value="<%=selectedLocale%>"/>
        </td>
    </tr>
</table>
<%
if (!"2".equals(siteAdminOption)) {
%>
<table border="0" cellpadding="5" cellspacing="0" style="width:100%" class="evenOddTable">
    <tr>
        <th class="lastCol" colspan="2" align="left"><fmt:message key="org.jahia.admin.site.ManageSites.newsite.administratorAccount.values"/></th>
    </tr>
    <tr class="evenLine">
        <td class="t3" style="width: 40%; ">
            <fmt:message key="label.username"/>&nbsp;
        </td>
        <td class="lastCol">
            &nbsp;<%=adminUsername%>
        </td>
    </tr>
    <tr class="oddLine">
        <td class="t3" >
            <fmt:message key="org.jahia.admin.firstName.label"/>&nbsp;
        </td>
        <td class="lastCol">
            &nbsp;<%=adminFirstName%>
        </td>
    </tr>
    <tr class="evenLine">
        <td class="t3" >
            <fmt:message key="org.jahia.admin.lastName.label"/>&nbsp;
        </td>
        <td class="lastCol">
            &nbsp;<%=adminLastName%>
        </td>
    </tr>
    <tr class="oddLine">
        <td class="t3" >
            <fmt:message key="label.email"/>&nbsp;
        </td>
        <td class="lastCol">
            &nbsp;<%=adminEmail%>
        </td>
    </tr>
    <tr class="evenLine">
        <td class="t3" >
            <fmt:message key="org.jahia.admin.organization.label"/>&nbsp;
        </td>
        <td class="lastCol">
            &nbsp;<%=adminOrganization%>
        </td>
    </tr>
    <tr class="oddLine">
        <td class="t3" >
            <fmt:message key="label.password"/>&nbsp;
        </td>
        <td class="lastCol">
            &nbsp;********************
        </td>
    </tr>
</table>
<%
        }
%>
</div>
</form>
</div>
</div>
            
            </td>
        </tr>
    </tbody>
</table>
</div>
<div id="actionBar">
  
    <span class="dex-PushButton"> 
      <span class="first-child">
         <a class="ico-back"
               href='<%=JahiaAdministration.composeActionURL(request,response,"sites","&sub=list")%>' onclick="showWorkInProgress(); return true;"><fmt:message key="org.jahia.admin.site.ManageSites.backToSitesList.label"/></a>
      </span>
     </span>
      
        <span class="dex-PushButton"> 
            <span class="first-child">                  
             <a class="ico-ok" href="javascript:sendForm();" onclick="showWorkInProgress(); return true;">
                <fmt:message key="org.jahia.createSite.button"/>
            </a>
            </span> 
        </span>
        </div>
</div>

<%@ include file="/admin/include/footer.inc" %>
