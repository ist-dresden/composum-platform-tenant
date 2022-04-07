<%@page session="false" pageEncoding="utf-8" %>
<%@taglib prefix="sling" uri="http://sling.apache.org/taglibs/sling/1.2" %>
<%@taglib prefix="cpn" uri="http://sling.composum.com/cpnl/1.0" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<sling:defineObjects/>
<cpn:component id="model" type="com.composum.platform.tenant.view.TenantBean">
    <div class="composum-platform-tenant-component_sites panel panel-default">
        <div id="${model.domId}_sites_toggle"
             class="composum-platform-tenant-component_heading panel-heading collapsible-heading">
            <a href="#${model.domId}_sites_panel" role="button" aria-expanded="true"
               data-toggle="collapse" aria-controls="${model.domId}_sites_panel"
               class="composum-platform-tenant-component_toggle">
                <div class="title">Sites</div>
                <div class="right">Hosts</div>
            </a>
        </div>
        <div id="${model.domId}_sites_panel" role="tabpanel"
             aria-labelledby="${model.domId}_sites_toggle"
             class="composum-platform-tenant-component_panel panel-collapse collapse in">
            <ul class="composum-platform-tenant-component_list-group list-group">
                <c:forEach items="${model.tenantSites}" var="site">
                    <li class="composum-platform-tenant-component_sites-item list-group-item">
                        <div class="composum-platform-tenant-component_sites_site">
                            <div class="composum-platform-tenant-component_sites_site-text">
                                <h4 class="composum-platform-tenant-component_sites_site-title">${cpn:text(site.title)}</h4>
                                <cpn:text class="composum-platform-tenant-component_sites_site-description"
                                          value="${site.description}" type="rich"/>
                                <a href="${cpn:unmappedUrl(slingRequest,'/bin/pages.html')}${cpn:path(site.path)}"
                                   class="composum-platform-tenant-component_sites_site-path">${cpn:text(site.path)}</a>
                            </div>
                        </div>
                        <div class="composum-platform-tenant-component_sites_site-host">
                            <cpn:div test="${site.publicHost!=null}"
                                     class="composum-platform-tenant-component_sites_host">
                                <i class="composum-platform-tenant-component_sites_host-stage fa fa-globe"></i>
                                <h5 class="composum-platform-tenant-component_sites_host-title">${cpn:text(site.publicHost.hostname)}</h5>
                            </cpn:div>
                            <cpn:div test="${site.previewHost!=null}"
                                     class="composum-platform-tenant-component_sites_host">
                                <i class="composum-platform-tenant-component_sites_host-stage fa fa-eye"></i>
                                <h5 class="composum-platform-tenant-component_sites_host-title">${cpn:text(site.previewHost.hostname)}</h5>
                            </cpn:div>
                        </div>
                    </li>
                </c:forEach>
                <c:forEach items="${model.tenantHosts}" var="host">
                    <li class="composum-platform-tenant-component_sites-item list-group-item">
                        <div class="composum-platform-tenant-component_sites_site">
                        </div>
                        <div class="composum-platform-tenant-component_sites_site-host">
                            <div class="composum-platform-tenant-component_sites_host">
                                <h5 class="composum-platform-tenant-component_sites_host-title">${cpn:text(host.hostname)}</h5>
                            </div>
                        </div>
                    </li>
                </c:forEach>
            </ul>
        </div>
    </div>
</cpn:component>
