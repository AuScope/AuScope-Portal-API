/**
 * A template for generating a eScript gravity inversion example.
 */
Ext.define('ScriptBuilder.templates.AEMInversionTemplate', {
    extend : 'ScriptBuilder.templates.BaseTemplate',

    description : null,
    name : null,
    
    /** A collection of column mapping values that is keyed by the relevent type name*/
    defaultMappings : {
        'ga:aemsurveys' : {
            'column-xcomponentsecondary' : 'Column 23',
            'column-zcomponentsecondary' : '-Column 61',
            'column-surveynumber' : 'Column 6',
            'column-datenumber' : 'Column 103',
            'column-flightnumber' : 'Column 3',
            'column-linenumber' : 'Column 2',
            'column-fidnumber' : 'Column 4',
            'column-easting' : 'Column 101',
            'column-northing' : 'Column 102',
            'column-groundelevation' : 'Column 13',
            'column-altimeter' : 'Column 10',
            'column-txheight' : 'Column 17',
            'column-txroll' : 'Column 16',
            'column-txpitch' : '-Column 15',
            'column-txrxdx' : 'Column 18',
            'column-txrxdz' : 'Column 19'
        },
        'ga:musgrave_aem' : {
            'column-xcomponentsecondary' : 'Column 15',
            'column-zcomponentsecondary' : '-Column 40',
            'column-surveynumber' : '1411',
            'column-datenumber' : '1999',
            'column-flightnumber' : 'Column 4',
            'column-linenumber' : 'Column 55',
            'column-fidnumber' : 'Column 5',
            'column-easting' : 'Column 2',
            'column-northing' : 'Column 3',
            'column-groundelevation' : 'Column 11',
            'column-altimeter' : 'Column 12',
            'column-txheight' : 'Column 12',
            'column-txroll' : 'Column 14',
            'column-txpitch' : '-Column 13',
            'column-txrxdx' : 'Column 60',
            'column-txrxdz' : 'Column 61'
        }
    },

    constructor : function(config) {
        this.callParent(arguments);
    },

    /**
     * See parent description
     */
    requestScript : function(callback) {
        var jobId = this.wizardState.jobId;
        var maxThreads = this.wizardState.ncpus;
        
        this.downloadStore = Ext.create('Ext.data.Store', {
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
        
        this.propertyStore = Ext.create('Ext.data.Store', {
            model : 'vegl.models.SimpleFeatureProperty',
            proxy : {
                type : 'ajax',
                url : 'describeSimpleFeature.do',
                reader : {
                    type : 'json',
                    root : 'data'
                }
            },
            autoLoad : false
        });
        
        var mappingComboDefaults = {
            emptyText : 'UNAVAILABLE',
            anchor : '-20',
            valueField : 'indexString',
            displayField : 'displayString',
            store : this.propertyStore,            
            queryMode: 'local',
            triggerAction: 'all',
            typeAhead : true
        };
        
        var systemTab = {
            title : 'EM System Options',
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
            },{
                xtype : 'fieldset',
                title : 'EM Column Mappings',
                padding : '0 0 0 20',
                items :[{
                    layout : 'column',
                    border : false,
                    items : [{
                        columnWidth : 0.5,
                        layout : 'anchor',
                        padding : '0 20 0 0',
                        border : false,
                        defaults : mappingComboDefaults,
                        items : [{
                            xtype : 'xcombo',
                            fieldLabel : 'X Primary',
                            labelWidth : 80,
                            name : 'column-xcomponentprimary'
                        },{
                            xtype : 'xcombo',
                            fieldLabel : 'Y Primary',
                            labelWidth : 80,
                            name : 'column-ycomponentprimary'
                        },{
                            xtype : 'xcombo',
                            fieldLabel : 'Z Primary',
                            labelWidth : 80,
                            name : 'column-zcomponentprimary'
                        },{
                            xtype : 'xcombo',
                            fieldLabel : 'X Secondary',
                            labelWidth : 80,
                            name : 'column-xcomponentsecondary'
                        },{
                            xtype : 'xcombo',
                            fieldLabel : 'Y Secondary',
                            labelWidth : 80,
                            name : 'column-ycomponentsecondary'
                        },{
                            xtype : 'xcombo',
                            fieldLabel : 'Z Secondary',
                            labelWidth : 80,
                            name : 'column-zcomponentsecondary'
                        }]
                    },{
                        columnWidth : 0.5,
                        layout : 'anchor',
                        padding : '0 20 0 0',
                        border : false,
                        defaults : mappingComboDefaults,
                        items : [{
                            xtype : 'xcombo',
                            fieldLabel : 'Std Dev X',
                            labelWidth : 75,
                            name : 'column-stddevxwindows'
                        },{
                            xtype : 'xcombo',
                            fieldLabel : 'Std Dev Y',
                            labelWidth : 75,
                            name : 'column-stddevywindows'
                        },{
                            xtype : 'xcombo',
                            fieldLabel : 'Std Dev Z',
                            labelWidth : 75,
                            name : 'column-stddevzwindows'
                        }]
                    }]
                }]
            }]
        };
        
        var optionsTab = {
            title : 'General Options',
            items : [{
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
            },{
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
        
        //The column mappings use a different empty text
        var columnComboDefaults = Ext.apply(Ext.clone(mappingComboDefaults), {emptyText : '0'});
        
        var columnsTab = {
            title : 'Input Data Mappings',
            layout : 'column',
            items : [{
                columnWidth: 0.5,
                layout : 'anchor',
                padding : '0 20 0 0',
                border : false,
                defaults : columnComboDefaults,
                items : [{
                    xtype : 'xcombo',
                    fieldLabel : 'Survey Number',
                    name : 'column-surveynumber'
                },{
                    xtype : 'xcombo',
                    fieldLabel : 'Date Number',
                    name : 'column-datenumber'
                },{
                    xtype : 'xcombo',
                    fieldLabel : 'Flight Number',
                    name : 'column-flightnumber'
                },{
                    xtype : 'xcombo',
                    fieldLabel : 'Line Number',
                    name : 'column-linenumber'
                },{
                    xtype : 'xcombo',
                    fieldLabel : 'Fid Number',
                    name : 'column-fidnumber'
                },{
                    xtype : 'xcombo',
                    fieldLabel : 'Easting',
                    name : 'column-easting'
                },{
                    xtype : 'xcombo',
                    fieldLabel : 'Northing',
                    name : 'column-northing'
                },{
                    xtype : 'xcombo',
                    fieldLabel : 'Ground Elevation',
                    name : 'column-groundelevation'
                },{
                    xtype : 'xcombo',
                    fieldLabel : 'Altimeter',
                    name : 'column-altimeter'
                }]
            },{
                columnWidth: 0.5,
                layout : 'anchor',
                border : false,
                defaults : columnComboDefaults,
                items : [{
                    xtype : 'xcombo',
                    fieldLabel : 'TX Height',
                    name : 'column-txheight'
                },{
                    xtype : 'xcombo',
                    fieldLabel : 'TX Roll',
                    name : 'column-txroll'
                },{
                    xtype : 'xcombo',
                    fieldLabel : 'TX Pitch',
                    name : 'column-txpitch'
                },{
                    xtype : 'xcombo',
                    fieldLabel : 'TX Yaw',
                    name : 'column-txyaw'
                },{
                    xtype : 'xcombo',
                    fieldLabel : 'TXRX DX',
                    name : 'column-txrxdx'
                },{
                    xtype : 'xcombo',
                    fieldLabel : 'TXRX DY',
                    name : 'column-txrxdy'
                },{
                    xtype : 'xcombo',
                    fieldLabel : 'TXRX DZ',
                    name : 'column-txrxdz'
                },{
                    xtype : 'xcombo',
                    fieldLabel : 'RX Roll',
                    name : 'column-rxroll'
                },{
                    xtype : 'xcombo',
                    fieldLabel : 'RX Pitch',
                    name : 'column-rxpitch'
                },{
                    xtype : 'xcombo',
                    fieldLabel : 'RX Yaw',
                    name : 'column-rxyaw'
                }]
            }]
        };
        
        this._getTemplatedScriptGui(callback, 'aem-inversion.py', {
            xtype : 'form',
            width : 710,
            height : 510,
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
                store : this.downloadStore,
                listeners : {
                    //Upon selecting a new dataset
                    select : Ext.bind(function(combo, records) {
                        this._handleDatasetSelection(combo.ownerCt, records.length ? records[0] : null);
                    }, this)
                }
            },{
                xtype : 'numberfield',
                fieldLabel : 'Max Threads',
                anchor : '-20',
                margin : '10',
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
                xtype : 'tabpanel',
                plain : true,
                margins : '10',
                border : false,
                defaults : {
                    layout : 'form',
                    padding : '20',
                    border : false
                },
                items : [systemTab, optionsTab, columnsTab]
            }]
        }, true);
    },
    
    _unsetDataSelection : function(parentForm, clearStore) {
        if (clearStore) {
            this.propertyStore.removeAll();
        }
        
        var mappingColumns = parentForm.query('xcombo');
        Ext.each(mappingColumns, function(xcombo) {
            xcombo.clearValue();
        });
    },
    
    _loadDefaultValues : function(parentForm, typeName) {
        this._unsetDataSelection(parentForm, false);
        
        var mapping = this.defaultMappings[typeName];
        if (!mapping) {
            return;
        }
        
        parentForm.getForm().setValues(mapping);
    },
    
    _handleDatasetSelection : function(parentForm, download) {
        if (!download) {
            this._unsetDataSelection(parentForm, true);
            return;
        }
        
        //Extract our URL + params
        var url = download.get('url');
        var parts = url.split('?');
        if (parts.length != 2) {
            this._unsetDataSelection(parentForm, true);
            return;
        }
        
        //Make sure we have a WFS URL
        var params = Ext.Object.fromQueryString(parts[1]);
        if (params.service != 'WFS' || !params.typeName) {
            this._unsetDataSelection(parentForm, true);
            return;
        }
        
        //Update our state
        var ajaxProxy = this.propertyStore.getProxy();
        ajaxProxy.extraParams.serviceUrl = parts[0];
        ajaxProxy.extraParams.typeName = params.typeName;
        this.propertyStore.load({
            scope : this,
            callback : Ext.bind(this._loadDefaultValues, this, [parentForm, params.typeName])
        });
    }

});

