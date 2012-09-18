/**
 * A template for generating a UBC gravity inversion example.
 */
Ext.define('ScriptBuilder.templates.UbcGravityTemplate', {
    extend : 'ScriptBuilder.templates.BaseTemplate',

    description : null,
    name : null,

    constructor : function(config) {
        this.callParent(arguments);
    },

    /**
     * See parent description
     */
    requestScript : function(callback) {
        this._getTemplatedScript(callback, 'ubc-gravity.py');
    }

});

