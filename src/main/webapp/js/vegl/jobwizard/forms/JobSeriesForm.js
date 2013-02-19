/**
 * Job wizard form for selecting/creating a job series
 *
 * Author - Josh Vote
 */
Ext.define('vegl.jobwizard.forms.JobSeriesForm', {
    extend : 'vegl.jobwizard.forms.BaseJobWizardForm',

    /**
     * Creates a new JobSeriesForm form configured to write/read to the specified global state
     */
    constructor: function(wizardState) {
        var jobSeriesObj = this;
        var mySeriesStore = Ext.create('Ext.data.Store', {
            model : 'vegl.models.Series',
            proxy : {
                type : 'ajax',
                url : 'secure/mySeries.do',
                reader : {
                    type : 'json',
                    root : 'data'
                },
				listeners : {
                	exception : function(proxy, response, operation) {
                		responseObj = Ext.JSON.decode(response.responseText);
                		errorMsg = responseObj.msg;
                    	errorInfo = responseObj.debugInfo;
                		portal.widgets.window.ErrorWindow.showText('Error', errorMsg, errorInfo);
                	}
                }
            },
            autoLoad : true,
            listeners: { 'loadexception': Ext.bind(jobSeriesObj.fireEvent, jobSeriesObj, ['jobWizardLoadException']) }
        });

        this.callParent([{
            wizardState : wizardState,
            bodyStyle: 'padding:10px;',
            frame: true,
            layout : {
                type : 'vbox',
                align : 'stretch'
            },
            monitorValid: true,
            items: [{
                xtype: 'label',
                text: 'A grid job is always part of a job series even if it is a single job. Please specify if you want to create a new series for this job or add it to an existing one:'
            }, {
                xtype: 'radiogroup',
                itemId : 'seriesRadioGroup',
                style: 'padding:10px;',
                hideLabel: true,
                items: [{
                    name: 'sCreateSelect',
                    boxLabel: 'Select existing series',
                    inputValue: 0,
                    checked: true,
                    handler: Ext.bind(jobSeriesObj.onSwitchCreateSelect, jobSeriesObj)
                }, {
                    name: 'sCreateSelect',
                    boxLabel: 'Create new series',
                    inputValue: 1
                }]
            }, {
                xtype: 'fieldset',
                itemId: 'seriesProperties',
                title: 'Series properties',
                collapsible: false,
                layout : {
                    type : 'vbox',
                    align : 'stretch'
                },
                flex : 1,
                items: [{
                    xtype: 'combo',
                    itemId: 'seriesCombo',
                    name: 'seriesName',
                    editable: false,
                    mode: 'local',
                    minLength: 3,
                    allowBlank: false,
                    maskRe: /[^\W]/,
                    store: mySeriesStore,
                    triggerAction: 'all',
                    displayField: 'name',
                    fieldLabel: 'Series Name',
                    listeners : {
                        select : function(combo, records, index) {
                            if (records.length > 0) {
                                var record = records[0];
                                var descArea = jobSeriesObj.getSeriesDesc();
                                descArea.setRawValue(record.get('description'));

                                jobSeriesObj.wizardState.seriesId = record.get('id');
                                combo.ownerCt.ownerCt.getComponent('jobspanel-seriesjobs').listJobsForSeries(record);
                            }
                        }
                    }
                },{
                    xtype: 'textarea',
                    itemId: 'seriesDesc',
                    name: 'seriesDesc',
                    anchor: '100%',
                    height : 200,
                    disabled: true,
                    fieldLabel: 'Description',
                    blankText: 'Please provide a meaningful description...',
                    allowBlank: false
                }]
            },{
                xtype : 'jobspanel',
                itemId : 'jobspanel-seriesjobs',
                title : 'Other Jobs in selected series',
                hideRegisterButton : true,
                flex : 1,
                jobSeriesFrm : jobSeriesObj,
                listeners : {
                    error : function(component, message) {
                        Ext.Msg.show({
                            title: 'Error',
                            msg: message,
                            buttons: Ext.Msg.OK,
                            icon: Ext.Msg.ERROR
                        });
                    }
                },
                viewConfig : {
                    deferEmptyText : false,
                    emptyText : '<p class="centeredlabel">The selected series doesn\'t have any jobs.</p>'
                }
            }]
        }]);
    },

    getSeriesCombo : function() {
        return this.getComponent('seriesProperties').getComponent('seriesCombo');
    },

    getSeriesDesc : function() {
        return this.getComponent('seriesProperties').getComponent('seriesDesc');
    },

    onSwitchCreateSelect : function(checkbox, checked) {
        var combo = this.getSeriesCombo();
        var descText = this.getSeriesDesc();
        if (checked) {
            combo.reset();
            combo.setEditable(false);
            combo.getStore().load();
            descText.setDisabled(true);
            descText.reset();
        } else {
            combo.reset();
            combo.setEditable(true);
            combo.getStore().removeAll();
            descText.setDisabled(false);
            descText.reset();
        }
    },

    getTitle : function() {
        return "Select Job Series...";
    },

    beginValidation : function(callback) {
        var radioGroup = this.getComponent('seriesRadioGroup');
        var wizardState = this.wizardState;
        var numDownloadReqs = this.getNumDownloadRequests();
        
        if (radioGroup.getValue().sCreateSelect === 0) {
            if (Ext.isEmpty(wizardState.seriesId)) {
                Ext.Msg.alert('No series selected', 'Please select a series to add the new job to.');
                callback(false);
                return;
            }
            
            if (!wizardState.skipConfirmPopup && numDownloadReqs === 0) {
                Ext.Msg.confirm('Confirm',
                        'No data set has been captured. Do you want to continue?', 
                        function(button) {
                            if (button === 'yes') {
                                wizardState.skipConfirmPopup = true;
                                callback(true);
                                return;
                            } else {
                                callback(false);
                                return;
                            }
                    });
            } else {
                callback(true);
                return;
            }
        } else {
            var seriesName = this.getSeriesCombo().getRawValue();
            var seriesDesc = this.getSeriesDesc().getRawValue();
            if (Ext.isEmpty(seriesName) || Ext.isEmpty(seriesDesc)) {
                Ext.Msg.alert('Create new series', 'Please specify a name and description for the new series.');
                callback(false);
                return;
            }
            
            var csFunc = this.createSeries(wizardState, seriesName, seriesDesc, callback);
            
            if (numDownloadReqs === 0) {
                Ext.Msg.confirm('Confirm',
                        'No data set has been captured. Do you want to continue?', 
                        function(button) {
                            if (button === 'yes') {
                                //Request our new series is created
                                csFunc();
                            } else {
                                callback(false);
                                return;
                            }
                    });
            } else {
                //Request our new series is created
                csFunc();
            }
        }
    },
    
    getNumDownloadRequests : function() {
        request = ((window.XMLHttpRequest) ? new XMLHttpRequest() : new ActiveXObject("Microsoft.XMLHTTP"));
        request.open("GET", "getNumDownloadRequests.do", false); //<-- false makes it a synchonous request!
        request.send(null);
        respObj = Ext.JSON.decode(request.responseText);
        size = respObj.data;
        return size;
    },
    
    createSeries : function(wizardState, seriesName, seriesDesc, callback) {
        return function() {
            
            Ext.Ajax.request({
                url: 'secure/createSeries.do',
                params: {
                    'seriesName': seriesName,
                    'seriesDescription': seriesDesc
                },
                callback : function(options, success, response) {
                    if (success) {
                        var responseObj = Ext.JSON.decode(response.responseText);
                        if (responseObj.success && Ext.isNumber(responseObj.data[0].id)) {
                            wizardState.seriesId = responseObj.data[0].id;
                            callback(true);
                            return;
                        } else {
                            errorMsg = responseObj.msg;
                            errorInfo = responseObj.debugInfo;
                        }
                    } else {
                        errorMsg = "There was an internal error saving your series.";
                        errorInfo = "Please try again in a few minutes or report this error to cg_admin@csiro.au.";                        
                    }
                    
                    portal.widgets.window.ErrorWindow.showText('Create new series', errorMsg, errorInfo);
                    callback(false);
                    return;
                }
            });
        }
    }
});