/**
 * A window for allowing the user to copy paste the cloud formation script
 *
 * {
 *  content : String JSON string for displaying to the user
 * }
 *
 *  Adds the following events:
 *  {
 *
 *  }
 *
 */
Ext.define('vegl.widgets.CloudFormationTemplateWindow', {
    extend : 'Ext.Window',
    alias: 'widget.cftwindow',

    constructor : function(config) {
        Ext.apply(config, {
            border: true,
            resizable: true,
            layout: 'fit',
            closable: false,
            modal: true,
            width: 600,
            height: 400,
            title: 'Cloud Formation Template',
            items: [{
                xtype: 'panel',
                border: false,
                padding: 5,
                scrollable: true,
                html: config.content,
                bodyStyle: {
                    'font-family': 'monospace',
                    'white-space': 'pre'
                }
            }]
        });

        this.callParent(arguments);
    }
});
