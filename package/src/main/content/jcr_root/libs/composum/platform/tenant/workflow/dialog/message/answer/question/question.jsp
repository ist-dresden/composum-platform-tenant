<%@page session="false" pageEncoding="UTF-8" %>
<%@taglib prefix="sling" uri="http://sling.apache.org/taglibs/sling/1.2" %>
<%@taglib prefix="cpn" uri="http://sling.composum.com/cpnl/1.0" %>
<sling:defineObjects/>
<cpn:component id="task" type="com.composum.platform.workflow.model.WorkflowTaskInstance">
    <div class="row">
        <div class="col-xs-6">
            <sling:include resourceType="composum/platform/tenant/widget/select/tenant"/>
        </div>
        <div class="col-xs-6">
            <sling:include resourceType="composum/platform/tenant/widget/select/sender"/>
        </div>
    </div>
    <div class="form-group widget text-field-widget">
        <label class="widget-label"><span
                class="label-text">${cpn:i18n(slingRequest,'Subject')}</span><cpn:text
                tagName="span" class="widget-hint" i18n="true" type="rich"
                value="the short summary"/></label>
        <input name="data/subject" class="form-control" type="text" value="${task.data.subject}"/>
    </div>
    <div class="form-group widget richtext-widget composum-widgets-richtext">
        <label class="widget-label"><span
                class="label-text">${cpn:i18n(slingRequest,'Message')}</span><cpn:text
                tagName="span" class="widget-hint" i18n="true" type="rich"
                value="your next messag eor question"/></label>
        <div class="composum-widgets-richtext_wrapper">
            <textarea name="data/message" class="rich-editor form-control" style="height:200px"></textarea>
        </div>
    </div>
</cpn:component>
