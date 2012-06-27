/**
 * An ActiveComponentModel is a component that has been added
 */
Ext.define('ScriptBuilder.ActiveComponentModel', {
    extend: 'Ext.data.Model',

    fields: [
        { name: 'componentClass', type: 'string'}, //The class name of the component to use as an editor
        { name: 'component', type: 'auto'}, //An instance of ScriptBuilder.components.BaseComponent
        { name: 'text', type: 'string'}, //Descriptive name
        { name: 'qtip', type: 'string'}, //Long description
        { name: 'leaf', type: 'boolean'}
    ]
});