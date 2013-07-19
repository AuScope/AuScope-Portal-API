/**
 * A class for creating instances of portal.layer.querier.Querier
 */
Ext.define('vegl.layer.VeglQuerierFactory', {

    extend : 'portal.layer.querier.QuerierFactory',


    /**
     * Creates a new instance of a Querier based on the specified values
     *
     * knownLayer can be null
     */
    _generateQuerier : function(knownLayer, wfsResources, wmsResources, wcsResources) {
        var cfg = {map : this.map};

        //Geodesy features don't allow gml:Id lookups </rant>
        //To workaround this we have a custom feature source that looks up via the gps site id.
        if (knownLayer && knownLayer.get('id') === 'geodesy:gnssstation') {
            cfg.featureSource = Ext.create('portal.layer.querier.wfs.featuresources.WFSFeatureByPropertySource', {
                property : 'GPSSITEID'
            });
        }

        if (knownLayer) {
            if (knownLayer.get('id') === 'gocad-models') {
                return Ext.create('vegl.layer.querier.csw.SelectableCSWQuerier', cfg);
            } else if (knownLayer.get('id') === 'geophysics-datasets') {
                return Ext.create('vegl.layer.querier.csw.CSWQuerier', cfg);
            }
        }

        if (wfsResources.length > 0 && wmsResources.length > 0) {
            cfg.parser = Ext.create('vegl.layer.querier.wfs.VeglParser', {});
            cfg.knownLayerParser = Ext.create('vegl.layer.querier.wfs.VeglKnownLayerParser', {});
            return Ext.create('portal.layer.querier.wfs.WFSWithMapQuerier', cfg);
        } else if (wfsResources.length > 0) {
            cfg.parser = Ext.create('vegl.layer.querier.wfs.VeglParser', {});
            cfg.knownLayerParser = Ext.create('vegl.layer.querier.wfs.VeglKnownLayerParser', {});
            return Ext.create('portal.layer.querier.wfs.WFSQuerier', cfg);
        } else if (wcsResources.length > 0) {
            return Ext.create('portal.layer.querier.coverage.WCSQuerier',cfg);
        } else if (wmsResources.length > 0) {
            //Or just the plain old WMS querier
            return Ext.create('portal.layer.querier.wms.WMSQuerier', cfg);
        } else {
            //Worst case scenario, we render the source CSW record
            return Ext.create('portal.layer.querier.csw.CSWQuerier', cfg);
        }
    },

    /**
     * See parent class for defn
     */
    buildFromKnownLayer : function(knownLayer) {
        var allOnlineResources = knownLayer.getAllOnlineResources();

        var wmsResources = portal.csw.OnlineResource.getFilteredFromArray(allOnlineResources, portal.csw.OnlineResource.WMS);
        var wfsResources = portal.csw.OnlineResource.getFilteredFromArray(allOnlineResources, portal.csw.OnlineResource.WFS);
        var wcsResources = portal.csw.OnlineResource.getFilteredFromArray(allOnlineResources, portal.csw.OnlineResource.WCS);

        return this._generateQuerier(knownLayer, wfsResources, wmsResources, wcsResources);
    },

    /**
     * See parent class for defn
     */
    buildFromCswRecord : function(cswRecord) {
        var allOnlineResources = cswRecord.get('onlineResources');

        var wmsResources = portal.csw.OnlineResource.getFilteredFromArray(allOnlineResources, portal.csw.OnlineResource.WMS);
        var wfsResources = portal.csw.OnlineResource.getFilteredFromArray(allOnlineResources, portal.csw.OnlineResource.WFS);
        var wcsResources = portal.csw.OnlineResource.getFilteredFromArray(allOnlineResources, portal.csw.OnlineResource.WCS);

        return this._generateQuerier(null, wfsResources, wmsResources, wcsResources);
    }
});