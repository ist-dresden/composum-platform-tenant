<%@page session="false" pageEncoding="utf-8" %>
<%@taglib prefix="sling" uri="http://sling.apache.org/taglibs/sling/1.2" %>
<%@taglib prefix="cpn" uri="http://sling.composum.com/cpnl/1.0" %>
<%@taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<sling:defineObjects/>
<cpn:component id="model" type="com.composum.platform.tenant.view.TenantBean">
    <cpn:div test="${model.host!=null}" class="tenant-host_title">
        <i class="tenant-host_title_icon enabled ${model.host.enabled?'on':'off'} fa fa-${model.host.enabled?'check':'power-off'}"></i>
        <span class="tenant-host_name">${cpn:text(model.host.hostname)}</span>
        <i class="tenant-host_title_icon configured ${model.host.configured?'on':'off'} fa fa-code"></i>
        <i class="tenant-host_title_icon certificate ${model.host.certAvailable?'on':'off'} fa fa-tag"></i>
        <i class="tenant-host_title_icon secured ${model.host.secured?'on':'off'} fa fa-key"></i>
        <i class="tenant-host_title_icon valid ${model.host.valid?'on':'off'} fa fa-${model.host.valid?'link':'chain-broken'}"></i>
        <span class="tenant-host_address">${cpn:text(model.host.address)}<i
                class="tenant-host_title_icon locked ${model.host.locked?'on':'off'} fa fa-${model.host.locked?'lock':'unlock'}"></i></span>
        <div class="tenant-host_delete fa fa-trash ${model.host.locked?'disabled':''}"
             title="${cpn:i18n(slingRequest,'Delete Host')}"></div>
    </cpn:div>
</cpn:component>