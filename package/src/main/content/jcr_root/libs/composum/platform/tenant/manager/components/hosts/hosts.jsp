<%@page session="false" pageEncoding="utf-8" %>
<%@taglib prefix="sling" uri="http://sling.apache.org/taglibs/sling/1.2" %>
<%@taglib prefix="cpn" uri="http://sling.composum.com/cpnl/1.0" %>
<%@taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<sling:defineObjects/>
<cpn:component id="model" type="com.composum.platform.tenant.view.TenantBean">
    <div class="composum-platform-tenant_hosts-view tenant-hosts_content">
        <c:choose>
            <c:when test="${not empty model.hosts}">
                <c:forEach items="${model.hosts}" var="host" varStatus="stat">
                    <%slingRequest.setAttribute("host", pageContext.getAttribute("host"));%>
                    <div class="tenant-hosts_host" data-host="${host.encodedData}">
                        <sling:include replaceSelectors="item"/>
                    </div>
                </c:forEach>
            </c:when>
            <c:otherwise>
                <div class="tenant-hosts_content_empty">${cpn:i18n(slingRequest,'no host joined to this tenant')}</div>
            </c:otherwise>
        </c:choose>
    </div>
</cpn:component>