<%@page session="false" pageEncoding="utf-8" %>
<%@taglib prefix="sling" uri="http://sling.apache.org/taglibs/sling/1.2" %>
<%@taglib prefix="cpn" uri="http://sling.composum.com/cpnl/1.0" %>
<sling:defineObjects/>
<cpn:component id="tenant" type="com.composum.platform.tenant.view.TenantBean">
    <div class="tenant-replication replication-config" data-path="${tenant.path}">
        <div class="composum-platform-tenant_replication-toolbar detail-toolbar">
            <div class="btn-group btn-group-sm" role="group">
                <button class="reload fa fa-refresh btn btn-default"
                        title="${cpn:i18n(slingRequest,'Reload')}"><cpn:text
                        value="Reload" tagName="span" class="label" i18n="true"/></button>
            </div>
        </div>
        <div class="tenant-detail_content">
            <sling:include resourceType="composum/platform/tenant/manager/components/replication"/>
        </div>
    </div>
</cpn:component>