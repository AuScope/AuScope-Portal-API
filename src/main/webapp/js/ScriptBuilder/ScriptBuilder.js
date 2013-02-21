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
            itemId : 'sb-templates-panel',
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
                itemId : 'sb-script-panel',
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

        //Create the template which will load our script
        var template = Ext.create(templateClass, {
            name : name,
            description : description,
            wizardState : me.wizardState,
            parameters : Ext.apply({}, me.templateVariables)
        });
        template.requestScript(function(status, script) {
            //Once we have the script text - ask the user what they want to do with it
            if (status === ScriptBuilder.templates.BaseTemplate.TEMPLATE_RESULT_SUCCESS) {
                //If there's nothing in the window - just put text in there
                if (me.getScript().length === 0) {
                    me.replaceScript(script);
                } else {
                    Ext.create('ScriptBuilder.InsertionPromptWindow', {
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
            } else if (status === ScriptBuilder.templates.BaseTemplate.TEMPLATE_RESULT_ERROR) {
                Ext.Msg.alert('Internal Error', 'An error has occured while loading the template from the server. Please try again in a few minutes or consider refreshing this page.');
            }
        });
    },

    /**
     * Builds components panel with selected toolbox
     */
    buildComponentsPanel : function(selectedToolbox) {
        var comps = ScriptBuilder.Components.getComponents(selectedToolbox);
        this.componentsPanel.setRootNode(comps);
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