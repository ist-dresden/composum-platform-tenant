<%@page session="false" pageEncoding="utf-8" %>
<%@taglib prefix="sling" uri="http://sling.apache.org/taglibs/sling/1.2" %>
<%@taglib prefix="cpn" uri="http://sling.composum.com/cpnl/1.0" %>
<%@taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<sling:defineObjects/>
<cpn:component id="root" type="com.composum.platform.tenant.view.TenantsRootBean">
    <div class="detail-panel tenants-root" data-path="${root.path}">
        <div class="detail-tabs action-bar btn-toolbar" role="toolbar">
            <div class="btn-group btn-group-sm" role="group">
                <a class="general fa fa-university btn btn-default" href="#general" data-group="general"
                   title="${cpn:i18n(slingRequest,'Tenants Overview')}"><cpn:text
                        value="Tenants Overview" tagName="span" tagClass="label" i18n="true"/></a>
            </div>
            <div class="btn-group btn-group-sm" role="group">
            </div>
        </div>
        <div class="detail-content">
        </div>
    </div>
</cpn:component>
