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
                                url : 'getJobObject.do',
                                waitMsg : 'Loading Job Object...',
                                params : {
                                    jobId : jobUploadFrm.wizardState.jobId
                                },
                                failure : Ext.bind(jobUploadFrm.fireEvent, jobUploadFrm, ['jobWizardLoadException']),
                                success : function(frm, action) {
                                    var responseObj = Ext.JSON.decode(action.response.responseText);

                                    if (responseObj.success) {
                                        jobUploadFrm.wizardState.jobId = responseObj.data[0].id;
                                    }
                                }
                            });
                            jobUploadFrm.updateFileList();
                        }
                        else {
                            // Ext.bind(jobUploadFrm.updateFileList, jobUploadFrm);
                        	 if (jobUploadFrm.wizardState.jobId === undefined) {
                                 jobUploadFrm.createJob(function() { jobUploadFrm.updateFileList(); });
                             } else {
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
                    text : 'Add Input',
                    iconCls : 'add',
                    itemId : 'add-button',
                    align : 'right',
                    handler : function() {
                        var showPopup = function() {
                            Ext.create('vegl.widgets.JobInputFileWindow', {
                                jobId : jobUploadFrm.wizardState.jobId,
                                width : 500,
                                height : 300,
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
                    }
                }]
            }]
        }]);
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

    createSeries : function(wizardState, callback) {
        // Check whether we already have a series
        if (wizardState.seriesId === undefined) {
            Ext.Ajax.request({
                url: 'secure/createSeries.do',
                callback : function(options, success, response) {
                    var errorMsg = '';
                    var errorInfo = '';
                    if (success) {
                        var responseObj = Ext.JSON.decode(response.responseText);
                        if (responseObj.success && Ext.isNumber(responseObj.data[0].id)) {
                            wizardState.seriesId = responseObj.data[0].id;
                            callback();
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
                    return;
                }
            });
        } else {
            // Already have a series, so just call the callback
            callback();
        }
    },

    /**
     * Creates a new job and hooks the generated JobId to the work-flow
     * @function
     * @param {function} callback
     */
    createJob : function(callback) {
        var wizardState = this.wizardState;

        this.createSeries(wizardState, function() {
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
                url : 'updateOrCreateJob.do',
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

                    var updatedJob = responseObj.data[0];
                    wizardState.jobId = updatedJob.id;

                    // Check with the user if no data set has been captured.
                    var numDownloadReqs = 0;
                    if (updatedJob.jobDownloads !== null) {
                        numDownloadReqs = updatedJob.jobDownloads.length;
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

                    callback(true);
                    return;
                }
            });
        });
    }
});
