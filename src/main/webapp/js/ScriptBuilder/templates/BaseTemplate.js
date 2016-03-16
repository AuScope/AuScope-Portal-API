/**
 * The base template that all other ScriptBuilder templates should inherit from.
 *
 * A script builder template utilitiy for setting variables. These variables
 * will be coupled with a script template in order to generate an actual text file.
 */
Ext.define('ScriptBuilder.templates.BaseTemplate', {
    extend : 'portal.util.ObservableMap',

    statics : {
        /**
         * The templating operation succeeded
         */
        TEMPLATE_RESULT_SUCCESS : 0,
        /**
         * The templating operation was cancelled by the user
         */
        TEMPLATE_RESULT_CANCELLED : 1,
        /**
         * The templating operation failed and the user should be warned
         */
        TEMPLATE_RESULT_ERROR : 2
    },

    description : null,
    name : null,
    wizardState : null,

    constructor : function(config) {
        this.description = config.description ? config.description : '';
        this.name = config.name ? config.name : '';
        this.wizardState = config.wizardState ? config.wizardState : {};

        this.callParent(arguments);
    },

    /**
     * Utility for getting the templated script AFTER showing a popup GUI where the user can
     * enter in additional parameters.
     *
     * callback(Number status, String script) - called by the template when a script snippet has finished templating.
     * templateName - the name of the template to use
     * formPanel - and Ext.form.Panel or equivalent constructor object that will be shown in the popup window
     * includeEmptyText - Whether the form field's empty text will be included in the template (defaults to false)
     */
    _getTemplatedScriptGui : function(callback, templateName, formPanel, includeEmptyText) {
        var popup = Ext.create('Ext.window.Window', {
            title : 'Enter Parameters',
            layout : 'fit',
            modal : true,
            width : (formPanel.width ? formPanel.width : 500),
            height : (formPanel.height ? formPanel.height : 400),
            items : [formPanel],
            buttons:[{
                xtype: 'button',
                text: 'Apply Template',
                scope : this,
                iconCls : 'add',
                handler: function(button) {
                    var parent = button.findParentByType('window');
                    var panel = parent.getComponent(0);

                    if (panel.getForm().isValid()) {
                        var additionalParams = panel.getForm().getValues(false, false, includeEmptyText, false);
                        
                        //We need to close our window when finished so we wrap callback
                        //with a function that ensures closing BEFORE the callback is executed
                        this._getTemplatedScript(function(status, script) {
                            parent.ignoreCloseEvent = true;
                            parent.close();
                            callback(status, script);
                        }, templateName, additionalParams);
                    }
                }
            }],
            listeners : {
                close : function(popup) {
                    if (!popup.ignoreCloseEvent) {
                        callback(ScriptBuilder.templates.BaseTemplate.TEMPLATE_RESULT_CANCELLED, null);
                    }
                }
            }
        });

        popup.show();
    },

    /**
     * Utility for calling a template function getTemplatedScript.do
     *
     * The keys from getValues and baseTemplateVariables will be used to populate
     * the keys/values list for the template
     *
     * callback(Number status, String script) - called by the template when a script snippet has finished templating.
     * additionalParams - a regular object containing key/value pairs to inject into the specified template
     * templateName - the name of the template to use
     */
    _getTemplatedScript : function(callback, template, additionalParams) {
    	 var params = {};
         for (var k in this.getParameters()) {
             params[k] = this.getParameters()[k];
         }
         for (var k in additionalParams) {
             params[k] = additionalParams[k];
         }

         var f = function(match, p1, offset, string) {
             return params[p1];
         };

         template = template.replace(/\$\{([a-zA-Z0-9_-]+)\}/g, f);

         // If the template config specified a value for 'n-threads',
         // store for later use in selecting an appropriate vm type.
         if (params['n-threads']) {
             this.wizardState.nthreads = params['n-threads'];
         }

         callback(ScriptBuilder.templates.BaseTemplate.TEMPLATE_RESULT_SUCCESS, template);
    },

    /**
     * Function for generating a script snippet (string) representing this components values
     *
     * function(Function callback) - returns void
     *
     * callback(Number status, String script) - called by the template when a script snippet has finished templating.
     */
    requestScript : portal.util.UnimplementedFunction
});

