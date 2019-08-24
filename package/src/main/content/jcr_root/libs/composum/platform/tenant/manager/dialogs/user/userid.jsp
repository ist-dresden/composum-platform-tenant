<%@page session="false" pageEncoding="utf-8" %>
<%@taglib prefix="sling" uri="http://sling.apache.org/taglibs/sling/1.2" %>
<%@taglib prefix="cpn" uri="http://sling.composum.com/cpnl/1.0" %>
<sling:defineObjects/>
<cpn:component id="bean" type="com.composum.platform.tenant.view.TenantUserBean">
    <div class="form-group widget text-field-widget user-id" data-rules="required">
        <label class="control-label">User ID</label>
        <input name="user.id" class="form-control" type="text" value="${cpn:text(bean.user.id)}"/>
    </div>
</cpn:component>