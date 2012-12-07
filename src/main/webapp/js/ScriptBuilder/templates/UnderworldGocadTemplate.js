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

        var jobInputsStore = Ext.create('Ext.data.Store', {
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

        //
        this._getTemplatedScriptGui(callback, 'underworld-gocad.py', {
            xtype : 'form',
            width : 400,
            height : 300,
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
                store : jobInputsStore
            },{
                xtype : 'combo',
                fieldLabel : 'Voxel Key',
                name : 'voxet-key',
                allowBlank : false,
                valueField : 'localPath',
                displayField : 'localPath',
                anchor : '-20',
                plugins: [{
                    ptype: 'fieldhelptext',
                    text: 'The file path to the CSV key describing the above voxel set.'
                }],
                store : jobInputsStore
            },{
                xtype : 'textfield',
                fieldLabel : 'Materials Property',
                anchor : '100%',
                name : 'materials-property',
                value : 'Geology',
                allowBlank : false
            },{
                xtype : 'fieldset',
                anchor : '100%',
                checkboxToggle : true,
                checkboxName : 'use-boundary-conditions',
                title : 'Lower Boundary Conditions',
                listeners : {
                    collapse : this.onLowerBoundaryDisable,
                    expand : Ext.bind(this.onLowerBoundaryEnable, this)
                },
                items : [{
                    xtype : 'radiogroup',
                    itemId : 'bc-radio',
                    hideLabel : true,
                    listeners : {
                        change : this.onLowerBoundaryChange
                    },
                    items : [
                        { boxLabel: 'Flux', inputValue: 'flux', name: 'lb-condition', checked: true },
                        { boxLabel: 'Temperature', inputValue: 'temp', name: 'lb-condition' }]
                },{
                    xtype : 'numberfield',
                    name : 'lower-boundary-flux',
                    anchor : '100%',
                    fieldLabel : 'Flux',
                    itemId : 'lower-boundary-flux',
                    decimalPrecision : 6,
                    plugins: [{
                        ptype: 'fieldhelptext',
                        text: 'The lower boundary flux condition value.'
                    }]
                },{
                    xtype : 'hiddenfield',
                    name : 'lower-boundary-flux',
                    itemId : 'lower-boundary-flux-empty',
                    disabled : true,
                    value : ''
                },{
                    xtype : 'numberfield',
                    name : 'lower-boundary-temp',
                    anchor : '100%',
                    fieldLabel : 'Temperature',
                    itemId : 'lower-boundary-temp',
                    decimalPrecision : 6,
                    hidden : true,
                    disabled : true,
                    plugins: [{
                        ptype: 'fieldhelptext',
                        text: 'The lower boundary temperature condition value.'
                    }]
                },{
                    xtype : 'hiddenfield',
                    name : 'lower-boundary-temp',
                    itemId : 'lower-boundary-temp-empty',
                    value : ''
                }]
            }]
        });
    },

    onLowerBoundaryDisable : function(fieldSet) {
        var flux = fieldSet.getComponent('lower-boundary-flux');
        var fluxEmpty = fieldSet.getComponent('lower-boundary-flux-empty');
        var temp = fieldSet.getComponent('lower-boundary-temp');
        var tempEmpty = fieldSet.getComponent('lower-boundary-temp-empty');

        temp.disable();
        flux.disable();
        tempEmpty.enable();
        fluxEmpty.enable();
    },

    onLowerBoundaryEnable : function(fieldSet) {
        var radioGroup = fieldSet.getComponent('bc-radio');
        this.onLowerBoundaryChange(radioGroup, radioGroup.getValue());
    },

    onLowerBoundaryChange : function(radioGroup, newVal, oldVal) {
        var flux = radioGroup.ownerCt.getComponent('lower-boundary-flux');
        var fluxEmpty = radioGroup.ownerCt.getComponent('lower-boundary-flux-empty');
        var temp = radioGroup.ownerCt.getComponent('lower-boundary-temp');
        var tempEmpty = radioGroup.ownerCt.getComponent('lower-boundary-temp-empty');
        if (newVal['lb-condition'] === 'flux') {
            temp.hide();
            temp.disable();
            tempEmpty.enable();
            flux.enable();
            flux.show();
            fluxEmpty.disable();
        } else {
            flux.hide();
            flux.disable();
            fluxEmpty.enable();
            temp.enable();
            temp.show();
            tempEmpty.disable();
        }
    }
});

