/**
 *
 *
 */
(function (window) {
    'use strict';

    window.tenants = window.tenants || {};

    (function (tenants, core) {

        tenants.const = _.extend(tenants.const || {}, {
            home: {
                css: {
                    base: 'composum-platform-tenant',
                    _memberForm: '_home_member-request-form',
                    _tenantForm: '_home_tenant-request-form'
                },
                url: {
                    wf: {
                        base: '/bin/cpm/platform/workflow',
                        _addTask: '.addTask.json'
                    }
                }
            }
        });

        tenants.PageRequestForm = core.components.FormWidget.extend({

            initialize: function (options) {
                core.components.FormWidget.prototype.initialize.apply(this, [options]);
                window.widgets.setUp(this.$el);
                this.$el.on('submit', _.bind(this.sendMemberRequest, this));
            },

            sendMemberRequest: function (event) {
                event.preventDefault();
                var $alert = $('.alert.alert-danger');
                $alert.addClass('hidden');
                tenants.memberRequestForm.validationReset();
                tenants.tenantRequestForm.validationReset();
                if (this.isValid()) {
                    this.submitForm(_.bind(function (result) {
                        window.location.reload();
                    }, this), _.bind(function (result) {
                        $alert.closest('.submission-alert').removeClass('hidden');
                    }, this));
                } else {
                    $alert.closest('.validation-alert').removeClass('hidden');
                }
                return false;
            }
        });

        tenants.memberRequestForm = core.getView('.'
            + tenants.const.home.css.base + tenants.const.home.css._memberForm, tenants.PageRequestForm);

        tenants.tenantRequestForm = core.getView('.'
            + tenants.const.home.css.base + tenants.const.home.css._tenantForm, tenants.PageRequestForm);

    })(window.tenants, window.core);

})(window);
