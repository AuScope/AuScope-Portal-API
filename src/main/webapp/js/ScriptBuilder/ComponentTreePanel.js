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
    extend	: 'Ext.tree.Panel',
    alias	: 'widget.sbcomponenttreepanel',

    constructor : function(config) {
        Ext.apply(config, {
            rootVisible : false
        });

        this.callParent(arguments);
        this.on('itemdblclick', this._onDblClick, this);
    },

    _onDblClick : function(view, node, el, index, e, eOpts) {
        var componentName = node.get('id');

        var name = node.get('text');
        var description = node.get('qtip');
        
        if (!Ext.isEmpty(componentName)) {
        	Ext.Ajax.request({
                url: 'getSolution.do',
                scope: this,
                headers: {
                    Accept: 'application/json'
                },
                params: {
                    solutionId: componentName
                },
                success: function(response) {
                    entry = Ext.JSON.decode(response.responseText);
                    if (entry && entry.data) {
                        this.fireEvent('addcomponent', this, entry.data[0], name, description);
                        this.addEvents({'addcomponent' : true});
                    }
                },

                failure: function(response) {
                    console.log("Get detailed entry failed! " + response);
                }
            });
        }
    }
});