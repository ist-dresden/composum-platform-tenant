/**
 *
 *
 */
(function () {
    'use strict';
    CPM.namespace('platform.tenants');

    (function (tenants, core) {

        tenants.getCreateTenantDialog = function () {
            return core.getView('#create-tenant-dialog', tenants.CreateTenantDialog);
        };

        tenants.CreateTenantDialog = core.components.Dialog.extend({

            initialize: function (options) {
                core.components.Dialog.prototype.initialize.apply(this, [options]);
                this.form = core.getWidget(this.el, 'form.widget-form', core.components.FormWidget);
                this.$id = this.$('input[name="tenant.id"]');
                this.form.onsubmit = _.bind(this.createTenant, this);
                this.$('button.create').click(_.bind(this.createTenant, this));
                this.$el.on('shown.bs.modal', _.bind(function () {
                    this.$id.focus();
                }, this));
            },

            createTenant: function (event) {
                event.preventDefault();
                if (this.form.isValid()) {
                    var tenantId = this.$id.val();
                    this.submitForm(function (result) {
                        $(document).trigger("tenant:created", [tenantId]);
                    });
                } else {
                    this.alert('danger', 'a tenant id must be specified');
                }
                return false;
            }
        });

        tenants.getChangeTenantDialog = function () {
            return core.getView('#change-tenant-dialog', tenants.ChangeTenantDialog);
        };

        tenants.ChangeTenantDialog = core.components.Dialog.extend({

            initialize: function (options) {
                core.components.Dialog.prototype.initialize.apply(this, [options]);
                this.form = core.getWidget(this.el, 'form.widget-form', core.components.FormWidget);
                this.$id = this.$('input[name="tenant.id"]');
                this.form.onsubmit = _.bind(this.changeTenant, this);
                this.$('button.change').click(_.bind(this.changeTenant, this));
                this.$el.on('shown.bs.modal', _.bind(function () {
                    this.$id.focus();
                }, this));
            },

            changeTenant: function (event) {
                event.preventDefault();
                if (this.form.isValid()) {
                    var tenantId = this.$id.val();
                    this.submitForm(function (result) {
                        $(document).trigger("tenant:changed", [tenantId]);
                    });
                } else {
                    this.alert('danger', 'a tenant id must be specified');
                }
                return false;
            }
        });

        tenants.getDeleteTenantDialog = function () {
            return core.getView('#delete-tenant-dialog', tenants.DeleteTenantDialog);
        };

        tenants.DeleteTenantDialog = core.components.Dialog.extend({

            initialize: function (options) {
                core.components.Dialog.prototype.initialize.apply(this, [options]);
                this.form = core.getWidget(this.el, 'form.widget-form', core.components.FormWidget);
                this.$id = this.$('input[name="tenant.id"]');
                this.form.onsubmit = _.bind(this.deleteTenant, this);
                this.$('button.delete').click(_.bind(this.deleteTenant, this));
                this.$el.on('shown.bs.modal', _.bind(function () {
                    this.$id.focus();
                }, this));
            },

            deleteTenant: function (event) {
                event.preventDefault();
                if (this.form.isValid()) {
                    var tenantId = this.$id.val();
                    this.submitForm(function (result) {
                        if (result.tenant.status && result.tenant.status === 'deactivated') {
                            $(document).trigger("tenant:changed", [tenantId]);
                        } else {
                            $(document).trigger("tenant:deleted", [tenantId]);
                        }
                    });
                } else {
                    this.alert('danger', 'a tenant id must be specified');
                }
                return false;
            }
        });

        tenants.getActivateTenantDialog = function () {
            return core.getView('#activate-tenant-dialog', tenants.ActivateTenantDialog);
        };

        tenants.ActivateTenantDialog = core.components.Dialog.extend({

            initialize: function (options) {
                core.components.Dialog.prototype.initialize.apply(this, [options]);
                this.form = core.getWidget(this.el, 'form.widget-form', core.components.FormWidget);
                this.$id = this.$('input[name="tenant.id"]');
                this.form.onsubmit = _.bind(this.activateTenant, this);
                this.$('button.change').click(_.bind(this.activateTenant, this));
                this.$el.on('shown.bs.modal', _.bind(function () {
                    this.$id.focus();
                }, this));
            },

            activateTenant: function (event) {
                event.preventDefault();
                if (this.form.isValid()) {
                    var tenantId = this.$id.val();
                    this.submitForm(function (result) {
                        $(document).trigger("tenant:changed", [tenantId]);
                    });
                } else {
                    this.alert('danger', 'a tenant id must be specified');
                }
                return false;
            }
        });

    })(CPM.platform.tenants, CPM.core);

})();
