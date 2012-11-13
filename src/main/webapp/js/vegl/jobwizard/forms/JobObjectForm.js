/**
 * Job wizard form for creating and editing a new Job Object
 *
 * Author - Josh Vote
 */

Ext.define('vegl.jobwizard.forms.JobObjectForm', {
    extend : 'vegl.jobwizard.forms.BaseJobWizardForm',

    imageStore : null,
    storageServicesStore : null,
    computeServicesStore : null,

    /**
     * Creates a new JobObjectForm form configured to write/read to the specified global state
     */
    constructor: function(wizardState) {
        var jobObjectFrm = this;

        this.imageStore = Ext.create('Ext.data.Store', {
            model: 'vegl.models.MachineImage',
            proxy: {
                type: 'ajax',
                url: 'getVmImagesForComputeService.do',
                reader: {
                   type: 'json',
                   root : 'data'
                }
            }
        });

        this.storageServicesStore = Ext.create('Ext.data.Store', {
            fields : [{name: 'id', type: 'string'},
                      {name: 'name', type: 'string'}],
            proxy: {
                type: 'ajax',
                url: 'getStorageServices.do',
                reader: {
                   type: 'json',
                   root : 'data'
                }
            },
            autoLoad : true
        });
        this.storageServicesStore.load();

        this.computeServicesStore = Ext.create('Ext.data.Store', {
            fields : [{name: 'id', type: 'string'},
                      {name: 'name', type: 'string'}],
            proxy: {
                type: 'ajax',
                url: 'getComputeServices.do',
                reader: {
                   type: 'json',
                   root : 'data'
                }
            },
            autoLoad : true
        });
        this.storageServicesStore.load();

        this.callParent([{
            wizardState : wizardState,
            bodyStyle: 'padding:10px;',
            frame: true,
            defaults: { anchor: "100%" },
            labelWidth: 150,
            autoScroll: true,
            listeners : {
                //The first time this form is active create a new job object
                jobWizardActive : function() {
                    //If we have a jobId, load that, OTHERWISE the job will be created later
                    if (jobObjectFrm.wizardState.jobId) {
                        jobObjectFrm.getForm().load({
                            url : 'getJobObject.do',
                            waitMsg : 'Loading Job Object...',
                            params : {
                                jobId : jobObjectFrm.wizardState.jobId
                            },
                            failure : Ext.bind(jobObjectFrm.fireEvent, jobObjectFrm, ['jobWizardLoadException']),
                            success : function(frm, action) {
                                var responseObj = Ext.JSON.decode(action.response.responseText);

                                if (responseObj.success) {
                                    frm.setValues(responseObj.data[0]);
                                    jobObjectFrm.wizardState.jobId = frm.getValues().id;
                                }
                            }
                        });
                    }
                }
            },
            fieldDefaults: {
                labelWidth: 120
            },
            items: [{
                xtype: 'textfield',
                name: 'name',
                fieldLabel: 'Job Name',
                value : Ext.util.Format.format('VGL Job {0}', Ext.Date.format(new Date(), 'Y-m-d g:i a')),
                plugins: [{
                    ptype: 'fieldhelptext',
                    text: 'Enter an optional descriptive name for your job here.'
                }],
                allowBlank: true
            },{
                xtype: 'textfield',
                name: 'description',
                fieldLabel: 'Job Description',
                plugins: [{
                    ptype: 'fieldhelptext',
                    text: 'Enter an optional description for your job here.'
                }],
                allowBlank: true
            },{
                xtype : 'combo',
                fieldLabel : 'Compute Provider',
                name: 'computeServiceId',
                allowBlank: false,
                queryMode: 'local',
                triggerAction: 'all',
                displayField: 'name',
                valueField : 'id',
                typeAhead: true,
                forceSelection: true,
                store : this.computeServicesStore,
                listConfig : {
                    loadingText: 'Getting Compute Services...',
                    emptyText: 'No compute services found.'
                },
                plugins: [{
                    ptype: 'fieldhelptext',
                    text: 'Select a location where your data will be processed. Different locations will have different toolboxes.'
                }],
                listeners : {
                    select : Ext.bind(this.onComputeSelect, this)
                }
            },{
                xtype : 'combo',
                fieldLabel : 'Storage Provider',
                name: 'storageServiceId',
                allowBlank: false,
                queryMode: 'local',
                triggerAction: 'all',
                displayField: 'name',
                valueField : 'id',
                typeAhead: true,
                forceSelection: true,
                store : this.storageServicesStore,
                listConfig : {
                    loadingText: 'Getting Storage Services...',
                    emptyText: 'No storage services found.'
                },
                plugins: [{
                    ptype: 'fieldhelptext',
                    text: 'Select a location where your data will be stored.'
                }]
            },{
                xtype : 'machineimagecombo',
                fieldLabel : 'Toolbox',
                name: 'computeVmId',
                itemId : 'image-combo',
                allowBlank: false,
                queryMode: 'local',
                triggerAction: 'all',
                typeAhead: true,
                forceSelection: true,
                store : this.imageStore,
                listConfig : {
                    loadingText: 'Getting tools...',
                    emptyText: 'No matching toolboxes found. Select a different compute location.'
                },
                plugins: [{
                    ptype: 'fieldhelptext',
                    text: 'Select a toolbox that contains software that you would like to use to process your data.'
                }]
            },
            { xtype: 'hidden', name: 'id' },
            { xtype: 'hidden', name: 'storageProvider' },
            { xtype: 'hidden', name: 'storageEndpoint' },
            { xtype: 'hidden', name: 'registeredUrl' },
            { xtype: 'hidden', name: 'vmSubsetFilePath' },
            { xtype: 'hidden', name: 'vmSubsetUrl' }
            ]
        }]);
    },

    onComputeSelect : function(combo, records) {
        if (!records.length) {
            this.imageStore.removeAll();
            return;
        }

        this.getComponent('image-combo').clearValue();
        this.imageStore.load({
            params : {
                computeServiceId : records[0].get('id')
            }
        });
    },

    getTitle : function() {
        return "Enter job details...";
    },

    beginValidation : function(callback) {
        var jobObjectFrm = this;
        var wizardState = this.wizardState;

        //Ensure we have entered all appropriate fields
        if (!jobObjectFrm.getForm().isValid()) {
            callback(false);
            return;
        }

        //Then save the job to the database before proceeding
        var values = jobObjectFrm.getForm().getValues();
        values.seriesId = jobObjectFrm.wizardState.seriesId;
        Ext.Ajax.request({
            url : 'updateOrCreateJob.do',
            params : values,
            callback : function(options, success, response) {
                if (!success) {
                    portal.widgets.window.ErrorWindow.showText('Error saving details', 'There was an unexpected error when attempting to save the details on this form. Please try again in a few minutes.');
                    callback(false);
                    return;
                }

                var responseObj = Ext.JSON.decode(response.responseText);
                if (!responseObj.success) {
                    portal.widgets.window.ErrorWindow.showText('Error saving details', 'There was an unexpected error when attempting to save the details on this form.', responseObj.msg);
                    callback(false);
                    return;
                }

                jobObjectFrm.wizardState.jobId = responseObj.data[0].id;
                // Store user selected toolbox into wizard state. That toolbox
                // will be used to select relevant script templates or examples.
                wizardState.toolbox = jobObjectFrm.getForm().findField("computeVmId").getRawValue();
                callback(true);
            }
        });
    }
});