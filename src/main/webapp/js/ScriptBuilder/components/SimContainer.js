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
      "scriptName": "particle_script",
      "particleType": "NRotSphere",
      "numTimesteps": "1000",
      "timeIncrement": "0.0001",
      "constrain2D": "0",
      "periodic": "0",
      "subdivX": "1",
      "subdivY": "1",
      "subdivZ": "1",
      "llfX": "-10",
      "llfY": "-10",
      "llfZ": "-10",
      "urbX": "10",
      "urbY": "10",
      "urbZ": "10",
      "gridSpacing": "2.5",
      "verletDist": "0.2"
    }
  },

  getScriptName: function() {
    return this.values.scriptName;
  },

  // collects the code fragments from all nodes in the tree
  getScript: function() {
    var scriptHeader = "\
# ESyS-Particle Simulation Script\n\
# Created using the ESyS-Particle Script Builder Web Interface\n\
# (c) 2009 ESSCC, The University of Queensland, Australia. All rights reserved.\n\n\
from esys.lsm import *\n\
from esys.lsm.util import *\n\
from esys.lsm.geometry import *\n\n";

    var ret = scriptHeader;

    appendScript = function(node) { ret+=node.getScript(); }

    var numWorkers=this.values.subdivX*this.values.subdivY*this.values.subdivZ;
    ret+="sim = LsmMpi ( numWorkerProcesses = "+numWorkers+", mpiDimList = [";
    ret+=this.values.subdivX+", "+this.values.subdivY+", "+this.values.subdivZ+"] )\n";
    ret+="sim.initVerletModel (\n   particleType = \""+this.values.particleType;
    ret+="\",\n   gridSpacing = "+this.values.gridSpacing+",\n   verletDist = ";
    ret+=this.values.verletDist+"\n)\n";
    ret+="sim.setNumTimeSteps ( "+this.values.numTimesteps+" )\n";
    ret+="sim.setTimeStepSize ( "+this.values.timeIncrement+" )\n";
    ret+="sim.setSpatialDomain ( BoundingBox( Vec3(";
    ret+=this.values.llfX+", "+this.values.llfY+", "+this.values.llfZ+"), Vec3(";
    ret+=this.values.urbX+", "+this.values.urbY+", "+this.values.urbZ+") ) )\n";
    if (this.values.constrain2D=="1") {
      ret+="sim.force2dComputations ( True )\n";
    }
    ret+="\n";
    this.eachChild(appendScript);
    ret+="\nsim.run()\n";
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

