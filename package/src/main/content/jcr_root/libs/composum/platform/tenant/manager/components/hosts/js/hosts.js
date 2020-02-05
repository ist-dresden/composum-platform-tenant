/**
 *
 *
 */
(function () {
    'use strict';
    CPM.namespace('platform.tenants');

    (function (tenants, core) {

        tenants.const = _.extend(tenants.const || {}, {
            hosts: {
                css: {
                    base: 'composum-platform-tenant_hosts',
                    _toolbar: '-toolbar',
                    _content: '-content',
                    status: {
                        base: 'tenant-host_status',
                        _enabled: '_enabled',
                        _configured: '_configured',
                        _certificate: '_certificate',
                        _secured: '_secured',
                        _toggle: '_toggle'
                    },
                    site: {
                        base: 'tenant-host_site',
                        _assign: '_assign',
                        _remove: '_remove'
                    }
                },
                dlg: {
                    host: {
                        base: '/libs/composum/platform/tenant/manager/dialogs/host',
                        _add: '/add.html',
                        _drop: '/drop.html',
                        _remove: '/remove.html',
                        _revoke: '/revoke.html'
                    },
                    site: {
                        base: '/libs/composum/platform/tenant/manager/dialogs/site',
                        _assign: '.html',
                        _remove: '/remove.html'
                    }
                },
                action: {
                    base: '/bin/cpm/platform/tenants/host',
                    _enable: '.enable.json',
                    _disable: '.disable.json',
                    _create: '.create.json',
                    _cert: '.cert.json',
                    _secure: '.secure.json',
                    _unsecure: '.unsecure.json'
                },
                load: {
                    host: '/libs/composum/platform/tenant/manager/components/hosts.item.in.html'
                }
            }
        });

        tenants.HostDialog = core.components.FormDialog.extend({

            initAction: function (action) {
                var $form = this.form.$el;
                $form.attr('action', $form.attr('action').replace('{action}', action));
            },

            doSubmit: function (callback) {
                tenants.hostsView.hostsBusyOn();
                this.submitForm(callback, undefined, tenants.hostsView.hostsBusyOff);
            }
        });

        tenants.HostView = Backbone.View.extend({

            initialize: function (options) {
                this.initItem();
            },

            initItem: function () {
                this.$item = this.$('.tenant-hosts_item');
                this.data = JSON.parse(atob(this.$item.data('host')));
                if (!this.data.locked) {
                    this.$('.tenant-host_title .tenant-host_delete').click(_.bind(this.removeHost, this));
                    var c = tenants.const.hosts.css.status;
                    this.$('.' + c.base + c._enabled + '.enabled .' + c.base + c._toggle).click(_.bind(this.disableHost, this));
                    this.$('.' + c.base + c._enabled + '.disabled .' + c.base + c._toggle).click(_.bind(this.enableHost, this));
                    this.$('.' + c.base + c._configured + '.unconfigured .' + c.base + c._toggle).click(_.bind(this.createHost, this));
                    this.$('.' + c.base + c._configured + '.configured .' + c.base + c._toggle).click(_.bind(this.dropHost, this));
                    this.$('.' + c.base + c._certificate + '.nocertificate .' + c.base + c._toggle).click(_.bind(this.hostCert, this));
                    this.$('.' + c.base + c._certificate + '.certificate .' + c.base + c._toggle).click(_.bind(this.revokeCert, this));
                    this.$('.' + c.base + c._secured + '.unsecure .' + c.base + c._toggle).click(_.bind(this.secureHost, this));
                    this.$('.' + c.base + c._secured + '.secured .' + c.base + c._toggle).click(_.bind(this.unsecureHost, this));
                    c = tenants.const.hosts.css.site;
                    this.$('.' + c.base + c._assign + '.enabled').click(_.bind(this.assignSite, this));
                    this.$('.' + c.base + c._remove + '.enabled').click(_.bind(this.removeSite, this));
                }
            },

            enableHost: function (event) {
                event.preventDefault();
                this.hosts.hostsBusyOn();
                var u = tenants.const.hosts.action;
                core.ajaxPost(u.base + u._enable + this.hosts.path, {
                    hostname: this.data.hostname
                }, {}, _.bind(this.reloadHost, this), this.onError);
                return false;
            },

            disableHost: function (event) {
                event.preventDefault();
                this.hosts.hostsBusyOn();
                var u = tenants.const.hosts.action;
                core.ajaxPost(u.base + u._disable + this.hosts.path, {
                    hostname: this.data.hostname
                }, {}, _.bind(this.reloadHost, this), this.onError);
                return false;
            },

            createHost: function (event) {
                event.preventDefault();
                this.hosts.hostsBusyOn();
                var u = tenants.const.hosts.action;
                core.ajaxPost(u.base + u._create + this.hosts.path, {
                    hostname: this.data.hostname
                }, {}, _.bind(this.reloadHost, this), this.onError);
                return false;
            },

            dropHost: function (event) {
                event.preventDefault();
                var u = tenants.const.hosts.dlg.host;
                core.getHtml(u.base + u._drop + this.hosts.path
                    + '?hostname=' + encodeURIComponent(this.data.hostname),
                    _.bind(function (content) {
                        core.showFormDialog(tenants.HostDialog, content, {}, _.bind(function (dialog) {
                            dialog.initAction('delete');
                        }, this), _.bind(this.reloadHost, this));
                    }, this));
                return false;
            },

            hostCert: function (event) {
                event.preventDefault();
                this.hosts.hostsBusyOn();
                var u = tenants.const.hosts.action;
                core.ajaxPost(u.base + u._cert + this.hosts.path, {
                    hostname: this.data.hostname
                }, {}, _.bind(this.reloadHost, this), this.onError);
                return false;
            },

            revokeCert: function (event) {
                event.preventDefault();
                var u = tenants.const.hosts.dlg.host;
                core.getHtml(u.base + u._revoke + this.hosts.path
                    + '?hostname=' + encodeURIComponent(this.data.hostname),
                    _.bind(function (content) {
                        core.showFormDialog(tenants.HostDialog, content, {}, _.bind(function (dialog) {
                            dialog.initAction('revoke');
                        }, this), _.bind(this.hosts.onHostsAction, this.hosts));
                    }, this));
                return false;
            },

            secureHost: function (event) {
                event.preventDefault();
                this.hosts.hostsBusyOn();
                var u = tenants.const.hosts.action;
                core.ajaxPost(u.base + u._secure + this.hosts.path, {
                    hostname: this.data.hostname
                }, {}, _.bind(this.reloadHost, this), this.onError);
                return false;
            },

            unsecureHost: function (event) {
                event.preventDefault();
                this.hosts.hostsBusyOn();
                var u = tenants.const.hosts.action;
                core.ajaxPost(u.base + u._unsecure + this.hosts.path, {
                    hostname: this.data.hostname
                }, {}, _.bind(this.reloadHost, this), this.onError);
                return false;
            },

            removeHost: function (event) {
                event.preventDefault();
                var u = tenants.const.hosts.dlg.host;
                core.getHtml(u.base + u._remove + this.hosts.path
                    + '?hostname=' + encodeURIComponent(this.data.hostname),
                    _.bind(function (content) {
                        core.showFormDialog(tenants.HostDialog, content, {}, _.bind(function (dialog) {
                            dialog.initAction('remove');
                        }, this), _.bind(this.hosts.onHostsAction, this.hosts));
                    }, this));
                return false;
            },

            assignSite: function (event) {
                event.preventDefault();
                var u = tenants.const.hosts.dlg.site;
                core.getHtml(u.base + u._assign + this.hosts.path
                    + '?hostname=' + encodeURIComponent(this.data.hostname),
                    _.bind(function (content) {
                        core.showFormDialog(tenants.HostDialog, content, {}, _.bind(function (dialog) {
                        }, this), _.bind(this.reloadHost, this));
                    }, this));
                return false;
            },

            removeSite: function (event) {
                event.preventDefault();
                var u = tenants.const.hosts.dlg.site;
                core.getHtml(u.base + u._remove + this.hosts.path
                    + '?hostname=' + encodeURIComponent(this.data.hostname),
                    _.bind(function (content) {
                        core.showFormDialog(tenants.HostDialog, content, {}, _.bind(function (dialog) {
                        }, this), _.bind(this.reloadHost, this));
                    }, this));
                return false;
            },

            reloadHost: function () {
                this.hosts.hostsBusyOn();
                var u = tenants.const.hosts.load.host;
                core.getHtml(u + this.hosts.path + '?hostname=' + this.data.hostname,
                    _.bind(function (content) {
                        this.$el.html(content);
                        this.initItem();
                    }, this), undefined, this.hosts.hostsBusyOff);
            },

            onError: function (xhr) {
                tenants.hostsView.hostsBusyOff();
                core.alert(xhr);
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
                var u = tenants.const.hosts.dlg.host;
                core.getHtml(u.base + u._add + this.path, _.bind(function (content) {
                    core.showFormDialog(tenants.HostDialog, content, {}, _.bind(function (dialog) {
                        dialog.initAction('add');
                    }, this), _.bind(this.onHostsAction, this));
                }, this));
                return false;
            },

            hostsBusyOn: function () {
                var $busy = $('.composum-platform-tenant_hosts-busy');
                $busy.removeClass('hidden');
            },

            hostsBusyOff: function () {
                var $busy = $('.composum-platform-tenant_hosts-busy');
                $busy.addClass('hidden');
            },

            onHostsAction: function () {
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
            tenants.hostsView.hostsBusyOff();
        };

    })(CPM.platform.tenants, CPM.core);

})();
