/**
 * A panel for rendering a set of CSWRecords that have been (sub)selected by a bounding box.
 */
Ext.define('vegl.widgets.DataSelectionPanel', {
    extend : 'Ext.grid.Panel',

    alias : 'widget.dataselectionpanel',

    /**
     * Accepts the following:
     * {
     *  region : portal.util.BBox - The selected area (defaults to 0,0,0,0)
     *  cswRecordAndLayers : Object[] - The records to display info for. Passed in as an object with two fields.
     *                        'layer' should reference the parent portal.layer.Layer and 'cswRecord' should reference
     *                        the child portal.csw.CSWRecord
     * }
     */
    constructor : function(config) {
        if (!config.region) {
            config.region = Ext.create('portal.util.BBox', {});
        }
        this.region = config.region;

        if (!config.cswRecordAndLayers) {
            config.cswRecordAndLayers = [];
        }

        var dataItems = vegl.widgets.DataSelectionPanelRow.parseCswRecords(config.cswRecordAndLayers);

        var groupingFeature = Ext.create('Ext.grid.feature.Grouping',{
            groupHeaderTpl: '{name} ({[values.rows.length]} {[values.rows.length > 1 ? "Items" : "Item"]})'
        });

        //Build our configuration object
        Ext.apply(config, {
            selModel : Ext.create('Ext.selection.CheckboxModel', {}),
            features : [groupingFeature],
            store : Ext.create('Ext.data.Store', {
                groupField : 'resourceType',
                model : 'vegl.widgets.DataSelectionPanelRow',
                data : dataItems
            }),
            hideHeaders : true,
            plugins : [{
                ptype : 'selectablegrid'
            }],
            columns: [{
                //Title column
                dataIndex: 'name',
                flex: 1,
                renderer: Ext.bind(this._nameRenderer, this)
            },{
                dataIndex: 'description',
                width: 280,
                renderer: Ext.bind(this._descriptionRenderer, this)
            }],
            buttons : [{
                text : 'Capture Data',
                iconCls : 'add',
                align : 'right',
                scope : this,
                handler : function(btn) {
                    var panel = btn.findParentByType('dataselectionpanel');
                    var sm = panel.getSelectionModel();
                    var selectedRows = sm.getSelection();

                    if (selectedRows.length === 0) {
                        Ext.Msg.alert('No selection', 'You haven\'t selected any data to capture. Please select one or more rows by checking the box alongside each row.');
                        return;
                    }

                    var myMask = new Ext.LoadMask(panel, {msg:"Saving selected datasets..."});
                    var totalResponses = 0;
                    var totalErrors = 0;
                    var bbox = this.region;
                    myMask.show();

                    var responseHandler = function(success) {
                        totalResponses++;
                        if (!success) {
                            totalErrors++;
                        }

                        if (totalResponses >= selectedRows.length) {
                            myMask.hide();
                            if (totalErrors > 0) {
                                Ext.Msg.error('Error saving data', 'There were one or more errors when saving some of the datasets you selected');
                            } else {
                                Ext.Msg.alert('Request Saved', 'Your ' + totalResponses + ' dataset(s) have been saved. You can either continue selecting more data or <a href="jobbuilder.html">create a job</a> to process your existing selections.');
                            }
                        }
                    };

                    //Different data sources have different functions to save
                    for (var i = 0; i < selectedRows.length; i++) {
                        var row = selectedRows[i];
                        switch (row.get('onlineResource').get('type')) {
                        case portal.csw.OnlineResource.WCS:
                            Ext.Ajax.request({
                                url : 'addErddapRequestToSession.do',
                                params : {
                                    northBoundLatitude : bbox.northBoundLatitude,
                                    southBoundLatitude : bbox.southBoundLatitude,
                                    eastBoundLongitude : bbox.eastBoundLongitude,
                                    westBoundLongitude : bbox.westBoundLongitude,
                                    format : 'nc',
                                    layerName : row.get('onlineResource').get('name'),
                                    name : row.get('onlineResource').get('name'),
                                    description : row.get('onlineResource').get('description'),
                                    localPath : row.get('onlineResource').get('name')
                                },
                                callback : function(options, success, response) {
                                    responseHandler(success);
                                }
                            });
                            break;
                        default:
                            Ext.Ajax.request({
                                url : 'addDownloadRequestToSession.do',
                                params : {
                                    northBoundLatitude : bbox.northBoundLatitude,
                                    southBoundLatitude : bbox.southBoundLatitude,
                                    eastBoundLongitude : bbox.eastBoundLongitude,
                                    westBoundLongitude : bbox.westBoundLongitude,
                                    url : row.get('onlineResource').get('url'),
                                    name : row.get('onlineResource').get('name'),
                                    description : row.get('onlineResource').get('description'),
                                    localPath : row.get('onlineResource').get('name')
                                },
                                callback : function(options, success, response) {
                                    responseHandler(success);
                                }
                            });
                            break;
                        }
                    }
                }
            }]
        });

        this.callParent(arguments);

        //Calculate any WCS size estimations
        for (var i = 0; i < dataItems.length; i++) {
            var dataItem = dataItems[i];
            var or = dataItem.get('onlineResource');
            if (or.get('type') === portal.csw.OnlineResource.WCS) {
                vegl.util.WCSUtil.estimateCoverageSize(this.region, or.get('url'), or.get('name'), dataItem, function(success, errorMsg, response, dataItem) {
                    if (!success) {
                        dataItem.set('description', errorMsg);
                    } else {
                        var approxTotal = Ext.util.Format.number(response.roundedTotal, '0,000');
                        var approxSize = Ext.util.Format.fileSize(vegl.util.WCSUtil.roundToApproximation(response.width * response.height * 4));
                        dataItem.set('description', Ext.util.Format.format('Approximately <b>{0}</b> data points in total.<br>Uncompressed that\'s roughly {1}', approxTotal, approxSize));
                    }
                });
            }
        }
    },

    _nameRenderer : function(value, metaData, record, row, col, store, gridView) {
        var name = record.get('name');
        var url = record.get('onlineResource').get('url');

        //Ensure there is a title (even it is just '<Untitled>'
        if (!name || name.length === 0) {
            name = '&gt;Untitled&lt;';
        }

        //Render our HTML
        return Ext.DomHelper.markup({
            tag : 'div',
            children : [{
                tag : 'b',
                html : name
            },{
                tag : 'br'
            },{
                tag : 'span',
                style : {
                    color : '#555'
                },
                children : [{
                    html : url
                }]
            }]
        });
    },

    _descriptionRenderer : function(value, metaData, record, row, col, store, gridView) {
        var type = record.get('onlineResource').get('type');
        var description = record.get('description');

        //Render our HTML
        switch(type) {
        case portal.csw.OnlineResource.WWW:
        case portal.csw.OnlineResource.FTP:
        case portal.csw.OnlineResource.IRIS:
        case portal.csw.OnlineResource.UNSUPPORTED:
            return Ext.DomHelper.markup({
                tag : 'div',
                children : [{
                    tag : 'a',
                    target : '_blank',
                    href : record.get('onlineResource').get('url'),
                    children : [{
                        tag : 'span',
                        html : 'Click here to download.'
                    }]
                }]
            });
        default:
            return description;
        }
    }
});

