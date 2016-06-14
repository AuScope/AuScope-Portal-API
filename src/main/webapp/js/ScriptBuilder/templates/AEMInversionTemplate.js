/**
 * A template for generating a eScript gravity inversion example.
 */
Ext.define('ScriptBuilder.templates.AEMInversionTemplate', {
    extend : 'ScriptBuilder.templates.BaseTemplate',

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
                url : 'secure/getAllJobInputs.do',
                extraParams : {
                    jobId : jobId
                },
                reader : {
                    type : 'json',
                    rootProperty : 'data'
                }
            },
            autoLoad : true
        });
        
        var templateStore = Ext.create('Ext.data.Store', {
            fields: ['display', 'value'],
            data : [
                {"display":"bhmar-skytem", "value":"aem-bhmar-skytem-galeisbstdem.py"},
                {"display":"Frome tempest do not solve geometry", "value":"aem-Frome-tempest-galeisbstdem-do-not-solve-geometry.py"},
                {"display":"Frome tempest solve rxpitch and offsets", "value":"aem-Frome-tempest-galeisbstdem-solve-rxpitch-and-offsets.py"},
                {"display":"Frome tempest  solve rxpitch", "value":"aem-Frome-tempest-galeisbstdem-solve-rxpitch.py"},
                {"display":"thomson vtem", "value":"aem-thomson-vtem-galeisbstdem.py"}
            ]
        });
        
        var templateCombo = Ext.create('Ext.form.ComboBox', {
            fieldLabel : 'Select Template',
            anchor : '-20',
            margin : '10',
            name : 'templateSelection',
            store : templateStore,
            displayField:'display',
            valueField : 'value',           
            plugins: [{
                ptype: 'fieldhelptext',
                text: Ext.String.format('Select from the drop down box the usual template control file', maxThreads)
            }]
        });
        
        
        var popup = Ext.create('Ext.window.Window', {
            title : 'Enter Parameters',
            layout : 'fit',
            modal : true,
            width : 500,
            height : 250,
            items : [{
                xtype : 'form',               
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
                            this._handleDatasetSelection(combo.ownerCt, records ? records : null);
                        }, this)
                    }
                },templateCombo]
            }],
            buttons:[{
                xtype: 'button',
                text: 'Apply Template',
                scope : this,
                iconCls : 'add',
                handler: function(button) {
                    var parent = button.findParentByType('window');
                    var panel = parent.getComponent(0);

                    if (panel.getForm().isValid()) {
                        var additionalParams = panel.getForm().getValues(false, false, false, false);

                        //We need to close our window when finished so we wrap callback
                        //with a function that ensures closing BEFORE the callback is executed
                        this._getTemplatedScript(function(status, script) {
                            parent.ignoreCloseEvent = true;
                            parent.close();
                            callback(status, script);
                        }, templateCombo.getValue(), additionalParams);
                    }
                }
            }],
            listeners : {
                close : function(popup) {
                    if (!popup.ignoreCloseEvent) {
                        callback(ScriptBuilder.templates.BaseTemplate.TEMPLATE_RESULT_CANCELLED, null);
                    }
                }
            }
        });

        popup.show();
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

