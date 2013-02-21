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
                    itemId : 'add-button',
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
    },

    getHelpInstructions : function() {
        var filesPanel = this.getComponent('files-panel');
        var addButton = filesPanel.queryById('add-button');

        return [Ext.create('portal.util.help.Instruction', {
            highlightEl : filesPanel.getEl(),
            title : 'Review Job Inputs',
            anchor : 'top',
            description : 'Every job has a number of input files. These files may yet to have been downloaded from a remote web service, or they may have already been uploaded from your desktop. Remote files will not be downloaded until after the job begins execution.<br/><br/>You can interact with these input files by right clicking them or by selecting them and pressing the \'Actions\' button.'
        }), Ext.create('portal.util.help.Instruction', {
            highlightEl : addButton.getEl(),
            title : 'Add more Inputs',
            anchor : 'top',
            description : 'If you\'d like to add your own custom input files/web service downloads then press this button. You will be prompted to either upload a file from your local machine or to specify the public URL where the file can be accessed from.'
        })];
    }
});