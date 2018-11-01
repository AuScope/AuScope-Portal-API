/**
 * A Facet for searching on the "date" attribute
 *
 */
Ext.define('vegl.widgets.search.DateFacet', {
    extend : 'vegl.widgets.search.BaseFacetWidget',
    alias: 'widget.datefacet',

    constructor : function(config) {
        Ext.apply(config, {
            title: 'Publication Date',
            items: [{
                xtype: 'datefield',
                itemId: 'fromfield',
                format: 'd-m-Y',
                fieldLabel: 'From',
                width: '100%',
                listeners: {
                    change: function() {
                        this.fireEvent('change', this);
                    },
                    scope: this
                }
            },{
                xtype: 'datefield',
                itemId: 'tofield',
                format: 'd-m-Y',
                fieldLabel: 'To',
                width: '100%',
                listeners: {
                    change: function() {
                        this.fireEvent('change', this);
                    },
                    scope: this
                }
            }]
        });

        this.callParent(arguments);
    },

    /**
     * See base class
     */
    clearSearch: function() {
        this.down('#fromfield').setValue('');
        this.down('#tofield').setValue('');
    },

    /**
     * See base class
     */
    extractSearchFacets : function() {
        var from = this.down('#fromfield').getValue();
        var to = this.down('#tofield').getValue();
        if (Ext.isEmpty(from) || Ext.isEmpty(to)) {
            return null;
        }

        return [Ext.create('vegl.models.SearchFacet', {
            field: 'datefrom',
            value: from.getTime(),
            type: vegl.models.SearchFacet.TYPE_DATE,
            comparison: vegl.models.SearchFacet.CMP_GREATER_THAN
        }),Ext.create('vegl.models.SearchFacet', {
            field: 'dateto',
            value: to.getTime(),
            type: vegl.models.SearchFacet.TYPE_DATE,
            comparison: vegl.models.SearchFacet.CMP_LESS_THAN
        })];
    }
});
