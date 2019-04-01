<%@page session="false" pageEncoding="UTF-8" %>
<%@taglib prefix="sling" uri="http://sling.apache.org/taglibs/sling/1.2" %>
<%@taglib prefix="cpn" uri="http://sling.composum.com/cpnl/1.0" %>
<sling:defineObjects/>
<cpn:component id="wfDialog" type="com.composum.platform.workflow.model.WorkflowDialogModel" scope="request">
    <div class="row">
        <div class="col-xs-4">
            <div class="form-group">
                <label class="widget-label"><span
                        class="label-text">${cpn:i18n(slingRequest,'Tenant')}</span><cpn:text
                        tagName="span" class="widget-hint" i18n="true" type="rich"
                        value="the tenants id"/></label>
                <input name="tenant.id" class="widget text-field-widget form-control" type="text"
                       data-rules="mandatory" value="${wfDialog.tenantId}"/>
            </div>
        </div>
        <div class="col-xs-8">
            <div class="form-group">
                <label class="widget-label"><span
                        class="label-text">${cpn:i18n(slingRequest,'Name')}</span><cpn:text
                        tagName="span" class="widget-hint" i18n="true" type="rich"
                        value="your given name and the surname"/></label>
                <input name="data/name" class="widget text-field-widget form-control" type="text"/>
            </div>
        </div>
    </div>
    <div class="form-group">
        <label class="widget-label"><span
                class="label-text">${cpn:i18n(slingRequest,'Message')}</span><cpn:text
                tagName="span" class="widget-hint" i18n="true" type="rich"
                value="your message to the manager"/></label>
        <textarea name="data/message" class="widget text-area-widget form-control"></textarea>
    </div>
</cpn:component>
