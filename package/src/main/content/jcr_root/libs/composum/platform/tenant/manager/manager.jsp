<%@page session="false" pageEncoding="utf-8" %>
<%@taglib prefix="sling" uri="http://sling.apache.org/taglibs/sling/1.2" %>
<%@taglib prefix="cpn" uri="http://sling.composum.com/cpnl/1.0" %>
<%@taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<sling:defineObjects/>
<cpn:bundle basename="composum-tenants">
    <cpn:component id="manager" type="com.composum.platform.tenant.view.TenantManagerBean" scope="request">
        <html data-context-path="${slingRequest.contextPath}">
        <head>
            <title>Composum ${cpn:i18n(slingRequest,'Tenant Manager')}</title>
            <sling:call script="/libs/composum/nodes/console/page/head-meta.jsp"/>
            <cpn:clientlib type="css" category="composum.platform.tenants.manager"/>
        </head>
        <body id="tenants" class="console left-open top-disabled">
        <div id="ui">
            <sling:include resourceType="composum/platform/tenant/manager/dialogs"/>
            <sling:include resourceType="composum/nodes/console/components/navbar"/>
            <div id="content-wrapper">
                <div id="split-view-horizontal-split" class="split-pane horizontal-split fixed-left">
                    <div class="split-pane-component left-pane">
                        <div>
                            <div class="tree-panel">
                                <div id="tenants-tree" data-selected="${manager.path}">
                                </div>
                            </div>
                            <div class="tree-actions action-bar btn-toolbar" role="toolbar">
                                <div class="align-left">
                                    <div class="btn-group btn-group-sm" role="group">
                                        <button type="button" class="refresh fa fa-refresh btn btn-default"
                                                title="Refresh tree view"><span class="label">Refresh</span></button>
                                    </div>
                                    <div class="btn-group btn-group-sm" role="group">
                                        <button type="button" class="create-tenant fa fa-plus btn btn-default"
                                                title="Create a new Tenant"><span class="label">Create Tenant</span>
                                        </button>
                                        <button type="button" class="activate-tenant fa fa-undo btn btn-default"
                                                title="(Re)Activate selected tenant"><span
                                                class="label">Activate Tenant</span></button>
                                        <button type="button" class="delete-tenant fa fa-minus btn btn-default"
                                                title="Delete selected tenant"><span class="label">Delete Tenant</span>
                                        </button>
                                    </div>
                                    <div class="btn-group btn-group-sm" role="group">
                                        <button type="button" class="change-tenant fa fa-sliders btn btn-default"
                                                title="Change Tenant configuration"><span
                                                class="label">Change Tenant</span>
                                        </button>
                                    </div>
                                </div>
                            </div>
                        </div>
                    </div>
                    <div class="split-pane-divider"><span class="fa fa-ellipsis-v"></span></div>
                    <div class="split-pane-component right-pane">
                        <div id="split-view-vertical-split" class="split-pane vertical-split fixed-top">
                            <div class="split-pane-component top-pane">
                                <%--div id="tenants-query">
                                    <sling:include resourceType="composum/tenants/manager/query"/>
                                </div--%>
                            </div>
                            <div class="split-pane-divider"><span class="fa fa-ellipsis-h"></span></div>
                            <div class="split-pane-component bottom-pane">
                                <div id="tenants-view">
                                    <sling:call script="view.jsp"/>
                                </div>
                                <div class="close-top"><a href="#" class="fa fa-angle-double-up"
                                                          title="Collapse top panel"></a></div>
                            </div>
                            <div class="open-top"><a href="#" class="fa fa-angle-double-down"
                                                     title="Restore top panel"></a></div>
                        </div>
                        <div class="close-left"><a href="#" class="fa fa-angle-double-left"
                                                   title="Collapse left panel"></a></div>
                    </div>
                    <div class="open-left"><a href="#" class="fa fa-angle-double-right" title="Restore left panel"></a>
                    </div>
                </div>
            </div>
        </div>
        <cpn:clientlib type="js" category="composum.platform.tenants.manager"/>
        </body>
        </html>
    </cpn:component>
</cpn:bundle>
