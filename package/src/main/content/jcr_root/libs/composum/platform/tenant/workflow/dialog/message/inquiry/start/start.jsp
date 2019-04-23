<%@page session="false" pageEncoding="UTF-8" %>
<%@taglib prefix="sling" uri="http://sling.apache.org/taglibs/sling/1.2" %>
<%@taglib prefix="cpn" uri="http://sling.composum.com/cpnl/1.0" %>
<sling:defineObjects/>
<sling:include resourceType="composum/platform/tenant/workflow/dialog/message/recipient"/>
<div class="form-group widget text-field-widget" data-rules="mandatory">
    <label class="widget-label"><span
            class="label-text">${cpn:i18n(slingRequest,'Subject')}</span><cpn:text
            tagName="span" class="widget-hint" i18n="true" type="rich"
            value="a short summary"/></label>
    <input name="data/subject" class="form-control" type="text"/>
</div>
<sling:call script="target-form.jsp"/>
<div class="form-group widget richtext-widget composum-widgets-richtext">
    <label class="widget-label"><span
            class="label-text">${cpn:i18n(slingRequest,'Message')}</span><cpn:text
            tagName="span" class="widget-hint" i18n="true" type="rich"
            value="your message to the resignated recipients"/></label>
    <div class="composum-widgets-richtext_wrapper">
        <textarea name="data/message" class="rich-editor form-control" style="height:200px"></textarea>
    </div>
</div>
