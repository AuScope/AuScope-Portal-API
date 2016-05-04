/**
 * A Ext.panel.Panel specialisation for rendering details about a single job
 *
 */
Ext.define('vegl.widgets.DetailsPanel', {
    extend : 'Ext.panel.Panel',
    alias : 'widget.detailspanel',

    taskRunner: null,

    /**
     * See ANVGl-117 for a breakdown on why we've included so much custom code
     */
    constructor : function(config) {
        var ddfStyle = 'float:none;margin: 5px;display:inline-block;';
        var ddfResponsiveCfg = {
            small: {
                cls: 'vl-job-details-small'
            },
            normal: {
                cls: 'vl-job-details'
            }
        };
        var ddfSetCls = function(cls) {
            if (this.rendered) {
                var el = this.getEl();
                var currentCls = el.getAttribute('class').replace(/vl-job-details[^ ]*/g, '');
                el.dom.className = currentCls + ' ' + cls;
            } else {
                this.on('afterrender', this.setCls, this, {single: true, args: [cls]});
            }
        };

        Ext.apply(config, {
            width: '100%',
            layout: {
                type: 'vbox',
                align: 'stretch'
            },
            title: 'Select a job on the left to view more details',
            items: [{
                xtype: 'container',
                itemId: 'top-container',
                hidden: true,
                plugins: 'responsive',
                layout: {
                    type: 'column'
                },
                style: 'text-align:center;',
                items: [{
                    xtype: 'datadisplayfield',
                    itemId: 'status',
                    fieldLabel: 'Status',
                    margin: '0 10 0 10',
                    plugins: 'responsive',
                    responsiveConfig: ddfResponsiveCfg,
                    style: ddfStyle,
                    setCls: ddfSetCls
                },{
                    xtype: 'datadisplayfield',
                    itemId: 'jobid',
                    fieldLabel: 'Job ID',
                    cls: 'vl-job-details',
                    margin: '0 10 0 10',
                    plugins: 'responsive',
                    responsiveConfig: ddfResponsiveCfg,
                    style: ddfStyle,
                    setCls: ddfSetCls
                },{
                    xtype: 'datadisplayfield',
                    itemId: 'ami',
                    fieldLabel: 'Instance ID',
                    cls: 'vl-job-details',
                    margin: '0 10 0 10',
                    plugins: 'responsive',
                    responsiveConfig: ddfResponsiveCfg,
                    style: ddfStyle,
                    setCls: ddfSetCls
                },{
                    xtype: 'datadisplayfield',
                    itemId: 'type',
                    fieldLabel: 'Instance Type',
                    cls: 'vl-job-details',
                    margin: '0 10 0 10',
                    plugins: 'responsive',
                    responsiveConfig: ddfResponsiveCfg,
                    style: ddfStyle,
                    setCls: ddfSetCls
                },{
                    xtype: 'datadisplayfield',
                    itemId: 'submitted',
                    fieldLabel: 'Submitted',
                    cls: 'vl-job-details',
                    margin: '0 10 0 10',
                    plugins: 'responsive',
                    responsiveConfig: ddfResponsiveCfg,
                    style: ddfStyle,
                    setCls: ddfSetCls
                }]
            },{
                xtype: 'container',
                itemId: 'bottom-container',
                hidden: true,
                margin: '10 0 0 0',
                padding: '5',
                flex: 1,
                style: 'text-align:center;',
                layout: {
                    type: 'hbox',
                    pack: 'center',
                    align: 'stretch'
                },
                items: [{
                    xtype: 'jobfilespanel',
                    itemId: 'files',
                    flex: 1
                },{
                    xtype: 'splitter'
                },{
                    xtype: 'joblogspanel',
                    itemId: 'logs',
                    flex: 1
                }]
            }]
        });

        this.callParent(arguments);
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