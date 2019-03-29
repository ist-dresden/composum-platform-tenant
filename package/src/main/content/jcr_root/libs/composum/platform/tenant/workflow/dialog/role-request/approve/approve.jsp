<%@page session="false" pageEncoding="UTF-8" %>
<%@taglib prefix="sling" uri="http://sling.apache.org/taglibs/sling/1.2" %>
<%@taglib prefix="cpn" uri="http://sling.composum.com/cpnl/1.0" %>
<sling:defineObjects/>
<cpn:component id="wfDialog" type="com.composum.platform.workflow.model.WorkflowDialogModel" scope="request">
    <input type="hidden" name="data/role@TypeHint" value="String[]"/>
    <div class="checkbox-list-label">${cpn:i18n(slingRequest,'Select Roles')}</div>
    <div class="checkbox">
        <label class="composum-form_checkbox-label form-check-label"><input
                class="composum-form_checkbox form-check-input" type="checkbox" name="data/role"
                value="editor"/><span class="composum-form_label-value">${cpn:i18n(slingRequest,'Editor')}</span>
            <cpn:text tagName="span" class="composum-form_label-hint" i18n="true"
                      value="an editor can create and modify sites and pages"/></label>
    </div>
    <div class="checkbox">
        <label class="composum-form_checkbox-label form-check-label"><input
                class="composum-form_checkbox form-check-input" type="checkbox" name="data/role"
                value="publisher"/><span class="composum-form_label-value">${cpn:i18n(slingRequest,'Publisher')}</span>
            <cpn:text tagName="span" class="composum-form_label-hint" i18n="true"
                      value="a publisher can build and publish site releases"/></label>
    </div>
    <div class="checkbox">
        <label class="composum-form_checkbox-label form-check-label"><input
                class="composum-form_checkbox form-check-input" type="checkbox" name="data/role"
                value="developer"/><span class="composum-form_label-value">${cpn:i18n(slingRequest,'Developer')}</span>
            <cpn:text tagName="span" class="composum-form_label-hint" i18n="true"
                      value="a developer can create and modify components"/></label>
    </div>
    <div class="checkbox">
        <label class="composum-form_checkbox-label form-check-label"><input
                class="composum-form_checkbox form-check-input" type="checkbox" name="data/role"
                value="manager"/><span class="composum-form_label-value">${cpn:i18n(slingRequest,'Manager')}</span>
            <cpn:text tagName="span" class="composum-form_label-hint" i18n="true"
                      value="a manager can assign roles to users and configure a tenant"/></label>
    </div>
    <div class="checkbox">
        <label class="composum-form_checkbox-label form-check-label"><input
                class="composum-form_checkbox form-check-input" type="checkbox" name="data/role"
                value="visitor"/><span class="composum-form_label-value">${cpn:i18n(slingRequest,'Visitor')}</span>
            <cpn:text tagName="span" class="composum-form_label-hint" i18n="true"
                      value="a visitor can view preview content only"/></label>
    </div>
</cpn:component>
