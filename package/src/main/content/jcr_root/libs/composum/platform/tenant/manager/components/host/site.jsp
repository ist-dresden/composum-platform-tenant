<%@page session="false" pageEncoding="utf-8" %>
<%@taglib prefix="sling" uri="http://sling.apache.org/taglibs/sling/1.2" %>
<%@taglib prefix="cpn" uri="http://sling.composum.com/cpnl/1.0" %>
<%@taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<sling:defineObjects/>
<cpn:component id="model" type="com.composum.platform.tenant.view.TenantBean">
    <cpn:div class="tenant-host_site-status">
        <c:if test="${not empty model.host.siteRef}">
            <div class="tenant-host_site-stage tenant-host_site-label">${cpn:text(model.host.siteStage)}</div>
            <div class="tenant-host_site-at tenant-host_site-label">@</div>
            <div class="tenant-host_site-path tenant-host_site-label">${cpn:text(model.host.siteRef)}</div>
        </c:if>
    </cpn:div>
</cpn:component>