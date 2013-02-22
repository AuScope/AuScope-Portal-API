/**
 * Page for listing and monitoring running jobs
 */
Ext.application({
    name : 'portal',

    //Here we build our GUI from existing components - this function should only be assembling the GUI
    //Any processing logic should be managed in dedicated classes - don't let this become a
    //monolithic 'do everything' function
    launch : function() {
        var onError = function(component, message) {
            Ext.Msg.show({
                title: 'Error',
                msg: message,
                buttons: Ext.Msg.OK,
                icon: Ext.Msg.ERROR
            });
        };

        var jobDetailsPanel = Ext.create('vegl.widgets.DetailsPanel', {
            title: 'Description',
            bodyStyle: 'padding:10px',
            listeners : {
                error : onError
            }
        });

        var jobLogsPanel = Ext.create('vegl.widgets.JobLogsPanel', {
            title: 'Logs',
            bodyStyle: 'padding:10px',
            listeners : {
                error : onError
            }
        });

        var jobFilesPanel = Ext.create('vegl.widgets.JobFilesPanel', {
            title: 'Files',
            stripeRows: true,
            listeners : {
                error : onError
            }
        });

        var jobsPanel = Ext.create('vegl.widgets.JobsPanel', {
            title: 'Jobs of selected series',
            region: 'center',
            stripeRows: true,
            itemId : 'vgl-jobs-panel',
            listeners : {
                selectjob : function(panel, job) {
                    jobDetailsPanel.showDetailsForJob(job);
                    jobFilesPanel.listFilesForJob(job);
                    jobLogsPanel.listLogsForJob(job);
                },
                refreshDetailsPanel : function(panel, series) {
                    jobDetailsPanel.showDetailsForSeries(series);
                    jobFilesPanel.cleanupDataStore();
                    jobLogsPanel.clearLogs(true);
                },
                refreshJobDescription : function(job) {
                    jobDetailsPanel.showDetailsForJob(job);
                },
                error : onError
            },
            viewConfig : {
                listeners : {
                    contextmenu : function(view, index, node, e) {
                        if (!this.contextMenu) {
                            this.contextMenu = new Ext.menu.Menu({
                                items: [ cancelSeriesAction, deleteSeriesAction ]
                            });
                        }
                        e.stopEvent();
                        this.contextMenu.showAt(e.getXY());
                    }
                }
            }
        });

        var seriesPanel = Ext.create('vegl.widgets.SeriesPanel', {
            title: 'Series List',
            region: 'north',
            itemId : 'vgl-series-panel',
            height: 250,
            split: true,
            stripeRows: true,
            listeners : {
                selectseries : function(panel, series) {
                    jobsPanel.listJobsForSeries(series);
                    jobDetailsPanel.showDetailsForSeries(series);
                    jobFilesPanel.cleanupDataStore();
                    jobLogsPanel.clearLogs(true);
                },
                refreshDetailsPanel : function() {
                    jobsPanel.cleanupDataStore();
                    jobDetailsPanel.cleanupDetails();
                    jobLogsPanel.clearLogs(true);
                    jobFilesPanel.cleanupDataStore();
                },
                error : onError
            }
        });

        Ext.create('Ext.container.Viewport', {
            layout: 'border',
            id : 'vgl-joblist-viewport',
            items: [{
                xtype: 'box',
                region: 'north',
                applyTo: 'body',
                height: 100
            },{
                border: false,
                region: 'west',
                split: true,
                margins: '2 2 2 0',
                layout: 'border',
                width: 400,
                items: [seriesPanel, jobsPanel]
            },{
                xtype : 'tabpanel',
                title: 'Details',
                itemId : 'vgl-details-panel',
                region: 'center',
                margins: '2 2 2 0',
                activeTab: 0,
                split: true,
                items: [jobDetailsPanel, jobLogsPanel, jobFilesPanel]
            }]
        });
    }
});

