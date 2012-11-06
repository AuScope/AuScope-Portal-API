/**
 * A Ext.form.field.ComboBox specialisation for choosing from a selection of vegl.models.MachineImage
 */
Ext.define('vegl.widgets.MachineImageCombo', {
    extend : 'Ext.form.field.ComboBox',
    alias : 'widget.machineimagecombo',

    /**
     * Accepts the config for a Ext.grid.Panel along with the following additions:
     *
     * hideRegisterButton : Boolean - if true the 'register to geonetwork' button will be hidden
     */
    constructor : function(config) {
        var ds = Ext.create('Ext.data.Store', {
            model: 'vegl.models.MachineImage',
            proxy: {
                type: 'ajax',
                url: 'getVmImages.do',
                reader: {
                   type: 'json',
                   root : 'data'
                }
            },
            autoLoad : true
        });

        var listCfg = config.listConfig ? config.listConfig : {};
        listCfg.getInnerTpl = function() {
            // Custom rendering template for each item
            return '<h3>{name}</h3>' +
                   '{description}<br/>' +
                   '<ul>' +
                       '<tpl for="keywords"><li style="list-style:disc inside none;">{.}</li></tpl>' +
                   '</ul>';
        };

        Ext.apply(config, {
            store: ds,
            displayField: 'name',
            valueField : 'imageId',
            listConfig: listCfg
        });

        this.callParent(arguments);
    }
});