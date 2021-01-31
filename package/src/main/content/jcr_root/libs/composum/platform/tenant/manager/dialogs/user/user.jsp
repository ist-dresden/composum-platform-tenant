<%@page session="false" pageEncoding="utf-8" %>
<%@taglib prefix="sling" uri="http://sling.apache.org/taglibs/sling/1.2" %>
<%@taglib prefix="cpn" uri="http://sling.composum.com/cpnl/1.0" %>
<sling:defineObjects/>
<cpn:component id="bean" type="com.composum.platform.tenant.view.TenantUserBean">
    <div class="composum-platform-tenant_dialog dialog modal fade" role="dialog" aria-hidden="true">
        <div class="modal-dialog">
            <div class="modal-content form-panel default">
                <cpn:form classes="widget-form tenant-dialog_form" method="POST"
                          action="/bin/cpm/platform/tenants/user.change.json">
                    <div class="modal-header tenant-dialog_header">
                        <button type="button" class="tenant-dialog_button-clode close" data-dismiss="modal"
                                aria-label="${cpn:i18n(slingRequest, 'Close')}"><span
                                aria-hidden="true">&times;</span></button>
                        <sling:call script="title.jsp"/>
                    </div>
                    <div class="modal-body tenant-dialog_content">
                        <div class="tenant-dialog_messages messages">
                            <div class="alert hidden"></div>
                        </div>

                        <input name="_charset_" type="hidden" value="UTF-8"/>
                        <input name="tenant.id" type="hidden" value="${bean.tenant.id}"/>
                        <sling:call script="userid.jsp"/>
                        <sling:call script="roles.jsp"/>
                    </div>

                    <div class="modal-footer buttons">
                        <button type="button" class="btn btn-default cancel"
                                data-dismiss="modal">${cpn:i18n(slingRequest, 'Cancel')}</button>
                        <sling:call script="submit.jsp"/>
                    </div>
                </cpn:form>
            </div>
        </div>
    </div>
</cpn:component>