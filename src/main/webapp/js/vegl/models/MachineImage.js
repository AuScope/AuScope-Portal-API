/**
 * A MachineImage is a unique ID of a Cloud Virtual Machine Image along with descriptive metadata
 */
Ext.define('vegl.models.MachineImage', {
    extend: 'Ext.data.Model',

    fields: [
        { name: 'imageId', type: 'string' }, //Unique ID for this image
        { name: 'name', type: 'string' }, //Short title of this image
        { name: 'description', type: 'string' }, //Long description of this image
        { name: 'keywords', type: 'auto'} //An array of strings
    ],

    idProperty : 'imageId'
});