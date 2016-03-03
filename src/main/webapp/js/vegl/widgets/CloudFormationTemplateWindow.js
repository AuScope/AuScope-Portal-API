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
            closable: true,
            modal: true,
            width: 600,
            height: 400,
            title: 'Cloud Formation Template',
            items: [{
                xtype: 'panel',
                border: false,
                padding: 5,
                scrollable: true,
                html: '<iframe style="width:100%;height:100%;border:0px;"></iframe>',
                bodyStyle: {
                    padding: '0px',
                    border: '0px'
                },
                listeners: {
                    afterrender: function(panel) {
                        var iframe = panel.getEl().down('iframe');
                        var doc = iframe.dom.contentWindow.document;
                        doc.open();
                        doc.write(config.content);

                        doc.close();

                        doc.body.setAttribute('style', 'white-space:pre;font-family:monospace;');
                    }
                }
            }]
        });

        this.callParent(arguments);
    }
});
