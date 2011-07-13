/**
 * A job wizard form for allowing the user to create their own custom script using the
 * ScriptBuilder library.
 * 
 * This file is a reworking of the AuScope Virtual Rock Lab (VRL) project ScriptBuilder.js
 * for the purpose of fitting into a VEGL 'Job Wizard' model 
 * 
 * Original Author - Cihan Altinay
 * Author - Josh Vote
 *  
 */
Ext.namespace("JobBuilder");

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

ScriptBuilderForm =  Ext.extend(BaseJobWizardForm, {
	
	textEditMode : false,
	
	ControllerURL : "scriptbuilder.html",

	// default content of the component description panel
	compDescText : '<p class="desc-info">Select a component to see its description, double-click to add it to the script.<br/><br/>Double-click the Simulation Container to change simulation settings.</p>',
	// content of the component description panel in text editor mode
	compDescTextEditor : '<p class="desc-info">Select a component to see its description.<br/></p>',
	
	/**
	 * Creates a new ScriptBuilderForm form configured to write/read to the specified global state
	 */
	constructor: function(wizardState) {
		var scriptBuilderFrm = this;
		
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
	                            scriptBuilderFrm.updateSource();
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
	        style : {
	        	'background-color' : '#ffffff'
	        },
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
	        	scriptBuilderFrm.showDialog(node.compId, node.text, node);
	        }
	    });

	    // the source textarea embedded in a form for further processing
	    // using buttons
	    var sourceForm = new Ext.form.FormPanel({
	        id: 'source-panel',
	        layout: 'fit',
	        title: 'Script Source',
	        url: scriptBuilderFrm.ControllerURL,
	        standardSubmit: true,
	        region:'center',
	        buttonAlign: 'right',
	        margins: '5 0 0 0',
	        buttons: [{
	            id: 'edit-script-btn',
	            text: 'Edit Script',
	            handler: function() {
	                // confirm and go back to main page
	                Ext.Msg.confirm('Switch to Texteditor', 'After switching to text edit mode you can no longer use the GUI editor. Are you sure you want to switch?',
	                    function(btn) {
	                        if (btn=='yes') {
	                        	scriptBuilderFrm.switchToTextEditor();
	                        }
	                    }
	                );
	            }
	        }],
	        items: [{
	            id: 'sourcetext',
	            xtype: 'textarea',
	            disabled: true,
	            width: '100%',
	            height: '100%',
	            style : {
	                'font-family' : 'monospace'
	            }
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
	        style : {
	        	'background-color' : '#ffffff'
	        },
	        
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
	                if (scriptBuilderFrm.textEditMode == true) {
	                    descEl.update(scriptBuilderFrm.compDescTextEditor).setStyle('background','#eee');
	                } else {
	                    descEl.update(scriptBuilderFrm.compDescText).setStyle('background','#eee');
	                }
	            }
	        },
	        'dblclick': function(node) {
	            if (scriptBuilderFrm.textEditMode == false && node.leaf &&
	                Ext.getCmp('usedcomps-panel').getRootNode().canAppend(node.attributes)) {
	            	scriptBuilderFrm.showDialog(node.attributes.id, node.attributes.title);
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
	        html: scriptBuilderFrm.compDescText
	    };
	    
	    // Finally, build the main layout once all the pieces are ready.
		ScriptBuilderForm.superclass.constructor.call(this, {
			wizardState : wizardState,
			layout : 'border',
			id : 'scriptbuilder-form',
			defaults: { layout: 'border' },
	        items: [{
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
		
		scriptBuilderFrm.loadDefaultComponents();
		scriptBuilderFrm.updateSource();
	},
	
	onGetScriptTextFailure : function() {
		this.textEditMode = false;
		this.updateSource();
	},
	
	onGetScriptTextResponse : function(response, request) {
	    var resp = Ext.decode(response.responseText);
	    if (resp.scriptText != null) {
	        Ext.getCmp('scriptname').setRawValue(resp.scriptName);
	        Ext.getCmp('sourcetext').setValue(resp.scriptText);
	        this.switchToTextEditor();

	    } else {
	        // Reuse code from failure path
	        this.onGetScriptTextFailure(response, request);
	    }
	},
	
	//Loads the default components
	loadDefaultComponents : function() {
		
	},
	
	// opens the configuration dialog for given component type and ensures
	// that values are stored and restored accordingly
	// If 'node' is given its values are edited otherwise a new node is
	// created.
	showDialog : function(compId, compTitle, node) {
	    var dlgContents = Ext.getCmp(compId+'Form');
	    var rootNode = Ext.getCmp('usedcomps-panel').getRootNode();
	    var scriptBuilderFrm = this;
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
	                scriptBuilderFrm.updateSource();
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
	},
	
	//updates the source textarea with the current script text
	updateSource : function() {
	    var s = Ext.getCmp('usedcomps-panel').getRootNode().getScript();
	    
	    var textArea = Ext.getCmp('sourcetext');
	    textArea.setValue(s);
	    
	    //Scroll to bottom (diving into DOM due to lack of support from ExtJS)
	    if (textArea.el) {
	        var textAreaDom = textArea.el.dom;
	        textAreaDom.scrollTop = textAreaDom.scrollHeight;
	    }
	},
	
	// changes the interface so script can be edited and component adding is
	// no longer possible
	switchToTextEditor : function() {
	    //Ext.getCmp('content-panel').remove('usedcomps-panel');
		Ext.getCmp('usedcomps-panel').hide();
	    Ext.getCmp('content-panel').doLayout();
	    var descEl = Ext.getCmp('sb-description-panel').body;
	    descEl.update(this.compDescTextEditor).setStyle('background','#eee');
	    Ext.getCmp('edit-script-btn').disable();
	    Ext.getCmp('sourcetext').enable();
	    this.textEditMode = true;
	},
	
	// submit script source for storage at the server
	beginValidation : function(callback) {
		var scriptBuilderFrm = this;
		var sourceTextCmp = Ext.getCmp('sourcetext'); 
		var sourceText = null;
		
        if (scriptBuilderFrm.textEditMode == false) {
        	sourceTextCmp.enable();
        	sourceText = sourceTextCmp.getValue();
        	sourceTextCmp.disable();
        } else {
        	sourceText = sourceTextCmp.getValue();
        }
        
        Ext.Ajax.request({
            url: 'saveScript.do',
            success: function() {
        		callback(true);
        	},
            failure: function() {
        		Ext.Msg.alert('Error', 'Error storing script file! Please try again in a few minutes');
            	callback(false);
        	},
            params: {
                'sourceText': sourceText,
                'jobId': scriptBuilderFrm.wizardState.jobId
            }
        });
	},
	
	/**
	 * [abstract] This function should return the title of the job wizard step.
	 */
	getTitle : function() {
		return "Define your job script.";
	}
});