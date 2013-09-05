/**
 * A template for generating a eScript gravity inversion example.
 */
Ext.define('ScriptBuilder.templates.AEMInversionTemplate', {
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
        
        var processTab = {
            title : 'Process Options',
            defaults : {
                labelWidth : 150
            },
            items : [{
                xtype : 'numberfield',
                fieldLabel : 'Max Threads',
                anchor : '-20',
                name : 'n-threads',
                value : maxThreads,
                allowBlank : false,
                allowDecimals : false,
                minValue : 1,
                plugins: [{
                    ptype: 'fieldhelptext',
                    text: Ext.String.format('The maximum number of execution threads to run (this job will have {0} CPUs)', maxThreads)
                }]
            },{
                xtype : 'numberfield',
                fieldLabel : 'Min Phi D',
                anchor : '-20',
                name : 'min-phi-d',
                value : 1.0,
                allowBlank : false
            },{
                xtype : 'numberfield',
                fieldLabel : 'Min % Improvement',
                anchor : '-20',
                name : 'min-percentage-imp',
                value : 1.0,
                allowBlank : false,
                plugins: [{
                    ptype: 'fieldhelptext',
                    text: 'The minimum improvement on results (as a percentage) an iteration can have and still continue.'
                }]
            },{
                xtype : 'numberfield',
                fieldLabel : 'Max Iterations',
                anchor : '-20',
                name : 'max-iterations',
                value : 1000,
                allowDecimals : false,
                allowBlank : false,
                plugins: [{
                    ptype: 'fieldhelptext',
                    text: 'The maximum number of iterations before termination.'
                }]
            }] 
        };
        var alphaTab = {
            title : 'Process Options',
            items : [{
                xtype : 'numberfield',
                fieldLabel : 'Conductivity',
                anchor : '-20',
                name : 'alpha-conductivity',
                value : 1.0,
                allowBlank : false
            },{
                xtype : 'numberfield',
                fieldLabel : 'Thickness',
                anchor : '-20',
                name : 'alpha-thickness',
                value : 0.0,
                allowBlank : false
            },{
                xtype : 'numberfield',
                fieldLabel : 'Geometry',
                anchor : '-20',
                name : 'alpha-geometry',
                value : 1.0,
                allowBlank : false
            },{
                xtype : 'numberfield',
                fieldLabel : 'Smoothness',
                anchor : '-20',
                name : 'alpha-smoothness',
                value : 1000000,
                allowBlank : false,
                plugins: [{
                    ptype: 'fieldhelptext',
                    text: 'Set to 0 for no vertical conductivity smoothing'
                }]
            }]
        };
        var solverTab = {
            title : 'Solver Flags',
            items : [{
                xtype : 'checkboxgroup',
                columns : 2,
                anchor : '100%',
                defaults : {
                    inputValue : 'yes',
                    uncheckedValue : 'no'
                },
                items : [
                    { boxLabel : 'Solve Conductivity', name : 'solve-conductivity', checked : true},
                    { boxLabel : 'Solve Thickness', name : 'solve-thickness', checked : false},
                    { boxLabel : 'Solve TX_Height', name : 'solve-txheight', checked : false},
                    { boxLabel : 'Solve TX_Roll', name : 'solve-txroll', checked : false},
                    { boxLabel : 'Solve TX_Pitch', name : 'solve-txpitch', checked : false},
                    { boxLabel : 'Solve TX_Yaw', name : 'solve-txyaw', checked : false},
                    { boxLabel : 'Solve TXRX_DX', name : 'solve-txrxdx', checked : true},
                    { boxLabel : 'Solve TXRX_DY', name : 'solve-txrxdy', checked : false},
                    { boxLabel : 'Solve TXRX_DZ', name : 'solve-txrxdz', checked : true},
                    { boxLabel : 'Solve RX_Roll', name : 'solve-rxroll', checked : false},
                    { boxLabel : 'Solve RX_Pitch', name : 'solve-rxpitch', checked : true},
                    { boxLabel : 'Solve RX_Yaw', name : 'solve-rxyaw', checked : false}
                ]
            }]
        };
        
        this._getTemplatedScriptGui(callback, 'aem-inversion.py', {
            xtype : 'form',
            width : 450,
            height : 340,
            items : [{
                xtype : 'combo',
                fieldLabel : 'Dataset',
                name : 'wfs-input-xml',
                allowBlank : false,
                valueField : 'localPath',
                displayField : 'localPath',
                anchor : '-20',
                margin : '10',
                plugins: [{
                    ptype: 'fieldhelptext',
                    text: 'The path to the input file.'
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
                items : [processTab, alphaTab, solverTab]
            }]
        });
    }

});

