/**
 * A Ext.form.field.ComboBox specialisation for choosing from a selection of vegl.models.MachineImage
 */
Ext.define('vegl.widgets.MachineImageCombo', {
    extend : 'Ext.form.field.ComboBox',
    alias : 'widget.machineimagecombo',

    /**
     * Accepts an Ext.field.Combo. It is expected that the internal store will be populated
     * using vegl.models.MachineImage objects.
     */
    constructor : function(config) {
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
            displayField: 'name',
            valueField : 'imageId',
            listConfig: listCfg
        });

        this.callParent(arguments);
    }
});