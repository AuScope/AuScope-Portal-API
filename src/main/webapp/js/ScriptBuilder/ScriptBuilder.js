Ext.define('ScriptBuilder.ScriptBuilder', {

    /**
     * @lends ScriptBuilder
     */
    extend : 'Ext.panel.Panel',

    textEditMode : false,
    wizardState : null,
    templateVariables : null,

    editor  : null,
    componentsPanel : null,


    /**
     * A component for tying all of the script builder components together
     * @constructs
     * @param {object} config
     */
    constructor: function(config) {
        this.wizardState = config.wizardState;
        this.templateVariables = config.templateVariables;

        this.editor = Ext.create('vegl.widgets.CodeEditorField',{
            mode      : 'python',
            name      : 'scriptcodefield'
        })

        var editorPanel =  Ext.create('Ext.form.FormPanel', {
            region : 'center',
            layout    : 'fit',
            scrollable  : 'y',
            items: [this.editor]
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
            margin: '10 0 0 0',
            bodyStyle: {
                'background-color': 'white'
            },
            items: [
                    {
                        xtype : 'panel',
                        border : false,
                        itemId : 'sb-script-panel',
                        layout : 'fit',
                        region : 'center',
                        title : 'Script Source',
                        margin: '0 0 0 10',
                        bodyStyle: {
                            'background-color': 'white'
                        },
                        items : [editorPanel]
                    },
                    this.componentsPanel
            ]
        });

        // Finally, build the main layout once all the pieces are ready.
        this.callParent(arguments);
    },


    /**
     * Add the script to the component
     * @function
     * @param {object} panel
     * @param {object} entry
     * @param {string} name
     * @param {string} description
     *
     */
    onAddComponent : function(panel, entry, name, description) {
    	var me = this;

        //Create the template which will load our script
        var template = Ext.create('ScriptBuilder.templates.DynamicTemplate', {
            entry: entry,
            name : name,
            description : description,
            wizardState : me.wizardState,
            parameters : Ext.apply({}, me.templateVariables)
        });

        template.requestScript(function(status, script) {

            //Once we have the script text - ask the user what they want to do with it
            if (status === ScriptBuilder.templates.BaseTemplate.TEMPLATE_RESULT_SUCCESS) {
                // Store the selected solution
                me.setSolution(entry);

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
     * @function
     */
    buildComponentsPanel : function() {
    	// Populate the panel after retrieving the templates
        var self = this;
        ScriptBuilder.Components.getComponents(
            this.componentsPanel,
            function() {
                // If a solution is currently active, select it
                self.selectSolution();
            });
    },


    /**
     * Inserts the specified script at the current caret location
     * @function
     */
    insertScript : function(script) {
        var from = null;
        var to = null;

        if (this.editor.somethingSelected()) {
            from = this.editor.getCursor(true);
            to = this.editor.getCursor(false);
        } else {
            from = this.editor.getCursor();
        }

        this.editor.replaceRange(script, from, to);
    },


    /**
     * Replaces the current tab with the specified script
     * @function
     */
    replaceScript : function(script) {
        this.editor.setValue(script);
    },


    /**
     * Get script from the editor
     * @function
     */
    getScript : function() {
        return this.editor.getValue();
    },


    /**
     * Get the solutionId (URI)
     * @function
     */
    getSolutionId: function() {
        return this.wizardState.solutionId;
    },


    /**
     * Set the solution
     * @function
     */
    setSolution: function(solution) {
    	// Store the solution information and select corresponding node
        this.solution = solution;
        this.setSolutionId(solution.uri);
    },


    /**
     * Set the solutionId
     * @function
     */
    setSolutionId: function(solutionId) {
        this.wizardState.solutionId = solutionId;
        this.selectSolution();
    },


    /**
     * Select the node corresponding to the current solution
     * @function
     */
    selectSolution: function() {
        if (!Ext.isEmpty(this.wizardState.solutionId)) {
            var solutionChild = this
                    .componentsPanel
                    .getRootNode()
                    .findChild('id', this.wizardState.solutionId, true);
            if (solutionChild) {
                this.componentsPanel.selectPath(solutionChild.getPath());
            }
        }
    }
});