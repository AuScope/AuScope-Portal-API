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
                        scrollbarStyle: 'null',
                        mode: config.mode
                    });

                    if (config.value) {
                        obj.setValue(config.value);
                    }

                    if (!Ext.isEmpty(config.readOnly)) {
                        this.setReadOnly(config.readOnly);
                    }
                },
                resize: function() {
                    this.editor.refresh();
                }
            }
        });

        this.callParent(arguments);
    },

    setReadOnly: function(readOnly) {
        if (this.editor) {
            this.editor.setOption('readOnly', readOnly);
        }
    },

    getReadOnly: function(readOnly) {
        if (this.editor) {
            return this.editor.getOption('readOnly');
        } else {
            return null;
        }
    },

    setValue : function(script){
        if(this.editor){
            this.editor.setValue(script);
        }
    },

    getValue : function(){
        if(this.editor){
            return this.editor.getValue();
        }else{
            return null;
        }

    },

    somethingSelected : function(){
        return this.editor.somethingSelected();
    },

    getCursor : function(start){
        return this.editor.getCursor(start);
    },


    replaceRange : function(replacement,from,to,origin){
        return this.editor.replaceRange(replacement,from,to,origin);
    }


});

