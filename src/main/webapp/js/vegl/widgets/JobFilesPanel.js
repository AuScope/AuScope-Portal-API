/**
 * A Ext.grid.Panel specialisation for rendering the Jobs
 * available to the current user.
 *
 * Adds the following events
 * selectjob : function(vegl.widgets.SeriesPanel panel, vegl.models.Job selection) - fires whenever a new Job is selected
 */
Ext.define('vegl.widgets.JobFilesPanel', {
    extend : 'Ext.grid.Panel',
    alias : 'widget.jobfilespanel',

    currentJob : null,
    downloadAction : null,
    downloadZipAction : null,

    constructor : function(config) {
        var jobFilesGrid = this;

        //Action for downloading a single file
        this.downloadAction = new Ext.Action({
            text: 'Download',
            iconCls: 'disk-icon',
            handler: function() {
                var files = jobFilesGrid.getSelectionModel().getSelection();
                if (files.length === 0) {
                    return;
                }
                var fileRecord = files[0];

                var params = {
                    jobId : jobFilesGrid.currentJob.get('id'),
                    filename : fileRecord.get('name'),
                    key : fileRecord.get('name')
                };

                portal.util.FileDownloader.downloadFile("secure/downloadFile.do", params);
            }
        });

        //Action for downloading one or more files in a zip
        this.downloadZipAction = new Ext.Action({
            text: 'Download as Zip',
            iconCls: 'disk-icon',
            handler: function() {
                var files = jobFilesGrid.getSelectionModel().getSelection();
                if (files.length === 0) {
                    return;
                }

                var fParam = files[0].get('name');
                for (var i = 1; i < files.length; i++) {
                    fParam += ',' + files[i].get('name');
                }

                portal.util.FileDownloader.downloadFile("secure/downloadAsZip.do", {
                    jobId : jobFilesGrid.currentJob.get('id'),
                    files : fParam
                });
            }
        });

        Ext.apply(config, {
            plugins : [{
                ptype : 'inlinecontextmenu',
                align : 'left',
                recordIdProperty: 'name',
                allowMultipleOpen: false,
                actions : [this.downloadAction]
            }],
            multiSelect : true,
            store : Ext.create('Ext.data.Store', {
                model : 'vegl.models.FileRecord',
                proxy : {
                    type : 'ajax',
                    url : 'secure/jobFiles.do',
                    reader : {
                        type : 'json',
                        rootProperty : 'data'
                    },
                    listeners : {
                        exception : function(proxy, response, operation) {
                            responseObj = Ext.JSON.decode(response.responseText);
                            if (response.responseObj) {
                                errorMsg = responseObj.msg;
                                errorInfo = responseObj.debugInfo;
                                portal.widgets.window.ErrorWindow.showText('Error', errorMsg, errorInfo);
                            }
                        }
                    }
                }
            }),
            columns: [{ header: 'Filename', width: 200, sortable: true, dataIndex: 'name'},
                      { header: 'Size', width: 100, sortable: true, dataIndex: 'size', renderer: Ext.util.Format.fileSize, align: 'right'}],
            tbar: [{
                text: 'Actions',
                iconCls: 'folder-icon',
                menu: [ this.downloadAction, this.downloadZipAction]
            }]
        });

        this.callParent(arguments);
        this.on('celldblclick', this._onDblClick, this);

    },

    _onDblClick : function(view, td, cellIndex, record, tr, rowIndex, e, eOpts) {
        var sm = this.getSelectionModel();

        this.getSelectionModel().select([record], false);
        this.downloadAction.execute();
    },

    /**
     * Reloads this store with all the jobs for the specified series
     */
    listFilesForJob : function(job) {
        var store = this.getStore();
        var ajaxProxy = store.getProxy();
        ajaxProxy.extraParams.jobId = job.get('id');
        ajaxProxy.abort(); //Stop loading any previous job files
        this.currentJob = job;
        store.removeAll(false);
        store.load();
    },

    /**
     * Removes all files from the store and refresh the job files panel
     */
    cleanupDataStore : function() {
        var store = this.getStore();
        store.removeAll(false);
    }
});