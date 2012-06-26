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
            url : 'submitJob.do',
            params : {
                jobId : jobSubmitFrm.wizardState.jobId
            },
            timeout : 1000 * 60 * 5, //5 minutes defined in milli-seconds
            callback : function(options, success, response) {
                loadMask.hide();
                if (success) {
                    var responseObj = Ext.JSON.decode(response.responseText);
                    if (responseObj.success) {
                        jobSubmitFrm.noWindowUnloadWarning = true;
                        callback(true);
                        window.location = 'joblist.html';
                        return;
                    }
                }

                Ext.Msg.alert('Failure', 'There was a problem submitting your job. Please try again in a few minutes.');
                callback(false);
            }
        });
    },

    getTitle : function() {
        return "Review job before submission...";
    },

    getNextText : function() {
        return 'Submit Job';
    }
});