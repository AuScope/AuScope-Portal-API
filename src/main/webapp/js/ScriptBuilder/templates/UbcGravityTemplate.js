/**
 * A template for generating a UBC gravity inversion example.
 */
Ext.define('ScriptBuilder.templates.UbcGravityTemplate', {
    extend : 'ScriptBuilder.templates.BaseTemplate',

    description : null,
    name : null,

    constructor : function(config) {
        this.callParent(arguments);
    },

    getScriptName : function() {
        return 'ubc-gravity.py';
    },

    /**
     * See parent description
     */
    requestScript : function(callback) {
        var jobId = this.wizardState.jobId;
        var maxThreads = this.wizardState.ncpus;

        this._getTemplatedScriptGui(callback, this.getScriptName(), {
            xtype : 'form',
            width : 720,
            height : 415,
            items : [{
                xtype : 'combo',
                itemId : 'dataset-combo',
                fieldLabel : 'Dataset',
                name : 'job-input-file',
                allowBlank : false,
                valueField : 'localPath',
                displayField : 'localPath',
                anchor: '-10',
                margin : '5 10 2 10',
                plugins: [{
                    ptype: 'fieldhelptext',
                    text: 'The path to a CSV input file.'
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
                }),
                listeners : {
                    change : Ext.bind(this.onComboChange, this)
                }
            },{
                xtype : 'numberfield',
                fieldLabel : 'Max Threads',
                anchor : '-10',
                name : 'n-threads',
                margin : '5 10 2 10',
                value : maxThreads,
                allowBlank : false,
                minValue : 1,
                plugins: [{
                    ptype: 'fieldhelptext',
                    text: Ext.String.format('The maximum number of execution threads to run (this job will have {0} CPUs)', maxThreads)
                }]
            },{
                xtype : 'fieldset',
                anchor: '-10',
                title : 'Spatial Region',
                margin : '5 10 2 10',
                items: [{
                    xtype: 'fieldcontainer',
                    fieldLabel: 'Bounding Box',
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
                        {xtype: 'numberfield',  name: 'job-selection-maxnorth', width: 80, allowBlank: false, itemId : 'north-number'},
                        {xtype: 'displayfield', value: 'North ) ('},
                        {xtype: 'numberfield',  name: 'job-selection-mineast', width: 80, allowBlank: false, itemId : 'west-number'},
                        {xtype: 'displayfield', value: 'West ) ('},
                        {xtype: 'numberfield',  name: 'job-selection-minnorth', width: 80, allowBlank: false, itemId : 'south-number'},
                        {xtype: 'displayfield', value: 'South ) ('},
                        {xtype: 'numberfield',  name: 'job-selection-maxeast', width: 80, allowBlank: false, itemId : 'east-number'},
                        {xtype: 'displayfield', value: 'East )'}
                    ],
                    plugins: [{
                        ptype: 'fieldhelptext',
                        text: 'The spatial region represented by the dataset. The values represent a UTM bounding box.'
                    }]
                },{
                    xtype: 'numberfield',
                    fieldLabel : 'MGA Zone',
                    itemId : 'mga-number',
                    name : 'job-mgazone',
                    anchor: '100%',
                    allowBlank : false,
                    plugins: [{
                        ptype: 'fieldhelptext',
                        text: 'The UTM MGA zone associated with the above bounding box.'
                    }]
                }]
            },{
                xtype : 'fieldset',
                anchor: '-10',
                title : 'Inversion details',
                margin : '5 10 2 10',
                items: [{
                    xtype: 'numberfield',
                    fieldLabel : 'Cell Size (X)',
                    anchor: '100%',
                    name : 'job-cellx',
                    allowBlank : false
                },{
                    xtype: 'numberfield',
                    fieldLabel : 'Cell Size (Y)',
                    anchor: '100%',
                    name : 'job-celly',
                    allowBlank : false
                },{
                    xtype: 'numberfield',
                    fieldLabel : 'Cell Size (Z)',
                    anchor: '100%',
                    name : 'job-cellz',
                    allowBlank : false
                },{
                    xtype: 'numberfield',
                    fieldLabel : 'Inversion Depth',
                    name : 'job-inversiondepth',
                    anchor: '100%',
                    allowBlank : false
                }]
            }]
        });
    },

    /**
     * Looks up the record (if any) that is current selected in a combo box
     */
    getComboRecordFromValue : function(combo, value) {
        var ds = combo.getStore();
        var foundRecord = null;
        ds.each(function(rec) {
            if (rec.get(combo.valueField) === value) {
                foundRecord = rec;
                return false;
            }
            return true;
        });

        return foundRecord;
    },

    onComboChange : function(combo, newValue, oldValue) {
        var parentForm = combo.findParentByType('form');
        var download = this.getComboRecordFromValue(combo, newValue);

        //If we don't have params - set everything to null
        if (!download || !download.get('northBoundLatitude')) {
            parentForm.queryById('mga-number').setValue('');
            parentForm.queryById('north-number').setValue('');
            parentForm.queryById('south-number').setValue('');
            parentForm.queryById('west-number').setValue('');
            parentForm.queryById('east-number').setValue('');
            return;
        }

        var loadMask = new Ext.LoadMask(parentForm.getEl(), {
            removeMask : true
        });
        loadMask.show();

        //Request for bbox reprojection and update the params
        Ext.Ajax.request({
            url : 'projectBBoxToUtm.do',
            params : {
                northBoundLatitude : download.get('northBoundLatitude'),
                southBoundLatitude : download.get('southBoundLatitude'),
                eastBoundLongitude : download.get('eastBoundLongitude'),
                westBoundLongitude : download.get('westBoundLongitude')
            },
            callback : function(options, success, response) {
                loadMask.hide();
                if (!success) {
                    return;
                }

                var responseObj = Ext.JSON.decode(response.responseText);
                if (!responseObj.success) {
                    return;
                }

                var mgaZone = responseObj.data.mgaZone;
                var minNorthing = responseObj.data.minNorthing;
                var maxNorthing = responseObj.data.maxNorthing;
                var minEasting = responseObj.data.minEasting;
                var maxEasting = responseObj.data.maxEasting;

                parentForm.queryById('mga-number').setValue(mgaZone);
                parentForm.queryById('north-number').setValue(maxNorthing);
                parentForm.queryById('south-number').setValue(minNorthing);
                parentForm.queryById('west-number').setValue(minEasting);
                parentForm.queryById('east-number').setValue(maxEasting);
            }
        });
    }

});

