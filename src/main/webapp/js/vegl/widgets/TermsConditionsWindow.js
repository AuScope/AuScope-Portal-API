/**
 * A window for allowing the user to review and accept terms and conditions
 *
 * {
 *  tccontent : String terms and conditions content
 * }
 *
 */
Ext.define('vegl.widgets.TermsConditionsWindow', {
    extend : 'Ext.Window',
    alias: 'widget.tcwindow',

    constructor : function(config) {
        Ext.apply(config, {
            border: true,
            resizable: true,
            layout: 'fit',
            closable: false,
            modal: true,
            width: 600,
            height: 400,
            title: 'Terms and Conditions',
            items: [{
                xtype: 'panel',
                border: false,
                padding: 5,
                scrollable: true,
                html: config.tccontent
            }],
            dockedItems: [{
                xtype: 'toolbar',
                dock: 'bottom',
                items: [{
                    xtype: 'tbfill'
                },{
                    xtype: 'button',
                    scale: 'large',
                    text: 'Reject',
                    handler: function(btn) {
                        alert('TODO');
                        btn.up('tcwindow').close();
                    }
                },{
                    xtype: 'button',
                    cls: 'important-button',
                    scale: 'large',
                    text: 'Accept',
                    handler: function(btn) {
                        alert('TODO');
                        btn.up('tcwindow').close();
                    }
                }]
            }]
        });

        this.callParent(arguments);
    }
});
