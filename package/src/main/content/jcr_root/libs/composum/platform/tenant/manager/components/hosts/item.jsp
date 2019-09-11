<%@page session="false" pageEncoding="utf-8" %>
<%@taglib prefix="sling" uri="http://sling.apache.org/taglibs/sling/1.2" %>
<%@taglib prefix="cpn" uri="http://sling.composum.com/cpnl/1.0" %>
<%@taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<sling:defineObjects/>
<cpn:component id="model" type="com.composum.platform.tenant.view.TenantBean">
    <div class="panel panel-${model.host.available?'success':'warning'}">
        <div class="panel-heading" role="tab" id="host_title_${model.host.id}">
            <h4 class="panel-title">
                <a role="button" data-toggle="collapse"
                   href="#host_content_${model.host.id}" aria-expanded="true"
                   aria-controls="host_content_${model.host.id}">
                    <sling:include resourceType="composum/platform/tenant/manager/components/host"
                                   replaceSelectors="title"/>
                </a>
            </h4>
        </div>
        <div id="host_content_${model.host.id}" class="panel-collapse collapse" role="tabpanel"
             aria-labelledby="host_title_${model.host.id}">
            <div class="panel-body">
                <sling:include resourceType="composum/platform/tenant/manager/components/host"/>
            </div>
        </div>
    </div>
</cpn:component>