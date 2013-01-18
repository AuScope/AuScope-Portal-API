/**
 * Class for parsing a set of portal.csw.CSWRecord objects request/response
 * using the Querier interface
 *
 * The resulting Querier allows the selection of the CSWRecord online resources
 * such that they will be downloaded by a VGL job.
 */
Ext.define('vegl.layer.querier.csw.SelectableCSWQuerier', {
    extend: 'portal.layer.querier.Querier',

    constructor: function(config){
        this.callParent(arguments);
    },

    /**
     * See parent class for definition
     */
    query : function(queryTarget, callback) {        
        var cswRecord = queryTarget.get('cswRecord');
        if (!cswRecord) {
            callback(this, [], queryTarget);
            return;
        }

        var keywordsString = "";
        var keywords = cswRecord.get('descriptiveKeywords');
        for (var i = 0; i < keywords.length; i++) {
            keywordsString += keywords[i];
            if (i < (keywords.length - 1)) {
                keywordsString += ', ';
            }
        }
        
        var selModel = Ext.create('Ext.selection.CheckboxModel');

        var panel = Ext.create('portal.layer.querier.BaseComponent', {
            border : false,
            autoScroll : true,
            items : [{
                layout : 'fit',
                items : [{
                    xtype : 'fieldset',
                    items : [{
                        xtype : 'displayfield',
                        fieldLabel : 'Source',
                        value : Ext.util.Format.format('<a target="_blank" href="{0}">Link back to registry</a>', cswRecord.get('recordInfoUrl'))
                    },{
                        xtype : 'displayfield',
                        fieldLabel : 'Title',
                        anchor : '100%',
                        value : cswRecord.get('name')
                    }, {
                        xtype : 'textarea',
                        fieldLabel : 'Abstract',
                        anchor : '100%',
                        value : cswRecord.get('description'),
                        readOnly : true
                    },{
                        xtype : 'displayfield',
                        fieldLabel : 'Keywords',
                        anchor : '100%',
                        value : keywordsString
                    },{
                        xtype : 'displayfield',
                        fieldLabel : 'Contact Org',
                        anchor : '100%',
                        value : cswRecord.get('contactOrg')
                    },{
                        fieldLabel : 'Resources',
                        xtype : 'onlineresourcepanel',
                        cswRecords : cswRecord,
                        selModel : selModel,
                        sortable : false,
                        hideHeaders : false
                    },{
                        xtype : 'button',
                        margin : '5 0 0 0',
                        text : 'Capture selected',
                        handler : Ext.bind(this.addSelectedResourcesToSession, this, selModel, true) 
                    }]
                }]
            }]
        });

        callback(this, [panel], queryTarget);
    },
    
    addSelectedResourcesToSession : function(button, event, selModel) {        
        // Check to ensure user has selected at least a file to capture
        var selected = selModel.getSelection();
        if (selected.length == 0) {
            Ext.Msg.alert("Input error", "You haven't selected any file(s) to capture.");
            return;
        }
        
        // Array variables for holding user selected resources meta-data 
        var urls = [];
        var names = [];
        var descriptions = [];
        var localPaths = [];
        var northBoundLatitudes = [];
        var eastBoundLongitudes = [];
        var southBoundLatitudes = [];
        var westBoundLongitudes = [];
        
        // Populate the above variables by iterating thru user selected resources.
        Ext.each(selected, function(orpRow) {
            resource = orpRow.get('onlineResource');
            cswRecord = orpRow.get('cswRecord');
            
            var bboxes = cswRecord.get('geographicElements');
            var bbox = null;
            if (bboxes.length > 0) {
                bbox = bboxes[0];
            }
            
            urls.push(resource.get('url') ? resource.get('url') : '');
            names.push(resource.get('name') ? resource.get('name') : 'ERDDAP Subset Request');
            descriptions.push(resource.get('description') ? resource.get('description') : '');
            localPaths.push(resource.get('name') ? resource.get('name') : '/tmp/subset-request');
            northBoundLatitudes.push(bbox.northBoundLatitude ? bbox.northBoundLatitude : 0);
            eastBoundLongitudes.push(bbox.eastBoundLongitude ? bbox.eastBoundLongitude : 0);
            southBoundLatitudes.push(bbox.southBoundLatitude ? bbox.southBoundLatitude : 0);
            westBoundLongitudes.push(bbox.westBoundLongitude ? bbox.westBoundLongitude : 0);
        });
        
        var loadMask = new Ext.LoadMask(Ext.getBody(), {
            msg : 'Capturing input file(s)...',
            removeMask : true
        });
        loadMask.show();
        
        Ext.Ajax.request({
            url : 'addSelectedResourcesToSession.do',
            params : {
                url : urls, 
                name : names,
                description : descriptions,
                localPath : localPaths,
                northBoundLatitude : northBoundLatitudes,
                eastBoundLongitude : eastBoundLongitudes,
                southBoundLatitude : southBoundLatitudes,
                westBoundLongitude : westBoundLongitudes
            },
            callback : function(options, success, response) {
                loadMask.hide();
                
                if (!success) {
                    Ext.Msg.alert('Communications Error', 'Failed to communicate with server. Please try again in a few minutes or report it to cg-admin@csiro.au.');
                    return;
                }

                var responseObj = Ext.JSON.decode(response.responseText);
                if (!responseObj.success) {
                    Ext.Msg.alert('Application Error', 'An unexpected error has occurred. Please report it to cg-admin@csiro.au.');
                    return;
                }
                
                Ext.Msg.alert('Request Saved', 'Your download request has been saved. You can either continue selecting more data or <a href="jobbuilder.html">create a job</a> to process your existing selections.');
            }
        });
    },

	/**
     * This function is now deprecated. It has been replaced by
	 * addSelectedResourcesToSession function.
     */
    showSelectionWindow : function(resource, cswRecord) {        
        var bboxes = cswRecord.get('geographicElements');
        var bbox = null;
        if (bboxes.length > 0) {
            bbox = bboxes[0];
        }

        var popup = Ext.create('Ext.window.Window', {
            layout : 'fit',
            width : 700,
            height : 400,
            modal : true,
            title : 'Download from ' + cswRecord.get('name'),
            items : [{
                xtype : 'filedownloadpanel',
                itemId : 'download-panel',
                region : bbox,
                url : resource.get('url'),
                localPath : resource.get('name'),
                name : resource.get('name'),
                description : resource.get('description')
            }],
            buttons : [{
                text : 'Capture File',
                iconCls : 'add',
                align : 'right',
                handler : function(btn) {
                    var parentWindow = btn.findParentByType('window');
                    var panel = parentWindow.getComponent('download-panel');
                    var params = panel.getForm().getValues();

                    var myMask = new Ext.LoadMask(parentWindow, {msg:"Saving download request..."});
                    myMask.show();
                    Ext.Ajax.request({
                        url : 'addDownloadRequestToSession.do',
                        params : params,
                        callback : function(options, success, response) {
                            myMask.hide();
                            if (!success) {
                                portal.widgets.window.ErrorWindow.show('Communications Error', 'Unable to communicate with server. Please try again in a few minutes');
                                return;
                            }

                            var responseObj = Ext.JSON.decode(response.responseText);
                            if (!responseObj.success) {
                                portal.widgets.window.ErrorWindow.show(responseObj);
                                return;
                            }

                            parentWindow.close();
                            Ext.Msg.alert('Request Saved', 'Your download request has been saved. You can either continue selecting more data or <a href="jobbuilder.html">create a job</a> to process your existing selections.');
                        }
                    });
                }
            }]
        });

        popup.show();
    }
});