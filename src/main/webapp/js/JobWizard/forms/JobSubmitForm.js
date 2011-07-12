/**
 * Job wizard form for reviewing and then submitting a job for prcessing
 * 
 * Author - Josh Vote
 */
Ext.namespace("JobBuilder");

JobSubmitForm =  Ext.extend(JobUploadForm, {

	/**
	 * Creates a new JobObjectForm form configured to write/read to the specified global state
	 */
	constructor: function(wizardState) {
		JobSubmitForm.superclass.constructor.call(this, wizardState);
	},
	
	//Validation means we go and submit the job
	beginValidation : function(callback) {
		var jobSubmitFrm = this;
		Ext.Ajax.request({
			url : 'submitJob.do',
			params : {
				jobId : jobSubmitFrm.wizardState.jobId
			},
			callback : function(options, success, response) {
				if (success) {
					var responseObj = Ext.util.JSON.decode(response.responseText);
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