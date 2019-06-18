<%@page session="false" pageEncoding="utf-8" %>
<%@taglib prefix="sling" uri="http://sling.apache.org/taglibs/sling/1.2" %>
<%@taglib prefix="cpn" uri="http://sling.composum.com/cpnl/1.0" %>
<%@taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<sling:defineObjects/>
<cpn:component id="tenant" type="com.composum.platform.tenant.view.TenantBean">
    <div class="tenant-detail" data-path="${tenant.path}">
        <div class="tenant-toolbar detail-toolbar">
            <div class="btn-group btn-group-sm" role="group">
                <button class="delete fa fa-trash btn btn-default"
                        title="${cpn:i18n(slingRequest,'Delete Tenant')}"><cpn:text
                        value="Delete" tagName="span" class="label" i18n="true"/></button>
            </div>
            <div class="btn-group btn-group-sm" role="group">
                <button class="reload fa fa-refresh btn btn-default"
                        title="${cpn:i18n(slingRequest,'Reload')}"><cpn:text
                        value="Reload" tagName="span" class="label" i18n="true"/></button>
            </div>
        </div>
        <div class="tenant-detail_content">
            <sling:include resourceType="composum/platform/tenant/manager/components/tenant"/>
        </div>
    </div>
</cpn:component>