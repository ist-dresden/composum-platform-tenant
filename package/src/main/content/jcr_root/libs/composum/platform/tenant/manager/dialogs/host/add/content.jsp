<%@page session="false" pageEncoding="utf-8" %>
<%@taglib prefix="sling" uri="http://sling.apache.org/taglibs/sling/1.2" %>
<%@taglib prefix="cpn" uri="http://sling.composum.com/cpnl/1.0" %>
<sling:defineObjects/>
<cpn:component id="model" type="com.composum.platform.tenant.view.TenantBean">
    <sling:call script="hints.jsp"/>
    <div class="form-group widget text-field-widget user-id" data-rules="required">
        <label class="control-label">Hostname</label>
        <input name="hostname" class="form-control" type="text"/>
    </div>
</cpn:component>