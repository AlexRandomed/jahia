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

<%@page language = "java" %>
<%@page import = "java.util.*"%>
<%@page import="org.jahia.bin.*"%>
<%@page import="org.jahia.params.ProcessingContext"%>
<%@page import = "org.jahia.data.JahiaData"%>
<%@taglib uri="http://www.jahia.org/tags/internalLib" prefix="internal" %>

<jsp:useBean id="URL" class="java.lang.String" scope="request"/>   <% // http files path. %>
<jsp:useBean id="userMessage" class="java.lang.String" scope="session"/>

<%
    String userSearch = (String)request.getAttribute("userSearch");
    String currentSite = (String)request.getAttribute("currentSite");
    JahiaData jData = (JahiaData)request.getAttribute("org.jahia.data.JahiaData");
    int stretcherToOpen   = 1;
%>

<!-- For future version : <script language="javascript" src="../search_options.js"></script> -->
<script type="text/javascript" src="<%=URL%>../javascript/selectbox.js"></script>
<script type="text/javascript">

function submitForm(action)
{
    document.mainForm.action = '<%=JahiaAdministration.composeActionURL(request,response,"users","&sub=")%>' + action;
    document.mainForm.method = "post";
    document.mainForm.submit();
}

function handleKey(e)
{
    if (e.altKey && e.ctrlKey) {
        submitForm('remove');
    } else if (e.altKey) {
        submitForm('membership');
    } else if (e.ctrlKey) {
        submitForm('copy');
    } else {
        submitForm('edit');
    }
}

function handleKeyCode(code)
{
    if (code == 46) {
        submitForm('remove');
    } else if (code == 45) {
        submitForm('create');
    } else if (code == 13) {
        submitForm('edit');
    }
}

function setFocus()
{
    document.mainForm.searchString.focus();
}

</script>

<div id="topTitle">
<h1>Jahia</h1>
<h2 class="edit"><internal:adminResourceBundle resourceName="org.jahia.admin.manageUsers.label"/> : <%= currentSite%></h2>
</div>
<div id="main">
<table style="width: 100%;" class="dex-TabPanel" cellpadding="0"
    cellspacing="0">
    <tbody>
        <tr>
            <td style="vertical-align: top;" align="left">
                <%@include file="/jsp/jahia/administration/include/tab_menu.inc"%>
            </td>
        </tr>
        <tr>
            <td style="vertical-align: top;" align="left" height="100%">
            <div class="dex-TabPanelBottom">
            <div class="tabContent">
                <%@include file="/jsp/jahia/administration/include/menu_site.inc"%>
            
            <div id="content" class="fit">
            <div class="head">
                <div class="object-title">
                <internal:adminResourceBundle resourceName="org.jahia.admin.manageUsers.label"/>
                </div>
            </div>
            <div class="content-body">
            <!-- User operations -->
            <div id="operationMenu">
                <span class="dex-PushButton"> 
                    <span class="first-child">                  
                    <a class="ico-user-add" href="javascript:submitForm('create');"><internal:adminResourceBundle resourceName="org.jahia.admin.users.ManageUsers.createNewUser.label"/></a>
                    </span> 
                </span>
                <span class="dex-PushButton"> 
                    <span class="first-child">                  
                    <a class="ico-user-reg" href="javascript:submitForm('register');"><internal:adminResourceBundle resourceName="org.jahia.admin.users.ManageUsers.registerExistingUser.label"/></a>
                    </span> 
                </span>
                <span class="dex-PushButton"> 
                    <span class="first-child">                  
                    <a class="ico-user-view" href="javascript:submitForm('edit');"><internal:adminResourceBundle resourceName="org.jahia.admin.users.ManageUsers.editViewProp.label"/></a>
                    </span> 
                </span>
                <span class="dex-PushButton"> 
                    <span class="first-child">                  
                    <a class="ico-user-delete" href="javascript:submitForm('remove');"><internal:adminResourceBundle resourceName="org.jahia.admin.users.ManageUsers.removeSelectedUser.label"/></a>
                    </span> 
                </span>                           
            </div>
            </div>
                <div class="head">
                    <div class="object-title">
                    <internal:adminResourceBundle resourceName="org.jahia.admin.users.ManageUsers.userList.label"/>
                    </div>
                </div>
             <%
            if ( userMessage.length()>0 ){
            %>        
              <p class="${not isError ? 'blueColor' : 'errorbold'}">
                <%=userMessage%>
              </p>
            <% } %>

<form name="mainForm" action="" method="post">
<!-- User management -->
    <table border="0" style="width:100%">
        <tr>
            <td>

            <jsp:include page="<%=userSearch%>" flush="true"/>

            </td>
        </tr>
        
    </table>
<!-- -->



</form>


	</div>		
			</td>
		</tr>
	</tbody>
</table>
</div>
  <div id="actionBar">
    <span class="dex-PushButton"> 
	  <span class="first-child">
      	 <a class="ico-back" href='<%=JahiaAdministration.composeActionURL(request,response,"displaymenu","")%>'><internal:adminResourceBundle resourceName="org.jahia.admin.backToMenu.label"/></a>
      </span>
     </span> 	      
  </div>

</div>
<script language="javascript">
    setFocus();
</script>

