<%@page session="false" pageEncoding="utf-8" %>
<%@taglib prefix="sling" uri="http://sling.apache.org/taglibs/sling/1.2" %>
<%@taglib prefix="cpn" uri="http://sling.composum.com/cpnl/1.0" %>
<%@taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<sling:defineObjects/>
<cpn:component id="root" type="com.composum.platform.tenant.view.TenantsRootBean">
    <div class="root-detail" data-path="${root.path}">
        <div class="tenants-toolbar detail-toolbar">
            <div class="btn-group btn-group-sm" role="group">
                <button class="create fa fa-plus btn btn-default"
                        title="${cpn:i18n(slingRequest,'Create a new Tenant')}"><cpn:text
                        value="Create" tagName="span" class="label" i18n="true"/></button>
            </div>
            <div class="btn-group btn-group-sm" role="group">
                <button class="reload fa fa-refresh btn btn-default"
                        title="${cpn:i18n(slingRequest,'Reload')}"><cpn:text
                        value="Reload" tagName="span" class="label" i18n="true"/></button>
            </div>
        </div>
        <div>Root</div>
    </div>
</cpn:component>