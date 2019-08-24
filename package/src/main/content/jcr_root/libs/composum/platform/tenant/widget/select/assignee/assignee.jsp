<%@page session="false" pageEncoding="UTF-8" %>
<%@taglib prefix="sling" uri="http://sling.apache.org/taglibs/sling/1.2" %>
<%@taglib prefix="cpn" uri="http://sling.composum.com/cpnl/1.0" %>
<%@taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<sling:defineObjects/>
<cpn:component id="model" type="com.composum.platform.tenant.widget.select.Assignee">
    <div class="form-group widget combobox-widget" data-rules="required">
        <label class="widget-label"><span
                class="label-text">${cpn:i18n(slingRequest,'Mail To')}</span><cpn:text
                tagName="span" class="widget-hint" i18n="true" type="rich"
                value="the addressed user or group"/></label>
        <div class="input-group">
            <sling:call script="value.jsp"/>
            <span class="input-group-btn">
            <button type="button" class="btn btn-default dropdown-toggle" data-toggle="dropdown" aria-haspopup="true"><i
                    class="fa fa-caret-down"></i></button>
            <ul class="dropdown-menu dropdown-menu-right">
            <c:forEach items="${model.options}" var="option">
                <li data-value="${option.value}"><a href="#">${cpn:i18n(slingRequest,option.label)}</a></li>
            </c:forEach>
            </ul>
        </span>
        </div>
    </div>
</cpn:component>
