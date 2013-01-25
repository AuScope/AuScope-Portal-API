/**
 * Job wizard form for reviewing and then submitting a job for prcessing
 *
 * Author - Josh Vote
 */
Ext.define('vegl.jobwizard.forms.JobSubmitForm', {
    extend : 'vegl.jobwizard.forms.JobUploadForm',

    /**
     * Creates a new JobObjectForm form configured to write/read to the specified global state
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

    //Validation means we go and submit the job
    beginValidation : function(callback) {
        var jobSubmitFrm = this;
        var loadMask = new Ext.LoadMask(Ext.getBody(), {
            msg : 'Submitting Job...',
            removeMask : true
        });
        loadMask.show();
        Ext.Ajax.request({
            url : 'secure/submitJob.do',
            params : {
                jobId : jobSubmitFrm.wizardState.jobId
            },
            timeout : 1000 * 60 * 5, //5 minutes defined in milli-seconds
            callback : function(options, success, response) {
                loadMask.hide();
                var errorMsg, errorInfo;

                if (success) {
                    var responseObj = Ext.JSON.decode(response.responseText);
                    msg = responseObj.msg;
                    if (responseObj.success) {
                        jobSubmitFrm.noWindowUnloadWarning = true;
                        callback(true);
                        window.location = 'joblist.html';
                        return;
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

    getTitle : function() {
        return "Review job before submission...";
    },

    getNextText : function() {
        return 'Submit Job';
    },

    getNextIconClass : function() {
        return 'submit-icon';
    },

    saveJob : function() {
        //This is a giant misdirection - the job was already saved way back in the Job Object form. This part is just
        //making the user feel more involved in the process
        this.noWindowUnloadWarning = true;
        Ext.Msg.alert('Job Saved', 'Your job has been saved for later submission. You can attempt submission later from the <a href="joblist.html">Monitor Jobs</a> page. It is now safe to close this window.');
    }
});