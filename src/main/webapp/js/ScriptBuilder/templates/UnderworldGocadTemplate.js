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
        var maxThreads = this.wizardState.ncpus;

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
            width : 450,
            height : 510,
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
                xtype : 'combo',
                fieldLabel : 'Voxel Set',
                name : 'voxet-filename',
                allowBlank : false,
                valueField : 'localPath',
                displayField : 'localPath',
                emptyText : '(eg: *.vo files)',
                submitEmptyText : false,
                anchor : '-20',
                plugins: [{
                    ptype: 'fieldhelptext',
                    text: 'The file path to the voxel set input'
                }],
                store : jobInputsStore
            },{
                xtype : 'combo',
                fieldLabel : 'Voxel Key',
                name : 'voxet-key',
                allowBlank : false,
                valueField : 'localPath',
                displayField : 'localPath',
                emptyText : '(eg: KeyToVoxet.csv)',
                submitEmptyText : false,
                anchor : '-20',
                plugins: [{
                    ptype: 'fieldhelptext',
                    text: 'The file path to the CSV key describing the above voxel set.'
                }],
                store : jobInputsStore
            },{
                xtype : 'textfield',
                fieldLabel : 'Materials Property',
                anchor : '-20',
                name : 'materials-property',
                value : 'Geology',
                allowBlank : false
            },{
                xtype : 'fieldset',
                anchor : '100%',
                title : 'Compute Resolution',
                margin : '0 20 0 20',
                items : [{
                    xtype : 'numberfield',
                    name : 'n-fem-x',
                    itemId : 'n-fem-x',
                    anchor : '100%',
                    fieldLabel : 'X Axis Elements',
                    allowDecimals : false,
                    value : 16,
                    plugins: [{
                        ptype: 'fieldhelptext',
                        text: 'Number of Finite Element Mesh elements along X axis'
                    }]
                },{
                    xtype : 'numberfield',
                    name : 'n-fem-y',
                    itemId : 'n-fem-y',
                    anchor : '100%',
                    fieldLabel : 'Y Axis Elements',
                    allowDecimals : false,
                    value : 16,
                    plugins: [{
                        ptype: 'fieldhelptext',
                        text: 'Number of Finite Element Mesh elements along Y axis'
                    }]
                },{
                    xtype : 'numberfield',
                    name : 'n-fem-z',
                    itemId : 'n-fem-z',
                    anchor : '100%',
                    fieldLabel : 'Z Axis Elements',
                    allowDecimals : false,
                    value : 16,
                    plugins: [{
                        ptype: 'fieldhelptext',
                        text: 'Number of Finite Element Mesh elements along Z axis'
                    }]
                }]
            },{
                xtype : 'fieldset',
                anchor : '100%',
                margin : '0 20 20 20',
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
                    value : 0.03,
                    step : 0.01,
                    plugins: [{
                        ptype: 'fieldhelptext',
                        text: 'The lower boundary flux condition value in W/m^2'
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

