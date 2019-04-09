/**
 *
 *
 */
(function (window) {
    'use strict';

    window.tenants = window.tenants || {};

    (function (tenants, core) {

        tenants.const = _.extend(tenants.const || {}, {
            users: {
                css: {
                    base: 'composum-platform-tenant_users',
                    _toolbar: '-toolbar',
                    _table: '-table',
                    _item: '-item'
                },
                dlg: {
                    base: '/libs/composum/platform/tenant/manager/dialogs/user',
                    _add: '/add.html',
                    _change: '/change.html',
                    _remove: '/remove.html'
                }
            }
        });

        tenants.UserDialog = core.components.FormDialog.extend({

            doSubmit: function (callback) {
                this.submitForm(callback);
            }
        });

        tenants.UsersView = Backbone.View.extend({

            initialize: function (options) {
                this.initContent();
            },

            initContent: function () {
                var c = tenants.const.users.css;
                this.path = this.$el.data('path');
                this.$users = this.$('.' + c.base + c._item).click(_.bind(this.selectUser, this));
                this.$selected = [];
            },

            selectUser: function (event) {
                var c = tenants.const.users.css;
                event.preventDefault();
                this.$selected = $(event.currentTarget).closest('.' + c.base + c._item);
                if (_.isFunction(this.onUserSelected)) {
                    this.onUserSelected();
                }
                return false;
            },

            addUser: function (event) {
                if (event) {
                    event.preventDefault();
                }
                var u = tenants.const.users.dlg;
                core.getHtml(u.base + u._add + this.path, _.bind(function (content) {
                    core.showFormDialog(tenants.UserDialog, content, undefined, _.bind(function () {
                        if (_.isFunction(this.onUserAction)) {
                            this.onUserAction();
                        }
                    }, this));
                }, this));
                return false;
            },

            changeUser: function (event) {
                if (event) {
                    event.preventDefault();
                }
                if (this.$selected.length === 1) {
                    var u = tenants.const.users.dlg;
                    var path = this.$selected.data('path');
                    var user = this.$selected.data('user');
                    core.getHtml(u.base + u._change + this.path + '?user.id=' + user, _.bind(function (content) {
                            core.showFormDialog(tenants.UserDialog, content, undefined, _.bind(function () {
                                if (_.isFunction(this.onUserAction)) {
                                    this.onUserAction();
                                }
                            }, this));
                        }, this)
                    );
                }
                return false;
            },

            removeUser: function (event) {
                if (event) {
                    event.preventDefault();
                }
                if (this.$selected.length === 1) {
                    var u = tenants.const.users.dlg;
                    var path = this.$selected.data('path');
                    var user = this.$selected.data('user');
                    core.getHtml(u.base + u._remove + this.path + '?user.id=' + user, _.bind(function (content) {
                        core.showFormDialog(tenants.UserDialog, content, undefined, _.bind(function () {
                            if (_.isFunction(this.onUserAction)) {
                                this.onUserAction();
                            }
                        }, this));
                    }, this));
                }
                return false;
            }
        });

        tenants.UsersTable = tenants.UsersView.extend({

            initialize: function (options) {
                tenants.UsersView.prototype.initialize.apply(this, [options]);
            },

            onUserSelected: function () {
                this.$selected.find('.sel input').prop('checked', true);
            },

            onUserAction: function () {
                $(document).trigger('detail:reload', [this.path]);
            }
        });

        tenants.UsersToolbar = Backbone.View.extend({

            initialize: function (options) {
                this.$add = this.$('.add');
                this.$change = this.$('.change');
                this.$remove = this.$('.remove');
                this.initHandlers();
            },

            initHandlers: function () {
                this.$add.off('click').click(_.bind(tenants.usersView.addUser, tenants.usersView));
                this.$change.off('click').click(_.bind(tenants.usersView.changeUser, tenants.usersView));
                this.$remove.off('click').click(_.bind(tenants.usersView.removeUser, tenants.usersView));
            }
        });

        tenants.onUsersTableLoad = function () {
            var c = tenants.const.users.css;
            tenants.usersView = core.getView('.' + c.base + c._table, tenants.UsersTable);
            tenants.usersToolbar = core.getView('.' + c.base + c._toolbar, tenants.UsersToolbar);
        };

        tenants.onUsersTableLoad();

    })(window.tenants, window.core);

})(window);
