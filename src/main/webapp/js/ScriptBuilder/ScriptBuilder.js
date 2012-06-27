/**
 * A component for tying all of the script builder components together
 *
 */
Ext.define('ScriptBuilder.ScriptBuilder', {
    extend : 'Ext.panel.Panel',

    textEditMode : false,
    wizardState : null,

    sourceText : null,
    activeComponentsPanel : null,
    componentsPanel : null,

    constructor: function(config) {
        var scriptBuilderFrm = this;
        this.wizardState = config.wizardState;

        this.sourceText = Ext.create('Ext.form.field.TextArea', {
            title : 'Script Source',
            region : 'center',
            readOnly : true,
            fieldStyle : {
                'font-family' : 'monospace'
            }
        });

        this.activeComponentsPanel = Ext.create('ScriptBuilder.ActiveComponentTreePanel', {
            region : 'west',
            title : 'Used Snippets',
            width : 250,
            listeners : {
                componentschanged : function(panel) {
                    scriptBuilderFrm.sourceText.setValue(panel.generateScript());
                }
            }
        });

        this.componentsPanel = Ext.create('ScriptBuilder.ComponentTreePanel', {
            region : 'west',
            title : 'Available Snippets',
            width : 250,
            listeners : {
                addcomponent : Ext.bind(this.onAddComponent, this)
            }
        });

        Ext.apply(config, {
            layout : 'border',
            id : 'scriptbuilder-form',
            border : false,
            items: [{
                xtype : 'panel',
                border : false,
                region : 'center',
                layout : 'border',
                title : 'Current Script',
                items : [this.activeComponentsPanel, {
                    xtype : 'panel',
                    border : false,
                    layout : 'fit',
                    region : 'center',
                    title : 'Script Source',
                    items : [this.sourceText],
                    buttons : [{
                        text : 'Edit Script',
                        scope : this,
                        handler : function() {
                            Ext.Msg.confirm('Switch to Texteditor', 'After switching to text edit mode you can no longer use the code snippets. Are you sure you want to switch?',
                                function(btn) {
                                    if (btn=='yes') {
                                        scriptBuilderFrm.enableTextEdit();
                                    }
                                }
                            );
                        }
                    }]
                }]
            }, this.componentsPanel]
        });

        // Finally, build the main layout once all the pieces are ready.
        this.callParent(arguments);
    },

    onAddComponent : function(panel, componentClass, name, description) {
        this.activeComponentsPanel.addActiveComponent(componentClass, name, description);
    },

    enableTextEdit : function() {
        this.textEditMode = true;
        this.componentsPanel.un('addcomponent', this.onAddComponent);

        this.activeComponentsPanel.ownerCt.remove(this.activeComponentsPanel);
        this.componentsPanel.ownerCt.remove(this.componentsPanel);

        this.sourceText.setReadOnly(false);

    },

    getScript : function() {
        return this.sourceText.getValue();
    }
});