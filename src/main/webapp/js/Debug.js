
Ext.namespace('Debug');

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
	
	
	var basicTestsPanel = new Ext.grid.GridPanel({
		title : 'Basic Tests',
		id: 'debug-basic-tests-panel',
		store: basicTestsStore,
		//layout: 'fit',
		columns : [
		    {id:'testName', dataIndex:'testName', header:'Test Name', sortable:true},
            {id:'testStatus', dataIndex:'testStatus', header:'Test Status', sortable:true}, 
            {id:'testNotes', dataIndex:'testNotes', header:'Test Notes', sortable:true},
        ]
	});
	
	var advancedTestsPanel = new Ext.grid.GridPanel({
		title : 'Advanced Tests',
		id: 'debug-advanced-tests-panel',
		store: advancedTestsStore,
		//layout: 'fit',
		columns : [
		           {id:'todo', dataIndex:'todo', header:'TODO', sortable:true},
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
							advancedTestsPanel
						]
				    }
                ]
        }]
    });
	
	Debug.runSimpleTest('Check User', 'checkUsername.do');
	Debug.runSimpleTest('Check Credentials', 'checkCredentials.do');
};

Ext.onReady(Debug.initialize);
	