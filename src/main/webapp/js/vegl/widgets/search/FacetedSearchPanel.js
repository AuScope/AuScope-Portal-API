/**
 * A widget for holding a collection BaseFacetWidgets as children and allowing search
 * queries to be formulated
 *
 * Fires a change event if any child facets change.
 *
 * Also includes a facetedcswbrowserpanel for displaying results
 */
Ext.define('vegl.widgets.search.FacetedSearchPanel', {
    extend : 'Ext.panel.Panel',
    alias: 'widget.facetedsearchpanel',

    constructor : function(config) {
        var listeners = {
            scope: this,
            change: this.onChange
        };

        Ext.applyIf(config, {
            plugins: ['collapsedaccordian'],
            autoScroll: true,
            layout: {
                type: 'accordion',
                hideCollapseTool: true,
                collapseFirst: true,
                fill: false,
                multi: true
            },
            items: [{
                xtype: 'anytextfacet',
                map: config.map,
                listeners: listeners
            },{
                xtype: 'spatialboundsfacet',
                map: config.map,
                collapsed: true,
                listeners: listeners
            },{
                xtype: 'keywordfacet',
                map: config.map,
                collapsed: true,
                listeners: listeners
            },{
                xtype: 'servicefacet',
                map: config.map,
                collapsed: true,
                listeners: listeners
            },{
                xtype: 'datefacet',
                map: config.map,
                collapsed: true,
                listeners: listeners
            },{
                xtype: 'cellsizefacet',
                map: config.map,
                collapsed: true,
                listeners: listeners
            },{
                xtype: 'linespacingfacet',
                map: config.map,
                collapsed: true,
                listeners: listeners
            },{
                xtype: 'facetedcswbrowserpanel',
                collapsed: true,
                map: config.map,
                title: 'Search Results',
                layerFactory: config.layerFactory,
                height: 380,
                listeners: {
                    registrychange: this.onRegistryChange,
                    scope: this
                }
            }]
        });

        this.callParent(arguments);
    },

    onRegistryChange: function(cswBrowserPanel, newRegistryId) {
        this.items.each(function(cmp) {
            if (cmp instanceof vegl.widgets.search.BaseFacetWidget) {
                if (cmp.onRegistryChange) {
                    cmp.onRegistryChange(cmp, newRegistryId);
                }
            }
        });
    },

    onChange: function(cmp) {
        this.fireEvent('change', this, cmp);
        var facets = this.extractSearchFacets();

        var browser = this.down('facetedcswbrowserpanel');
        browser.expand();
        browser.updateResults(facets);
    },

    /**
     * function()
     *
     * returns - vegl.models.SearchFacet[] - An array of search facets which when AND'ed together represent all child facet contributions
     * to the search
     */
    extractSearchFacets : function() {
        var facets = [];

        this.items.each(function(cmp) {
            if (cmp instanceof vegl.widgets.search.BaseFacetWidget) {
                var facetArr = cmp.extractSearchFacets();
                if (!Ext.isEmpty(facetArr)) {
                    facets = facets.concat(facetArr);
                }
            }
        });

        return facets;
    }
});