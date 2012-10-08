/**
 * A panel for housing the components listed at ScriptBuilder.Components
 *
 * Adds the following events
 *
 * {
 *  addcomponent : function(ComponentTreePanel this, String componentClass, String componentName, String componentDescription) - raised whenever a component is requested to be made active
 * }
 */
Ext.define('ScriptBuilder.ComponentTreePanel', {
    extend : 'Ext.tree.Panel',

    alias : 'widget.sbcomponenttreepanel',

    constructor : function(config) {
        Ext.apply(config, {
            rootVisible : false,
            root : ScriptBuilder.Components.getComponents()
        });

        this.callParent(arguments);

        this.addEvents({
            'addcomponent' : true
        });

        this.on('itemdblclick', this._onDblClick, this);
    },

    _onDblClick : function(view, node, el, index, e, eOpts) {
        var componentName = node.get('id');
        var name = node.get('text');
        var description = node.get('qtip');

        if (!Ext.isEmpty(componentName)) {
            this.fireEvent('addcomponent', this, componentName, name, description);
        }
    }
});