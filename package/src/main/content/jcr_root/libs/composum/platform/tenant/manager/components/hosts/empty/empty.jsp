<%@page session="false" pageEncoding="utf-8" %>
<%@taglib prefix="sling" uri="http://sling.apache.org/taglibs/sling/1.2" %>
<%@taglib prefix="cpn" uri="http://sling.composum.com/cpnl/1.0" %>
<sling:defineObjects/>
<cpn:component id="model" type="com.composum.platform.tenant.view.TenantBean">
    <div style="padding: 20px 10px; border-bottom: 1px solid #ccc; color: #08c;">
            ${cpn:i18n(slingRequest,'There are no hosts configured currently. Use the \'+\' button on the right in the toolbar to add your first host.')}
    </div>
    <div style="padding: 10px;">
        <h4>Platform Tenant Manager - ${cpn:i18n(slingRequest,'Host Management')}</h4>
        <p>${cpn:i18n(slingRequest,'the tenant management console view to manage a tenants internet hosts')}</p>
        <div style="width: 100%; text-align: center;">
            <cpn:image src="/libs/composum/platform/tenant/manager/components/hosts/empty/screen.png"
                       style="display: inline-block; max-width: 90%; margin: 1em;"/>
        </div>
        <p>${cpn:i18n(slingRequest,'You can add each host of your own domain to this tenant host configuration list.')}
                ${cpn:i18n(slingRequest,'Each added host should point to this platform.')}</p>
        <p>${cpn:i18n(slingRequest,'Configure the DNS service of your domain to support this; a DNS configuration for a host should look like')}</p>
        <div class="panel panel-default" style="margin: 1em 2em; padding: 0.5em;">
            <code style="background: transparent;">[hostname] IN CNAME ${cpn:text(model.publicHostname)}.</code>
        </div>
        <p>${cpn:i18n(slingRequest,'to point to this system (the closing \'.\' is important).')}</p>
    </div>
</cpn:component>
