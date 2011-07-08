/*
 * This file is part of the AuScope Virtual Rock Lab (VRL) project.
 * Copyright (c) 2009 ESSCC, The University of Queensland
 *
 * Licensed under the terms of the GNU Lesser General Public License.
 */

// reference local blank image
Ext.BLANK_IMAGE_URL = 'js/external/extjs/resources/images/default/s.gif';

Ext.namespace('ScriptBuilder');

ScriptBuilder.ControllerURL = "scriptbuilder.html";

// default content of the component description panel
ScriptBuilder.compDescText = '<p class="desc-info">Select a component to see its description, double-click to add it to the script.<br/><br/>Double-click the Simulation Container to change simulation settings.</p>';
// content of the component description panel in text editor mode
ScriptBuilder.compDescTextEditor = '<p class="desc-info">Select a component to see its description.<br/></p>';

//
// Extend the XmlTreeLoader to set some custom TreeNode attributes specific to
// our application:
//
ESySComponentLoader = Ext.extend(Ext.ux.XmlTreeLoader, {
    processAttributes : function(attr) {
        if (attr.name) { // category node
            // Set the node text that will show in the tree
            attr.text = attr.name;

            // Override these values for our folder nodes because we are
            // loading all data at once. If we were loading each node
            // asynchronously (the default) we would not want to do this:
            attr.loaded = true;
        } else if (attr.title) { // component node
            attr.text = attr.title;
            //attr.iconCls = 'book';
            attr.leaf = true;
        }
    }
});

////////////////////////
////// Callbacks ///////
////////////////////////

// called when the user tries to navigate away from this site
ScriptBuilder.onWindowUnloading = function(e) {
    if (this.confirmUnloading != false) {
        e.browserEvent.returnValue = "All changes will be lost! Please use the Download button if you want to keep the current script file or 'Use Script' to submit it.";
    }
}

// called when retrieving an initial script from the server failed
ScriptBuilder.onGetScriptTextFailure = function(response, request) {
    ScriptBuilder.textEditMode = false;
    ScriptBuilder.updateSource();

    // Show settings dialog for new script
    //ScriptBuilder.showDialog('SimContainer', 'New Script',
            //Ext.getCmp('usedcomps-panel').getRootNode());
}

// called when the server replied to the initial script text request
ScriptBuilder.onGetScriptTextResponse = function(response, request) {
    var resp = Ext.decode(response.responseText);
    if (resp.scriptText != null) {
        Ext.getCmp('scriptname').setRawValue(resp.scriptName);
        Ext.getCmp('sourcetext').setValue(resp.scriptText);
        ScriptBuilder.switchToTextEditor();

    } else {
        // Reuse code from failure path
        ScriptBuilder.onGetScriptTextFailure(response, request);
    }
}

////////////////////////
////// Functions ///////
////////////////////////

//Loads a number of default pre-filled components and adds them to the SimContainer
ScriptBuilder.loadDefaultComponents = function() {
	var rootNode = Ext.getCmp('usedcomps-panel').getRootNode();
	var newNode = null;
	var i = 1;


}

// opens the configuration dialog for given component type and ensures
// that values are stored and restored accordingly
// If 'node' is given its values are edited otherwise a new node is
// created.
ScriptBuilder.showDialog = function(compId, compTitle, node) {
    var dlgContents = Ext.getCmp(compId+'Form');
    var rootNode = Ext.getCmp('usedcomps-panel').getRootNode();
    var isNewNode;

    // Fill the form elements with correct values if editing component
    if (node) {
        //node.fillFormValues(dlgContents.getForm());
        isNewNode = false;
    } else {
        node = new dlgContents.nclass(rootNode);
        //node.fillFormValues(dlgContents.getForm());
        isNewNode = true;
    }

    var dlg = new Ext.Window({
        title: compTitle+' Settings',
        plain: true,
        minWidth: 300,
        minHeight: 200,
        width: 500,
        resizable: false,
        autoScroll: true,
        constrainHeader: true,
        bodyStyle:'padding:5px;',
        items: dlgContents,
        modal: true,
        buttons: [{
            text: 'OK',
            handler: function() {
                if (!dlgContents.getForm().isValid()) {
                    Ext.Msg.alert('Invalid Field(s)',
                        'Please provide values for all fields!');
                    return;
                }
                if (!node.setValues(dlgContents.getForm())) {
                    return;
                }
                if (isNewNode) {
                    rootNode.addComponent(node);
                }
                ScriptBuilder.updateSource();
                if (compId == "SimContainer") {
                    Ext.getCmp('scriptname').setRawValue(
                            rootNode.getScriptName());
                }
                dlg.close();
            }
        }, {
            text: 'Cancel',
            handler: function() { dlg.close(); }
        }]
    });
    // Prevent destruction of dlgContents (IE needs this)
    dlg.on('beforedestroy', function() { return false; });
    dlg.on('beforeshow', function() {
        node.fillFormValues(dlgContents.getForm());
    });
    dlg.show();
}

