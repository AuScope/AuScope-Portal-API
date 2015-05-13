/**
 * A Ext.grid.Panel specialisation for rendering the series
 * available to the current user.
 *
 * Adds the following events
 * selectseries : function(vegl.widgets.SeriesPanel panel, vegl.models.Series selection) - fires whenever a new Series is selected
 * refreshDetailsPanel : function(vegl.widgets.SeriesPanel panel, vegl.models.Series series) - fires whenever a Series is successfully deleted
 * error : function(vegl.widgets.SereisPanel panel, String message) - fires whenever a comms error occurs
 */
Ext.define('vegl.widgets.SeriesPanel', {
    extend : 'Ext.grid.Panel',
    alias : 'widgets.seriespanel',

    cancelSeriesAction : null,
    deleteSeriesAction : null,
    contextMenu : null,

    constructor : function(config) {
        
        this.cancelSeriesAction = new Ext.Action({
            text: 'Cancel series jobs',
            iconCls: 'cross-icon',
            scope : this,
            disabled : true,
            handler: function() {
                var selection = this.getSelectionModel().getSelection();
                if (selection.length > 0) {
                    this.cancelSeries(selection[0]);
                }
            }
        });

        this.deleteSeriesAction = new Ext.Action({
            text: 'Delete entire series',
            iconCls: 'cross-icon',
            scope : this,
            disabled : true,
            handler: function() {
                var selection = this.getSelectionModel().getSelection();
                if (selection.length > 0) {
                    this.deleteSeries(selection[0]);
                }
            }
        });
        
        var seriesStore =Ext.create('Ext.data.Store', {
            model : 'vegl.models.Series',
            proxy : {
                type : 'ajax',
                url : 'secure/querySeries.do',
                reader : {
                    type : 'json',
                    rootProperty : 'data'
                },
                listeners : {
                    exception : function(proxy, response, operation) {
                        responseObj = Ext.JSON.decode(response.responseText);
                        errorMsg = responseObj.msg;
                        errorInfo = responseObj.debugInfo;
                        portal.widgets.window.ErrorWindow.showText('Error', errorMsg, errorInfo);
                    }
                }
            },               
            autoLoad : true
        });

        Ext.apply(config, {
//            plugins : [{
//                ptype : 'inlinecontextmenu',
//                align : 'center',
//                actions: [this.cancelSeriesAction, this.deleteSeriesAction]
//            }],
            store : seriesStore,
            columns: [{ header: 'User', width: 150, sortable: true, dataIndex: 'user'},
                      { header: 'Series Name', flex : 1, sortable: true, dataIndex: 'name'}]
//            buttons: [{
//                text: 'Query...',
//                tooltip: 'Displays the query dialog to search for job series',
//                handler: Ext.bind(this.onQuerySeries, this),
//                cls: 'x-btn-text-icon',
//                iconCls: 'find-icon'
//            }]
        });   

        this.callParent(arguments);

        this.on('select', this.onSeriesSelection, this);
        this.on('selectionchange', this._onSelectionChange, this);   
        this.on('viewready', function(grid,eOpts){            
            grid.store.on('load', function(store, records, successful, eOpts){
                grid.selModel.doSelect(store.data.items[0]);
            });            
            
        });
       
    },

    _onSelectionChange : function(sm) {
        var selections = this.getSelectionModel().getSelection();
        if (selections.length === 0) {
            this.cancelSeriesAction.setDisabled(true);
            this.deleteSeriesAction.setDisabled(true);
        } else {
            this.cancelSeriesAction.setDisabled(false);
            this.deleteSeriesAction.setDisabled(false);
        }
    },

    onSeriesSelection : function(sm, series) {
        this.fireEvent('selectseries', this, series);
    },

    onQuerySeries : function(btn) {
        var me = this;

        var queryWindow = new Ext.Window({
            title: 'Query job series',
            plain: true,
            width: 500,
            resizable: false,
            autoScroll: true,
            constrainHeader: true,
            bodyStyle: 'padding:5px;',
            items: [{
                xtype : 'form',
                itemId : 'qForm',
                bodyStyle: 'padding:5px;',
                defaults: { anchor: "100%" },
                items: [{
                    xtype: 'textfield',
                    itemId: 'qSeriesName',
                    fieldLabel: 'Series Name'
                }, {
                    xtype: 'textfield',
                    itemId: 'qSeriesDesc',
                    fieldLabel: 'Description'
                }]
            }],
            modal: true,
            buttons: [{
                text: 'Query',
                handler: function(btn) {
                    var qWindow = btn.ownerCt.ownerCt;
                    var qForm = qWindow.getComponent('qForm');
                    var qName = qForm.getComponent('qSeriesName').getValue();
                    var qDesc = qForm.getComponent('qSeriesDesc').getValue();
                    me.querySeries(qName, qDesc);
                    qWindow.close();
                }
            }, {
                text: 'Cancel',
                handler: function(btn) {
                    var qWindow = btn.ownerCt.ownerCt;
                    qWindow.close();
                }
            }]
        });

        queryWindow.show();
    },

    /**
     * Update the contents of this panel with the specified query parameters
     */
    querySeries : function(name, desc) {
        var store = this.getStore();
        var ajaxProxy = store.getProxy();

        if (Ext.isEmpty(name)) {
            ajaxProxy.extraParams.qSeriesName = null;
        } else {
            ajaxProxy.extraParams.qSeriesName = name;
        }
        if (Ext.isEmpty(desc)) {
            ajaxProxy.extraParams.qSeriesDesc = null;
        } else {
            ajaxProxy.extraParams.qSeriesDesc = desc;
        }
        store.load();
    },

    cancelSeries : function(series) {
        Ext.Msg.show({
            title: 'Cancel Series Jobs',
            msg: 'Are you sure you want to cancel all jobs for the selected series?',
            buttons: Ext.Msg.YESNO,
            icon: Ext.Msg.WARNING,
            modal: true,
            closable: false,
            scope : this,
            fn: function(btn) {
                if (btn == 'yes') {

                    Ext.getBody().mask('Cancelling Series Jobs...');
                    
                    Ext.Ajax.request({
                        url: 'secure/killSeriesJobs.do',
                        params: { 'seriesId': series.get('id')},
                        scope : this,
                        callback : function(options, success, response) {                            
                            Ext.getBody().unmask();
                            if (!success) {
                                this.fireEvent('error', this, 'There was an error communicating with the VL server. Please try again later.');
                                return;
                            }

                            var responseObj = Ext.JSON.decode(response.responseText);
                            if (!responseObj.success) {
                                this.fireEvent('error', this, Ext.util.Format.format('There was an error cancelling the jobs for the selected series. {0}', responseObj.msg));
                                return;
                            }

                            this.getStore().load();//refresh our store
                        }
                    });
                }
            }
        });
    },

    deleteSeries : function(series) {
        Ext.Msg.show({
            title: 'Delete Series Jobs',
            msg: 'Are you sure you want to delete this series and all jobs associated with it?',
            buttons: Ext.Msg.YESNO,
            icon: Ext.Msg.WARNING,
            modal: true,
            closable: false,
            scope : this,
            fn: function(btn) {
                if (btn == 'yes') {
                    Ext.getBody().mask('Deleting Series...');
                    Ext.Ajax.request({
                        url: 'secure/deleteSeriesJobs.do',
                        params: { 'seriesId': series.get('id')},
                        scope : this,
                        callback : function(options, success, response) {
                            Ext.getBody().unmask();

                            if (!success) {
                                this.fireEvent('error', this, 'There was an error communicating with the VL server. Please try again later.');
                                return;
                            }

                            var responseObj = Ext.JSON.decode(response.responseText);
                            if (!responseObj.success) {
                                this.fireEvent('error', this, Ext.util.Format.format('There was an error deleting the jobs for the selected series. {0}', responseObj.msg));
                                return;
                            }

                            this.getStore().load();//refresh our store
                            this.fireEvent('refreshDetailsPanel'); //refresh Details panel
                        }
                    });
                }
            }
        });
    }
});