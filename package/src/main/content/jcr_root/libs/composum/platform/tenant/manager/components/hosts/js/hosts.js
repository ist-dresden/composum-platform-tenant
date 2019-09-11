/**
 *
 *
 */
(function (window) {
    'use strict';

    window.tenants = window.tenants || {};

    (function (tenants, core) {

        tenants.const = _.extend(tenants.const || {}, {
            hosts: {
                css: {
                    base: 'composum-platform-tenant_hosts',
                    _toolbar: '-toolbar',
                    _content: '-content'
                },
                dlg: {
                    base: '/libs/composum/platform/tenant/manager/dialogs/host',
                    _add: '/add.html',
                    _drop: '/drop.html',
                    _remove: '/remove.html',
                    _revoke: '/revoke.html'
                }
            }
        });

        tenants.HostDialog = core.components.FormDialog.extend({

            doSubmit: function (callback) {
                this.submitForm(callback);
            }
        });

        tenants.HostView = Backbone.View.extend({

            initialize: function (options) {
                this.data = JSON.parse(atob(this.$el.data('host')));
                this.$('.tenant-host_title .tenant-host_delete').click(_.bind(this.deleteHost, this));
            },

            deleteHost: function (event) {
                if (event) {
                    event.preventDefault();
                }
                var u = tenants.const.hosts.dlg;
                core.getHtml(u.base + u._remove + this.hosts.path
                    + '?hostname=' + encodeURIComponent(this.data.hostname),
                    _.bind(function (content) {
                        core.showFormDialog(tenants.HostDialog, content, undefined, _.bind(function () {
                            if (_.isFunction(this.hosts.onHostAction)) {
                                this.hosts.onHostAction();
                            }
                        }, this));
                    }, this));
                return false;
            }
        });

        tenants.HostsView = Backbone.View.extend({

            initialize: function (options) {
                var c = tenants.const.hosts.css;
                this.path = this.$el.data('path');
                this.tenant = this.$el.data('tenant');
                this.hosts = [];
                var self = this;
                this.$('.tenant-hosts_host').each(function () {
                    var view = core.getView(this, tenants.HostView);
                    self.hosts.push(view);
                    view.hosts = self;
                });
            },

            addHost: function (event) {
                if (event) {
                    event.preventDefault();
                }
                var u = tenants.const.hosts.dlg;
                core.getHtml(u.base + u._add + this.path, _.bind(function (content) {
                    core.showFormDialog(tenants.HostDialog, content, undefined, _.bind(function () {
                        if (_.isFunction(this.onHostAction)) {
                            this.onHostAction();
                        }
                    }, this));
                }, this));
                return false;
            },

            onHostAction: function () {
                $(document).trigger('detail:reload', [this.path]);
            }
        });

        tenants.HostsToolbar = Backbone.View.extend({

            initialize: function (options) {
                this.$add = this.$('.add');
                this.$add.off('click').click(_.bind(tenants.hostsView.addHost, tenants.hostsView));
            }
        });

        tenants.onHostsContentLoad = function () {
            var c = tenants.const.hosts.css;
            tenants.hostsView = core.getView('.' + c.base + c._content, tenants.HostsView);
            tenants.hostsToolbar = core.getView('.' + c.base + c._toolbar, tenants.HostsToolbar);
        };

    })(window.tenants, window.core);

})(window);
