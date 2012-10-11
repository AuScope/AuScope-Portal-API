/**
 * A template for generating a UBC gravity inversion example.
 */
Ext.define('ScriptBuilder.templates.UbcMagneticTemplate', {
    extend : 'ScriptBuilder.templates.UbcGravityTemplate',

    description : null,
    name : null,

    constructor : function(config) {
        this.callParent(arguments);
    },

    getScriptName : function() {
        return 'ubc-magnetic.py';
    }
});

