/**
 * Class for transforming individual 'features' (WFS or otherwise) of a KnownLayer into
 * components representing GUI widgets for demonstrating ancillary information about
 * that 'feature'
 *
 * For example, the process of identifying the 'NVCL KnownLayer' and creating a 'Observations Download'
 * window is handled by this class (supported by underlying factories).
 */
Ext.define('vegl.layer.querier.wfs.VeglKnownLayerParser', {
    extend: 'portal.layer.querier.wfs.KnownLayerParser',

    /**
     * Builds a new KnownLayerParser from a list of factories. Factories in factoryList will be tested before
     * the items in factoryNames
     *
     * {
     *  factoryNames : String[] - an array of class names which will be instantiated as portal.layer.querier.wfs.factories.BaseFactory objects
     *  factoryList : portal.layer.querier.wfs.factories.BaseFactory[] - an array of already instantiated factory objects
     * }
     */
    constructor : function(config) {
        Ext.apply(config, {
            factoryNames : [
            ]
        });
        this.callParent(arguments);
    }
});