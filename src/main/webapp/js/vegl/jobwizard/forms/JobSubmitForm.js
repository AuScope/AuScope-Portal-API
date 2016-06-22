/**
 * @author  Josh Vote
 */
Ext.define('vegl.jobwizard.forms.JobSubmitForm', {
    /** @lends anvgl.JobBuilder.JobSubmitForm */
    extend : 'vegl.jobwizard.forms.JobUploadForm',

    /**
     * Extends 'vegl.jobwizard.forms.JobUploadForm'
     * Job wizard form for reviewing and then submitting a job for processing
     * Creates a new JobObjectForm form configured to write/read to the specified global state
     * @constructs
     */
    constructor: function(wizardState) {
        this.additionalButtons = [{
            text : 'Save Job',
            qtip : 'Save this job for later submission',
            iconCls : 'disk-icon',
            handler : Ext.bind(this.saveJob, this)
        }];

        this.callParent(arguments);
    },

    /**
     * Submits the job
     * @function
     * @param {object} callback
     */
    beginValidation : function(callback) {
        var jobSubmitFrm = this;
        Ext.getBody().mask('Submitting Job...').setStyle('z-index', '99999'); //ANVGL-107 Ensure this mask doesn't end up behind any modal window masks

        Ext.Ajax.request({
            url : 'secure/submitJob.do',
            params : {
                jobId : jobSubmitFrm.wizardState.jobId
            },
            timeout : 1000 * 60 * 5, //5 minutes defined in milli-seconds
            callback : function(options, success, response) {
                Ext.getBody().unmask();
                var errorMsg, errorInfo;

                if (success) {
                    var responseObj = Ext.JSON.decode(response.responseText);
                    msg = responseObj.msg;
                    if (responseObj.success) {
                        if (responseObj.data && responseObj.data.containsPersistentVolumes) {
                            Ext.window.MessageBox.alert({
                                title: 'Warning',
                                message: 'This job will create an instance with persistent EBS volumes. These will need to be manually removed from AWS as the portal cannot remove them without potentially causing you to lose data.',
                                fn: function() {
                                    jobSubmitFrm.noWindowUnloadWarning = true;
                                    callback(true);
                                    window.location = 'joblist.html';
                                }
                            });
                            return;
                        } else {
                            jobSubmitFrm.noWindowUnloadWarning = true;
                            callback(true);
                            window.location = 'joblist.html';
                            return;
                        }
                    } else {
                        errorMsg = responseObj.msg;
                        errorInfo = responseObj.debugInfo;
                    }
                } else {
                    errorMsg = "There was an error submitting your script for processing.";
                    errorInfo = "Please try again in a few minutes or report this error to cg_admin@csiro.au.";
                }

                //Create an error object and pass it to custom error window
                var errorObj = {
                    title : 'Failure',
                    message : errorMsg,
                    info : errorInfo
                };

                var errorWin = Ext.create('portal.widgets.window.ErrorWindow', {
                    errorObj : errorObj
                });
                errorWin.show();

                callback(false);
            }
        });
    },

    /**
     * Title for the interface
     * @function
     * @return {string}
     */
    getTitle : function() {
        return "Review job before submission...";
    },

    /**
     * 'Next' text
     * @function
     * @return {string}
     */
    getNextText : function() {
        return 'Submit Job';
    },

    /**
     * 'Next' icon
     * @function
     * @return {string}
     */
    getNextIconClass : function() {
        return 'submit-icon';
    },

    /**
     * This is a giant misdirection - the job was already saved way back in the Job Series form. This part is just
     * making the user feel more involved in the process
     * @function
     */
    saveJob : function() {
        this.noWindowUnloadWarning = true;

        Ext.Msg.alert('Job Saved', 'Your job has been saved for later submission. You can attempt submission later from the <a href="joblist.html">Monitor Jobs</a> page. It is now safe to close this window.');
    }
});