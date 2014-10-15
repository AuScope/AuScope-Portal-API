/**
 * A FileRecord represents a single input/output file being used by the VL workflow
 */
Ext.define('vegl.models.FileRecord', {
    extend: 'Ext.data.Model',

    fields: [
        { name: 'name', type: 'string' }, //name of the input file
        { name: 'size', type: 'int' }, //Size of the input file in bytes
        { name: 'parentPath', type: 'string' } //Parent path for where the input file is located
    ]
});