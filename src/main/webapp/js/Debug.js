
Ext.namespace('Debug');


Debug.statusRenderer = function jobStatusRenderer(value, cell, record, rowIndex, colIndex, store) {
	if (value == "success")
		return '<span style="color:green;">' + value + '</span>';
	else
		return '<span style="color:red;">' + value + '</span>';
}
/**
 * Creates or updates the specified basic test with new details
 */
Debug.addOrUpdateSimpleTest = function(testName, status, notes) {
	var basicTestsStore = Ext.StoreMgr.get('basic-tests-store');
	//var index = basicTestsStore.find('testName', testName);
	for (var i = 0; i < basicTestsStore.getCount(); i++) {
		var record = basicTestsStore.getAt(i);
		if (record && record.get('testName') === testName) {
			record.set('testStatus', status);
			record.set('testNotes', notes);
			return;
		}
	}
	
	basicTestsStore.loadData([[testName, status, notes]], true);
};

/**
 * Runs a simple test and stores the result into basicTestsStore
 * 
 * testName : Descriptive test name (Also used as lookup ID)
 * testUrl : The url to run the test against
 * testParams : [Optional] and parameters to pass the url 
 */
Debug.runSimpleTest = function(testName, testUrl, testParams) {
	
	var callbackFunc = function(options, success, response) {
		if (success) {
			responseObj = Ext.util.JSON.decode(response.responseText);
			
			if (responseObj.success) 
				Debug.addOrUpdateSimpleTest(testName, 'success', responseObj.notes);
			else
				Debug.addOrUpdateSimpleTest(testName, 'failure', responseObj.notes);
		} else {
			Debug.addOrUpdateSimpleTest(testName, 'failure', 'Server connection problem (Test couldn\'t be run');
		}
	};
	
	//Firstly create a stub entry to let the user know a test is running
	Debug.addOrUpdateSimpleTest(testName, 'running', '');
	
	Ext.Ajax.request({
			url : testUrl,
			params : testParams,
			callback : callbackFunc
	});
};

Debug.generateTestForm_urlCopyTest = function() {
	return new Ext.form.FormPanel({
		title: 'Url Copy Test',
		id: 'advTest-url-copy',
		collapsible: true,
		collapsed: true,
		items: [
		        {
		        	xtype: 'fieldset',
		        	title: 'Urls',
		        	items: [
		        	        {
		        	        	anchor: '100%',
		        	        	xtype: 'textfield',
		        	        	fieldLabel: 'From Url',
		        	        	name: 'fromUrl'
		        	        },{
		        	        	anchor: '100%',
		        	        	xtype: 'textfield',
		        	        	fieldLabel: 'To Url',
		        	        	name: 'toUrl'
		        	        }
		           ]
		        	
		        }, {
		        	xtype: 'button',
		        	text: 'Run Test',
		        	handler: function() {
		        		Debug.runSimpleTest('Url copy Test', 'urlCopyTest.do', Ext.getCmp('advTest-url-copy').getForm().getValues());
		        	}
		        }
		]
	});

};

Debug.generateTestForm_submitJobTest = function() {
	return new Ext.form.FormPanel({
		title: 'Submit Job Test',
		id: 'advTest-submit-job',
		collapsible: true,
		collapsed: true,
		items: [
		        {
		        	xtype: 'button',
		        	text: 'Run Test',
		        	handler: function() {
		        		Debug.runSimpleTest('Submit test job', 'testRunJob.do');
		        	}
		        }
		]
	});

};

Debug.initialize = function() {
	
	var fieldList = ['testName', 'testStatus', 'testNotes'];

    var basicTestsStore = new Ext.data.ArrayStore({
        fields : fieldList,
        data : [],
        storeId : 'basic-tests-store',
        sortInfo: {field:'testName', direction:'ASC'}
    });

    var advancedTestsStore = new Ext.data.ArrayStore({
        fields : fieldList,
        data : [],
        storeId : 'advanced-tests-store',
    	sortInfo: {field:'testName', direction:'ASC'}
    });
	
	
	var basicTestsPanel = new Ext.grid.EditorGridPanel({
		title : 'Test Results',
		id: 'debug-basic-tests-panel',
		store: basicTestsStore,
		autoExpandColumn: 'testNotes',
		//Make the notes column editable (So it's easy to copy paste)
		columns : [
		    {id:'testName', dataIndex:'testName', header:'Test Name', sortable:true},
            {id:'testStatus', dataIndex:'testStatus', header:'Test Status', sortable:true, renderer:Debug.statusRenderer}, 
            {id:'testNotes', dataIndex:'testNotes', header:'Test Notes', sortable:true, editor: new Ext.form.TextField({allowBlank: false})},
        ]
	});
	
	var viewPort = new Ext.Viewport({
        layout: 'border',
        renderTo     : Ext.getBody(),
        border:false,
        items: [
            //This is a little waste of space to sit behind the page header
            {
            	xtype: 'box',
			    region: 'north',
			    height: 100
			},
			//The panel that sits "full screen" with a title
			{
				xtype : 'panel',
				id: 'debug-grouping-panel',
				title: 'Geodesy debug functions',
				region: 'center',
				margins: '2 0 0 0',
				layout: 'border',
				items: [
				    //The hbox that holds our basic / advanced test panels
				    {
				    	xtype: 'panel',
				    	layout       : 'vbox',
						defaults     : { flex : 1 }, //auto stretch
						layoutConfig : { align : 'stretch' },
						id			 : "debug-test-parent-panel",
				        region       : 'center',
						items        : [
							basicTestsPanel,
							Debug.generateTestForm_urlCopyTest(),
							Debug.generateTestForm_submitJobTest()
						]
				    }
                ]
        }]
    });
	
	Debug.runSimpleTest('Username', 'checkUsername.do');
	Debug.runSimpleTest('Credentials', 'checkCredentials.do');
	Debug.runSimpleTest('Input files', 'checkInputFiles.do');
	Debug.runSimpleTest('Local stageIn dir', 'checkLocalStageInDir.do');
	Debug.runSimpleTest('Grid stageIn dir', 'checkRemoteStageInDir.do');
	Debug.runSimpleTest('RINEX Urls', 'testRinexUrlAvailability.do');
};

Ext.onReady(Debug.initialize);
	