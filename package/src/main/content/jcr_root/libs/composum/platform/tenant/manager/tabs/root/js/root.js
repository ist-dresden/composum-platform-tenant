/**
 *
 *
 */
(function (window) {
    'use strict';

    window.tenants = window.tenants || {};

    (function (tenants, core) {

        tenants.RootTab = tenants.AbstractManagerTab.extend({

            initContent: function () {
                tenants.AbstractManagerTab.prototype.initContent.apply(this);
                this.$('.detail-toolbar .create').click(_.bind(this.createTenant, this));
                this.$('.detail-toolbar .reload').click(_.bind(this.reloadTab, this));
            },

            createTenant: function (event) {
                tenants.treeActions.createTenant(event);
            }
        });

    })(window.tenants, window.core);

})(window);
