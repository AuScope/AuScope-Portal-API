/**
 * Builds a form panel for WMS Layers (Containing WMS specific options such as transparency).
 *
 */

WMSLayerFilterForm = function(record, map) {

	var pos = new GControlPosition(G_ANCHOR_BOTTOM_LEFT, new GSize(0, -85));
	map.addControl(new MStatusControl({position:pos}));
	
	// create a drag control for each bounding box
	var meshBbox = new MPolyDragControl({map:map,type:'rectangle',label:'Mesh'});
	var bufferBbox = new MPolyDragControl({map:map,type:'rectangle',label:'Buffer'});
	var dataBbox = new MPolyDragControl({map:map,type:'rectangle',label:'Data'});
	
    var sliderHandler = function(caller, newValue) {
        record.set('opacity', (newValue / 100));
        
        if (record.tileOverlay instanceof OverlayManager) {
        	record.tileOverlay.updateOpacity(record.get('opacity'));
        } else {
	        record.tileOverlay.getTileLayer().opacity = record.get('opacity');
	
	        map.removeOverlay(record.tileOverlay);
	        map.addOverlay(record.tileOverlay);
        }
    };
    
    var drawBoundsButton = new Ext.Button({  
        text 	: 'Draw Bounding Box',
        width	: 110,
        handler : function() {
	    	meshBbox.enableTransMarker();
			bufferBbox.enableTransMarker();
			dataBbox.enableTransMarker();
			
			drawBoundsButton.hide();
    		clearBoundsButton.show();
    		sendToGridButton.setDisabled(false);
    	}
    });
    
    var clearBoundsButton = new Ext.Button({  
        text 	: 'Clear Bounding Box',
        width	: 110,
        hidden	: true,
        handler : function() {
    		meshBbox.reset();
    		bufferBbox.reset();
    		dataBbox.reset();
			
			drawBoundsButton.show();
			clearBoundsButton.hide();
			sendToGridButton.setDisabled(true);
    	}
    });
    
    var sendToGridButton = new Ext.Button({  
        text 	: 'Send to Grid',
        disabled: true,
        handler: function() {
    		if (dataBbox.getParams() == null || bufferBbox.getParams() == null || meshBbox.getParams() == null) {
    			alert("You must specify bounding boxes for data, buffer and mesh before sending to the grid.");
    		}
    		else {
    		alert('Data: ' + dataBbox.getParams() + 
    				'\nBuffer: ' + bufferBbox.getParams() + 
    				'\nMesh: ' + meshBbox.getParams());
    		}
    	}
    });
    
    //-----------Panel
    WMSLayerFilterForm.superclass.constructor.call(this, {
        id          : String.format('{0}',record.get('id')),
        border      : false,
        autoScroll  : true,
        hideMode    : 'offsets',
        //width       : '100%',
        labelAlign  : 'right',
        bodyStyle   : 'padding:5px',
        autoHeight:    true,
        layout: 'anchor',
        items:[ {
            xtype      :'fieldset',
            title      : 'WMS Properties',
            autoHeight : true,
            items      : [{
                    xtype       : 'slider',
                    fieldLabel  : 'Opacity',
                    minValue    : 0,
                    maxValue    : 100,
                    value       : (record.get('opacity') * 100),
                    listeners   : {changecomplete: sliderHandler}
            },
            	drawBoundsButton,
            	clearBoundsButton,
            	sendToGridButton,
            	{
    				// column layout with 2 columns
                    layout:'column',
                    border: false,
                    items:[{
                        // right column
                        border: false,
                        items:[drawBoundsButton,
                           	clearBoundsButton]
                    }
                    ,{
                        // right column
                        border: false,
                    	bodyStyle:'margin-left:5px',
                        items:[sendToGridButton]
                    }
                    ]
                }
            ]
        }]
    });
};

Ext.extend(WMSLayerFilterForm, Ext.FormPanel, {
    
});