<%@page session="false" pageEncoding="utf-8" %>
<%@taglib prefix="sling" uri="http://sling.apache.org/taglibs/sling/1.2" %>
<%@taglib prefix="cpn" uri="http://sling.composum.com/cpnl/1.0" %>
<%@taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<sling:defineObjects/>
<cpn:component id="model" type="com.composum.platform.tenant.view.TenantBean">
    <div class="composum-platform-tenant-component_header panel panel-default">
        <div class="composum-platform-tenant-component_heading panel-heading">
            <div class="title">${model.title}</div>
            <div class="name">${model.id}</div>
        </div>
        <ul class="composum-platform-tenant-component_panel list-group ">
            <li class="composum-platform-tenant-component_header-item list-group-item">
                <ul class="composum-platform-tenant-component_paths">
                    <li class="content"><i class="fa fa-globe"></i>${cpn:text(model.tenant.contentRoot)}</li>
                    <li class="apps"><i class="fa fa-code"></i>${cpn:text(model.tenant.applicationRoot)}</li>
                    <li class="conf"><i class="fa fa-sliders"></i>${cpn:text(model.tenant.configurationRoot)}</li>
                </ul>
                <div class="composum-platform-tenant-component_aspects">
                    <div class="composum-platform-tenant-component_entry">
                        <div class="number">${model.countUsers}</div>
                        <div class="aspect">
                            <div class="left">${cpn:i18n(slingRequest,'users')}</div>
                            <div class="right">${cpn:i18n(slingRequest,'last login')}&nbsp;&nbsp;${model.lastLoginString}</div>
                        </div>
                    </div>
                    <div class="composum-platform-tenant-component_entry">
                        <div class="number">${model.activeWorkflows}</div>
                        <div class="aspect">
                            <div class="left">${cpn:i18n(slingRequest,'active workflows')}</div>
                            <div class="right">last activity&nbsp;&nbsp;${model.lastWorkflowActivityString}</div>
                        </div>
                    </div>
                </div>
            </li>
        </ul>
    </div>
</cpn:component>
