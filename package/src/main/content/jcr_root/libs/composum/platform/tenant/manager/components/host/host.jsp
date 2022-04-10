<%@page session="false" pageEncoding="utf-8" %>
<%@taglib prefix="sling" uri="http://sling.apache.org/taglibs/sling/1.2" %>
<%@taglib prefix="cpn" uri="http://sling.composum.com/cpnl/1.0" %>
<%@taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<sling:defineObjects/>
<cpn:component id="model" type="com.composum.platform.tenant.view.TenantBean">
    <cpn:div test="${model.host!=null}" class="tenant-host row">
        <cpn:div test="${!model.host.foreignHost}" class="tenant-host_status col col-xs-5">
            <div class="tenant-host_status_enabled ${model.host.enabled?'enabled':'disabled'}">
                <i class="tenant-host_status_icon enabled ${model.host.enabled?'on':'off'} fa fa-${model.host.enabled?'check':'power-off'}"></i>
                <span class="tenant-host_status_label">${cpn:i18n(slingRequest,model.host.enabled?'enabled':'disabled')}</span>
                <span class="tenant-host_status_toggle fa fa-toggle-${model.host.enabled?'on':'off'}"
                      title="${cpn:i18n(slingRequest,model.host.enabled?'disable':'enable')}"></span>
            </div>
            <div class="tenant-host_status_configured ${model.host.configured?'configured':'unconfigured'}">
                <i class="tenant-host_status_icon configured ${model.host.configured?'on':'off'} fa fa-code"></i>
                <span class="tenant-host_status_label">${cpn:i18n(slingRequest,model.host.configured?'configured':'not configured')}</span>
                <span class="tenant-host_status_toggle fa fa-${model.host.configured?'trash':'toggle-off'} ${model.host.locked?'disabled':''}"
                      title="${cpn:i18n(slingRequest,model.host.configured?'drop configuration':'create configuration')}"></span>
            </div>
            <div class="tenant-host_status_certificate ${model.host.certAvailable?'certificate':'nocertificate'}">
                <i class="tenant-host_status_icon certificate ${model.host.certAvailable?'on':'off'} fa fa-tag"></i>
                <span class="tenant-host_status_label">${cpn:i18n(slingRequest,model.host.certAvailable?'valid certificate':'no certificate')}</span>
                <span class="tenant-host_status_toggle fa fa-${model.host.certAvailable?'trash':'toggle-off'}"
                      title="${cpn:i18n(slingRequest,model.host.certAvailable?'revoke certificate':'get certificate')}"></span>
            </div>
            <div class="tenant-host_status_secured ${model.host.secured?'secured':'unsecure'}">
                <i class="tenant-host_status_icon secured ${model.host.secured?'on':'off'} fa fa-key"></i>
                <span class="tenant-host_status_label">${cpn:i18n(slingRequest,model.host.secured?'secured':'not secured')}</span>
                <span class="tenant-host_status_toggle fa fa-toggle-${model.host.secured?'on':'off'} ${model.host.locked?'disabled':''}"
                      title="${cpn:i18n(slingRequest,model.host.secured?'secure':'unsecure')}"></span>
            </div>
            <div class="tenant-host_status_static ${model.host.valid?'locked':'unlocked'}">
                <a class="tenant-host_status_valid" href="#">
                    <i class="tenant-host_status_icon valid ${model.host.valid?'on':'off'} fa fa-${model.host.valid?'link':'chain-broken'}"></i>
                    <span class="tenant-host_status_label">${cpn:i18n(slingRequest,model.host.valid?'reachable':'not reachable!')}</span>
                </a>
                <a class="tenant-host_status_locked" href="#">
                    <i class="tenant-host_status_icon locked ${model.host.locked?'on':'off'} fa fa-${model.host.locked?'lock':'unlock'}"></i>
                    <span class="tenant-host_status_label">${cpn:i18n(slingRequest,model.host.locked?'locked':'unlocked')}</span>
                </a>
            </div>
        </cpn:div>
        <cpn:div test="${model.host.foreignHost}" class="tenant-host_status col col-xs-5">
            <div class="tenant-host_status_static ${model.host.valid?'locked':'unlocked'}">
                <a class="tenant-host_status_valid" href="#">
                    <i class="tenant-host_status_icon valid ${model.host.valid?'on':'off'} fa fa-${model.host.valid?'link':'chain-broken'}"></i>
                    <span class="tenant-host_status_label">${cpn:i18n(slingRequest,model.host.valid?'reachable':'not reachable!')}</span>
                </a>
                <a class="tenant-host_status_locked" href="#">
                    <i class="tenant-host_status_icon locked ${model.host.locked?'on':'off'} fa fa-${model.host.locked?'lock':'unlock'}"></i>
                    <span class="tenant-host_status_label">${cpn:i18n(slingRequest,model.host.locked?'locked':'unlocked')}</span>
                </a>
            </div>
        </cpn:div>
        <div class="tenant-host_site col col-xs-7">
            <div class="tenant-host_site">
                <div class="tenant-host_site-content">
                    <span class="tenant-host_site_assign ${model.host.locked?'locked':'enabled'}"><i
                            class="tenant-host_status_icon ${empty model.host.siteRef?'off':'on'} fa fa-${model.host.locked?'globe':'pencil'}"></i></span>
                    <sling:include replaceSelectors="site"/>
                    <span class="tenant-host_site_remove ${empty model.host.siteRef or model.host.locked?'locked':'enabled'}"><i
                            class="tenant-host_status_icon ${empty model.host.siteRef or model.host.locked?'locked':'off'} fa fa-times"></i></span>
                </div>
                <cpn:div test="${not empty model.host.siteRef}" class="tenant-host_site-tile">
                    <sling:include path="${model.host.siteRef}"
                                   resourceType="composum/pages/stage/edit/default/site/tile" replaceSelectors="wide"/>
                </cpn:div>
            </div>
        </div>
    </cpn:div>
</cpn:component>