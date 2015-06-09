Ext.application({
    name : 'portal',

    //Here we build our GUI from existing components - this function should only be assembling the GUI
    //Any processing logic should be managed in dedicated classes - don't let this become a
    //monolithic 'do everything' function
    launch : function() {
        Ext.create('Ext.container.Viewport', {
            layout: 'border',
            style: {
                'background-color': 'white'
            },
            items: [{
                xtype: 'box',
                region: 'north',
                applyTo: 'body',
                height: 100
            },{
                region: 'center',
                margin: '10 0 10 0',
                border: false,
                layout: 'center',
                bodyStyle: {
                    'background-color': 'white'
                },
                items: [{
                    id: 'job-submit-panel',
                    maxWidth: 1150,
                    height: '100%',
                    bodyStyle: {
                        'background-color': 'white'
                    },
                    layout: 'fit',
                    items: [ Ext.create('vegl.jobwizard.JobWizard', {
                        id : 'job-wizard-panel',
                        forms : ['vegl.jobwizard.forms.JobObjectForm',
                                 'vegl.jobwizard.forms.JobUploadForm',
                                 'vegl.jobwizard.forms.ScriptBuilderForm',
                                 'vegl.jobwizard.forms.JobSubmitForm']
                    }) ]
                }]
            }]
        });
    }
});