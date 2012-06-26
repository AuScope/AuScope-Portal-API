/**
 * A series represents a collection of Jobs
 */
Ext.define('vegl.models.Series', {
    extend: 'Ext.data.Model',

    fields: [
        { name: 'id', type: 'int' }, //Unique identifier for the series
        { name: 'name', type: 'string' }, //Descriptive name of the series
        { name: 'description', type: 'string' }, //Long description of the series
        { name: 'user', type: 'string'} //Username who created this series
    ]
});