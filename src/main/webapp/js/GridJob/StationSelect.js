

Ext.BLANK_IMAGE_URL = 'js/external/ext-2.2/resources/images/default/s.gif';

Ext.namespace('StationSelect');

var xmlHeader = '<?xml version="1.0" encoding="ISO-8859-1"?>';
var xmlRootOpenTag = "<data>";
var xmlRootCloseTag = "</data>";
var fileUrl = "fileUrl";
var fileUrlOpenTag = "<fileUrl>";
var fileUrlCloseTag = "</fileUrl>";

StationSelect.requestStationListSuccess = function(response, options) {
	var responseObj = eval('(' + response.responseText + ')');//Ext.JSON.decode(response.responseText);

	Ext.getBody().unmask(true);
	
	
	if (!responseObj.success) {
		StationSelect.requestStationListFailure(response, options);
		return;
	}
	
	var store = Ext.StoreMgr.get('unselected-stations-store');
	store.loadData(responseObj);
	
	StationSelect.serviceUrl = responseObj.serviceUrl;
}

StationSelect.requestStationListFailure = function(response, options) {
	
	Ext.getBody().unmask(true);
	
	if (response.status == 200) 
		Ext.Msg.alert('Failure Loading Stations', 'Server returned unknown error');
	else
		Ext.Msg.alert('Failure Loading Stations', 'Error ' + response.status + ': ' + response.statusText );
}

StationSelect.saveStationListSuccess = function (response, options) {
	var responseObj = eval('(' + response.responseText + ')');//Ext.JSON.decode(response.responseText);
	
	Ext.getBody().unmask(true);
	
	if (!responseObj.success) {
		StationSelect.saveStationListFailure(response, options);
		return;
	}
	
	//Ext.Msg.alert('Success', 'Selected points are now available in the Data Service Tool');
	if (StationSelect.onSuccessfulSubmit) {
		StationSelect.onSuccessfulSubmit();
	}
}

StationSelect.saveStationListFailure = function (response, options) {
	
	Ext.getBody().unmask(true);
	
	if (response.status == 200) 
		Ext.Msg.alert('Failure Saving Stations', 'Server returned unknown error');
	else
		Ext.Msg.alert('Failure Saving Stations', 'Error ' + response.status + ': ' + response.statusText );
}

//This is raised whenever a submit to the server has been made succesfully
StationSelect.onSuccessfulSubmit = undefined;

StationSelect.requestStationListUrlsSuccess = function (response, options) {
	var responseObj = eval('(' + response.responseText + ')');//Ext.JSON.decode(response.responseText);
	
	Ext.getBody().unmask(true);
	
	if (!responseObj.success) {
		StationSelect.requestStationListUrlsFailure(response, options);
		return;
	}
	
	//Lets save our list of URL's
	var mask = new Ext.LoadMask(Ext.getBody(),{
		msg : 'Submitting to grid...'
	})
	mask.show();
	
	Ext.Ajax.request({
    	url : 'sendToGrid.do',
    	success : StationSelect.saveStationListSuccess,
    	failure : StationSelect.saveStationListFailure,
    	timeout : (6000 * 20), //Timeout is 20 minutes
    	params : {
			myFiles : Ext.util.JSON.encode(responseObj.urlList)
		}
    });
}

StationSelect.requestStationListUrlsFailure = function (response, options) {
	
	Ext.getBody().unmask(true);
	
	if (response.status == 200) 
		Ext.Msg.alert('Failure Fetching URLs', 'Server returned unknown error');
	else
		Ext.Msg.alert('Failure Fetching URLs', 'Error ' + response.status + ': ' + response.statusText );
}

StationSelect.validateFields = function() {
    var startDateField = Ext.getCmp('startDateField');
    var endDateField = Ext.getCmp('endDateField');
    var selectedStationsGrid = Ext.getCmp('selectedStationsGrid');

    if (!startDateField.isValid(false)) {
        Ext.Msg.alert('Invalid Field', 'Please enter a valid value for the start date');
        return false;
    }

    if (!endDateField.isValid(false)) {
        Ext.Msg.alert('Invalid Field', 'Please enter a valid value for the end date');
        return false;
    }

    var startDate = startDateField.getValue();
    var endDate = endDateField.getValue();

    if (startDate > endDate) {
        Ext.Msg.alert('Invalid Field', 'The start date must precede the end date');
        return false;
    }

    //If we have no selections, we want to skip everything and just move forward
    /*if (selectedStationsGrid.store.getCount() == 0) {
        Ext.Msg.alert('No Selections', 'You must select at least 1 station before continuing');
        return false;
    }*/

    return true;
}

