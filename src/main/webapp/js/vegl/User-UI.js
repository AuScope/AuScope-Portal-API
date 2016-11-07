Ext.application({
    name : 'portal',

    //Here we build our GUI from existing components - this function should only be assembling the GUI
    //Any processing logic should be managed in dedicated classes - don't let this become a
    //monolithic 'do everything' function
    launch : function() {
        var reportFailure = function() {
            return;
        };

        var getTCs = function(callback) {
            Ext.Ajax.request({
                url: 'getTermsConditions.do',
                callback: function(options, success, response) {
                    if (!success) {
                        callback(false);
                        return;
                    }

                    var responseObj = Ext.JSON.decode(response.responseText);
                    if (!responseObj.success) {
                        callback(false);
                    }

                    callback(true, responseObj.data.html, responseObj.data.currentVersion);
                }
            });
        }

        Ext.create('Ext.container.Viewport', {
            layout: 'border',
            style: {
                'background-color': 'white'
            },
            items: [{	
                xtype: 'box',
                region: 'north',
                applyTo: 'body',
                height: 100
            },{
                region: 'center',
                margin: '10 0 10 0',
                border: false,
                bodyStyle: {
                    'background-color': 'white'
                },
                items: [{
                	xtype: 'tabpanel',
                	layout: 'fit',
                	items: [{
                		title: 'AWS',
                		xtype: 'panel',
	                    border: false,
	                    maxWidth: 1200,
	                    width: '100%',
	                    height: 500,
	                    margin: '10 10 10 10',
	                    layout: {
	                        type: 'hbox',
	                        pack: 'center',
	                    },
	                    items: [{
	                        xtype: 'panel',
	                        title: 'Step 1: Initialising your AWS',
	                        flex: 0.4,
	                        height: '100%',
	                        margin: '0 10 0 0',
	                        bodyStyle: {
	                            'background-color': 'white',
	                            padding: '5px',
	                        },
	                        html: '<h1>Tutorial Video</h1><img src="http://placehold.it/450x250"><p>You can also follow instructions at our wiki</p>',
	                        dockedItems: [{
	                            xtype: 'toolbar',
	                            dock: 'bottom',
	                            items: [{
	                                xtype: 'tbfill'
	                            },{
	                                xtype: 'button',
	                                scale: 'large',
	                                cls: 'important-button',
	                                text: 'Download Cloud Formation Policy',
	                                handler: function() {
	                                    portal.util.FileDownloader.downloadFile('secure/getCloudFormationScript.do');
	                                }
	                            }]
	                        }]
	                    },{
	                        xtype: 'panel',
	                        title: 'Step 2: Configuring ANVGL',
	                        flex: 0.6,
	                        height: '100%',
	                        margin: '0 0 0 10',
	                        bodyStyle: {
	                            'background-color': 'white'
	                        },
	                        layout: 'fit',
	                        items: [{
	                            border: false,
	                            xtype: 'userpanel'
	                        }]
	                    }]
                	},{
                		title: 'NCI',
                		xtype: 'panel',
	                    border: false,
	                    maxWidth: 800,
	                    width: '100%',
	                    height: 500,
	                    margin: '10 10 10 10',
	                    items: [{
	                    	xtype: 'panel',
	                        title: 'Enter Your NCI Credentials',
	                        flex: 0.6,
	                        height: '100%',
	                        margin: '0 0 0 10',
	                        bodyStyle: {
	                            'background-color': 'white'
	                        },
	                        layout: 'fit',
	                        items: [{
	                            border: false,
	                            xtype: 'ncidetailspanel'
	                        }]
	                    }]
                	}]
                }]
            }],
            listeners: {
                afterrender: function(vp) {
                    var mask = new Ext.LoadMask({msg: 'Loading User', target: vp});
                    mask.show();
                    getTCs(function(tcSuccess, tcHtml, tcVersion) {
                        if (!tcSuccess) {
                            Ext.MessageBox.alert('Error','Failed to contact ANVGL server. Please try refreshing the page.');
                            mask.hide();
                            return;
                        }

                        Ext.Ajax.request({
                            url: 'secure/getUser.do',
                            callback: function(options, success, response) {
                                mask.hide();
                                if (!success) {
                                    Ext.MessageBox.alert('Error','Failed to contact ANVGL server. Please try refreshing the page.');
                                    return;
                                }

                                var responseObj = Ext.JSON.decode(response.responseText);
                                if (!responseObj.success) {
                                    Ext.MessageBox.alert('Error','Failed to retrieve user details. Please try refreshing the page.');
                                    return;
                                }

                                var userPanel = vp.down('userpanel');
                                var user = Ext.create('vegl.models.ANVGLUser', responseObj.data);
                                userPanel.setUser(user);

                                if (!user.get('acceptedTermsConditions') || user.get('acceptedTermsConditions') < tcVersion) {
                                    Ext.create('vegl.widgets.TermsConditionsWindow', {
                                        tccontent: tcHtml,
                                        listeners: {
                                            accept: function() {
                                                Ext.Ajax.request({
                                                    url: 'secure/setUser.do',
                                                    params: {
                                                        acceptedTermsConditions: tcVersion
                                                    }
                                                });
                                            },
                                            reject: function() {
                                                window.location.href = 'j_spring_security_logout';
                                            }
                                        }
                                    }).show();
                                }
                                
                                mask = new Ext.LoadMask({msg: 'Loading NCI Details', target: vp});
                                mask.show();
                                Ext.Ajax.request({
                                    url: 'secure/getNCIDetails.do',
                                    callback: function(options, success, response) {
                                    	var detailsPanel = vp.down('ncidetailspanel');
                                    	responseObj = Ext.JSON.decode(response.responseText);
                                        if (!responseObj.success) {
                                        	mask.hide();
                                            return;
                                        }                                    	
                                        var details = Ext.create('vegl.models.NCIDetails', responseObj.data);
                                        detailsPanel.setDetails(details);
                                    	mask.hide();
                                    }
                                });                                
                            }
                        });
                    });
                }
            }
            
        });
    }
});