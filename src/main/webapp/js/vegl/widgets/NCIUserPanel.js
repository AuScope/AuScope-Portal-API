/**
 * A Ext.grid.Panel specialisation for rendering the NCI details of an ANVGLUser object
 *
 *
 * Adds the following events
 *
 */
Ext.define('vegl.widgets.NCIUserPanel', {
    extend : 'Ext.panel.Panel',
    alias : 'widget.nciuserpanel',

    /**
     * Accepts the config for a Ext.grid.Panel along with the following additions:
     *
     * user: Instance of ANVGLUser to render NCI details
     */
    constructor : function(config) {
        tm = new Ext.util.TextMetrics(),
        n = tm.getWidth("NCI Username:");
      
        Ext.apply(config, {
            layout: 'fit',
            items: [{
                xtype: 'form',
                border: false,
                padding: '10 20 20 5',
                layout: 'anchor',
                items: [{
                    xtype: 'textfield',
                    itemId: 'nciUsername',
                    name: 'nciUsername',
                    fieldLabel: 'NCI Username',
                    labelWidth: n,
                    anchor: '100%',
                    allowBlank: false,
                    allowOnlyWhitespace: false,
                    plugins: [{
                        ptype: 'fieldhelptext',
                        text: 'The username for the NCI account.'
                    }]
                },{
                	xtype: 'panel',
                	border: false,
                	
                	//width: '100%',
                	
                	layout: {
                		type: 'hbox',
                		
                		//pack: 'justify'
                			
                	},
                	items: [{
                		xtype: 'textfield',
                        itemId: 'nciKey',
                        name: 'nciKey',
                        fieldLabel: 'NCI Key',
                        labelWidth: n,
                        anchor: '100%',
                        
                        layout: 'fit',
                        flex: 1,
                        
                        //allowBlank: false,
                        //allowOnlyWhitespace: false,
                        readOnly: true,
                        plugins: [{
                            ptype: 'fieldhelptext',
                            text: 'The NCI key (SSH) for the NCI account.'
                        }]
                	},{
                		xtype: 'button',
                		margin: '0 0 0 5',
                        cls: 'important-button',
                        scale: 'small',
                        text: 'Upload'
                		
                	}]
                }
                /*,{
                    xtype: 'textfield',
                    itemId: 'arnStorage',
                    name: 'arnStorage',
                    fieldLabel: 'Job Instance ARN',
                    labelWidth: n,
                    anchor: '100%',
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
                    labelWidth: n,
                    anchor: '100%',
                    allowBlank: true,
                    plugins: [{
                        ptype: 'fieldhelptext',
                        text: 'The (optional) name of the key to be applied to every VM started by ANVGL'
                    }]
                }*/]
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
                        var formPanel = btn.up('nciuserpanel').down('form');
                        var statusLabel = btn.up('nciuserpanel').down('#status');
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
                afterrender: function(nciUserPanel) {
                    if (config.user) {
                        nciUserPanel.setUser(config.user);
                    }
                }
            }
        });

        this.callParent(arguments);
    },

    setUser: function(user) {
    	this.down('#nciUsername').setValue(user.get('nciUsername'));
    	//this.down('#nciKey').setValue(user.get('nciKey'));
    	/*
        this.down('#awsKeyName').setValue(user.get('awsKeyName'));
        this.down('#arnExecution').setValue(user.get('arnExecution'));
        this.down('#arnStorage').setValue(user.get('arnStorage'));
        */
    }
});