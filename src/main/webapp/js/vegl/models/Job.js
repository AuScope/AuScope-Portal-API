/**
 * A job is a collection of all input/processing/output information describing a single unit of work
 * within the VEGL workflow
 */
Ext.define('vegl.models.Job', {
    extend: 'Ext.data.Model',

    statics : {
        STATUS_FAILED : "Failed",
        STATUS_ACTIVE : "Active",
        STATUS_DONE : "Done",
        STATUS_CANCELLED : "Cancelled",
        STATUS_UNSUBMITTED : "Unsubmitted",
    },

    fields: [
        { name: 'id', type: 'int' }, //Unique identifier for the series
        { name: 'name', type: 'string' }, //Descriptive name of the series
        { name: 'description', type: 'string' }, //Long description of the series
        { name: 'emailAddress', type: 'string'}, //Email who created this series
        { name: 'user', type: 'string'}, //Username who created this series
        { name: 'submitDate', type: 'date', convert: function(value, record) {
            //var d = new Date(year, month, day, hours, minutes, seconds, milliseconds);
            //var d = new Date(milliseconds);
            if (!value) {
                return null;
            } else {
                return new Date(value.time);
            }
        }}, //When this job was submitted to the cloud
        { name: 'status', type: 'string'}, //The status of the job

        { name: 'computeVmId', type: 'string'}, //the ID of the VM that will be used to run this job
        { name: 'computeInstanceId', type: 'string'}, //the ID of the VM instance that is running this job (will be null if no job is currently running)
        { name: 'computeInstanceType', type: 'string'}, //The type of the compute instance to start (size of memory, number of CPUs etc) - eg m1.large. Can be null
        { name: 'computeInstanceKey', type: 'string'}, //The name of the key to inject into the instance at startup for root access. Can be null

        { name: 'storageProvider', type: 'string'}, //A unique identifier identifying the type of storage API used to store this job's files
        { name: 'storageEndpoint', type: 'string'}, //The endpoint for the cloud storage service
        { name: 'storageBucket', type: 'string'}, //The 'bucket' name where input/output files will be staged for this job
        { name: 'storageBaseKey', type: 'string'}, //The key prefix for all files associated with this job in the specified storage bucket
        { name: 'storageAccessKey', type: 'string'}, //The access key (user name) for writing to storage
        { name: 'storageSecretKey', type: 'string'}, //the secret key (password) for writing to storage

        { name: 'registeredUrl', type: 'string'},
        { name: 'seriesId', type: 'int'},
        { name: 'vmSubsetFilePath', type: 'string'},
        { name: 'vmSubsetUrl', type: 'string'},
        { name: 'paddingMinEasting', type: 'double'},
        { name: 'paddingMaxEasting', type: 'double'},
        { name: 'paddingMinNorthing', type: 'double'},
        { name: 'paddingMaxNorthing', type: 'double'},
        { name: 'selectionMinEasting', type: 'double'},
        { name: 'selectionMaxEasting', type: 'double'},
        { name: 'selectionMinNorthing', type: 'double'},
        { name: 'selectionMaxNorthing', type: 'double'},
        { name: 'mgaZone', type: 'string'},
        { name: 'cellX', type: 'int'},
        { name: 'cellY', type: 'int'},
        { name: 'cellZ', type: 'int'},
        { name: 'inversionDepth', type: 'int'}
    ]
});