StationSelect.okButtonHandler = function() {
    if (!StationSelect.validateFields()) {
        return;
    }
    
    //If we have no selections, we want to skip everything and just move forward
    var selectedStationsGrid = Ext.getCmp('selectedStationsGrid');
    if (selectedStationsGrid.store.getCount() == 0) {
    	if (StationSelect.onSuccessfulSubmit) {
    		StationSelect.onSuccessfulSubmit();
    	}
    	return;
    }
    
    var startDateField = Ext.getCmp('startDateField');
    var endDateField = Ext.getCmp('endDateField');
    
    var startDate = startDateField.getValue();
    var endDate = endDateField.getValue();
    
    var selectedStations = '';
    for (var i = 0; i < selectedStationsGrid.store.getCount(); i++) {
    	if (i > 0)
    		selectedStations += ',';
    	
    	selectedStations += selectedStationsGrid.store.getAt(i).get('gpsSiteId');
    }
    
    var mask = new Ext.LoadMask(Ext.getBody(),{
		msg : 'Requesting Files...'
	})
	mask.show();
    
    //Make the request for the URL list (which we will then save)
    Ext.Ajax.request({
    	url : 'getStationListUrls.do',
    	success : StationSelect.requestStationListUrlsSuccess,
    	failure : StationSelect.requestStationListUrlsFailure,
    	timeout : (6000 * 20), //Timeout is 20 minutes
    	params : {
			dateFrom : startDate.format('Y-m-d'),
			dateTo   : endDate.format('Y-m-d'),
			serviceUrl : StationSelect.serviceUrl,
			stationList : selectedStations
		}
    });
};

