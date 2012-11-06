/**
 * Builds a form panel for Vegl Coverage Layers
 *
 */
Ext.define('vegl.layer.filterer.forms.VeglCoverageFilterForm', {
    extend: 'portal.layer.filterer.BaseFilterForm',

    xCellDim : 0,
    yCellDim : 0,
    zCellDim : 0,
    maxDepth : 0,
    mgaZone : 0,

    /**
     * Accepts a config for portal.layer.filterer.BaseFilterForm
     */
    constructor : function(config) {
        var layer = config.layer;
        var map = config.map;
        var filterer = layer.get('filterer');
        var displayTextFormat = '<span style="color:#888888;">{0}</span>';

        //Warning - the following limits us to Google Maps. A necessary evil unless we upgrade the base map abstraction.
        if (!(map instanceof portal.map.gmap.GoogleMap)) {
            console.error('VeglCoverageFilterForm is currently only compatible with Google Maps.');
            this.callParent(arguments);
            return;
        }

        //Prepare the map instance (Gmap specific)
        var gMap2 = map.map;
        var pos = new GControlPosition(G_ANCHOR_BOTTOM_LEFT, new GSize(0, -85));
        gMap2.addControl(new MStatusControl({position:pos}));


        // create a drag control for each bounding box
        this.dataBbox = new MPolyDragControl({
            map: gMap2,
            type: 'rectangle',
            labelText: layer.get('name'),
            ondragend : Ext.bind(function() {
                this.drawDisplayField.setValue(Ext.util.Format.format(displayTextFormat, ''));
                this.captureDisplayField.setValue(Ext.util.Format.format(displayTextFormat, 'Capture the drawn subset'));
                this.captureButton.setDisabled(false);
            }, this)
        });

        var sliderHandler = function(caller, newValue) {
            var newOpacity = (newValue / 100);
            filterer.setParameter('opacity',newOpacity);
        };

        if(!filterer.getParameter('opacity')){
            filterer.setParameter('opacity',100);
        }

        // data bounding box lat/lng text fields
        this.drawDisplayField = Ext.create('Ext.form.field.Display', {
            itemId : 'drawField',
            value : Ext.util.Format.format(displayTextFormat, '')
        });
        this.captureDisplayField = Ext.create('Ext.form.field.Display', {
            itemId : 'captureField',
            value : Ext.util.Format.format(displayTextFormat, 'Use the viewport as a subset')
        });

        this.captureButton = Ext.create('Ext.Button', {
            text : 'Capture Viewport',
            bodyStyle : 'margin:5px 0 0 0',
            itemId : 'captureButton',
            scope: this,
            handler: function() {
                var bbox = null;

                //Our bbox is either the visible bounds or the drawn subset box
                if (this.dataBbox.getParams() === null) {
                    bbox = map.getVisibleMapBounds();
                } else {
                    bbox = Ext.create('portal.util.BBox', {
                        eastBoundLongitude : this.dataBbox.getNorthEastLng(),
                        westBoundLongitude : this.dataBbox.getSouthWestLng(),
                        northBoundLatitude : this.dataBbox.getNorthEastLat(),
                        southBoundLatitude : this.dataBbox.getSouthWestLat()
                    });
                }

                // check if selected region is within the bounds of the coverage layer
                var cswRecords = layer.get('cswRecords');
                if (cswRecords.length !== 0) {
                    //Assumption - we only expect 1 WCS
                    var cswRecord = cswRecords[0];
                    var bboxes = cswRecord.get('geographicElements');
                    if (bboxes.length > 0) {
                        //Assumption - we only expect 1 BBox
                        var datasetBbox = bboxes[0];

                        if (!datasetBbox.containsBbox(bbox)) {
                            Ext.Msg.alert("Error", 'Selected region exceeds the coverage bounds. Please adjust region selection.');
                            return;
                        }
                    }

                    this.showSelectionWindow(bbox, layer, cswRecord);
                }
            }
        });

        this.drawButton = Ext.create('Ext.Button', {
            text : 'Draw Subset',
            itemId : 'drawButton',
            scope : this,
            handler : function() {
                if (this.dataBbox.getParams() || this.dataBbox.transMarkerEnabled) {
                    this.dataBbox.reset();
                    this.dataBbox.disableTransMarker();

                    this.drawButton.setText("Draw Subset");
                    this.captureButton.setText("Capture Viewport");
                    this.captureButton.setDisabled(false);
                    this.drawDisplayField.setValue(Ext.util.Format.format(displayTextFormat, ''));
                    this.captureDisplayField.setValue(Ext.util.Format.format(displayTextFormat, 'Use the viewport as a subset'));
                } else {
                    this.dataBbox.enableTransMarker();
                    this.drawButton.setText("Clear Subset");
                    this.captureButton.setText("Capture Subset");
                    this.captureButton.setDisabled(true);
                    this.drawDisplayField.setValue(Ext.util.Format.format(displayTextFormat, 'Click and drag a box on the map'));
                    this.captureDisplayField.setValue(Ext.util.Format.format(displayTextFormat, ''));
                }
            }
        });

        Ext.apply(config, {
            delayedFormLoading: false, //we don't need to use a delayed load as all of our info is ready now
            border      : false,
            autoScroll  : true,
            hideMode    : 'offsets',
            labelAlign  : 'right',
            bodyStyle   : 'padding:5px',
            autoHeight:    true,
            layout: 'anchor',
            items:[{
                xtype      :'fieldset',
                title      : 'Coverage Subset Controls',
                autoHeight : true,
                items      : [{
                    xtype       : 'slider',
                    fieldLabel  : 'Opacity',
                    anchor      : '100%',
                    minValue    : 0,
                    maxValue    : 100,
                    value       : (filterer.getParameter('opacity') * 100),
                    listeners   : {changecomplete: sliderHandler}
                },{
                    xtype: 'fieldcontainer',
                    anchor: '100%',
                    layout: {
                        type: 'hbox',
                        defaultMargins: {top: 0, right: 5, bottom: 0, left: 0}
                    },
                    items:[this.drawButton, this.drawDisplayField]
                },{
                    xtype: 'fieldcontainer',
                    anchor: '100%',
                    layout: {
                        type: 'hbox',
                        defaultMargins: {top: 0, right: 5, bottom: 0, left: 0}
                    },
                    items:[this.captureButton, this.captureDisplayField]
                }]
            }]
        });
        this.callParent(arguments);
    },

    /**
     * Shows a selection window confirming the specified bounding box selection for the specified layer
     */
    showSelectionWindow : function(bbox, layer, cswRecord) {
        var wcsResources = portal.csw.OnlineResource.getFilteredFromArray(cswRecord.get('onlineResources'), portal.csw.OnlineResource.WCS);
        if (wcsResources.length === 0) {
            return;
        }

        //Assumption - only 1 WCS per layer
        var wcsResource = wcsResources[0];

        var popup = Ext.create('Ext.window.Window', {
            layout : 'fit',
            width : 700,
            height : 400,
            modal : true,
            title : 'Subset of ' + layer.get('name'),
            items : [{
                xtype : 'erddapsubsetpanel',
                itemId : 'subset-panel',
                region : bbox,
                name : 'Subset of ' + layer.get('name')
            }],
            buttons : [{
                text : 'Capture Request',
                iconCls : 'add',
                align : 'right',
                handler : function(btn) {
                    var parentWindow = btn.findParentByType('window');
                    var panel = parentWindow.getComponent('subset-panel');

                    var params = panel.getForm().getValues();
                    params.layerName = wcsResource.get('name');

                    var myMask = new Ext.LoadMask(parentWindow, {msg:"Saving subset request..."});
                    myMask.show();
                    Ext.Ajax.request({
                        url : 'addErddapRequestToSession.do',
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
                            Ext.Msg.alert('Request Saved', 'Your subset request has been saved. You can either continue selecting more data or <a href="jobbuilder.html">create a job</a> to process your existing selections.');
                        }
                    });
                }
            }]
        });

        popup.show();
    }
});