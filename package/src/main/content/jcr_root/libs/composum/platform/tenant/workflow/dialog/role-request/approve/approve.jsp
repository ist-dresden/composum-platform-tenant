<%@page session="false" pageEncoding="UTF-8" %>
<%@taglib prefix="sling" uri="http://sling.apache.org/taglibs/sling/1.2" %>
<%@taglib prefix="cpn" uri="http://sling.composum.com/cpnl/1.0" %>
<sling:defineObjects/>
<cpn:component id="wfDialog" type="com.composum.platform.workflow.model.WorkflowDialogModel" scope="request">
    <div>
        <label class="control-label">Select Roles</label>
    </div>
    <div>
        <input class="composum-form_checkbox form-check-input" type="checkbox" name="data/role"
               value="editor"/><label
            class="composum-form_checkbox-label form-check-label">${cpn:i18n(slingRequest,'Editor')}
        <cpn:text tagName="span" class="composum-form_label-hint" i18n="true"
                  value="an editor can create and modify sites and pages"/></label>
    </div>
    <div>
        <input class="composum-form_checkbox form-check-input" type="checkbox" name="data/role"
               value="publisher"/><label
            class="composum-form_checkbox-label form-check-label">${cpn:i18n(slingRequest,'Publisher')}
        <cpn:text tagName="span" class="composum-form_label-hint" i18n="true"
                  value="a publisher can build and publish site releases"/></label>
    </div>
    <div>
        <input class="composum-form_checkbox form-check-input" type="checkbox" name="data/role"
               value="developer"/><label
            class="composum-form_checkbox-label form-check-label">${cpn:i18n(slingRequest,'Developer')}
        <cpn:text tagName="span" class="composum-form_label-hint" i18n="true"
                  value="a developer can create and modify components"/></label>
    </div>
    <div>
        <input class="composum-form_checkbox form-check-input" type="checkbox" name="data/role"
               value="manager"/><label
            class="composum-form_checkbox-label form-check-label">${cpn:i18n(slingRequest,'Manager')}
        <cpn:text tagName="span" class="composum-form_label-hint" i18n="true"
                  value="a manager can assign roles to users and configure a tenant"/></label>
    </div>
    <div>
        <input class="composum-form_checkbox form-check-input" type="checkbox" name="data/role"
               value="visitor"/><label
            class="composum-form_checkbox-label form-check-label">${cpn:i18n(slingRequest,'Visitor')}
        <cpn:text tagName="span" class="composum-form_label-hint" i18n="true"
                  value="a visitor can view preview content only"/></label>
    </div>
</cpn:component>
