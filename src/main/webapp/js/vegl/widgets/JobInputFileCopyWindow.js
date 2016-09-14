/**
 * A window for creating a new job input file in the form of a vegl.models.Download or a vgl.models.FileRecord
 * from a previously run job
 */
Ext.define('vegl.widgets.JobInputFileCopyWindow', {
    extend : 'Ext.Window',

    jobId : null,
    jobStore: null,

    /**
     * Adds the following config options:
     *
     * {
     *  jobId : String - The unique ID of a job to add input files to
     * }
     */
    constructor : function(config) {
        this.jobId = config.jobId;

        this.jobStore = Ext.create('Ext.data.Store', {
            model: 'vegl.models.Job',
            proxy: {
                type: 'memory',
                reader: {
                    type: 'json'
                }
            }
        });

        Ext.apply(config, {
            layout : 'fit',
            title : 'Copy from a previous job',
            items : [{
                xtype : 'container',
                layout: {
                    type: 'hbox',
                    align: 'stretch',
                    pack: 'center'
                },
                items : [{
                    xtype: 'jobstree',
                    flex: 1,
                    jobStore: this.jobStore,
                    hideSettings: true,
                    listeners: {
                        selectjob : Ext.bind(function(panel, job) {
                            if (job == null) {
                                this.down('jobfilespanel').cleanupDataStore();
                            } else {
                                this.down('jobfilespanel').listFilesForJob(job);
                            }
                        }, this)
                    }
                },{
                    xtype: 'splitter'
                },{
                    xtype: 'jobfilespanel',
                    width: 400,
                    currentJobId: this.jobId,
                    cloudFiles: true,
                    fileGroupName: 'Files in cloud storage',
                    remoteGroupName: 'Data service downloads',
                    nameColumnWidth: 150,
                    hideDeleteButton: true,
                    hideRowExpander: true,
                    hideLocationColumn: true,
                    emptyText: '<p class="centeredlabel">This job doesn\'t have any input files or service downloads configured.</p>',
                    selModel: {
                        selType: 'checkboxmodel'
                    }
                }]
            }],
            buttons:[{
                xtype: 'button',
                text: 'Copy selections to Job',
                scope : this,
                iconCls : 'add',
                handler: function() {
                    this.addJobFile();
                }
            }]
        });

        this.callParent(arguments);

        this.on('afterrender', this._loadNodes, this);
    },

    _loadNodes : function() {
        var jobsTree = this.down('jobstree');
        var mask = new Ext.LoadMask({
            msg: 'Loading Jobs...',
            target: jobsTree
        });
        mask.show();

        Ext.Ajax.request({
            url: 'secure/treeJobs.do',
            scope: this,
            callback: function(options, success, response) {
                mask.hide();
                if (!success) {
                    Ext.Msg.alert("Error", "Unable to load your jobs due to a connection error. Please try refreshing the page.");
                    return;
                }

                var responseObj = Ext.JSON.decode(response.responseText);
                if (!responseObj || !responseObj.success) {
                    Ext.Msg.alert("Error", "Unable to load your jobs due to a server error. Please try refreshing the page.");
                    return;
                }

                this.jobStore.loadData(responseObj.data.jobs);
                jobsTree.getStore().setRoot(responseObj.data.nodes);
            }
        });
    },

    /**
     *
     * Closes this window on success
     */
    addJobFile : function(filePanel) {
        var selectedJobs = this.down('jobstree').getSelection();
        var selectedItems = this.down('jobfilespanel').getSelection();


        if (Ext.isEmpty(selectedJobs) || Ext.isEmpty(selectedItems)) {
            Ext.Msg.alert("No Files Selected", "You haven't selected any files to copy to this job.");
            return;
        }

        var fileKeys = selectedItems
            .filter(function(item) {return item.get('source') instanceof vegl.models.FileRecord;})
            .map(function(file) {return file.get('name');});

        var downloadIds = selectedItems
            .filter(function(item) {return item.get('source') instanceof vegl.models.Download;})
            .map(function(download) {return download.get('source').get('id');});

        var mask = new Ext.LoadMask({
            msg: 'Copying Files...',
            target: this
        });
        mask.show();

        Ext.Ajax.request({
            url: 'secure/copyJobFiles.do',
            params: {
                sourceJobId: selectedJobs[0].get('id'),
                targetJobId: this.jobId,
                fileKey: fileKeys,
                downloadId: downloadIds,
            },
            scope: this,
            callback: function(options, success, response) {
                mask.hide();
                if (!success) {
                    Ext.Msg.alert("Error", "Unable to copy your files due to a connection error. Please try refreshing the page.");
                    return;
                }

                var responseObj = Ext.JSON.decode(response.responseText);
                if (!responseObj || !responseObj.success) {
                    Ext.Msg.alert("Error", "Unable to copy your files due to a server error. Please try refreshing the page.");
                    return;
                }

                this.close();
            }
        });
    }
});
