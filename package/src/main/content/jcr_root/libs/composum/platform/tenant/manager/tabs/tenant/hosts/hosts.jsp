<%@page session="false" pageEncoding="utf-8" %>
<%@taglib prefix="sling" uri="http://sling.apache.org/taglibs/sling/1.2" %>
<%@taglib prefix="cpn" uri="http://sling.composum.com/cpnl/1.0" %>
<%@taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<sling:defineObjects/>
<cpn:component id="model" type="com.composum.platform.tenant.view.TenantBean">
    <div class="composum-platform-tenant_hosts-console" data-path="${model.path}">
        <div class="composum-platform-tenant_hosts-toolbar detail-toolbar">
            <div class="btn-group btn-group-sm" role="group">
                <button class="add fa fa-plus btn btn-default"
                        title="${cpn:i18n(slingRequest,'Add Host')}"><cpn:text
                        value="Add Host" tagName="span" class="label" i18n="true"/></button>
            </div>
            <div class="btn-group btn-group-sm" role="group">
                <button class="reload fa fa-refresh btn btn-default"
                        title="${cpn:i18n(slingRequest,'Reload')}"><cpn:text
                        value="Reload" tagName="span" class="label" i18n="true"/></button>
            </div>
        </div>
        <div class="composum-platform-tenant_hosts-content detail-content"
             data-path="${model.path}" data-tenant="${model.tenant.id}">
            <sling:include resourceType="composum/platform/tenant/manager/components/hosts"/>
        </div>
    </div>
    <div class="composum-platform-tenant_hosts-busy">
        <div class="symbol fa fa-spinner fa-pulse fa-3x fa-fw"></div>
    </div>
</cpn:component>