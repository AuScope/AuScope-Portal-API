/**
 * A template for generating a UBC gravity inversion example.
 */
Ext.define('ScriptBuilder.templates.UnderworldGocadTemplate', {
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

        this._getTemplatedScriptGui(callback, 'underworld-gocad.py', {
            xtype : 'form',
            width : 400,
            height : 200,
            items : [{
                xtype : 'combo',
                fieldLabel : 'Voxel Set',
                name : 'voxet-filename',
                allowBlank : false,
                valueField : 'localPath',
                displayField : 'localPath',
                anchor : '-20',
                plugins: [{
                    ptype: 'fieldhelptext',
                    text: 'The file path to the voxel set input.'
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
                xtype : 'textfield',
                fieldLabel : 'Conductivity Property',
                anchor : '100%',
                name : 'conductivity-property',
                value : 'Geology',
                allowBlank : false
            }]
        });
    }

});

