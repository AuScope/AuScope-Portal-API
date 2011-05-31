/**
 * A window for showing the projected UTM bounds to the user and allowing edit of values
 * 
 * {
 *  callback : function(WMSLayerFilterForm, int, int, int, int, int) Called when this window is closed and is passed 
 *  the mgaZone and cellDimension values 
 *  scope : the object to use as the scope 
 *  eastingArray : the int array containing the min and max easting UTM coordinate values [min,max]
 *  northingArray : the int array containing the min and max northing UTM coordinate values [min,max]
 *  depthArray : the int array containing the min and max depth values [min,max] (we only care about the max though as 
 *  	the min will alwys be 0)
 * }
 * 
 */

UTMBoundsInfoWindow = function(config) {
	
	this.callback = config.callback;
	var parentWindow = this;
	var scope = config.scope;
	
	var minEast = createNumberfield('Min Easting', 'minEast', config.eastingArray[0]);
	var maxEast = createNumberfield('Max Easting', 'maxEast', config.eastingArray[1]);
	var minNorth = createNumberfield('Min Northing', 'minNorth', config.northingArray[0]);
	var maxNorth = createNumberfield('Max Northing', 'maxNorth', config.northingArray[1]);
	var maxDepth = createNumberfield('Max Depth', 'maxDepth', config.depthArray[1]);
	
	UTMBoundsInfoWindow.superclass.constructor.call(this, {
	    border          : true,        
	    layout          : 'fit',
	    resizable       : true,
	    modal           : true,
	    plain           : false,
	    buttonAlign     : 'right',
	    title           : 'Projected UTM Co-ordinates',
	    width           : 250,
	    autoHeight 		: true,
	    scope 			: this,
	    items 		: [{   
	            // Bounding form
	            id      :'wcsDownloadFrm',
	            xtype   :'form',
	            layout  :'form',
	            frame   : true,
	            autoHeight : true,
	            autoWidth	: true,
	            
	            // these are applied to columns
	            defaults:{
	                xtype: 'fieldset', layout: 'form'
	            },
	            
	            // fieldsets
	            items: [
                    minEast,
                    maxEast,
                    minNorth,
                    maxNorth,
                    maxDepth
                ]
	        }],
	        buttons:[{
	            xtype: 'button',
	            text: 'OK',
	            handler: function() {
	            	parentWindow.callback(scope,
	            			minEast.getValue(),
	            			maxEast.getValue(),
	            			minNorth.getValue(),
	            			maxNorth.getValue(),
	            			maxDepth.getValue());
	            	parentWindow.close();
	            }
	        },{
	            xtype: 'button',
	            text: 'Cancel',
	            handler: function() {
	            	parentWindow.close();
	            }
	        }]
    });
};

Ext.extend(UTMBoundsInfoWindow, Ext.Window, {
    callback : function() {}
});

//create a number field
createNumberfield = function(label, id, value) {
	return new Ext.form.NumberField({
    	width		: 100,
    	xtype      	: 'numberfield',
        fieldLabel 	: label,
        name       	: id,
        value 		: value
    });
};
