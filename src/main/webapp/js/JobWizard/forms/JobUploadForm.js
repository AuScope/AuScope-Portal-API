/**
 * Job wizard form for handling uploads of custom user input files
 * 
 * Author - Josh Vote
 */
Ext.namespace("JobBuilder");

JobUploadForm =  Ext.extend(BaseJobWizardForm, {
	
	uploadedFilesStore : null,
	fileRecord : Ext.data.Record.create([{ name: 'name', mapping: 'name' },
	                                     { name: 'size', mapping: 'size' },
	                                     { name: 'parentPath', mapping: 'parentPath' }]),
    fileGrid : null,
	
	/**
	 * Creates a new JobUploadForm form configured to write/read to the specified global state
	 */
	constructor: function(wizardState) {
		var jobUploadFrm = this;
		
		var uploadAction = new Ext.Action({
	        text: 'Upload File',
	        disabled: false,
	        iconCls: 'disk-icon',
	        handler: jobUploadFrm.uploadFile.createDelegate(jobUploadFrm)
	    });
		

	    var deleteAction = new Ext.Action({
	        text: 'Delete Selection',
	        disabled: true,
	        iconCls: 'cross-icon',
	        handler: jobUploadFrm.deleteFiles.createDelegate(jobUploadFrm)
	    });
	    
	    var downloadAction = new Ext.Action({
	        text: 'Download',
	        disabled: true,
	        iconCls: 'disk-icon',
	        handler: jobUploadFrm.downloadFile.createDelegate(jobUploadFrm)
	    });
	    
	    //Store for uploaded file details
	    jobUploadFrm.uploadedFilesStore = new Ext.data.SimpleStore({
	        fields: [
	            { name: 'name', type: 'string' },
	            { name: 'size', type: 'int' },
	            { name: 'parentPath', type: 'string' }
	        ]
	    });
	    
	    jobUploadFrm.fileGrid = new Ext.grid.GridPanel({
	        title: 'Uploaded files',
	        store: jobUploadFrm.uploadedFilesStore,
	        stripeRows: true,
	        anchor: '100% -20',
	        columns: [
	            { header: 'Filename', width: 200, sortable: true, dataIndex: 'name' },
	            { header: 'Size', width: 100, sortable: true, dataIndex: 'size',
	                renderer: Ext.util.Format.fileSize, align: 'right' }
	        ],
	        sm: new Ext.grid.RowSelectionModel({
	            singleSelect: false,
	            listeners: {
	                'selectionchange': function(sm) {
	                    if (jobUploadFrm.fileGrid.getSelectionModel().getCount() == 0) {
	                        deleteAction.setDisabled(true);
	                        downloadAction.setDisabled(true);
	                    } else {
	                        deleteAction.setDisabled(false);
	                        downloadAction.setDisabled(false);
	                    }
	                }
	            }

	        })
	    });
		
		JobUploadForm.superclass.constructor.call(this, {
			wizardState : wizardState,
	        bodyStyle: 'padding:10px;',
	        fileUpload: true,
	        frame: true,
	        labelWidth: 150,
	        buttons: [
	            uploadAction,
	            deleteAction,
	            downloadAction
	        ],
	        listeners : {
				jobWizardActive : jobUploadFrm.updateFileList.createDelegate(jobUploadFrm)
			},
	        items: [{
	            anchor: '100%',
	            xtype: 'label',
	            name: 'jobStatus',
	            style: 'font-weight:bold;',
	            text : ''
	        },{
	            anchor: '100%',
	            xtype: 'textfield',
	            name: 'file',
	            inputType: 'file',
	            allowBlank: false,
	            fieldLabel: 'Select File to upload'
	        },
	        	jobUploadFrm.fileGrid
	        ]
	    });
	},
	
	//Handler for uploading the currently browsed file to the job staging area
	uploadFile : function(b, e, overwrite) {
		var jobUploadFrm = this;
		var fileGrid = jobUploadFrm.fileGrid;
	    if (this.getForm().isValid()) {
	        var ufName = jobUploadFrm.getForm().getValues().fileInputField;
	        var fileStore = fileGrid.getStore();
	        if (!overwrite && fileStore.find('name', ufName) > -1) {
	            Ext.Msg.confirm('File exists',
	            		'A file by that name already exists. Overwrite?',
	            		function(btn) {
	            			if (btn === "yes") {
	            				jobUploadFrm.uploadFile(b, e, true);
	            			}
	            	   	});
	            return;
	        }
	        
	        //Submit our form so our files get uploaded...
	        jobUploadFrm.getForm().submit({
	            url: 'uploadFile.do',
	            success: function(form, action) {
	                if (action.result.success) {
	                    jobUploadFrm.updateFileList();
	                    return;
	                } else {
	                	Ext.Msg.alert('Error uploading file. '+action.result.error);
	                }
	                Ext.Msg.alert('Failure', 'File upload failed. Please try again in a few minutes.');
	            },
	            failure: function() {
	        		Ext.Msg.alert('Failure', 'File upload failed. Please try again in a few minutes.');
	        	},
	            params: {
	        		jobId : jobUploadFrm.wizardState.jobId
	        	},
	            waitMsg: 'Uploading file, please wait...',
	            waitTitle: 'Upload file'
	        });
	    } else {
	        Ext.Msg.alert('No file selected',
	                'Please use the browse button to select a file.');
	    }
	},
	
	//Handler for downloading a file from the job staging area
	downloadFile : function() {
		var jobUploadFrm = this;
	    var body = Ext.getBody();
	    var frame = body.createChild({
	        tag:'iframe',
	        cls:'x-hidden',
	        id:'iframe',
	        name:'iframe'
	    });
	    var myGrid = jobUploadFrm.fileGrid;
	    var jobData = myGrid.getSelectionModel().getSelected().data;
	    var params = {jobId : jobUploadFrm.wizardState.jobId, filename: jobData.name};
	    var form = body.createChild({
	        tag:'form',
	        cls:'x-hidden',
	        id:'form',
	        target:'iframe',
	        method:'POST'
	    });
	    form.dom.action = 'downloadInputFile.do?'+Ext.urlEncode(params);
	    form.dom.submit();
	},
	
	//Handler for deleting the files the user has currently selected from the staging area
	deleteFiles : function() {
		var jobUploadFrm = this;
	    var fileGrid = jobUploadFrm.fileGrid;
	    if (fileGrid.getSelectionModel().getCount() > 0) {
	        var selData = fileGrid.getSelectionModel().getSelections();
	        var files = new Array();
	        var jobIds = new Array();
	        for (var i=0; i<selData.length; i++) {
	            files.push(selData[i].get('name'));
	        }
	        
	        Ext.Msg.show({
	            title: 'Delete Files',
	            msg: 'Are you sure you want to delete the selected files?',
	            buttons: Ext.Msg.YESNO,
	            icon: Ext.Msg.WARNING,
	            closable: false,
	            fn: function(btn) {
	                if (btn == 'yes') {
	                    Ext.Ajax.request({
	                        url: 'deleteFiles.do',
	                        success: jobUploadFrm.updateFileList.createDelegate(jobUploadFrm),
	                        failure: function() {
	                    		Ext.Msg.alert('Failure', 'File deletion failed! Please try again in a few minutes.');
	                    	}, 
	                        params: { 
	                    		'fileName': files,
	                    		'jobId': jobUploadFrm.wizardState.jobId
	                        }
	                    });
	                }
	            }
	        });
	    }
	},
	
	//Refresh the server side file list
	updateFileList : function() {
		var jobUploadFrm = this;
		var loadMask = new Ext.LoadMask(Ext.getBody(), {
			msg : 'Listing input files...',
			removeMask : true
		});
		loadMask.show();
		Ext.Ajax.request({
	        url: 'listJobFiles.do',
	        callback : loadMask.hide.createDelegate(loadMask),
	        params : {
				jobId : jobUploadFrm.wizardState.jobId
			},
	        success: function(response, request) {
				var responseObj = Ext.util.JSON.decode(response.responseText);
	            var fileStore = jobUploadFrm.uploadedFilesStore;
	            fileStore.removeAll();
	            
	            if (responseObj.success) {
		            for (var i=0; i<responseObj.data.length; i++) {
		                var newFile = new jobUploadFrm.fileRecord({
		                    name: responseObj.data[i].name,
		                    size: responseObj.data[i].size,
		                    parentPath: responseObj.data[i].parentPath
		                });
		                fileStore.add(newFile);
		            }
	            } else {
	            	Ext.Msg.alert('Failure', 'File listing failed! Please try again in a few minutes.');
	            }
	        },
	        failure: jobUploadFrm.fireEvent.createDelegate(jobUploadFrm, ['jobWizardLoadException'])
	    });
	},
	
	//We don't validate on this form
	beginValidation : function(callback) {
		callback(true);
	},
	
	getTitle : function() {
		return "Manage job input files...";
	}
});