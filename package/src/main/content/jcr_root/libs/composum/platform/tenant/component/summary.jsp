<%@page session="false" pageEncoding="utf-8" %>
<%@taglib prefix="sling" uri="http://sling.apache.org/taglibs/sling/1.2" %>
<%@taglib prefix="cpn" uri="http://sling.composum.com/cpnl/1.0" %>
<%@taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<sling:defineObjects/>
<cpn:component id="model" type="com.composum.platform.tenant.view.TenantBean">
    <div class="composum-platform-tenant-component status-${model.status}" data-path="${model.path}">
        <div class="composum-platform-tenant-component_summary panel panel-default">
            <div id="${model.domId}_summary_toggle"
                 class="composum-platform-tenant-component_heading panel-heading collapsible-heading">
                <a href="${cpn:unmappedUrl(slingRequest,'/bin/platform/tenants.html')}${cpn:path(model.path)}"
                   class="composum-platform-tenant-component_open btn btn-default"><i
                        class="fa fa-external-link"></i></a>
                <a href="#${model.domId}_summary_panel" role="button" aria-expanded="false"
                   data-toggle="collapse" aria-controls="${model.domId}_summary_panel"
                   class="composum-platform-tenant-component_toggle">
                    <div class="title">${model.title}</div>
                    <div class="name">${model.id}</div>
                </a>
            </div>
            <div id="${model.domId}_summary_panel" data-path="${model.path}"
                 role="tabpanel" aria-labelledby="${model.domId}_summary_toggle"
                 class="composum-platform-tenant-component_panel panel-collapse collapse">
            </div>
        </div>
    </div>
</cpn:component>
