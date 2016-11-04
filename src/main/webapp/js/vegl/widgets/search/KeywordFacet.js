/**
 * A Facet for searching on the "keyword" attribute
 *
 */
Ext.define('vegl.widgets.search.KeywordFacet', {
    extend : 'vegl.widgets.search.BaseFacetWidget',
    alias: 'widget.keywordfacet',

    spacerHeight : 24,

    constructor : function(config) {

        this.keywordStore = new Ext.data.Store({
            autoload: true,
            fields: ['keyword'],
            proxy : {
                type : 'ajax',
                url : 'facetedKeywords.do',
                extraParams : {
                    serviceId : ''
                },
                reader : {
                    type : 'json',
                    transform: function(responseObj){
                        if (Ext.isEmpty(responseObj.data)) {
                            return [];
                        }

                        data = responseObj.data.map(function(val){
                            return { keyword: val };
                        });
                        return data;
                    },
                    rootProperty : 'data'
                }
            }
        });

        Ext.apply(config, {
            title: 'Keywords',
            items: [{
                xtype : 'fieldset',
                itemId : 'cswfilterkeywordfieldsetitemid',
                layout : 'column',
                anchor : '100%',
                border : false,
                style : 'padding:5px 10px 0px 10px',
                items : [{
                    columnWidth : 1,
                    border : false,
                    layout : 'anchor',
                    bodyStyle:'padding:0px 2 0px 2px',
                    items : []
                },{
                    width : 25,
                    border : false,
                    bodyStyle:'padding:0px 0 0px 0px',
                    items : []
                }, {
                    width : 25,
                    border : false,
                    bodyStyle:'padding:0px 0 0px 0px',
                    items : []
                }],
                listeners: {
                    afterrender: function() {
                        this.handlerNewKeywordField();
                    },
                    scope: this
                }
            }]
        });

        this.callParent(arguments);
    },

    onRegistryChange: function(cmp, newServiceId) {
        this.setServiceId(newServiceId);
    },

    setServiceId: function(serviceId) {
        this.clearSearch(true);
        this.keywordStore.getProxy().extraParams.serviceId = serviceId;
        this.keywordStore.load();
    },

    /**
     * Updates the visibility on all add/remove buttons
     */
    updateAddRemoveButtons : function() {
        var keywordFieldSet = this.down('#cswfilterkeywordfieldsetitemid');

        var comboKeywordColumn = keywordFieldSet.items.getAt(0);
        var buttonRemoveColumn = keywordFieldSet.items.getAt(1);
        var buttonAddColumn = keywordFieldSet.items.getAt(2);

        var existingKeywordFields = comboKeywordColumn.items.getCount();

        for (var i = 0; i < existingKeywordFields; i++) {
            var addButton = buttonAddColumn.items.getAt(i);
            var removeButton = buttonRemoveColumn.items.getAt(i);

            //We can always remove so long as we have at least 1 keyword
            if (existingKeywordFields <= 1) {
                removeButton.hide();
            } else {
                removeButton.show();
            }
        }
    },

    /**
     * This function removes the buttons and keywords associated with button
     */
    handlerRemoveKeywordField : function(button, e) {
        var keywordFieldSet = this.down('#cswfilterkeywordfieldsetitemid');

        var comboKeywordColumn = keywordFieldSet.items.getAt(0);
        var buttonRemoveColumn = keywordFieldSet.items.getAt(1);
        var buttonAddColumn = keywordFieldSet.items.getAt(2);

        //Figure out what component index we are attempting to remove
        var id = button.initialConfig.keywordIDCounter;
        var index = buttonRemoveColumn.items.findIndexBy(function(cmp) {
            return cmp === button;
        });

        //Remove that index from each column
        comboKeywordColumn.remove(comboKeywordColumn.getComponent(index));
        buttonRemoveColumn.remove(buttonRemoveColumn.getComponent(index));
        buttonAddColumn.remove(buttonAddColumn.getComponent(0)); //always remove spacers

        //Update our add/remove buttons
        this.updateAddRemoveButtons();
        keywordFieldSet.doLayout();

        this.fireEvent('change', this);
    },

    handlerNewKeywordField : function(button, e) {
        var keywordFieldSet = this.down('#cswfilterkeywordfieldsetitemid');

        var comboKeywordColumn = keywordFieldSet.items.getAt(0);
        var buttonRemoveColumn = keywordFieldSet.items.getAt(1);
        var buttonAddColumn = keywordFieldSet.items.getAt(2);


        //Add our combo for selecting keywords
        comboKeywordColumn.add({
            xtype: 'combo',
            anchor: '18',
            name: 'keywords',
            queryMode: 'local',
            typeAhead: true,
            margin: '0 20 0 0',
            typeAheadDelay : 500,
            forceSelection : false,
            displayField: 'keyword',
            valueField:'keyword',
            store: this.keywordStore,
            listeners: {
                select: function() {
                    this.fireEvent('change', this);
                },
                specialkey: function(field, e) {
                    if (e.getKey() == e.ENTER) {
                        this.fireEvent('change', this);
                    }
                },
                scope: this
            }
        });

        //We also need a button to remove this keyword field
        buttonRemoveColumn.add({
            xtype : 'button',
            iconCls : 'remove',
            width : 24,
            height : this.spacerHeight,
            scope : this,
            keywordIDCounter : this.keywordIDCounter,
            handler : this.handlerRemoveKeywordField,
            style: {
                'background-color': '#fff',
                'background-image': 'none',
                'border-color': '#c1c1c1'
            }
        });

        //Because our add button will always be at the bottom of the list
        //we need to pad preceding elements with spacers
        if (buttonAddColumn.items.getCount()===0) {
            buttonAddColumn.add({
                xtype : 'button',
                iconCls : 'add',
                width : 24,
                height : this.spacerHeight,
                scope : this,
                handler : this.handlerNewKeywordField,
                style: {
                    'background-color': '#fff',
                    'background-image': 'none',
                    'border-color': '#c1c1c1'
                }
            });
        } else {
            buttonAddColumn.insert(0, {
                xtype : 'box',
                width : 22,
                height : this.spacerHeight
            })
        }

        this.keywordIDCounter++;
        this.updateAddRemoveButtons();
        keywordFieldSet.doLayout();

    },

    installToolTips: function() {
        this.callParent(arguments);
    },

    /**
     * See base class
     */
    clearSearch: function(silent) {
        var keywordFieldSet = this.down('#cswfilterkeywordfieldsetitemid');

        var comboKeywordColumn = keywordFieldSet.items.getAt(0);
        var buttonRemoveColumn = keywordFieldSet.items.getAt(1);
        var buttonAddColumn = keywordFieldSet.items.getAt(2);

        this.suspendLayouts();
        comboKeywordColumn.removeAll();
        buttonRemoveColumn.removeAll();
        buttonAddColumn.removeAll();

        this.handlerNewKeywordField();

        if (!silent) {
            this.fireEvent('change', this);
        }
        this.resumeLayouts();
    },

    /**
     * See base class
     */
    extractSearchFacets : function() {
        var keywordFieldSet = this.down('#cswfilterkeywordfieldsetitemid');
        var comboKeywordColumn = keywordFieldSet.items.getAt(0);

        var rawValues = []
        comboKeywordColumn.items.each(function(combo) {
            var value = combo.getValue();
            if (value) {
                value = value.trim();
            }

            if (!Ext.isEmpty(value)) {
                rawValues.push(value);
            }
        });

        var facets = [];
        Ext.each(rawValues, function(value) {
            facets.push(Ext.create('vegl.models.SearchFacet', {
                field: 'keyword',
                value: value,
                comparison: vegl.models.SearchFacet.CMP_EQUAL,
                type: vegl.models.SearchFacet.TYPE_STRING
            }));
        });
        return facets;
    }
});
