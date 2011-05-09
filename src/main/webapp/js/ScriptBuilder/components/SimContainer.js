/*
 * This file is part of the AuScope Virtual Rock Lab (VRL) project.
 * Copyright (c) 2009 ESSCC, The University of Queensland
 *
 * Licensed under the terms of the GNU Lesser General Public License.
 */

Ext.namespace("ScriptBuilder");
Ext.ux.ComponentLoader.load({url: ScriptBuilder.componentPath+"SimContainer.json"});

SimContainerNode = Ext.extend(ScriptBuilder.BaseComponent, {
  constructor: function() {
    SimContainerNode.superclass.constructor.apply(this,
        [null, "Simulation Container", "SimContainer", "r"]
    );
    this.geomDesc = undefined;
    this.wallList = [];
    this.intList = [];
    this.expanded = true;
    
    this.values = {
      "scriptName": "vegl_script"
    }
  },

  getScriptName: function() {
    return this.values.scriptName;
  },

  // collects the code fragments from all nodes in the tree
  getScript: function() {
    
	var scriptHeader = "\
#!/bin/bash\n\
# VEGL script. \n\n";
			
	var scriptFooter = "\
# adds a dummy file to the output directory for testing purposes\n\
echo hello world > /tmp/output/result.txt";

		
	var ret = scriptHeader;

    appendScript = function(node) { ret+=node.getScript(); };

    this.eachChild(appendScript);
    ret+=scriptFooter;
    return ret;
  },

  getUniqueName: function() {
    return "sim";
  },

  // special container functions follow

  //
  // Returns the node with given unique name or null if not found
  //
  findByName: function(name) {
    nameMatch = function(node) { return node.getUniqueName() == name; }
    
    return this.findChildBy(nameMatch);
  },

  getInteractions: function() {
    return this.intList;
  },

  getWalls: function() {
    return this.wallList;
  },

  canAppend: function(nodeAttr) {
    var compID = nodeAttr.id;
    if (this.geomDesc != undefined && nodeAttr.type == "g") {
      Ext.Msg.alert('Not allowed', 'Only one geometry node can be added.');
      return false;
    } else if (this.geomDesc == undefined && (nodeAttr.type == "i" || nodeAttr.type == "f")) {
      Ext.Msg.alert('Not allowed', 'A particle geometry must be inserted before using this component.');
      return false;
    } else if (this.wallList.length < Number(nodeAttr.requiresWalls)) {
      Ext.Msg.alert('Not allowed', 'At least '+nodeAttr.requiresWalls+' wall(s) must be inserted before using this component.');
      return false;
    } else if (this.intList.length < Number(nodeAttr.requiresInts)) {
      Ext.Msg.alert('Not allowed', 'At least '+nodeAttr.requiresInts+' interaction(s) must be inserted before using this component.');
      return false;
    } else {
      return true;
    }
  },

  canRemove: function(node) {
    // TODO: Add more cases
    if (node.type == "g" && this.intList.length > 0) {
      Ext.Msg.alert('Not allowed', 'Cannot remove geometry node. Other components depend on it.');
      return false;
    } else {
      return true;
    }
  },

  addComponent: function(node) {
    this.appendChild(node);
    this.expand();
    if (node.type == "g") {
      this.geomDesc = node;
    } else if (node.type == "w") {
      this.wallList.push(node);
    } else if (node.type == "i" || node.type == "i") {
      this.intList.push(node);
    }
  },

  removeComponent: function(node) {
    this.removeChild(node);
    if (node.type == "g") {
      this.geomDesc = undefined;
    } else if (node.type == "w") {
      this.wallList.remove(node);
    } else if (node.type == "i" || node.type == "i") {
      this.intList.remove(node);
    }
  }

});

