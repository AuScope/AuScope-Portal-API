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
    preview : function(job, fileName, size, hash) {
        var img = this.getEl().down('img');
        var mask = new Ext.LoadMask({
            msg: 'Loading Image...',
            target: this.ownerCt
        });
        mask.show();

        var hideMask = function() {
            mask.hide();
            img.un('load', hideMask);
            img.un('error', hideMask);
        };
        img.on('load', hideMask);
        img.on('error', hideMask);

        img.dom.setAttribute('src', 'secure/getImagePreview.do?jobId=' + job.get('id') + '&file=' + fileName + '&_dc=' + Math.random());

        this.job = job;
        this.fileName = fileName;
        this.size = size;
        this.hash = hash;
    },

    /**
     * Removes logs from this panel. Optionally adds a replacement tab indicating this panel is empty
     */
    clearPreview : function(addEmptyTab, emptyTabMsg) {
        this.job = null;
        this.fileName = null;
        this.size = null;
        this.hash = null;
        this.doLayout();
    }
});