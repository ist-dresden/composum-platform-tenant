<%@page session="false" pageEncoding="utf-8" %>
<%@taglib prefix="sling" uri="http://sling.apache.org/taglibs/sling/1.2" %>
<%@taglib prefix="cpn" uri="http://sling.composum.com/cpnl/1.0" %>
<%@taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<sling:defineObjects/>
<cpn:component id="model" type="com.composum.platform.tenant.view.TenantBean">
    <div class="composum-platform-tenant_users-view">
        <div class="composum-platform-tenant_users-table" data-path="${model.path}">
            <table class="table table-striped table-hover table-condensed">
                <thead>
                <tr>
                    <th class="sel">&nbsp;</th>
                    <th class="name">${cpn:i18n(slingRequest,'Name')}</th>
                    <th class="id">${cpn:i18n(slingRequest,'ID')}</th>
                    <th class="email">${cpn:i18n(slingRequest,'E-Mail')}</th>
                    <th class="role visitor text-muted" title="${cpn:i18n(slingRequest,'Visitor')}">V</th>
                    <th class="role publisher text-primary" title="${cpn:i18n(slingRequest,'Publisher')}">P</th>
                    <th class="role editor text-primary" title="${cpn:i18n(slingRequest,'Editor')}">E</th>
                    <th class="role developer text-warning" title="${cpn:i18n(slingRequest,'Developer')}">D</th>
                    <th class="role manager text-danger" title="${cpn:i18n(slingRequest,'Manager')}">M</th>
                    <th class="role assistant text-success" title="${cpn:i18n(slingRequest,'Assistant')}">A</th>
                </tr>
                </thead>
                <tbody>
                <c:forEach items="${model.users}" var="user">
                    <tr class="composum-platform-tenant_users-item item table-row" data-user="${user.id}">
                        <td class="sel"><label><input type="radio" name="user" value="${user.id}"/></label></td>
                        <td class="name">${cpn:text(user.name)}</td>
                        <td class="id">${cpn:text(user.id)}</td>
                        <td class="email">${cpn:text(user.email)}</td>
                        <td class="role visitor text-muted"
                            title="${cpn:i18n(slingRequest,'Visitor')}">${user.visitor?'x':' '}</td>
                        <td class="role publisher text-primary"
                            title="${cpn:i18n(slingRequest,'Publisher')}">${user.publisher?'x':' '}</td>
                        <td class="role editor text-primary"
                            title="${cpn:i18n(slingRequest,'Editor')}">${user.editor?'x':' '}</td>
                        <td class="role developer text-warning"
                            title="${cpn:i18n(slingRequest,'Developer')}">${user.developer?'x':' '}</td>
                        <td class="role manager text-danger"
                            title="${cpn:i18n(slingRequest,'Manager')}">${user.manager?'x':' '}</td>
                        <td class="role assistant text-success"
                            title="${cpn:i18n(slingRequest,'Assistant')}">${user.assistant?'x':' '}</td>
                    </tr>
                </c:forEach>
                <c:if test="${model.countUsers<2}">
                    <tr>
                        <td colspan="10" style="padding: 0; background-color: #fff;"><sling:include
                                resourceType="composum/platform/tenant/manager/components/users/hint"/></td>
                    </tr>
                </c:if>
                </tbody>
            </table>
        </div>
    </div>
</cpn:component>