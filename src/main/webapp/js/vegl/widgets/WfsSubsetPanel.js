/**
 * A panel for allowing the user to confirm a sub selection of an Web Feature Service dataset
 *
 */
Ext.define('vegl.widgets.WfsSubsetPanel', {
    extend : 'Ext.form.Panel',

    alias : 'widget.wfssubsetpanel',

    /**
     * Accepts the following:
     * {
     *  region : portal.util.BBox - The selected area (defaults to 0,0,0,0)
     *  localPath : String - path to where the subset will be downloaded (on the VM) (defaults to '/tmp/subset-request')
     *  name : String - a descriptive name for this subset (defaults to 'ERDDAP Subset Request')
     *  description : String - a longer description for this subset (defaults to ''),
     *  featureType : String - The name of the feature (WFS name) being subsetted
     *  serviceUrl : String - the URL of the WFS which can be queried for more specific info on the feature type
     *  outputFormat : String - The default data type selection
     *  srsName : String - The output spatial reference system to request
     * }
     */
    constructor : function(config) {
        var outputFormat = config.outputFormat ? config.outputFormat : undefined;
        var region = config.region ? config.region : Ext.create('portal.util.BBox', {northBoundLatitude : 0,
            southBoundLatitude : 0,
            eastBoundLongitude : 0,
            westBoundLongitude : 0});
        var name = config.name ? config.name : 'WFS Subset Request';
        var description = config.description ? config.description : '';
        var localPath = config.localPath ? config.localPath : '/tmp/subset-request';

        this.featureType = config.featureType;
        this.serviceUrl = config.serviceUrl;

        // subset file type selection values
        var formatStore = Ext.create('Ext.data.Store', {
            fields : [{name : 'format'}],
            proxy : {
                type : 'ajax',
                url : 'getFeatureRequestOutputFormats.do',
                extraParams : {
                    serviceUrl : config.serviceUrl
                },
                reader : {
                    type : 'json',
                    root : 'data'
                }
            }
        });

        Ext.apply(config, {
            items : [{
                xtype : 'combo',
                anchor : '100%',
                fieldLabel : 'Data Type',
                mode : 'local',
                store : formatStore,
                triggerAction : 'all',
                typeAhead : false,
                valueField : 'format',
                displayField : 'format',
                name : 'outputFormat',
                value : outputFormat,
                plugins: [{
                    ptype: 'fieldhelptext',
                    text: 'The data format to request from the remote service.'
                }]
            },{
                xtype: 'fieldcontainer',
                fieldLabel: 'Region',
                msgTarget: 'under',
                anchor: '100%',
                layout: {
                    type: 'hbox',
                    defaultMargins: {top: 0, right: 5, bottom: 0, left: 0}
                },
                defaults: {
                    hideLabel: true,
                    decimalPrecision : 8
                },
                items: [
                    {xtype: 'displayfield', value: '('},
                    {xtype: 'numberfield',  name: 'northBoundLatitude', width: 80, value: region.northBoundLatitude, listeners : {change : Ext.bind(this.updateSelectionSize, this)}},
                    {xtype: 'displayfield', value: 'North ) ('},
                    {xtype: 'numberfield',  name: 'westBoundLongitude', width: 80, value: region.westBoundLongitude, listeners : {change : Ext.bind(this.updateSelectionSize, this)}},
                    {xtype: 'displayfield', value: 'West ) ('},
                    {xtype: 'numberfield',  name: 'southBoundLatitude', width: 80, value: region.southBoundLatitude, listeners : {change : Ext.bind(this.updateSelectionSize, this)}},
                    {xtype: 'displayfield', value: 'South ) ('},
                    {xtype: 'numberfield',  name: 'eastBoundLongitude', width: 80, value : region.eastBoundLongitude, listeners : {change : Ext.bind(this.updateSelectionSize, this)}},
                    {xtype: 'displayfield', value: 'East )'},
                    {xtype: 'hiddenfield', name : 'crs', value: region.crs}
                ],
                plugins: [{
                    ptype: 'fieldhelptext',
                    text: 'The spatial region subset to request from the data service. The values represent a WGS:84 bounding box.'
                }]
            },{
                xtype : 'displayfield',
                fieldLabel : 'Selection Size',
                itemId : 'selection-size',
                anchor : '100%',
                value : 'Loading...'
            },{
                xtype : 'hiddenfield',
                name : 'featureType',
                value : this.featureType
            },,{
                xtype : 'hiddenfield',
                name : 'serviceUrl',
                value : this.serviceUrl
            },{
                xtype : 'textfield',
                fieldLabel : 'Response SRS',
                value : config.srsName,
                anchor : '100%',
                name : 'srsName',
                allowBlank : true,
                plugins: [{
                    ptype: 'fieldhelptext',
                    text: 'The response features can be reprojected to a preferred coordinate reference system. The default response value is EPSG:4326.'
                }]
            },{
                xtype : 'textfield',
                fieldLabel : 'Location',
                value : localPath,
                anchor : '100%',
                name : 'localPath',
                plugins: [{
                    ptype: 'fieldhelptext',
                    text: 'A file path where requested data will be downloaded to. It will be accessible from within your job script.'
                }]
            },{
                xtype : 'textfield',
                fieldLabel : 'Name',
                value : name,
                anchor : '100%',
                name : 'name',
                plugins: [{
                    ptype: 'fieldhelptext',
                    text: 'A short name to describe this download request.'
                }]
            },{
                xtype : 'textarea',
                fieldLabel : 'Description',
                value : description,
                anchor : '100%',
                name : 'description',
                plugins: [{
                    ptype: 'fieldhelptext',
                    text: 'A longer description to outline the purpose of this download request.'
                }]
            }]
        });

        this.callParent(arguments);

        this.on('render', this.updateSelectionSize, this);
    },

    updateSelectionSize : function() {
        var ssField = this.getComponent('selection-size');
        var values = this.getForm().getValues();

        ssField.setValue('Loading...');

        vegl.util.WFSUtil.estimateFeatureCount(values, this.serviceUrl, this.featureType, ssField, function(success, msg, response, ssField) {
            if (!success) {
                ssField.setValue(msg);
                return;
            }

            var total = Ext.util.Format.number(response.total, '0,000');
            ssField.setValue(Ext.util.Format.format('Approximately <b>{0}</b> features have been selected.', total));
        });
    }
});
