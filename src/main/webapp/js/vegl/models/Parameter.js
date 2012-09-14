/**
 * A Parameter is a typed key value pair associated with a job
 */
Ext.define('vegl.models.Parameter', {
    extend: 'Ext.data.Model',

    statics: {
	    TYPE_STRING : 'string',
	    TYPE_NUMBER : 'number'
    },

    fields: [
        { name: 'id', type: 'int' }, //Unique ID for this parameter
        { name: 'name', type: 'string' }, //Key for this parameter
        { name: 'type', type: 'string' }, //type of this paramter either [TYPE_NUMBER, TYPE_STRING]
        { name: 'value', type: 'string'} //The value of this field (always a string)
    ],

    idProperty : 'id'
});