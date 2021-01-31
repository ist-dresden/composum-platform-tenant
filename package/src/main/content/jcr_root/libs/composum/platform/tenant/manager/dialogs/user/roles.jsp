<%@page session="false" pageEncoding="UTF-8" %>
<%@taglib prefix="sling" uri="http://sling.apache.org/taglibs/sling/1.2" %>
<%@taglib prefix="cpn" uri="http://sling.composum.com/cpnl/1.0" %>
<sling:defineObjects/>
<cpn:component id="bean" type="com.composum.platform.tenant.view.TenantUserBean">
    <div class="form-group">
        <label class="control-label">${cpn:i18n(slingRequest,'Select Roles')}</label>
        <input type="hidden" name="role@TypeHint" value="String[]"/>
        <div class="checkbox-set">
            <div class="checkbox">
                <label class="composum-form_checkbox-label form-check-label"><input
                        class="composum-form_checkbox form-check-input" type="checkbox" name="role"
                        value="visitor"${bean.user.visitor?' checked="true"':''}/><span
                        class="composum-form_label-value text-muted">${cpn:i18n(slingRequest,'Visitor')}</span>
                    <cpn:text tagName="span" class="composum-form_label-hint" i18n="true"
                              value="a visitor can view preview content only (review guest role)"/></label>
            </div>
            <div class="checkbox">
                <label class="composum-form_checkbox-label form-check-label"><input
                        class="composum-form_checkbox form-check-input" type="checkbox" name="role"
                        value="publisher"${bean.user.publisher?' checked="true"':''}/><span
                        class="composum-form_label-value text-primary">${cpn:i18n(slingRequest,'Publisher')}</span>
                    <cpn:text tagName="span" class="composum-form_label-hint" i18n="true"
                              value="a publisher can build and publish the tenants site releases"/></label>
            </div>
            <div class="checkbox">
                <label class="composum-form_checkbox-label form-check-label"><input
                        class="composum-form_checkbox form-check-input" type="checkbox" name="role"
                        value="editor"${bean.user.editor?' checked="true"':''}/><span
                        class="composum-form_label-value text-primary">${cpn:i18n(slingRequest,'Editor')}</span>
                    <cpn:text tagName="span" class="composum-form_label-hint" i18n="true"
                              value="an editor can create and modify the tenants sites and pages"/></label>
            </div>
            <div class="checkbox">
                <label class="composum-form_checkbox-label form-check-label"><input
                        class="composum-form_checkbox form-check-input" type="checkbox" name="role"
                        value="developer"${bean.user.developer?' checked="true"':''}/><span
                        class="composum-form_label-value text-warning">${cpn:i18n(slingRequest,'Developer')}</span>
                    <cpn:text tagName="span" class="composum-form_label-hint" i18n="true"
                              value="a developer can create and modify the tenants components"/></label>
            </div>
            <div class="checkbox">
                <label class="composum-form_checkbox-label form-check-label"><input
                        class="composum-form_checkbox form-check-input" type="checkbox" name="role"
                        value="manager"${bean.user.manager?' checked="true"':''}/><span
                        class="composum-form_label-value text-danger">${cpn:i18n(slingRequest,'Manager')}</span>
                    <cpn:text tagName="span" class="composum-form_label-hint" i18n="true"
                              value="a manager can assign roles to users and configure the tenant"/></label>
            </div>
            <div class="checkbox">
                <label class="composum-form_checkbox-label form-check-label"><input
                        class="composum-form_checkbox form-check-input" type="checkbox" name="role"
                        value="assistant"${bean.user.assistant?' checked="true"':''}/><span
                        class="composum-form_label-value text-success">${cpn:i18n(slingRequest,'Assistant')}</span>
                    <cpn:text tagName="span" class="composum-form_label-hint" i18n="true"
                              value="an assistant can view the resources but change nothing (support role)"/></label>
            </div>
        </div>
    </div>
</cpn:component>
