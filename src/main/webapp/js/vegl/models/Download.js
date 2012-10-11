/**
 * A Parameter is a single request that an external URL be downloaded
 * (in the job VM) before the job starts execution.
 */
Ext.define('vegl.models.Download', {
    extend: 'Ext.data.Model',

    fields: [
        { name: 'id', type: 'int' }, //Unique ID for this download
        { name: 'name', type: 'string' }, //short name of this download
        { name: 'description', type: 'string' }, //longer descriptiion of this download
        { name: 'url', type: 'string'}, //The remote URL
        { name: 'localPath', type: 'string'}, //The path (local to job VM) where data will be downloaded
        { name: 'eastBoundLongitude', type: 'double'}, //The spatial location of this download in WGS84
        { name: 'northBoundLatitude', type: 'double'}, //The spatial location of this download in WGS84
        { name: 'southBoundLatitude', type: 'double'}, //The spatial location of this download in WGS84
        { name: 'westBoundLongitude', type: 'double'} //The spatial location of this download in WGS84
    ],

    idProperty : 'id'
});