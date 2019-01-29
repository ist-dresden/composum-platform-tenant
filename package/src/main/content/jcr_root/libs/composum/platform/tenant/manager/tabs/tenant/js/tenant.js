/**
 *
 *
 */
(function (window) {
    'use strict';

    window.tenants = window.tenants || {};

    (function (tenants, core) {

        tenants.TenantTab = tenants.AbstractManagerTab.extend({

            initContent: function () {
                tenants.AbstractManagerTab.prototype.initContent.apply(this);
                this.$('.detail-toolbar .delete').click(_.bind(this.deleteTenant, this));
                this.$('.detail-toolbar .reload').click(_.bind(this.reloadTab, this));
            },

            deleteTenant: function (event) {
                tenants.treeActions.deleteTenant(event);
            }
        });

        tenants.UsersTab = tenants.AbstractManagerTab.extend({

            initContent: function () {
                tenants.AbstractManagerTab.prototype.initContent.apply(this);
                this.$('.detail-toolbar .join').click(_.bind(this.joinUser, this));
                this.$('.detail-toolbar .remove').click(_.bind(this.removeUser, this));
                this.$('.detail-toolbar .reload').click(_.bind(this.reloadTab, this));
            },

            joinUser: function (event) {
                tenants.treeActions.deleteTenant(event);
            },

            removeUser: function (event) {
                tenants.treeActions.deleteTenant(event);
            }
        });

        tenants.SitesTab = tenants.AbstractManagerTab.extend({

            initContent: function () {
                tenants.AbstractManagerTab.prototype.initContent.apply(this);
                this.$('.detail-toolbar .reload').click(_.bind(this.reloadTab, this));
            }
        });

    })(window.tenants, window.core);

})(window);
