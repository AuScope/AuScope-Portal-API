/**
 * A Facet for searching on the custom "cell size" details
 *
 */
Ext.define('vegl.widgets.search.CellSizeFacet', {
    extend : 'vegl.widgets.search.NumberRangeFacet',
    alias: 'widget.cellsizefacet',

    statics: {
        MIN_VALUE: 1,
        MAX_VALUE: 128
    },

    constructor : function(config) {
        Ext.apply(config, {
            title: 'Cell Size',
            fieldName: 'cellsize',
            minValue: vegl.widgets.search.CellSizeFacet.MIN_VALUE,
            maxValue: vegl.widgets.search.CellSizeFacet.MAX_VALUE
        });

        this.callParent(arguments);
    }
});
