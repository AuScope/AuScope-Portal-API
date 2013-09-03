/**
 * Builds a form panel for Project filters
 */
Ext.define('vegl.layer.filterer.forms.ProjectFilterForm', {
    extend: 'portal.layer.filterer.BaseFilterForm',

    /**
     * Accepts a config for portal.layer.filterer.BaseFilterForm
     */
    constructor : function(config) {
        //First build our keyword/resource data from our list of CSWRecords
        var cswRecords = config.layer.get('cswRecords');
        var resourceData = {}; //sotre the counts of providers keyed by provider names
        for (var i = 0; i < cswRecords.length; i++) {
            //Add resource providers
            var resourceProvider = cswRecords[i].get('resourceProvider');
            if (resourceData[resourceProvider]) {
                resourceData[resourceProvider]++;
            } else {
                resourceData[resourceProvider] = 1;
            }
        }
        
        //We will have easy to read labels mapping to a set of equivalent keywords
        var keywordList = [{
            label : 'Elevation Grid',
            keywords : ['ELE', 'ELEVATION']
        }, {
            label : 'Magnetics Grid',
            keywords : ['MAG', 'MAGNETICS']
        }, {
            label : 'Radiometrics Grid',
            keywords : ['RAD', 'RADIOMETRICS']
        }];
        var keywordStore = Ext.create('Ext.data.Store', {
            fields      : ['keywords', 'label'],
            data        : keywordList
        });

        //Map our resource data into a form that can be used by an Ext store
        var providerList = [];
        for (var provider in resourceData) {
            var temp={};
            temp.resourceProvider=provider;
            temp.count=resourceData[provider];
            providerList.push(temp);
        }
        var resourceProviderStore = Ext.create('Ext.data.Store', {
            fields      : ['resourceProvider', 'count'],
            data        : providerList
        });

        Ext.apply(config, {
            delayedFormLoading: false,
            border: false,
            autoScroll: true,
            hideMode:'offsets',
            width:'100%',
            buttonAlign:'right',
            labelAlign:'right',
            labelWidth: 70,
            bodyStyle:'padding:5px',
            autoHeight: true,
            items: [{
                xtype:'fieldset',
                title: 'Project Filter Properties',
                autoHeight: true,
                items: [{
                    anchor: '100%',
                    xtype: 'textfield',
                    fieldLabel: 'Title',
                    name: 'title'
                },{
                    xtype: 'combo',
                    tplWriteMode: 'set',
                    anchor: '100%',
                    queryMode: 'local',
                    name: 'keyword',
                    fieldLabel: 'Keyword',
                    labelAlign: 'left',
                    forceSelection: true,
                    store: keywordStore,
                    triggerAction: 'all',
                    typeAhead: true,
                    displayField:'label',
                    valueField:'keywords',
                    autoScroll: true
                },{
                    xtype: 'combo',
                    tpl: '<tpl for="."><li style="word-wrap" data-qtip="{resourceProvider} - {count} record(s)" class="x-boundlist-item" role="option">{resourceProvider}</li></tpl>',
                    anchor: '100%',
                    queryMode: 'local',
                    name: 'resourceProvider',
                    fieldLabel: 'Resource Provider',
                    labelAlign: 'left',
                    forceSelection: true,
                    store: resourceProviderStore,
                    triggerAction: 'all',
                    typeAhead: true,
                    displayField:'resourceProvider',
                    valueField:'resourceProvider',
                    autoScroll: true
                }]
            }]
        });

        this.callParent(arguments);
    }
});