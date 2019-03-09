/**
 *
 *
 */
(function (window) {
    'use strict';

    window.tenants = window.tenants || {};

    (function (tenants, core) {

        tenants.current = {};

        tenants.const = {
            path: {
                root: '/etc/tenants'
            },
            link: {
                base: '/bin/platform/tenants',
                _view: '.view.html'
            },
            url: {
                base: '/bin/cpm/platform/tenants/manager',
                _tree: '.tree.json'
            }
        };

        tenants.getCurrentId = function () {
            return tenants.current ? tenants.current.id : undefined;
        };

        tenants.getCurrentPath = function () {
            return tenants.current ? tenants.current.path : undefined;
        };

        tenants.setCurrentPath = function (path, callback) {
            if (!tenants.current || tenants.current.path !== path) {
                if (path) {
                    var u = tenants.const.url;
                    core.getJson(u.base + u._tree + path, undefined, undefined,
                        _.bind(function (result) {
                            var l = tenants.const.link;
                            tenants.current = {
                                id: path.length > 13 ? path.substring(13) : '',
                                path: path,
                                node: result.responseJSON,
                                viewUrl: core.getContextUrl(l.base + l._view + window.core.encodePath(path)),
                                nodeUrl: core.getContextUrl(l.base + '.html' + window.core.encodePath(path))
                            };
                            core.console.getProfile().set('tenants', 'current', path);
                            if (history.replaceState) {
                                history.replaceState(tenants.current.path, name, tenants.current.nodeUrl);
                            }
                            $(document).trigger("path:selected", [path]);
                            if (_.isFunction(callback)) {
                                callback();
                            }
                        }, this));
                } else {
                    tenants.current = undefined;
                    $(document).trigger("path:selected", [path]);
                }
            }
        };

        tenants.Manager = core.components.SplitView.extend({

            initialize: function (options) {
                core.components.SplitView.prototype.initialize.apply(this, [options]);
                $(document).on('path:select', _.bind(this.onPathSelect, this));
                $(document).on('path:selected', _.bind(this.onPathSelected, this));
                $(document).on('path:changed', _.bind(this.onPathChanged, this));
                core.unauthorizedDelegate = core.console.authorize;
            },

            onPathSelect: function (event, path) {
                if (!path) {
                    path = event.data.path;
                }
                tenants.setCurrentPath(path);
            },

            onPathSelected: function (event, path) {
                tenants.tree.selectNode(path, _.bind(function (path) {
                    tenants.treeActions.refreshNodeState();
                }, this));
            },

            onPathChanged: function (event, path) {
                tenants.current = undefined;
                this.onPathSelected(event, path);
            }
        });

        tenants.manager = core.getView('#tenants', tenants.Manager);

        tenants.Tree = core.components.Tree.extend({

            nodeIdPrefix: 'TM_',

            getProfileId: function () {
                return 'tenants'
            },

            initialize: function (options) {
                var id = this.nodeIdPrefix + 'Tree';
                this.rootPath = tenants.const.path.root;
                this.initialSelect = this.$el.attr('data-selected');
                if (!this.initialSelect || this.initialSelect === '/') {
                    this.initialSelect = core.console.getProfile().get(this.getProfileId(), 'current', "/");
                }
                this.initializeFilter();
                core.components.Tree.prototype.initialize.apply(this, [options]);
                $(document).on('tenant:created.' + id, _.bind(this.onTenantCreated, this));
                $(document).on('tenant:changed.' + id, _.bind(this.onTenantChanged, this));
                $(document).on('tenant:deleted.' + id, _.bind(this.onTenantDeleted, this));
            },

            initializeFilter: function () {
            },

            dataUrlForPath: function (path) {
                var u = tenants.const.url;
                return u.base + u._tree + path;
            },

            onNodeSelected: function (path, node) {
                if (!this.suppressEvent) {
                    $(document).trigger("path:select", [path, node.original.name, node.original.type]);
                } else {
                    this.$el.trigger("node:selected", [path]);
                }
            },

            onTenantCreated: function (event, tenantId) {
                this.onPathInserted(event, tenants.const.path.root, tenantId);
            },

            onTenantChanged: function (event, tenantId) {
                this.onPathChanged(event, tenants.const.path.root + '/' + tenantId);
            },

            onTenantDeleted: function (event, tenantId) {
                this.onPathDeleted(event, tenants.const.path.root + '/' + tenantId);
            }
        });

        tenants.tree = core.getView('#tenants-tree', tenants.Tree);

        tenants.TreeActions = Backbone.View.extend({

            initialize: function (options) {
                this.tree = tenants.tree;
                this.$('button.create-tenant').on('click', _.bind(this.createTenant, this));
                this.$('button.activate-tenant').on('click', _.bind(this.activateTenant, this));
                this.$('button.delete-tenant').on('click', _.bind(this.deleteTenant, this));
                this.$('button.change-tenant').on('click', _.bind(this.changeTenant, this));
                this.$('button.refresh').on('click', _.bind(this.refreshNode, this));
            },

            getCurrent: function () {
                return tenants.current;
            },

            getCurrentPath: function () {
                return tenants.getCurrentPath();
            },

            createTenant: function (event, tenantId, callback) {
                if (event) {
                    event.preventDefault();
                }
                var dialog = tenants.getCreateTenantDialog();
                dialog.show(_.bind(function () {
                }, this), _.bind(function () {
                    if (_.isFunction(callback)) {
                        callback.call(this, tenantId);
                    }
                }, this));
                return false;
            },

            changeTenant: function (event, tenantId, callback) {
                if (event) {
                    event.preventDefault();
                }
                var dialog = tenants.getChangeTenantDialog();
                dialog.show(_.bind(function () {
                    if (tenantId || (tenantId = tenants.getCurrentId())) {
                        dialog.$id.val(tenantId);
                    }
                }, this), _.bind(function () {
                    if (_.isFunction(callback)) {
                        callback.call(this, tenantId);
                    }
                }, this));
                return false;
            },

            deleteTenant: function (event, tenantId, callback) {
                if (event) {
                    event.preventDefault();
                }
                if (tenantId || (tenantId = tenants.getCurrentId())) {
                    var dialog = tenants.getDeleteTenantDialog();
                    dialog.show(_.bind(function () {
                        dialog.$id.val(tenantId);
                    }, this), _.bind(function () {
                        if (_.isFunction(callback)) {
                            callback.call(this, tenantId);
                        }
                    }, this));
                }
                return false;
            },

            activateTenant: function (event, tenantId, callback) {
                if (event) {
                    event.preventDefault();
                }
                var dialog = tenants.getActivateTenantDialog();
                dialog.show(_.bind(function () {
                    if (tenantId || (tenantId = tenants.getCurrentId())) {
                        dialog.$id.val(tenantId);
                    }
                }, this), _.bind(function () {
                    if (_.isFunction(callback)) {
                        callback.call(this, tenantId);
                    }
                }, this));
                return false;
            },

            /**
             * adjust actions state according to the current node state
             */
            refreshNodeState: function () {
            },

            /**
             * redraw tree node identified by the path
             */
            refreshNode: function (event, path, callback) {
                if (event) {
                    event.preventDefault();
                }
                if (!path) {
                    path = tenants.getCurrentPath();
                }
                var id = undefined;
                if (path) {
                    id = this.tree.nodeId(path);
                }
                tenants.tree.refreshNodeById(id);
                if (_.isFunction(callback)) {
                    callback.call(this, path);
                } else {
                    this.refreshNodeState();
                }
            }
        });

        tenants.treeActions = core.getView('.tree-actions', tenants.TreeActions);

        //
        // detail view (console)
        //

        tenants.detailViewTabTypes = [{
            selector: '> .root-detail',
            tabType: tenants.RootTab
        }, {
            selector: '> .tenant-detail',
            tabType: tenants.TenantTab
        }, {
            selector: '> .tenant-users',
            tabType: tenants.UsersTab
        }, {
            selector: '> .tenant-sites',
            tabType: tenants.SitesTab
        }, {
            // the fallback to the basic implementation as a default rule
            selector: '> div',
            tabType: core.console.DetailTab
        }];

        /**
         * the node view (node detail) which controls the node view tabs
         */
        tenants.DetailView = core.console.DetailView.extend({

            getProfileId: function () {
                return 'tenants';
            },

            getCurrentPath: function () {
                return tenants.current ? tenants.current.path : undefined;
            },

            getViewUri: function () {
                return tenants.current.viewUrl;
            },

            getTabUri: function (name) {
                var l = tenants.const.link;
                return l.base + '.tab.' + name + '.html';
            },

            getTabTypes: function () {
                return tenants.detailViewTabTypes;
            },

            initialize: function (options) {
                core.console.DetailView.prototype.initialize.apply(this, [options]);
                $(document).on('tenant:changed.DetailView', _.bind(function (event, tenantId) {
                    if (tenantId === tenants.getCurrentId()) {
                        this.reload();
                    }
                }, this));
            }
        });

        tenants.detailView = core.getView('#tenants-view', tenants.DetailView);

    })(window.tenants, window.core);

})(window);
