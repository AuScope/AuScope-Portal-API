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
                    margin: '20 0 0 0',
                    anchor: '100%',
                    plugins: [{
                        ptype: 'fieldhelptext',
                        text: 'The email used that will be used to contact you (Modify this at accounts.google.com)'
                    }],
                    fieldStyle: {
                        color: '#888888',
                        'background-color': '#eeeeee'
                    }
                },{
                    xtype: 'textfield',
                    itemId: 'awsAccount',
                    name: 'awsAccount',
                    fieldLabel: 'AWS Account',
                    anchor: '100%',
                    allowBlank: false,
                    allowOnlyWhitespace: false,
                    plugins: [{
                        ptype: 'fieldhelptext',
                        text: 'The Amazon IAM account name which will be configured to run ANVGL virtual machines.'
                    }]
                },{
                    xtype: 'textfield',
                    itemId: 'arnExecution',
                    name: 'arnExecution',
                    fieldLabel: 'Compute ARN',
                    anchor: '100%',
                    allowBlank: false,
                    allowOnlyWhitespace: false,
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
                    allowOnlyWhitespace: false,
                    plugins: [{
                        ptype: 'fieldhelptext',
                        text: 'The Amazon resource name describing the storage S3 resource to be used for job artifacts'
                    }]
                },{
                    xtype: 'textfield',
                    itemId: 'awsKeyName',
                    name: 'awsKeyName',
                    fieldLabel: 'AWS Key Name',
                    anchor: '100%',
                    allowBlank: true,
                    plugins: [{
                        ptype: 'fieldhelptext',
                        text: 'The (optional) name of the key to be applied to every VM started by ANVGL'
                    }]
                }]
            }],
            dockedItems: [{
                xtype: 'toolbar',
                dock: 'bottom',
                items: [{
                    xtype: 'tbfill'
                },{
                    xtype: 'label',
                    itemId: 'status',
                    style: {
                        'color': 'gray'
                    }
                },{
                    xtype: 'button',
                    scale: 'large',
                    text: 'Download AWS Policy',
                    handler: function() {
                        Ext.Ajax.request({
                            url: 'secure/getCloudFormationScript.do',
                            callback: function(options, success, response) {
                                if (!success) {
                                    Ext.MessageBox.alert('Error', 'There was an error accessing template. Please try refreshing the page');
                                    return;
                                }

                                var responseObj = Ext.JSON.decode(response.responseText);
                                if (!responseObj.success) {
                                    Ext.MessageBox.alert('Error', 'There was an error generating template. Please try refreshing the page.');
                                    return;
                                }

                                Ext.create('vegl.widgets.CloudFormationTemplateWindow', {content: responseObj.data}).show();
                            }
                        });
                    }
                },{
                    xtype: 'button',
                    cls: 'important-button',
                    scale: 'large',
                    text: 'Save Changes',
                    handler: function(btn) {
                        var formPanel = btn.up('userpanel').down('form');
                        var statusLabel = btn.up('userpanel').down('#status');
                        if (!formPanel.isValid()) {
                            return;
                        }

                        statusLabel.setText('Saving your changes...');
                        Ext.Ajax.request({
                            url: 'secure/setUser.do',
                            params: formPanel.getValues(),
                            callback: function(options, success, response) {
                                if (!success) {
                                    statusLabel.setText('');
                                    Ext.MessageBox.alert('Error', 'There was an error saving your changes. Please try refreshing the page.');
                                    return;
                                }

                                var responseObj = Ext.JSON.decode(response.responseText);
                                if (!responseObj.success) {
                                    statusLabel.setText('');
                                    Ext.MessageBox.alert('Error', 'There was an error saving your changes. Please try refreshing the page.');
                                    return;
                                }

                                statusLabel.setText('Saved!');
                            }
                        });
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