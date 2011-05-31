/**
 * Builds a form panel for WMS Layers (Containing WMS specific options such as transparency).
 *
 */

WMSLayerFilterForm = function(activeLayerRecord, map) {

	this.activeLayerRecord = activeLayerRecord;
	var pos = new GControlPosition(G_ANCHOR_BOTTOM_LEFT, new GSize(0, -85));
	map.addControl(new MStatusControl({position:pos}));
	
	this.xCellDim;
	this.yCellDim;
	this.zCellDim;
	this.maxDepth;
	this.mgaZone;
	
	// create a drag control for each bounding box
	var dataBbox = new MPolyDragControl({map:map,type:'rectangle',activeLayerRecord:activeLayerRecord});
	this.dataBbox = dataBbox;
	
    var sliderHandler = function(caller, newValue) {
    	var overlayManager = activeLayerRecord.getOverlayManager();
    	var newOpacity = (newValue / 100);
    	
    	activeLayerRecord.setOpacity(newOpacity);
    	overlayManager.updateOpacity(newOpacity);
    };
    
    var freeDrawButton = new Ext.Button({  
        text 		: 'Enable Free Draw',
        right		: '5px',
        handler 	: function() {
			dataBbox.enableTransMarker();
			freeDrawButton.disable();
			captureSubsetButton.enable();
    	}
    });
    
    var drawCoordsButton = new Ext.Button({  
        text 	: 'Draw Co-ords',
        width	: 85,
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
    var resetButton = new Ext.Button({  
        text 	: 'Reset',
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
    
    var captureSubsetButton = new Ext.Button({  
        text 	: 'Capture Subset',
        width	: 85,
        bodyStyle	: 'margin:5px 0 0 0',
        disabled: true,
        scope: this,
        handler: function() {
        	
        	if (dataBbox.getParams() == null) {
        		Ext.Msg.alert("Error", 'You must draw the data bounds before capturing.');
        	} 
        	else {
        		
        		// check if selected region is within the bounds of the coverage layer
        		var cswRecords = activeLayerRecord.getCSWRecordsWithType('WCS');
                if (cswRecords.length != 0) {
                	//Assumption - we only expect 1 WCS
            		var cswRecord = cswRecords[0];
            		var bbox = cswRecord.getGeographicElements()[0];
            		            	
            		if (bbox.eastBoundLongitude < dataBbox.getNorthEastLng() ||
            				bbox.westBoundLongitude > dataBbox.getSouthWestLng() ||
            				bbox.northBoundLatitude < dataBbox.getNorthEastLat() ||
            				bbox.southBoundLatitude > dataBbox.getSouthWestLat()) {
            			Ext.Msg.alert("Error", 'Selected region exceeds the coverage bounds. Please adjust region selection.');
            			return;
            		}
                }    
                
                Ext.Ajax.request({
	        		url		: 'calculateMgaZone.do' ,
	        		scope	: this,
	        		success : WMSLayerFilterForm.onCalculateMgaZoneResponse,
	        		failure : WMSLayerFilterForm.onRequestFailure,
	        		params	: {
	        			dataCoords 		: dataBbox.getParams()
	            	}
	        	});
        	}
        }
    });
    
    // data bounding box lat/lng text fields
    var neLat = createNumberfield('Lat (NE)', 'neLat');
    var neLng = createNumberfield('Lon (NE)', 'neLng');
    var swLat = createNumberfield('Lat (SW)', 'swLat');
    var swLng = createNumberfield('Lon (SW)', 'swLng');
    var buffer = createNumberfield('Buffer (km)', 'buffer');
    
    // subset file type selection values
	var fileTypes =  [
   		 ['CSV','csv'],
   		 ['GeoTIFF','geotif'],
   		 ['NetCDF','nc']
   	];
	
	var fileTypeStore = new Ext.data.SimpleStore({
		fields : ['type','value'],
        data   : fileTypes
    });
	
	var fileTypeCombo = new Ext.form.ComboBox({  
		tpl: '<tpl for="."><div ext:qtip="{type}" class="x-combo-list-item">{type}</div></tpl>',
        width          : 100,
        editable       : false,
        forceSelection : true,
        fieldLabel     : 'Subset File Type',
        mode           : 'local',
        store          : fileTypeStore,
        triggerAction  : 'all',
        typeAhead      : false,
        displayField   : 'type',
        valueField     : 'value',
        value          : 'csv',
        submitValue	   : false
    });
	
	this.fileTypeCombo = fileTypeCombo;
    
	this.isFormLoaded = true; //We aren't reliant on any remote downloads
	
    //-----------Panel
    WMSLayerFilterForm.superclass.constructor.call(this, {
    	id          : String.format('{0}',activeLayerRecord.getId()),
    	scope		: this,
        border      : false,
        autoScroll  : true,
        hideMode    : 'offsets',
        labelAlign  : 'right',
        bodyStyle   : 'padding:5px',
        autoHeight:    true,
        layout: 'anchor',
        items:[ {
            xtype      :'fieldset',
            title      : 'Coverage Subset Controls',
            autoHeight : true,
            items      : [{
                    xtype       : 'slider',
                    fieldLabel  : 'Opacity',
                    minValue    : 0,
                    maxValue    : 100,
                    value       : (activeLayerRecord.getOpacity() * 100),
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
};

WMSLayerFilterForm.onCalculateMgaZoneResponse = function(response, request) {
    var resp = Ext.decode(response.responseText);
    var wmsForm = this;
    
    if (resp.mgaZone != null) {

    	var mgaZoneWindow = new InversionDimensionWindow({
            mgaZone : resp.mgaZone,
            scope	: wmsForm,
            callback : function(wmsForm, newMgaZone, xCellDim, yCellDim, zCellDim, maxDepth) {
            	
            	// set instance variables for use again in creatingErddapRequest
            	wmsForm.xCellDim = xCellDim;
            	wmsForm.yCellDim = yCellDim;
            	wmsForm.zCellDim = zCellDim;
            	wmsForm.maxDepth = maxDepth;
            	wmsForm.mgaZone = newMgaZone;
            	
            	Ext.Ajax.request({
	        		url		: 'projectToUtm.do' ,
	        		scope	: wmsForm,
	        		success : WMSLayerFilterForm.onProjectToUtmResponse,
	        		failure : WMSLayerFilterForm.onRequestFailure,
	        		params	: {
	        			dataCoords 	: wmsForm.dataBbox.getParams(),
	        			mgaZone		: newMgaZone,
	        			xCellDim	: xCellDim,
	        			yCellDim	: yCellDim,
	        			zCellDim	: zCellDim,
	        			maxDepth	: maxDepth
	            	}
	        	});
            }
        });
    	
    	mgaZoneWindow.show();
    } else {
    	Ext.Msg.alert("Error", "Could not calculate MGA Zone" );
    }
};

WMSLayerFilterForm.onProjectToUtmResponse = function(response, request) {
    var resp = Ext.decode(response.responseText);
    var wmsForm = this;
    
    if (resp.eastingArray != null && resp.northingArray != null && resp.depthArray) {

    	var mgaZoneWindow = new UTMBoundsInfoWindow({
    		scope	: wmsForm,
    		eastingArray : resp.eastingArray,
    		northingArray : resp.northingArray,
    		depthArray : resp.depthArray,
            callback : function(wmsForm, minEast, maxEast, minNorth, maxNorth, maxDepth) {
            	
            	Ext.Ajax.request({
	        		url		: 'createErddapRequest.do' ,
	        		scope	: this,
	        		success : WMSLayerFilterForm.onCreateErddapRequestResponse,
	        		failure : WMSLayerFilterForm.onRequestFailure,
	        		params	: {
	        			dataCoords 		: wmsForm.dataBbox.getParams(),
	        			mgaZone		: wmsForm.mgaZone,
	        			xCellDim	: wmsForm.xCellDim,
	        			yCellDim	: wmsForm.yCellDim,
	        			zCellDim	: wmsForm.zCellDim,
	        			maxDepth	: wmsForm.maxDepth,
	        			minEast		: minEast,
	        			maxEast		: maxEast,
	        			minNorth	: minNorth,
	        			maxNorth	: maxNorth,
	        			layerName  		: wmsForm.activeLayerRecord.getLayerName(),
	        			format			: wmsForm.fileTypeCombo.getValue()
	            	}
	        	});
            }
        });
    	
    	mgaZoneWindow.show();
    } else {
    	Ext.Msg.alert("Error", "Failed to project co-ordinates to UTM" );
    }
};

WMSLayerFilterForm.onCreateErddapRequestResponse = function(response, request) {
    var resp = Ext.decode(response.responseText);
    if (resp.error != null) {
        JobList.showError(resp.error);
    } else {
        Ext.Msg.alert("Success", "Subset bounds have been captured.");
    }
};

//called when an Ajax request fails
WMSLayerFilterForm.onRequestFailure = function(response, request) {
	Ext.Msg.alert("Error", 'Could not execute last request. Status: '+
        response.status+' ('+response.statusText+')');
};

// create a number field
createNumberfield = function(label, id) {
	return new Ext.form.NumberField({
    	width		: 100,
    	xtype      	: 'numberfield',
        fieldLabel 	: label,
        name       	: id
    });
};

Ext.extend(WMSLayerFilterForm, Ext.FormPanel, {
    
});