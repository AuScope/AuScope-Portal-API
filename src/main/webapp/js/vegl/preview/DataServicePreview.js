/**
 * A Ext.panel.Panel specialisation for rendering a quick overview of a data service download
 *
 */
Ext.define('vegl.preview.DataServicePreview', {
    extend : 'Ext.panel.Panel',
    alias : 'widget.dataservicepreview',

    mixins: {
        preview: 'vegl.preview.FilePreviewMixin'
    },

    constructor : function(config) {
        this.mapContainerId = Ext.id(undefined, 'dataservicepreview-');

        Ext.apply(config, {
            autoScroll: true,
            layout: {
                type: 'vbox',
                align: 'stretch',
                pack: 'start'
            },
            items: [{
                xtype: 'label',
                itemId: 'title'
            },{
                xtype: 'textfield',
                itemId: 'url',
                fieldLabel: 'Full URL',
                readOnly: true
            },{
                xtype: 'panel',
                itemId: 'map-container',
                title: 'Spatial Bounds',
                flex: 1,
                html: '<div id="' + this.mapContainerId + '" class="smallmap" style="width:100%; height:100%"></div>'
            }]
        });

        this.callParent(arguments);
    },

    _renderMap: function() {
        this.map = new OpenLayers.Map(this.mapContainerId);
        var ol_wms = new OpenLayers.Layer.WMS( "OpenLayers WMS", "http://vmap0.tiles.osgeo.org/wms/vmap0?", {layers: 'basic'} );
        this.boxesLayer = new OpenLayers.Layer.Vector( "Boxes" );
        this.map.addLayers([ol_wms, this.boxesLayer]);

        this.on('resize', function() {
            this.map.updateSize();
        }, this);
        this.on('boxready', function() {
            this.map.updateSize();
        }, this);

        this.mapRendered = true;
    },

    /**
     * Reloads this store with all the jobs for the specified series
     */
    preview : function(job, download) {
        if (!this.mapRendered) {
            this._renderMap();
        }

        var bounds = new OpenLayers.Bounds(download.get('westBoundLongitude'), download.get('southBoundLatitude'), download.get('eastBoundLongitude'), download.get('northBoundLatitude'));
        var box = new OpenLayers.Feature.Vector(bounds.toGeometry());
        this.boxesLayer.removeAllFeatures();
        this.boxesLayer.addFeatures(box);
        this.map.zoomToExtent(bounds, true);
        this.map.zoomOut();

        this.down('#url').setValue(download.get('url'));

        var hostNameMatches = /.*:\/\/(.*?)\//g.exec(download.get('url'));
        var hostName = (hostNameMatches && hostNameMatches.length >= 2) ? hostNameMatches[1] : download.get('url');
        this.down('#title').setText(Ext.util.Format.format('Service call to {0}', hostName));
    },

    isRefreshRequired: function(callback) {
        callback(false);
    }


});