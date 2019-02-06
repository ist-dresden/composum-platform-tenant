<%@page session="false" pageEncoding="UTF-8" %>
<%@taglib prefix="sling" uri="http://sling.apache.org/taglibs/sling/1.2" %>
<%@taglib prefix="cpn" uri="http://sling.composum.com/cpnl/1.0" %>
<sling:defineObjects/>
<cpn:component id="model" type="com.composum.platform.tenant.view.HomePageBean">
    <cpn:form action="/bin/cpm/platform/tenants.create.json" method="POST">

        <div class="alert alert-hidden" role="alert"></div>

        <input name="_charset_" type="hidden" value="UTF-8"/>
        <div class="row">
            <div class="col-xs-4">
                <div class="form-group">
                    <label class="control-label">${model.properties['form/tenantId']}</label>
                    <input name="tenant.id" class="widget text-field-widget form-control" type="text"
                           data-rules="mandatory"/>
                </div>
            </div>
            <div class="col-xs-8">
                <div class="form-group">
                    <label class="control-label">${model.properties['form/tenantName']}</label>
                    <input name="tenant.name" class="widget text-field-widget form-control" type="text"/>
                </div>
            </div>
        </div>
        <div class="form-group">
            <label class="control-label">${model.properties['form/description']}</label>
            <input name="tenant.description" class="widget text-area-widget form-control" type="text"/>
        </div>

        <div class="buttons">
            <button type="submit"
                    class="btn btn-primary create">${model.properties['form/btnCreate']}</button>
        </div>

    </cpn:form>
</cpn:component>
