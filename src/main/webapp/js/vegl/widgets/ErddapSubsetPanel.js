/**
 * A panel for allowing the user to confirm a sub selection of an ERDDAP dataset
 *
 */
Ext.define('vegl.widgets.ErddapSubsetPanel', {
    extend : 'Ext.form.Panel',

    alias : 'widget.erddapsubsetpanel',

    /**
     * Accepts the following:
     * {
     *  region : portal.util.BBox - The selected area (defaults to 0,0,0,0)
     *  localPath : String - path to where the subset will be downloaded (on the VM) (defaults to '/tmp/subset-request')
     *  name : String - a descriptive name for this subset (defaults to 'ERDDAP Subset Request')
     *  description : String - a longer description for this subset (defaults to '')
     * }
     */
    constructor : function(config) {
        var dataType = 'nc';
        var region = config.region ? config.region : Ext.create('portal.util.BBox', {northBoundLatitude : 0,
            southBoundLatitude : 0,
            eastBoundLongitude : 0,
            westBoundLongitude : 0});
        var name = config.name ? config.name : 'ERDDAP Subset Request';
        var description = config.description ? config.description : '';
        var localPath = config.localPath ? config.localPath : '/tmp/subset-request';

        // subset file type selection values
        var fileTypeStore = Ext.create('Ext.data.Store', {
            fields : [{name : 'urn'}, {name : 'label'}],
            data : [{label : 'CSV', urn : 'csv'},
                    {label : 'GeoTIFF', urn : 'geotif'},
                    {label : 'NetCDF', urn : 'nc'}]
        });


        Ext.apply(config, {
            items : [{
                xtype : 'combo',
                anchor : '100%',
                editable : false,
                forceSelection : true,
                fieldLabel : 'Data Type',
                mode : 'local',
                store : fileTypeStore,
                triggerAction : 'all',
                typeAhead : false,
                displayField : 'label',
                valueField : 'urn',
                value : dataType,
                name : 'format',
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
                    {xtype: 'numberfield',  name: 'northBoundLatitude', width: 80, value: region.northBoundLatitude},
                    {xtype: 'displayfield', value: 'North ) ('},
                    {xtype: 'numberfield',  name: 'westBoundLongitude', width: 80, value: region.westBoundLongitude},
                    {xtype: 'displayfield', value: 'West ) ('},
                    {xtype: 'numberfield',  name: 'southBoundLatitude', width: 80, value: region.southBoundLatitude},
                    {xtype: 'displayfield', value: 'South ) ('},
                    {xtype: 'numberfield',  name: 'eastBoundLongitude', width: 80, value : region.eastBoundLongitude},
                    {xtype: 'displayfield', value: 'East )'}
                ],
                plugins: [{
                    ptype: 'fieldhelptext',
                    text: 'The spatial region subset to request from the data service. The values represent a WGS:84 bounding box.'
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
    }
});
