<%@page session="false" pageEncoding="UTF-8" %>
<%@taglib prefix="sling" uri="http://sling.apache.org/taglibs/sling/1.2" %>
<%@taglib prefix="cpn" uri="http://sling.composum.com/cpnl/1.0" %>
<sling:defineObjects/>
<cpn:component id="task" type="com.composum.platform.workflow.model.WorkflowTaskInstance">
    <table class="composum-platform-workflow_data-table table table-bordered">
        <tbody>
        <tr>
            <td class="name">${cpn:i18n(slingRequest,'User')}</td>
            <td class="value"><cpn:text value="${task.data.userId}"/></td>
        </tr>
        <tr>
            <td class="name">${cpn:i18n(slingRequest,'Name')}</td>
            <td class="value"><cpn:text value="${task.data.name}"/></td>
        </tr>
        <tr>
            <td class="name">${cpn:i18n(slingRequest,'Message')}</td>
            <td class="value"><cpn:text value="${task.data.message}"/></td>
        </tr>
        </tbody>
    </table>
</cpn:component>
