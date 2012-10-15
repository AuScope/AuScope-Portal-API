/**
 * A job wizard form for allowing the user to create their own custom script using the
 * ScriptBuilder library.
 *
 * This file is a reworking of the AuScope Virtual Rock Lab (VRL) project ScriptBuilder.js
 * for the purpose of fitting into a VEGL 'Job Wizard' model
 *
 * Original Author - Cihan Altinay
 * Author - Josh Vote
 * Author - Richard Goh
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
            listeners : {
                jobWizardActive : function() {
                    if (this.wizardState.userAction == 'edit' || this.wizardState.userAction == 'duplicate') {
                        this.loadSavedScript(this.wizardState.jobId);
                        // Once the script is loaded into the memory,
                        // we don't want it to be loaded again to prevent
                        // unsaved changes.
                        this.wizardState.userAction = null;
                    }
                }
            },
            items: [this.scriptBuilderFrm]
        }]);
    },

    // load script source from VGL server filesystem
    loadSavedScript : function(jobId) {
        var loadMask = new Ext.LoadMask(Ext.getBody(), {
            msg : 'Loading saved script...',
            removeMask : true
        });
        loadMask.show();
        Ext.Ajax.request({
            url : 'getSavedScript.do',
            params : {
                'jobId' : jobId
            },
            scope : this,
            callback : function(options, success, response) {
                loadMask.hide();
                var errorMsg, errorInfo;

                if (success) {
                    var responseObj = Ext.JSON.decode(response.responseText);
                    if (responseObj.success) {
                        this.scriptBuilderFrm.replaceScript(responseObj.data);
                        return;
                    } else {
                        errorMsg = responseObj.msg;
                        errorInfo = responseObj.debugInfo;
                    }
                } else {
                    errorMsg = "There was an error loading your script.";
                    errorInfo = "Please try again in a few minutes or report this error to cg_admin@csiro.au.";
                }

                //Create an error object and pass it to custom error window
                var errorObj = {
                    title : 'Script Loading Error',
                    message : errorMsg,
                    info : errorInfo
                };

                var errorWin = Ext.create('portal.widgets.window.ErrorWindow', {
                    errorObj : errorObj
                });
                errorWin.show();
            }
        });
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