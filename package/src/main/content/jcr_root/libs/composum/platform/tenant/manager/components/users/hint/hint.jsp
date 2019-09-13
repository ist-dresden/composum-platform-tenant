<%@page session="false" pageEncoding="utf-8" %>
<%@taglib prefix="sling" uri="http://sling.apache.org/taglibs/sling/1.2" %>
<%@taglib prefix="cpn" uri="http://sling.composum.com/cpnl/1.0" %>
<sling:defineObjects/>
<cpn:component id="model" type="com.composum.platform.tenant.view.TenantBean">
    <cpn:div test="${empty model.users}" style="padding: 20px 10px; border-bottom: 1px solid #ccc; color: #08c;">
        ${cpn:i18n(slingRequest,'No user joined to this tenant.')}
    </cpn:div>
    <div style="padding: 10px;">
        <h4>Platform Tenant Manager - ${cpn:i18n(slingRequest,'User Management')}</h4>
        <p>${cpn:i18n(slingRequest,'the tenant management console view to manage the users of a tenant')}</p>
        <div style="width: 100%; text-align: center;">
            <cpn:image src="/libs/composum/platform/tenant/manager/components/users/hint/screen.png"
                       style="display: inline-block; max-width: 90%; margin: 1em;"/>
        </div>
        <p>${cpn:i18n(slingRequest,'You can join each user which is registered at this system and manage the roles of each such joined user in relation to your tenant.')}</p>
    </div>
</cpn:component>
