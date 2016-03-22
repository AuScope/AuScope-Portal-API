/**
 * A ANVGLUser is the metadata attached to a user within ANVGL.
 */
Ext.define('vegl.models.ANVGLUser', {
    extend: 'Ext.data.Model',

    fields: [
        { name: 'id', type: 'string' }, //Unique ID for this user
        { name: 'fullName', type: 'string' }, //Full name of this user
        { name: 'email', type: 'string' }, //email of this user
        { name: 'arnExecution', type: 'string' }, //Amazon resource name for execution
        { name: 'arnStorage', type: 'string' }, //Amazon resource name for storage
        { name: 'acceptedTermsConditions', type: 'int' }, //The last version of the T&C's the user accepted
        { name: 'awsKeyName', type: 'string' }, //Amazon key name to be used for started VMs (or null)
    ],

    idProperty : 'id'
});
