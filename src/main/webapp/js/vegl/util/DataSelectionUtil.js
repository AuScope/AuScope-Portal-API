/**
 * Utility functions for selecting data and saving it into a user session
 */
Ext.define('vegl.util.DataSelectionUtil', {
    singleton: true
}, function() {
    
    /**
     * Given an OnlineResource, work out whether the type of that resource is supported
     * for data selection.
     * 
     * @param or portal.csw.OnlineResource The resource to query
     */
    vegl.util.DataSelectionUtil.isResourceSupported = function(or) {
        switch(or.get('type')) {
        case portal.csw.OnlineResource.WCS:            
        case portal.csw.OnlineResource.WFS:
        case portal.csw.OnlineResource.WWW:
            return true;
            
        default:
            return false;
        }
    };
    
    /**
     * Creates a default download options object based on an online resource and CSWrecord.
     * 
     * The object itself is used to maintain the state of dataset download options specific to
     * the type of the online resource. Its use is only intended within this class 
     */
    vegl.util.DataSelectionUtil.createDownloadOptionsForResource = function(or, cswRecord, defaultBbox) {
        var dsBounds = cswRecord.get('geographicElements').length ? cswRecord.get('geographicElements')[0] : null; 
        
        //Set the defaults of our new item
        var downloadOptions = {
            name : 'Subset of ' + or.get('name'),
            description : or.get('description'),
            url : or.get('url'),
            localPath : '/tmp/' + or.get('name'),
            crs : (defaultBbox ? defaultBbox.crs : ''),
            eastBoundLongitude : (defaultBbox ? defaultBbox.eastBoundLongitude : 0),
            northBoundLatitude : (defaultBbox ? defaultBbox.northBoundLatitude : 0),
            southBoundLatitude : (defaultBbox ? defaultBbox.southBoundLatitude : 0),
            westBoundLongitude : (defaultBbox ? defaultBbox.westBoundLongitude : 0),
            dsEastBoundLongitude : (dsBounds ? dsBounds.eastBoundLongitude : null),
            dsNorthBoundLatitude : (dsBounds ? dsBounds.northBoundLatitude : null),
            dsSouthBoundLatitude : (dsBounds ? dsBounds.southBoundLatitude : null),
            dsWestBoundLongitude : (dsBounds ? dsBounds.westBoundLongitude : null)
        };

        //Add/subtract info based on resource type
        switch(or.get('type')) {
        case portal.csw.OnlineResource.WCS:            
            delete downloadOptions.url;
            downloadOptions.format = 'nc';
            downloadOptions.layerName = or.get('name');
            break;
        case portal.csw.OnlineResource.WFS:
            
            delete downloadOptions.url;
            downloadOptions.serviceUrl = or.get('url');
            downloadOptions.featureType = or.get('name');
            break;
        case portal.csw.OnlineResource.WWW:
            break;
            
        //We don't support EVERY type
        default:
            break;
        }
        
        return downloadOptions;
    };
    
    /**
     * Creates and shows a GUI widget for editing the download options specific to the type of or selected.
     * 
     * The results of the editing (if successful) will be written to a generic object which can be used
     * in conjuction with other methods in this utility class
     * 
     * @param or portal.csw.OnlineResource The resource to query
     * @param callback function(Object) Called when editing finishes (only on success). Contains the newly updated dlOptions
     * @param dlOptions Object The current state of download options.
     */
    vegl.util.DataSelectionUtil.showDownloadOptionsForResource = function(or, dlOptions, callback) {
        switch (or.get('type')) {
        case portal.csw.OnlineResource.WCS:
            Ext.create('Ext.window.Window', {
                layout : 'fit',
                width : 700,
                height : 400,
                modal : true,
                title : 'Subset of ' + or.get('name'),
                items : [{
                    xtype : 'erddapsubsetpanel',
                    itemId : 'subset-panel',
                    region : Ext.create('portal.util.BBox', dlOptions),
                    coverageName : dlOptions.layerName,
                    coverageUrl : or.get('url'),
                    name : dlOptions.name,
                    localPath : dlOptions.localPath,
                    description : dlOptions.description,
                    dataType : dlOptions.format
                }],
                buttons : [{
                    text : 'Save Changes',
                    iconCls : 'add',
                    align : 'right',
                    scope : this,
                    handler : function(btn) {
                        var parentWindow = btn.findParentByType('window');
                        var panel = parentWindow.getComponent('subset-panel');

                        var params = panel.getForm().getValues();

                        //The ERDDAP subset panel doesn't use coverage name for anything but size estimation
                        //Therefore we need to manually preserve it ourselves
                        params.layerName = dlOptions.layerName;
                        params = Ext.apply(dlOptions, params);
                        
                        parentWindow.close();
                        
                        callback(params);
                    }
                }]
            }).show();
            break;
        case portal.csw.OnlineResource.WFS:
            Ext.create('Ext.window.Window', {
                layout : 'fit',
                width : 700,
                height : 450,
                modal : true,
                title : 'Subset of ' + or.get('name'),
                items : [{
                    xtype : 'wfssubsetpanel',
                    itemId : 'subset-panel',
                    bodyPadding : 10,
                    region : Ext.create('portal.util.BBox', dlOptions),
                    featureType : dlOptions.featureType,
                    serviceUrl : dlOptions.serviceUrl,
                    name : dlOptions.name,
                    localPath : dlOptions.localPath,
                    description : dlOptions.description,
                    outputFormat : dlOptions.outputFormat,
                    srsName : dlOptions.srsName
                }],
                buttons : [{
                    text : 'Save Changes',
                    iconCls : 'add',
                    align : 'right',
                    scope : this,
                    handler : function(btn) {
                        var parentWindow = btn.findParentByType('window');
                        var panel = parentWindow.getComponent('subset-panel');

                        var params = panel.getForm().getValues();

                        params.serviceUrl = params.serviceUrl;

                        parentWindow.close();
                        
                        callback(params);
                    }
                }]
            }).show();
            break;
        default:
            Ext.create('Ext.window.Window', {
                layout : 'fit',
                width : 700,
                height : 400,
                modal : true,
                title : 'Download from ' + or.get('name'),
                items : [{
                    xtype : 'filedownloadpanel',
                    itemId : 'download-panel',
                    region : Ext.create('portal.util.BBox', dlOptions),
                    url : dlOptions.url,
                    localPath : dlOptions.localPath,
                    name : dlOptions.name,
                    description : dlOptions.description
                }],
                buttons : [{
                    text : 'Save Changes',
                    iconCls : 'add',
                    align : 'right',
                    scope : this,
                    handler : function(btn) {
                        var parentWindow = btn.findParentByType('window');
                        var panel = parentWindow.getComponent('download-panel');
                        var params = panel.getForm().getValues();

                        parentWindow.close();
                        
                        callback(params);
                    }
                }]
            }).show();
            break;
        }
    };
    
    /**
     * Create a download request URL (in the form of a Download object). 
     * The request can optionally be stored into the user session
     * for use later in the job submit phase of the workflow.
     * 
     * @param or portal.csw.OnlineResource The resource to query
     * @param dlOptions Object The current state of download options.
     * @param saveInSession Boolean Should this download be saved into the users session for use during job submit?
     * @param callback function(Boolean, vegl.models.Download) called whenever the save request returns. Success is passed as the first parameter
     */
    vegl.util.DataSelectionUtil.makeDownloadUrl = function(or, dlOptions, saveInSession, callback) {
        
        var ajaxResponseHandler = function(options, success, response, callback) {
            if (!success) {
                callback(false, null);
                return;
            }
            var responseObj = Ext.JSON.decode(response.responseText);
            if (!responseObj || !responseObj.success) {
                callback(false, null);
                return;
            }
            
            callback(true, Ext.create('vegl.models.Download', responseObj.data));
        };
        
        switch (or.get('type')) {
        case portal.csw.OnlineResource.WCS:
            
            //Unfortunately ERDDAP requests that extend beyond the spatial bounds of the dataset
            //will fail. To workaround this, we need to crop our selection to the dataset bounds
            if (dlOptions.dsEastBoundLongitude != null && (dlOptions.dsEastBoundLongitude < dlOptions.eastBoundLongitude)) {
                dlOptions.eastBoundLongitude = dlOptions.dsEastBoundLongitude;
            }
            if (dlOptions.dsWestBoundLongitude != null && (dlOptions.dsWestBoundLongitude > dlOptions.westBoundLongitude)) {
                dlOptions.westBoundLongitude = dlOptions.dsWestBoundLongitude;
            }
            if (dlOptions.dsNorthBoundLatitude != null && (dlOptions.dsNorthBoundLatitude < dlOptions.northBoundLatitude)) {
                dlOptions.northBoundLatitude = dlOptions.dsNorthBoundLatitude;
            }
            if (dlOptions.dsSouthBoundLatitude != null && (dlOptions.dsSouthBoundLatitude > dlOptions.southBoundLatitude)) {
                dlOptions.southBoundLatitude = dlOptions.dsSouthBoundLatitude;
            }
            
            Ext.Ajax.request({
                url : 'makeErddapUrl.do',
                params : Ext.apply({saveSession : saveInSession}, dlOptions),
                callback : Ext.bind(ajaxResponseHandler, this, [callback], true)
            });
            break;
        case portal.csw.OnlineResource.WFS:
            Ext.Ajax.request({
                url : 'makeWfsUrl.do',
                params : Ext.apply({saveSession : saveInSession}, dlOptions),
                callback : Ext.bind(ajaxResponseHandler, this, [callback], true)
            });
            break;
        default:
            Ext.Ajax.request({
                url : 'makeDownloadUrl.do',
                params : Ext.apply({saveSession : saveInSession}, dlOptions),
                callback : Ext.bind(ajaxResponseHandler, this, [callback], true)
            });
            break;
        }
    };
});