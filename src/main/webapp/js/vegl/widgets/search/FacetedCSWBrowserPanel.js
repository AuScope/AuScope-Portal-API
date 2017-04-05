/**
 * A widget for receiving a number of search facets and then generating
 * a display for the search results
 *
 */
Ext.define('vegl.widgets.search.FacetedCSWBrowserPanel', {
    extend : 'Ext.panel.Panel',
    alias: 'widget.facetedcswbrowserpanel',

    statics: {
        PAGE_SIZE: 10
    },

    constructor : function(config) {

        this.store = Ext.create('Ext.data.Store', {
            model : 'portal.csw.CSWRecord',
            autoLoad: false
        });

        Ext.apply(config, {
            layout: {
                type: 'vbox',
                align: 'stretch',
                pack: 'center'
            },
            items: [{
                xtype: 'tabbar'
            },{
                xtype: 'vlcswrecordpanel',
                itemId: 'recordpanel',
                border: false,
                store: this.store,
                hideSearch: true,
                header: false,
                map: config.map,
                layerFactory: config.layerFactory,
                flex: 1
            }],
            dockedItems: [{
                xtype: 'toolbar',
                dock: 'bottom',
                border: false,
                items: [{
                    xtype: 'tbfill'
                },{
                    xtype: 'label',
                    itemId: 'pagelabel',
                    text: 'Page 1'
                },{
                    tooltip: 'Previous Page',
                    itemId: 'previousbutton',
                    disabled: true,
                    iconCls: Ext.baseCSSPrefix + 'tbar-page-prev',
                    handler: this._handlePreviousPage,
                    scope: this
                },{
                    tooltip: 'Next Page',
                    itemId: 'nextbutton',
                    disabled: true,
                    iconCls: Ext.baseCSSPrefix + 'tbar-page-next',
                    handler: this._handleNextPage,
                    scope: this
                }]
            }]
        });

        this.callParent(arguments);
    },

    initComponent: function() {
        if (this.rendered) {
            this._loadRegistries();
        } else {
            this.on('afterrender', this._loadRegistries, this);
        }

        this._initPagingParams();

        this.callParent(arguments);
    },

    _handleNextPage: function() {
        if (this.searchNextIndex < 1) {
            return;
        }

        this.previousStartIndexes.push(this.searchStartIndex);
        this.searchStartIndex = this.searchNextIndex;
        this.searchNextIndex = -1;
        this.refreshResults();
    },

    _handlePreviousPage: function() {
        if (this.previousStartIndexes.length === 0) {
            return;
        }

        this.searchNextIndex = this.searchStartIndex;
        this.searchStartIndex = this.previousStartIndexes.pop();
        this.refreshResults();
    },

    /**
     * Populates the registry selection bar (possibly hiding it if there is only 1 registry)
     */
    _loadRegistries: function() {
        var mask = new Ext.LoadMask({
            target: this,
            msg: 'Loading Registries...'
        });
        portal.util.Ajax.request({
            url: 'getCSWServices.do',
            scope: this,
            success: function(data, message, debugInfo) {
                var configs = [];
                for (var i = 0; i < data.length; i++) {
                    configs.push({
                        xtype: 'tab',
                        closable: false,
                        text: data[i].title,
                        serviceId:  data[i].id,
                        serviceUrl:  data[i].url,
                        scope: this,
                        handler: function(btn) {
                            this._initPagingParams();
                            this.searchServiceId = btn.serviceId;
                            this.refreshResults();
                            this.fireEvent('registrychange', this, this.searchServiceId);
                        }
                    });
                }

                var tabBar = this.down('tabbar');
                tabBar.add(configs);
                var activeTab = tabBar.items.getAt(0);
                tabBar.setActiveTab(activeTab);
                this.searchServiceId = activeTab.serviceId;
                if (data.length === 1) {
                    tabBar.setHidden(true);
                }

                this.fireEvent('registrychange', this, this.searchServiceId);
            },
            failure: function(message, debugInfo) {
                this.getEl().setHtml("Unable to load registries. Please try refreshing the page");
            }
        });
    },

    /**
     * Reloads the displayed CSWRecords for the current page.
     */
    refreshResults: function() {
        this.down('#pagelabel').setText(Ext.util.Format.format('Page {0}', this.previousStartIndexes.length + 1));
        this.down('#nextbutton').setDisabled(true);
        this.down('#previousbutton').setDisabled(true);

        var params = {
            field: [],
            value: [],
            type: [],
            comparison: [],
            start: this.searchStartIndex,
            limit: vegl.widgets.search.FacetedCSWBrowserPanel.PAGE_SIZE,
            serviceId: this.searchServiceId
        };

        Ext.each(this.searchFacets, function(facet) {
            params.field.push(facet.get('field'));
            params.value.push(facet.get('value'));
            params.type.push(facet.get('type'));
            params.comparison.push(facet.get('comparison'));
        });

        var mask = new Ext.LoadMask({
            target: this.down('#recordpanel'),
            text: 'Searching for records...'
        });

        if (this.currentAjax) {
            Ext.Ajax.abort(this.currentAjax);
            this.currentAjax = null;
        }

        mask.show();
        this.currentAjax = portal.util.Ajax.request({
            url: 'facetedCSWSearch.do',
            params: params,
            timeout: 300 * 1000, //5 minutes
            scope: this,
            callback: function(success, data, msg, debugInfo, response) {
                mask.hide();
                mask.destroy();
                this.currentAjax = null;

                if (!success) {
                    this.store.removeAll();
                    if (!response || !response.aborted) {
                        portal.widgets.window.ErrorWindow.showText('Error', 'Unable to complete search request', debugInfo);
                    }
                    return;
                }

                var records = data.records.map(function(rawRec) {
                    return Ext.create('portal.csw.CSWRecord', rawRec);
                });
                this.searchStartIndex = data.startIndex;
                this.searchNextIndex = data.nextIndex;
                this.store.loadData(records);
                this.store.fireEvent('load', this.store, records, true);

                this.down('#nextbutton').setDisabled(this.searchNextIndex <= 0);
                this.down('#previousbutton').setDisabled(this.previousStartIndexes.length <= 0);
            }
        });
    },

    /**
     * Initialises the paging params to the first page of results (and no next index - that will need to be
     * set by refreshResults)
     */
    _initPagingParams: function() {
        this.previousStartIndexes = [];
        this.searchStartIndex = 1;
        this.searchNextIndex = -1;
    },

    /**
     * Updates the underlying search facets and scrolls the page back to the start.
     */
    updateResults: function(searchFacets) {
        this.searchFacets = searchFacets;
        this._initPagingParams();
        this.refreshResults();
    }
});
