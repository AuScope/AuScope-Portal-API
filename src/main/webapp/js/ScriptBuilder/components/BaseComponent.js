/**
 * The base component that all other ScriptBuilder components should inherit from.
 *
 * A script builder component is a GUI widget coupled with logic to generate a script
 */
Ext.define('ScriptBuilder.components.BaseComponent', {
    extend : 'Ext.form.Panel',

    description : null,
    name : null,

    constructor : function(config) {
        this.description = config.description ? config.description : '';
        this.name = config.name ? config.name : '';

        this.callParent(arguments);
    },

    /**
     * Function for validating the currently entered values
     */
    validateValues : function() {
        return this.getForm().isValid();
    },

    /**
     * Function for returning a javascript object representing the state or set of values of this component
     */
    getValues : function() {
        return this.getForm().getValues();
    },

    /**
     * Function for restoring the state of the base component from a simple object
     */
    setValues : function(values) {
        this.getForm().setValues(values);
    },

    /**
     * Function for generating a script snippet (string) representing this components values
     *
     * function() - returns String
     */
    getScript : portal.util.UnimplementedFunction

});

