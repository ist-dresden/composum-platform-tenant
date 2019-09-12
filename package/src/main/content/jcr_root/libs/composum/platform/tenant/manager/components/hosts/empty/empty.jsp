<%@page session="false" pageEncoding="utf-8" %>
<%@taglib prefix="sling" uri="http://sling.apache.org/taglibs/sling/1.2" %>
<%@taglib prefix="cpn" uri="http://sling.composum.com/cpnl/1.0" %>
<sling:defineObjects/>
<cpn:component id="model" type="com.composum.platform.tenant.view.TenantBean">
    <h4>Platform Tenant Manager - Host Management</h4>
    <p>the tenant management console view to manage a tenants internet hosts</p>
    <div style="width: 100%; text-align: center;">
        <cpn:image src="/libs/composum/platform/tenant/manager/components/hosts/empty/screen.png"
                   style="display: inline-block; max-width: 90%; margin: 1em;"/>
    </div>
    <p>
        There are no hosts configured currently. Use the '+' button on the right in the toolbar to add your first
        host.
    </p>
    <p>
        You can add each host of your own domain to this tenant host configuration list. Each added host should point to
        this platform. Configure the DNS service of your domain to support this; a DNS configuration for a host should
        look like
    </p>
    <div class="panel panel-default" style="margin: 1em 2em; padding: 0.5em;">
        <code style="background: transparent;">[hostname] IN CNAME ${model.publicHostname}.</code>
    </div>
    <p>
        to point to this system (the closing '.' is important).
    </p>
</cpn:component>
