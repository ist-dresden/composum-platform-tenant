<%@page session="false" pageEncoding="utf-8" %>
<%@taglib prefix="sling" uri="http://sling.apache.org/taglibs/sling/1.2" %>
<%@taglib prefix="cpn" uri="http://sling.composum.com/cpnl/1.0" %>
<%@taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<sling:defineObjects/>
<cpn:component id="model" type="com.composum.platform.tenant.view.TenantBean">
    <div class="tenant-component_content">
        <div class="tenant-component_path tenant-content">
            <div class="tenant-component_path_header">
                <h4 class="label">${cpn:i18n(slingRequest, 'principal base')}</h4>
                <h5 class="path">${cpn:path(model.tenant.principalBase)}</h5>
            </div>
        </div>
    </div>
</cpn:component>