StationSelect.generatePanel = function() {
	//======= Configure Date fields
    var datePanel = new Ext.form.FormPanel({

        autoScroll:true,
        hideMode:'offsets',
        buttonAlign: 'right',
        labelAlign: 'right',
        labelWidth: 140,
        autoHeight : true,
        title: 'Time Series',
        region : 'north',
        
        items: [{
            xtype : 'panel',
            layout:'hbox',
            height : 50,
            border :false,
            anchor : '100%',
            defaults     : { flex : 1 }, //auto stretch
            layoutConfig : { align : 'stretch' },
            items : [{
                xtype:'fieldset',
                border : false,

                items :[{
                    xtype: 'datefield',
                    fieldLabel: 'Series start date',
                    name: 'startDate',
                    id : 'startDateField',
                    anchor: '80%',
                    height : 100,
                    format : 'Y/m/d',
                    value : new Date()

                }]
            },{
                xtype:'fieldset',
                border : false,
                autoHeight:true,

                items:[{
                    xtype: 'datefield',
                    fieldLabel: 'Series end Date',
                    style : 'text-align:left',
                    id : 'endDateField',
                    anchor: '80%',
                    name: 'endDate',
                    format : 'Y/m/d',
                    value : new Date()
                }]
            }]
        }]
    });

    //======= Configure Station Selection Grids
    var fieldList = ['stationName', 'gpsSiteId', 'stationNumber', 'countryId', 'stateId'];

    var unselectedStationsStore = new Ext.data.JsonStore({
        fields : fieldList,
        data : {stations : []},
        storeId : 'unselected-stations-store',
        root   : 'stations',
        sortInfo: {field:'gpsSiteId', direction:'ASC'},
        groupField: 'countryId'
    });

    var selectedStationsStore = new Ext.data.JsonStore({
        fields : fieldList,
        storeId : 'selected-stations-store',
        root   : 'stations',
        data : {stations : []},
        sortInfo: {field:'gpsSiteId', direction:'ASC'},
        groupField: 'countryId'
    });

    var moveRecords = function(sourceGrid, destinationGrid, recordList) {
        Ext.each(recordList, sourceGrid.store.remove, sourceGrid.store);
        destinationGrid.store.add(recordList);
        destinationGrid.store.sort('name', 'ASC');
    }

    var unselectedStationsGrid = new Ext.grid.GridPanel({
        ddGroup : 'ddSelectedStations',
        store : unselectedStationsStore,
        columns : [{id:'gpsSiteId', dataIndex:'gpsSiteId', header:'Station ID', sortable:true},
                   {id:'stationName', dataIndex:'stationName', header:'Station Name', sortable:true}, 
                   {id:'countryId', dataIndex:'countryId', header:'Location', sortable:true},
                   {id:'stationNumber', dataIndex:'stationNumber', header:'Station Number', sortable:true, hidden:true},
                   {id:'stateId', dataIndex:'stateId', header:'State ID', sortable:true, hidden:true}],
        enableDragDrop : true,
        stripeRows : true,
        autoExpandColumn : 'stationName',
        title : 'Unselected Stations',
        /*view: new Ext.grid.GroupingView({
        forceFit:true,
        groupTextTpl: '{text} ({[values.rs.length]} {[values.rs.length > 1 ? "Items" : "Item"]})'
    }),*/
        listeners : {
            render: function() {
                new Ext.dd.DropTarget(this.el.dom, {
                    ddGroup    : 'ddUnselectedStations',
                    notifyDrop : function(ddSource, e, data){
                        var records =  ddSource.dragData.selections;
                        moveRecords(ddSource.grid, unselectedStationsGrid, records);
                        return true;
                    }
                });
            },
            cellcontextmenu_disabled : function(grid, rowIndex, colIndex, event) {
                event.stopEvent();

                if (grid.getSelectionModel().getSelections().length == 0)
                    grid.getSelectionModel().selectRow(rowIndex);

                var contextMenu = new Ext.menu.Menu();
                contextMenu.add({xtype : 'button',
                                pressed:true,
                                text : 'Select station',
                                handler : function() {
                                    moveRecords(grid, selectedStationsGrid, grid.getSelectionModel().getSelections());
                                }});
                contextMenu.showAt(event.getXY());
            }}
    });


    var selectedStationsGrid = new Ext.grid.GridPanel({
        ddGroup : 'ddUnselectedStations',
        store : selectedStationsStore,
        columns : [{id:'gpsSiteId', dataIndex:'gpsSiteId', header:'Station ID', sortable:true},
                   {id:'stationName', dataIndex:'stationName', header:'Station Name', sortable:true}, 
                   {id:'countryId', dataIndex:'countryId', header:'Location', sortable:true},
                   {id:'stationNumber', dataIndex:'stationNumber', header:'Station Number', sortable:true, hidden:true},
                   {id:'stateId', dataIndex:'stateId', header:'State ID', sortable:true, hidden:true}],
        enableDragDrop : true,
        stripeRows : true,
        autoExpandColumn : 'stationName',
        title : 'Selected Stations',
        id : 'selectedStationsGrid',
        /*view: new Ext.grid.GroupingView({
            forceFit:true,
            groupTextTpl: '{text} ({[values.rs.length]} {[values.rs.length > 1 ? "Items" : "Item"]})'
        }),*/
        listeners : {
            render : function() {
                new Ext.dd.DropTarget(this.el.dom, {
                    ddGroup    : 'ddSelectedStations',
                    notifyDrop : function(ddSource, e, data){
                        var records =  ddSource.dragData.selections;
                        moveRecords(ddSource.grid, selectedStationsGrid, records);
                        return true
                    }
                });
            },
            cellcontextmenu_disabled : function(grid, rowIndex, colIndex, event) {
                event.stopEvent();

                if (grid.getSelectionModel().getSelections().length == 0)
                    grid.getSelectionModel().selectRow(rowIndex);

                var contextMenu = new Ext.menu.Menu({
                    ignoreParentClicks : false
                });
                contextMenu.add({xtype : 'button',
                                pressed:true,
                                text : 'Unselect station',
                                handler : function() {
                                    moveRecords(grid, unselectedStationsGrid, grid.getSelectionModel().getSelections());
                                }});
                contextMenu.showAt(event.getXY());
            }}
    });

    //Simple 'border layout' panel to house both grids
	var stationSelectionPanel = new Ext.Panel({
		layout       : 'hbox',
		defaults     : { flex : 1 }, //auto stretch
		layoutConfig : { align : 'stretch' },
		id			 : "station-selection-panel",
        region       : 'center',
		items        : [
			unselectedStationsGrid,
			selectedStationsGrid
		],
		listeners : {
			render : function() {
				var mask = new Ext.LoadMask(Ext.getBody(),{
					msg : 'Loading Stations...',
					removeMask : true
				})
				mask.show();
			}
		}
	});
	
	//Request our data from the server
    Ext.Ajax.request({
    	url : 'getStationList.do',
    	success : StationSelect.requestStationListSuccess,
    	failure : StationSelect.requestStationListFailure
    });
    
    return { 
    	xtype : 'panel',
        region : 'center',
        layout: 'border',
        margins: '2 150 0 150',
        height : 600,
        width : 800,
        items: [datePanel, stationSelectionPanel ],
        /*buttons: [{xtype   : 'button',
                   text    : 'OK',
                   handler : StationSelect.okButtonHandler}]*/
    };
};

StationSelect.initialize = function() {

    //================ Pull it all together in a parent
    var panel = StationSelect.generatePanel();
    panel.buttons = [{xtype   : 'button', text    : 'OK', handler : StationSelect.okButtonHandler}];
    
    StationSelect.onSuccessfulSubmit = function () {
    	Ext.Msg.alert('Success', 'Selected points are now available in the Data Service Tool');
    };
    
    var viewPort = new Ext.Viewport({
        layout: 'border',
        renderTo     : Ext.getBody(),
        border:false,
        items: [{
            xtype: 'box',
            region: 'north',
            applyTo: 'body',
            height: 100
        },{
            xtype : 'panel',
            id: 'station-select-panel',
            title: 'Select Stations for Processing',
            region: 'center',
            margins: '2 0 0 0',
            layout: 'border',
            items: [panel,
                    {xtype: 'box',
                    region: 'south',
                    height: 5
                }]
        }]
    });
};

//Ext.onReady(StationSelect.initialize);


