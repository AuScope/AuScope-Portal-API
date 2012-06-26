Ext.application({
    name : 'portal',

    //Here we build our GUI from existing components - this function should only be assembling the GUI
    //Any processing logic should be managed in dedicated classes - don't let this become a
    //monolithic 'do everything' function
    launch : function() {
        Ext.create('Ext.container.Viewport', {
            layout: 'border',
            items: [{
                xtype: 'box',
                region: 'north',
                applyTo: 'body',
                height: 100
            },{
                id: 'job-submit-panel',
                //border : false,
                region: 'center',
                margins: '2 2 2 0',
                layout: 'fit',
                items: [ Ext.create('vegl.jobwizard.JobWizard', {}) ]
            }]
        });
    }
});