/**
 * A Ext.grid.Panel specialisation for rendering the Jobs
 * available to the current user.
 *
 * Adds the following events
 * selectjob : function(vegl.widgets.SeriesPanel panel, vegl.models.Job selection) - fires whenever a new Job is selected
 * refreshDetailsPanel : function(vegl.widgets.SeriesPanel panel, vegl.models.Series series) - fires whenever a job successfully deleted
 * jobregistered : function(vegl.widgets.SeriesPanel panel, vegl.models.Job jobRegistered) - fires whenever a new Job is registered in a remote registry
 * error : function(vegl.widgets.SereisPanel panel, String message) - fires whenever a comms error occurs
 */
Ext.define('vegl.widgets.JobsPanel', {
    extend : 'Ext.grid.Panel',
    alias : 'widget.jobspanel',

    jobSeriesFrm : null,

    currentSeries : null,
    cancelJobAction : null,
    deleteJobAction : null,
    duplicateJobAction : null,
    editJobAction : null,

    /**
     * Accepts the config for a Ext.grid.Panel along with the following additions:
     *
     * hideRegisterButton : Boolean - if true the 'register to geonetwork' button will be hidden
     */
    constructor : function(config) {
        this.jobSeriesFrm = config.jobSeriesFrm;

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
            text: 'Duplicate job',
            iconCls: 'refresh-icon',
            scope : this,
            disabled : true,
            handler: function() {
                var selection = this.getSelectionModel().getSelection();
                if (selection.length > 0) {
                    //This is needed to ensure job submission from edit or duplicate
                    //job action via Submit Jobs tab won't show up the warning
                    if (this.jobSeriesFrm != null) {
                        this.jobSeriesFrm.noWindowUnloadWarning = true;
                    }
                    this.repeatJob(selection[0]);
                }
            }
        });

        this.editJobAction = new Ext.Action({
            text: 'Edit job',
            iconCls: 'edit-icon',
            scope : this,
            disabled : true,
            handler: function() {
                var selection = this.getSelectionModel().getSelection();
                if (selection.length > 0) {
                    //This is needed to ensure job submission from edit or duplicate
                    //job action via Submit Jobs tab won't show up the warning
                    if (this.jobSeriesFrm != null) {
                        this.jobSeriesFrm.noWindowUnloadWarning = true;
                    }
                    this.editJob(selection[0]);
                }
            }
        });

        this.submitJobAction = new Ext.Action({
            text: 'Submit job',
            iconCls: 'submit-icon',
            scope : this,
            disabled : true,
            handler: function() {
                var selection = this.getSelectionModel().getSelection();
                if (selection.length > 0) {
                    this.submitJob(selection[0]);
                }
            }
        });



        var displayColumns = [{ header: 'Job Name', sortable: true, flex : 1, dataIndex: 'name'},
         { header: 'Submit Date', width: 160, sortable: true, dataIndex: 'submitDate', renderer: Ext.util.Format.dateRenderer('d M Y, H:i:s')},
         { header: 'Status', sortable: true, dataIndex: 'status', width : 100, renderer: this._jobStatusRenderer}];



        if(config.showProcessDuration==true){
            displayColumns.push({
                header: 'Processed Time Log',
                width: 320,
                sortable: true,
                dataIndex: 'processTimeLog',
                renderer:function(val){
                    var result=val.replace("Total time","<br>Total time");
                    return '<div style="white-space:normal !important;">'+ result +'</div>';
                }

            })
        }





        Ext.apply(config, {
            plugins : [{
                ptype : 'rowcontextmenu',
                contextMenu : Ext.create('Ext.menu.Menu', {
                    items: [this.cancelJobAction, this.deleteJobAction, this.duplicateJobAction, this.editJobAction, this.submitJobAction]
                })
            }],
            store : Ext.create('Ext.data.Store', {
                model : 'vegl.models.Job',
                proxy : {
                    type : 'ajax',
                    url : 'secure/listJobs.do',
                    extraParams : {seriesId : null},
                    reader : {
                        type : 'json',
                        root : 'data'
                    },
                    listeners : {
                        exception : function(proxy, response, operation) {
                            responseObj = Ext.JSON.decode(response.responseText);
                            errorMsg = responseObj.msg;
                            errorInfo = responseObj.debugInfo;
                            portal.widgets.window.ErrorWindow.showText('Error', errorMsg, errorInfo);
                        }
                    }
                }
            }),
            columns: displayColumns,
            buttons: [{
                text: 'Register to GeoNetwork',
                itemId : 'btnRegister',
                disabled : true,
                hidden : config.hideRegisterButton,
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
                menu: [this.cancelJobAction, this.deleteJobAction, this.duplicateJobAction, this.editJobAction, this.submitJobAction]
            }]
        });

        this.addEvents({
            'selectjob' : true,
            'refreshDetailsPanel' : true,
            'jobregistered' : true,
            'error' : true
        });

        this.callParent(arguments);

        this.on('select', this._onJobSelection, this);
        this.on('selectionchange', this._onSelectionChange, this);
    },


    _timeDiffinMinutes: function(d1, d2) {
        var t2 = d2.getTime();
        var t1 = d1.getTime();

        return parseInt((t2-t1)/(3600*1000));
    },


    _onSelectionChange : function(sm) {
        var selections = this.getSelectionModel().getSelection();
        if (selections.length === 0) {
            this.cancelJobAction.setDisabled(true);
            this.deleteJobAction.setDisabled(true);
            this.duplicateJobAction.setDisabled(true);
            this.submitJobAction.setDisabled(true);
            this.editJobAction.setDisabled(true);
        } else {
            // Change the job available options based on its actual status
            switch(selections[0].get('status')) {
                case vegl.models.Job.STATUS_ACTIVE:
                    this.cancelJobAction.setDisabled(false);
                    this.deleteJobAction.setDisabled(true);
                    this.duplicateJobAction.setDisabled(false);
                    this.submitJobAction.setDisabled(true);
                    this.editJobAction.setDisabled(true);
                    break;
                case vegl.models.Job.STATUS_UNSUBMITTED:
                    this.cancelJobAction.setDisabled(true);
                    this.deleteJobAction.setDisabled(false);
                    this.duplicateJobAction.setDisabled(true);
                    this.submitJobAction.setDisabled(false);
                    this.editJobAction.setDisabled(false);
                    break;
                case vegl.models.Job.STATUS_DONE:
                    this.cancelJobAction.setDisabled(true);
                    this.deleteJobAction.setDisabled(false);
                    this.duplicateJobAction.setDisabled(false);
                    this.submitJobAction.setDisabled(true);
                    this.editJobAction.setDisabled(true);
                    break;
                default:
                    // Job status is STATUS_PENDING
                    this.cancelJobAction.setDisabled(false);
                    this.deleteJobAction.setDisabled(true);
                    this.duplicateJobAction.setDisabled(false);
                    this.submitJobAction.setDisabled(true);
                    this.editJobAction.setDisabled(true);
                    break;
            }
        }
    },

    _onRegisterToGeonetwork : function(btn) {
        var selectedJob = this.getSelectionModel().getSelection()[0];

        var popup = Ext.create('Ext.window.Window', {
            id : 'jobRegisterWin',
            width : 800,
            modal : true,
            layout : 'anchor',
            title : 'Register to GeoNetwork',
            items :[{
                xtype : 'jobregister',
                id : 'jobRegisterPanel',
                job : selectedJob,
                jobId : selectedJob.get('id')
            }]
        });

        popup.on('beforerender', function() {
            //loads user contact/signature data into the form
            popup.items.getAt(0).loadFormData();
        }, this);

        popup.on('close', function() {
            //refresh selected job description
            this.fireEvent('refreshJobDescription', selectedJob);
        }, this);

        popup.show();
    },

    _onRefresh : function(btn) {
        if (this.currentSeries) {
            this.listJobsForSeries(this.currentSeries, true);
            this.queryById('btnRegister').setDisabled(true);
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
        } else if (value === vegl.models.Job.STATUS_INQUEUE) {
            return '<span style="color:green;">' + value + '</span>';
        } else if (value === vegl.models.Job.STATUS_ERROR) {
            return '<span style="color:red;">' + value + '</span>';
        }
        return value;
    },

    /**
     * Reloads this store with all the jobs for the specified series.
     *
     * series - either a vegl.models.Series object
     */
    listJobsForSeries : function(series, forceStatusRefresh) {
        this.currentSeries = series;
        var store = this.getStore();
        var ajaxProxy = store.getProxy();
        ajaxProxy.extraParams.seriesId = series.get('id');
        if (forceStatusRefresh) {
            ajaxProxy.extraParams.forceStatusRefresh = true;
        }
        store.load();
    },

    /**
     * Reloads the store with all the jobs of currently loaded series
     */
    refreshJobsForSeries : function() {
        this.getStore().load();
        this.queryById('btnRegister').setDisabled(true);
    },

    /**
     * Removes all jobs from the store and refresh the jobs panel
     */
    cleanupDataStore : function() {
        var store = this.getStore();
        store.removeAll(false);
    },

    /**
     * Cleans up the job wizard internal states including deactivating
     * any event handler(s) associated with the forms in the job wizard.
     */
    cleanupJobWizard : function(jobWizPanelId) {
        jobWizPanel = Ext.getCmp(jobWizPanelId);
        layout = jobWizPanel.getLayout();
        //Deactivate our current form
        layout.activeItem.items.get(0).fireEvent('jobWizardDeactive');
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
                    loadMask = new Ext.LoadMask(Ext.getBody(), {
                        msg : 'Cancelling Job...',
                        removeMask : true
                    });
                    loadMask.show();
                    Ext.Ajax.request({
                        url: 'secure/killJob.do',
                        params: { 'jobId': job.get('id')},
                        scope : this,
                        callback : function(options, success, response) {
                            loadMask.hide();

                            if (!success) {
                                this.fireEvent('error', this, 'There was an error communicating with the VEGL server. Please try again later.');
                                return;
                            }

                            var responseObj = Ext.JSON.decode(response.responseText);
                            if (!responseObj.success) {
                                this.fireEvent('error', this, Ext.util.Format.format('There was an error cancelling this job. {0}', responseObj.msg));
                                return;
                            }

                            //refresh our store
                            this.refreshJobsForSeries();
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
                    loadMask = new Ext.LoadMask(Ext.getBody(), {
                        msg : 'Deleting Job...',
                        removeMask : true
                    });
                    loadMask.show();
                    Ext.Ajax.request({
                        url: 'secure/deleteJob.do',
                        params: { 'jobId': job.get('id')},
                        timeout : 1000 * 60 * 5, //5 minutes defined in milli-seconds
                        scope : this,
                        callback : function(options, success, response) {
                            loadMask.hide();

                            if (!success) {
                                this.fireEvent('error', this, 'There was an error communicating with the VEGL server. Please try again later.');
                                return;
                            }

                            var responseObj = Ext.JSON.decode(response.responseText);
                            if (!responseObj.success) {
                                this.fireEvent('error', this, Ext.util.Format.format('There was an error deleting this job. {0}', responseObj.msg));
                                return;
                            }

                            //refresh our store
                            this.refreshJobsForSeries();
                            //refresh Details panel
                            this.fireEvent('refreshDetailsPanel', this, this.currentSeries);
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
            title : 'Duplicate Job Wizard',
            items :[{
                xtype : 'jobwizard',
                id : 'repeatJobPanel',
                border : false,
                wizardState : {
                    duplicateJobId : job.get('id'),
                    userAction : 'duplicate'
                },
                forms : ['vegl.jobwizard.forms.DuplicateJobForm',
                         'vegl.jobwizard.forms.JobObjectForm',
                         'vegl.jobwizard.forms.ScriptBuilderForm',
                         'vegl.jobwizard.forms.JobSubmitForm']
            }]
        });

        //On close event, (a) refreshes our store so that the cloned job
        //will be displayed on the job list panel, (b) cleans up the job
        //wizard internal states and (c) resets the noWindowUnloadWarning
        //attribute back to its default value if the repeat job action
        //is performed via Submit Jobs tab
        popup.on('close', function() {
                    this.refreshJobsForSeries();
                    this.cleanupJobWizard("repeatJobPanel");
                    if (this.jobSeriesFrm != null) {
                        this.jobSeriesFrm.noWindowUnloadWarning = false;
                    }
                }, this);

        popup.show();
    },

    editJob : function(job) {
        var popup = Ext.create('Ext.window.Window', {
            width : 800,
            height : 600,
            modal : true,
            layout : 'fit',
            title : 'Edit Job Wizard',
            items : [{
                xtype : 'jobwizard',
                id : 'editJobPanel',
                border : false,
                wizardState : {
                    jobId : job.get('id'),
                    seriesId : job.get('seriesId'),
                    userAction : 'edit'
                },
                forms : ['vegl.jobwizard.forms.JobObjectForm',
                         'vegl.jobwizard.forms.JobUploadForm',
                         'vegl.jobwizard.forms.ScriptBuilderForm',
                         'vegl.jobwizard.forms.JobSubmitForm']
            }]
        });

        //on close event, (a) cleans up the job wizard internal states and
        //(b) resets the noWindowUnloadWarning attribute back to its default
        //value if the edit job action is performed via Submit Jobs tab
        popup.on('close', function() {
                    this.cleanupJobWizard("editJobPanel");
                    if (this.jobSeriesFrm != null) {
                        this.jobSeriesFrm.noWindowUnloadWarning = false;
                    }
                }, this);

        popup.show();
    },



    /**
     * Submits the specified job to the cloud for processing
     *
     * job - A vegl.models.Job object that will be submitted
     */
    submitJob : function(job) {
        var loadMask = new Ext.LoadMask(Ext.getBody(), {
            msg : 'Submitting Job...',
            removeMask : true
        });
        loadMask.show();
        Ext.Ajax.request({
            url : 'secure/submitJob.do',
            params : {
                jobId : job.get('id')
            },
            timeout : 1000 * 60 * 5, //5 minutes defined in milli-seconds
            scope : this,
            callback : function(options, success, response) {
                loadMask.hide();
                var responseObj;
                var error = true;
                if (success) {
                    responseObj = Ext.JSON.decode(response.responseText);
                    if (responseObj.success) {
                        error = false;
                    }
                }

                this.listJobsForSeries(this.currentSeries);

                if (error) {
                    //Create an error object and pass it to custom error window
                    var errorObj = {
                        title : 'Failure',
                        message : responseObj.msg,
                        info : responseObj.debugInfo
                    };

                    var errorWin = Ext.create('portal.widgets.window.ErrorWindow', {
                        errorObj : errorObj
                    });
                    errorWin.show();
                }
            }
        });
    }
});