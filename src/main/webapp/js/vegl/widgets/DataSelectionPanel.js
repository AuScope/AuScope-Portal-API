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
     *  cswRecords : portal.csw.CSWRecord[] - The records to display info for.
     * }
     */
    constructor : function(config) {
        if (!config.region) {
            config.region = Ext.create('portal.util.BBox', {});
        }
        this.region = config.region;

        if (!config.cswRecords) {
            config.cswRecords = [];
        }

        var dataItems = vegl.widgets.DataSelectionPanelRow.parseCswRecords(config.cswRecords, this.region);

        var groupingFeature = Ext.create('Ext.grid.feature.Grouping',{
            groupHeaderTpl: '{name} ({[values.rows.length]} {[values.rows.length > 1 ? "Items" : "Item"]})'
        });

        var hideHeaders = true;
        if (typeof(config.hideHeaders) !== 'undefined' && config.hideHeaders != null) {
            hideHeaders = config.hideHeaders;
        }

        //Build our configuration object
        Ext.apply(config, {
            selModel : Ext.create('Ext.selection.CheckboxModel', {}),
            features : [groupingFeature],
            store : Ext.create('Ext.data.Store', {
                groupField : 'resourceType',
                model : 'vegl.widgets.DataSelectionPanelRow',
                data : dataItems
            }),
            hideHeaders : hideHeaders,
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
                width: 260,
                renderer: Ext.bind(this._descriptionRenderer, this)
            },{
                xtype: 'actioncolumn',
                width:30,
                sortable: false,
                items: [{
                    icon: 'portal-core/img/download.png',
                    tooltip: 'Download this data to your local machine now',
                    scope : this,
                    handler: function(grid, rowIndex, colIndex, item, e, record) {
                        this._handleRowDownload(record);
                    }
                }]
            },{
                xtype: 'actioncolumn',
                width:30,
                sortable: false,
                items: [{
                    icon: 'portal-core/img/edit.png',
                    tooltip: 'Edit the download options',
                    scope : this,
                    handler: function(grid, rowIndex, colIndex, item, e, record) {
                        this._handleRowEdit(record);
                    }
                }]
            }],
            listeners: {
                afterrender: function(panel) {
                    if (dataItems.length === 1) {
                        panel.setSelection(dataItems[0]);
                    }
                }
            }
        });

        this.callParent(arguments);

        //Update the descriptions of all layers now they have been added
        for (var i = 0; i < dataItems.length; i++) {
            this._updateDescription(dataItems[i]);
        }
    },

    /**
     * Handles the editing of a row by showing a popup and then updating the row description upon completion
     */
    _handleRowEdit : function(dataItem) {
        var or = dataItem.get('onlineResource');
        var dlOptions = dataItem.get('downloadOptions');

        vegl.util.DataSelectionUtil.showDownloadOptionsForResource(or, dlOptions, Ext.bind(function(updatedDlOptions) {
            dataItem.set('downloadOptions', updatedDlOptions);
            this._updateDescription(dataItem);
        }, this));
    },

    /**
     * Handles the editing of a row by showing a popup and then updating the row description upon completion
     */
    _handleRowDownload : function(dataItem) {
        var or = dataItem.get('onlineResource');
        var dlOptions = dataItem.get('downloadOptions');

        vegl.util.DataSelectionUtil.makeDownloadUrl(or, dlOptions, false, function(success, dl) {
            if (!success) {
                Ext.Msg.alert('Download Error', 'There was an error generating your download URL. Please try again in a few minutes or consider refreshing this page.');
                return;
            }

            portal.util.FileDownloader.downloadFile(dl.get('url'),[],dlOptions.method);
        });
    },

    /**
     * Saves every single selected row in this panel to the user's session. Errors are reported
     * via a callback function
     *
     * @param callback function(totalSelected, totalErrors)
     */
    saveCurrentSelection: function(callback) {
        var sm = this.getSelectionModel();
        var selectedRows = sm.getSelection();

        if (selectedRows.length === 0) {
            callback(0, 0);
            return;
        }


        var totalResponses = 0;
        var totalErrors = 0;

        var myMask = new Ext.LoadMask({
            msg : 'Saving selected datasets...',
            target : this
        });

        myMask.show();

        //Simple handler for keeping track of how many requests we've sent vs how many responses we've received
        var responseHandler = function(success) {
            totalResponses++;
            if (!success) {
                totalErrors++;
            }

            if (totalResponses >= selectedRows.length) {
                myMask.hide();
                callback(totalResponses, totalErrors);
            }
        };

        //Different data sources have different functions to save
        for (var i = 0; i < selectedRows.length; i++) {
            vegl.util.DataSelectionUtil.makeDownloadUrl(selectedRows[i].get('onlineResource'),
                    selectedRows[i].get('downloadOptions'),
                    true,
                    responseHandler);
        }
    },

    /**
     * Updates the description text for the specified dataitem. The update may occur asynchronously
     */
    _updateDescription : function(dataItem) {
        var or = dataItem.get('estimateResource');
        var dlOptions = dataItem.get('downloadOptions');

        // WCS resources have their description based on the selected region (whatever that may be)
        if (or.get('type') === portal.csw.OnlineResource.WCS) {
            dataItem.set('description', 'Loading size details...');
            vegl.util.WCSUtil.estimateCoverageSize(dlOptions, or.get('url'), dlOptions.layerName, dataItem, function(success, errorMsg, response, dataItem) {
                if (!success) {
                    dataItem.set('description', errorMsg);
                } else {
                    var approxTotal = Ext.util.Format.number(response.roundedTotal, '0,000');
                    var approxSize = Ext.util.Format.fileSize(vegl.util.WCSUtil.roundToApproximation(response.width * response.height * 4));
                    dataItem.set('description', Ext.util.Format.format('Approximately <b>{0}</b> data points in total.<br>Uncompressed that\'s roughly {1}', approxTotal, approxSize));
                }
            });
        } else if (or.get('type') === portal.csw.OnlineResource.WFS) {
            dataItem.set('description', 'Loading size details...');
            vegl.util.WFSUtil.estimateFeatureCount(dlOptions, or.get('url'), dlOptions.featureType, dataItem, function(success, errorMsg, response, dataItem) {
                if (!success) {
                    dataItem.set('description', errorMsg);
                } else {
                    var total = Ext.util.Format.number(response.total, '0,000');
                    dataItem.set('description', Ext.util.Format.format('Approximately <b>{0}</b> features have been selected.', total));
                }
            });
        } else {
            //Otherwise the description is pulled direct from the online resource
            dataItem.set('description', or.get('description'));
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
        DATA_TYPE_PRIORITY: [portal.csw.OnlineResource.WCS, portal.csw.OnlineResource.NCSS, portal.csw.OnlineResource.WFS], //From low to high
        ESTIMATE_TYPE_PRIORITY: [portal.csw.OnlineResource.WCS, portal.csw.OnlineResource.WFS], //From low to high

        /**
         * Given an array of OnlineResources, choose the first item in that array with the highest priority.
         * (will only return resources that match vegl.util.DataSelectionUtil.isResourceSupported)
         */
        getHighestPriorityType: function(onlineResources, priorityArray) {
            //Gets the priority for a single online resource or -1.
            var getPriority = function(or) {
                var currentPriority = -1;
                Ext.each(priorityArray, function(pType, index) {
                    if (or.get('type') === pType ) {
                        currentPriority = index;
                        return false;
                    }
                });
                return currentPriority;
            };

            var highestPriority = -99;
            var highestResource = null;
            Ext.each(onlineResources, function(currentResource) {
                if (vegl.util.DataSelectionUtil.isResourceSupported(currentResource)) {
                    var currentPriority = getPriority(currentResource);
                    if (currentPriority > highestPriority) {
                        highestPriority = currentPriority;
                        highestResource = currentResource;
                    }
                }
            });

            return highestResource;
        },

        /**
         * Parses a single csw record and parent layer into an array of DataSelectionPanelRow items
         */
        parseCswRecord : function(cswRecord, defaultBbox) {
            var dataItems = [];
            var resources = cswRecord.get('onlineResources');

            var preferredDataServiceResource = vegl.widgets.DataSelectionPanelRow.getHighestPriorityType(resources, vegl.widgets.DataSelectionPanelRow.DATA_TYPE_PRIORITY);
            var preferredEstimateServiceResource = vegl.widgets.DataSelectionPanelRow.getHighestPriorityType(resources, vegl.widgets.DataSelectionPanelRow.ESTIMATE_TYPE_PRIORITY);

            if (preferredDataServiceResource != null) {
                //Set the defaults of our new item
                var newItem = {
                    resourceType : portal.csw.OnlineResource.typeToString(preferredDataServiceResource.get('type')),
                    name : preferredDataServiceResource.get('name'),
                    description : preferredDataServiceResource.get('description'),
                    selected : true,
                    cswRecord : cswRecord,
                    onlineResource : preferredDataServiceResource,
                    estimateResource : preferredEstimateServiceResource,
                    downloadOptions : vegl.util.DataSelectionUtil.createDownloadOptionsForResource (preferredDataServiceResource, cswRecord, defaultBbox)
                };

                dataItems.push(Ext.create('vegl.widgets.DataSelectionPanelRow', newItem));
            }

            var childCswRecords = cswRecord.get('childRecords');
            if (childCswRecords) {
                for (var i = 0; i < childCswRecords.length; i++) {
                    dataItems = dataItems.concat(vegl.widgets.DataSelectionPanelRow.parseCswRecord(childCswRecords[i], defaultBbox));
                }
            }

            return dataItems;
        },

        /**
         * Parses a CSWRecord array into an array of simple JS objects for usage with the internal data store for this panel
         */
        parseCswRecords : function(cswRecords, defaultBbox) {
            var dataItems = [];
            for (var i = 0; i < cswRecords.length; i++) {
                var cswRecord = cswRecords[i];

                dataItems = dataItems.concat(vegl.widgets.DataSelectionPanelRow.parseCswRecord(cswRecord, defaultBbox));
          }

          return dataItems;
        }
    },

    fields: [
             {name : 'layerName', type: 'string'},
             {name : 'resourceType', type: 'string'},
             {name : 'name', type: 'string'},
             {name : 'description', type: 'string'},
             {name : 'selected', type: 'boolean'},
             {name : 'onlineResource', type: 'auto'},
             {name : 'estimateResource', type: 'auto'}, //used for data estimation
             {name : 'cswRecord', type: 'auto'},
             {name : 'downloadOptions', type: 'auto'}
    ]



});