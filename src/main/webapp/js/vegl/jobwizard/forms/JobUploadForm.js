/**
 * Job wizard form for handling uploads of custom user input files
 *
 * Author - Josh Vote
 */
Ext.define('vegl.jobwizard.forms.JobUploadForm', {
    extend : 'vegl.jobwizard.forms.BaseJobWizardForm',

    uploadedFilesStore : null,
    fileGrid : null,

    /**
     * Creates a new JobUploadForm form configured to write/read to the specified global state
     */
    constructor: function(wizardState) {
        var jobUploadFrm = this;

        this.callParent([{
            wizardState : wizardState,
            bodyStyle: 'padding:10px;',
            fileUpload: true,
            frame: true,
            buttons: [],
            listeners : {
                jobWizardActive : Ext.bind(jobUploadFrm.updateFileList, jobUploadFrm)
            },
            items: [{
                xtype : 'jobinputfilespanel',
                itemId : 'files-panel',
                currentJobId : wizardState.jobId,
                title: 'Input files',
                stripeRows: true,
                anchor: '100% -20',
                buttons : [{
                    text : 'Add Input',
                    iconCls : 'add',
                    align : 'right',
                    handler : function() {
                        Ext.create('vegl.widgets.JobInputFileWindow', {
                            jobId : jobUploadFrm.wizardState.jobId,
                            width : 500,
                            height : 300,
                            modal : true,
                            listeners : {
                                close : function() {
                                    jobUploadFrm.updateFileList();
                                }
                            }
                        }).show();
                    }
                }]
            }]
        }]);
    },

    //Refresh the server side file list
    updateFileList : function() {
        var filesPanel = this.getComponent('files-panel');
        filesPanel.currentJobId = this.wizardState.jobId;
        filesPanel.updateFileStore();
    },

    //We don't validate on this form
    beginValidation : function(callback) {
        callback(true);
    },

    getTitle : function() {
        return "Manage job input files...";
    }
});