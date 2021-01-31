<%@page session="false" pageEncoding="UTF-8" %>
<%@taglib prefix="sling" uri="http://sling.apache.org/taglibs/sling/1.2" %>
<sling:defineObjects/>
<div class="row">
    <div class="col-xs-6">
        <sling:include resourceType="composum/platform/tenant/widget/select/tenant"/>
    </div>
    <div class="col-xs-6">
        <sling:include resourceType="composum/platform/tenant/widget/select/assignee"/>
    </div>
</div>
