/**
 * An abstract base class to be extended.
 *
 * Represents a grid panel for containing layers
 * that haven't yet been added to the map. Each row
 * will be grouped under a heading, contain links
 * to underlying data sources and have a spatial location
 * that can be viewed by the end user.
 *
 * This class is expected to be extended for usage within
 * the 'Registered Layers', 'Known Layers' and 'Custom Layers'
 * panels in the portal. Support for KnownLayers/CSWRecords and
 * other row types will be injected by implementing the abstract
 * functions of this class
 *
 */

Ext.define('portal.widgets.panel.BaseActiveRecordPanel', {
    extend : 'portal.widgets.panel.CommonBaseRecordPanel',
    alias: 'widget.baseactiverecordpanel',

    visibleIcon : 'img/eye.png',
    notVisibleIcon : 'img/eye_off.png',

    constructor : function(cfg) {
        var me = this;

        me.listeners = Object.extend(me.listenersHere, cfg.listeners);
        this.store = cfg.store;

        Ext.apply(cfg, {
            cls : 'auscope-dark-grid',
            header: false,
            hideSearch: true,
            titleField: 'name',
            emptyText: '<p class="centeredlabel">Layers that have been added from the left will be shown here.</p>',
            titleIndex: 1,
            allowReordering: true,
            tools: [{
                field: ['loading', 'active'],
                stopEvent: true,
                clickHandler: Ext.bind(me._loadingClickHandler, me),
                tipRenderer: Ext.bind(me._loadingTipRenderer, me),
                iconRenderer: Ext.bind(me._loadingRenderer, this)
            },{
                field: 'info',
                stopEvent: true,
                tipRenderer: function(value, record, tip) {
                    return 'Show layer information';
                },
                iconRenderer: function(value, record) {
                    return 'portal-core/img/information.png'
                },
                clickHandler: me._serviceInformationClickHandler
            },{
                field: 'visible',
                stopEvent: true,
                tipRenderer: function(value, record, tip) {
                    var tip = 'Toggle layer visibility ';
                    if(record.visible){
                        tip+='off';
                    }else{
                        tip+='on';
                    }
                    return tip;
                },
                iconRenderer: function(value, record) {
                    if(record.visible){
                        return me.visibleIcon;
                    }else{
                        return me.notVisibleIcon;
                    }
                },
                clickHandler: function(value, record) {
                    me._setVisibilityAction(record).execute();
                }
            },{
                field: 'remove',
                stopEvent: true,
                tipRenderer: function(value, record, tip) {
                    return 'Remove layer from map';
                },
                iconRenderer: function(value, record) {
                    return 'portal-core/img/cross.png';
                },
                clickHandler: function(value, record) {
                    ActiveLayerManager.removeLayer(record);
                    record.get('source').set('layer', null);
                }
            }],
            childPanelGenerator: function(layer) {
                //Don't show filter forms for layers that don't need them
                if (layer.get('filterForm') instanceof portal.layer.filterer.forms.EmptyFilterForm) {
                    return null;
                }

                layer.set('filterForm', cfg.layerFactory.formFactory.getFilterForm(layer).form);
                return me._getInlineLayerPanel(layer, layer.get('filterForm'));
            },
            listeners: {
                reorder: function(recordPanel, record) {
                    ActiveLayerManager.updateLayerOrder(me.map, record);
                }
            }
        });

        this.callParent(arguments);
    },

    // Column Function
    _setVisibilityAction : function(layer){
        var visibleLayerAction = new Ext.Action({
            text : 'Toggle Layer Visibility OFF',
            iconCls : 'visible_eye',
            handler : function(){
                layer.setLayerVisibility(!layer.visible);
            }
        });

        return visibleLayerAction;
    },

    _handleFilterUpdateClick: function(layer, filterForm) {
        var filterer = layer.get('filterer');
        filterer.setSpatialParam(this.map.getVisibleMapBounds(), true);
        filterForm.writeToFilterer(filterer);
    },

    /**
     * Column definition function to draw the panel when a row is clicked upon.  Here is a common one to draw the WMS/WFS filter with Opacity, drop-downs etc..
     * Override
     */
    _getInlineLayerPanel : function(layer, filterForm){
        var panel = Ext.create('Ext.panel.Panel', {
            bbar: [{
                xtype: 'button',
                text: 'Update Layer on Map',
                iconCls:   'edit',
                handler: Ext.bind(this._handleFilterUpdateClick, this, [layer, filterForm], false)
            }],
            layout: 'fit',
            items: [filterForm]
        });

        return panel
    },

    /**
     * Helper function.  Useful to define here for subclasses.
     *
     * Show a popup containing info about the services that 'power' this layer
     */
    _serviceInformationClickHandler : function(value, record) {
        Ext.create('Ext.window.Window', {
            title : 'CSW Record Information',
            items : [{
                xtype : 'cswmetadatapanel',
                width : 500,
                border : false,
                cswRecord : record.get('source')
            }]
        }).show();
    },

    /**
     * Renderer for the loading column
     */
    _loadingRenderer : function(value, layer) {
        if (value) {
            return 'portal-core/img/loading.gif';
        } else {

            if(layer.get('active')){

                var renderStatus = layer.get('renderer').renderStatus;
                var listOfStatus=renderStatus.getParameters();
                var errorCount = this._statusListErrorCount(listOfStatus);
                var sizeOfList = Ext.Object.getSize(listOfStatus);
                if(errorCount > 0 && errorCount == sizeOfList){
                    return 'portal-core/img/exclamation.png';
                }else if(errorCount > 0 && errorCount < sizeOfList){
                    return 'portal-core/img/warning.png';
                }else{
                    return 'portal-core/img/tick.png';
                }

            }else{
                return 'portal-core/img/notloading.gif';
            }
        }
    },

    _statusListErrorCount : function(listOfStatus){
        var match =["reached","error","did not complete","AJAX","Unable"];

        var erroCount = 0;

        for(key in listOfStatus){
            for(var i=0; i< match.length; i++){
                if(listOfStatus[key].indexOf(match[i]) > -1){
                    erroCount++;
                    break;
                }
            }
        }
        return erroCount;
    },


    /**
     * A renderer for generating the contents of the tooltip that shows when the
     * layer is loading
     */
    _loadingTipRenderer : function(value, layer, tip) {
        var renderer = layer.get('renderer');
        var update = function(renderStatus, keys) {
            tip.update(renderStatus.renderHtml());
        };

        //Update our tooltip as the underlying status changes
        renderer.renderStatus.on('change', update, this);
        tip.on('hide', function() {
            renderer.renderStatus.un('change', update); //ensure we remove the handler when the tip closes
        });

        return renderer.renderStatus.renderHtml();
    },

    _loadingClickHandler : function(value, layer) {
        var html = '<p>No Service recorded, Click on Add layer to map</p>';

        if(layer){
            var renderer = layer.get('renderer');
            html =  renderer.renderStatus.renderHtml();
        }
        var win = Ext.create('Ext.window.Window', {
            title: 'Service Loading Status',
            height: 200,
            width: 500,
            layout: 'fit',
            items: {  // Let's put an empty grid in just to illustrate fit layout
                xtype: 'panel',
                autoScroll : true,
                html : html
            }
        });

        win.show();
    }
});