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
Ext.define('vegl.widgets.UTMBoundsInfoWindow', {
    extend : 'Ext.Window',

    constructor : function(config) {

        this.callback = config.callback;
        var parentWindow = this;
        var scope = config.scope;

        var minEast = Ext.create('Ext.form.NumberField', {
            anchor      : '100%',
            fieldLabel  : 'Min Easting',
            name        : 'minEast',
            value       : config.eastingArray[0]
        });
        var maxEast = Ext.create('Ext.form.NumberField', {
            anchor      : '100%',
            fieldLabel  : 'Max Easting',
            name        : 'maxEast',
            value       : config.eastingArray[1]
        });
        var minNorth = Ext.create('Ext.form.NumberField', {
            anchor      : '100%',
            fieldLabel  : 'Min Northing',
            name        : 'minNorth',
            value       : config.northingArray[0]
        });
        var maxNorth = Ext.create('Ext.form.NumberField', {
            anchor      : '100%',
            fieldLabel  : 'Max Northing',
            name        : 'maxNorth',
            value       : config.northingArray[1]
        });
        var maxDepth = Ext.create('Ext.form.NumberField', {
            anchor      : '100%',
            fieldLabel  : 'Max Depth',
            name        : 'maxDepth',
            value       : config.depthArray[1]
        });

        Ext.apply(config, {
            border          : true,
            resizable       : true,
            modal           : true,
            plain           : false,
            buttonAlign     : 'right',
            title           : 'Projected UTM Co-ordinates',
            width           : 250,
            autoHeight 		: true,
            scope 			: this,
            items 		: [minEast,
                           maxEast,
                           minNorth,
                           maxNorth,
                           maxDepth],
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

        this.callParent(arguments);
    }
});
