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
                url : 'mySeries.do',
                reader : {
                    type : 'json',
                    root : 'data'
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
        if (radioGroup.getValue().sCreateSelect == 0) {
            if (Ext.isEmpty(wizardState.seriesId)) {
                Ext.Msg.alert('No series selected', 'Please select a series to add the new job to.');
                callback(false);
                return;
            }

            //Goto next step as we have an already existing series
            callback(true);
            return;
        } else {
            var seriesName = this.getSeriesCombo().getRawValue();
            var seriesDesc = this.getSeriesDesc().getRawValue();
            if (Ext.isEmpty(seriesName) ||
                    Ext.isEmpty(seriesDesc)) {
                Ext.Msg.alert('Create new series',
                    'Please specify a name and description for the new series.');
                callback(false);
                return;
            }

            //Request our new series is created
            Ext.Ajax.request({
                url: 'createSeries.do',
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
                        }
                    }

                    Ext.Msg.alert('Create new series','There was an internal error saving your series. Please try again in a few minutes.');
                    callback(false);
                    return;
                }
            });
        }
    }
});