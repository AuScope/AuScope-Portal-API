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
            disabled: true,
            iconCls: 'disk-icon',
            handler: function() {
                var fileRecord = jobFilesGrid.getSelectionModel().getSelection()[0];

                var params = {
                    jobId : jobFilesGrid.currentJob.get('id'),
                    filename : fileRecord.get('name'),
                    key : fileRecord.get('name')
                };

                portal.util.FileDownloader.downloadFile("downloadFile.do", params);
            }
        });

        //Action for downloading one or more files in a zip
        this.downloadZipAction = new Ext.Action({
            text: 'Download as Zip',
            disabled: true,
            iconCls: 'disk-icon',
            handler: function() {
                var files = jobFilesGrid.getSelectionModel().getSelection();

                var fParam = files[0].get('name');
                for (var i = 1; i < files.length; i++) {
                    fParam += ',' + files[i].get('name');
                }

                portal.util.FileDownloader.downloadFile("downloadAsZip.do", {
                    jobId : jobFilesGrid.currentJob.get('id'),
                    files : fParam
                });
            }
        });

        Ext.apply(config, {
            plugins : [{
                ptype : 'rowcontextmenu',
                contextMenu : Ext.create('Ext.menu.Menu', {
                    items: [this.downloadAction, this.downloadZipAction]
                })
            }],
            multiSelect : true,
            store : Ext.create('Ext.data.Store', {
                model : 'vegl.models.FileRecord',
                proxy : {
                    type : 'ajax',
                    url : 'jobFiles.do',
                    reader : {
                        type : 'json',
                        root : 'data'
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

        this.on('selectionchange', this._onSelectionChange, this);
        this.on('celldblclick', this._onDblClick, this);

    },

    _onDblClick : function(view, td, cellIndex, record, tr, rowIndex, e, eOpts) {
        var sm = this.getSelectionModel();

        this.getSelectionModel().select([record], false);
        this.downloadAction.execute();
    },

    _onSelectionChange : function(sm) {
        var totalSelections = this.getSelectionModel().getSelection().length;
        if (totalSelections == 0) {
            this.downloadAction.setDisabled(true);
            this.downloadZipAction.setDisabled(true);
        } else {
            if (totalSelections != 1) {
                this.downloadAction.setDisabled(true);
            } else {
                this.downloadAction.setDisabled(false);
            }
            this.downloadZipAction.setDisabled(false);
        }
    },

    /**
     * Reloads this store with all the jobs for the specified series
     */
    listFilesForJob : function(job) {
        var store = this.getStore();
        var ajaxProxy = store.getProxy();
        ajaxProxy.extraParams.jobId = job.get('id');
        this.currentJob = job;
        store.removeAll(false);
        store.load();
    }
});