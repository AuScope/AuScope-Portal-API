/**
 * An extension to portal.widgets.panel.CSWRecordPanel that adds
 * VL specific metadata+other additions.
 */
Ext.define('vegl.widgets.search.VLCSWRecordPanel', {
    extend : 'portal.widgets.panel.CSWRecordPanel',
    alias: 'widget.vlcswrecordpanel',

    constructor: function(config) {
        var me = this;

        Ext.apply(config, {
            titleIndex: 1,
            tools: [{
                field: 'layer',
                clickHandler: Ext.bind(me.addClickHandler, me),
                stopEvent: true,
                tipRenderer: function(value, layer, tip) {
                    if(value) {
                        return 'Click to remove layer from map';
                    } else {
                        return 'Click to add this layer to the map';
                    }
                },
                iconRenderer: me.addRenderer
            },{
                field: 'serviceInformation',
                stopEvent: true,
                clickHandler: Ext.bind(me._serviceInformationClickHandler, me),
                tipRenderer: function(layer, tip) {
                    return 'Click for detailed information about the web services this layer utilises.';
                },
                iconRenderer: Ext.bind(me._serviceInformationRenderer, me)
            },{
                field: 'spatialBoundsRenderer',
                stopEvent: true,
                clickHandler: Ext.bind(me._spatialBoundsClickHandler, me),
                doubleClickHandler: Ext.bind(me._spatialBoundsDoubleClickHandler, me),
                tipRenderer: function(layer, tip) {
                    return 'Click to see the bounds of this layer, double click to pan the map to those bounds';
                },
                iconRenderer: Ext.bind(me._spatialBoundsRenderer, me)
            }],
            childPanelGenerator: null
        });
        this.callParent(arguments);
    },

    addRenderer: function(layer, record) {
        if (layer) {
            return 'portal-core/img/trash.png';
        } else {
            return 'portal-core/img/add.gif';
        }
    },

    addClickHandler: function(value, record) {
        if (value) {
            ActiveLayerManager.removeLayer(record.get('layer'));
            record.set('layer', null);
        } else {
            var layer = record.get('layer');
            if (!layer) {
                layer = this.layerFactory.generateLayerFromCSWRecord(record);
                record.set('layer', layer);
            }

            var filterer = layer.get('filterer');
            var filterForm = layer.get('filterForm')

            //Before applying filter, update the spatial bounds (silently)
            try {
                filterer.setSpatialParam(this.map.getVisibleMapBounds(), true);
                filterForm.writeToFilterer(filterer);
            } catch (e) {
                console.log(e);
            }

            ActiveLayerManager.addLayer(layer);
        }
    },

    _serviceInformationRenderer : function(value, record) {
        var onlineResources = this.getOnlineResourcesForRecord(record);
        var serviceType = this._getServiceType(onlineResources);
        var containsDataService = serviceType.containsDataService;

        // default iconPath where there is no service info available
        var iconPath = 'portal-core/img/information.png';

        if (containsDataService) {
            iconPath = 'portal-core/img/binary.png'; //a single data service will label the entire layer as a data layer
        }

        return iconPath;
    },

    /**
     * Helper function.  Useful to define here for subclasses.
     *
     * Show a popup containing info about the services that 'power' this layer
     */
    _serviceInformationClickHandler : function(value, record) {
        var cswRecords = this.getCSWRecordsForRecord(record);
        if (!cswRecords || cswRecords.length === 0) {
            return;
        }

        Ext.create('Ext.window.Window', {
            title : 'CSW Record Information',
            maxHeight: Math.max(100, window.innerHeight - 50),
            autoScroll: true,
            items : [{
                xtype : 'cswmetadatapanel',
                width : 500,
                border : false,
                cswRecord : cswRecords[0]
            }]
        }).show();
    }
});
