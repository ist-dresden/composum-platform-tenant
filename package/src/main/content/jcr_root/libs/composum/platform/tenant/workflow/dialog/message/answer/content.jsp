<%@page session="false" pageEncoding="UTF-8" %>
<%@taglib prefix="sling" uri="http://sling.apache.org/taglibs/sling/1.2" %>
<%@taglib prefix="cpn" uri="http://sling.composum.com/cpnl/1.0" %>
<sling:defineObjects/>
<cpn:component id="task" type="com.composum.platform.workflow.model.WorkflowTaskInstance">
    <sling:include resourceType="composum/platform/tenant/workflow/dialog/message/sequence"/>
    <table class="composum-platform-workflow_data-table">
        <tbody>
        <tr>
            <td class="name">${cpn:i18n(slingRequest,'From')}</td>
            <td class="value" style="width:40%"><cpn:text value="${task.data.userId}"/></td>
            <td class="name">${cpn:i18n(slingRequest,'To')}</td>
            <td class="value" style="width:40%"><cpn:text value="${task.assignee}"/></td>
        </tr>
        <tr>
            <td class="name">${cpn:i18n(slingRequest,'Subject')}</td>
            <td class="value" colspan="3"><cpn:text value="${task.data.subject}" type="rich"/></td>
        <tr>
            <td class="name">${cpn:i18n(slingRequest,'Target')}</td>
            <td class="value" colspan="3"><sling:call script="task-target.jsp"/></td>
        </tr>
        </tbody>
    </table>
    <div class="composum-platform-workflow_message-body">
        <cpn:text value="${task.data.message}" type="rich"/>
    </div>
</cpn:component>
