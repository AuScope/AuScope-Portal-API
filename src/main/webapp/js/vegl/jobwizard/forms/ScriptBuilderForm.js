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
 */
Ext.define('vegl.jobwizard.forms.ScriptBuilderForm', {
    extend : 'vegl.jobwizard.forms.BaseJobWizardForm',

    scriptBuilderFrm : null,

    /**
     * Creates a new ScriptBuilderForm form configured to write/read to the specified global state
     */
    constructor: function(wizardState) {
        this.scriptBuilderFrm = Ext.create('ScriptBuilder.ScriptBuilder', {
            wizardState : wizardState,
        });

        // Finally, build the main layout once all the pieces are ready.
        this.callParent([{
            wizardState : wizardState,
            layout : 'fit',
            listeners : {
                jobWizardActive : function() {
                    // Builds scriptbuilder component tree with user selected toolbox
                    this.scriptBuilderFrm.buildComponentsPanel(wizardState.toolbox);

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
        // replace tab with 4 spaces whenever it occurs in the sourceText
        sourceText = sourceText.replace(/\t/g,"\u0020\u0020\u0020\u0020");

        Ext.Ajax.request({
            url: 'secure/saveScript.do',
            success: function(response, opts) {
                responseObj = Ext.JSON.decode(response.responseText);
                if (responseObj.success) {
                    callback(true);
                } else {
                    errorMsg = responseObj.msg;
                    errorInfo = responseObj.debugInfo;
                    portal.widgets.window.ErrorWindow.showText('Error', errorMsg, errorInfo);
                    callback(false);
                }
            },
            failure: function(response, opts) {
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
    },

    getHelpInstructions : function() {
        var templates = this.scriptBuilderFrm.queryById('sb-templates-panel');
        var script = this.scriptBuilderFrm.queryById('sb-script-panel')

        return [Ext.create('portal.util.help.Instruction', {
            highlightEl : script.getEl(),
            title : 'Write Job Script',
            anchor : 'left',
            description : 'Everything you have configured so far has been around building an environment in which to run a job. This panel here is where you can enter Python script that will be executed as part of the job processing. The script will be executed in an environment with access to all the tools and input files you have configured.<br/><br/>Please be aware that after this script finishes execution, the entire environment will be erased. If you have any output files they must be uploaded manually using the \'cloud\' utility. You can find more information about this at the <a target="_blank" href="https://www.seegrid.csiro.au/wiki/NeCTARProjects/VglUserGuide">VGL wiki</a>.'
        }), Ext.create('portal.util.help.Instruction', {
            highlightEl : templates.getEl(),
            title : 'Apply Templates',
            anchor : 'right',
            description : 'Depending on the toolbox you have selected, you will have access to number of template scripts that can be prefilled with references to your input files. To add a job script, simply double click it and fill in any required fields. The final template will be added to the \'Script Source\' automatically.'
        })];
    }
});