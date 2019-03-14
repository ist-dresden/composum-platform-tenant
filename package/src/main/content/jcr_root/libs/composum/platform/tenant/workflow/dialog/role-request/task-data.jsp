<%@page session="false" pageEncoding="UTF-8" %>
<%@taglib prefix="sling" uri="http://sling.apache.org/taglibs/sling/1.2" %>
<%@taglib prefix="cpn" uri="http://sling.composum.com/cpnl/1.0" %>
<sling:defineObjects/>
<cpn:component id="wfDialog" type="com.composum.platform.workflow.model.WorkflowDialogModel" scope="request">
    <table class="composum-platform-workflow_data-table table table-bordered table-condensed">
        <tbody>
        <tr>
            <td class="name">${cpn:i18n(slingRequest,'User')}</td>
            <td class="value"><cpn:text value="${wfDialog.task.initiator}"/></td>
        </tr>
        <tr>
            <td class="name">${cpn:i18n(slingRequest,'Name')}</td>
            <td class="value"><cpn:text value="${wfDialog.task.data.name}"/></td>
        </tr>
        <tr>
            <td class="name">${cpn:i18n(slingRequest,'Message')}</td>
            <td class="value"><cpn:text value="${wfDialog.task.data.message}"/></td>
        </tr>
        </tbody>
    </table>
</cpn:component>
