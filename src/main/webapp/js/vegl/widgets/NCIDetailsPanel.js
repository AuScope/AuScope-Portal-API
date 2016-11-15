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
                    itemId: 'nciProject',
                    name: 'nciProject',
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
                	xtype : 'filefield',
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
                    }],
                    listeners: {
                    	change: function(f, new_val) {
                    		console.log(new_val);
                    	}
                    }
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
                        
                        formPanel.submit({
                        	url: 'secure/setNCIDetails.do',
                        	params: formPanel.getValues(),
                        	success: function() {
                        		statusLabel.setText('Saved!');
                                var queryParams = Ext.Object.fromQueryString(window.location.search);
                                if (queryParams.next) {
                                    window.location.href = queryParams.next;
                                }
                                return;
                        	},
                        	//failure: function(options, success, response) {
                        	failure: function() {
                        		statusLabel.setText('');
                                Ext.MessageBox.alert('Error', 'There was an error saving your changes. Please try refreshing the page.');
                                return;
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

    setDetails: function(nciDetails) {
    	this.down('#nciUsername').setValue(nciDetails.get('nciUsername'));
    	this.down('#nciProject').setValue(nciDetails.get('nciProject'));
    	this.down('#nciKey').setValue(nciDetails.get('nciKey'));
    }
});