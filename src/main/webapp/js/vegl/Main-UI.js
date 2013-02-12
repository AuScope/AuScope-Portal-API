Ext.application({
    name : 'portal',

    //Here we build our GUI from existing components - this function should only be assembling the GUI
    //Any processing logic should be managed in dedicated classes - don't let this become a
    //monolithic 'do everything' function
    launch : function() {

        //Send these headers with every AJax request we make...
        Ext.Ajax.defaultHeaders = {
            'Accept-Encoding': 'gzip, deflate' //This ensures we use gzip for most of our requests (where available)
        };

        var urlParams = Ext.Object.fromQueryString(window.location.search.substring(1));
        var isDebugMode = urlParams.debug;

        //Create our CSWRecord store (holds all CSWRecords not mapped by known layers)
        var unmappedCSWRecordStore = Ext.create('Ext.data.Store', {
            model : 'portal.csw.CSWRecord',
            groupField: 'contactOrg',
            proxy : {
                type : 'ajax',
                url : 'getUnmappedCSWRecords.do',
                reader : {
                    type : 'json',
                    root : 'data'
                }
            },
            autoLoad : true
        });

        //Our custom record store holds layers that the user has
        //added to the map using a OWS URL entered through the
        //custom layers panel
        var customRecordStore = Ext.create('Ext.data.Store', {
            model : 'portal.csw.CSWRecord',
            proxy : {
                type : 'ajax',
                url : 'getCustomLayers.do',
                reader : {
                    type : 'json',
                    root : 'data'
                }
            },
            autoLoad : false,
            data : []
        });

        //Create our KnownLayer store
        var knownLayerStore = Ext.create('Ext.data.Store', {
            model : 'portal.knownlayer.KnownLayer',
            groupField: 'group',
            proxy : {
                type : 'ajax',
                url : 'getKnownLayers.do',
                reader : {
                    type : 'json',
                    root : 'data'
                }
            },
            autoLoad : true
        });

        //Create our store for holding the set of
        //layers that have been added to the map
        var layerStore = Ext.create('portal.layer.LayerStore', {});

        //We need something to handle the clicks on the map
        var queryTargetHandler = Ext.create('portal.layer.querier.QueryTargetHandler', {});


        //Create our map implementations
        var mapCfg = {
            container : null,   //We will be performing a delayed render of this map
            layerStore : layerStore,
            listeners : {
                query : function(mapWrapper, queryTargets) {
                    queryTargetHandler.handleQueryTargets(mapWrapper, queryTargets);
                }
            }
        };
        var urlParams = Ext.Object.fromQueryString(window.location.search.substring(1));
        var map = Ext.create('portal.map.gmap.GoogleMap', mapCfg);

        var layersPanel = Ext.create('portal.widgets.panel.LayerPanel', {
            title : 'Active Layers',
            region : 'center',
            store : layerStore,
            map : map,
            allowDebugWindow : isDebugMode,
            listeners : {
                //On selection, update our filter panel
                select : function(rowModel, record, index) {
                    filterPanel.showFilterForLayer(record);
                },
                removelayerrequest: function(sourceGrid, record) {
                    filterPanel.clearFilter();
                }
            }
        });

        /**
         * Used to show extra details for querying services
         */
        var filterPanel = Ext.create('portal.widgets.panel.FilterPanel', {
            region: 'south',
            layerPanel : layersPanel,
            map : map,
            split: true,
            height: 170
        });

        var layerFactory = Ext.create('portal.layer.LayerFactory', {
            map : map,
            formFactory : Ext.create('vegl.layer.filterer.VeglFormFactory', {map : map}),
            downloaderFactory : Ext.create('vegl.layer.VeglDownloaderFactory', {map: map}),
            querierFactory : Ext.create('vegl.layer.VeglQuerierFactory', {map: map}),
            rendererFactory : Ext.create('vegl.layer.VeglRendererFactory', {map: map})
        });

        //Utility function for adding a new layer to the map
        //record must be a CSWRecord or KnownLayer
        var handleAddRecordToMap = function(sourceGrid, record) {
            var newLayer = null;

            //Ensure the layer DNE first
            var existingRecord = layerStore.getById(record.get('id'));
            if (existingRecord) {
                layersPanel.getSelectionModel().select([existingRecord], false);
                return;
            }

            //Turn our KnownLayer/CSWRecord into an actual Layer
            if (record instanceof portal.csw.CSWRecord) {
                newLayer = layerFactory.generateLayerFromCSWRecord(record);
            } else {
                newLayer = layerFactory.generateLayerFromKnownLayer(record);
            }

            //We may need to show a popup window with copyright info
            var cswRecords = newLayer.get('cswRecords');
            for (var i = 0; i < cswRecords.length; i++) {
                if (cswRecords[i].hasConstraints()) {
                    Ext.create('portal.widgets.window.CSWRecordConstraintsWindow', {
                        cswRecords : cswRecords
                    }).show();
                    break;
                }
            }

            layerStore.add(newLayer); //this adds the layer to our store
            layersPanel.getSelectionModel().select([newLayer], false); //this ensures it gets selected
        };

        var knownLayersPanel = Ext.create('portal.widgets.panel.KnownLayerPanel', {
            title : 'Featured Layers',
            store : knownLayerStore,
            map : map,
            listeners : {
                addlayerrequest : handleAddRecordToMap
            }
        });

        // basic tabs 1, built from existing content
        var tabsPanel = Ext.create('Ext.TabPanel', {
            activeTab : 0,
            region : 'north',
            split : true,
            height : 265,
            enableTabScroll : true,
            items:[knownLayersPanel]
        });

        /**
         * Used as a placeholder for the tree and details panel on the left of screen
         */
        var westPanel = {
            layout: 'border',
            region:'west',
            border: false,
            split:true,
            //margins: '100 0 0 0',
            margins:'100 0 0 3',
            width: 350,
            items:[tabsPanel , layersPanel, filterPanel]
        };

        /**
         * This center panel will hold the google maps instance
         */
        var centerPanel = Ext.create('Ext.panel.Panel', {
            region: 'center',
            id: 'center_region',
            margins: '100 0 0 0',
            cmargins:'100 0 0 0'
        });

        /**
         * Add all the panels to the viewport
         */
        var viewport = Ext.create('Ext.container.Viewport', {
            layout:'border',
            items:[westPanel, centerPanel]
        });

        map.renderToContainer(centerPanel);   //After our centerPanel is displayed, render our map into it

        // The subset button needs a handler for when the user draws a subset bbox on the map:
        map.map.addControl(new GmapSubsetControl(function(nw, ne, se, sw) {
          var bbox = Ext.create('portal.util.BBox', {
            northBoundLatitude : nw.lat(),
                southBoundLatitude : sw.lat(),
                eastBoundLongitude : ne.lng(),
                westBoundLongitude : sw.lng()
          });

          //Iterate all active layers looking for data sources (csw records) that intersect the selection
          var intersectedRecords = [];
          for (var layerIdx = 0; layerIdx < layerStore.getCount(); layerIdx++) {
              var layer = layerStore.getAt(layerIdx);
              var cswRecs = layer.get('cswRecords');
              for (var recIdx = 0; recIdx < cswRecs.length; recIdx++) {
                  var cswRecord = cswRecs[recIdx];
                  var geoEls = cswRecord.get('geographicElements');
                  for (var geoIdx = 0; geoIdx < geoEls.length; geoIdx++) {
                      var bboxToCompare = geoEls[geoIdx];
                      if (bbox.intersects(bboxToCompare)) {
                          intersectedRecords.push(cswRecord);
                          break;
                      }
                  }
              }
          }

          //Show a dialog allow users to confirm the selected data sources
          if (intersectedRecords.length > 0) {
              Ext.create('Ext.Window', {
                  width : 700,
                  maxHeight : 400,
                  title : 'Confirm which datasets you wish to select',
                  modal : true,
                  autoScroll : true,
                  items : [{
                      xtype : 'dataselectionpanel',
                      region : bbox,
                      itemId : 'dataselection-panel',
                      cswRecords : intersectedRecords
                  }],
                  buttons : [{
                      text : 'Capture Data',
                      iconCls : 'add',
                      align : 'right',
                      scope : this,
                      handler : function(btn) {
                          var parentWindow = btn.findParentByType('window');
                          var panel = parentWindow.getComponent('dataselection-panel');

                          panel.saveCurrentSelection(function(totalSelected, totalErrors) {
                              if (totalSelected === 0) {
                                  Ext.Msg.alert('No selection', 'You haven\'t selected any data to capture. Please select one or more rows by checking the box alongside each row.');
                              } else if (totalErrors === 0) {
                                  Ext.Msg.alert('Request Saved', 'Your ' + totalSelected + ' dataset(s) have been saved. You can either continue selecting more data or <a href="jobbuilder.html">create a job</a> to process your existing selections.');
                                  parentWindow.close();
                              } else {
                                  Ext.Msg.alert('Error saving data', 'There were one or more errors when saving some of the datasets you selected');
                                  parentWindow.close();
                              }
                          });
                      }
                  }]
              }).show();
          }

        }), new GControlPosition(G_ANCHOR_TOP_RIGHT, new GSize(405, 7)));

        //Create our help button content
        var manager = Ext.create('portal.util.help.InstructionManager', {});
        var helpButtonEl = Ext.get('help-button');
        helpButtonEl.on('click', function() {
            manager.showInstructions([Ext.create('portal.util.help.Instruction', {
                highlightEl : tabsPanel.getEl(),
                title : 'Find data/layers',
                description : 'In this panel a list of all available datasets in the form of layers will be presented to you. To visualise a layer, select it and press the "Add Layer to Map" button.<br/><br/>Further information about the data behind each layer can be displayed by clicking the icons alongside the layer name.'
            }),Ext.create('portal.util.help.Instruction', {
                highlightEl : layersPanel.getEl(),
                title : 'Manage Layers',
                description : 'Whenever you add a layer to the map, it will be listed in this window. Layers can be removed by selecting them and pressing "Remove Layer". Selecting a layer will also bring up any advanced filter options in the window below.'
            }),Ext.create('portal.util.help.Instruction', {
                highlightEl : filterPanel.getEl(),
                title : 'Apply filters',
                description : 'Some layers allow you to filter what data will get visualised on the map. If the layer supports filtering, additional options will be displayed in this window. Select "Apply Filter" to update the visualised data on the map'
            }),Ext.create('portal.util.help.Instruction', {
                highlightEl : centerPanel.getEl(),
                anchor : 'right',
                title : 'Visualise Data',
                description : 'The map panel here is where all of the currently added layers will be visualised. You can pan and zoom the map to an area of interest if required.'
            }),Ext.create('portal.util.help.Instruction', {
                highlightEl : 'gmap-subset-control',
                anchor : 'right',
                title : 'Select Data',
                description : 'After reviewing one or more layers you can draw a region of interest using this button. All layers with data in the region you draw will be selected for use in a processing job. If the layer supports it, the data will be constrained to the region you select'
            }),Ext.create('portal.util.help.Instruction', {
                highlightEl : 'help-button',
                anchor : 'bottom',
                title : 'More information',
                description : 'For futher information, please consult the online <a target="_blank" href="https://www.seegrid.csiro.au/wiki/NeCTARProjects/VglUserGuide">VGL wiki</a>.'
            })]);
        });
    }
});