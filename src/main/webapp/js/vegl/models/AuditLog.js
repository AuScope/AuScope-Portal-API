/**
 * A FileRecord represents a single input/output file being used by the VL workflow
 */
Ext.define('vegl.models.AuditLog', {
    extend: 'Ext.data.Model',

    fields: [
        { name: 'id', type: 'int' },
        { name: 'jobId', type: 'int' },
        { name: 'jobId', type: 'int' },
        { name: 'fromStatus', type: 'string' },
        { name: 'toStatus', type: 'string' },
        { name: 'message', type: 'string' },
        { name: 'transitionDate', type: 'date', convert: function(value, record) {
            if (!value) {
                return null;
            } else {
                return new Date(value);
            }
        }}
    ]
});
