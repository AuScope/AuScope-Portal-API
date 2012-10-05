/**
 * A Ext.grid.Panel specialisation for rendering the Jobs
 * available to the current user.
 *
 * Adds the following events
 * selectjob : function(vegl.widgets.SeriesPanel panel, vegl.models.Job selection) - fires whenever a new Job is selected
 */
Ext.define('vegl.widgets.JobInputFilesPanel', {
    extend : 'Ext.grid.Panel',
    alias : 'widget.jobinputfilespanel',

    currentJobId : null,
    downloadAction : null,
    deleteAction : null,

    constructor : function(config) {
        var jobFilesGrid = this;

        this.currentJobId = config.currentJob ? config.currentJob.get("id") : config.currentJobId;

        //Action for downloading a single file
        this.downloadAction = new Ext.Action({
            text: 'Download this input to your local machine.',
            disabled: true,
            iconCls: 'disk-icon',
            scope : this,
            handler: function() {
                var item = jobFilesGrid.getSelectionModel().getSelection()[0];
                var source = item.get('source');
                if (source instanceof vegl.models.FileRecord) {
                    var params = {
                        jobId : this.currentJobId,
                        filename : source.get('name')
                    };

                    portal.util.FileDownloader.downloadFile("downloadInputFile.do", params);
                } else if (source instanceof vegl.models.Download) {
                    portal.util.FileDownloader.downloadFile(source.get('url'));
                }
            }
        });

        //Action for deleting a single file
        this.deleteAction = new Ext.Action({
            text: 'Delete this input.',
            disabled: true,
            iconCls: 'cross-icon',
            scope : this,
            handler: function() {
                var item = jobFilesGrid.getSelectionModel().getSelection()[0];
                var source = item.get('source');

                if (source instanceof vegl.models.FileRecord) {
                    Ext.Ajax.request({
                        url: 'deleteFiles.do',
                        callback: Ext.bind(this.updateFileStore, this),
                        params: {
                            'fileName': source.get('name'),
                            'jobId': this.currentJobId
                        }
                    });
                } else if (source instanceof vegl.models.Download) {
                    Ext.Ajax.request({
                        url: 'deleteDownloads.do',
                        callback: Ext.bind(this.updateFileStore, this),
                        params: {
                            'downloadId': source.get('id'),
                            'jobId': this.currentJobId
                        }
                    });
                }
            }
        });

        Ext.apply(config, {
            viewConfig : {
                emptyText : '<p class="centeredlabel">This job doesn\'t have any input files or service downloads configured. You can add some by using the add button below or by selecting a dataset from the <a href="gmap.html">main page</a>.</p>'
            },
            features : [Ext.create('Ext.grid.feature.Grouping',{
                groupHeaderTpl: '{name} ({[values.rows.length]} {[values.rows.length > 1 ? "Items" : "Item"]})'
            })],
            plugins : [{
                ptype: 'rowexpander',
                rowBodyTpl : [
                    '<p>{description}</p><br>'
                ]
            },{
                ptype : 'rowcontextmenu',
                contextMenu : Ext.create('Ext.menu.Menu', {
                    items: [this.downloadAction, this.deleteAction]
                })
            }],
            store : Ext.create('Ext.data.Store', {
                model : 'vegl.widgets.JobInputFilesPanel.Item',
                groupField : 'group',
                data : []
            }),
            columns: [{ header: 'Name', width: 200, sortable: true, dataIndex: 'name'},
                      { header: 'Location', width: 200, dataIndex: 'localPath'},
                      { header: 'Details', flex : 1, dataIndex: 'details'}],
            tbar: [{
                text: 'Actions',
                iconCls: 'folder-icon',
                menu: [ this.downloadAction, this.deleteAction]
            }]
        });

        this.callParent(arguments);

        this.on('selectionchange', this._onSelectionChange, this);
        this.on('celldblclick', this._onDblClick, this);

    },

    /**
     * Updates the store by making AJAX requests for the current job object
     */
    updateFileStore : function() {
        if (!this.currentJobId) {
            this.getStore().removeAll();
            return;
        }

        //Fire off the first request for already uploaded input files
        var loadMask = new Ext.LoadMask(Ext.getBody(), {
            msg : 'Requesting input files...',
            removeMask : true
        });
        loadMask.show();

        Ext.Ajax.request({
            url : 'listJobFiles.do',
            params : {
                jobId : this.currentJobId
            },
            scope : this,
            callback : function(options, success, response) {
                if (!success) {
                    loadMask.hide();
                    return;
                }

                var responseObj = Ext.JSON.decode(response.responseText);
                if (!responseObj.success || !responseObj.data) {
                    loadMask.hide();
                    return;
                }

                var fileRecords = [];
                for (var i = 0; i < responseObj.data.length; i++) {
                    fileRecords.push(Ext.create('vegl.models.FileRecord', responseObj.data[i]));
                }

                //Fire off a second request for the downloads
                Ext.Ajax.request({
                    url : 'getJobDownloads.do',
                    params : {
                        jobId : this.currentJobId
                    },
                    scope : this,
                    fileRecords : fileRecords,
                    callback : function(options, success, response) {
                        loadMask.hide();
                        if (!success) {
                            return;
                        }

                        var responseObj = Ext.JSON.decode(response.responseText);
                        if (!responseObj.success || !responseObj.data) {
                            return;
                        }

                        var downloads = [];
                        for (var i = 0; i < responseObj.data.length; i++) {
                            downloads.push(Ext.create('vegl.models.Download', responseObj.data[i]));
                        }

                        this._setStoreData(options.fileRecords, downloads);
                    }
                });
            }
        });
    },

    /**
     * Sets the store data from a set of vegl.models.FileRecord and vegl.models.Download objects
     */
    _setStoreData : function(fileRecords, downloads) {
        var ds = this.getStore();
        var dsData = [];

        for (var i = 0; i < fileRecords.length; i++) {
            var fr = fileRecords[i];
            dsData.push(Ext.create('vegl.widgets.JobInputFilesPanel.Item', {
                id : 'fr-' + fr.get('name'),
                name : fr.get('name'),
                description : 'This file will be made available to the job upon startup. It will be put in the same working directory as the job script.',
                details : Ext.util.Format.fileSize(fr.get('size')),
                localPath : 'Local directory',
                source: fr,
                group : 'Your Uploaded Files'
            }));
        }

        for (var i = 0; i < downloads.length; i++) {
            var dl = downloads[i];
            var hostNameMatches = /.*:\/\/(.*?)\//g.exec(dl.get('url'));
            var hostName = (hostNameMatches && hostNameMatches.length >= 2) ? hostNameMatches[1] : dl.get('url');

            dsData.push(Ext.create('vegl.widgets.JobInputFilesPanel.Item', {
                id : 'dl-' + dl.get('id'),
                name : dl.get('name'),
                description : dl.get('description'),
                details : Ext.util.Format.format('Service call to <a href="{1}" target="_blank">{0}</a>.', hostName, dl.get('url')),
                localPath : dl.get('localPath'),
                source: dl,
                group : 'Remote Web Service Downloads'
            }));
        }

        ds.loadData(dsData);
    },

    _onDblClick : function(view, td, cellIndex, record, tr, rowIndex, e, eOpts) {
        var sm = this.getSelectionModel();

        this.getSelectionModel().select([record], false);
    },

    _onSelectionChange : function(sm) {
        var totalSelections = this.getSelectionModel().getSelection().length;
        if (totalSelections == 0) {
            this.downloadAction.setDisabled(true);
            this.deleteAction.setDisabled(true);
        } else {
            this.downloadAction.setDisabled(false);
            this.deleteAction.setDisabled(false);
        }
    },

    /**
     * Reloads this store with all the files for the specified job
     */
    listFilesForJob : function(job) {
        this.currentJob = job;
        this._updateStore();
    }
});

/**
 * Represents a generic model for containing Download or File objects.
 */
Ext.define('vegl.widgets.JobInputFilesPanel.Item', {
    extend: 'Ext.data.Model',

    fields: [
        { name: 'id', type: 'string' }, //Unique ID for this download/file
        { name: 'name', type: 'string' }, //short name of this download/file
        { name: 'description', type: 'string' }, //longer descriptiion of this download
        { name: 'details', type: 'string'}, //The remote URL or file size
        { name: 'localPath', type: 'string'}, //Where the file will be made available
        { name: 'source', type: 'auto'}, //Either a vegl.models.FileRecord or a vegl.models.Download object.
        { name: 'group', type: 'string'} //How is this item grouped? Unique group name
    ],

    idProperty : 'id'
});