/**
 * Generates a GUI for a template entry from the SCM.
 */
function _setItemField(variable, vKey, item, iKey, fn) {
    var val = variable[vKey];

    if (val !== undefined) {
        if (fn !== undefined) {
            val = fn(val);
        }
        item[iKey] = val;
    }
}


Ext.define('ScriptBuilder.templates.DynamicTemplate', {
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
        var items = [];
        var item;

        if (this.entry) {

            if (this.entry.variables) {

	            this.entry.variables.forEach(function(variable, index) {
	                item = {
	                    id: variable.name,
	                    name: variable.name,
	                    fieldLabel: variable.label,
	                    anchor: '-20',
	                    allowBlank: variable.optional
	                };

	                // Add help text if available
	                _setItemField(variable, 'description', item, 'plugins',
	                                   function(value) {
	                                       return [{
	                                           ptype: 'fieldhelptext',
	                                           text: value
	                                       }];
	                                   });

	                // Map var type to item type
	                if (variable.values) {
	                    item.xtype = 'combo';
	                    item.forceSelection = !variable.optional;
	                    item.queryMode = 'local';
	                    item.valueField = 'value';
	                    item.displayField = 'value';
	                    item.store = Ext.create('Ext.data.Store', {
	                        fields: ['value'],
	                        data: variable.values.map(function(v) {
	                            return {'value': v};
	                        })
	                    });
	                    // Default to the first entry in the list - this
	                    // will be overidden by a 'default' entry below
	                    item.value = variable.values[0];
	                }
	                else {
	                    switch (variable.type) {
	                    case 'int':
	                        item.xtype = 'numberfield';
	                        item.allowDecimals = false;
	                        break;
	                    case 'double':
	                        item.xtype = 'numberfield';
	                        item.decimalPrecision = 10;
	                        break;
	                    case 'string':
	                        item.xtype = 'textfield';
	                        break;
	                    case 'random-int':
	                        item.xtype = 'numberfield';
	                        var min = variable.min;
	                        var max = variable.max;
	                        item.value =
	                            Math.floor(Math.random() * (max - min)) + min;
	                        break;
	                    case 'file':
	                        item.xtype = 'combo';
	                        item.allowBlank = false;
	                        item.valueField = 'localPath';
	                        item.displayField = 'localPath';
	                        item.store = Ext.create('Ext.data.Store', {
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
	                        break;
	                    default:
	                        item.xtype = 'textfield';
	                    };
	                }

	                _setItemField(variable, 'default', item, 'value');

	                // Set constraints if applicable
	                _setItemField(variable, 'min', item, 'minValue');
	                _setItemField(variable, 'max', item, 'maxValue');
	                _setItemField(variable, 'step', item, 'step');

	                // If this is a numeric field with a minimum value and
	                // no default, default to the min value.
	                if (item.xtype == 'numberfield' && item.value === undefined) {
	                    if (variable.min !== undefined) {
	                        item.value = variable.min;
	                    }
	                    else {
	                        item.value = 1;
	                    }
	                }

	                items.push(item);
	            });
        	}
        }

        this._getTemplatedScriptGui(callback, this.entry.template, {
            xtype : 'form',
            width : 500,
            height : 520,
            autoScroll: true,
            items : [{
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
                items : [{
                    title: 'Configuration',
                    items: items
                }]
            }]
        });
    }
});
