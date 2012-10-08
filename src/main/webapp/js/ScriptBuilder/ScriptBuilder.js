/**
 * A component for tying all of the script builder components together
 *
 */
Ext.define('ScriptBuilder.ScriptBuilder', {
    extend : 'Ext.panel.Panel',

    textEditMode : false,
    wizardState : null,
    templateVariables : null,

    sourceText : null,
    componentsPanel : null,

    constructor: function(config) {
        var scriptBuilderFrm = this;
        this.wizardState = config.wizardState;
        this.templateVariables = config.templateVariables;

        this.sourceText = Ext.create('Ext.ux.form.field.CodeMirror', {
            title : 'Script Source',
            region : 'center',
            cls : 'prettyprint',
            mode : 'text/x-python',
            showAutoIndent : false,
            showLineNumbers : true,
            listModes : [{text: "Python", mime: "text/x-python"},
                         {text: "Plain text", mime: "text/plain"}],
            modes: [{
                mime:           ['text/plain'],
                dependencies:   []
            },{
                mime:           ['text/x-python', 'python'],
                dependencies:   ['CodeMirror-2.33/lib/python/python.js']
            }]
        });

        this.componentsPanel = Ext.create('ScriptBuilder.ComponentTreePanel', {
            region : 'west',
            title : 'Available Templates',
            width : 250,
            listeners : {
                addcomponent : Ext.bind(this.onAddComponent, this)
            }
        });

        Ext.apply(config, {
            layout : 'border',
            border : false,
            items: [{
                xtype : 'panel',
                border : false,
                layout : 'fit',
                region : 'center',
                title : 'Script Source',
                items : [this.sourceText]
            }, this.componentsPanel]
        });

        // Finally, build the main layout once all the pieces are ready.
        this.callParent(arguments);
    },

    onAddComponent : function(panel, templateClass, name, description) {
        var me = this;
        var loadMask = new Ext.LoadMask(me.getEl(), {
            msg : 'Loading script...',
            removeMask : true
        });
        loadMask.show();

        //Create the template which will load our script
        var template = Ext.create(templateClass, {
            name : name,
            description : description,
            parameters : Ext.apply({}, me.templateVariables)
        });
        template.requestScript(function(success, script) {
            loadMask.hide();

            //Once we have the script text - ask the user what they want to do with it
            if (success) {
                //If there's nothing in the window - just put text in there
                if (me.getScript().length === 0) {
                    me.replaceScript(script);
                } else {
                    var popup = Ext.create('ScriptBuilder.InsertionPromptWindow', {
                        script : script,
                        listeners : {
                            select : function(popup, selection) {
                                switch(selection) {
                                case ScriptBuilder.InsertionPromptWindow.OPTION_REPLACE:
                                    me.replaceScript(script);
                                    break;
                                case ScriptBuilder.InsertionPromptWindow.OPTION_INSERT:
                                    me.insertScript(script);
                                    break;
                                }
                            }
                        }
                    }).show();
                }
            } else {
                Ext.Msg.alert('Internal Error', 'An error has occured while loading the template from the server. Please try again in a few minutes or consider refreshing this page.');
            }
        });
    },

    /**
     * Inserts the specified script at the current caret location
     */
    insertScript : function(script) {
        var from = null;
        var to = null;

        if (this.sourceText.editor.somethingSelected()) {
            from = this.sourceText.editor.getCursor(true);
            to = this.sourceText.editor.getCursor(false);
        } else {
            from = this.sourceText.editor.getCursor();
        }

        this.sourceText.editor.replaceRange(script, from, to);
    },

    /**
     * Replaces the current tab with the specified script
     */
    replaceScript : function(script) {
        this.sourceText.setValue(script);
    },

    getScript : function() {
        return this.sourceText.getValue();
    }
});