/**
 * A template for generating a eScript joint gravity/magnetic inversion example.
 */
Ext.define('ScriptBuilder.templates.EScriptJointTemplate', {
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

        this._getTemplatedScriptGui(callback, 'escript-joint.py', {
            xtype : 'form',
            width : 575,
            height : 520,
            items : [{
                xtype : 'combo',
                fieldLabel : 'Magnetic Dataset',
                name : 'magnetic-file',
                allowBlank : false,
                valueField : 'localPath',
                displayField : 'localPath',
                anchor : '-20',
                plugins: [{
                    ptype: 'fieldhelptext',
                    text: 'The path to a NetCDF input file.'
                }],
                store : Ext.create('Ext.data.Store', {
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
                })
            },{
                xtype : 'combo',
                fieldLabel : 'Gravity Dataset',
                name : 'gravity-file',
                allowBlank : false,
                valueField : 'localPath',
                displayField : 'localPath',
                anchor : '-20',
                plugins: [{
                    ptype: 'fieldhelptext',
                    text: 'The path to a NetCDF input file.'
                }],
                store : Ext.create('Ext.data.Store', {
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
                })
            },{
                xtype: 'fieldcontainer',
                fieldLabel: 'Background B',
                msgTarget: 'under',
                anchor: '100%',
                layout: {
                    type: 'hbox',
                    defaultMargins: {top: 0, right: 5, bottom: 0, left: 0}
                },
                defaults: {
                    hideLabel: true,
                    decimalPrecision : 8
                },
                items: [
                    {xtype: 'displayfield', value: '('},
                    {xtype: 'numberfield',  name: 'bb-north', width: 80, value: 29800},
                    {xtype: 'displayfield', value: 'North ) ('},
                    {xtype: 'numberfield',  name: 'bb-east', width: 80, value: 2780},
                    {xtype: 'displayfield', value: 'East ) ('},
                    {xtype: 'numberfield',  name: 'bb-vertical', width: 80, value: -44000},
                    {xtype: 'displayfield', value: 'Vertical )'}
                ],
                plugins: [{
                    ptype: 'fieldhelptext',
                    text: 'The background magnetic flux density in <b>nano Tesla</b>. Values for Australia can be calculated at the <a href="http://www.ga.gov.au/oracle/geomag/agrfform.jsp" target="_blank">Geoscience Australia website</a>'
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
                margin : 5,
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
                },{
                    xtype : 'numberfield',
                    fieldLabel : 'Mu Magnetic',
                    anchor : '-5',
                    name : 'mu-magnetic',
                    value : 0.0,
                    allowBlank : false,
                    minValue : 0,
                    maxValue : 1,
                    labelWidth : 83,
                    step : 0.1
                }]
            }]
        });
    }

});