// updates the source textarea with the current script text
ScriptBuilder.updateSource = function() {
    var s = Ext.getCmp('usedcomps-panel').getRootNode().getScript();
    Ext.getCmp('sourcetext').setValue(s);
}

// changes the interface so script can be edited and component adding is
// no longer possible
ScriptBuilder.switchToTextEditor = function() {
    //Ext.getCmp('content-panel').remove('usedcomps-panel');
	Ext.getCmp('usedcomps-panel').hide();
    Ext.getCmp('content-panel').doLayout();
    var descEl = Ext.getCmp('sb-description-panel').body;
    descEl.update(this.compDescTextEditor).setStyle('background','#eee');
    Ext.getCmp('edit-script-btn').disable();
    Ext.getCmp('sourcetext').enable();
    this.textEditMode = true;
}

//
//Called when the use script request fails
//
ScriptBuilder.onUseScriptFailure = function(response, request) {
	ScriptBuilder.showError('Error storing script file!');
}



//
// This is the main layout definition.
//
ScriptBuilder.initialize = function() {

    Ext.QuickTips.init();

    // a template for the component description html area
    var compDescTpl = new Ext.Template(
        '<h2 class="title">{title}</h2>',
        '<p>{innerText}</p>'
    );
    compDescTpl.compile();

    var deleteNodeAction = new Ext.Action({
        text: 'Delete component',
        iconCls: 'cross-icon',
        handler: function() {
            var node = Ext.getCmp('usedcomps-panel').getSelectionModel()
                .getSelectedNode();
            if (node.parentNode.canRemove(node)) {
                Ext.Msg.confirm('Delete Node',
                    'Are you sure you want to delete the selected component?',
                    function(btn) {
                        if (btn=='yes') {
                            node.parentNode.removeComponent(node);
                            ScriptBuilder.updateSource();
                        }
                    }
                );
            }
        }
    });

    // the tree that holds added components
    var usedCompsTree = new Ext.tree.TreePanel({
        xtype: 'treepanel',
        id: 'usedcomps-panel',
        title: 'Used Components',
        collapsible: true,
        region: 'west',
        floatable: false,
        margins: '5 0 0 0',
        cmargins: '5 5 0 0',
        width: 175,
        minSize: 100,
        maxSize: 250,
        rootVisible: true,
        root: new SimContainerNode,
        //rootVisible: false,
        //root: new Ext.tree.TreeNode(),
        contextMenu: new Ext.menu.Menu({
            items: [ deleteNodeAction ]
        })
    });

    usedCompsTree.on({
        'beforecollapsenode': function(node, deep, anim) {
            // Fix for the problem that child nodes are not visible if
            // removed and re-added.
            if (node.hasChildNodes()) {
                return false;
            } else {
                return true;
            }
        },

        'contextmenu': function(node, e) {
            // do not show menu for root node
            if (node.parentNode) {
                node.select();
                var menu = node.getOwnerTree().contextMenu;
                menu.showAt(e.getXY());
            }
        },

        'dblclick': function(node) {
            ScriptBuilder.showDialog(node.compId, node.text, node);
        }
    });

    // the source textarea embedded in a form for further processing
    // using buttons
    var sourceForm = new Ext.form.FormPanel({
        id: 'source-panel',
        layout: 'fit',
        title: 'Script Source',
        url: ScriptBuilder.ControllerURL,
        standardSubmit: true,
        region:'center',
        buttonAlign: 'right',
        margins: '5 0 0 0',
        buttons: [{
            text: 'Cancel',
            handler: function() {
                // confirm and go back to main page
                Ext.Msg.confirm('Quit ScriptBuilder', 'Are you sure you want to quit?',
                    function(btn) {
                        if (btn=='yes') {
                            ScriptBuilder.confirmUnloading = false;
                            window.location = "gmap.html";
                        }
                    }
                );
            }
        }, {
            id: 'edit-script-btn',
            text: 'Edit Script',
            handler: function() {
                // confirm and go back to main page
                Ext.Msg.confirm('Switch to Texteditor', 'After switching to text edit mode you can no longer use the GUI editor. Are you sure you want to switch?',
                    function(btn) {
                        if (btn=='yes') {
                            ScriptBuilder.switchToTextEditor();
                        }
                    }
                );
            }
        }, {
            text: 'Download Script',
            handler: function() {
                // submit script source for download
            	var params = {sourcetext: Ext.getCmp('sourcetext').getValue()};
            	var body = Ext.getBody();
            	var form = body.createChild({
                    tag:'form',
                    cls:'x-hidden',
                    id:'form',
                    action: 'downloadScript.do?'+Ext.urlEncode(params),
                    target:'iframe',
                    method:'POST'
                });

                form.dom.submit();
            }
        }, {
            text: 'Use Script',
            handler: function() {
                // submit script source for use in grid submit
                Ext.getCmp('action').setRawValue('useScript');
                if (ScriptBuilder.textEditMode == false) {
                    Ext.getCmp('sourcetext').enable();
                }
                ScriptBuilder.confirmUnloading = false;
                Ext.getCmp('source-panel').getForm().submit();

                Ext.Ajax.request({
                    url: 'useScript.do',
                    success: window.location = "gridsubmit.html",
                    failure: ScriptBuilder.onUseScriptFailure,
                    params: {
                        'sourcetext': Ext.getCmp('sourcetext').getValue()
                    }
                });
            }
        }],
        items: [{
            id: 'sourcetext',
            xtype: 'textarea',
            disabled: true,
            width: '100%',
            height: '100%'
        }, {
            id: 'action',
            xtype: 'hidden',
            value: ''
        }, {
            id: 'scriptname',
            xtype: 'hidden',
            value: 'vegl_script'
        }]
    });

    // the tree of available components
    var treePanel = new Ext.tree.TreePanel({
        id: 'tree-panel',
        title: 'Available Components',
        region:'center',
        height: 300,
        minSize: 150,
        autoScroll: true,

        // tree-specific configs:
        rootVisible: false,
        root: new Ext.tree.AsyncTreeNode(),
        loader: new ESySComponentLoader({
            dataUrl:'js/ScriptBuilder/components.xml'
        })
    });

    // Show corresponding description on click on a component
    treePanel.on({
        'click': function(node) {
            if (node.leaf) { // click on a component
                var descEl = Ext.getCmp('sb-description-panel').body;
                descEl.update('').setStyle('background','#fff');
                compDescTpl.overwrite(descEl, node.attributes);
            } else { // click on a category
                var descEl = Ext.getCmp('sb-description-panel').body;
                if (ScriptBuilder.textEditMode == true) {
                    descEl.update(ScriptBuilder.compDescTextEditor).setStyle('background','#eee');
                } else {
                    descEl.update(ScriptBuilder.compDescText).setStyle('background','#eee');
                }
            }
        },
        'dblclick': function(node) {
            if (ScriptBuilder.textEditMode == false && node.leaf &&
                Ext.getCmp('usedcomps-panel').getRootNode().canAppend(node.attributes)) {
                ScriptBuilder.showDialog(node.attributes.id, node.attributes.title);
            }
        }
    });

    // This is the description panel that contains the description for the
    // selected component.
    var descriptionPanel = {
        id: 'sb-description-panel',
        title: 'Component Description',
        region: 'south',
        split: true,
        bodyStyle: 'padding-bottom:15px;background:#eee;',
        autoScroll: true,
        collapsible: true,
        html: ScriptBuilder.compDescText
    };

    // Finally, build the main layout once all the pieces are ready.
    new Ext.Viewport({
        layout: 'border',
        defaults: { layout: 'border' },
        items: [{
            xtype: 'box',
            region: 'north',
            applyTo: 'body',
            height: 100
        },{
            id: 'component-browser',
            region:'west',
            border: false,
            split:true,
            margins: '2 0 2 2',
            width: 250,
            minSize: 150,
            maxSize: 500,
            items: [ treePanel, descriptionPanel ]
        },{
            id: 'content-panel',
            title: 'Current Script',
            region: 'center',
            margins: '2 2 2 0',
            defaults: {
                collapsible: false,
                split: true
            },
            items: [ usedCompsTree, sourceForm ]
        }]
    });

    // Avoid accidentally navigating away from this page
    Ext.EventManager.on(window, 'beforeunload',
            ScriptBuilder.onWindowUnloading, ScriptBuilder);

    // Check for existing script text to edit
    Ext.Ajax.request({
        url: 'getScriptText.do',
        success: ScriptBuilder.onGetScriptTextResponse,
        failure: ScriptBuilder.onGetScriptTextFailure
    });

    ScriptBuilder.loadDefaultComponents();
};


Ext.onReady(ScriptBuilder.initialize);

