/**
 * A window for creating a new job input file in the form of a vegl.models.Download or a vgl.models.FileRecord
 */
Ext.define('vegl.widgets.JobInputFileWindow', {
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
            title : 'New Job Input File',
            items : [{
                xtype : 'tabpanel',
                activeTab : 1,
                itemId : 'tab-panel',
                items : [{
                    xtype : 'form',
                    itemId : 'tab-local',
                    title : 'From Local File',
                    items : [{
                        xtype: 'filefield',
                        name: 'file',
                        anchor : '100%',
                        labelWidth: 150,
                        allowBlank: false,
                        fieldLabel: 'Select File to upload'
                    }]
                },{
                    xtype : 'form',
                    itemId : 'tab-remote',
                    title : 'From Remote Service',
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
                        anchor : '100%'
                    }]
                }]
            }],
            buttons:[{
                xtype: 'button',
                text: 'Create Input',
                scope : this,
                iconCls : 'add',
                handler: function() {
                    var tab = this.getComponent('tab-panel').getActiveTab();
                    if (tab.getItemId() === 'tab-local') {
                        this.addLocal(tab);
                    } else {
                        this.addRemote(tab);
                    }
                }
            }]
        });

        this.callParent(arguments);
    },

    /**
     * Loads the file listed in the tab-local tab into the specified job.
     *
     * Closes this window on success
     */
    addLocal : function(formPanel) {
        var form = formPanel.getForm();
        if (!form.isValid()) {
            return;
        }

        //Submit our form so our files get uploaded...
        form.submit({
            url: 'uploadFile.do',
            scope : this,
            success: function(form, action) {
                if (!action.result.success) {
                    Ext.Msg.alert('Error uploading file. ' + action.result.error);
                    return;
                }
                this.close();
            },
            failure: function() {
                Ext.Msg.alert('Failure', 'File upload failed. Please try again in a few minutes.');
            },
            params: {
                jobId : this.jobId
            },
            waitMsg: 'Uploading file, please wait...',
            waitTitle: 'Upload file'
        });
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
            url: 'updateJobDownloads.do',
            scope : this,
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
            },
            params: params
        });
    }
});
