<%@page session="false" pageEncoding="UTF-8" %>
<%@taglib prefix="sling" uri="http://sling.apache.org/taglibs/sling/1.2" %>
<%@taglib prefix="cpn" uri="http://sling.composum.com/cpnl/1.0" %>
<sling:defineObjects/>
<cpn:component id="task" type="com.composum.platform.workflow.model.WorkflowTaskInstance">
    <table class="composum-platform-workflow_data-table">
        <tbody>
        <tr>
            <td class="name">${cpn:i18n(slingRequest,'From')}</td>
            <td class="value"><cpn:text value="${task.data.from}"/></td>
        </tr>
        <tr>
            <td class="name">${cpn:i18n(slingRequest,'To')}</td>
            <td class="value"><cpn:text value="${task.assignee}"/></td>
        </tr>
        <tr>
            <td class="name">${cpn:i18n(slingRequest,'Subject')}</td>
            <td class="value"><cpn:text value="${task.data.subject}" type="rich"/></td>
        </tbody>
    </table>
</cpn:component>
