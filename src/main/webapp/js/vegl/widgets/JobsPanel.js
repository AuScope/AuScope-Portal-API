/**
 * A Ext.grid.Panel specialisation for rendering the Jobs
 * available to the current user.
 *
 * Adds the following events
 * selectjob : function(vegl.widgets.SeriesPanel panel, vegl.models.Job selection) - fires whenever a new Job is selected
 * jobregistered : function(vegl.widgets.SeriesPanel panel, vegl.models.Job jobRegistered) - fires whenever a new Job is registered in a remote registry
 * error : function(vegl.widgets.SereisPanel panel, String message) - fires whenever a comms error occurs
 */
Ext.define('vegl.widgets.JobsPanel', {
    extend : 'Ext.grid.Panel',
    alias : 'widget.jobspanel',

    currentSeries : null,
    cancelJobAction : null,
    deleteJobAction : null,
    duplicateJobAction : null,

    constructor : function(config) {
        this.cancelJobAction = new Ext.Action({
            text: 'Cancel job',
            iconCls: 'cross-icon',
            scope : this,
            disabled : true,
            handler: function() {
                var selection = this.getSelectionModel().getSelection();
                if (selection.length > 0) {
                    this.cancelJob(selection[0]);
                }
            }
        });

        this.deleteJobAction = new Ext.Action({
            text: 'Delete job',
            iconCls: 'cross-icon',
            scope : this,
            disabled : true,
            handler: function() {
                var selection = this.getSelectionModel().getSelection();
                if (selection.length > 0) {
                    this.deleteJob(selection[0]);
                }
            }
        });

        this.duplicateJobAction = new Ext.Action({
            text: 'Repeat job',
            iconCls: 'refresh-icon',
            scope : this,
            disabled : true,
            handler: function() {
                var selection = this.getSelectionModel().getSelection();
                if (selection.length > 0) {
                    this.repeatJob(selection[0]);
                }
            }
        });

        Ext.apply(config, {
            plugins : [{
                ptype : 'rowcontextmenu',
                contextMenu : Ext.create('Ext.menu.Menu', {
                    items: [this.cancelJobAction, this.deleteJobAction, this.duplicateJobAction]
                })
            }],
            store : Ext.create('Ext.data.Store', {
                model : 'vegl.models.Job',
                proxy : {
                    type : 'ajax',
                    url : 'listJobs.do',
                    extraParams : {seriesId : null},
                    reader : {
                        type : 'json',
                        root : 'data'
                    }
                }
            }),
            columns: [{ header: 'Job Name', sortable: true, flex : 1, dataIndex: 'name'},
                      { header: 'Submit Date', width: 160, sortable: true, dataIndex: 'submitDate', renderer: Ext.util.Format.dateRenderer('d M Y, H:i:s')},
                      { header: 'Status', sortable: true, dataIndex: 'status', width : 100, renderer: this._jobStatusRenderer}],
            buttons: [{
                text: 'Register to GeoNetwork',
                itemId : 'btnRegister',
                disabled : true,
                tooltip: 'Register the job result into GeoNetwork',
                handler: Ext.bind(this._onRegisterToGeonetwork, this)
            },{
                text: 'Refresh',
                itemId : 'btnRefresh',
                tooltip : 'Refresh the list of jobs for the selected series',
                iconCls: 'refresh-icon',
                handler: Ext.bind(this._onRefresh, this)
            }],
            tbar: [{
                text: 'Actions',
                iconCls: 'folder-icon',
                menu: [ this.cancelJobAction, this.deleteJobAction, this.duplicateJobAction]
            }]
        });

        this.addEvents({
            'selectjob' : true,
            'jobregistered' : true,
            'error' : true
        });

        this.callParent(arguments);

        this.on('select', this._onJobSelection, this);
        this.on('selectionchange', this._onSelectionChange, this);
    },

    _onSelectionChange : function(sm) {
        var selections = this.getSelectionModel().getSelection();
        if (selections.length == 0) {
            this.cancelJobAction.setDisabled(true);
            this.deleteJobAction.setDisabled(true);
            this.duplicateJobAction.setDisabled(true);
        } else {
            this.cancelJobAction.setDisabled(false);
            this.deleteJobAction.setDisabled(false);
            this.duplicateJobAction.setDisabled(false);

            switch(selections[0].get('status')) {
            case vegl.models.Job.STATUS_ACTIVE:
                this.deleteJobAction.setDisabled(true);
                break;
            case vegl.models.Job.STATUS_UNSUBMITTED:
                this.duplicateJobAction.setDisabled(true);
                break;
            default:
                this.cancelJobAction.setDisabled(true);
                break;
            }
        }
    },

    _onRegisterToGeonetwork : function(btn) {
        var selectedJob = this.getSelectionModel().getSelection()[0];
        Ext.Ajax.request({
            url: 'insertRecord.do',
            params: {jobId: selectedJob.get('id') },
            scope : this,
            callback : function(options, success, response) {
                if (!success) {
                    this.fireEvent('error', this, 'There was an error communicating with the VEGL server. Please try again later.');
                    return;
                }

                var responseObj = Ext.JSON.decode(response.responseText);
                if (!responseObj.success) {
                    this.fireEvent('error', this, Ext.util.Format.format('There was an error registering this job with Geonetwork. {0}', responseObj.msg));
                    return;
                }

                this.getStore().load();//refresh our store
                this.fireEvent('jobregistered', this, selectedJob);
            }
        });
    },

    _onRefresh : function(btn) {
        if (this.currentSeries) {
            this.listJobsForSeries(this.currentSeries);
        }
    },

    _onJobSelection : function(sm, job) {
        var allowedToRegister = (job.get('status') === vegl.models.Job.STATUS_DONE) && Ext.isEmpty(job.get('registeredUrl'));
        this.queryById('btnRegister').setDisabled(!allowedToRegister);

        this.fireEvent('selectjob', this, job);
    },

    _jobStatusRenderer : function(value, cell, record) {
        if (value === vegl.models.Job.STATUS_FAILED) {
            return '<span style="color:red;">' + value + '</span>';
        } else if (value === vegl.models.Job.STATUS_ACTIVE) {
            return '<span style="color:green;">' + value + '</span>';
        } else if (value === vegl.models.Job.STATUS_DONE) {
            return '<span style="color:blue;">' + value + '</span>';
        } else if (value === vegl.models.Job.STATUS_PENDING) {
            return '<span style="color:#e59900;">' + value + '</span>';
        }
        return value;
    },

    /**
     * Reloads this store with all the jobs for the specified series
     */
    listJobsForSeries : function(series) {
        this.currentSeries = series;
        var store = this.getStore();
        var ajaxProxy = store.getProxy();
        ajaxProxy.extraParams.seriesId = series.get('id');
        store.load();
    },

    cancelJob : function(job) {
        Ext.Msg.show({
            title: 'Cancel Job',
            msg: 'Are you sure you want to cancel the selected job?',
            buttons: Ext.Msg.YESNO,
            icon: Ext.Msg.WARNING,
            modal: true,
            closable: false,
            scope : this,
            fn: function(btn) {
                if (btn == 'yes') {
                    Ext.Ajax.request({
                        url: 'killJob.do',
                        params: { 'jobId': job.get('id')},
                        scope : this,
                        callback : function(options, success, response) {
                            if (!success) {
                                this.fireEvent('error', this, 'There was an error communicating with the VEGL server. Please try again later.');
                                return;
                            }

                            var responseObj = Ext.JSON.decode(response.responseText);
                            if (!responseObj.success) {
                                this.fireEvent('error', this, Ext.util.Format.format('There was an error cancelling this job. {0}', responseObj.msg));
                                return;
                            }

                            this.getStore().load();//refresh our store
                        }
                    });
                }
            }
        });
    },

    deleteJob : function(job) {
        Ext.Msg.show({
            title: 'Delete Job',
            msg: 'Are you sure you want to delete the selected job?',
            buttons: Ext.Msg.YESNO,
            icon: Ext.Msg.WARNING,
            modal: true,
            closable: false,
            scope : this,
            fn: function(btn) {
                if (btn == 'yes') {
                    Ext.Ajax.request({
                        url: 'deleteJob.do',
                        params: { 'jobId': job.get('id')},
                        scope : this,
                        callback : function(options, success, response) {
                            if (!success) {
                                this.fireEvent('error', this, 'There was an error communicating with the VEGL server. Please try again later.');
                                return;
                            }

                            var responseObj = Ext.JSON.decode(response.responseText);
                            if (!responseObj.success) {
                                this.fireEvent('error', this, Ext.util.Format.format('There was an error deleting this job. {0}', responseObj.msg));
                                return;
                            }

                            this.getStore().load();//refresh our store
                        }
                    });
                }
            }
        });
    },

    repeatJob : function(job) {
        var popup = Ext.create('Ext.window.Window', {
            width : 800,
            height : 600,
            modal : true,
            layout : 'fit',
            title : 'Repeat Job Wizard',
            items :[{
                xtype : 'jobwizard',
                border : false,
                wizardState : {
                    duplicateJobId : job.get('id')
                },
                forms : ['vegl.jobwizard.forms.DuplicateJobForm',
                         'vegl.jobwizard.forms.JobObjectForm',
                         'vegl.jobwizard.forms.JobSubmitForm']
            }]
        });
        popup.show();
    }
});