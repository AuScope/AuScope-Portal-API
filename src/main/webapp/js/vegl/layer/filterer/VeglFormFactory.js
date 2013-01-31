/**
 * AuScope implementation of the core portal FormFactory
 */
Ext.define('vegl.layer.filterer.VeglFormFactory', {
    extend : 'portal.layer.filterer.FormFactory',

    /**
     * map : [Required] an instance of portal.map.BaseMap
     */
    constructor : function(config) {
        this.callParent(arguments);
    },

    /**
     * Given an portal.layer.Layer, work out whether there is an appropriate portal.layer.filterer.BaseFilterForm to show
     *
     * Returns a response in the form
     * {
     *    form : Ext.FormPanel - the formpanel to be displayed when this layer is selected (can be EmptyFilterForm)
     *    supportsFiltering : boolean - whether this formpanel supports the usage of the filter button
     *    layer : portal.layer.Layer that was used to generate this object
     * }
     *
     */
    getFilterForm : function(layer) {
        var baseFilterForm = null;
        var baseFilterFormCfg = {
            layer : layer,
            map : this.map
        };

        //A number of known layer's have specific filter forms
        if (layer.get('sourceType') === portal.layer.Layer.KNOWN_LAYER) {
            switch (layer.get('source').get('id')) {
                case 'ga-onshore-bouguer-geodetic':
                case 'ga-grav-anom-geo':
                case 'ga-magmap-v5-2010':
                case 'ga-radmap-totaldose':
                case 'gswa-magmerge':
                case 'gswa-umerge':
                case 'gswa-thmerge':
                case 'gswa-tcmerge':
                case 'gswa-kmerge':
                    baseFilterForm = Ext.create('vegl.layer.filterer.forms.VeglCoverageFilterForm', baseFilterFormCfg);
                    return this._generateResult(baseFilterForm, true);
                case 'geophysics-datasets':
                    baseFilterForm = Ext.create('vegl.layer.filterer.forms.ProjectFilterForm', baseFilterFormCfg);
                    return this._generateResult(baseFilterForm, true);
            }
        }

        //otherwise let's see if we can guess an appropriate filter based on layer renderer
        if (layer.get('renderer') instanceof portal.layer.renderer.wms.LayerRenderer) {
            baseFilterForm = Ext.create('portal.layer.filterer.forms.WMSLayerFilterForm', baseFilterFormCfg);
            //VT: Filtering is support but for WMS, we want the image to be displayed immediately after it has been added and
            //the opacity can be adjusted from there on
            return this._generateResult(baseFilterForm, false);
        }

        //And otherwise we just show the empty filter form
        return this._generateResult(Ext.create('portal.layer.filterer.forms.EmptyFilterForm', baseFilterFormCfg), false);
    }
});