/**
 * A Ext.grid.Panel specialisation for rendering the NCI details of an ANVGLUser object
 *
 *
 * Adds the following events
 *
 */
Ext.define('vegl.widgets.NCIDetailsPanel', {
    extend : 'Ext.panel.Panel',
    alias : 'widget.ncidetailspanel',

    /**
     * Accepts the config for a Ext.grid.Panel along with the following additions:
     *
     * user: Instance of ANVGLUser to render NCI details
     */
    constructor : function(config) {
        tm = new Ext.util.TextMetrics(),
        n = tm.getWidth("NCI Project Code:");

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
                    allowBlank: true,
                    allowOnlyWhitespace: false,
                    plugins: [{
                        ptype: 'fieldhelptext',
                        text: 'The username for the NCI account.'
                    }]
                },{
                    xtype: 'textfield',
                    itemId: 'nciProjectCode',
                    name: 'nciProjectCode',
                    fieldLabel: 'NCI Project Code',
                    labelWidth: n,
                    anchor: '100%',
                    allowBlank: true,
                    allowOnlyWhitespace: false,
                    plugins: [{
                        ptype: 'fieldhelptext',
                        text: 'The default NCI project code.'
                    }]
                },{
                	xtype : 'fileuploadfield',
                	itemId: 'nciKey',
                    name: 'nciKey',
                    fieldLabel: 'NCI Key',
                    labelWidth: n,
                    anchor: '100%',
                    allowBlank: true,
                    emptyText : 'Select SSH File',
                    plugins: [{
                        ptype: 'fieldhelptext',
                        text: 'The NCI key (SSH) for the NCI account.'
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
                        var formPanel = btn.up('ncidetailspanel').down('form');
                        var statusLabel = btn.up('ncidetailspanel').down('#status');
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
                afterrender: function(nciDetailsPanel) {
                    if (config.user) {
                        nciDetailsPanel.setUser(config.user);
                    }
                }
            }
        });

        this.callParent(arguments);
    },

    setUser: function(user) {
    	//this.down('#nciUsername').setValue(user.get('nciUsername'));
    	//this.down('#nciKey').setValue(user.get('nciKey'));
    	/*
        this.down('#awsKeyName').setValue(user.get('awsKeyName'));
        this.down('#arnExecution').setValue(user.get('arnExecution'));
        this.down('#arnStorage').setValue(user.get('arnStorage'));
        */
    },
    
    setDetails: function(nciDetails) {
    	this.down('#username').setValue(user.get('username'));
    	this.down('#project').setValue(user.get('project'));
    	this.down('#key').setValue(user.get('key'));
    }
});