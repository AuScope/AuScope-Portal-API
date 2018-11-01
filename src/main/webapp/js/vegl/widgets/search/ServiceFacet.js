/**
 * A Facet for searching on the "servicetype" attribute
 *
 */
Ext.define('vegl.widgets.search.ServiceFacet', {
    extend : 'vegl.widgets.search.BaseFacetWidget',
    alias: 'widget.servicefacet',

    constructor : function(config) {
        var cbListeners = {
            change: this._cbChange,
            scope: this
        };
        Ext.apply(config, {
            title: 'Available Services',
            items: [{
                xtype: 'fieldset',
                title: 'Gridded Data Services',
                border: '1 0 0 0',
                items: [{
                    xtype: 'checkboxfield',
                    itemId: 'servicewcs',
                    name: 'WCS',
                    width: '100%',
                    boxLabel: 'Web Coverage Service',
                    listeners: cbListeners
                },{
                    xtype: 'checkboxfield',
                    itemId: 'servicencss',
                    name: 'NCSS',
                    width: '100%',
                    boxLabel: 'NetCDF Subset Service',
                    listeners: cbListeners
                },{
                    xtype: 'checkboxfield',
                    itemId: 'serviceopendap',
                    name: 'OPeNDAP',
                    width: '100%',
                    boxLabel: 'OPeNDAP Service',
                    listeners: cbListeners
                }]
            },{
                xtype: 'fieldset',
                title: 'Point/Line Data Services',
                items: [{
                    xtype: 'checkboxfield',
                    itemId: 'servicewfs',
                    name: 'WFS',
                    width: '100%',
                    boxLabel: 'Web Feature Service',
                    listeners: cbListeners
                }]
            },{
                xtype: 'fieldset',
                title: 'Visualisation Services',
                items: [{
                    xtype: 'checkboxfield',
                    itemId: 'servicewms',
                    name: 'WMS',
                    width: '100%',
                    boxLabel: 'Web Map Service',
                    listeners: cbListeners
                }]
            }]
        });

        this.callParent(arguments);
    },

    _cbChange: function(cb, newVal, oldVal) {
        this.fireEvent('change', this);
    },

    installToolTips: function() {
        this.callParent(arguments);
    },

    /**
     * See base class
     */
    clearSearch: function() {
        Ext.each(this.query('checkbox'), function(cb) {
            cb.suspendEvents(false);
            cb.setValue(false);
            cb.resumeEvents();
        });

        this.fireEvent('change', this);
    },

    /**
     * See base class
     */
    extractSearchFacets : function() {
        var facets = [];

        Ext.each(this.query('checkbox'), function(cb) {
            if (cb.getValue()) {
                facets.push(Ext.create('vegl.models.SearchFacet', {
                    field: 'servicetype',
                    value: cb.getName(),
                    comparison: vegl.models.SearchFacet.CMP_EQUAL,
                    type: vegl.models.SearchFacet.TYPE_SERVICETYPE
                }))
            }
        });

        return facets;
    }
});
