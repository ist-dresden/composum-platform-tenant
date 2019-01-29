<%@page session="false" pageEncoding="utf-8" %>
<%@taglib prefix="sling" uri="http://sling.apache.org/taglibs/sling/1.2" %>
<%@taglib prefix="cpn" uri="http://sling.composum.com/cpnl/1.0" %>
<%@taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<sling:defineObjects/>
<cpn:component id="model" type="com.composum.platform.tenant.view.TenantBean">
    <div class="tenant-users" data-path="${model.path}">
        <div class="tenant-toolbar detail-toolbar">
            <div class="btn-group btn-group-sm" role="group">
                <button class="join fa fa-plus btn btn-default"
                        title="${cpn:i18n(slingRequest,'Join User')}"><cpn:text
                        value="Join User" tagName="span" tagClass="label" i18n="true"/></button>
                <button class="remove fa fa-minus btn btn-default"
                        title="${cpn:i18n(slingRequest,'Remove User')}"><cpn:text
                        value="Remove User" tagName="span" tagClass="label" i18n="true"/></button>
            </div>
            <div class="btn-group btn-group-sm" role="group">
                <button class="reload fa fa-refresh btn btn-default"
                        title="${cpn:i18n(slingRequest,'Reload')}"><cpn:text
                        value="Reload" tagName="span" tagClass="label" i18n="true"/></button>
            </div>
        </div>
        <div class="tenant-detail_content">
            <sling:include resourceType="composum/platform/tenant/manager/components/users"/>
        </div>
    </div>
</cpn:component>