Ext.define('vegl.widgets.DataSelectionPanelRow', {
    extend : 'Ext.data.Model',

    statics : {
        /**
         * Parses a single csw record and parent layer into an array of DataSelectionPanelRow items
         */
        parseCswRecord : function(layer, cswRecord) {
            var dataItems = [];
            var resources = cswRecord.get('onlineResources');
            for (var i = 0; i < resources.length; i++) {
                var onlineResource = resources[i];

                //Set the defaults of our new item
                newItem = {
                    layerName : layer.get('name'),
                    resourceType : portal.csw.OnlineResource.typeToString(onlineResource.get('type')),
                    name : onlineResource.get('name'),
                    description : onlineResource.get('description'),
                    edit : false,
                    selected : true,
                    cswRecord : cswRecord,
                    onlineResource : onlineResource,
                    layer : layer
                };

                //Add/subtract info based on resource type
                switch(onlineResource.get('type')) {
                case portal.csw.OnlineResource.WCS:
                    newItem.description = 'Loading size details...';
                    newItem.edit = true;
                    break;
                case portal.csw.OnlineResource.WWW:
                    break;

                //We don't support EVERY type
                default:
                    continue;
                }

                dataItems.push(Ext.create('vegl.widgets.DataSelectionPanelRow', newItem));
            }

            var childCswRecords = cswRecord.get('childRecords');
            if (childCswRecords) {
                for (var i = 0; i < childCswRecords.length; i++) {
                    dataItems = dataItems.concat(vegl.widgets.DataSelectionPanelRow.parseCswRecord(layer, childCswRecords[i]));
                }
            }

            return dataItems;
        },

        /**
         * Parses a CSWRecord array into an array of simple JS objects for usage with the internal data store for this panel
         */
        parseCswRecords : function(cswRecordAndLayers) {
            var dataItems = [];
            for (var i = 0; i < cswRecordAndLayers.length; i++) {
                var cswRecord = cswRecordAndLayers[i].cswRecord;
                var layer = cswRecordAndLayers[i].layer;

                dataItems = dataItems.concat(vegl.widgets.DataSelectionPanelRow.parseCswRecord(layer, cswRecord));
          }

          return dataItems;
        }
    },

    fields: [
             {name : 'layerName', type: 'string'},
             {name : 'resourceType', type: 'string'},
             {name : 'name', type: 'string'},
             {name : 'description', type: 'string'},
             {name : 'edit', type: 'boolean'},
             {name : 'selected', type: 'boolean'},
             {name : 'layer', type: 'auto'},
             {name : 'onlineResource', type: 'auto'},
             {name : 'cswRecord', type: 'auto'}
    ]



});