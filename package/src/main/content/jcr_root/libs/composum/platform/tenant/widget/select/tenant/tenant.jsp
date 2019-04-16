<%@page session="false" pageEncoding="UTF-8" %>
<%@taglib prefix="sling" uri="http://sling.apache.org/taglibs/sling/1.2" %>
<%@taglib prefix="cpn" uri="http://sling.composum.com/cpnl/1.0" %>
<%@taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<sling:defineObjects/>
<cpn:component id="model" type="com.composum.platform.tenant.widget.select.TenantSelect">
    <div class="form-group widget select-widget">
        <label class="widget-label"><span
                class="label-text">${cpn:i18n(slingRequest,'Tenant')}</span><cpn:text
                tagName="span" class="widget-hint" i18n="true" type="rich"
                value="the tenants id"/></label>
        <select name="data/tenant.id" class="form-control" data-rules="mandatory">
            <c:forEach items="${model.options}" var="option">
                <option value="${option.value}"${option.selected?' selected="true"':''}>${option.label}</option>
            </c:forEach>
        </select>
    </div>
</cpn:component>
