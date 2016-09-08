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

        // Retrieve the default toolbox so we can restrict the solution sets the
        // user can create.
        if (!Ext.isDefined(this.wizardState.defaultToolbox)) {
            try {
                Ext.Ajax.request({
                    url : 'getDefaultToolbox.do',
                    scope : this,
                    callback : function(options, success, response) {
                        var errorMsg, errorInfo;
                        if (success) {
                            var responseObj = Ext.JSON.decode(response.responseText);
                            if (responseObj.success) {
                                this.wizardState.defaultToolbox = responseObj.data[0];
                                return;
                            } else {
                                errorMsg = responseObj.msg;
                                errorInfo = responseObj.debugInfo;
                            }
                        } else {
                            errorMsg = "There was an error fetching the default toolbox.";
                            errorInfo = "Please try again in a few minutes or report this error to cg_admin@csiro.au.";
                        }

                        //Create an error object and pass it to custom error window
                        var errorObj = {
                            title : 'Script Loading Error',
                            message : errorMsg,
                            info : errorInfo
                        };

                        var errorWin = Ext.create('portal.widgets.window.ErrorWindow', {
                            errorObj : errorObj
                        });
                        errorWin.show();
                    }
                });
            } catch (exception) {
                console.log("Exception: ScriptBuilder.constructor(), details below - ");
                console.log(exception);
            }
        }

        this.editor = Ext.create('vegl.widgets.CodeEditorField',{
            mode      : 'python',
            name      : 'scriptcodefield',
            readOnly  : true
        });

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
                me.editor.setReadOnly(false);
                //If there's nothing in the window - just put text in there and
                //reset solution set to the single solution.
                if (me.getScript().length === 0) {
                    me.setSolutions([entry]);
                    me.replaceScript(script);
                } else {
                    var popup = Ext.create('ScriptBuilder.InsertionPromptWindow', {
                        script : script,
                        listeners : {
                            select : function(popup, selection) {
                                switch(selection) {
                                case ScriptBuilder.InsertionPromptWindow.OPTION_REPLACE:
                                    me.setSolutions([entry]);
                                    me.replaceScript(script);
                                    break;
                                case ScriptBuilder.InsertionPromptWindow.OPTION_INSERT:
                                    me.addSolution(entry);
                                    me.insertScript(script);
                                    break;
                                }
                            }
                        }
                    });

                    // Check whether this solution can be added to the current
                    // set of solutions. We allow at most one solution that uses
                    // a non-default toolbox. If it's not allowed, explain why
                    // in the popup, and disable the "insert into script" option
                    // there.
                    if (!me.validSolution(entry)) {
                        popup.disableInsertOption("The selected solution uses a toolbox that is not compatible with the other(s) you have already selected. You may cancel and select a different solution, or proceed by replacing the entire script.");
                    }

                    popup.show();
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
     * Return the solution ids.
     * @function
     */
    getSolutions: function() {
        return this.wizardState.solutions;
    },

    /**
     * Set the solution ids, replacing any existing set of solutions.
     * @function
     */
    setSolutions: function(solutions) {
        this.wizardState.solutions = solutions;
    },

    /**
     * Add a solution to the current set in the wizardState.
     *
     * Initialises wizardState.solutions as an empty array if it is not already
     * defined.
     *
     * @function
     */
    addSolution: function(solution) {
        var solutions = this.wizardState.solutions;
        if (!Ext.isArray(solutions)) {
            solutions = [];
        }
        this.wizardState.solutions = Ext.Array.merge(solutions, [solution]);
    },

    /**
     * Return true if solution can be added to the current solution set.
     *
     * A solution is valid if it uses the default toolbox, or if, after adding
     * it to the set, there would only be one non-default toolbox used by the
     * solutions in the new set.
     *
     * @function
     */
    validSolution: function(solution) {
        var defaultToolbox = this.wizardState.defaultToolbox;
        var newToolbox = solution.toolbox.uri;

        // Default toolbox is always valid
        if (newToolbox == defaultToolbox) {
            return true;
        }

        // Check that all current solutions either use the default toolbox, or
        // the new one.
        return this.wizardState.solutions.every(function(s) {
            return ((s.toolbox.uri == defaultToolbox) ||
                    (s.toolbox.uri == newToolbox));
        });
    },

    /**
     * Select the node corresponding to the current solution
     * @function
     */
    selectSolution: function() {
        if (!Ext.isEmpty(this.wizardState.solutions)) {
            var solutionChild = this
                .componentsPanel
                .getRootNode()
                .findChild('id',
                           this.wizardState.solutions[this.wizardState.solutions.length - 1].uri,
                           true);
            if (solutionChild) {
                this.componentsPanel.selectPath(solutionChild.getPath());
            }
        }
    }
});
