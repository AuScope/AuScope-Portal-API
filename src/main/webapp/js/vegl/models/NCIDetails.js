/**
 * A ANVGLUser is the metadata attached to a user within ANVGL.
 */
Ext.define('vegl.models.NCIDetails', {
    extend: 'Ext.data.Model',

    fields: [
        { name: 'nciUsername', type: 'string' },
        { name: 'nciKey', type: 'string' },
        { name: 'nciProject', type: 'string' },
    ]
});
