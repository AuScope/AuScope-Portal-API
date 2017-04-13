/**
 * A Ext.panel.Panel extension for displaying a set of registries
 *
 */
Ext.define('vegl.widgets.search.RegistryFacet', {
    extend : 'Ext.panel.Panel',
    alias: 'widget.registryfacet',

    map: null,
    registries: null,
    selectedValues: null,

    constructor : function(config) {
        this.map = config.map;
        this.registries = [];
        this.selectedValues = [];

        Ext.apply(config, {
            title: 'Available Registries',
            items: [{
                xtype: 'checkboxgroup',
                columns: 1,
                vertical: true,
                items: [],
                listeners: {
                    change: this._handleChange,
                    scope: this
                }
            }]
        });

        this.callParent(arguments);
    },

    initComponent: function() {
        this._loadRegistries();
        this.callParent(arguments);
    },

    _handleChange: function(cbGroup, newValue, oldValue) {
        this.selectedValues = Object.values(newValue);
        this.fireEvent('registrychange', this, this.selectedValues);
    },

    /**
     * Populates the internal store of registry config objects. Updates CheckboxGroup
     */
    _loadRegistries: function() {
        this.fireEvent('loadstart', this);
        portal.util.Ajax.request({
            url: 'getCSWServices.do',
            scope: this,
            success: function(data, message, debugInfo) {
                var configs = [];
                this.registries = [];
                var newItems = [];
                var ids = {};
                for (var i = 0; i < data.length; i++) {
                    this.registries.push(data[i]);
                    this.selectedValues.push(data[i].id);
                    newItems.push({
                        boxLabel: data[i].title,
                        inputValue: data[i].id,
                        checked: true
                    });
                }

                var cbGroup = this.down('checkboxgroup');
                cbGroup.removeAll();
                cbGroup.add(newItems);

                this.fireEvent('load', this, true, this.registries);
                this.fireEvent('registrychange', this, this.selectedValues);
            },
            failure: function(message, debugInfo) {
                this.fireEvent('load', this, false);
            }
        });
    },
});