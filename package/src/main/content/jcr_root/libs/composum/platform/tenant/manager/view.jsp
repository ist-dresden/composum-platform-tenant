<%@page session="false" pageEncoding="utf-8" %>
<%@taglib prefix="sling" uri="http://sling.apache.org/taglibs/sling/1.2" %>
<%@taglib prefix="cpn" uri="http://sling.composum.com/cpnl/1.0" %>
<sling:defineObjects/>
<cpn:component id="manager" type="com.composum.platform.tenant.view.TenantManagerBean" scope="request">
    <div class="detail-view">
        <sling:include resourceType="composum/platform/tenant/manager/tabs/${manager.viewType}"/>
    </div>
</cpn:component>
