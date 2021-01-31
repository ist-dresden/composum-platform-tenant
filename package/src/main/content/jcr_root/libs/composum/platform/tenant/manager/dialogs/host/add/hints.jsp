<%@page session="false" pageEncoding="utf-8" %>
<%@taglib prefix="sling" uri="http://sling.apache.org/taglibs/sling/1.2" %>
<%@taglib prefix="cpn" uri="http://sling.composum.com/cpnl/1.0" %>
<sling:defineObjects/>
<cpn:component id="model" type="com.composum.platform.tenant.view.TenantBean">
    <div class="form-group">
        <cpn:text i18n="true">You can add (register) each of your own hosts to this tenants hosts list and assign this host to a stage of one of your content sites.</cpn:text>
    </div>
    <div class="form-group">
        <cpn:text i18n="true">You should be the owner of the hosts domain and be able to configure the DNS configuration of this domain. This configuration should contain an entry like</cpn:text>
    </div>
    <div class="form-group">
        <code class="panel panel-default">[your hostname] IN CNAME ${cpn:text(model.publicHostname)}.</code>
    </div>
    <div class="form-group">
        <cpn:text i18n="true">to point to this system (the closing '.' is important).</cpn:text>
    </div>
</cpn:component>
