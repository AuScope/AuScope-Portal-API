/**
 * @author Josh Vote
 */
Ext.define('vegl.jobwizard.forms.JobObjectForm', {
    /** @lends anvgl.JobBuilder.JobObjectForm */

    extend : 'vegl.jobwizard.forms.BaseJobWizardForm',

    imageStore : null,
    computeTypeStore : null,

    /**
     * Extends 'vegl.jobwizard.forms.BaseJobWizardForm'
     * Job wizard form for creating and editing a new Job Object.
     * Creates a new JobObjectForm form configured to write/read to the specified global state
     * @constructs
     * @param {object} wizardState
     */
    constructor: function(wizardState) {
        var jobObjectFrm = this;

        this.computeServicesStore = Ext.create('Ext.data.Store', {
            fields : [{name: 'id', type: 'string'},
                      {name: 'name', type: 'string'}],
            proxy: {
                type: 'ajax',
                url: 'secure/getComputeServices.do',
                reader: {
                   type: 'json',
                   rootProperty : 'data'
                }
            },
            autoLoad : true
        });

        // create the store, get the machine image
        this.imageStore = Ext.create('Ext.data.Store', {
            model: 'vegl.models.MachineImage',
            proxy: {
                type: 'ajax',
                url: 'secure/getVmImagesForComputeService.do',
                reader: {
                   type: 'json',
                   rootProperty : 'data'
                }
            }
        });

        // create the store and get the compute type
        this.computeTypeStore = Ext.create('Ext.data.Store', {
            model: 'vegl.models.ComputeType',
            proxy: {
                type: 'ajax',
                url: 'secure/getVmTypesForComputeService.do',
                reader: {
                   type: 'json',
                   rootProperty : 'data'
                }
            }
        });

        // call the parent class
        this.callParent([{
            wizardState : wizardState,
            bodyStyle: 'padding:10px;',
            header : false,
            defaults: { anchor: "100%" },
            labelWidth: 150,
            autoScroll: true,
            listeners : {
                //The first time this form is active create a new job object
                jobWizardActive : function() {
                    //If we have a jobId, load that, OTHERWISE the job will be created later
                    if (jobObjectFrm.wizardState.jobId) {
                        jobObjectFrm.handleLoadingJobObject();
                    } else if (jobObjectFrm.wizardState.solutions) {
                        this.imageStore.load();
                    } else {
                        this.imageStore.load();
                    }
                }
            },
            fieldDefaults: {
                labelWidth: 120
            },
            items: [{
                xtype: 'textfield',
                name: 'name',
                itemId : 'name',
                fieldLabel: 'Job Name',
                plugins: [{
                    ptype: 'fieldhelptext',
                    text: 'Enter a useful name for your job here.'
                }],
                allowBlank: true
            },{
                xtype: 'textfield',
                name: 'description',
                itemId : 'description',
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
                itemId : 'computeServiceId',
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
                xtype : 'machineimagecombo',
                fieldLabel : 'Toolbox<span>*</span>',
                name: 'computeVmId',
                itemId : 'image-combo',
                allowBlank: false,
                queryMode: 'local',
                triggerAction: 'all',
                typeAhead: true,
                forceSelection: false,
                store : this.imageStore,
                listConfig : {
                    loadingText: 'Getting tools...',
                    emptyText: 'No matching toolboxes found. Select a different compute location.'
                },
                plugins: [{
                    ptype: 'fieldhelptext',
                    text: 'Select a toolbox that contains software that you would like to use to process your data.'
                }],
                listeners : {
                    select : this.onImageSelect,
                    change : this.onImageChange,
                    scope: this
                }
            },{
                xtype : 'combo',
                fieldLabel : 'Resources<span>*</span>',
                name: 'computeTypeId',
                itemId : 'resource-combo',
                displayField : 'longDescription',
                valueField : 'id',
                allowBlank: false,
                queryMode: 'local',
                triggerAction: 'all',
                typeAhead: true,
                forceSelection: true,
                store : this.computeTypeStore,
                listConfig : {
                    loadingText: 'Getting available resources...',
                    emptyText: 'No matching resource configurations found. Select a different compute location.'
                },
                plugins: [{
                    ptype: 'fieldhelptext',
                    text: 'Select a compute resource configuration that is sufficient for your needs.'
                }]
            },{
                xtype: 'numberfield',
                name: 'ncpus',
                itemId : 'ncpus',
                fieldLabel: 'Number of CPUs',
                plugins: [{
                    ptype: 'fieldhelptext',
                    text: 'This will be the number of CPU\'s that the job scripts will request from the HPC.'
                }],
                allowBlank: false,
                disabled: true,
                hidden: true,
                value: wizardState.ncpus
            },{
                xtype: 'numberfield',
                name: 'mem',
                itemId : 'mem',
                fieldLabel: 'Memory (GB)',
                plugins: [{
                    ptype: 'fieldhelptext',
                    text: 'How much memory (in GB) should be requested for the running job.'
                }],
                allowBlank: false,
                disabled: true,
                hidden: true,
                value: 64
            },{
                xtype: 'numberfield',
                name: 'jobfs',
                itemId : 'jobfs',
                fieldLabel: 'Disk Space (GB)',
                plugins: [{
                    ptype: 'fieldhelptext',
                    text: 'How much working disk space (in GB) should be requested for the running jobs.'
                }],
                allowBlank: false,
                disabled: true,
                hidden: true,
                value: 16
            },{
                xtype : 'checkboxfield',
                fieldLabel : 'Email Notification',
                name: 'emailNotification',
                itemId : 'emailNotification',
                checked: true,
                plugins: [{
                    ptype: 'fieldhelptext',
                    text: 'Tick to receive email notification upon job processing.'
                }]
            },{
                xtype: 'checkbox',
                fieldLabel: 'Set Job Walltime',
                name: 'setJobWalltime',
                itemId: 'setJobWalltime',
                checked: false,
                plugins: [{
                    ptype: 'fieldhelptext',
                    text: 'Select to add an optional walltime (minutes) for your job.'
                }],
                listeners: {
	                change: function(cb, checked) {
	                	Ext.getCmp('walltime').setDisabled(!checked);
	                	//if(!checked)
	                	//	Ext.getCmp('walltime').setValue('0');
	                }
                }
            },{
            	xtype: 'textfield',
                name: 'walltime',
                itemId : 'walltime',
                id: 'walltime',
                disabled: true,
                fieldLabel: 'Walltime',
                maskRe:/[\d]/,
                allowBlank: true
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

    handleLoadingJobObject: function() {
        this.getForm().load({
            url : 'secure/getJobObject.do',
            waitMsg : 'Loading Job Object...',
            params : {
                jobId : this.wizardState.jobId
            },
            scope: this,
            failure : Ext.bind(this.fireEvent, this, ['jobWizardLoadException']),
            success : function(frm, action) {
                var responseObj = Ext.JSON.decode(action.response.responseText);
                if (responseObj.success) {
                    //Loads the image store of user selected
                    //compute provider
                    var jobData = responseObj.data[0];
                    frm.setValues(jobData);

                    // Store the vm type if specified
                    // in the job, and solutionId, for later use.
                    this.wizardState.jobComputeInstanceType = jobData.computeInstanceType;
                    this.wizardState.jobId = frm.getValues().id;

                    this.imageStore.getProxy().setExtraParam('jobId', this.wizardState.jobId);

                    // Load the solution info for the job into the wizardState
        	          Ext.Ajax.request({
                        url: 'getSolutions.do',
                        scope: this,
                        headers: { Accept: 'application/json' },
                        params: { uris: jobData.jobSolutions },
                        success: function(response) {
                            results = Ext.JSON.decode(response.responseText);
                            if (results && results.data) {
                                this.wizardState.solutions = results.data;
                            }
                        },

                        failure: function(response) {
                            console.log("Load job solutions failed! " + response);
                        }
                    });

                    if (!Ext.isEmpty(jobData.computeVmId)) {
                        this.handleAutoSelectingImage(jobData.computeServiceId, jobData.computeVmId);
                    }
                }
            }
        });
    },

    /**
     * We need to handle setting our VM ID's and instance types
     * but those will need to wait on stores to be loaded (frustrating!)
     *
     * forceFinishedUpdating:
     *      Set to true to ignore the imageStore.updating property and assume it's not updating
     *
     *      ANVGL-119 The "load" event in imageStore won't fire if we listen at this point
     *      even though the "updating" property is set on the store (frustrating!)
     *      This override allows us to workaround this when calling from an update callback
     */
    handleAutoSelectingImage: function(computeServiceId, computeVmId, forceFinishedUpdating) {
        if (!Ext.isEmpty(computeVmId)) {
            var jobObjectFrm = this;
            var whenImagesReady = function() {
                if (jobObjectFrm.imageStore.getCount() === 0) {
                    return;
                }

                jobObjectFrm.computeTypeStore.load({
                    params : {
                        computeServiceId : computeServiceId,
                        machineImageId : computeVmId
                    },
                    callback: function() {
                        jobObjectFrm.preselectVmType();
                    }
                });
            };


            if (!forceFinishedUpdating && this.imageStore.updating) {
                this.imageStore.on('load', whenImagesReady, this, {single: true});
            } else if (this.imageStore.getCount() === 0) {
                this.imageStore.getProxy().setExtraParam('jobId', this.wizardState.jobId);
                this.imageStore.load({
                    callback: Ext.bind(whenImagesReady, this)
                });
            } else {
                whenImagesReady();
            }
        }
    },

    /**
     * Select a VM type based on the number of threads specified in the template.
     * @function
     *
     */
    preselectVmType: function() {
        var jobObjectFrm = this;
        var wizardState = this.wizardState;
        var computeTypeStore = this.computeTypeStore;
        var computeTypeId = wizardState.jobComputeInstanceType;
        var frm = jobObjectFrm.getForm();

        // Select a vm type that has ncpus
        // >= nthreads if one hasn't
        // already been selected.
        if (!Ext.isEmpty(computeTypeId)) {
            frm.setValues({computeTypeId: computeTypeId});
        }
        else if (wizardState.nthreads) {
            // Get vm types that are big enough
            var vmtype, vcpus;
            var ncpus = 99999;
            computeTypeStore.each(function(r) {
                vcpus = r.get('vcpus');
                if (vcpus < ncpus && vcpus >= wizardState.nthreads) {
                    vmtype = r;
                    ncpus = vcpus;
                }
            });
            if (vmtype) {
                frm.setValues({computeTypeId: vmtype.get('id')});
            }
        }
    },

    /**
     * loads images for computeServiceId
     * @function
     */
    loadImages : function() {
        this.getComponent('image-combo').clearValue();
        this.getComponent('resource-combo').clearValue();
        this.imageStore.getProxy().setExtraParam('computeServiceId', this.getComponent('computeServiceId').getValue());
        this.imageStore.load();
    },

    onComputeSelect : function(combo, records) {
        if (!records) {
            this.imageStore.removeAll();
            this.computeTypeStore.removeAll();
            return;
        }

        var imageCombo = this.getComponent('image-combo');
        imageCombo.clearValue();
        var resourceCombo = this.getComponent('resource-combo');
        resourceCombo.clearValue();

        if (combo.getValue() === 'nci-raijin-compute') {
            resourceCombo.setHidden(true).setDisabled(true);
            this.getComponent('ncpus').setHidden(false).setDisabled(false);
            this.getComponent('jobfs').setHidden(false).setDisabled(false);
            this.getComponent('mem').setHidden(false).setDisabled(false);

        } else {
            resourceCombo.setDisabled(false).setHidden(false);
            this.getComponent('ncpus').setHidden(true).setDisabled(true);
            this.getComponent('jobfs').setHidden(true).setDisabled(true);
            this.getComponent('mem').setHidden(true).setDisabled(true);
        }

        this.imageStore.getProxy().setExtraParam('computeServiceId', records.get('id'));
        this.imageStore.load({
            scope: this,
            callback: function(records, operation, success) {
                if (records.length === 1) {
                    this.getComponent('image-combo').setValue(records[0]);
                }
            }
        });
    },

    /**
     * Handles the setting of a raw value on a 'Toolbox'
     * @function
     */
    onImageChange: function(combo, newValue, oldValue) {
        var rec = this.imageStore.getById(newValue);
        this.onImageSelect(combo, rec);
    },

    /**
     * Handles the selection on 'Toolbox'
     * @function
     */
    onImageSelect : function(combo, records) {
        var me = this;

        if (!records) {
            this.computeTypeStore.removeAll();
            return;
        }

        this.getComponent('resource-combo').clearValue();
        var selectedComputeService = this.getComponent('computeServiceId').getValue();

        this.computeTypeStore.load({
            params : {
                computeServiceId : selectedComputeService,
                machineImageId : records.get('imageId')
            },
            callback: function() {
                me.preselectVmType();
            }
        });
    },


    /**
     * Title for the interface
     * @function
     * @return {string}
     */
    getTitle : function() {
        return "Enter job details...";
    },

    /**
     * getNumDownloadRequests
     * @function
     * @return {object} size
     */
    getNumDownloadRequests : function() {
        request = ((window.XMLHttpRequest) ? new XMLHttpRequest() : new ActiveXObject("Microsoft.XMLHTTP"));
        request.open("GET", "getNumDownloadRequests.do", false); //<-- false makes it a synchonous request!
        request.send(null);
        respObj = Ext.JSON.decode(request.responseText);
        size = respObj.data;
        return size;
    },

    /**
     * Updates the job with additional details on storage, computing provider etc.
     * @function
     * @param {object} callback
     */
    beginValidation : function(callback) {
        var jobObjectFrm = this;
        var wizardState = this.wizardState;

        // ensure we have entered all appropriate fields
        if (!jobObjectFrm.getForm().isValid()) {
            callback(false);
            return;
        }

        // then save the job to the database before proceeding
        var values = jobObjectFrm.getForm().getValues();
        values.seriesId = jobObjectFrm.wizardState.seriesId;
        values.jobId = jobObjectFrm.wizardState.jobId;

        // update the job here
        Ext.Ajax.request({
            url : 'secure/updateOrCreateJob.do',
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

                var updatedJob = responseObj.data[0];
                jobObjectFrm.wizardState.jobId = updatedJob.id;
                // Store user selected toolbox into wizard state.
                // That toolbox will be used to select relevant script templates or examples.
                wizardState.toolbox = jobObjectFrm.getForm().findField("computeVmId").getRawValue();

                // Store selected resource limits into wizard state. These values will be included
                // in template generation (to ensure valid numbers of CPU's are chosen etc)
                var computeTypeId = jobObjectFrm.getComponent('resource-combo').getValue();

                if (computeTypeId) {
                    var computeType = jobObjectFrm.computeTypeStore.getById(computeTypeId);

                    wizardState.ncpus = computeType.get('vcpus');
                    wizardState.nrammb = computeType.get('ramMB');
                } else {
                    wizardState.ncpus = jobObjectFrm.getComponent('ncpus').getValue();
                    wizardState.nrammb = jobObjectFrm.getComponent('mem').getValue();
                }

                // Don't need to check for data sets at this point since that
                // was done at the start. Continue with the wizard.
                callback(true);
                return;
            }
        });
    },

    /**
     * Gets the help instructions for the interface
     * @function
     * @return {object} instance of 'portal.util.help.Instruction'
     */
    getHelpInstructions : function() {
        var name = this.getComponent('name');
        var description = this.getComponent('description');
        var toolbox = this.getComponent('image-combo');
        var emailNotification = this.getComponent('emailNotification');
        var setJobWalltime = this.getComponent('setJobWalltime');
        var walltime = this.getComponent('walltime');

        return [Ext.create('portal.util.help.Instruction', {
            highlightEl : name.getEl(),
            title : 'Name your job',
            anchor : 'bottom',
            description : 'Every job requires a name. Names don\'t have to be unique but it\'s recommended you choose something meaningful as it will be the primary way to identify this job in the future.'
        }), Ext.create('portal.util.help.Instruction', {
            highlightEl : description.getEl(),
            title : 'Describe your job',
            anchor : 'bottom',
            description : 'Enter an optional description for your job here.'
        }), Ext.create('portal.util.help.Instruction', {
            highlightEl : toolbox.getEl(),
            title : 'Storage Toolbox',
            anchor : 'bottom',
            description : 'A toolbox is a collection of software packages that will be made available to your job when it starts processing. Some toolboxes are restricted to authorised users for licensing reasons. You will not be able to choose a toolbox until after you select a compute provider.'
        }), Ext.create('portal.util.help.Instruction', {
            highlightEl : emailNotification.getEl(),
            title : 'Job completion email notification',
            anchor : 'bottom',
            description : 'The VL will send out email notification to your email address upon job completion. Untick the checkbox if you don\'t want to receive the notification.'
        }), Ext.create('portal.util.help.Instruction', {
            highlightEl : setJobWalltime.getEl(),
            title : 'Set job walltime',
            anchor : 'bottom',
            description : 'If you would like the job to terminate after a set period, check this box and enter a value in the textfield below.'
        }), Ext.create('portal.util.help.Instruction', {
            highlightEl : walltime.getEl(),
            title : 'Walltime',
            anchor : 'bottom',
            description : 'If you would like your job to terminate after a set period, enter the walltime (in minutes) here.'
        })];
    }
});
