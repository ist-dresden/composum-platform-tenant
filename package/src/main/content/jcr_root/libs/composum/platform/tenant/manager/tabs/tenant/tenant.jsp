<%@page session="false" pageEncoding="utf-8" %>
<%@taglib prefix="sling" uri="http://sling.apache.org/taglibs/sling/1.2" %>
<%@taglib prefix="cpn" uri="http://sling.composum.com/cpnl/1.0" %>
<%@taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<sling:defineObjects/>
<cpn:component id="model" type="com.composum.platform.tenant.view.TenantBean">
    <div class="detail-panel tenant tenant-status_${model.tenant.status}" data-path="${model.path}">
        <div class="detail-tabs action-bar btn-toolbar" role="toolbar">
            <div class="btn-group btn-group-sm" role="group">
                <a class="general fa fa-university btn btn-default" href="#general" data-group="general"
                   title="${cpn:i18n(slingRequest,'Tenant Summary')}"><cpn:text
                        value="Tenant" tagName="span" class="label" i18n="true"/></a>
                <a class="view fa fa-inbox btn btn-default" href="#inbox" data-group="inbox"
                   title="${cpn:i18n(slingRequest,'Task Inbox')}"><cpn:text
                        value="Inbox" tagName="span" class="label" i18n="true"/></a>
                <a class="view fa fa-users btn btn-default" href="#users" data-group="users"
                   title="${cpn:i18n(slingRequest,'Users View')}"><cpn:text
                        value="Users" tagName="span" class="label" i18n="true"/></a>
                <a class="view fa fa-sitemap btn btn-default" href="#hosts" data-group="hosts"
                   title="${cpn:i18n(slingRequest,'Hosts View')}"><cpn:text
                        value="Hosts" tagName="span" class="label" i18n="true"/></a>
            </div>
            <div class="btn-group btn-group-sm" role="group">
                <span class="tenant-status_label text-${model.tenant.active?'success':'danger'}">${model.tenant.status}</span>
            </div>
        </div>
        <div class="detail-content">
        </div>
    </div>
</cpn:component>
