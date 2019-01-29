/**
 *
 *
 */
(function (window) {
    'use strict';

    window.tenants = window.tenants || {};

    (function (tenants, core) {

        tenants.AbstractManagerTab = core.console.DetailTab.extend({

            initialize: function (options) {
                core.console.DetailTab.prototype.initialize.apply(this, [options]);
                this.initContent();
            },

            initContent: function () {
                window.widgets.setUp(this.el);
            },

            reloadTab: function (event) {
                if (event) {
                    event.preventDefault();
                }
                tenants.detailView.refreshContent();
                return false;
            }
        });

    })(window.tenants, window.core);

})(window);
