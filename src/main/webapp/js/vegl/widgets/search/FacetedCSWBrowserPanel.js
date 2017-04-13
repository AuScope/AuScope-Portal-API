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
        this.serviceIds = null;

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

    _hasNextPage: function() {
        for (var serviceId in this.searchNextIndexes) {
            if (this.searchNextIndexes[serviceId] >= 1) {
                return true;
            }
        }
        return false;
    },

    _handleNextPage: function() {
        if (!this._hasNextPage()) {
            return;
        }

        this.previousStartIndexes.push(this.searchStartIndexes);
        this.searchStartIndexes = this.searchNextIndexes;
        this.searchNextIndexes = null;
        this.refreshResults();
    },

    _handlePreviousPage: function() {
        if (this.previousStartIndexes.length === 0) {
            return;
        }

        this.searchNextIndexes = this.searchStartIndexes;
        this.searchStartIndexes = this.previousStartIndexes.pop();
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
                this.serviceIds = [];
                for (var i = 0; i < data.length; i++) {
                    this.serviceIds.push(data[i].id);
                }

                this._initPagingParams();
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
            start: [],
            serviceId: [],
            limit: vegl.widgets.search.FacetedCSWBrowserPanel.PAGE_SIZE
        };

        Ext.each(this.serviceIds, function(serviceId) {
            if (Ext.isNumber(this.searchStartIndexes[serviceId])) {
                params.start.push(this.searchStartIndexes[serviceId]);
                params.serviceId.push(serviceId);
            }
        }, this);

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
                this.searchStartIndexes = data.startIndexes;
                this.searchNextIndexes = data.nextIndexes;
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

        this.searchStartIndexes = {};
        this.searchNextIndexes = {};
        if (!Ext.isEmpty(this.serviceIds)) {
            Ext.each(this.serviceIds, function(serviceId) {
                this.searchStartIndexes[serviceId] = 1;
                this.searchNextIndexes[serviceId] = 1;
            }, this);
        }
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
