<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>

<script type="text/javascript">
    function send(task, state) {
        form = document.forms['myform'];
        form.action = '${url.base}' + task;
        form.elements.state.value = state;
        form.submit();
    }
</script>

<div class="Form taskForm"><!--start Form -->
    <form method="post" name="myform" action="${url.base}${currentNode.path}">
        <input type="hidden" name="nodeType" value="jnt:task">
        <input type="hidden" name="state">


        <fieldset>
            <legend>
                <fmt:message key="jnt_task.newTask"/>
            </legend>


            <p>
                <label class="left">
                    <fmt:message key="mix_title.jcr_title"/>
                </label>
                <span class="value">${currentNode.properties['jcr:title'].string}</span>

            <p>
                <label class="left">
                    <fmt:message key="jnt_task.description"/>
                    :</label>
                <span class="value">${currentNode.properties.description.string}</span>
            </p>

            <p>
                <label class="left">
                    <fmt:message key="jnt_task.priority"/>
                    :</label>
                <span class="value">${currentNode.properties.priority.string}</span>
            </p>
            <%--<p><label for="task_dueDate" class="left">Due date:</label>--%>
            <%--<input type="text" name="dueDate" id="task_dueDate" class="field" value="" tabindex="17" /></p>--%>


            <p>
                <label class="left">
                    <fmt:message key="jnt_task.assignee"/>
                    :</label>
                <span class="value">${currentNode.properties.assignee.node.name}</span>

            </p>

            <p>
                <label class="left">
                    <fmt:message key="jnt_task.state"/>
                    :</label>
                ${currentNode.properties.state.string}
                <c:choose>
                    <c:when test="${currentNode.properties.state.string == 'active'}">
                        <span><img alt="" src="${url.currentModule}/images/right_16.png"/></span>
            <span>
                <a href="javascript:send('${currentNode.path}','suspended')"><fmt:message key="jnt_task.suspended"/></a>&nbsp;
                <a href="javascript:send('${currentNode.path}','cancelled')"><fmt:message key="jnt_task.cancel"/></a>&nbsp;
                <a href="javascript:send('${currentNode.path}','finished')"><fmt:message key="jnt_task.complete"/></a>
            </span>
                    </c:when>
                    <c:when test="${currentNode.properties.state.string == 'finished'}">
                        <img alt="" src="${url.currentModule}/images/tick_16.png"/>
                    </c:when>
                    <c:when test="${currentNode.properties.state.string == 'suspended'}">
                        <span><img alt="" src="${url.currentModule}/images/bubble_16.png"/></span>
            <span>
                <a href="javascript:send('${currentNode.path}','cancelled')"><fmt:message key="jnt_task.cancel"/></a>&nbsp;
                <a href="javascript:send('${currentNode.path}','active')"><fmt:message key="jnt_task.continue"/></a>
            </span>
                    </c:when>
                    <c:when test="${currentNode.properties.state.string == 'canceled'}">
                        <img alt="" src="${url.currentModule}/images/warning_16.png"/>
                    </c:when>
                </c:choose>
            </p>

        </fieldset>

    </form>
</div>