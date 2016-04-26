/**
 * A Ext.panel.Panel specialisation for rendering details about a single job
 *
 */
Ext.define('vegl.widgets.DetailsPanel', {
    extend : 'Ext.panel.Panel',
    alias : 'widget.detailspanel',

    taskRunner: null,

    /**
     *
     */
    constructor : function(config) {
        Ext.apply(config, {
            width: '100%',
            layout: {
                type: 'auto',
            },
            plugins: 'responsive',
            responsiveConfig: {
                small: {
                    scrollable: 'vertical'
                },
                normal: {
                    scrollable: false
                }
            },
            items: [{
                xtype: 'container',
                itemId: 'top-container',
                plugins: 'responsive',
                responsiveConfig: {
                    small: {
                        layout: {
                            type: 'hbox',
                            align: 'middle',
                            pack: 'center',
                            vertical: true
                        }
                    },
                    normal: {
                        layout: {
                            type: 'hbox',
                            align: 'middle',
                            pack: 'center',
                            vertical: false
                        }
                    }
                },
                items: [{
                    xtype: 'datadisplayfield',
                    itemId: 'status',
                    fieldLabel: 'Status',
                    cls: 'vl-job-details',
                    margin: '0 10 0 10'
                },{
                    xtype: 'datadisplayfield',
                    itemId: 'jobid',
                    fieldLabel: 'Job ID',
                    cls: 'vl-job-details',
                    margin: '0 10 0 10'
                },{
                    xtype: 'datadisplayfield',
                    itemId: 'ami',
                    fieldLabel: 'Instance ID',
                    cls: 'vl-job-details',
                    margin: '0 10 0 10'
                },{
                    xtype: 'datadisplayfield',
                    itemId: 'type',
                    fieldLabel: 'Instance Type',
                    cls: 'vl-job-details',
                    margin: '0 10 0 10'
                },{
                    xtype: 'datadisplayfield',
                    itemId: 'submitted',
                    fieldLabel: 'Submitted',
                    cls: 'vl-job-details',
                    margin: '0 10 0 10'
                }]
            },{
                xtype: 'container',
                itemId: 'bottom-container',
                margin: '10 0 0 0',
                padding: '5',
                flex: 1,
                height: '100%',
                style: 'text-align:center;',
                layout: {
                    type: 'column'
                },
                items: [{
                    xtype: 'joblogspanel',
                    itemId: 'logs',
                    minWidth: 300,
                    plugins: 'responsive',
                    responsiveConfig: {
                        small: {
                            maxWidth: 700,
                            style: 'float:none;margin: 5px auto;display:block;',
                            columnWidth: 1,
                            height: 400
                        },
                        normal: {
                            maxWidth: 600,
                            style: 'float:none;margin: 5px;display:inline-block;',
                            columnWidth: 0.5,
                            height: '100%'
                        }
                    },
                    setStyle: function(style) {
                        if (this.rendered) {
                            Ext.dom.Helper.applyStyles(this.getEl().dom, style);
                        } else {
                            this.on('afterrender', this.setStyle, this, {single: true, args: [style]});
                        }
                    },
                    setColumnWidth: function(cw) {
                        this.columnWidth = cw;
                        if (this.rendered) {
                            this.ownerCt.doLayout();
                        } else {
                            this.on('afterrender', this.setColumnWidth, this, {single: true, args: [cw]});
                        }
                    },
                    setHeight: function(height) {
                        if (this.rendered) {
                            if (height === '100%') {
                                height = window.screen.height - 200;
                                console.log('oVerridding to ', height);
                            }
                            this.setSize(undefined, height);
                        } else {
                            this.on('afterrender', this.setHeight, this, {single: true, args: [height]});
                        }
                    }
                },{
                    xtype: 'jobfilespanel',
                    itemId: 'files',
                    minWidth: 300,
                    plugins: 'responsive',
                    responsiveConfig: {
                        small: {
                            maxWidth: 700,
                            style: 'float:none;margin: 5px auto;display:block;',
                            columnWidth: 1,
                            height: 400
                        },
                        normal: {
                            maxWidth: 600,
                            style: 'float:none;margin: 5px;display:inline-block;',
                            columnWidth: 0.5,
                            height: '100%'
                        }
                    },
                    setStyle: function(style) {
                        if (this.rendered) {
                            Ext.dom.Helper.applyStyles(this.getEl().dom, style);
                        } else {
                            this.on('afterrender', this.setStyle, this, {single: true, args: [style]});
                        }
                    },
                    setColumnWidth: function(cw) {
                        this.columnWidth = cw;
                        if (this.rendered) {
                            this.ownerCt.doLayout();
                        } else {
                            this.on('afterrender', this.setColumnWidth, this, {single: true, args: [cw]});
                        }
                    },
                    setHeight: function(height) {
                        if (this.rendered) {
                            if (height === '100%') {
                                height = window.screen.height - 200;
                                console.log('oVerridding to ', height);
                            }
                            this.setSize(undefined, height);
                        } else {
                            this.on('afterrender', this.setHeight, this, {single: true, args: [height]});
                        }
                    }
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
        this.down('#jobid').setValue(job.get('id'));

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

        var field = this.down('#submitted');
        if (field.getValue() !== timeString) {
            field.setValue(timeString);
        }
    },

    cleanupDetails: function() {
        this.job = null;
        this.setTitle('Select a job on the left to view more details');
        this.down('#top-container').setVisible(false);
        this.down('#bottom-container').setVisible(false);
    }
});