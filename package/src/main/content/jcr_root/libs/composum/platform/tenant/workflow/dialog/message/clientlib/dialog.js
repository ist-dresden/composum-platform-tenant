/**
 * the workflow dialog extension for conversations
 */
(function (window) {
    'use strict';

    window.tenants = window.tenants || {};

    (function (tenants, core) {

        tenants.Conversation = Backbone.View.extend({

            initialize: function (options) {
                this.$conversation = this.$('.conversation-item');
                this.$toggle = this.$('.conversation-toggle');
                this.$toggle.click(_.bind(this.toogleItem, this));
            },

            toogleItem: function (event) {
                event.preventDefault();
                var $toggle = $(event.currentTarget);
                var $prevItems = $toggle.closest('.conversation-item,.conversation-start').prevAll();
                if ($prevItems.length > 0) {
                    if ($prevItems.filter('.visible').length > 0) {
                        $prevItems.removeClass('visible');
                        $prevItems.find('.conversation-toggle').removeClass('active');
                        $toggle.removeClass('active');
                    } else {
                        $($prevItems[0]).addClass('visible');
                        $toggle.addClass('active');
                    }
                }
                return false;
            }
        });

        tenants.initConversationDialog = function () {
            core.getView(this.$('.conversation'), tenants.Conversation);
        };

    })(window.tenants, window.core);

})(window);
