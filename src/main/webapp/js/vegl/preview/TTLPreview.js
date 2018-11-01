/**
 * A Ext.panel.Panel specialisation for rendering a single instance of a TTL file
 *
 */
Ext.define('vegl.preview.TTLPreview', {
    extend : 'Ext.panel.Panel',
    alias : 'widget.ttlpreview',

    mixins: {
        preview: 'vegl.preview.FilePreviewMixin'
    },

    constructor : function(config) {
        Ext.apply(config, {
            html: '<iframe style="width:100%;height:100%;border:0px;"></iframe>',
        });

        this.callParent(arguments);
    },

    /**
     * Reloads this store with all the jobs for the specified series
     */
    preview : function(job, fileName, size, hash) {
    	var promsReportUrl = job.get('promsReportUrl');
        if (this.currentRequest != null) {
            Ext.Ajax.abort(this.currentRequest);
            this.currentRequest = null;
        }

        var loadMaskElement = null;
        if (this.rendered) {
            loadMaskElement = this.getEl();
            loadMaskElement.mask('Loading preview...');
        }

        this.clearPreview();
        this.job = job;
        this.fileName = fileName;
        this.size = size;
        this.hash = hash;

        this.currentRequest = Ext.Ajax.request({
            url : 'secure/getPlaintextPreview.do',
            params : {
                jobId : job.get('id'),
                file: this.fileName,
                maxSize: 20 * 1024 //20 KB
            },
            scope : this,
            callback : function(options, success, response) {
                if (loadMaskElement) {
                    loadMaskElement.unmask();
                }

                if (!success) {
                    this.clearPreview(true, 'Error communicating with the server.');
                    return;
                }

                var responseObj = Ext.JSON.decode(response.responseText);
                if (!responseObj || !responseObj.success) {
                    this.clearPreview(true, 'No file data available');
                    return;
                }

                var previewText = responseObj.data;
                this.writeText(previewText, promsReportUrl);
            }
        });
    },

    writeText: function(text, reportUrl) {
    	text = text.replace(/</g, "&lt;");
    	text = text.replace(/>/g, "&gt;");
        var iframe = this.getEl().down('iframe');
        var doc = iframe.dom.contentWindow.document;
        doc.open();
        if(reportUrl!=null && reportUrl!="") {
        	doc.write('<a href="' + reportUrl + '" target="_blank">View PROMS Report</a><br><br>');
        } else {
        	doc.write('No associated PROMS Report could be located.<br><br>');
        }
        doc.write(text);
        doc.close();
        doc.body.setAttribute('style', 'white-space:pre;font-family:monospace;');
    },

    /**
     * Removes logs from this panel. Optionally adds a replacement tab indicating this panel is empty
     */
    clearPreview : function(addEmptyTab, emptyTabMsg) {
        this.job = null;
        this.fileName = null;
        this.size = null;
        this.hash = null;
        this.writeText('', null);
    }
});