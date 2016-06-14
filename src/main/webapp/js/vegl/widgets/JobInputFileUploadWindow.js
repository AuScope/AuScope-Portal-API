/**
 * A window for creating a new job input file in the form of a vegl.models.Download or a vgl.models.FileRecord
 * from a user uploaded file
 */
Ext.define('vegl.widgets.JobInputFileUploadWindow', {
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
            title : 'Upload from your computer',
            items : [{
                xtype : 'form',
                items : [{
                    xtype: 'filefield',
                    name: 'file',
                    anchor : '100%',
                    labelWidth: 150,
                    allowBlank: false,
                    fieldLabel: 'Select File to upload'
                }]
            }],
            buttons:[{
                xtype: 'button',
                text: 'Create Input',
                scope : this,
                iconCls : 'add',
                handler: function() {
                    this.addLocal(this.down('form'));
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

        var params = form.getValues();
        params.jobId = this.jobId;

        //Submit our form so our files get uploaded...
        form.submit({
            url: 'secure/uploadFile.do',
            params: params,
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
            waitMsg: 'Uploading file, please wait...',
            waitTitle: 'Upload file'
        });
    }
});
