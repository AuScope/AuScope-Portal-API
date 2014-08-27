/**
 * Class for parsing a set of portal.csw.CSWRecord objects request/response
 * using the Querier interface
 *
 * The resulting Querier allows the selection of the CSWRecord online resources
 * such that they will be downloaded by a VL job.
 */
Ext.define('vegl.layer.querier.csw.SelectableCSWQuerier', {
    extend: 'portal.layer.querier.Querier',

    constructor: function(config){
        this.callParent(arguments);
    },

    /**
     * See parent class for definition
     */
    query : function(queryTarget, callback) {        
        var cswRecord = queryTarget.get('cswRecord');
        if (!cswRecord) {
            callback(this, [], queryTarget);
            return;
        }

        var keywordsString = "";
        var keywords = cswRecord.get('descriptiveKeywords');
        for (var i = 0; i < keywords.length; i++) {
            keywordsString += keywords[i];
            if (i < (keywords.length - 1)) {
                keywordsString += ', ';
            }
        }

        var panel = Ext.create('portal.layer.querier.BaseComponent', {
            border : false,
            autoScroll : true,
            items : [{
                layout : 'fit',
                items : [{
                    xtype : 'fieldset',
                    items : [{
                        xtype : 'displayfield',
                        fieldLabel : 'Source',
                        value : Ext.util.Format.format('<a target="_blank" href="{0}">Link back to registry</a>', cswRecord.get('recordInfoUrl'))
                    },{
                        xtype : 'displayfield',
                        fieldLabel : 'Title',
                        anchor : '100%',
                        value : cswRecord.get('name')
                    }, {
                        xtype : 'textarea',
                        fieldLabel : 'Abstract',
                        anchor : '100%',
                        value : cswRecord.get('description'),
                        readOnly : true
                    },{
                        xtype : 'displayfield',
                        fieldLabel : 'Keywords',
                        anchor : '100%',
                        value : keywordsString
                    },{
                        xtype : 'displayfield',
                        fieldLabel : 'Contact Org',
                        anchor : '100%',
                        value : cswRecord.get('contactOrg')
                    },{
                        xtype : 'dataselectionpanel',
                        itemId : 'dataselection-panel',
                        region : cswRecord.get('geographicElements'),
                        hideHeaders : false,
                        cswRecords : [ cswRecord ]
                    },{
                        xtype : 'button',
                        margin : '5 0 0 0',
                        text : 'Capture selected',
                        iconCls : 'add',
                        handler : Ext.bind(this.addSelectedResourcesToSession, this) 
                    }]
                }]
            }]
        });

        callback(this, [panel], queryTarget);
    },
    
    addSelectedResourcesToSession : function(button) {
        var fieldSetContainer = button.findParentByType('fieldset');
        var dataSelPanel = fieldSetContainer.getComponent('dataselection-panel');
        
        dataSelPanel.saveCurrentSelection(function(totalSelected, totalErrors) {
            if (totalSelected === 0) {
                Ext.Msg.alert('No selection', 'You haven\'t selected any data to capture. Please select one or more rows by checking the box alongside each row.');
            } else if (totalErrors === 0) {
                Ext.Msg.alert('Request Saved', 'Your ' + totalSelected + ' dataset(s) have been saved. You can either continue selecting more data or <a href="jobbuilder.html">create a job</a> to process your existing selections.');
                this.close();
            } else {
                Ext.Msg.alert('Error saving data', 'There were one or more errors when saving some of the datasets you selected');
                this.close();
            }
        });
    }
});