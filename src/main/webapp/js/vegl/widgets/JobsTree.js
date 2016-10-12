/**
 * A tree specialisation for rendering the Jobs and their folders
 * that are available to the current user.
 *
 * Adds the following events
 * selectjob : function(vegl.widgets.SeriesPanel panel, vegl.models.Job job) - fires whenever a new Job is selected
 * refreshDetailsPanel : function(vegl.widgets.SeriesPanel panel) - fires whenever a job successfully deletes/updates
 * error : function(vegl.widgets.SereisPanel panel, String message) - fires whenever a comms error occurs
 */
Ext.define('vegl.widgets.JobsTree', {
    extend : 'Ext.tree.Panel',
    alias : 'widget.jobstree',

    jobSeriesFrm : null,

    currentSeries : null,
    cancelJobAction : null,
    deleteJobAction : null,
    duplicateJobAction : null,
    editJobAction : null,

    /**
     * Accepts the config for a Ext.tree.Panel along with the following additions:
     *
     * rootNode : Root node for the folder/job organisation
     * jobStore : store containing all jobs
     * hideSettings: if true, the settings column will be hidden
     */
    constructor : function(config) {
        this.jobStore = config.jobStore;

        this.cancelJobAction = new Ext.Action({
            text: 'Cancel',
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
            text: 'Delete',
            iconCls: 'trash-icon',
            scope : this,
            disabled : true,
            handler: function() {
            	var selection = this.getSelectionModel().getSelection();
            	if(selection.length > 0) {
	                var confirmationTitle = 'Delete Jobs';
	                var confirmationMessage = 'Are you sure you want to delete all selected jobs and folders?';
	                if (selection.length === 1) {
	                    if (selection[0].get('leaf')) {
	                    	confirmationTitle = 'Delete Job';
	                    	confirmationMessage = Ext.util.Format.format('Are you sure you want to delete the job <b>{0}</b>?', selection[0].get('name'));
	                    } else {
	                    	confirmationTitle = 'Delete Series';
	                    	confirmationMessage = Ext.util.Format.format('Are you sure you want to delete the folder <b>{0}</b> and its jobs?<br><ul>', selection[0].get('name'));
	                    }
	                }	                
	                Ext.Msg.show({
	                    title: confirmationTitle,
	                    msg: confirmationMessage,
	                    buttons: Ext.Msg.YESNO,
	                    icon: Ext.Msg.WARNING,
	                    modal: true,
	                    closable: false,
	                    scope : this,
	                    fn: function(btn) {
	                        if (btn == 'yes') {
	                        	for(i=0; i<selection.length; i++) {
	        	                	if (selection[i].get('leaf'))
	        	                    	this.deleteJob(selection[i]);
	        	                    else
	        	                    	this.deleteSeries(selection[i]);
	        	                }
	                        }
	                    }
	                });
            	}
            }
        });

        this.duplicateJobAction = new Ext.Action({
            text: 'Duplicate',
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
            text: 'Edit',
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
            text: 'Submit',
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

        Ext.apply(config, {
            rootVisible: false,
            store : Ext.create('Ext.data.TreeStore', {
                root: config.rootNode,
                fields: [{ name: 'id', type: 'string'},
                         { name: 'name', type: 'string'},
                         { name: 'status', type: 'string'},
                         { name: 'submitDate', type: 'date', convert: function(value, record) {
                            if (!value) {
                                return null;
                            } else {
                                return new Date(value.time);
                            }
                }}],
                sorters: [{
                    direction: 'DESC',
                    sorterFn: function(node1, node2) {
                        var isFolder1 = !Ext.isNumber(node1.get('id'));
                        var isFolder2 = !Ext.isNumber(node2.get('id'));
                        if (isFolder1 || isFolder2) {
                            if (isFolder1 && isFolder2) {
                                return node1.get('name').localeCompare(node2.get('name'));
                            } else if (isFolder1) {
                                return 1;
                            } else {
                                return -1;
                            }
                        }

                        var submitDate1 = node1.get('submitDate');
                        var submitDate2 = node2.get('submitDate');
                        if (!submitDate1 || !submitDate2) {
                            if (!submitDate1 && !submitDate2) {
                                return node1.get('id') - node2.get('id');
                            } else if (!submitDate1) {
                                return -1;
                            } else {
                                return 1;
                            }
                        }

                        return submitDate1.getTime() - submitDate2.getTime();
                    }
                }]
            }),
            columns: [{
                xtype: 'treecolumn',
                header: 'Job Name',
                sortable: true,
                flex : 1,
                dataIndex: 'name'
            },{
                 header: 'Submit Date',
                 width: 160,
                 sortable: true,
                 dataIndex: 'submitDate',
                 renderer: Ext.util.Format.dateRenderer('d M Y, H:i:s')
            },{
                 header: 'Status',
                 sortable: true,
                 dataIndex: 'status',
                 width : 100,
                 renderer: this._jobStatusRenderer
            },{
                xtype: 'actioncolumn',
                width: 50,
                hidden: config.hideSettings ? true : false,
                items: [{
                    iconCls: 'setting-icon',
                    tooltip: 'Actions for this job / folder.',
                    scope: this,
                    handler: function(grid, rowIndex, colIndex, item, e, node, row) {
                    	// If more than 1 selection has been made, show menu without selecting row
                    	if(grid.getSelectionModel().getCount()<2) {
                    		grid.getSelectionModel().select(node);
                    	}
                    	
                        var items = [this.deleteJobAction];
                        if (!this.submitJobAction.isDisabled()) {
                            items.push(this.submitJobAction);
                        }

                        if (!this.editJobAction.isDisabled()) {
                            items.push(this.editJobAction);
                        }

                        if (!this.duplicateJobAction.isDisabled()) {
                            items.push(this.duplicateJobAction);
                        }

                        Ext.create('Ext.menu.Menu', {
                            width: 100,
                            items: items
                        }).showAt(e.getXY());
                    }
                }]
            }]
        });


        this.callParent(arguments);

        this.on('select', this._onJobSelection, this);
        this.on('selectionchange', this._onSelectionChange, this);
        this.on('itemclick', function(view, node) {
            if(node.isLeaf()) {

            } else if(node.isExpanded()) {

            } else {
                node.expand();
            }
        });
    },

    _onSelectionChange : function(sm) {
        var selections = this.getSelectionModel().getSelection();
        if (selections.length === 0) {
            this.cancelJobAction.setDisabled(true);
            this.deleteJobAction.setDisabled(true);
            this.duplicateJobAction.setDisabled(true);
            this.submitJobAction.setDisabled(true);
            this.editJobAction.setDisabled(true);
        } else if(selections.length > 1) {
        	// Currently only allow deletion of multiple jobs if none are active
        	this.cancelJobAction.setDisabled(true);
            this.deleteJobAction.setDisabled(false);
            this.duplicateJobAction.setDisabled(true);
            this.submitJobAction.setDisabled(true);
            this.editJobAction.setDisabled(true);
        } else if (!Ext.isNumber(Number(selections[0].get('id')))) {
            this.cancelJobAction.setDisabled(true);
            this.deleteJobAction.setDisabled(false);
            this.duplicateJobAction.setDisabled(true);
            this.submitJobAction.setDisabled(true);
            this.editJobAction.setDisabled(true);
        } else {
            // Change the job available options based on its actual status
            switch(selections[0].get('status')) {
                case vegl.models.Job.STATUS_ACTIVE:
                    this.cancelJobAction.setDisabled(false);
                    this.deleteJobAction.setDisabled(false);
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
                case vegl.models.Job.STATUS_ERROR:
                case vegl.models.Job.STATUS_WALLTIME_EXCEEDED:
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
                    this.deleteJobAction.setDisabled(false);
                    this.duplicateJobAction.setDisabled(false);
                    this.submitJobAction.setDisabled(true);
                    this.editJobAction.setDisabled(true);
                    break;
            }
        }
    },

    _onRefresh : function(btn) {
        if (this.currentSeries) {
            this.listJobsForSeries(this.currentSeries, true);
            this.queryById('btnRegister').setDisabled(true);
        }
    },

    _onJobSelection : function(sm, jobNode) {
        var job = this.jobStore.getById(jobNode.get('id'));
        this.fireEvent('selectjob', this, job);
    },

    _jobStatusRenderer : function(value, cell, record) {
        var style = vegl.widgets.JobsPanel.styleFromStatus(value);
        return Ext.util.Format.format('<span title="{0}" style="color:{1};">{2}</span>', style.tip, style.color, style.text);
    },

    /**
     * Removes all jobs from the store and refresh the jobs panel
     */
    cleanupDataStore : function() {
        var store = this.getStore();
        store.removeAll(false);
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
                    Ext.getBody().mask('Cancelling Job...');

                    Ext.Ajax.request({
                        url: 'secure/killJob.do',
                        params: { 'jobId': job.get('id')},
                        scope : this,
                        callback : function(options, success, response) {
                            Ext.getBody().unmask();
                            if (!success) {
                                this.fireEvent('error', this, 'There was an error communicating with the VL server. Please try again later.');
                                return;
                            }

                            var responseObj = Ext.JSON.decode(response.responseText);
                            if (!responseObj.success) {
                                this.fireEvent('error', this, Ext.util.Format.format('There was an error cancelling this job. {0}', responseObj.msg));
                                return;
                            }

                            job.set('status', vegl.models.Job.STATUS_CANCELLED);
                            this.jobStore.getById(job.get('id')).set('status', vegl.models.Job.STATUS_CANCELLED);

                            this.fireEvent('refreshDetailsPanel', this, job);
                        }
                    });
                }
            }
        });
    },
    
    deleteSeries : function(series) {
        Ext.getBody().mask('Deleting Folder...');

        Ext.Ajax.request({
            url: 'secure/deleteSeriesJobs.do',
            params: { 'seriesId': series.get('seriesId')},
            timeout : 1000 * 60 * 5, //5 minutes defined in milli-seconds
            scope : this,
            callback : function(options, success, response) {
                Ext.getBody().unmask();

                if (!success) {
                    this.fireEvent('error', this, 'There was an error communicating with the VL server. Please try again later.');
                    return;
                }

                var responseObj = Ext.JSON.decode(response.responseText);
                if (!responseObj.success) {
                    this.fireEvent('error', this, Ext.util.Format.format('There was an error deleting this series. {0}', responseObj.msg));
                    return;
                }

                Ext.each(series.childNodes, function(jobNode) {
                    this.jobStore.remove(this.jobStore.getById(jobNode.get('id')));
                    this.getStore().remove(jobNode);
                }, this);

                this.getStore().remove(series);
                this.fireEvent('refreshDetailsPanel', this, null);
            }
        });
    },
    
    deleteJob : function(job) {
    	Ext.getBody().mask('Deleting Job...');

        Ext.Ajax.request({
            url: 'secure/deleteJob.do',
            params: { 'jobId': job.get('id')},
            timeout : 1000 * 60 * 5, //5 minutes defined in milli-seconds
            scope : this,
            callback : function(options, success, response) {
                Ext.getBody().unmask();

                if (!success) {
                    this.fireEvent('error', this, 'There was an error communicating with the VL server. Please try again later.');
                    return;
                }

                var responseObj = Ext.JSON.decode(response.responseText);
                if (!responseObj.success) {
                    this.fireEvent('error', this, Ext.util.Format.format('There was an error deleting this job. {0}', responseObj.msg));
                    return;
                }

                this.jobStore.remove(this.jobStore.getById(job.get('id')));
                this.getStore().remove(job);

                //refresh Details panel
                job.set('status', vegl.models.Job.STATUS_DELETED);
                this.fireEvent('refreshDetailsPanel', this, job);
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
                forms : [
                         'vegl.jobwizard.forms.DuplicateJobForm',
                         'vegl.jobwizard.forms.ScriptBuilderForm',
                         'vegl.jobwizard.forms.JobObjectForm',
                         'vegl.jobwizard.forms.JobSubmitForm'
                 ]
            }]
        });

        //On close event, (a) refreshes our store so that the cloned job
        //will be displayed on the job list panel, (b) cleans up the job
        //wizard internal states and (c) resets the noWindowUnloadWarning
        //attribute back to its default value if the repeat job action
        //is performed via Submit Jobs tab
        popup.on('close', function() {
                    this.fireEvent('refreshjobs', this);
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
                forms : [
                         'vegl.jobwizard.forms.JobUploadForm',
                         'vegl.jobwizard.forms.ScriptBuilderForm',
                         'vegl.jobwizard.forms.JobObjectForm',
                         'vegl.jobwizard.forms.JobSubmitForm'
                 ]
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
     * Cleans up the job wizard internal states including deactivating
     * any event handler(s) associated with the forms in the job wizard.
     */
    cleanupJobWizard : function(jobWizPanelId) {
        jobWizPanel = Ext.getCmp(jobWizPanelId);
        layout = jobWizPanel.getLayout();
        //Deactivate our current form
        layout.activeItem.items.get(0).fireEvent('jobWizardDeactive');
    },

    /**
     * Submits the specified job to the cloud for processing
     *
     * job - A vegl.models.Job object that will be submitted
     */
    submitJob : function(job) {

        Ext.getBody().mask('Submitting Job...');

        Ext.Ajax.request({
            url : 'secure/submitJob.do',
            params : {
                jobId : job.get('id')
            },
            timeout : 1000 * 60 * 5, //5 minutes defined in milli-seconds
            scope : this,
            callback : function(options, success, response) {
                Ext.getBody().unmask();
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