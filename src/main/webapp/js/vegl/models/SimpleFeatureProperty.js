/**
 * A Simple representation of a single property in a simple feature level 0 feature type.
 */
Ext.define('vegl.models.SimpleFeatureProperty', {
    extend: 'Ext.data.Model',

    fields: [
        { name: 'maxOccurs', type: 'int' }, 
        { name: 'minOccurs', type: 'int' }, 
        { name: 'name', type: 'string' },
        { name: 'typeName', type: 'string' },
        { name: 'index', type: 'int' },
        { name: 'nillable', type: 'boolean' },
        { name: 'indexString', type: 'string', convert : function(value, record){
            return Ext.util.Format.format('Column {0}', record.get('index'));
        }},
        { name: 'displayString', type: 'string', convert : function(value, record){
            return Ext.util.Format.format('{1} - Column {0}', record.get('index'), record.get('name'));
        }}
    ]
});