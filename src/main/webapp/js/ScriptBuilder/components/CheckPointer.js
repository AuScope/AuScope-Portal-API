/*
 * This file is part of the AuScope Virtual Rock Lab (VRL) project.
 * Copyright (c) 2009 ESSCC, The University of Queensland
 *
 * Licensed under the terms of the GNU Lesser General Public License.
 */

Ext.namespace("ScriptBuilder");
Ext.ux.ComponentLoader.load({url: ScriptBuilder.componentPath+"CheckPointer.json"});

CheckPointerNode = Ext.extend(ScriptBuilder.BaseComponent, {
  constructor: function(container) {
    CheckPointerNode.superclass.constructor.apply(this,
        [container, "CheckPointer", "CheckPointer", "c"]
    );
  },

  getUniqueName: function() {
    return "chkpointer";
  },

  getScript: function() {
    var ret="sim.createCheckPointer (\n   CheckPointPrms (\n";
    ret+="      fileNamePrefix = \""+this.values.fnPrefix+"\",\n";
    ret+="      beginTimeStep = "+this.values.beginTS+",\n";
    ret+="      endTimeStep = "+this.values.endTS+",\n";
    ret+="      timeStepIncr = "+this.values.timeIncrement+"\n   )\n)\n\n";

    return ret;
  }
});

