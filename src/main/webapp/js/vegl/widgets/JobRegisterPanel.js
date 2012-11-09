/**
 * A window for registering a new processed job metadata and
 * its output file(s) to GeoNetwork.
 */
Ext.define('vegl.widgets.JobRegisterPanel', {
    extend : 'Ext.form.Panel',
    alias : 'widget.jobregister',

    jobId : null,

    constructor : function(config) {
        this.jobId = config.jobId;

        Ext.apply(config, {
            id : 'jobRegisterFrm',
            frame : true,
            title : 'Define Job Registration Details',
            bodyPadding : '5 5 0',
            fieldDefaults : {
                msgTarget : 'side',
                labelWidth : 120
            },
            defaults : {
                anchor : '100%'
            },
            items : [{
                xtype : 'fieldset',
                title : 'Contact Details',
                checkboxToggle : true,
                defaultType : 'textfield',
                layout : 'anchor',
                defaults : {
                    anchor : '100%'
                },
                items : [{
                        fieldLabel : 'Individual name',
                        name : 'individualName'
                    },{
                        fieldLabel : 'Organisation name',
                        name : 'organisationName'
                    },{
                        fieldLabel : 'Position name',
                        name : 'positionName'
                    },{
                        fieldLabel : 'Telephone',
                        name : 'telephone'
                    },{
                        fieldLabel : 'Facsimile',
                        name : 'facsimile'
                    },{
                        fieldLabel : 'Delivery point',
                        name : 'deliveryPoint'
                    },{
                        fieldLabel : 'City',
                        name : 'city'
                    },{
                        fieldLabel : 'Administrative area',
                        name : 'administrativeArea'
                    },{
                        fieldLabel : 'Postal code',
                        name : 'postalCode'
                    },{
                        fieldLabel : 'Country',
                        name : 'country'
                    }
                ]
            },{
                xtype : 'fieldset',
                title : 'Online Contact',
                checkboxToggle : true,
                defaultType : 'textfield',
                collapsed : true,
                layout : 'anchor',
                defaults : {
                    anchor : '100%'
                },
                items : [{
                        fieldLabel : 'Name',
                        name : 'onlineContactName'
                    },{
                        fieldLabel : 'Description',
                        name : 'onlineContactDescription'
                    },{
                        fieldLabel : 'URL',
                        name : 'onlineContactURL',
                        vtype : 'url',
                        plugins: [{
                            ptype: 'fieldhelptext',
                            text: 'Enter a valid URL e.g. http://www.example.com'
                        }]
                    }
                ]
            },{
                xtype : 'fieldset',
                title : 'Other Details',
                checkboxToggle : true,
                defaultType : 'textfield',
                collapsed : true,
                layout : 'anchor',
                defaults : {
                    anchor : '100%'
                },
                items : [{
                        fieldLabel : 'Descriptive keywords',
                        name : 'keywords',
                        plugins: [{
                            ptype: 'fieldhelptext',
                            text: 'Use comma to separate keywords.'
                        }]
                    },{
                        fieldLabel : 'Constraints',
                        xtype : 'textareafield',
                        name : 'constraints'
                    }
                ]
            }],
            buttons : [{
                text : 'Register',
                scope : this,
                handler : function(btn) {
                    formPanel = Ext.getCmp('jobRegisterFrm');
                    this.insertRecord(formPanel);
                }
            },{
                text : 'Cancel',
                handler: function(btn) {
                    popupWin = Ext.getCmp('jobRegisterWin');
                    popupWin.close();
                }
            }]
        });

        this.callParent(arguments);
    },

    /**
     * Loads user's contact/signature details into the form
     * Closes the window that loaded this form on failure
     */
    loadFormData : function() {
        this.getForm().load({
            url : 'getUserSignature.do',
            waitMsg : 'Loading contact details...',
            failure : function(frm, action) {
                responseObj = Ext.JSON.decode(action.response.responseText);
                errorMsg = responseObj.msg;
                errorInfo = responseObj.debugInfo;
                portal.widgets.window.ErrorWindow.showText('Failure', errorMsg, errorInfo);
                popupWin = Ext.getCmp('jobRegisterWin');
                popupWin.close();
            },
            success : function(frm, action) {
                responseObj = Ext.JSON.decode(action.response.responseText);
                if (responseObj.success) {
                    frm.setValues(responseObj.data[0]);
                }
            }
        });
    },

    /**
     * Inserts the user defined job metadata record into GeoNetwork
     * Closes the window that loaded this form on success
     */
    insertRecord : function(formPanel) {
        var form = formPanel.getForm();
        if (!form.isValid()) {
            return;
        }

        form.submit({
            url: 'insertRecord.do',
            params: {
                jobId : this.jobId
            },
            success: function(form, action) {
                if (!action.result.success) {
                    Ext.Msg.alert('Failure', 'Job registration failed. Please try again in a few minutes or report this error to cg_admin@csiro.au.');
                    return;
                }
                popupWin = Ext.getCmp('jobRegisterWin');
                popupWin.close();
            },
            failure: function(form, action) {
                responseObj = Ext.JSON.decode(action.response.responseText);
                errorMsg = responseObj.msg;
                errorInfo = responseObj.debugInfo;
                portal.widgets.window.ErrorWindow.showText('Failure', errorMsg, errorInfo);
            }
        });
    }
});