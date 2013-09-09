/**
 * A template for generating a eScript gravity inversion example.
 */
Ext.define('ScriptBuilder.templates.EScriptGravityPointTemplate', {
    extend : 'ScriptBuilder.templates.BaseTemplate',

    description : null,
    name : null,

    constructor : function(config) {
        this.callParent(arguments);
    },

    /**
     * See parent description
     */
    requestScript : function(callback) {
        var jobId = this.wizardState.jobId;
        var maxThreads = this.wizardState.ncpus;

        var jobStore = Ext.create('Ext.data.Store', {
            model : 'vegl.models.Download',
            proxy : {
                type : 'ajax',
                url : 'getAllJobInputs.do',
                extraParams : {
                    jobId : jobId
                },
                reader : {
                    type : 'json',
                    root : 'data'
                }
            },
            autoLoad : true
        });

        var ratioY = 1;
        var ratioX = 1;


        var gdalTab = {
                title : 'GDAL Option',
                items : [{
                    xtype : 'numberfield',
                    id  : 'EScriptGravityPointXSize',
                    fieldLabel : 'X Size',
                    anchor : '-20',
                    name : 'xsize',
                    emptyText : 'Please select a dataset first',
                    decimalPrecision : 0,
                    allowBlank : false,
                    minValue : 1,
                    maxValue : 500,
                    step : 1,
                    listeners:{
                        'change' : function(field, newValue, oldValue, eOpts ){
                            var ySizeField = Ext.getCmp('EScriptGravityPointYSize');
                            ySizeField.suspendEvents();
                            ySizeField.setValue(ratioY * newValue);
                            ySizeField.resumeEvents();
                        }
                    },
                    plugins: [{
                        ptype: 'fieldhelptext',
                        text: 'The width of the grid that the selected gravity points will be projected on to'
                    }]
                },{
                    xtype : 'numberfield',
                    id  : 'EScriptGravityPointYSize',
                    fieldLabel : 'Y Size',
                    anchor : '-20',
                    name : 'ysize',
                    emptyText : 'Please select a dataset first',
                    decimalPrecision : 0,
                    allowBlank : false,
                    minValue : 1,
                    maxValue : 500,
                    step : 1,
                    listeners:{
                        'change' : function(field, newValue, oldValue, eOpts ){
                            var xSizeField = Ext.getCmp('EScriptGravityPointXSize');
                            xSizeField.suspendEvents();
                            xSizeField.setValue(ratioX * newValue);
                            xSizeField.resumeEvents();
                        }
                    },
                    plugins: [{
                        ptype: 'fieldhelptext',
                        text: 'The height of the grid that the selected gravity points will be projected on to'
                    }]
                }]
            };


        var escriptTab={
                title : 'EScript Option',
                items : [{
                    xtype : 'numberfield',
                    fieldLabel : 'Max Threads',
                    anchor : '-20',
                    name : 'n-threads',
                    value : maxThreads,
                    allowBlank : false,
                    minValue : 1,
                    plugins: [{
                        ptype: 'fieldhelptext',
                        text: Ext.String.format('The maximum number of execution threads to run (this job will have {0} CPUs)', maxThreads)
                    }]
                },{
                    xtype : 'numberfield',
                    fieldLabel : 'Max Depth',
                    anchor : '-20',
                    name : 'max-depth',
                    value : 40000,
                    allowBlank : false,
                    plugins: [{
                        ptype: 'fieldhelptext',
                        text: 'The maximum depth of the inversion (in meters).'
                    }]
                },{
                    xtype : 'numberfield',
                    fieldLabel : 'Air Buffer',
                    anchor : '-20',
                    name : 'air-buffer',
                    value : 6000,
                    allowBlank : false,
                    plugins: [{
                        ptype: 'fieldhelptext',
                        text: 'buffer zone above data (in meters; 6-10km recommended)'
                    }]
                },{
                    xtype : 'numberfield',
                    fieldLabel : 'Z Mesh Elements',
                    anchor : '-20',
                    name : 'vertical-mesh-elements',
                    value : 25,
                    allowDecimals : false,
                    allowBlank : false,
                    plugins: [{
                        ptype: 'fieldhelptext',
                        text: 'Number of mesh elements in vertical direction (approx 1 element per 2km recommended)'
                    }]
                },{
                    xtype : 'numberfield',
                    fieldLabel : 'X Padding',
                    anchor : '-20',
                    name : 'x-padding',
                    value : 0.2,
                    minValue : 0,
                    maxValue : 1,
                    step : 0.1,
                    allowBlank : false,
                    plugins: [{
                        ptype: 'fieldhelptext',
                        text: 'The amount of horizontal padding in the X direction. This affects end result, about 20% recommended'
                    }]
                },{
                    xtype : 'numberfield',
                    fieldLabel : 'Y Padding',
                    anchor : '-20',
                    name : 'y-padding',
                    value : 0.2,
                    allowBlank : false,
                    minValue : 0,
                    maxValue : 1,
                    step : 0.1,
                    plugins: [{
                        ptype: 'fieldhelptext',
                        text: 'The amount of horizontal padding in the Y direction. This affects end result, about 20% recommended'
                    }]
                },{
                    xtype : 'fieldset',
                    title : 'Trade-off Factors',
                    items : [{
                        xtype : 'displayfield',
                        anchor : '-20',
                        hideLabel : true,
                        fieldStyle : {
                            //'font-style' : 'italic',
                            'font-size' : '10px'
                        },
                        value : 'For a detailed description of these values, please consult the <a href="http://esys.esscc.uq.edu.au/esys13/nightly/inversion/inversion.pdf" target="_blank">escript inversion cookbook.</a>'
                    },{
                        xtype : 'numberfield',
                        fieldLabel : 'Mu Gravity',
                        anchor : '-5',
                        name : 'mu-gravity',
                        value : 0.0,
                        allowBlank : false,
                        labelWidth : 83,
                        minValue : 0,
                        maxValue : 1,
                        step : 0.1
                    }]
                }]

        };

        this._getTemplatedScriptGui(callback, 'escript-gravity-point.py', {
            xtype : 'form',
            width : 500,
            height : 520,
            items : [{
                xtype : 'combo',
                fieldLabel : 'Dataset',
                name : 'inversion-file',
                allowBlank : false,
                valueField : 'localPath',
                displayField : 'localPath',
                anchor : '-20',
                plugins: [{
                    ptype: 'fieldhelptext',
                    text: 'The path to a NetCDF input file.'
                }],
                store : jobStore,
                listeners:{
                    'select': function( combo, records, eOpts ){
                        var x = Math.abs(records[0].get('eastBoundLongitude') - records[0].get('westBoundLongitude'));
                        var y = Math.abs(records[0].get('northBoundLatitude') - records[0].get('southBoundLatitude'));
                        ratioY = y/x;
                        ratioX = x/y;
                        var xSizeField = Ext.getCmp('EScriptGravityPointXSize');
                        var ySizeField = Ext.getCmp('EScriptGravityPointYSize');
                        xSizeField.setValue(150);

                    }
                }

            },{
                xtype : 'tabpanel',
                anchor : '100%',
                plain : true,
                margins : '10',
                border : false,
                defaults : {
                    layout : 'form',
                    padding : '20',
                    border : false
                },
                items : [escriptTab, gdalTab]
            }]
        });
    }

});

