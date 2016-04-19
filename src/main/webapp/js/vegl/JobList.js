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
            region: 'center',
            split: true,
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
                },
                refreshDetailsPanel : function(panel, series) {

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

        var folderPanel = Ext.create('vegl.widgets.FolderPanel', {
            title: 'Folder Organization',
            region: 'north',
            itemId : 'vgl-series-panel',
            height: 250,
            split: true,
            stripeRows: true,
            listeners : {
                selectseries : function(panel, series) {
                    jobsPanel.listJobsForSeries(series);
                    jobDetailsPanel.cleanupDetails();
                },
                refreshDetailsPanel : function() {
                    jobsPanel.cleanupDataStore();
                    jobDetailsPanel.cleanupDetails();
                },
                error : onError
            }
        });


        var me = this;


        Ext.create('Ext.container.Viewport', {
            layout: 'border',
            id : 'vgl-joblist-viewport',
            style: {
                'background-color': 'white'
            },
            listeners:{
              afterrender : function(vp,eOpts){
                  var GridDropTarget = new Ext.dd.DropTarget(folderPanel.getEl(), {
                      ddGroup    : 'grid2tree',
                      notifyDrop: function(dragsource, event, data) {
                          var inprogressStatus=['Pending','Provisioning','In Queue'];
                          if(Ext.Array.contains(inprogressStatus,data.records[0].get('status'))){
                              Ext.Msg.alert('Warning', 'Please wait until the job has finished provisioning');
                              return;
                          }else{
                              var folder = event.getTarget().textContent;
                              me.updateJobSeries(data.records[0].get('id'),folder,function(){
                                  jobsPanel.refreshJobsForSeries();
                              });
                          }

                      }

                  });
              }
            },
            items: [{
                xtype: 'box',
                region: 'north',
                applyTo: 'body',
                height: 60
            },{
                border: false,
                region: 'west',
                split: true,
                margins: '2 2 2 0',
                layout: 'border',
                width: 400,
                bodyStyle: {
                    'background-color': 'white'
                },
                items: [folderPanel, jobsPanel]
            },jobDetailsPanel]
        });
    },

    updateJobSeries : function(jobId,newSeriesId,callback){
        Ext.Ajax.request({
            url: 'updateJobSeries.do',
            params: {
                'id': jobId,
                'folderName': newSeriesId
            },
            callback : function(options, success, response) {
                if (success) {
                    callback();
                  return;
                } else {
                    errorMsg = "There was an internal error saving your series.";
                    errorInfo = "Please try again in a few minutes or report this error to cg_admin@csiro.au.";
                }

                portal.widgets.window.ErrorWindow.showText('Create new series', errorMsg, errorInfo);

                return;
            }
        });
    }
});

