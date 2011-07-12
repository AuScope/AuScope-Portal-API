/**
* The Base object for all Job wizard steps to inherit from
* 
* Author - Josh Vote
*/
Ext.namespace("JobBuilder");

BaseJobWizardForm = Ext.extend(Ext.FormPanel, {
	
	/**
	 * State object that is shared by all wizard forms. Use it to communicate
	 * work state between steps (although ideally a job's dependency on this object
	 * should be kept to a bare minimum)
	 */
	wizardState : {},
	
	/**
	 * Set this to true in a child class to skip the warning when the user attempts to navigate
	 * away from the page
	 */
	noWindowUnloadWarning : false,
	
	constructor: function(obj) {
		BaseJobWizardForm.superclass.constructor.call(this, obj);
		
		this.wizardState = obj.wizardState;
		this.addEvents('jobWizardActive',			//Fired whenever a job wizard form becomes active
					   'jobWizardDeactive',			//Fired whenever a job wizard form deactivates
					   'jobWizardLoadException');	//Fired whenever a job wizard form experiences problems loading from the server
		
		this.on('jobWizardActive', function() {
			Ext.EventManager.on(window, 'beforeunload', this.onBeforeWindowUnload, this);
		}, this);
		
		this.on('jobWizardDeactive', function() {
			Ext.EventManager.un(window, 'beforeunload', this.onBeforeWindowUnload, this);
		}, this);
	},
	
	/**
	 * Handler that is called whenever the user attempts to navigate away from the page whilst this 
	 * job configuration step is active.
	 */
	onBeforeWindowUnload : function(e) {
		if (!this.noWindowUnloadWarning) {
			e.browserEvent.returnValue = "All entered details will be lost!";
		}
	},
	
	/**
	 * [abstract] This function should begin the validation of this wizard step asynchronously.
	 * and the result of the validation should be passed back to the callback function.
	 * 
	 * If the validation result is false then the user will not be permitted to progress
	 * the workflow
	 * 
	 * callback : function(success)
	 */
	beginValidation : function(callback) {
		callback.call(this, [true]);
	},
	
	/**
	 * [abstract] This function should return the title of the job wizard step.
	 */
	getTitle : function() {
		return "";
	},
	
	/**
	 * Gets the text to show on the 'Previous' button
	 */
	getPreviousText : function() {
		return '&laquo; Previous';
	},
	
	/**
	 * Gets the text to show on the 'Next' button
	 */
	getNextText : function() {
		return 'Next &raquo;';
	}
});
