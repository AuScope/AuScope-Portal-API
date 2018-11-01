/**
 * A Facet for searching on the "anytext" attribute
 *
 */
Ext.define('vegl.widgets.search.AnyTextFacet', {
    extend : 'vegl.widgets.search.BaseFacetWidget',
    alias: 'widget.anytextfacet',

    constructor : function(config) {
        Ext.apply(config, {
            title: 'Any Text',
            items: [{
                xtype: 'textfield',
                itemId: 'anytextfield',
                width: '100%',
                emptyText: 'eg. Geophysics',
                triggers: {
                    visible: {
                        cls: 'filter-icon',
                        width: '24px',
                        scope: this,
                        hideOnReadOnly: false,
                        handler: function() {
                            this.fireEvent('change', this);
                        }
                    }
                },
                listeners: {
                    specialkey: function(field, e) {
                        if (e.getKey() == e.ENTER) {
                            this.fireEvent('change', this);
                        }
                    },
                    scope: this
                }
            }]
        });

        this.callParent(arguments);
    },

    installToolTips: function() {
        Ext.create('Ext.tip.ToolTip', {
            target: this.down('#anytextfield').getEl(),
            html: 'Apply text filter'
        });
        this.callParent(arguments);
    },

    /**
     * See base class
     */
    clearSearch: function() {
        this.down('#anytextfield').setValue('');
        this.fireEvent('change', this);
    },

    /**
     * See base class
     */
    extractSearchFacets : function() {
        var val = this.down('#anytextfield').getValue();
        if (Ext.isEmpty(val)) {
            return null;
        }

        return [Ext.create('vegl.models.SearchFacet', {
            field: 'anytext',
            value: val,
            comparison: vegl.models.SearchFacet.CMP_EQUAL,
            type: vegl.models.SearchFacet.TYPE_STRING
        })];
    }
});
