/**
 * Job wizard form for handling uploads of custom user input files
 *
 * Author - Josh Vote
 */
Ext.define('vegl.jobwizard.forms.JobUploadForm', {
    extend : 'vegl.jobwizard.forms.BaseJobWizardForm',

    uploadedFilesStore : null,
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
            handler: Ext.bind(this.uploadFile, jobUploadFrm)
        });


        var deleteAction = new Ext.Action({
            text: 'Delete Selection',
            disabled: true,
            iconCls: 'cross-icon',
            handler: Ext.bind(jobUploadFrm.deleteFiles, jobUploadFrm)
        });

        var downloadAction = new Ext.Action({
            text: 'Download',
            disabled: true,
            iconCls: 'disk-icon',
            handler: Ext.bind(jobUploadFrm.downloadFile, jobUploadFrm)
        });

        //Store for uploaded file details
        jobUploadFrm.uploadedFilesStore = Ext.create('Ext.data.Store', {
            model : 'vegl.models.FileRecord',
            autoLoad : true,
            proxy : {
                type : 'ajax',
                url : 'listJobFiles.do',
                extraParams : {
                    jobId : jobUploadFrm.wizardState.jobId
                },
                reader : {
                    type : 'json',
                    root : 'data'
                }
            }
        });

        jobUploadFrm.fileGrid = Ext.create('Ext.grid.Panel', {
            title: 'Uploaded files',
            store: jobUploadFrm.uploadedFilesStore,
            stripeRows: true,
            anchor: '100% -20',
            columns: [
                { header: 'Filename', width: 200, sortable: true, dataIndex: 'name' },
                { header: 'Size', width: 100, sortable: true, dataIndex: 'size',
                    renderer: Ext.util.Format.fileSize, align: 'right' }
            ],
            listeners : {
                selectionchange : function(rowModel, records, eOpts) {
                    if (records.length === 0) {
                        deleteAction.setDisabled(true);
                        downloadAction.setDisabled(true);
                    } else {
                        deleteAction.setDisabled(false);
                        downloadAction.setDisabled(false);
                    }
                }
            }
        });

        this.callParent([{
            wizardState : wizardState,
            bodyStyle: 'padding:10px;',
            fileUpload: true,
            frame: true,
            buttons: [
                uploadAction,
                deleteAction,
                downloadAction
            ],
            listeners : {
                jobWizardActive : Ext.bind(jobUploadFrm.updateFileList, jobUploadFrm)
            },
            items: [{
                xtype: 'filefield',
                name: 'file',
                anchor : '100%',
                labelWidth: 150,
                allowBlank: false,
                fieldLabel: 'Select File to upload'
            },
                jobUploadFrm.fileGrid
            ]
        }]);
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
        var myGrid = jobUploadFrm.fileGrid;
        var fileSelections = myGrid.getSelectionModel().getSelection();

        if (fileSelections.length > 0) {
            var selection = fileSelections[0];
            var params = {jobId : jobUploadFrm.wizardState.jobId, filename: selection.get('name')};
            portal.util.FileDownloader.downloadFile('downloadInputFile.do', params)
        }


    },

    //Handler for deleting the files the user has currently selected from the staging area
    deleteFiles : function() {
        var jobUploadFrm = this;
        var fileGrid = jobUploadFrm.fileGrid;
        if (fileGrid.getSelectionModel().getCount() > 0) {
            var selData = fileGrid.getSelectionModel().getSelection();
            var files = [];
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
                            success: Ext.bind(jobUploadFrm.updateFileList, jobUploadFrm),
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
        var ajaxProxy = this.uploadedFilesStore.getProxy();
        ajaxProxy.extraParams.jobId = this.wizardState.jobId;
        this.uploadedFilesStore.load();
    },

    //We don't validate on this form
    beginValidation : function(callback) {
        callback(true);
    },

    getTitle : function() {
        return "Manage job input files...";
    }
});