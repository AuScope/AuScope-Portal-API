/**
 * Class for parsing a set of portal.csw.CSWRecord objects request/response
 * using the Querier interface
 * 
 * The resulting Querier includes online resources of the selected 
 * CSWRecord object and its immediate child CSWRecord object(s).
 */
Ext.define('vegl.layer.querier.csw.CSWQuerier', {
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
        
        var cswRecords = [];
        cswRecords.push(cswRecord);
        var childRecords = cswRecord.get('childRecords');
        if (childRecords) {
            Ext.each(childRecords, function(record) {
                cswRecords.push(record);
            });
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
                        fieldLabel : 'Resources',
                        xtype : 'onlineresourcepanel',
                        cswRecords : cswRecords
                    }]
                }]
            }]
        });

        callback(this, [panel], queryTarget);
    }
});