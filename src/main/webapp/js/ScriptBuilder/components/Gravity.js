/*
 * This file is part of the AuScope Virtual Rock Lab (VRL) project.
 * Copyright (c) 2009 ESSCC, The University of Queensland
 *
 * Licensed under the terms of the GNU Lesser General Public License.
 */

Ext.namespace("ScriptBuilder");
Ext.ux.ComponentLoader.load({url: ScriptBuilder.componentPath+"Gravity.json"});

GravityNode = Ext.extend(ScriptBuilder.BaseComponent, {
  constructor: function(container) {
    GravityNode.superclass.constructor.apply(this,
        [container, "Gravity", "Gravity", "i"]
    );
    var numInts = container.getInteractions().length;
    this.values.uniqueName = "interaction"+numInts;
  },

  getScript: function() {
    var ret="sim.createInteractionGroup (\n   GravityPrms (\n";
    ret+="      name = \""+this.values.uniqueName+"\",\n";
    ret+="      acceleration = Vec3("+this.values.accX+", "+this.values.accY+", "+this.values.accZ+")\n";
    ret+="   )\n)\n\n";
    return ret;
  }
});

