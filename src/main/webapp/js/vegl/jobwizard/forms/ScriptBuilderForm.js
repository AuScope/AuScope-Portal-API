/**
 * A job wizard form for allowing the user to create their own custom script using the
 * ScriptBuilder library.
 *
 * This file is a reworking of the AuScope Virtual Rock Lab (VRL) project ScriptBuilder.js
 * for the purpose of fitting into a VEGL 'Job Wizard' model
 *
 * Original Author - Cihan Altinay
 * Author - Josh Vote
 *
 */
Ext.define('vegl.jobwizard.forms.ScriptBuilderForm', {
    extend : 'vegl.jobwizard.forms.BaseJobWizardForm',

    scriptBuilderFrm : null,

    /**
     * Creates a new ScriptBuilderForm form configured to write/read to the specified global state
     */
    constructor: function(wizardState) {
        this.scriptBuilderFrm = Ext.create('ScriptBuilder.ScriptBuilder', {
            wizardState : wizardState
        });

        // Finally, build the main layout once all the pieces are ready.
        this.callParent([{
            wizardState : wizardState,
            layout : 'fit',
            items: [this.scriptBuilderFrm]
        }]);
    },

    // submit script source for storage at the server
    beginValidation : function(callback) {
        sourceText = this.scriptBuilderFrm.getScript();

        Ext.Ajax.request({
            url: 'saveScript.do',
            success: function() {
                callback(true);
            },
            failure: function() {
                Ext.Msg.alert('Error', 'Error storing script file! Please try again in a few minutes');
                callback(false);
            },
            params: {
                'sourceText': sourceText,
                'jobId': this.wizardState.jobId
            }
        });
    },

    /**
     * [abstract] This function should return the title of the job wizard step.
     */
    getTitle : function() {
        return "Define your job script.";
    }
});