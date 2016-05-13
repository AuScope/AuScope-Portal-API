/**
 * A Ext.panel.Panel specialisation for rendering a single instance of an Image
 *
 */
Ext.define('vegl.preview.ImagePreview', {
    extend : 'Ext.panel.Panel',
    alias : 'widget.imagepreview',

    mixins: {
        preview: 'vegl.preview.FilePreviewMixin'
    },

    constructor : function(config) {
        Ext.apply(config, {
            autoScroll: true,
            html: '<img src=""></img>',
        });

        this.callParent(arguments);
    },

    /**
     * Reloads this store with all the jobs for the specified series
     */
    preview : function(job, fileName, size) {
        var img = this.getEl().down('img');
        img.dom.setAttribute('src', 'secure/getImagePreview.do?jobId=' + job.get('id') + '&file=' + fileName);
    },

    /**
     * Removes logs from this panel. Optionally adds a replacement tab indicating this panel is empty
     */
    clearPreview : function(addEmptyTab, emptyTabMsg) {
        this.currentJob = null;
        this.currentFile = null;
        this.doLayout();
    }
});