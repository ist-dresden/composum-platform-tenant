<%@page session="false" pageEncoding="utf-8" %>
<%@taglib prefix="sling" uri="http://sling.apache.org/taglibs/sling/1.2" %>
<%@taglib prefix="cpn" uri="http://sling.composum.com/cpnl/1.0" %>
<%@taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<sling:defineObjects/>
<cpn:component id="model" type="com.composum.platform.tenant.view.TenantBean">
    <div class="composum-platform-tenant-component" data-path="${model.path}">
        <div class="composum-platform-tenant-component_summary panel panel-default">
            <div id="${model.domId}_summary_toggle"
                 class="composum-platform-tenant-component_heading panel-heading collapsible-heading">
                <a href="${cpn:unmappedUrl(slingRequest,'/bin/platform/tenants.html')}${cpn:path(model.path)}"
                   class="composum-platform-tenant-component_open btn btn-default"><i
                        class="fa fa-external-link"></i></a>
                <a href="#${model.domId}_summary_panel" role="button" aria-expanded="true"
                   data-toggle="collapse" aria-controls="${model.domId}_summary_panel"
                   class="composum-platform-tenant-component_toggle">
                    <div class="title">${model.title}</div>
                    <div class="name">${model.id}</div>
                </a>
            </div>
            <div id="${model.domId}_summary_panel" role="tabpanel"
                 aria-labelledby="${model.domId}_summary_toggle"
                 class="composum-platform-tenant-component_panel panel-collapse collapse in">
                <ul class="composum-platform-tenant-component_list-group list-group ">
                    <li class="composum-platform-tenant-component_summary-item list-group-item">
                        <ul class="composum-platform-tenant-component_paths">
                            <li class="content"><i class="fa fa-globe"></i>${cpn:text(model.tenant.contentRoot)}</li>
                            <li class="apps"><i class="fa fa-code"></i>${cpn:text(model.tenant.applicationRoot)}</li>
                            <li class="conf"><i class="fa fa-sliders"></i>${cpn:text(model.tenant.configurationRoot)}
                            </li>
                        </ul>
                    </li>
                    <li class="list-group-item">
                        <div class="number">${model.countSites}</div>
                        <div class="aspect">${cpn:i18n(slingRequest,'sites')}</div>
                            <%--div class="right">${cpn:i18n(slingRequest,'last publishing')}</div>
                            <div class="time">2020-02-02 19:53</div--%>
                    </li>
                    <li class="list-group-item">
                        <div class="number">${model.countHosts}</div>
                        <div class="aspect">${cpn:i18n(slingRequest,'hosts')}</div>
                            <%--div class="right">${cpn:i18n(slingRequest,'last access')}</div>
                            <div class="time">2020-02-02 19:53</div--%>
                    </li>
                    <li class="list-group-item">
                        <div class="number">${model.countUsers}</div>
                        <div class="aspect">${cpn:i18n(slingRequest,'users')}</div>
                        <div class="right">${cpn:i18n(slingRequest,'last login')}</div>
                        <div class="time">${model.lastLoginString}</div>
                    </li>
                    <li class="list-group-item">
                        <div class="number">${model.activeWorkflows}</div>
                        <div class="aspect">${cpn:i18n(slingRequest,'active workflows')}</div>
                        <div class="right">${cpn:i18n(slingRequest,'last activity')}</div>
                        <div class="time">${model.lastWorkflowActivityString}</div>
                    </li>
                </ul>
            </div>
        </div>
    </div>
</cpn:component>
