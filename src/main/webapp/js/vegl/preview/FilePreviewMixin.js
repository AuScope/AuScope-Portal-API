/**
 * Mixin for rendering a specific file previewer
 */
Ext.define('vegl.preview.FilePreviewMixin', {
    extend: 'Ext.Mixin',

    /**
     * function(job, fileName, size, hash)
     * job - ANVGLJob - Job to preview
     * fileName - String - name of the job file to preview
     * size - Number - size of the file in bytes
     * hash - String - the hash of the file
     *
     * Setup this preview page to preview the specified file for the specified job
     *
     * returns nothing
     */
    preview : portal.util.UnimplementedFunction,

    /**
     * Used for checking if a call to handleRefresh will generate changed content.
     *
     * Default implementation is to always return true
     *
     * function(callback)
     * callback - function(refreshRequired, newFileHash) - called with a boolean indicating
     *            whether a refresh is required (and what the new file hash is)
     */
    isRefreshRequired: function(callback) {
        if (!this.job || !this.fileName) {
            callback(false);
            return;
        }

        Ext.Ajax.request({
            url: 'secure/getCloudFileMetadata.do',
            params: {
                jobId: this.job.get('id'),
                fileName: this.fileName
            },
            scope: this,
            callback: function(options, success, response) {
                if (!success) {
                    callback(false);
                    return;
                }

                var responseObj = Ext.JSON.decode(response.responseText);
                if (!responseObj.success) {
                    callback(false);
                    return;
                }

                var newFileHash = responseObj.data[0].fileHash;
                var refreshRequired = newFileHash !== this.hash;
                callback(refreshRequired, newFileHash);
            }
        });
    },

    /**
     * Can be overridden
     *
     * Forces a complete refresh of the current preview.
     */
    handleRefresh : function() {
        this.preview(this.job, this.fileName, this.size, this.hash);
    },

});
