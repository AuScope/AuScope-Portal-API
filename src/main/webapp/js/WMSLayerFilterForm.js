/**
 * Builds a form panel for WMS Layers (Containing WMS specific options such as transparency).
 *
 */

WMSLayerFilterForm = function(activeLayerRecord, map) {

	var pos = new GControlPosition(G_ANCHOR_BOTTOM_LEFT, new GSize(0, -85));
	map.addControl(new MStatusControl({position:pos}));
	
	// create a drag control for each bounding box
	//var bufferBbox = new MPolyDragControl({map:map,type:'rectangle',label:'Buffer'});
	var dataBbox = new MPolyDragControl({map:map,type:'rectangle',activeLayerRecord:activeLayerRecord});
	
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
        handler: function() {
        	
        	if (dataBbox.getParams() == null) {
        		Ext.Msg.alert("Error", 'You must draw the data bounds before capturing.');
        	} 
        	/*else if (bufferBbox.getParams() == null) {
        		Ext.Msg.alert("Error", 'A buffer amount must be entered before capturing.');
        	}*/
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
        		
        		// set buffer co-ordinates
        		//calculateBufferCoords(neLat.getValue(), neLng.getValue(), swLat.getValue(), swLng.getValue(), buffer.getValue());
        		
	        	Ext.Ajax.request({
	        		url: 'sendSubsetsToGrid.do' ,
	        		success: WMSLayerFilterForm.onSendToGridResponse,
	        		failure: WMSLayerFilterForm.onRequestFailure,
	        		params		: {
	        			layerName  		: activeLayerRecord.getLayerName(),
	        			dataCoords 		: dataBbox.getParams(),
	        			//bufferCoords	: bufferBbox.getParams(),
	        			format			: fileTypeCombo.getValue()
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
   		 //['NetCDF','nc'],
   		 ['CSV','csv'],
   		 ['GeoTIFF','geotif'],
   		 ['KML','kml']
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
    
	this.isFormLoaded = true; //We aren't reliant on any remote downloads
	
    //-----------Panel
    WMSLayerFilterForm.superclass.constructor.call(this, {
    	id          : String.format('{0}',activeLayerRecord.getId()),
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
            	//buffer,
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

WMSLayerFilterForm.onSendToGridResponse = function(response, request) {
    var resp = Ext.decode(response.responseText);
    if (resp.error != null) {
        JobList.showError(resp.error);
    } else {
        Ext.Msg.alert("Success", "The selected coverage subset was added as an input for the job.");
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

// this method is incomplete due to the decision to drop the buffer sub set for now.
// keeping code in case this decision changed.
calculateBufferCoords = function(neLat, neLng, swLat, swLng, buffer) {
	
	// convert buffer to meters
	buffer = buffer * 1000;
	
	// convert lat/lng to UTM
	neLatLng = LatLng(neLat,neLng);
	swLatLng = LatLng(wsLat,swLng);
	neUTM = neLatLng.toUTMRef();
	swUTM = swLatLng.toUTMRef();
	
	// apply buffer
	neUTM.easting = neUTM.easting + buffer;
	neUTM.northing = neUTM.northing + buffer;
	swUTM.easting = swUTM.easting - buffer;
	swUTM.northing = swUTM.northing - buffer;
	
	// convert back to lat/lng	
	
};

Ext.extend(WMSLayerFilterForm, Ext.FormPanel, {
    
});