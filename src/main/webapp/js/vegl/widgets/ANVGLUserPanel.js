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
        tm = new Ext.util.TextMetrics(),
        n = tm.getWidth("Job Instance ARN:");

        Ext.apply(config, {
            layout: 'fit',
            padding: '10 20 20 5',
            items: [{
                xtype: 'form',
                border: false,
                defaults: {
                    labelWidth: n,
                    width: '100%',
                    margin: '15 0 0 0'
                },
                items: [{
                    xtype: 'textfield',
                    itemId: 'arnExecution',
                    name: 'arnExecution',
                    fieldLabel: 'VGL Access ARN',
                    allowBlank: false,
                    allowOnlyWhitespace: false,
                    plugins: [{
                        ptype: 'fieldhelptext',
                        text: 'Amazon resource name for the role which is used to access the client AWS account.'
                    }]
                },{
                    xtype: 'textfield',
                    itemId: 'arnStorage',
                    name: 'arnStorage',
                    fieldLabel: 'Job Instance ARN',
                    allowBlank: false,
                    allowOnlyWhitespace: false,
                    plugins: [{
                        ptype: 'fieldhelptext',
                        text: 'The Amazon resource name for the instance profile which is used when processing user jobs.'
                    }]
                },{
                    xtype: 'textfield',
                    itemId: 'awsKeyName',
                    name: 'awsKeyName',
                    fieldLabel: 'AWS Key Name',
                    allowBlank: true,
                    plugins: [{
                        ptype: 'fieldhelptext',
                        text: 'The (optional) name of the key to be applied to every VM started by VGL'
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

                                var queryParams = Ext.Object.fromQueryString(window.location.search);
                                if (queryParams.next) {
                                    window.location.href = queryParams.next;
                                }
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
        this.down('#awsKeyName').setValue(user.get('awsKeyName'));
        this.down('#arnExecution').setValue(user.get('arnExecution'));
        this.down('#arnStorage').setValue(user.get('arnStorage'));
    }
});