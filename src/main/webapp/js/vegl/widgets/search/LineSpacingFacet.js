/**
 * A Facet for searching on the custom "line spacing" details
 *
 */
Ext.define('vegl.widgets.search.LineSpacingFacet', {
    extend : 'vegl.widgets.search.NumberRangeFacet',
    alias: 'widget.linespacingfacet',

    statics: {
        MIN_VALUE: 1,
        MAX_VALUE: 128
    },

    constructor : function(config) {
        Ext.apply(config, {
            title: 'Line Spacing',
            fieldName: 'linespacing',
            minValue: vegl.widgets.search.CellSizeFacet.MIN_VALUE,
            maxValue: vegl.widgets.search.CellSizeFacet.MAX_VALUE
        });

        this.callParent(arguments);
    }
});
