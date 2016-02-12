/**
  * @author Josh Vote
  */
Ext.define('vegl.jobwizard.forms.JobUploadForm', {
   /** 
     * @lends JobUploadForm 
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
        
        // create a series
        this.createSeries(wizardState);
        
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
                                    /*
                                    if (responseObj.success) {
                                        jobUploadFrm.wizardState.jobId = frm.getValues().id;
                                    }
                                    */
                                }
                            });
                        }
                        else {
                            // Ext.bind(jobUploadFrm.updateFileList, jobUploadFrm);
                            jobUploadFrm.updateFileList();
                        }
                    }
            },
            items: [{
                xtype : 'jobinputfilespanel',
                itemId : 'files-panel',
                currentJobId : wizardState.jobId,
                title: 'Input files (review)',
                stripeRows: true,
                anchor: '100% -20',
                buttons : [{
                    text : 'Add Input',
                    iconCls : 'add',
                    itemId : 'add-button',
                    align : 'right',
                    handler : function() {
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
     * Creates a series
     * @function
     * @param {object} wizardState
     */
    createSeries : function(wizardState) {
        var me = this;
        
        try {
            Ext.Ajax.request({
                url: 'secure/createSeries.do',
                callback : function(options, success, response) {
                    if (success) {
                        var responseObj = Ext.JSON.decode(response.responseText);
                        if (responseObj.success && Ext.isNumber(responseObj.data[0].id)) {
                            wizardState.seriesId = responseObj.data[0].id;
                            me.createJob(wizardState);
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
        } catch (exception) {
            console.log("Exception: JobUploadForm.createSeries(), details below - ");
            console.log(exception);
        }
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
        var me = this;
        var wizardState = this.wizardState;
        
        var numDownloadReqs = this.getNumDownloadRequests();
        
        // set the pop-up flag to if it is not true
        if (!!!wizardState.skipConfirmPopup) {
            wizardState.skipConfirmPopup = false;
        }
        
        // render the pop-up the first time the user proceeds with no data-set captured
        if (wizardState.skipConfirmPopup === false && numDownloadReqs === 0) {
            Ext.Msg.confirm('Confirm',
                    'No data set has been captured. Do you want to continue?',
                    function(button) {
                        if (button === 'yes') {

                            // toggle the flag
                            wizardState.skipConfirmPopup = true;
                            
                            // proceed to the next step
                            callback(true);
                            return;
                        } else {
                            // do not proceed to the next step
                            callback(false);
                            return false;
                        }
                });
        } else {
            // proceed to 'update' a job
            me.updateJob();
            
            callback(true);
            return; 
        };
    },
    
    
    /**
     * Creates a job
     * @function
     * @param {object} callback
     */
    createJob : function(wizardState) {
        var values = {};
        values.seriesId = this.wizardState.seriesId;
        values.name = Ext.util.Format.format('ANVGL Job - {0}', Ext.Date.format(new Date(), 'Y-m-d g:i a')); 
        
        this.job(values);
    },
    
    
    /**
     * Updates a job
     * @function
     * @param {object} callback
     */
    updateJob : function() {
        var me = this;
        
        var values = this.getForm().getValues();
        values.seriesId = this.wizardState.seriesId;
        values.jobId = this.wizardState.jobId;

        me.job(values);
    },
    
    
    /**
     * Ajax call to create (or update) a job
     * @function
     * @param {object} callback
     */
    job : function(values) {
        var jobUploadFrm = this;
        var wizardState = this.wizardState;
        
        try {
            Ext.Ajax.request({
                url : 'updateOrCreateJob.do',
                params : values,
                callback : function(options, success, response) {
                    if (!success) {
                        portal.widgets.window.ErrorWindow.showText('Error saving details', 'There was an unexpected error when attempting to save the details on this form. Please try again in a few minutes.');
                    }
    
                    var responseObj = Ext.JSON.decode(response.responseText);
                    
                    if (!responseObj.success) {
                        portal.widgets.window.ErrorWindow.showText('Error saving details', 'There was an unexpected error when attempting to save the details on this form.', responseObj.msg);
                    }
    
                    // if a jobId does not already exist (the first time)
                    if(!!!wizardState.jobId) {
                        wizardState.jobId = responseObj.data[0].id;
                        
                    }
                    return responseObj.data[0].id;
                }
            });
        } catch (exception) {
            console.log("Exception: JobUploadForm.job(), details below - ");
            console.log(exception);
        }
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
    }
});