/**
 * A Ext.panel.Panel specialisation for rendering details about a single job
 *
 */
Ext.define('vegl.widgets.DetailsPanel', {
    extend : 'Ext.panel.Panel',
    alias : 'widgets.detailspanel',

    taskRunner: null,

    constructor : function(config) {
        Ext.apply(config, {
            width: '100%',
            layout: {
                type: 'vbox',
                pack: 'center',
                align: 'stretch'
            },
            items: [{
                xtype: 'container',
                itemId: 'top-container',
                height: 80,
                layout: {
                    type: 'hbox',
                    align: 'middle',
                    pack: 'center'
                },
                items: [{
                    xtype: 'datadisplayfield',
                    itemId: 'status',
                    fieldLabel: 'Status',
                    cls: 'vl-job-details',
                    margin: '0 20 0 20'
                },{
                    xtype: 'datadisplayfield',
                    itemId: 'ami',
                    fieldLabel: 'Instance ID',
                    cls: 'vl-job-details',
                    margin: '0 20 0 20'
                },{
                    xtype: 'datadisplayfield',
                    itemId: 'type',
                    fieldLabel: 'Instance Type',
                    cls: 'vl-job-details',
                    margin: '0 20 0 20'
                },{
                    xtype: 'datadisplayfield',
                    itemId: 'submitted',
                    fieldLabel: 'Submitted',
                    cls: 'vl-job-details',
                    margin: '0 20 0 20'
                }]
            },{
                xtype: 'container',
                itemId: 'bottom-container',
                flex: 1,
                margin: '10 0 0 0',
                padding: '10',
                layout: {
                    type: 'hbox',
                    align: 'stretch',
                    pack: 'center'
                },
                items: [{
                    xtype: 'joblogspanel',
                    itemId: 'logs',
                    margin: '0 5 0 0',
                    flex: 1,
                    plain: true,
                    title: 'Logs'
                },{
                    xtype: 'jobfilespanel',
                    itemId: 'files',
                    margin: '0 0 0 5',
                    flex: 1,
                    title: 'Files'
                }]
            }]
        });

        this.callParent(arguments);

        this.on('afterender', function(jdp) {
            jdp.cleanupDetails();
        });
    },

    /**
     * Updates the body of this panel with the contents of a vegl.models.Job
     */
    showDetailsForJob : function(job) {
        this.job = job;

        this.down('#top-container').setVisible(true);
        this.down('#bottom-container').setVisible(true);

        this.setTitle(job.get('name'));
        var style = vegl.widgets.JobsPanel.styleFromStatus(job.get('status'));
        this.down('#status').setValue(Ext.util.Format.format('<span title="{0}" style="color:{1};">{2}</span>', style.tip, style.color, style.text));
        this.down('#ami').setValue(job.get('computeInstanceId') ? job.get('computeInstanceId') : 'N/A');
        this.down('#type').setValue(job.get('computeInstanceType') ? job.get('computeInstanceType') : 'N/A');

        this.down('#logs').listLogsForJob(job);
        this.down('#files').listFilesForJob(job);

        this.updateSubmitTime();

        if (!this.taskRunner) {
            this.taskRunner = new Ext.util.TaskRunner();
            this.taskRunner.start({
                run: Ext.bind(this.updateSubmitTime, this),
                interval: 1000 //1 Second
            });
        }
    },

    updateSubmitTime: function() {
        if (!this.job) {
            return;
        }

        if (!this.job.get('submitDate')) {
            this.down('#submitted').setValue('N/A');
            return;
        }

        var timeString = '';
        var diffSeconds = Math.floor((new Date().getTime() - this.job.get('submitDate').getTime()) / 1000);
        if (diffSeconds < 0) {
            timeString = '???'
        } else if (diffSeconds === 0) {
            timeString = 'Now'
        } else if (diffSeconds < 60 ) {
            timeString = Ext.util.Format.format('{0} seconds ago', diffSeconds);
        } else if (diffSeconds < 60 * 60) {
            timeString = Ext.util.Format.format('{0} minute(s) ago', Math.floor(diffSeconds / 60));
        } else if (diffSeconds < 60 * 60 * 36) {
            timeString = Ext.util.Format.format('{0} hour(s) ago', Math.floor(diffSeconds / (60 * 60)));
        } else {
            timeString = Ext.util.Format.format('{0} day(s) ago', Math.floor(diffSeconds / (60 * 60 * 24)));
        }

        this.down('#submitted').setValue(timeString);
    },

    cleanupDetails: function() {
        this.job = null;
        this.setTitle('Select a job on the left to view more details');
        this.down('#top-container').setVisible(false);
        this.down('#bottom-container').setVisible(false);
    }
});