<%@page session="false" pageEncoding="utf-8" %>
<%@taglib prefix="sling" uri="http://sling.apache.org/taglibs/sling/1.2" %>
<%@taglib prefix="cpn" uri="http://sling.composum.com/cpnl/1.0" %>
<%@taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<sling:defineObjects/>
<cpn:component id="model" type="com.composum.platform.tenant.view.TenantBean">
    <div class="composum-platform-tenant_replication-view tenant-replication_content">
        <div class="tenant-replication_tabs">
            <div class="tenant-replication_tabs-hint">${cpn:i18n(slingRequest,'the replication configuration for each site of the tenant')}</div>
            <div class="tenant-replication_tabs-panel">
                <ul class="nav nav-tabs tabs-left">
                    <c:forEach items="${model.siteOptions}" var="site">
                        <li data-config="${site.replicationConfig}"><a href="#">
                            <div class="title">${cpn:text(site.title)}</div>
                            <div class="path">${cpn:text(site.path)}</div>
                        </a></li>
                    </c:forEach>
                </ul>
            </div>
        </div>
        <div class="tenant-replication_panel tab-content">
        </div>
        <cpn:div test="${empty model.tenantSites}" class="tenant-replication_panel-empty">
            <sling:include resourceType="composum/platform/tenant/manager/components/replication/empty"/>
        </cpn:div>
    </div>
</cpn:component>
