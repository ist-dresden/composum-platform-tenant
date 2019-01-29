<%@page session="false" pageEncoding="utf-8" %>
<%@taglib prefix="sling" uri="http://sling.apache.org/taglibs/sling/1.2" %>
<%@taglib prefix="cpn" uri="http://sling.composum.com/cpnl/1.0" %>
<sling:defineObjects/>
<cpn:component id="model" type="com.composum.platform.tenant.view.TenantBean">
    <div class="tenant-component_header">
        <h2 class="tenant-name">${cpn:text(model.tenant.name)}</h2>
        <h3 class="tenant-id"><span class="label">${cpn:i18n(slingRequest, 'tenant id')}: </span><span
                class="value">${cpn:text(model.tenant.id)}</span></h3>
    </div>
</cpn:component>
