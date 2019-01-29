<%@page session="false" pageEncoding="utf-8" %>
<%@taglib prefix="sling" uri="http://sling.apache.org/taglibs/sling/1.2" %>
<%@taglib prefix="cpn" uri="http://sling.composum.com/cpnl/1.0" %>
<sling:defineObjects/>
<cpn:component id="manager" type="com.composum.platform.tenant.view.TenantManagerBean" scope="request">
    <div id="create-tenant-dialog" class="dialog modal fade" role="dialog" aria-hidden="true">
        <div class="modal-dialog">
            <div class="modal-content form-panel default">
                <cpn:form classes="widget-form" action="/bin/cpm/platform/tenants.create.json" method="POST">
                    <div class="modal-header">
                        <button type="button" class="close" data-dismiss="modal"
                                aria-label="${cpn:i18n(slingRequest, 'Close')}"><span
                                aria-hidden="true">&times;</span></button>
                        <cpn:text tagName="h4" class="modal-title" i18n="true">Create a new Tenant</cpn:text>
                    </div>
                    <div class="modal-body">
                        <div class="messages">
                            <div class="alert"></div>
                        </div>

                        <input name="_charset_" type="hidden" value="UTF-8"/>
                        <div class="row">
                            <div class="col-xs-4">
                                <div class="form-group">
                                    <label class="control-label">Tenant-ID</label>
                                    <input name="tenant.id" class="widget text-field-widget form-control"
                                           type="text" data-rules="mandatory"/>
                                </div>
                            </div>
                            <div class="col-xs-8">
                                <div class="form-group">
                                    <label class="control-label">Tenant Name (Title)</label>
                                    <input name="tenant.name" class="widget text-field-widget form-control"
                                           type="text"/>
                                </div>
                            </div>
                        </div>
                        <div class="form-group">
                            <label class="control-label">Description</label>
                            <input name="tenant.description" class="widget text-area-widget form-control" type="text"/>
                        </div>
                    </div>

                    <div class="modal-footer buttons">
                        <button type="button" class="btn btn-default cancel"
                                data-dismiss="modal">${cpn:i18n(slingRequest, 'Cancel')}</button>
                        <button type="submit"
                                class="btn btn-primary create">${cpn:i18n(slingRequest, 'Create')}</button>
                    </div>
                </cpn:form>
            </div>
        </div>
    </div>
</cpn:component>