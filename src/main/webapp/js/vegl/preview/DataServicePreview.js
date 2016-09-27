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
            border: false,
            bodyPadding: '10',
            layout: {
                type: 'vbox',
                align: 'stretch',
                pack: 'start'
            },
            items: [{
                xtype: 'displayfield',
                itemId: 'title',
                margin: '0 0 10 0',
                hideLabel: true,
                fieldStyle: {
                    'font-size': '16px',
                    'text-align': 'left'
                }
            },{
                xtype: 'panel',
                itemId: 'map-container',
                title: 'Request Spatial Bounds',
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

        //To be consistent with other preview implementations
        this.job = job;
        this.fileName = download;
        this.size = null;
        this.hash = null;

        var bounds = new OpenLayers.Bounds(download.get('westBoundLongitude'), download.get('southBoundLatitude'), download.get('eastBoundLongitude'), download.get('northBoundLatitude'));
        var box = new OpenLayers.Feature.Vector(bounds.toGeometry());
        this.boxesLayer.removeAllFeatures();
        this.boxesLayer.addFeatures(box);
        this.map.zoomToExtent(bounds, true);
        this.map.zoomOut();

        var hostNameMatches = /.*:\/\/(.*?)\//g.exec(download.get('url'));
        var hostName = (hostNameMatches && hostNameMatches.length >= 2) ? hostNameMatches[1] : download.get('url');
        this.down('#title').setValue(Ext.util.Format.format('Remote service call to <b><a href="{1}" target="_blank">{0}</a></b>', hostName, download.get('url')));
    },

    isRefreshRequired: function(callback) {
        callback(false);
    }


});