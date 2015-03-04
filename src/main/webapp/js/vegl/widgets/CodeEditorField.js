/**
 * A panel for rendering a set of CSWRecords that have been (sub)selected by a bounding box.
 */
Ext.define('vegl.widgets.CodeEditorField', {
    extend : 'Ext.form.field.TextArea',

    alias : 'widget.codeeditor',

    editor : null,
    /**
     * Accepts the following:
     * {
     *  region : portal.util.BBox - The selected area (defaults to 0,0,0,0)
     *  cswRecords : portal.csw.CSWRecord[] - The records to display info for.
     * }
     */
    constructor : function(config) {
    

        //Build our configuration object
        Ext.apply(config, {
            listeners: {
                render: function(obj,eopts){
                    var element = document.getElementById(obj.getInputId());                    
                    this.editor = CodeMirror.fromTextArea(element,{
                        lineNumbers: true,
                        mode: config.mode       
                    });
                }
            }
        });

        this.callParent(arguments);

      
    }

    
});

