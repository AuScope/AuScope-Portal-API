/**
 * A FileRecord represents a single input/output file being used by the VL workflow
 */
Ext.define('vegl.models.FileRecord', {
    extend: 'Ext.data.Model',

    fields: [
        { name: 'name', type: 'string' }, //name of the input file
        { name: 'size', type: 'int' }, //Size of the input file in bytes
        { name: 'fileHash', type: 'string' }, //Hash of the file as returned by the cloud
        { name: 'parentPath', type: 'string' } //Parent path for where the input file is located
    ],

    idProperty: 'name',

    /**
     * Returns true if this file matches a known pattern for a "utility" file that the end user
     * will not find of any use
     */
    isVlUtilityFile: function() {
        switch(this.get('name')) {
        case 'vl.end':
        case 'workflow-version.txt':
        case 'vl-download.sh':
            return true;
        default:
            return false;
        }
    }
});
