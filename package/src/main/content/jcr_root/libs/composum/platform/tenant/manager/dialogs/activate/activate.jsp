<%@page session="false" pageEncoding="utf-8" %>
<%@taglib prefix="sling" uri="http://sling.apache.org/taglibs/sling/1.2" %>
<%@taglib prefix="cpn" uri="http://sling.composum.com/cpnl/1.0" %>
<sling:defineObjects/>
<cpn:component id="model" type="com.composum.platform.tenant.view.TenantBean" scope="request">
    <div id="activate-tenant-dialog" class="dialog modal fade" role="dialog" aria-hidden="true">
        <div class="modal-dialog">
            <div class="modal-content form-panel default">
                <cpn:form classes="widget-form" action="/bin/cpm/platform/tenants/manager.activate.json" method="POST">
                    <div class="modal-header">
                        <button type="button" class="close" data-dismiss="modal"
                                aria-label="${cpn:i18n(slingRequest, 'Close')}"><span
                                aria-hidden="true">&times;</span></button>
                        <cpn:text tagName="h4" class="modal-title" i18n="true">Activate a Tenant</cpn:text>
                    </div>
                    <div class="modal-body">
                        <div class="messages">
                            <div class="alert"></div>
                        </div>

                        <input name="_charset_" type="hidden" value="UTF-8"/>
                        <div class="row">
                            <div class="col-xs-4">
                                <div class="form-group widget text-field-widget tenant-id" data-rules="required">
                                    <label class="control-label">Tenant-ID</label>
                                    <input name="tenant.id" class="form-control" type="text"/>
                                    <div class="hint  text-${model.tenant.active?'success':'danger'}">${model.tenant.status}</div>
                                </div>
                            </div>
                            <div class="col-xs-8">
                                <div class="hint">${cpn:text('This will reanimate the tenant with the given id - useful only if tenant is deactivated.')}</div>
                            </div>
                        </div>
                    </div>

                    <div class="modal-footer buttons">
                        <button type="button" class="btn btn-default cancel"
                                data-dismiss="modal">${cpn:i18n(slingRequest, 'Cancel')}</button>
                        <button type="submit"
                                class="btn btn-primary change">${cpn:i18n(slingRequest, 'Activate')}</button>
                    </div>
                </cpn:form>
            </div>
        </div>
    </div>
</cpn:component>