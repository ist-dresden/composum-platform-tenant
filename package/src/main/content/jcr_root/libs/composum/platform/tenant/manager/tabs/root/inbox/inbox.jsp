<%@page session="false" pageEncoding="utf-8" %>
<%@taglib prefix="sling" uri="http://sling.apache.org/taglibs/sling/1.2" %>
<%@taglib prefix="cpn" uri="http://sling.composum.com/cpnl/1.0" %>
<sling:defineObjects/>
<cpn:component id="tenantRoot" type="com.composum.platform.tenant.view.TenantsRootBean">
    <div class="tenant-inbox workflow-inbox" data-path="${tenantRoot.path}">
        <sling:include path="/var/composum/workflow/platform"
                       resourceType="composum/platform/workflow/components/inbox/toolbar"/>
        <div class="tenant-detail_content">
            <sling:include path="/var/composum/workflow/platform"
                           resourceType="composum/platform/workflow/components/inbox"/>
        </div>
    </div>
</cpn:component>