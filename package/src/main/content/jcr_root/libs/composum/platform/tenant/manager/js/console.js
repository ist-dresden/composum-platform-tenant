/**
 *
 *
 */
(function () {
    'use strict';
    CPM.namespace('platform.tenants');

    (function (tenants, core) {

        tenants.AbstractManagerTab = core.console.DetailTab.extend({

            initialize: function (options) {
                core.console.DetailTab.prototype.initialize.apply(this, [options]);
                $(document).on('detail:reload', _.bind(this.reloadTab, this));
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

            initialize: function (options) {
                tenants.AbstractManagerTab.prototype.initialize.apply(this, [options]);
                $(document).off('detail:reload.users').on('detail:reload.users', _.bind(this.reloadTab, this));
            },

            initContent: function () {
                tenants.AbstractManagerTab.prototype.initContent.apply(this);
                this.$('.detail-toolbar .reload').click(_.bind(this.reloadTab, this));
            },

            reloadTab: function (event) {
                if (event) {
                    event.preventDefault();
                }
                tenants.detailView.refreshContent();
                return false;
            },

            reload: function () {
                tenants.onUsersTableLoad();
            }
        });

        tenants.HostsTab = tenants.AbstractManagerTab.extend({

            initialize: function (options) {
                tenants.AbstractManagerTab.prototype.initialize.apply(this, [options]);
            },

            initContent: function () {
                tenants.AbstractManagerTab.prototype.initContent.apply(this);
                this.$('.detail-toolbar .reload').click(_.bind(this.reloadTab, this));
            },

            reloadTab: function (event) {
                if (event) {
                    event.preventDefault();
                }
                if (tenants.hostsView) {
                    tenants.hostsView.hostsBusyOn();
                }
                tenants.detailView.refreshContent();
                return false;
            },

            reload: function () {
                tenants.onHostsContentLoad();
            }
        });

        tenants.InboxTab = CPM.platform.workflow.InboxConsoleTab.extend({

            initialize: function (options) {
                CPM.platform.workflow.InboxConsoleTab.prototype.initialize.apply(this, [options]);
                $(document).off('scope:changed.inbox').on('scope:changed.inbox', _.bind(this.reloadTab, this));
                $(document).off('detail:reload.inbox').on('detail:reload.inbox', _.bind(this.reloadTab, this));
            },

            initContent: function () {
                CPM.platform.workflow.InboxConsoleTab.prototype.initContent.apply(this);
                this.$('.detail-toolbar .reload').click(_.bind(this.reloadTab, this));
            },

            reloadTab: function (event) {
                if (event) {
                    event.preventDefault();
                }
                tenants.detailView.refreshContent(undefined, undefined, this.reloadParameters());
                return false;
            },

            reload: function () {
                CPM.platform.workflow.onTableLoad();
            }
        });

        tenants.ReplicationTab = tenants.AbstractManagerTab.extend({

            initialize: function (options) {
                tenants.AbstractManagerTab.prototype.initialize.apply(this, [options]);
            },

            initContent: function () {
                tenants.AbstractManagerTab.prototype.initContent.apply(this);
                this.$('.detail-toolbar .reload').click(_.bind(this.reloadTab, this));
            },

            reloadTab: function (event) {
                if (event) {
                    event.preventDefault();
                }
                tenants.detailView.refreshContent();
                return false;
            },

            reload: function () {
                tenants.onReplicationContentLoad();
            }
        });

    })(CPM.platform.tenants, CPM.core);

})();
