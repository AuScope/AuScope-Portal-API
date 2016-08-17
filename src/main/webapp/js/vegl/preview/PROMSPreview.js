/**
 * A Ext.panel.Panel specialisation for rendering a single instance of a PROMS webpage link
 *
 */
Ext.define('vegl.preview.PROMSPreview', {
    extend : 'Ext.panel.Panel',
    alias : 'widget.promspreview',

    mixins: {
        preview: 'vegl.preview.FilePreviewMixin'
    },

    constructor : function(config) {
        Ext.apply(config, {
        	autoScroll: true,
        	html: '<object type="text/html" data="http://proms-dev.vhirl.net/id/report/?uri=http%3A//localhost/report/9d5080b5-418b-4293-a5f3-0cf992f6be9b" width="100%" height="100%"></object>',
            //html: '<iframe style="width:100%;height:100%;border:0px;"></iframe>',
        });

        this.callParent(arguments);
    },

    /**
     * Reloads this store with all the jobs for the specified series
     */
    preview : function(job, fileName, size, hash) {
    	/*
    	// XXX Need fileName? Get from job object
    	var page = this.getEl().down('object');
        page.dom.setAttribute('data', promsReportUrl);
        */

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