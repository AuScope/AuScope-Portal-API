/**
 * A Facet for searching in a numeric range with defined min/max values
 *
 */
Ext.define('vegl.widgets.search.NumberRangeFacet', {
    extend : 'vegl.widgets.search.BaseFacetWidget',
    alias: 'widget.numberrangefacet',

    /**
     * Adds the following config:
     *
     * fieldName - String - The name of the field to be filtering on.
     * minValue - Number - The minimum value that can be represented
     * maxValue - Number - The maximum value that can be represented
     * values - Number[] - The initial values for this field. Defaults to [minValue, maxValue]
     */
    constructor : function(config) {
        this.minValue = config.minValue;
        this.maxValue = config.maxValue;
        this.fieldName = config.fieldName;
        var values = Ext.isEmpty(config.values) ? [this.minValue, this.maxValue] : config.values;

        Ext.apply(config, {
            layout: {
                type: 'hbox',
                align: 'stretch',
                pack: 'center'
            },
            items: [{
                xtype: 'label',
                itemId: 'fromlabel',
                width: 40,
                text: ''
            },{
                xtype:'multislider',
                itemId: 'numberrangeslider',
                flex: 1,
                increment: 1,
                minValue: config.minValue,
                maxValue: config.maxValue,
                constrainThumbs: true,
                padding: '0 10 0 5',
                values:values,
                listeners: {
                    change: this._onChange,
                    dragend: function() {
                        this.fireEvent('change', this);
                    },
                    scope: this
                }
            },{
                xtype: 'label',
                itemId: 'tolabel',
                width: 40,
                text: ''
            }],
            listeners: {
                afterrender: this._onChange,
                scope: this
            }
        });

        this.callParent(arguments);
    },

    _onChange: function() {
        var value = this.down('#numberrangeslider').getValue();
        this.down('#fromlabel').setText(Ext.util.Format.format('{0} m', value[0]));
        this.down('#tolabel').setText(Ext.util.Format.format('{0} m', value[1]));
    },

    /**
     * See base class
     */
    clearSearch: function() {
        this.down('#numberrangeslider').setValue([this.minValue, this.maxValue]);
        this.fireEvent('change', this);
    },

    /**
     * See base class
     */
    extractSearchFacets : function() {
        var values = this.down('#numberrangeslider').getValues();

        if (values[0] === this.minValue &&
            values[1] === this.maxValue) {
            return null;
        }

        return [Ext.create('vegl.models.SearchFacet', {
            field: this.fieldName,
            value: values[0],
            type: vegl.models.SearchFacet.TYPE_STRING,
            comparison: vegl.models.SearchFacet.CMP_GREATER_THAN
        }),Ext.create('vegl.models.SearchFacet', {
            field: this.fieldName,
            value: values[1],
            type: vegl.models.SearchFacet.TYPE_STRING,
            comparison: vegl.models.SearchFacet.CMP_LESS_THAN
        })];
    }
});
