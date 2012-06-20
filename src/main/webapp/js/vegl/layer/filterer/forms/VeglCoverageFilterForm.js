/**
 * Builds a form panel for Vegl Coverage Layers
 *
 */
Ext.define('vegl.layer.filterer.forms.VeglCoverageFilterForm', {
    extend: 'portal.layer.filterer.BaseFilterForm',

    /**
     * Accepts a config for portal.layer.filterer.BaseFilterForm
     */
    constructor : function(config) {
        var layer = config.layer;
        var map = config.map;
        var filterer = layer.get('filterer');

        //Warning - the following limits us to Google Maps. A necessary evil unless we upgrade the base map abstraction.
        if (!(map instanceof portal.map.gmap.GoogleMap)) {
            console.error('VeglCoverageFilterForm is currently only compatible with Google Maps.');
            this.callParent(arguments);
            return;
        }

        //Prepare the map instance (Gmap specific)
        var gMap2 = map.map;
        var pos = new GControlPosition(G_ANCHOR_BOTTOM_LEFT, new GSize(0, -85));
        gMap2.addControl(new MStatusControl({position:pos}));
        this.xCellDim;
        this.yCellDim;
        this.zCellDim;
        this.maxDepth;
        this.mgaZone;

        // create a drag control for each bounding box
        var dataBbox = new MPolyDragControl({map: gMap2, type: 'rectangle', labelText: layer.get('name')});
        this.dataBbox = dataBbox;

        var sliderHandler = function(caller, newValue) {
            var newOpacity = (newValue / 100);
            filterer.setParameter('opacity',newOpacity);
        };

        if(!filterer.getParameter('opacity')){
            filterer.setParameter('opacity',100);
        }

        var freeDrawButton = Ext.create('Ext.Button', {
            text        : 'Enable Free Draw',
            right       : '5px',
            handler     : function() {
                dataBbox.enableTransMarker();
                freeDrawButton.disable();
                captureSubsetButton.enable();
            }
        });

        var drawCoordsButton = Ext.create('Ext.Button', {
            text    : 'Draw Co-ords',
            width   : 85,
            handler : function() {

                if (neLat.getValue() == '' || neLng.getValue() == '' || swLat.getValue() == '' || swLng.getValue() == '') {
                    Ext.Msg.alert("Error", 'All lat/long co-ordinates must be entered before drawing.');
                }
                else {
                    dataBbox.drawRectangle(neLat.getValue(), neLng.getValue(), swLat.getValue(), swLng.getValue());
                    freeDrawButton.disable();
                    captureSubsetButton.enable();
                }
            }
        });

        // remove any bounding box and clear lat/long entries
        var resetButton = Ext.create('Ext.Button', {
            text    : 'Reset',
            handler : function() {
                dataBbox.reset();
                neLat.setValue('');
                neLng.setValue('');
                swLat.setValue('');
                swLng.setValue('');

                freeDrawButton.enable();
                captureSubsetButton.disable();
            }
        });

        var captureSubsetButton = Ext.create('Ext.Button', {
            text    : 'Capture Subset',
            width   : 85,
            bodyStyle   : 'margin:5px 0 0 0',
            disabled: true,
            scope: this,
            handler: function() {
                if (dataBbox.getParams() == null) {
                    Ext.Msg.alert("Error", 'You must draw the data bounds before capturing.');
                } else {
                    // check if selected region is within the bounds of the coverage layer
                    var cswRecords = layer.get('cswRecords');
                    if (cswRecords.length != 0) {
                        //Assumption - we only expect 1 WCS
                        var cswRecord = cswRecords[0];
                        var bboxes = cswRecord.get('geographicElements');
                        if (bboxes.length > 0) {
                            //Assumption - we only expect 1 BBox
                            bbox = bboxes[0];
                            if (bbox.eastBoundLongitude < dataBbox.getNorthEastLng() ||
                                    bbox.westBoundLongitude > dataBbox.getSouthWestLng() ||
                                    bbox.northBoundLatitude < dataBbox.getNorthEastLat() ||
                                    bbox.southBoundLatitude > dataBbox.getSouthWestLat()) {
                                Ext.Msg.alert("Error", 'Selected region exceeds the coverage bounds. Please adjust region selection.');
                                return;
                            }
                        }
                    }

                    Ext.Ajax.request({
                        url     : 'calculateMgaZone.do' ,
                        scope   : this,
                        success : this.onCalculateMgaZoneResponse,
                        failure : this.onRequestFailure,
                        params  : {
                            dataCoords      : dataBbox.getParams()
                        }
                    });
                }
            }
        });

        // data bounding box lat/lng text fields
        var neLat = Ext.create('Ext.form.NumberField', {
            anchor      : '100%',
            fieldLabel  : 'Lat (NE)',
            name        : 'neLat'
        });
        var neLng = Ext.create('Ext.form.NumberField', {
            anchor      : '100%',
            fieldLabel  : 'Lon (NE)',
            name        : 'neLng'
        });
        var swLat = Ext.create('Ext.form.NumberField', {
            anchor      : '100%',
            fieldLabel  : 'Lat (SW)',
            name        : 'swLat'
        });
        var swLng = Ext.create('Ext.form.NumberField', {
            anchor      : '100%',
            fieldLabel  : 'Lon (SW)',
            name        : 'swLng'
        });
        var buffer = Ext.create('Ext.form.NumberField', {
            anchor      : '100%',
            fieldLabel  : 'Buffer (km)',
            name        : 'buffer'
        });

        // subset file type selection values
        var fileTypeStore = Ext.create('Ext.data.Store', {
            fields : [{name : 'urn'}, {name : 'label'}],
            data : [{label : 'CSV', urn : 'csv'},
                    {label : 'GeoTIFF', urn : 'geotif'},
                    {label : 'NetCDF', urn : 'nc'}]
        });

        var fileTypeCombo = Ext.create('Ext.form.ComboBox', {
            anchor         : '100%',
            editable       : false,
            forceSelection : true,
            fieldLabel     : 'Subset File Type',
            mode           : 'local',
            store          : fileTypeStore,
            triggerAction  : 'all',
            typeAhead      : false,
            displayField   : 'label',
            valueField     : 'urn',
            value          : 'csv',
            submitValue    : false
        });
        this.fileTypeCombo = fileTypeCombo;


        Ext.apply(config, {
            delayedFormLoading: false, //we don't need to use a delayed load as all of our info is ready now
            border      : false,
            autoScroll  : true,
            hideMode    : 'offsets',
            labelAlign  : 'right',
            bodyStyle   : 'padding:5px',
            autoHeight:    true,
            layout: 'anchor',
            items:[{
                xtype      :'fieldset',
                title      : 'Coverage Subset Controls',
                autoHeight : true,
                items      : [{
                        xtype       : 'slider',
                        fieldLabel  : 'Opacity',
                        anchor      : '100%',
                        minValue    : 0,
                        maxValue    : 100,
                        value       : (filterer.getParameter('opacity') * 100),
                        listeners   : {changecomplete: sliderHandler}
                },
                    fileTypeCombo,
                    neLat,
                    neLng,
                    swLat,
                    swLng,
                    {
                        // column layout with 3 columns
                        layout:'column',
                        border: false,
                        items:[{
                            border: false,
                            bodyStyle:'margin-right:3px',
                            items:[drawCoordsButton]
                        },{
                            border: false,
                            bodyStyle:'margin-right:3px',
                            items:[freeDrawButton]
                        },{
                            border: false,
                            items:[resetButton]
                        }]
                    },{
                    border: false,
                    bodyStyle:'margin-top:3px',
                    items:[captureSubsetButton]
                    }
                ]
            }]
        });

        this.callParent(arguments);
    },

    onCalculateMgaZoneResponse : function(response, request) {
        var resp = Ext.JSON.decode(response.responseText);
        var wmsForm = this;

        if (resp.success && resp.data != null) {

            var mgaZoneWindow = Ext.create('vegl.widgets.InversionDimensionWindow', {
                mgaZone : resp.data,
                scope   : wmsForm,
                callback : function(wmsForm, newMgaZone, xCellDim, yCellDim, zCellDim, maxDepth) {

                    // set instance variables for use again in creatingErddapRequest
                    wmsForm.xCellDim = xCellDim;
                    wmsForm.yCellDim = yCellDim;
                    wmsForm.zCellDim = zCellDim;
                    wmsForm.maxDepth = maxDepth;
                    wmsForm.mgaZone = newMgaZone;

                    Ext.Ajax.request({
                        url     : 'projectToUtm.do' ,
                        scope   : wmsForm,
                        success : wmsForm.onProjectToUtmResponse,
                        failure : wmsForm.onRequestFailure,
                        params  : {
                            dataCoords  : wmsForm.dataBbox.getParams(),
                            mgaZone     : newMgaZone,
                            xCellDim    : xCellDim,
                            yCellDim    : yCellDim,
                            zCellDim    : zCellDim,
                            maxDepth    : maxDepth
                        }
                    });
                }
            });

            mgaZoneWindow.show();
        } else {
            Ext.Msg.alert("Error", "Could not calculate MGA Zone: " + resp.msg);
        }
    },

    onProjectToUtmResponse : function(response, request) {
        var resp = Ext.JSON.decode(response.responseText);
        var wmsForm = this;

        if (resp.success && resp.data.eastingArray != null && resp.data.northingArray != null && resp.data.depthArray) {

            var mgaZoneWindow = Ext.create('vegl.widgets.UTMBoundsInfoWindow', {
                scope   : wmsForm,
                eastingArray : resp.data.eastingArray,
                northingArray : resp.data.northingArray,
                depthArray : resp.data.depthArray,
                callback : function(wmsForm, minEast, maxEast, minNorth, maxNorth, maxDepth) {

                    var onlineResources = wmsForm.layer.getAllOnlineResources();
                    var wcsResources = portal.csw.OnlineResource.getFilteredFromArray(onlineResources, portal.csw.OnlineResource.WCS);
                    var coverageName = wcsResources[0].get('name');

                    Ext.Ajax.request({
                        url     : 'createErddapRequest.do' ,
                        scope   : this,
                        success : wmsForm.onCreateErddapRequestResponse,
                        failure : wmsForm.onRequestFailure,
                        params  : {
                            dataCoords      : wmsForm.dataBbox.getParams(),
                            mgaZone     : wmsForm.mgaZone,
                            xCellDim    : wmsForm.xCellDim,
                            yCellDim    : wmsForm.yCellDim,
                            zCellDim    : wmsForm.zCellDim,
                            maxDepth    : wmsForm.maxDepth,
                            minEast     : minEast,
                            maxEast     : maxEast,
                            minNorth    : minNorth,
                            maxNorth    : maxNorth,
                            layerName   : coverageName,
                            format      : wmsForm.fileTypeCombo.getValue()
                        }
                    });
                }
            });

            mgaZoneWindow.show();
        } else {
            Ext.Msg.alert("Error", "Failed to project co-ordinates to UTM" );
        }
    },

    onCreateErddapRequestResponse : function(response, request) {
        var resp = Ext.decode(response.responseText);
        if (resp.success) {
            Ext.Msg.alert("Success", "Subset bounds have been captured.");
        } else {
            Ext.Msg.alert("Failure", "There has been a problem generating the ERRDAP request: " + resp.msg);
        }
    },

    //called when an Ajax request fails
    onRequestFailure : function(response, request) {
        Ext.Msg.alert("Error", 'Could not execute last request. Status: ' + response.status+' ('+response.statusText+')');
    }
});