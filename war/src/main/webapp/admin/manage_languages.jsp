<%@page import="org.jahia.bin.Jahia,org.jahia.utils.LanguageCodeConverters,java.util.Iterator,java.util.Locale,java.util.Set" %>
<%@include file="/admin/include/header.inc" %>
<%
    Set<String> languageSet = (Set<String>) request.getAttribute("languageSet");
    Set<String> mandatoryLanguageSet = (Set<String>) request.getAttribute("mandatoryLanguageSet");
    Boolean mixLanguages = (Boolean) request.getAttribute("mixLanguages");
    String defaultLanguage = (String) request.getAttribute("defaultLanguage");
    Locale currentLocale = Jahia.getThreadParamBean().getUILocale();
%>
<script type="text/javascript" language="javascript">
    <!--

    function sendForm() {
        showWorkInProgress();
        document.mainForm.submit();
    }

    //-->
</script>
<div id="topTitle">
    <h1>Jahia</h1>

    <h2 class="edit"><fmt:message
            key="label.manageLanguages"/>: <% if (currentSite != null) { %><fmt:message
            key="org.jahia.admin.site.label"/>&nbsp;<%=currentSite.getTitle() %>&nbsp;&nbsp;<%} %></h2>
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
                            <div class="head">
                                <div class="object-title">
                                    <fmt:message
                                            key="org.jahia.admin.languages.ManageSiteLanguages.configuredLanguages.label"/>&nbsp;
                                </div>
                            </div>
                            <div class="content-body">
                                <div id="operationMenu">
                                    * = <fmt:message
                                        key="org.jahia.admin.languages.ManageSiteLanguages.languagesUsedByHomePage.label"/>&nbsp;
                                </div>
                            </div>
                            <form name="mainForm"
                                  action='<%=JahiaAdministration.composeActionURL(request,response,"siteLanguages","&sub=commit")%>'
                                  method="post">
                                <table class="evenOddTable" border="0" cellpadding="5" cellspacing="0"
                                       style="width: 100%">
                                    <thead>
                                    <tr>
                                        <th>
                                            <fmt:message
                                                    key="label.language"/>
                                        </th>
                                        <th style="text-align:center">
                                            <fmt:message
                                                    key="org.jahia.admin.languages.ManageSiteLanguages.mandatory.label"/>
                                        </th>
                                        <th style="text-align:center">
                                            <fmt:message
                                                    key="org.jahia.admin.languages.ManageSiteLanguages.default.label"/>
                                        </th>
                                        <th style="text-align:center"  ><fmt:message key="label.delete"/></th>
                                    </tr>
                                    </thead>
                                    <tbody>
                                    <%
                                        int count = 0;
                                        if (languageSet.size() == 0) { %>
                                    <tr>
                                        <td colspan="5">
                                            <b><fmt:message
                                                    key="org.jahia.admin.languages.ManageSiteLanguages.noLanguageDefined.label"/></b>
                                        </td>
                                    </tr>
                                    <%
                                    } else {
                                        for (String lang : languageSet) {
                                            count++;
                                            Locale curLocale = LanguageCodeConverters.languageCodeToLocale(lang);
                                            if (count % 2 == 0) { %>
                                    <tr class="evenLine">
                                            <%
                                            } else { %>
                                    <tr class="oddLine">
                                        <%
                                            } %>
                                        <td align="left">
                                            <%=curLocale.getDisplayName(currentLocale) %>(<%=curLocale.toString() %>
                                            )
                                        </td>
                                        <td align="center">
                                            <% if (mandatoryLanguageSet.contains(lang)) { %>
                                            <input type="checkbox" name="mandatoryLanguages" value="<%=lang%>"
                                                   checked><% } else { %>
                                            <input type="checkbox" name="mandatoryLanguages" value="<%=lang%>"><% } %>
                                        </td>
                                        <td align="center">
                                            <input type="radio" name="defaultLanguage" value="<%=lang%>"
                                                   <% if (lang.equals(defaultLanguage)) {%>checked="true"<% } %>>
                                        </td>
                                        <td align="center">
                                            <input type="checkbox" name="deletedLanguages" value="<%=lang%>">
                                        </td>
                                    </tr>
                                    <%
                                            }
                                        } %>
                                    </tbody>
                                </table>
                                <div class="head headtop">
                                    <div class="object-title">
                                        <fmt:message key="label.options"/>
                                    </div>
                                </div>
                                <%
                                    if (mixLanguages) { %>
                                <input type="checkbox" name="mixLanguages" id="mixLanguages" checked="checked" value="true"/><%
                            } else { %>
                                <input type="checkbox" name="mixLanguages" id="mixLanguages" value="true"/><%
                                } %>
                                <label for="mixLanguages"><fmt:message
                                        key="org.jahia.admin.languages.ManageSiteLanguages.mixLanguages.label"/></label>
                                <br>
                                <% if (request.getAttribute("jahiaErrorMessage") != null) { %>
                                <br>

                                <div class="text2" style="text-align:center">
                                    <%=request.getAttribute("jahiaErrorMessage") %>
                                </div>
                                <%
                                    } %>
                                <br>
                                <br>

                                <div style="text-align:center">
                                    <table border="0" cellpadding="5" cellspacing="0">
                                        <tr>
                                            <td>
                                                <b><fmt:message
                                                        key="org.jahia.admin.languages.ManageSiteLanguages.availableLanguages.label"/></b><br/>
                                                <select name="language_list" multiple="" size="10">
                                                    <%
                                                        Iterator localeIter = LanguageCodeConverters.getSortedLocaleList(
                                                                currentLocale).iterator();
                                                        while (localeIter.hasNext()) {
                                                            Locale curLocale = (Locale) localeIter.next();
                                                            // we must now check if this language wasn't already inserted in
                                                            // the site.
                                                            if (!languageSet.contains(curLocale.toString())) {
                                                                String displayName = "";
                                                                displayName = curLocale.getDisplayName(
                                                                        currentLocale); %>
                                                    <option value="<%=curLocale%>"><%=displayName %>&nbsp;(<%=curLocale.toString() %>)</option>
                                                    <%
                                                            }
                                                        } %>
                                                </select>
                                            </td>
                                            <td>
                                <span class="dex-PushButton">
                                    <span class="first-child">
                                        <a class="add-lang" href="javascript:sendForm();"
                                           title="<fmt:message key='org.jahia.admin.languages.ManageSiteLanguages.addLanguages.label'/>"><fmt:message
                                                key="org.jahia.admin.languages.ManageSiteLanguages.addLanguages.label"/></a>
                                    </span>
                                </span>
                                            </td>
                                        </tr>
                                    </table>
                                </div>
                            </form>
                            <br/>
                        </div>
                    </div>
                </div>
            </td>
        </tr>
        </tbody>
    </table>
</div>
</div>
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
                    <a class="ico-ok" href="javascript:sendForm();"><fmt:message key='label.save'/></a>
                  </span>
                </span>
</div>
</div>
<%@include file="/admin/include/footer.inc" %>
