/**
 * A ComputeType is a unique ID of a Cloud Virtual Machine Compute Type/Flavor along with descriptive metadata
 */
Ext.define('vegl.models.ComputeType', {
    extend: 'Ext.data.Model',

    fields: [
        { name: 'id', type: 'string' }, //Unique ID for this image
        { name: 'description', type: 'string' }, //Long description of this image
        { name: 'vcpus', type: 'string' }, //How many virtual CPUs
        { name: 'ramMB', type: 'string' }, //How much RAM (roughly) in MB does this  compute type offer
        { name: 'rootDiskGB', type: 'string' }, //How much does the root disk of this compute type offer (in GB)
        { name: 'ephemeralDiskGB', type: 'string' } //How much does the Ephemeral disk of this compute type offer (in GB)
    ],

    idProperty : 'id'
});