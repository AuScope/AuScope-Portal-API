/**
 * Mixin for rendering a specific file previewer
 */
Ext.define('vegl.preview.FilePreviewMixin', {
    extend: 'Ext.Mixin',

    /**
     * function(job, fileName)
     * job - ANVGLJob - Job to preview
     * fileName - String - name of the job file to preview
     * size - Number - size of the file in bytes
     *
     * Setup this preview page to preview the specified file for the specified job
     *
     * returns nothing
     */
    preview : portal.util.UnimplementedFunction
});
