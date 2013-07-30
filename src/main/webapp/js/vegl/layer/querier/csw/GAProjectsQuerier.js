/**
 * Class for parsing a set of portal.csw.CSWRecord objects request/response
 * using the Querier interface
 * 
 * The resulting Querier is specialised for rendering a parent/child
 * relationship of CSW records. The GUI is specialised for Geoscience Australia
 * ISO records for their gridded /elevation/magnetics/radiometrics data.
 */
Ext.define('vegl.layer.querier.csw.GAProjectsQuerier', {
    extend: 'portal.layer.querier.Querier',

    constructor: function(config){
        this.callParent(arguments);
    },

    _getFirstCSWRecordWithKeyword : function(cswRecords, keywords) {
        for (var i = 0; i < cswRecords.length; i++) {
            if (cswRecords[i].containsKeywords(keywords)) {
                return cswRecords[i];
            }
        }
        return null;
    },
    
    _getFirstMatchingGridInfoValue : function(griddedInfo, name, uom) {
        for (var i = 0; i < griddedInfo.length; i++) {
            if (griddedInfo[i].nameOfMeasure === name && 
                griddedInfo[i].unitOfMeasure === uom) {
                return griddedInfo[i].value;
            }
        }
        return null;
    },
    
    _createNamedTabForCSWRecord : function(title, cswRecord) {
        var griddedInfo = cswRecord.get('extensions').griddedInfo;
        var cellSizeM = this._getFirstMatchingGridInfoValue(griddedInfo, "CellSize", "metres");
        var cellSizeDD = this._getFirstMatchingGridInfoValue(griddedInfo, "CellSize", "decimal degrees");
        var lineSpacingM = this._getFirstMatchingGridInfoValue(griddedInfo, "LineSpacing", "metres");
        
        //Build a preview URL
        var geoEls = cswRecord.get('geographicElements');
        var wmsResources = portal.csw.OnlineResource.getFilteredFromArray(cswRecord.get('onlineResources'), portal.csw.OnlineResource.WMS);
        var previewUrl = null;
        var previewWidth = 1;
        var previewHeight = 1;
        if (geoEls.length > 0 && wmsResources.length > 0) {
            var superBbox = geoEls[0];
            for (var i = 1; i < geoEls.length; i++) {
                superBbox = superBbox.combine(geoEls[i]);
            }
    
            //Set our Height to a constant and scale the height appropriately
            var widthRatio = (superBbox.eastBoundLongitude - superBbox.westBoundLongitude) /
                             (superBbox.northBoundLatitude - superBbox.southBoundLatitude);
            var height = 128;
            var width = Math.floor(height * widthRatio);
    
            previewWidth = width;
            previewHeight = height;
    
            var url = wmsResources[0].get('url');
            var name = wmsResources[0].get('name');
            if (cswRecord.get('version')=='1.3.0') {
                previewUrl = portal.map.primitives.BaseWMSPrimitive.getWms_130_Url(url, name, superBbox, width, height); 
            } else {
                previewUrl = portal.map.primitives.BaseWMSPrimitive.getWmsUrl(url, name, superBbox, width, height);
            }
        }
        
        return {
            title : title,
            items : [{
                xtype : 'label',
                style : 'font-size: 11px; font-style:normal;',
                html : Ext.util.Format.format('<a target="_blank" href="{1}">{0}</a>', cswRecord.get('name'), cswRecord.get('recordInfoUrl')) 
            },{
                layout : 'hbox',
                border : false,
                items : [{
                    xtype : 'container',
                    flex : 1,
                    height : 100,
                    layout : {
                        type : 'hbox',
                        pack : 'center',
                        align : 'middle'
                    },
                    items : [{
                        xtype : 'datadisplayfield',
                        uom : 'm',
                        uomTip : 'metres',
                        fieldLabel : 'Cell Size',
                        value : cellSizeM ? cellSizeM : 'Unknown',
                    }]
                },{
                    xtype : 'container',
                    flex : 1,
                    height : 100,
                    layout : {
                        type : 'hbox',
                        pack : 'center',
                        align : 'middle'
                    },
                    items : [{
                        xtype : 'datadisplayfield',
                        uom : 'DD',
                        uomTip : 'decimal degrees',
                        fieldLabel : 'Cell Size',
                        value : cellSizeDD ? cellSizeDD : 'Unknown',
                    }]
                },{
                    xtype : 'container',
                    flex : 1,
                    height : 100,
                    layout : {
                        type : 'hbox',
                        pack : 'center',
                        align : 'middle'
                    },
                    items : [{
                        xtype : 'datadisplayfield',
                        uom : 'm',
                        uomTip : 'metres',
                        fieldLabel : 'Line Spacing',
                        value : lineSpacingM ? lineSpacingM : 'Unknown',
                    }]
                },{
                    xtype : 'imagedisplayfield',
                    flex : 1,
                    imgHref : previewUrl,
                    imgWidth : previewWidth,
                    imgHeight : previewHeight,
                    fieldLabel : "Grid Preview"
                }]
            },{
                xtype : 'button',
                iconCls : 'add',
                text : 'Capture this grid',
                style : 'position: absolute; bottom: 10px; left: 5px'
            },{
                xtype : 'button',
                iconCls : 'edit',
                text : 'Edit this grid',
                style : 'position: absolute; bottom: 10px; left: 130px'
            }]
        };
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
        
        //source url/domain
        var sourceUrl = cswRecord.get('recordInfoUrl');
        var urlRegexp = /^((http[s]?|ftp):\/)?\/?([^:\/\s]+)((\/\w+)*\/)([\w\-\.]+[^#?\s]+)(.*)?(#[\w\-]+)?$/;
        var match = urlRegexp.exec(sourceUrl);
        var sourceHost = match[3];
        
        var dateStamp = cswRecord.get('extensions').dateStamp;
        if (dateStamp) {
            dateStamp = '(' + dateStamp + ')';
        } else {
            dateStamp = '';
        }
            
        //Create gridded info
        var childRecords = cswRecord.get('childRecords');
        var magRecord = this._getFirstCSWRecordWithKeyword(childRecords, ["MAG", "MAGNETICS"]);
        var elevationRecord = this._getFirstCSWRecordWithKeyword(childRecords, ["ELE", "ELEVATION"]);
        var radRecord = this._getFirstCSWRecordWithKeyword(childRecords, ["RAD", "RADIOMETRICS"]);
        var tabs = [];
        if (elevationRecord) {
            tabs.push(this._createNamedTabForCSWRecord("Elevation Grid", elevationRecord));
        }
        if (magRecord) {
            tabs.push(this._createNamedTabForCSWRecord("Magnetics Grid", magRecord));
        }
        if (radRecord) {
            tabs.push(this._createNamedTabForCSWRecord("Radiometrics Grid", radRecord));
        }
        
        var panel = Ext.create('portal.layer.querier.BaseComponent', {
            border : false,
            autoScroll : true,
            items : [{
                layout : 'fit',
                border : false,
                items : [{
                    xtype : 'fieldset',
                    title : Ext.util.Format.format('<span style="font-size:16px;">{0} {1}</span>',cswRecord.get('name'), dateStamp),
                    border : false,
                    items : [{
                        xtype : 'label',
                        anchor : '100%',
                        style : "font-size: 12px; font-style:italic;",
                        html : cswRecord.get('description') + '<br/><br/>'
                    },{
                        xtype : 'label',
                        anchor : '100%',
                        style : "font-size: 12px;",
                        html : Ext.util.Format.format('<span>This metadata is sourced from <a target="_blank" href="{1}">{0}</a></span>', sourceHost, sourceUrl)
                    }, {
                        xtype : 'tabpanel',
                        padding : '20 0 0 0', 
                        tabBar : {
                            plain : true
                        },
                        items : tabs
                    }]
                }]
            }]
        });

        callback(this, [panel], queryTarget);
    }
});