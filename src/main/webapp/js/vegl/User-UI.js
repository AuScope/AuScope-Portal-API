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
                layout: 'center',
                bodyStyle: {
                    'background-color': 'white'
                },
                items: [{
                    border: false,
                    maxWidth: 1000,
                    width: '100%',
                    height: '100%',
                    bodyStyle: {
                        'background-color': 'white'
                    },
                    layout: 'fit',
                    items: [{
                        xtype: 'userpanel'
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
                                        tccontent: tcHtml
                                    }).show();
                                }
                            }
                        });
                    });
                }
            }
        });
    }
});