/**
 * A widget for holding a collection BaseFacetWidgets as children and allowing filter
 * queries to be formulated
 *
 * Fires a change event if any child facets change.
 */
Ext.define('vegl.jobwizard.sssc.FacetedFilterPanel', {
    extend : 'vegl.widgets.search.FacetedSearchPanel',
    alias: 'widget.facetedfilterpanel',

    constructor : function(config) {
        var listeners = {
            scope: this,
            change: this.onChange
        };

        Ext.applyIf(config, {
            items: [{
                xtype: 'textfacet',
                map: config.map,
                listeners: listeners
            },{
                xtype: 'providerfacet',
                listeners: listeners
            }]
        });

        this.callParent(arguments);
    },

    onRegistryChange: function(cswBrowserPanel, newRegistryId) {

    },

    onChange: function(cmp) {
        this.fireEvent('change', this, cmp);
        var facets = this.extractSearchFacets();

    }
});