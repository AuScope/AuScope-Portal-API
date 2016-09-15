/**
 * A Facet for searching on the spatial bounds attribute
 *
 */
Ext.define('vegl.widgets.search.SpatialBoundsFacet', {
    extend : 'vegl.widgets.search.BaseFacetWidget',
    alias: 'widget.spatialboundsfacet',

    currentBbox: null,

    constructor : function(config) {
        Ext.apply(config, {
            title: 'Spatial Bounds',
            items: [{
                xtype: 'textfield',
                itemId: 'spatialtextfield',
                width: '100%',
                emptyText: 'Use the buttons to the right to enter a bounds',
                fieldStyle: {
                    cursor: 'pointer'
                },
                triggers: {
                    visible: {
                        cls: 'eye-icon',
                        width: '24px',
                        scope: this,
                        hideOnReadOnly: false,
                        handler: this._handleVisibleClick
                    },
                    draw: {
                        cls: 'draw-icon',
                        width: '24px',
                        scope: this,
                        hideOnReadOnly: false,
                        handler: this._handleDrawClick
                    }
                },
                listeners: {
                    afterrender: function(tf) {
                        tf.setReadOnlyAttr(true); //Set readonly at a low level so we still keep our triggers working
                        tf.getEl().on('click', this._handleFieldClick, this);
                        tf.getEl().on('dblclick', this._handleFieldDoubleClick, this);
                    },
                    scope: this
                }
            },{
                xtype: 'label',
                itemId: 'helplabel',
                style: {
                    'color': '#888',
                    'font-style': 'italic',
                    'text-align': 'right'
                },
                width: '100%',
                text: 'Draw a box on the map...',
                hidden: true
            }]
        });

        this.callParent(arguments);
    },

    installToolTips: function() {
        Ext.create('Ext.tip.ToolTip', {
            target: this.down('#spatialtextfield').getTrigger('draw').getEl(),
            html: 'Draw a spatial filter on the map'
        });

        Ext.create('Ext.tip.ToolTip', {
            target: this.down('#spatialtextfield').getTrigger('visible').getEl(),
            html: 'Add the current map bounds as a spatial filter'
        });
        this.callParent(arguments);
    },

    _handleFieldDoubleClick: function() {
        if (this.currentBbox) {
            this.map.scrollToBounds(this.currentBbox);
        }
    },

    _handleFieldClick: function() {
        if (this.currentBbox) {
            this.map.highlightBounds(this.currentBbox);
        }
    },

    _handleVisibleClick: function(field, trigger, e) {
        e.stopPropagation();
        this.setBbox(this.map.getVisibleMapBounds());
    },

    _handleDrawFinish: function(olBounds) {
        this.handler.deactivate();
        this.down('#helplabel').setVisible(false);

        if (olBounds instanceof OpenLayers.Bounds) {
            var topLeft = {x: olBounds.left, y: olBounds.top};
            var bottomRight = {x: olBounds.right, y: olBounds.bottom};

            var tl = this.map.map.getLonLatFromPixel(topLeft).transform('EPSG:3857', 'EPSG:4326');
            var br = this.map.map.getLonLatFromPixel(bottomRight).transform('EPSG:3857', 'EPSG:4326');

            this.setBbox(Ext.create('portal.util.BBox', {
                northBoundLatitude: tl.lat,
                southBoundLatitude: br.lat,
                eastBoundLongitude: br.lon,
                westBoundLongitude: tl.lon
            }));
        }
    },

    _handleDrawClick: function(field, trigger, e) {
        e.stopPropagation();

        if (!this.handler) {
            this.handler = new OpenLayers.Handler.Box( this.map, {done: Ext.bind(this._handleDrawFinish, this)}, {} );
        }

        if (this.handler.activate()) {
            this.down('#helplabel').setVisible(true);
            this.down('#helplabel').getEl().setStyle('display', 'block');
        } else {
            this.handler.deactivate();
            this.down('#helplabel').setVisible(false);
        }
    },

    /**
     * See base class
     */
    clearSearch: function() {
        this.setBbox(null);
    },

    setBbox: function(bbox) {
        this.currentBbox = bbox;
        var text = '';
        if (bbox) {
            text = Ext.util.Format.format('{0},{1} to {2},{3}',
                    Ext.util.Format.number(bbox.northBoundLatitude, '0.0000'),
                    Ext.util.Format.number(bbox.westBoundLongitude, '0.0000'),
                    Ext.util.Format.number(bbox.southBoundLatitude, '0.0000'),
                    Ext.util.Format.number(bbox.eastBoundLongitude, '0.0000'));
        }
        this.down('#spatialtextfield').setValue(text);
        this.fireEvent('change', this);
    },

    /**
     * See base class
     */
    extractSearchFacets : function() {
        if (!this.currentBbox) {
            return null;
        }

        return [Ext.create('vegl.models.SearchFacet', {
            field: 'bbox',
            value: Ext.JSON.encode(this.currentBbox.clone()),
            type: vegl.models.SearchFacet.TYPE_BBOX,
            comparison: vegl.models.SearchFacet.CMP_EQUAL
        })];
    }
});
