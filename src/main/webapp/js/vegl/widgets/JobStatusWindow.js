/**
 * Ext.window.Window specialisation for rendering advanced information about a Jobs status
 */
Ext.define('vegl.widgets.JobStatusWindow', {
    extend : 'Ext.window.Window',

    /**
     * Accepts the config for a Ext.grid.Panel along with the following additions:
     *
     * job : vegl.models.Job - Job object to view advanced job status information
     */
    constructor : function(config) {
        var status = config.job.get('status');
        var statusDescription = vegl.models.Job.STATUS_DESCRIPTIONS[status];
        var statusStyle = vegl.widgets.JobsPanel.styleFromStatus(status);
        var showInstanceLog;

        var auditLogStore = Ext.create('Ext.data.Store', {
            model : 'vegl.models.AuditLog',
            proxy : {
                type : 'ajax',
                url : 'secure/getAuditLogsForJob.do',
                extraParams : {jobId : config.job.get('id')},
                reader : {
                    type : 'json',
                    rootProperty : 'data'
                }
            },
            autoLoad: true,
            sorters: [{
                property: 'transitionDate',
                direction: 'DESC'
            }]
        });

        switch (status) {
        case vegl.models.Job.STATUS_PENDING:
        case vegl.models.Job.STATUS_PROVISIONING:
        case vegl.models.Job.STATUS_ACTIVE:
            showInstanceLog = true;
            break;
        default:
            showInstanceLog = false;
            break;
        }

        Ext.apply(config, {
            title: 'Status information for ' + config.job.get('name'),
            layout: {
                type: 'vbox',
                align: 'stretch',
                pack: 'start'
            },
            items:[{
                xtype: 'container',
                height: 60,
                layout: {
                    type: 'hbox',
                    align: 'stretch',
                    pack: 'start'
                },
                items: [{
                    xtype: 'datadisplayfield',
                    itemId: 'status',
                    cls: 'vl-job-details',
                    fieldLabel: 'Status',
                    value: Ext.util.Format.format('<span title="{0}" style="color:{1};">{2}</span>', statusStyle.tip, statusStyle.color, statusStyle.text)
                },{
                    xtype: 'displayfield',
                    value: statusDescription,
                    padding: '10 0 0 10',
                    fieldStyle: 'font-style: italic; font-size:16px; color: #aaa;',
                    flex: 1
                }]
            },{
                xtype: 'tabpanel',
                flex: 1,
                plain: true,
                items: [{
                    title: 'Status Log',
                    layout: 'fit',
                    items: [{
                        xtype: 'gridpanel',
                        store: auditLogStore,
                        viewConfig: {
                          enableTextSelection: true
                        },
                        columns: [{
                            text: 'Status',
                            dataIndex: 'toStatus',
                            renderer: function(value) {
                                var style = vegl.widgets.JobsPanel.styleFromStatus(value);
                                return Ext.util.Format.format('<span title="{0}" style="color:{1};">{2}</span>', style.tip, style.color, style.text);
                            }
                        },{
                            text: 'Changed At',
                            dataIndex: 'transitionDate',
                            width: 160,
                            sortable: true,
                            renderer: Ext.util.Format.dateRenderer('d M Y, H:i:s')
                        },{
                            text: 'Message',
                            dataIndex: 'message',
                            flex: 1,
                            renderer: function(value) {
                                return Ext.util.Format.format('<span title="{0}">{0}</span>', value);
                            }
                        }]
                    }]
                },{
                    title: 'Raw Compute Logs',
                    hidden: !showInstanceLog,
                    layout: 'fit',
                    items: [{
                        xtype: 'plaintextpreview',
                        listeners: {
                            scope: this,
                            afterrender: function() {
                                this.updateRawInstanceLog();
                            }
                        },
                        dockedItems: [{
                            xtype: 'toolbar',
                            dock: 'bottom',
                            items: [{
                                xtype: 'tbfill'
                            },{
                                xtype: 'button',
                                text: 'Refresh',
                                iconCls: 'refresh-icon',
                                scope: this,
                                handler: function() {
                                    this.updateRawInstanceLog();
                                }
                            }]
                        }]
                    }]
                }]
            }]
        });

        this.callParent(arguments);
    },


    updateRawInstanceLog: function() {
        var preview = this.down('plaintextpreview');

        if (preview._currentRequest) {
            Ext.Ajax.abort(preview._currentRequest);
            preview._currentRequest = null;
        }

        var mask = new Ext.LoadMask({
            msg    : 'Accessing logs...',
            target : preview
        });
        mask.show();
        preview._currentRequest = portal.util.Ajax.request({
            url: 'secure/getRawInstanceLogs.do',
            params: {
                jobId: this.job.get('id')
            },
            callback: function() {
                mask.hide();
                preview._currentRequest = null;
            },
            success: function(data) {
                preview.writeText(data);
            },
            failure: function(message) {
                preview.writeText('Unable to access the compute logs directly: ' + message);
            }
        });
    }

});