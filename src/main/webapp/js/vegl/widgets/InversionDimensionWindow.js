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
Ext.define('vegl.widgets.InversionDimensionWindow', {
    extend : 'Ext.Window',

    constructor : function(config) {

        this.callback = config.callback;
        var parentWindow = this;
        var scope = config.scope;

        //subset file type selection values
        var mgaZoneStore = Ext.create('Ext.data.Store', {
            fields : ['type', 'value'],
            data : [{type : '49', value : 49},
                    {type : '50', value : 50},
                    {type : '51', value : 51},
                    {type : '52', value : 52},
                    {type : '53', value : 53},
                    {type : '54', value : 54},
                    {type : '55', value : 55},
                    {type : '56', value : 56}]
        });

        var mgaZoneCombo = Ext.create('Ext.form.ComboBox', {
            anchor         : '100%',
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

        var xCellDim = Ext.create('Ext.form.NumberField', {
            anchor      : '100%',
            fieldLabel  : 'X Cell Dimension',
            name        : 'xCellDim',
            value       : 0
        });
        var yCellDim = Ext.create('Ext.form.NumberField', {
            anchor      : '100%',
            fieldLabel  : 'Y Cell Dimension',
            name        : 'yCellDim',
            value       : 0
        });
        var zCellDim = Ext.create('Ext.form.NumberField', {
            anchor      : '100%',
            fieldLabel  : 'Z Cell Dimension',
            name        : 'zCellDim',
            value       : 0
        });
        var maxDepth = Ext.create('Ext.form.NumberField', {
            anchor      : '100%',
            fieldLabel  : 'Max Depth',
            name        : 'maxDepth',
            value       : 0
        });

        Ext.apply(config, {
            border          : true,
            resizable       : true,
            layout          : 'fit',
            modal           : true,
            plain           : false,
            buttonAlign     : 'right',
            title           : 'Enter Inversion Cell Dimensions',
            width           : 250,
            autoHeight 		: true,
            scope 			: this,
            items 		: [{
                xtype : 'form',
                items : [mgaZoneCombo,
                         xCellDim,
                         yCellDim,
                         zCellDim,
                         maxDepth]
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

        this.callParent(arguments);
    }
});
