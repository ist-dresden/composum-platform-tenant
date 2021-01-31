/**
 *
 *
 */
(function () {
    'use strict';
    CPM.namespace('platform.tenants');

    (function (tenants, components, core) {

        tenants.const = _.extend(tenants.const || {}, {
            replication: {
                css: {
                    base: 'composum-platform-tenant_replication',
                    _view: '-view',
                    site: {
                        base: 'tenant-replication',
                        _tabs: '_tabs',
                        _panel: '_panel'
                    },
                    config: 'composum-platform-replication-setup'
                },
                url: {
                    config: '/libs/composum/platform/tenant/manager/components/replication/config.html'
                }
            }
        });

        tenants.ReplicationView = Backbone.View.extend({

            initialize: function (options) {
                var c = tenants.const.replication.css.site;
                this.$tabs = this.$('.' + c.base + c._tabs);
                this.$panel = this.$('.' + c.base + c._panel);
                this.$sites = this.$tabs.find('li');
                this.$sites.find('a').click(_.bind(this.onSiteSelected, this));
                this.loadConfig();
                $(document).on('site:changed.ConfigSetup', _.bind(this.loadConfig, this));
            },

            onSiteSelected: function (event) {
                event.preventDefault();
                this.loadConfig($(event.currentTarget).closest('li').data('config'));
                return false;
            },

            loadConfig: function (configPath) {
                this.$sites.removeClass('active');
                if (!configPath) {
                    configPath = core.console.getProfile().get('tenants', 'replication');
                }
                if (!configPath || this.$sites.filter('[data-config="' + configPath + '"]').length === 0) {
                    configPath = this.$sites.first().data('config');
                }
                if (configPath) {
                    var u = tenants.const.replication.url;
                    core.console.getProfile().set('tenants', 'replication', configPath);
                    this.$sites.filter('[data-config="' + configPath + '"]').addClass('active');
                    core.getHtml(u.config + core.encodePath(configPath), _.bind(function (content) {
                        var c = tenants.const.replication.css;
                        this.$panel.html(content);
                        this.configSetup = core.getWidget(this.$el, '.' + c.config,
                            CPM.platform.replication.ConfigSetup);
                    }, this));
                } else {
                    this.$panel.html('');
                }
            }
        });

        tenants.onReplicationContentLoad = function () {
            var c = tenants.const.replication.css;
            tenants.replicationView = core.getView('.' + c.base + c._view, tenants.ReplicationView);
        };

    })(CPM.platform.tenants, CPM.core.components, CPM.core);
})();
