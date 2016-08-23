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
        	//html: '<object type="text/html" data="http://proms-dev.vhirl.net/id/report/?uri=http%3A//localhost/report/9d5080b5-418b-4293-a5f3-0cf992f6be9b" width="100%" height="100%"></object>',
            html: '<iframe style="width:100%;height:100%;border:0px;" src=""></iframe>',
        });
        
        window.addEventListener('message', function(event) {
        	console.log("Message received");
        	if (~event.origin.indexOf('http://localhost')) {
        		console.log("Message data: " + message.data);
        		var page = this.getEl().down('iframe');
                page.dom.setAttribute('src', event.data);
            } else {
            	console.log("Message from unknown domain");
                return;
            }
        });

        this.callParent(arguments);
    },

    /**
     * Reloads this store with all the jobs for the specified series
     */
    preview : function(job, fileName, size, hash) {
    	
    	this.job = job;
        this.fileName = fileName;
        this.size = size;
        this.hash = hash;
    	
    	/*
    	// XXX Need fileName? Get from job object
    	var page = this.getEl().down('object');
        page.dom.setAttribute('data', promsReportUrl);
        */
    	if(job.get('promsReportUrl') != null && job.get('promsReportUrl') != "") {
    		console.log("Found PROMS report URL: " + job.get('promsReportUrl'));
    		var page = this.getEl().down('iframe');
    		
    		// XXX
            page.dom.setAttribute('src', job.get('promsReportUrl'));
    		//page.el.dom.contentWindow.postMessage(job.get('promsReportUrl'), '*');
    		
    	} else {
    		console.log("No PROMS Report URL, loading file");
    		
    		var iframe = this.getEl().down('iframe');
            iframe.dom.setAttribute('src', '');
    		
    		if (this.currentRequest != null) {
                Ext.Ajax.abort(this.currentRequest);
                this.currentRequest = null;
            }

            var loadMaskElement = null;
            if (this.rendered) {
                loadMaskElement = this.getEl();
                loadMaskElement.mask('Loading preview...');
            }
    		
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
                    this.writeText(previewText);
                }
            });
    	}

        
    },
    
    
    updateExternalPage: function(pageUrl) {
    	var page = this.getEl().down('iframe');
        page.dom.setAttribute('src', pageUrl);
    },
    
    
    writeText: function(text) {
        //var iframe = this.getEl().down('iframe');
        
        //iframe.dom.setAttribute('src', '');
        
        var doc = iframe.dom.contentWindow.document;
        doc.open();
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
        
        this.writeText('');
        
        this.doLayout();
    }
});