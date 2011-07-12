JobBuilder.initialise = function() {
	new Ext.Viewport({
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
            items: [ new JobWizard() ]
        }]
    });
};

Ext.onReady(JobBuilder.initialise);