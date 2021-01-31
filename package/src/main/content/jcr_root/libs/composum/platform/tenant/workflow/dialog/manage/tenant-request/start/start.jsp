<%@page session="false" pageEncoding="UTF-8" %>
<%@taglib prefix="sling" uri="http://sling.apache.org/taglibs/sling/1.2" %>
<%@taglib prefix="cpn" uri="http://sling.composum.com/cpnl/1.0" %>
<sling:defineObjects/>
<div class="row">
    <div class="col-xs-4">
        <div class="form-group widget text-field-widget tenant-id" data-rules="required">
            <label class="widget-label"><span
                    class="label-text">${cpn:i18n(slingRequest,'Tenant')}</span><cpn:text
                    tagName="span" class="widget-hint" i18n="true" type="rich"
                    value="the tenants id"/></label>
            <input name="data/tenantId" class="form-control" type="text"/>
        </div>
    </div>
    <div class="col-xs-8">
        <div class="form-group widget text-field-widget">
            <label class="widget-label"><span
                    class="label-text">${cpn:i18n(slingRequest,'Name')}</span><cpn:text
                    tagName="span" class="widget-hint" i18n="true" type="rich"
                    value="the new tenants name"/></label>
            <input name="data/name" class="form-control" type="text"/>
        </div>
    </div>
</div>
<div class="form-group widget text-area-widget">
    <label class="widget-label"><span
            class="label-text">${cpn:i18n(slingRequest,'Message')}</span><cpn:text
            tagName="span" class="widget-hint" i18n="true" type="rich"
            value="a short description for the new tenant"/></label>
    <textarea name="data/description" class="form-control"></textarea>
</div>
