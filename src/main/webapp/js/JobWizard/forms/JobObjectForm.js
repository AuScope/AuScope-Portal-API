/**
 * Job wizard form for creating and editing a new Job Object
 * 
 * Author - Josh Vote
 */
Ext.namespace("JobBuilder");

JobObjectForm =  Ext.extend(BaseJobWizardForm, {
	
	jobObjectCreated : false,
	
	/**
	 * Creates a new JobObjectForm form configured to write/read to the specified global state
	 */
	constructor: function(wizardState) {
		var jobObjectFrm = this;
		
		jobObjectFrm.jobObjectCreated = false;
		
		JobObjectForm.superclass.constructor.call(this, {
			wizardState : wizardState,
	        bodyStyle: 'padding:10px;',
	        frame: true,
	        defaults: { anchor: "100%" },
	        labelWidth: 150,
	        autoScroll: true,
	        listeners : {
	        	//The first time this form is active create a new job object
	        	jobWizardActive : function() {
	        		if (!jobObjectFrm.jobObjectCreated) {
	        			jobObjectFrm.getForm().load({
	        				url : 'createJobObject.do',
	        				waitMsg : 'Creating Job Object...',
	        				failure : jobObjectFrm.fireEvent.createDelegate(jobObjectFrm, ['jobWizardLoadException']),
	        				success : function(frm, action) {
	        					jobObjectFrm.jobObjectCreated = true;
	        					jobObjectFrm.wizardState.jobId = frm.getValues().id;
	        				}
	        			});
	        		}
	        	}
	        },
	        items: [{
	            xtype: 'textfield',
	            name: 'ec2Endpoint',
	            fieldLabel: 'EC2 Endpoint',
	            readOnly : true
	        }, {
	            xtype: 'textfield',
	            name: 'ec2AMI',
	            fieldLabel: 'EC2 Machine Instance',
	            readOnly : true
	        },{
	            xtype: 'textfield',
	            name: 'name',
	            fieldLabel: 'Job Name',
	            emptyText : 'Enter an optional descriptive name for your job here.',
	            allowBlank: true
	        },{
	            xtype: 'textfield',
	            name: 'description',
	            fieldLabel: 'Job Description',
	            emptyText : 'Enter an optional description for your job here.',
	            allowBlank: true
	        },{
	            xtype: 'textfield',
	            id: 's3OutputBucket',
	            name: 's3OutputBucket',
	            emptyText: 'Enter an Amazon S3 bucket where your job results will be stored',
	            fieldLabel: 'S3 Bucket',
	            value : 'vegl-portal',
	            allowBlank: false
	        },{
	            xtype: 'textfield',
	            id: 's3OutputAccessKey',
	            name: 's3OutputAccessKey',
	            emptyText: 'Enter an Amazon S3 access key that will be used to store your job outputs',
	            fieldLabel: 'S3 Access Key',
	            allowBlank: false
	        }, {
	            xtype: 'textfield',
	            id: 's3OutputSecretKey',
	            name: 's3OutputSecretKey',
	            inputType: 'password',
	            fieldLabel: 'S3 Secret Key',
	            allowBlank: false
	        },
	        { xtype: 'hidden', name: 'id' },
	        { xtype: 'hidden', name: 'emailAddress' },
	        { xtype: 'hidden', name: 'user' },
	        { xtype: 'hidden', name: 'submitDate' },
	        { xtype: 'hidden', name: 'status' },
	        { xtype: 'hidden', name: 'ec2InstanceId' },
	        { xtype: 'hidden', name: 's3OutputBaseKey' },
	        { xtype: 'hidden', name: 'fileStorageId' },
	        { xtype: 'hidden', name: 'registeredUrl' },
	        { xtype: 'hidden', name: 'paddingMinEasting' },
	        { xtype: 'hidden', name: 'paddingMaxEasting' },
	        { xtype: 'hidden', name: 'paddingMinNorthing' },
	        { xtype: 'hidden', name: 'paddingMaxNorthing' },
	        { xtype: 'hidden', name: 'selectionMinEasting' },
	        { xtype: 'hidden', name: 'selectionMaxEasting' },
	        { xtype: 'hidden', name: 'selectionMinNorthing' },
	        { xtype: 'hidden', name: 'selectionMaxNorthing' },
	        { xtype: 'hidden', name: 'mgaZone' },
	        { xtype: 'hidden', name: 'cellX' },
	        { xtype: 'hidden', name: 'cellY' },
	        { xtype: 'hidden', name: 'cellZ' },
	        { xtype: 'hidden', name: 'inversionDepth' },
	        { xtype: 'hidden', name: 'vmSubsetFilePath' }
	        ]
	    });
	},
	
	getTitle : function() {
		return "Enter job details...";
	},
	
	beginValidation : function(callback) {
		var jobObjectFrm = this;
		
		//Ensure we have entered all appropriate fields
		if (!jobObjectFrm.getForm().isValid()) {
			callback(false);
		}
		
		//Then save the job to the database before proceeding
		jobObjectFrm.getForm().submit({
			url : 'updateJob.do',
			params : {
				seriesId : jobObjectFrm.wizardState.seriesId
			},
			waitMsg : 'Saving Job state...',
			failure : function() {
				Ext.Msg.alert('Internal Error', 'There was an error saving these job details. Please try again in a few minutes.');
				callback(false);
			},
			success : function() {
				callback(true);
			}
		});
		
	}
});