/**
 * A panel for allowing the user to confirm a sub selection of an ERDDAP dataset
 *
 */
Ext.define('vegl.widgets.FileDownloadPanel', {
    extend : 'Ext.form.Panel',

    alias : 'widget.filedownloadpanel',

    /**
     * Accepts the following:
     * {
     *  url : String - The remote URL to download
     *  region : portal.util.BBox - The selected area (defaults to 0,0,0,0)
     *  localPath : String - path to where the subset will be downloaded (on the VM) (defaults to '/tmp/subset-request')
     *  name : String - a descriptive name for this subset (defaults to 'ERDDAP Subset Request')
     *  description : String - a longer description for this subset (defaults to '')
     * }
     */
    constructor : function(config) {
        var url = config.url ? config.url : '';
        var region = config.region ? config.region : Ext.create('portal.util.BBox', {northBoundLatitude : 0,
            southBoundLatitude : 0,
            eastBoundLongitude : 0,
            westBoundLongitude : 0});
        var name = config.name ? config.name : 'ERDDAP Subset Request';
        var description = config.description ? config.description : '';
        var localPath = config.localPath ? config.localPath : '/tmp/subset-request';


        Ext.apply(config, {
            items : [{
                xtype : 'textfield',
                fieldLabel : 'URL',
                value : url,
                anchor : '100%',
                name : 'url',
                plugins: [{
                    ptype: 'fieldhelptext',
                    text: 'The location of the file or data you wish to download.'
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
            },
            {xtype: 'hiddenfield',  name: 'northBoundLatitude', value: region.northBoundLatitude},
            {xtype: 'hiddenfield',  name: 'westBoundLongitude', value: region.westBoundLongitude},
            {xtype: 'hiddenfield',  name: 'southBoundLatitude', value: region.southBoundLatitude},
            {xtype: 'hiddenfield',  name: 'eastBoundLongitude', value : region.eastBoundLongitude}]
        });

        this.callParent(arguments);
    }
});
