<%@page session="false" pageEncoding="utf-8" %>
<%@taglib prefix="sling" uri="http://sling.apache.org/taglibs/sling/1.2" %>
<%@taglib prefix="cpn" uri="http://sling.composum.com/cpnl/1.0" %>
<sling:defineObjects/>
<cpn:component id="model" type="com.composum.platform.tenant.view.TenantBean">
    <a href="${cpn:url(slingRequest,'/libs/composum/pages/home.html')}"
       style="display: block; padding: 20px 10px; border-bottom: 1px solid #ccc; color: #08c;">
            ${cpn:i18n(slingRequest,'There are no sites available for the tenant.')}
        <span style="white-space: nowrap;">${cpn:i18n(slingRequest,'Create your first site')}...<i
                class="fa fa-external-link" style="margin-left: 10px;"></i></span>
    </a>
    <div style="padding: 10px;">
        <h4>Platform Tenant Manager - ${cpn:i18n(slingRequest,'Replication Configuration')}</h4>
        <p>${cpn:i18n(slingRequest,'the tenant management console view to configure the replication of all sites of a tenant')}</p>
        <div style="width: 100%; text-align: center;">
            <cpn:image src="/libs/composum/platform/tenant/manager/components/replication/empty/screen.png"
                       style="display: inline-block; max-width: 90%; margin: 1em;"/>
        </div>
        <p>${cpn:i18n(slingRequest,'Several replication strategies can be implemented and than configured here.')}</p>
        <p>${cpn:i18n(slingRequest,"By default the 'in-place' an the 'remote' replication types are available.")}</p>
        <p>${cpn:i18n(slingRequest,'Some replication settings can be added for the available stages and various paths of a site.')}</p>
    </div>
</cpn:component>
