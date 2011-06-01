<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%--

    This file is part of Jahia: An integrated WCM, DMS and Portal Solution
    Copyright (C) 2002-2010 Jahia Solutions Group SA. All rights reserved.

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

<%@ page import="net.sf.ehcache.Ehcache" %>
<%@ page import="net.sf.ehcache.Element" %>
<%@ page import="org.jahia.services.cache.CacheEntry" %>
<%@ page import="org.jahia.services.render.filter.cache.DefaultCacheKeyGenerator" %>
<%@ page import="org.jahia.services.render.filter.cache.ModuleCacheProvider" %>
<%@ page import="java.util.Collections" %>
<%@ page import="java.util.List" %>
<%@ page import="java.util.Set" %>
<%--
  Output cache monitoring JSP.
  User: rincevent
  Date: 28 mai 2008
  Time: 16:59:07
--%>
<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<c:if test="${not empty param.key}">
    <html>
    <body>
    <%
        System.out.println(request.getParameter("key"));
        Element elem = ModuleCacheProvider.getInstance().getCache().get(request.getParameter("key"));
        Object obj = elem != null ? ((CacheEntry) elem.getValue()).getObject() : null;
    %><%= obj %>
    </body>
    </html>
</c:if>
<c:if test="${empty param.key}">
    <html>
    <head>
        <link type="text/css" href="css/demo_table.css" rel="stylesheet"/>
        <script type="text/javascript" src="jquery.min.js" language="JavaScript"></script>
        <script type="text/javascript" src="jquery.dataTables.min.js" language="JavaScript"></script>
        <title>Display content of module cache dependencies</title>
        <script type="text/javascript">
            var myTable = $(document).ready(function() {
                $('#cacheTable').dataTable({
                    "bLengthChange": true,
                    "bFilter": true,
                    "bSort": true,
                    "bInfo": false,
                    "bAutoWidth": true,
                    "bStateSave" : true,
                    "aoColumns": [
                        null,
                        {
                            "sType": "html"
                        }
                    ],
                    "sPaginationType": "full_numbers"
                });
            });
        </script>
    </head>
    <%
        ModuleCacheProvider cacheProvider = ModuleCacheProvider.getInstance();
        Ehcache cache = cacheProvider.getCache();
        Ehcache depCache = cacheProvider.getDependenciesCache();
        if (pageContext.getRequest().getParameter("flush") != null) {
            System.out.println("Flushing cache content");
            cache.flush();
            cache.clearStatistics();
            cache.removeAll();
            depCache.flush();
            depCache.clearStatistics();
            depCache.removeAll();
            ((DefaultCacheKeyGenerator) cacheProvider.getKeyGenerator()).flushUsersGroupsKey();
        }
        List keys = depCache.getKeys();
        Collections.sort(keys);
        pageContext.setAttribute("keys", keys);
        pageContext.setAttribute("cache", cache);
    %>
    <body style="background-color: white;">
    <a href="index.html" title="back to the overview of caches">overview</a>&nbsp;
    <a href="?flush=true"
       onclick="return confirm('This will flush the content of the cache. Would you like to continue?')"
       title="flush the content of the module output cache">flush</a>&nbsp;
    <div id="keys">
        <table id="cacheTable">
            <thead>
            <tr>
                <th>Key</th>
                <th>Dependencies</th>
            </tr>
            </thead>
            <tbody>
            <c:forEach items="${keys}" var="key" varStatus="i">
                <% String attribute = (String) pageContext.getAttribute("key");
                    final Element element = depCache.getQuiet(attribute);
                    if (element != null) {
                %>
                <tr>

                    <td>${key}</td>
                    <td><%
                        Set<String> deps = (Set<String>) element.getValue();
                        for (String dep : deps) {
                            out.print(dep + "<br/>");
                        }%>
                        <br/>
                    </td>
                </tr>
                <%}%>
            </c:forEach>
            </tbody>
        </table>
    </div>
    <a href="index.html">overview</a>
    </body>
    </html>
</c:if>