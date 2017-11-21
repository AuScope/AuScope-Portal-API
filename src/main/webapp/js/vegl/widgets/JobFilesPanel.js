Ext.define('vegl.widgets.JobFilesPanel', {
    /** @lends anvgl.JobBuilder.JobFilesPanel */

    extend : 'Ext.grid.Panel',
    alias : 'widget.jobfilespanel',

    currentJobId : null,
    downloadAction : null,
    deleteAction : null,

    /**
     * A Ext.grid.Panel specialisation for rendering the Jobs available to the current user.
     *
     * Adds the following config
     * cloudFiles: Boolean - default false - If true, files will be sourced from cloud. If false, files will be sourced from local staging.
     * fileLookupUrl: String - The URL where job files will be requested from - defaults to secure/stagedJobFiles.do
     * fileGroupName: String - the name of the group for files in storage/staging
     * remoteGroupName:  String - the name of the group for remote web service downloads
     * hideDeleteButton: Boolean - Whether to hide access to the delete button
     * hideRowExpander: Boolean - Whether to hide the row expander column
     * hideLocationColumn: Boolean - Whether to hide the Location column
     * hideDetailsColumn: Boolean - Whether to hide the Details column
     *
     * nameColumnWidth: Number - defaults to 200
     * detailsColumnWidth: Number - defaults to 200
     * emptyText: String - empty text to show
     *
     * Adds the following events:
     * selectjob : function(vegl.widgets.SeriesPanel panel, vegl.models.Job selection) - fires whenever a new Job is selected
     * @constructs
     * @param {object} config
     */
    constructor : function(config) {
        var jobFilesGrid = this;

        this.fileLookupUrl = !!config.cloudFiles ? 'secure/jobCloudFiles.do' : 'secure/stagedJobFiles.do';
        this.fileDownloadUrl = !!config.cloudFiles ? 'secure/downloadFile.do' : 'secure/downloadInputFile.do';
        this.fileMultiDownloadUrl = !!config.cloudFiles ? 'secure/downloadAsZip.do' : null;

        this.fileGroupName = config.fileGroupName ? config.fileGroupName : 'Your Uploaded Files';
        this.remoteGroupName = config.remoteGroupName ? config.remoteGroupName : 'Remote Web Service Downloads';
        this.hideRowExpander = !!config.hideRowExpander;
        this.hideLocationColumn = !!config.hideLocationColumn;
        this.hideDetailsColumn = !!config.hideDetailsColumn;
        this.hideDeleteButton = !!config.hideDeleteButton;
        this.nameColumnWidth = Ext.isNumber(config.nameColumnWidth) ? config.nameColumnWidth : 200;
        this.detailsColumnWidth = Ext.isNumber(config.detailsColumnWidth) ? config.detailsColumnWidth : 200;

        // while creating a job the jobId is not available at this stage
        this.currentJobId = config.currentJob ? config.currentJob.get("id") : config.currentJobId;

        //Action for downloading a single file
        this.downloadAction = new Ext.Action({
            text: 'Download file(s) to your machine.',
            disabled: true,
            iconCls: 'disk-icon',
            scope : this,
            handler: function() {
                var items = jobFilesGrid.getSelectionModel().getSelection();
                var downloadCount = 0;
                var fileRecordCount = 0;

                Ext.each(items, function(item) {
                    var source = item.get('source');
                    if (source instanceof vegl.models.FileRecord) {
                        fileRecordCount++;
                    } else if (source instanceof vegl.models.Download) {
                        downloadCount++;
                    }
                });

                if (downloadCount > 0 && fileRecordCount > 0) {
                    portal.widgets.window.ErrorWindow.showText('Invalid Selection', 'Sorry, but combining multiple file categories isn\'t supported. Please only select files from the same category and try again.');
                } else if (downloadCount > 1) {
                    portal.widgets.window.ErrorWindow.showText('Invalid Selection', 'Sorry, you will need to download data service calls individually. Please only select a single data service download and try again.');
                } else if (downloadCount == 0 && fileRecordCount == 1) {
                    var source = items[0].get('source');
                    var params = {
                        jobId : this.currentJobId,
                        filename : source.get('name'),
                        key: source.get('name')
                    };

                    portal.util.FileDownloader.downloadFile(this.fileDownloadUrl, params);
                } else if (downloadCount == 1 && fileRecordCount == 0) {
                    var source = items[0].get('source');
                    portal.util.FileDownloader.downloadFile(source.get('url'));
                } else if (fileRecordCount > 1) {
                    if (!this.fileMultiDownloadUrl) {
                        portal.widgets.window.ErrorWindow.showText('Invalid Selection', 'Sorry, but these files must be downloaded individually. Please select a single file and try again.');
                        return;
                    }

                    var params = {
                        jobId : this.currentJobId,
                        files : []
                    };

                    Ext.each(items, function(item) {
                        params.files.push(item.get('source').get('name'));
                    });

                    portal.util.FileDownloader.downloadFile(this.fileMultiDownloadUrl, params);
                }
            }
        });

        //Action for deleting a single file
        this.deleteAction = new Ext.Action({
            text: 'Delete this input.',
            disabled: true,
            hidden: this.hideDeleteButton,
            iconCls: 'cross-icon',
            scope : this,
            handler: function() {
                var item = jobFilesGrid.getSelectionModel().getSelection()[0];
                var source = item.get('source');

                if (source instanceof vegl.models.FileRecord) {
                    Ext.Ajax.request({
                        url: 'secure/deleteFiles.do',
                        callback: Ext.bind(this.updateFileStore, this),
                        params: {
                            'fileName': source.get('name'),
                            'jobId': this.currentJobId
                        }
                    });
                } else if (source instanceof vegl.models.Download) {
                    Ext.Ajax.request({
                        url: 'secure/deleteDownloads.do',
                        callback: Ext.bind(this.updateFileStore, this),
                        params: {
                            'downloadId': source.get('id'),
                            'jobId': this.currentJobId
                        }
                    });
                }
            }
        });

        var plugins = [{
            ptype : 'rowcontextmenu',
            contextMenu : Ext.create('Ext.menu.Menu', {
                 items: [this.downloadAction, this.deleteAction]
            })
        }];
        if (!this.hideRowExpander) {
            plugins.push({
                ptype: 'rowexpander',
                rowBodyTpl : [
                    '<p>{description}</p><br>'
                ]
            });
        }

        Ext.apply(config, {
            viewConfig : {
                emptyText : Ext.isEmpty(config.emptyText) ? '<p class="centeredlabel">This job doesn\'t have any input files or service downloads configured. You can add some by using the add button below or by selecting a dataset from the <a href="gmap.html">main page</a>.</p>' : config.emptyText
            },
            features : [Ext.create('Ext.grid.feature.Grouping',{
                groupHeaderTpl: '{name} ({[values.rows.length]} {[values.rows.length > 1 ? "Items" : "Item"]})'
            })],
            plugins : plugins,
            store : Ext.create('Ext.data.Store', {
                model : 'vegl.widgets.JobFilesPanel.Item',
                groupField : 'group',
                data : []
            }),
            columns: [{
                header: 'Name',
                flex: this.hideDetailsColumn ? 1 : undefined,
                width: this.hideDetailsColumn ? undefined : this.nameColumnWidth,
                sortable: true,
                dataIndex: 'name'
            },
            { header: 'Location', width: 200, dataIndex: 'localPath', hidden: this.hideLocationColumn},
            {
                header: 'Details',
                flex: this.hideDetailsColumn ? undefined : 1,
                width: this.hideDetailsColumn ? this.detailsColumnWidth : undefined,
                dataIndex: 'details',
                hidden: this.hideDetailsColumn
            }],
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
     * @function
     */
    updateFileStore : function() {
        if (!this.currentJobId) {
            this.getStore().removeAll();
            return;
        }

        //Fire off the first request for already uploaded input files
        var loadMask = new Ext.LoadMask({
            msg : 'Requesting input files...',
            target : this,
            removeMask : true
        });
        loadMask.show();

        portal.util.Ajax.request({
            url : this.fileLookupUrl,
            params : {
                jobId : this.currentJobId
            },
            scope : this,
            callback : function(success, data, message, debugInfo) {
                if (!success) {
                    loadMask.hide();
                    portal.widgets.window.ErrorWindow.showText('Error', 'Unable to request list of files: ' + message + ' Please try refreshing the page.', debugInfo);
                    return;
                }

                var fileRecords = [];
                for (var i = 0; i < data.length; i++) {
                    fileRecords.push(Ext.create('vegl.models.FileRecord', data[i]));
                }

                //Fire off a second request for the downloads
                portal.util.Ajax.request({
                    url : 'secure/getJobDownloads.do',
                    params : {
                        jobId : this.currentJobId
                    },
                    scope : this,
                    callback : Ext.bind(function(success, data, message, debugInfo ) {
                        loadMask.hide();
                        if (!success) {
                            portal.widgets.window.ErrorWindow.showText('Error', 'Unable to request list of job downloads. Please try refreshing the page.', debugInfo);
                            return;
                        }

                        var downloads = [];
                        for (var i = 0; i < data.length; i++) {
                            downloads.push(Ext.create('vegl.models.Download', data[i]));
                        }

                        this._setStoreData(fileRecords, downloads);
                    }, this, [fileRecords], true)
                });
            }
        });
    },

    /**
     * Sets the store data from a set of vegl.models.FileRecord and vegl.models.Download objects
     * @function
     * @param {object} fileRecords
     * @param {object} downloads
     */
    _setStoreData : function(fileRecords, downloads) {
        var ds = this.getStore();
        var dsData = [];

        for (var i = 0; i < fileRecords.length; i++) {
            var fr = fileRecords[i];
            if (fr.isVlUtilityFile()) {
                continue;
            }

            dsData.push(Ext.create('vegl.widgets.JobFilesPanel.Item', {
                id : 'fr-' + fr.get('name'),
                name : fr.get('name'),
                description : 'This file will be made available to the job upon startup. It will be put in the same working directory as the job script.',
                details : Ext.util.Format.fileSize(fr.get('size')),
                localPath : 'Local directory',
                source: fr,
                group : this.fileGroupName
            }));
        }

        for (var i = 0; i < downloads.length; i++) {
            var dl = downloads[i];
            var hostNameMatches = /.*:\/\/(.*?)\//g.exec(dl.get('url'));
            var hostName = (hostNameMatches && hostNameMatches.length >= 2) ? hostNameMatches[1] : dl.get('url');

            dsData.push(Ext.create('vegl.widgets.JobFilesPanel.Item', {
                id : 'dl-' + dl.get('id'),
                name : dl.get('name'),
                description : dl.get('description'),
                details : Ext.util.Format.format('Service call to <a href="{1}" target="_blank">{0}</a>.', hostName, dl.get('url')),
                localPath : dl.get('localPath'),
                source: dl,
                group : this.remoteGroupName
            }));
        }

        ds.loadData(dsData);
    },

    /**
     * @function
     * @param view
     * @param td
     * @param cellIndex
     * @param record
     * @param tr
     * @param rowIndex
     * @param e
     * @param eOpts
     */
    _onDblClick : function(view, td, cellIndex, record, tr, rowIndex, e, eOpts) {
        var sm = this.getSelectionModel();

        this.getSelectionModel().select([record], false);
    },


    /**
     * @function
     * @param sm
     */
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
     * @function
     * @param {object} job
     */
    listFilesForJob : function(job) {
        this.currentJobId = job.get('id');
        this.updateFileStore();
        this.getSelectionModel().clearSelections();
    }
});

/**
 * Represents a generic model for containing Download or File objects.
 */
Ext.define('vegl.widgets.JobFilesPanel.Item', {
    extend: 'Ext.data.Model',

    fields: [
        { name: 'id', type: 'string' }, //Unique ID for this download/file
        { name: 'name', type: 'string' }, //short name of this download/file
        { name: 'description', type: 'string' }, //longer description of this download
        { name: 'details', type: 'string'}, //The remote URL or file size
        { name: 'localPath', type: 'string'}, //Where the file will be made available
        { name: 'source', type: 'auto'}, //Either a vegl.models.FileRecord or a vegl.models.Download object.
        { name: 'group', type: 'string'} //How is this item grouped? Unique group name
    ],

    idProperty : 'id'
});
