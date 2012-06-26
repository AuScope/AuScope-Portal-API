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

    textEditMode : false,

    ControllerURL : "scriptbuilder.html",

    // default content of the component description panel
    compDescText : '<p class="desc-info">Select a component to see its description, double-click to add it to the script.<br/><br/>Double-click the Simulation Container to change simulation settings.</p>',
    // content of the component description panel in text editor mode
    compDescTextEditor : '<p class="desc-info">Select a component to see its description.<br/></p>',

    /**
     * Creates a new ScriptBuilderForm form configured to write/read to the specified global state
     */
    constructor: function(wizardState) {
        var scriptBuilderFrm = this;


        // Finally, build the main layout once all the pieces are ready.
        this.callParent([{
            wizardState : wizardState,
            layout : 'fit',
            id : 'scriptbuilder-form',
            title : 'Current Script',
            items: [{
                id: 'sourcetext',
                xtype: 'textarea',
                border : false,
                width: '100%',
                height: '100%',
                style : {
                    'font-family' : 'monospace'
                }
            }]
        }]);
    },

    // submit script source for storage at the server
    beginValidation : function(callback) {
        var scriptBuilderFrm = this;
        var sourceTextCmp = Ext.getCmp('sourcetext');
        var sourceText = null;

        sourceText = sourceTextCmp.getValue();

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
                'jobId': scriptBuilderFrm.wizardState.jobId
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