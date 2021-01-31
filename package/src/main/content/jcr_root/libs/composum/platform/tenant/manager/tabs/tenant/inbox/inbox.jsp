<%@page session="false" pageEncoding="utf-8" %>
<%@taglib prefix="sling" uri="http://sling.apache.org/taglibs/sling/1.2" %>
<%@taglib prefix="cpn" uri="http://sling.composum.com/cpnl/1.0" %>
<sling:defineObjects/>
<cpn:component id="tenant" type="com.composum.platform.tenant.view.TenantBean">
    <div class="tenant-inbox workflow-inbox" data-path="${tenant.path}">
        <sling:include path="/var/composum/workflow/${tenant.name}"
                       resourceType="composum/platform/workflow/components/inbox/toolbar"/>
        <div class="tenant-detail_content">
            <sling:include path="/var/composum/workflow/${tenant.name}"
                           resourceType="composum/platform/workflow/components/inbox"/>
        </div>
    </div>
</cpn:component>