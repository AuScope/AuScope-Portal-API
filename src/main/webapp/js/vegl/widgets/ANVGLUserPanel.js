/**
 * A Ext.grid.Panel specialisation for rendering a single ANVGLUser object
 *
 *
 * Adds the following events
 *
 */
Ext.define('vegl.widgets.ANVGLUserPanel', {
    extend : 'Ext.panel.Panel',
    alias : 'widget.userpanel',

    /**
     * Accepts the config for a Ext.grid.Panel along with the following additions:
     *
     * user: Instance of ANVGLUser to render
     */
    constructor : function(config) {
        Ext.apply(config, {
            layout: 'fit',
            items: [{
                xtype: 'form',
                border: false,
                padding: '10 20 20 5',
                layout: 'anchor',
                items: [{
                    xtype: 'label',
                    itemId: 'fullName',
                    text: 'Loading...',
                    style: {
                        'font-size': '24px',
                    }
                },{
                    xtype: 'textfield',
                    itemId: 'email',
                    readOnly: true,
                    fieldLabel: 'Email',
                    margin: '10 0 0 0',
                    anchor: '100%',
                    plugins: [{
                        ptype: 'fieldhelptext',
                        text: 'The email used that will be used to contact you (Modify this at accounts.google.com)'
                    }]
                },{
                    xtype: 'textfield',
                    itemId: 'arnExecution',
                    name: 'arnExecution',
                    fieldLabel: 'Compute ARN',
                    anchor: '100%',
                    allowBlank: false,
                    plugins: [{
                        ptype: 'fieldhelptext',
                        text: 'The Amazon resource name describing the compute EC2 resource to be used for job execution'
                    }]
                },{
                    xtype: 'textfield',
                    itemId: 'arnStorage',
                    name: 'arnStorage',
                    fieldLabel: 'Storage ARN',
                    anchor: '100%',
                    allowBlank: false,
                    plugins: [{
                        ptype: 'fieldhelptext',
                        text: 'The Amazon resource name describing the storage S3 resource to be used for job artifacts'
                    }]
                }]
            }],
            dockedItems: [{
                xtype: 'toolbar',
                dock: 'bottom',
                items: [{
                    xtype: 'tbfill'
                },{
                    xtype: 'button',
                    scale: 'large',
                    text: 'Download AWS Policy',
                    handler: function() {
                        alert('TODO');
                    }
                },{
                    xtype: 'button',
                    cls: 'important-button',
                    scale: 'large',
                    text: 'Save Changes',
                    handler: function() {
                        alert('TODO');
                    }
                }]
            }],
            listeners: {
                afterrender: function(userPanel) {
                    if (config.user) {
                        userPanel.setUser(config.user);
                    }
                }
            }
        });

        this.callParent(arguments);
    },

    setUser: function(user) {
        this.down('#email').setValue(user.get('email'));
        this.down('#arnExecution').setValue(user.get('arnExecution'));
        this.down('#arnStorage').setValue(user.get('arnStorage'));
        this.down('#fullName').setText('Hello ' + user.get('fullName'));

    }
});