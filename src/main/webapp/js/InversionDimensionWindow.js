/**
 * A window for allowing the user to select the required MGA zone and enter the inversion cell dimensions
 * 
 * {
 *  callback : function(WMSLayerFilterForm, int, int, int, int, int) Called when this window is closed and is passed the mgaZone and cellDimension values 
 *  mgaZone : the MGA zone calculated based on the center of the selected area
 *  scope : the object to use as the scope 
 * }
 * 
 */

InversionDimensionWindow = function(config) {
	
	this.callback = config.callback;
	var parentWindow = this;
	var scope = config.scope;
	
	//subset file type selection values
	var mgaZoneTypes =  [
			 ['49', 49],
			 ['50', 50],
			 ['51', 51],
			 ['52', 52],
			 ['53', 53],
			 ['54', 54],
			 ['55', 55],
			 ['56', 56],
		];

	var mgaZoneStore = new Ext.data.SimpleStore({
		fields : ['type','value'],
	    data   : mgaZoneTypes
	});

	var mgaZoneCombo = new Ext.form.ComboBox({  
		tpl: '<tpl for="."><div ext:qtip="{type}" class="x-combo-list-item">{type}</div></tpl>',
	    width          : 50,
	    editable       : false,
	    forceSelection : true,
	    fieldLabel     : 'MGA Zone',
	    mode           : 'local',
	    store          : mgaZoneStore,
	    triggerAction  : 'all',
	    typeAhead      : false,
	    displayField   : 'type',
	    valueField     : 'value',
	    submitValue	   : false,
	    value		   : config.mgaZone
	});
	
	var xCellDim = createNumberfield('X Cell Dimension', 'xCellDim', 0);
	var yCellDim = createNumberfield('Y Cell Dimension', 'yCellDim', 0);
	var zCellDim = createNumberfield('Z Cell Dimension', 'zCellDim', 0);
	var maxDepth = createNumberfield('Max Depth', 'maxDepth', 0);
	
	InversionDimensionWindow.superclass.constructor.call(this, {
	    border          : true,        
	    layout          : 'fit',
	    resizable       : true,
	    modal           : true,
	    plain           : false,
	    buttonAlign     : 'right',
	    title           : 'Enter Inversion Cell Dimensions',
	    width           : 250,
	    autoHeight 		: true,
	    scope 			: this,
	    items 		: [{   
	            // Bounding form
	            //id      :'wcsDownloadFrm',
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
                    mgaZoneCombo,
                    xCellDim,
                    yCellDim,
                    zCellDim,
                    maxDepth
                ]
	        }],
	        buttons:[{
	            xtype: 'button',
	            text: 'OK',
	            handler: function() {
	            	
	            	if (xCellDim.getValue() > 0 || yCellDim.getValue() > 0 || zCellDim.getValue() > 0) {
	            		parentWindow.callback(scope,
		            			mgaZoneCombo.getValue(), 
		            			xCellDim.getValue(),
		            			yCellDim.getValue(),
		            			zCellDim.getValue(),
		            			maxDepth.getValue());
		            	parentWindow.close();
	            	} else {
	            		Ext.Msg.alert("Warning", "Cell dimensions must be greater than 0.");
	            	}
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

Ext.extend(InversionDimensionWindow, Ext.Window, {
    callback : function() {}
});

//create a number field
createNumberfield = function(label, id, value) {
	return new Ext.form.NumberField({
    	width		: 100,
    	xtype      	: 'numberfield',
        fieldLabel 	: label,
        name       	: id,
        value		: value
    });
};
