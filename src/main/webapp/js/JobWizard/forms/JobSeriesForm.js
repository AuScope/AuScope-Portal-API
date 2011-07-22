/**
 * Job wizard form for selecting/creating a job series
 * 
 * Author - Josh Vote
 */
Ext.namespace("JobBuilder");

JobSeriesForm =  Ext.extend(BaseJobWizardForm, {
	
	/**
	 * Creates a new JobSeriesForm form configured to write/read to the specified global state
	 */
	constructor: function(wizardState) {
		var jobSeriesObj = this;
		var mySeriesStore = new Ext.data.JsonStore({
	        url: 'mySeries.do',
	        root: 'data',
	        autoLoad: true,
	        fields: [
	            { name: 'id', type: 'int' },
	            { name: 'name', type: 'string' },
	            { name: 'description', type: 'string' },
	            { name: 'user', type: 'string'}
	        ],
	        listeners: { 'loadexception': jobSeriesObj.fireEvent.createDelegate(jobSeriesObj, ['jobWizardLoadException']) }
	    });
		
		JobSeriesForm.superclass.constructor.call(this, {
			wizardState : wizardState,
			bodyStyle: 'padding:10px;',
	        frame: true,
	        defaults: { anchor: "100%" },
	        monitorValid: true,
	        items: [{
	            xtype: 'label',
	            text: 'A grid job is always part of a job series even if it is a single job. Please specify if you want to create a new series for this job or add it to an existing one:'
	        }, {
	            xtype: 'radiogroup',
	            style: 'padding:10px;',
	            hideLabel: true,
	            items: [{
	                name: 'sCreateSelect',
	                id: 'selExistRadio',
	                boxLabel: 'Select existing series',
	                inputValue: 0,
	                checked: true,
	                handler: jobSeriesObj.onSwitchCreateSelect
	            }, {
	                name: 'sCreateSelect',
	                id: 'createNewRadio',
	                boxLabel: 'Create new series',
	                inputValue: 1
	            }]
	        }, {
	            xtype: 'fieldset',
	            title: 'Series properties',
	            collapsible: false,
	            anchor: '100% -80',
	            defaults: { anchor: '100%' },
	            items: [{
	                xtype: 'combo',
	                id: 'seriesCombo',
	                name: 'seriesName',
	                editable: false,
	                mode: 'local',
	                minLength: 3,
	                allowBlank: false,
	                maskRe: /[^\W]/,
	                store: mySeriesStore,
	                triggerAction: 'all',
	                displayField: 'name',
	                tpl: '<tpl for="."><div ext:qtip="{description}" class="x-combo-list-item">{name}</div></tpl>',
	                fieldLabel: 'Series Name',
	                listeners : {
	            		select : function(combo, record, index) {
	                        var descArea = Ext.getCmp('seriesDesc');
	                        descArea.setRawValue(record.get('description'));
	                        jobSeriesObj.wizardState.seriesId = record.get('id');
	                    }
	            	}
	            }, {
	                xtype: 'textarea',
	                id: 'seriesDesc',
	                name: 'seriesDesc',
	                anchor: '100% -30',
	                disabled: true,
	                fieldLabel: 'Description',
	                blankText: 'Please provide a meaningful description...',
	                allowBlank: false
	            }]
	        }]
		});
	},
	
	onSwitchCreateSelect : function(checkbox, checked) {
        if (checked) {
            var combo = Ext.getCmp('seriesCombo');
            var descText = Ext.getCmp('seriesDesc');
            combo.reset();
            combo.setEditable(false);
            combo.getStore().reload();
            descText.setDisabled(true);
            descText.reset();
        } else {
            var combo = Ext.getCmp('seriesCombo');
            var descText = Ext.getCmp('seriesDesc');
            combo.reset();
            combo.setEditable(true);
            combo.getStore().removeAll();
            descText.setDisabled(false);
            descText.reset();
        }
    },
    
    getTitle : function() {
		return "Select Job Series...";
	},
    
    beginValidation : function(callback) {
    	var wizardState = this.wizardState;
        if (Ext.getCmp('selExistRadio').getGroupValue() == 0) {
            if (Ext.isEmpty(wizardState.seriesId)) {
                Ext.Msg.alert('No series selected', 'Please select a series to add the new job to.');
                callback(false);
                return;
            }
            
            //Goto next step as we have an already existing series
            callback(true);
            return;
        } else {
            var seriesName = Ext.getCmp('seriesCombo').getRawValue();
            var seriesDesc = Ext.getCmp('seriesDesc').getRawValue();
            if (Ext.isEmpty(seriesName) ||
                    Ext.isEmpty(seriesDesc)) {
                Ext.Msg.alert('Create new series',
                    'Please specify a name and description for the new series.');
                callback(false);
                return;
            }
            
            //Request our new series is created
            Ext.Ajax.request({
                url: 'createSeries.do',
                params: { 
            		'seriesName': seriesName,
            		'seriesDescription': seriesDesc
                },
                callback : function(options, success, response) {
            		if (success) {
            			var responseObj = Ext.util.JSON.decode(response.responseText);
            			if (responseObj.success && Ext.isNumber(responseObj.data.id)) {
            				wizardState.seriesId = responseObj.data.id;
            				callback(true);
            				return;
            			}
            		}
            		
            		Ext.Msg.alert('Create new series','There was an internal error saving your series. Please try again in a few minutes.');
            		callback(false);
            		return;
            	}
            });
        }
    }
	
	
});