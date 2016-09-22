/**
 * Job wizard form for handling duplication of an already run job.
 *
 * Author - Josh Vote
 */
Ext.define('vegl.jobwizard.forms.DuplicateJobForm', {
    extend : 'vegl.jobwizard.forms.BaseJobWizardForm',

    cloudFilesStore : null,
    fileGrid : null,

    /**
     * Creates a new JobUploadForm form configured to write/read to the specified global state
     */
    constructor: function(wizardState) {
        var me = this;

        var downloadAction = new Ext.Action({
            text: 'Download',
            disabled: true,
            iconCls: 'disk-icon',
            handler: Ext.bind(this.downloadFile, this)
        });

        //Store for uploaded file details
        this.cloudFilesStore = Ext.create('Ext.data.Store', {
            model : 'vegl.models.FileRecord',
            autoLoad : true,
            filters : [
                function(item) {
                    //These three files should not be copyable
                    return item.get('name') !== 'vl.sh.log' &&
                           item.get('name') !== 'workflow-version.txt' &&
                           item.get('name') !== 'vl.end';
                }
            ],
            proxy : {
                type : 'ajax',
                url : 'secure/jobCloudFiles.do',
                extraParams : {
                    jobId : wizardState.jobId
                },
                reader : {
                    type : 'json',
                    rootProperty : 'data'
                }
            },
            listeners: {
                load: Ext.bind(function(store, records) {
                    var rec = store.findRecord('name', 'vl_script.py');
                    if (rec) {
                        this.fileGrid.getSelectionModel().select([rec]);
                    }
                }, this)
            }
        });

        this.fileGrid = Ext.create('Ext.grid.Panel', {
            title: 'Available files',
            store: this.cloudFilesStore,
            viewConfig : {
                emptyText : '<p class="centeredlabel">This job doesn\'t have any input/output files to copy.</p>'
            },
            stripeRows: true,
            anchor: '100% -20',
            selModel : Ext.create('Ext.selection.CheckboxModel', {}),
            columns: [
                { header: 'Filename', width: 200, sortable: true, dataIndex: 'name' },
                { header: 'Size', width: 100, sortable: true, dataIndex: 'size',
                    renderer: Ext.util.Format.fileSize, align: 'right' }
            ]
        });

        this.callParent([{
            wizardState : wizardState,
            header : false,
            bodyStyle: 'padding:10px;',
            fileUpload: true,
            frame: true,
            listeners : {
                jobWizardActive : Ext.bind(this.onActive, this)
            },
            items: [
                this.fileGrid
            ]
        }]);
    },

    onActive : function() {
        //If we already have a jobId when this form activates, get rid of it.
        //When we move past this form we will be creating a new job with this ID
        if (this.wizardState.jobId) {
            this.deleteJobWithId(this.wizardState.jobId);
        }

        this.updateFileList();
    },

    deleteJobWithId : function(id) {

        Ext.getBody().mask('Removing duplicate job...').setStyle('z-index', '99999');

        //Tell the backend to remove duplicate job
        Ext.Ajax.request({
            url : 'secure/deleteJob.do',
            params : {
                jobId : id
            },
            scope : this,
            callback : function(options, success, response) {
                Ext.getBody().unmask();
            }
        });
    },

    //Refresh the server side file list
    updateFileList : function() {
        var ajaxProxy = this.cloudFilesStore.getProxy();
        ajaxProxy.extraParams.jobId = this.wizardState.duplicateJobId;
        this.cloudFilesStore.load();
    },

    //Validate by performing the job duplication
    beginValidation : function(callback) {
        //Don't duplicate more than once
        if (this.performedDuplication) {
            callback(true);
            return;
        }

        //Workout what files to duplicate
        var filesToDuplicate = [];
        var selectedFiles = this.fileGrid.getSelectionModel().getSelection();
        for (var i = 0; i < selectedFiles.length; i++) {
            filesToDuplicate.push(selectedFiles[i].get('name'));
        }
        var jobId = this.wizardState.duplicateJobId;

        Ext.getBody().mask('Duplicating Job...').setStyle('z-index', '99999');;

        //Tell the backend to duplicate
        Ext.Ajax.request({
            url : 'secure/duplicateJob.do',
            params : {
                jobId : jobId,
                file : filesToDuplicate
            },
            scope : this,
            callback : function(options, success, response) {
                Ext.getBody().unmask();

                if (success) {
                    var responseObj = Ext.JSON.decode(response.responseText);
                    if (responseObj.success && Ext.isNumber(responseObj.data[0].id)) {
                        this.wizardState.jobId = responseObj.data[0].id;
                        this.wizardState.seriesId = responseObj.data[0].seriesId;
                        this.wizardState.solutionId = responseObj.data[0].solutionId;
                        this.wizardState.solutions = responseObj.data[0].jobSolutions;
                        callback(true);
                        return;
                    }
                }

                Ext.Msg.alert('Create new series','There was an internal error duplicating your job. Please try again in a few minutes.');
                callback(false);
            }
        });
    },

    getTitle : function() {
        return "Select files to copy into the new job...";
    }
});