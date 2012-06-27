/**
 * A panel for housing script builder component nodes (not ScriptBuilder BaseComponent)
 *
 * adds events {
 *  componentschanged : function(ScriptBuilder.ActiveComponentTreePanel this) - raised whenever one or more components change
 * }
 */
Ext.define('ScriptBuilder.ActiveComponentTreePanel', {
    extend : 'Ext.tree.Panel',

    alias : 'widget.sbactivecomponenttreepanel',

    deleteComponentAction : null,
    configureComponentAction : null,
    contextMenu : null,

    constructor : function(config) {
        this.deleteComponentAction = new Ext.Action({
            text: 'Delete Snippet',
            iconCls: 'cross-icon',
            disabled : true,
            scope : this,
            handler: function() {
                var selection = this.getSelectionModel().getSelection();
                for (var i = 0; i < selection.length; i++) {
                    //Dont delete root
                    if (selection[i].parentNode) {
                        selection[i].remove();
                    }
                }
            }
        });

        this.configureComponentAction = new Ext.Action({
            text: 'Configure Snippet',
            iconCls: 'settings-icon',
            disabled : true,
            scope : this,
            handler: function() {
                var selection = this.getSelectionModel().getSelection();
                if (selection.length > 0) {
                    this.showSettingsForComponent(selection[0]);
                }
            }
        });

        this.contextMenu = Ext.create('Ext.menu.Menu', {
            items: [this.deleteComponentAction, this.configureComponentAction]
        });

        Ext.apply(config, {
            viewConfig: {
                plugins: {
                    ptype: 'treeviewdragdrop'
                },
                emptyText : '<p class="centeredlabel">You haven\'t added any script snippets. You can add some by double clicking the snippets to the left</p>',
                deferEmptyText : false,
                listeners : {
                    itemcontextmenu : Ext.bind(this._onContextMenu, this)
                }
            },
            store: new Ext.data.TreeStore({
                model: 'ScriptBuilder.ActiveComponentModel',
                root: {
                    text : 'Script Snippets',
                    qtip : 'The below nodes are every code snippet that has been added to the script',
                    children : [],
                    expanded : true
                }
            }),
            rootVisible : false,
            multiSelect : true,
            tbar: [{
                text: 'Actions',
                iconCls: 'folder-icon',
                menu: [ this.deleteComponentAction, this.configureComponentAction]
            }]
        });

        this.addEvents({
            'componentschanged' : true
        });

        this.callParent(arguments);

        this.on('itemremove', this._onItemRemove, this);
        this.on('itemdblclick', this._onDblClick, this);
        this.on('selectionchange', this._onSelectionChange, this);
        this.on('itemmove', this._onItemMove, this);

    },

    _onContextMenu : function(view, record, el, index, e, eOpts) {
        e.stopEvent();

        var sm = this.getSelectionModel();
        if (!sm.isSelected(record)) {
            this.getSelectionModel().select([record]);
        }

        this.contextMenu.showAt(e.getXY());
        return false;
    },

    _onItemMove : function() {
        this.fireEvent('componentschanged', this);
    },

    _onSelectionChange : function() {
        var selections = this.getSelectionModel().getSelection();
        this.deleteComponentAction.setDisabled(selections.length === 0);
        this.configureComponentAction.setDisabled(selections.length !== 1);
    },

    /**
     * Handles the user wanting to edit an existing node
     */
    _onDblClick : function(view, node, el, index, e, eOpts) {
        this.showSettingsForComponent(node);
    },

    /**
     * Handles destroying nodes as they are permanently removed from the tree
     */
    _onItemRemove : function(ni, node, isMove, eOpts) {
        if (!isMove) {
            var cmp = node.get('component');
            if (cmp) {
                cmp.destroy();
            }

            this.fireEvent('componentschanged', this);
        }
    },

    /**
     * Shows a settings window for editing the values of the specified activeComponent
     */
    showSettingsForComponent : function(activeComponent) {
        var component = activeComponent.get('component');
        if (!component) {
            return;
        }

        var dlg = Ext.create('Ext.window.Window', {
            title: activeComponent.get('text') + ' Settings',
            plain: true,
            minWidth: 300,
            minHeight: 200,
            width: 500,
            resizable: false,
            autoScroll: true,
            constrainHeader: true,
            bodyStyle:'padding:5px;',
            items: [component],
            modal: true,
            buttons: [{
                text: 'OK',
                scope : this,
                handler: function(btn) {
                    if (!component.validateValues()) {
                        return;
                    }

                    dlg.close();

                    this.fireEvent('componentschanged', this);
                }
            },{
                text: 'Cancel',
                handler: function() { dlg.close(); }
            }],
            listeners : {
                beforeclose : function(dlg) {
                    dlg.remove(component, false);  //This is to avoid destroying our component on window close
                }
            }
        });

        dlg.show();
    },

    /**
     * Adds a ScriptBuilder component of the specified class to this panel
     */
    addActiveComponent : function(componentClass, componentName, componentDescription, noPrompt) {
        var component = Ext.create(componentClass, {
            name : componentName,
            description : componentDescription,
            autoDestroy : false
        });

        var activeComponent = Ext.create('ScriptBuilder.ActiveComponentModel', {
            componentClass : componentClass,
            component : component,
            text : componentName,
            qtip : componentDescription,
            leaf : true
        });

        var root = this.getRootNode();
        root.appendChild(activeComponent);

        if (noPrompt) {
            this.fireEvent('componentschanged', this);
        } else {
            this.showSettingsForComponent(activeComponent);
        }

        return activeComponent;
    },

    /**
     * Generates the script representing the concatentation of all components in this tree
     */
    generateScript : function() {
        var root = this.getRootNode();
        var activeComponents = root.childNodes;
        var script = '';

        for (var i = 0; i < activeComponents.length; i++) {
            var cmp = activeComponents[i].get('component');
            if (cmp) {
                script += cmp.getScript();
            }
        }

        return script;
    }
});