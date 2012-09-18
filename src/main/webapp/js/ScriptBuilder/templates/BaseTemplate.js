/**
 * The base template that all other ScriptBuilder templates should inherit from.
 *
 * A script builder template utilitiy for setting variables. These variables
 * will be coupled with a script template in order to generate an actual text file.
 */
Ext.define('ScriptBuilder.templates.BaseTemplate', {
    extend : 'portal.util.ObservableMap',

    description : null,
    name : null,

    constructor : function(config) {
        this.description = config.description ? config.description : '';
        this.name = config.name ? config.name : '';

        this.callParent(arguments);
    },

    /**
     * Utility for calling a template function getTemplatedScript.do
     *
     * The keys from getValues and baseTemplateVariables will be used to populate
     * the keys/values list for the template
     *
     * callback(Boolean success, String script) - called by the template when a script snippet has finished templating.
     * additionalParams - a regular object containing key/value pairs to inject into the specified template
     * templateName - the name of the template to use
     */
    _getTemplatedScript : function(callback, templateName, additionalParams) {
        //Convert our keys/values into a form the controller can read
        var keys = [];
        var values = [];
        //Utility function
        var denormaliseKvp = function(keyList, valueList, kvpObj) {
            if (kvpObj) {
                for (key in kvpObj) {
                    keyList.push(key);
                    valueList.push(kvpObj[key]);
                }
            }
        };

        denormaliseKvp(keys, values, this.getParameters());
        denormaliseKvp(keys, values, additionalParams);

        Ext.Ajax.request({
            url : 'getTemplatedScript.do',
            params : {
                templateName : templateName,
                key : keys,
                value : values
            },
            callback : function(options, success, response) {
                if (!success) {
                    callback(false, null);
                    return;
                }

                var responseObj = Ext.JSON.decode(response.responseText);
                if (!responseObj || !responseObj.success) {
                    callback(false, null);
                    return;
                }

                callback(true, responseObj.data);
            }
        });
    },

    /**
     * Function for generating a script snippet (string) representing this components values
     *
     * function(Function callback) - returns void
     *
     * callback(Boolean success, String script) - called by the template when a script snippet has finished templating.
     */
    requestScript : portal.util.UnimplementedFunction
});

