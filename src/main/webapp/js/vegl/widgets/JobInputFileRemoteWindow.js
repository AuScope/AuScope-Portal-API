/**
 * A window for creating a new job input file in the form of a vegl.models.Download or a vgl.models.FileRecord
 * from a remote data server
 */
Ext.define('vegl.widgets.JobInputFileRemoteWindow', {
    extend : 'Ext.Window',

    jobId : null,

    /**
     * Adds the following config options:
     *
     * {
     *  jobId : String - The unique ID of a job to add input files to
     * }
     */
    constructor : function(config) {
        this.jobId = config.jobId;

        Ext.apply(config, {
            layout : 'fit',
            title : 'Download from remote service',
            items : [{
                xtype : 'form',
                itemId : 'tab-remote',
                items : [{
                    xtype : 'textfield',
                    fieldLabel: 'URL',
                    anchor : '100%',
                    name : 'url',
                    emptyText : 'URL for the job to download.',
                    allowBlank : false
                },{
                    xtype : 'textfield',
                    fieldLabel: 'Location',
                    anchor : '100%',
                    name : 'localPath',
                    emptyText : 'File path where the downloaded data will be stored.',
                    allowBlank : false
                },{
                    xtype : 'textfield',
                    fieldLabel: 'Name',
                    anchor : '100%',
                    name : 'name',
                    allowBlank : false
                },{
                    xtype : 'textarea',
                    fieldLabel: 'Description',
                    name : 'description',
                    anchor : '100%',
                    value: ' '
                }]
            }],
            buttons:[{
                xtype: 'button',
                text: 'Create Input',
                scope : this,
                iconCls : 'add',
                handler: function() {
                    this.addRemote(this.down('form'));
                }
            }]
        });

        this.callParent(arguments);
    },

    /**
     * Loads the remote input listed in the tab-remote tab into the specified job.
     *
     * Closes this window on success
     */
    addRemote : function(formPanel) {
        var form = formPanel.getForm();
        if (!form.isValid()) {
            return;
        }

        var params = form.getValues();
        params.id = this.jobId;
        params.append = true;

        Ext.Ajax.request({
            url: 'secure/updateJobDownloads.do',
            scope : this,
            params: params,
            callback: function(options, success, response) {
                if (!success) {
                    Ext.Msg.alert('Failure', 'Operation failed. Please try again in a few minutes.');
                    return;
                }

                var responseObj = Ext.JSON.decode(response.responseText);
                if (!responseObj.success) {
                    Ext.create('portal.widgets.window.ErrorWindow', {errorObj : responseObj}).show();
                    return;
                }

                this.close();
            }
        });
    }
});
