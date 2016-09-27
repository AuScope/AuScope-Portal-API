/**
 * Panel extension for rendering a set of BaseFilePreview implementations
 */
Ext.define('vegl.preview.FilePreviewPanel', {
    extend: 'Ext.panel.Panel',

    alias: 'widget.filepreviewpanel',

    /**
     * Adds the following config to Ext.grid.Panel
     * {
     *
     * }
     *
     * Adds the following events
     * {
     *
     * }
     */
    constructor : function(config) {

        var previewers = [
            {
                border: false,
                itemId: 'empty',
                html: '<div style="display:table;width:100%;height:50%;"><div style="display:table-cell;vertical-align:middle;"><p class="centeredlabel">Select a file to the left to preview it here.</p></div></div>'
            },
            Ext.create('vegl.preview.DataServicePreview', {itemId: 'dataservice'}),
            Ext.create('vegl.preview.LogPreview', {itemId: 'log'}),
            Ext.create('vegl.preview.PlainTextPreview', {itemId: 'plaintext'}),
            Ext.create('vegl.preview.ImagePreview', {itemId: 'image'}),
            Ext.create('vegl.preview.TTLPreview', {itemId: 'ttl'})
        ];


        Ext.apply(config, {
            layout: 'card',
            items: previewers,
            dockedItems: [{
                xtype: 'toolbar',
                dock: 'bottom',
                items: [{
                    xtype: 'tbfill'
                },{
                    xtype: 'button',
                    text: 'Refresh',
                    iconCls: 'refresh-icon',
                    scope: this,
                    handler: function() {
                        var preview = this.getLayout().getActiveItem();
                        if (preview.handleRefresh) {
                            preview.handleRefresh();
                        }
                    }
                }]
            }]
        });

        this.callParent(arguments);
    },

    /**
     * Previews a file for a job
     * @param job EAVLJob owning fileName
     * @param fileName The name of the file to preview
     * @param type String Which file previewer should be used?
     */
    preview : function(job, fileName, size, hash, type) {
        var l = this.getLayout();

        if (l.getActiveItem().getItemId() === type || l.setActiveItem(type)) {
            var previewer = l.getActiveItem();
            previewer.preview(job, fileName, size, hash);
        }
    },

    /**
     * Determines whether the current preview's underlying data has changed and a refresh is required
     *
     * @param callback function(refreshRequired, newHash) - will be passed true if the underlying data has changed. False otherwise
     */
    isRefreshRequired : function(callback) {
        var l = this.getLayout();

        if (l.getActiveItem().getItemId() === 'empty') {
            callback(false);
            return;
        }

        var previewer = l.getActiveItem();
        previewer.isRefreshRequired(callback);
    },

    /**
     * Refreshes the current preview panel. Has no effect if nothing is showing
     *
     * @param newHash The new hash value returned from the call to isRefreshRequired
     */
    refresh: function(newHash) {
        var l = this.getLayout();
        if (l.getActiveItem().getItemId() === 'empty') {
            return;
        }

        var previewer = l.getActiveItem();
        previewer.hash = newHash;
        previewer.handleRefresh();
    },


    /**
     * Clears the current file preview (if any)
     */
    clearPreview : function() {
        this.getLayout().setActiveItem('empty');
    }
});