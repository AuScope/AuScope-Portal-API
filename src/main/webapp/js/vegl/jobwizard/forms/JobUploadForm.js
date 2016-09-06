/**
  * @author Josh Vote
  */
Ext.define('vegl.jobwizard.forms.JobUploadForm', {
   /**
     * @lends anvgl.JobSubmitForm.JobUploadForm
     */
    extend : 'vegl.jobwizard.forms.BaseJobWizardForm',

    fileGrid : null,
    uploadedFilesStore : null,

    /**
     * Job wizard form for handling uploads of custom user input files. First interface of the 4-step job submission work-flow.
     * Creates a new JobUploadForm form configured to write/read to the specified global state
     *
     * @constructs
     * @param {object} wizardState
     */
    constructor: function(wizardState) {
        var jobUploadFrm = this;

        // execute the parent class
        this.callParent([{
            wizardState : wizardState,
            bodyStyle: 'padding:10px;',
            fileUpload: true,
            header : false,
            buttons: [],
            listeners : {
                    jobWizardActive : function() {

                        if (this.wizardState.userAction == 'edit') {
                            jobUploadFrm.getForm().load({
                                url : 'secure/getJobObject.do',
                                waitMsg : 'Loading Job Object...',
                                params : {
                                    jobId : jobUploadFrm.wizardState.jobId
                                },
                                failure : Ext.bind(jobUploadFrm.fireEvent, jobUploadFrm, ['jobWizardLoadException']),
                                success : function(frm, action) {
                                    var responseObj = Ext.JSON.decode(action.response.responseText);

                                    if (responseObj.success) {
                                        jobUploadFrm.wizardState.jobId = responseObj.data[0].id;
                                        jobUploadFrm.updateFileList();
                                    }
                                }
                            });
                        }
                        else {
                        	  if (jobUploadFrm.wizardState.jobId === undefined) {
                                jobUploadFrm.confirmContinue(function(doContinue) {
                                    if (doContinue) {
                                        jobUploadFrm.createJob(function() {
                                            jobUploadFrm.updateFileList();
                                        });
                                    }
                                    else {
                                        Ext.util.History.back();
                                    }
                                });
                            }
                            else {
                                jobUploadFrm.updateFileList();
                            }
                        }
                    }
            },
            items: [{
                xtype : 'jobinputfilespanel',
                itemId : 'files-panel',
                currentJobId : wizardState.jobId,
                title: 'Input files',
                stripeRows: true,
                anchor: '100% -20',
                buttons : [{
                    text: 'Copy from Job',
                    iconCls: 'download-cloud-icon',
                    align : 'right',
                    scope: this,
                    handler: function() {
                        this._doPopup('vegl.widgets.JobInputFileCopyWindow', 870, 400);
                    }
                },{
                    text: 'Remote Download',
                    iconCls: 'world-add-icon',
                    align : 'right',
                    scope: this,
                    handler: function() {
                        this._doPopup('vegl.widgets.JobInputFileRemoteWindow');
                    }
                },{
                    text : 'Upload File',
                    iconCls : 'add',
                    itemId : 'add-button',
                    align : 'right',
                    scope: this,
                    handler : function() {
                        this._doPopup('vegl.widgets.JobInputFileUploadWindow');
                    }
                }]
            }]
        }]);
    },

    _doPopup: function(cls, width, height) {
        var jobUploadFrm = this;
        var showPopup = function() {
            Ext.create(cls, {
                jobId : jobUploadFrm.wizardState.jobId,
                width : width ? width : 500,
                height : height ? height : 300,
                modal : true,
                listeners : {
                    close : function() {
                        jobUploadFrm.updateFileList();
                    }
                }
            }).show();
        };

        if (jobUploadFrm.wizardState.jobId === undefined) {
            jobUploadFrm.createJob(showPopup);
        } else {
            showPopup();
        }
    },

    /**
     * Refresh the server side file list, and fetch all input-files associated with the job.
     * @function
     */
    updateFileList : function() {
        var filesPanel = this.getComponent('files-panel');
        filesPanel.currentJobId = this.wizardState.jobId;
        filesPanel.updateFileStore();
    },

    /**
     *
     * @function
     */
    getNumDownloadRequests : function() {
        request = ((window.XMLHttpRequest) ? new XMLHttpRequest() : new ActiveXObject("Microsoft.XMLHTTP"));
        // 'false' makes it a synchonous request!
        request.open("GET", "getNumDownloadRequests.do", false);
        request.send(null);

        respObj = Ext.JSON.decode(request.responseText);
        size = respObj.data;

        return size;
    },

    /**
     * When the user wishes to proceed to the next step and clicks 'Next'
     * @function
     */
    beginValidation : function(callback) {
        // Create the job and invoke the callback
        this.createJob(callback);
    },

    /**
     * Title of the interface
     * @function
     * @return {string}
     */
    getTitle : function() {
        return "Manage job input files...";
    },

    /**
     * Gets the help instructions for the interface
     * @function
     * @return {object} instance of 'portal.util.help.Instruction'
     */
    getHelpInstructions : function() {
        var filesPanel = this.getComponent('files-panel');
        var addButton = filesPanel.queryById('add-button');

        // return the help instructions for the interface
        return [Ext.create('portal.util.help.Instruction', {
            highlightEl : filesPanel.getEl(),
            title : 'Review Job Inputs',
            anchor : 'top',
            description : 'Every job has a number of input files. These files may yet to have been downloaded from a remote web service, or they may have already been uploaded from your desktop. Remote files will not be downloaded until after the job begins execution.<br/><br/>You can interact with these input files by right clicking them or by selecting them and pressing the \'Actions\' button.'
        }), Ext.create('portal.util.help.Instruction', {
            highlightEl : addButton.getEl(),
            title : 'Add more Inputs',
            anchor : 'top',
            description : 'If you\'d like to add your own custom input files/web service downloads then press this button. You will be prompted to either upload a file from your local machine or to specify the public URL where the file can be accessed from.'
        })];
    },

    /**
     * Confirm with the user whether to continue if no dataset has been captured.
     *
     * Calls callback passing true/false to indicate whether to continue.
     *
     * @function
     * @param {function} callback
     */
    confirmContinue: function(callback) {
        // Did the user already confirm to continue earlier?
        if (this.wizardState.skipConfirmPopup) {
            callback(true);
            return;
        }

        // If no data set has been captured in the session and we have no job
        // then check with the user whether or not to continue.
        if (this.wizardState.jobId === undefined && this.getNumDownloadRequests() == 0) {
            var self = this;
            Ext.Msg.confirm('Confirm',
                            'No data set has been captured. Do you want to continue?',
                            function(button) {
                                if (button === 'yes') {
                                    self.wizardState.skipConfirmPopup = true;
                                    callback(true);
                                    return;
                                } else {
                                    callback(false);
                                    return;
                                }
                            });
        }
        else {
            // Continue by default
            callback(true);
        }
    },

    /**
     * Creates a new job and hooks the generated JobId to the work-flow
     * @function
     * @param {function} callback
     */
    createJob : function(callback) {
        var wizardState = this.wizardState;
        var params = {};

        // If we already created the job, pass the id so it's updated instead.
        if (wizardState.jobId !== undefined) {
            params.id = wizardState.jobId;
        }

        if (typeof wizardState.name === "undefined") {
            params.name = Ext.util.Format.format('ANVGL Job - {0}', Ext.Date.format(new Date(), 'd M Y g:i a'));
        }

        // see ANVGL 35
        params.computeServiceId = "aws-ec2-compute";
        params.storageServiceId = "amazon-aws-storage-sydney";

        Ext.Ajax.request({
            url : 'secure/updateOrCreateJob.do',
            params : params,
            callback : function(options, success, response) {
                if (!success) {
                    portal.widgets.window.ErrorWindow.showText(
                        'Error creating job',
                        'There was an unexpected error when attempting to save the details on this form. Please try again in a few minutes.'
                    );
                    callback(false);
                    return;
                }

                var responseObj = Ext.JSON.decode(response.responseText);
                if (!responseObj.success) {
                    portal.widgets.window.ErrorWindow.showText(
                        'Error saving details',
                        'There was an unexpected error when attempting to save the details on this form.',
                        responseObj.msg
                    );
                    callback(false);
                    return;
                }

                // continue with the wizard
                wizardState.jobId = responseObj.data[0].id;
                callback(true);
                return;
            }
        });
    }

});
