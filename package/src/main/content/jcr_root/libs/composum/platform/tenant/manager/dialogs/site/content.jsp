<%@page session="false" pageEncoding="utf-8" %>
<%@taglib prefix="sling" uri="http://sling.apache.org/taglibs/sling/1.2" %>
<%@taglib prefix="cpn" uri="http://sling.composum.com/cpnl/1.0" %>
<%@taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<sling:defineObjects/>
<cpn:component id="model" type="com.composum.platform.tenant.view.TenantBean">
    <sling:call script="hints.jsp"/>
    <div class="row">
        <div class="col col-xs-9">
            <div class="form-group widget select-widget site" data-rules="required">
                <label class="control-label">${cpn:i18n(slingRequest,'Site')}</label>
                <select name="site" class="form-control">
                    <c:forEach items="${model.siteOptions}" var="option">
                        <option value="${option.path}"${option.selected?' selected="true"':''}>${option.label}</option>
                    </c:forEach>
                </select>
            </div>
        </div>
        <div class="col col-xs-3">
            <div class="form-group widget select-widget stage" data-rules="required">
                <label class="control-label">${cpn:i18n(slingRequest,'Stage')}</label>
                <select name="stage" class="form-control">
                    <option value="public" ${empty model.host.siteStage or model.host.siteStage=='public'?' selected="true"':''}>${cpn:i18n(slingRequest,'public')}</option>
                    <option value="preview" ${model.host.siteStage=='preview'?' selected="true"':''}>${cpn:i18n(slingRequest,'preview')}</option>
                </select>
            </div>
        </div>
    </div>
</cpn:component>