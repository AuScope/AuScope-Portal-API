/**
 * A Facet for searching on the SSSC based on toolbox provider ids
 *
 */
Ext.define('vegl.jobwizard.sssc.ProviderFacet', {
    extend : 'vegl.widgets.search.BaseFacetWidget',
    alias: 'widget.providerfacet',

    constructor : function(config) {
        this.computeServicesStore = Ext.create('Ext.data.Store', {
            fields : [{name: 'providerId', type: 'string'},
                      {name: 'name', type: 'string'}],
            proxy: {
                type: 'ajax',
                url: 'secure/getConfiguredComputeServices.do',
                reader: {
                   type: 'json',
                   rootProperty : 'data'
                }
            },
            autoLoad : true,
            listeners: {
                scope: this,
                load: function() {
                    var cmb = this.down('#providercombo');
                    if (cmb) {
                        cmb.setValue('');
                    }
                }
            }
        });

        Ext.apply(config, {
            title: 'Compute Provider',
            items: [{
                xtype: 'combo',
                itemId: 'providercombo',
                width: '100%',
                allowBlank: false,
                queryMode: 'local',
                displayField: 'name',
                valueField : 'providerId',
                typeAhead: true,
                forceSelection: true,
                store: this.computeServicesStore,
                listeners: {
                    scope: this,
                    select: function() {
                        this.fireEvent('change', this);
                    },
                    load: function() {
                        this.setValue('');
                    }
                }
            }]
        });

        this.callParent(arguments);
    },

    installToolTips: function() {
        Ext.create('Ext.tip.ToolTip', {
            target: this.down('#providercombo').getEl(),
            html: 'Filter the solutions by the specified compute providers'
        });
        this.callParent(arguments);
    },

    /**
     * See base class
     */
    clearSearch: function() {
        this.down('#providercombo').setValue('');
        this.fireEvent('change', this);
    },

    /**
     * See base class
     */
    extractSearchFacets : function() {
        var val = this.down('#providercombo').getValue();
        return [Ext.create('vegl.models.SearchFacet', {
            field: 'provider',
            value: val,
            comparison: vegl.models.SearchFacet.CMP_EQUAL,
            type: vegl.models.SearchFacet.TYPE_STRING
        })];
    }
});
