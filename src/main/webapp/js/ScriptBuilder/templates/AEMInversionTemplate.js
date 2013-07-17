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

        this._getTemplatedScriptGui(callback, 'aem-inversion.py', {
            xtype : 'form',
            width : 450,
            height : 390,
            items : [{
                xtype : 'combo',
                fieldLabel : 'Dataset',
                name : 'InputFile',
                allowBlank : false,
                valueField : 'localPath',
                displayField : 'localPath',
                anchor : '-20',
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
            }]
        });
    }

});

