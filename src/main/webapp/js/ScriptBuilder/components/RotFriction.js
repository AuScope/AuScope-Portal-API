/*
 * This file is part of the AuScope Virtual Rock Lab (VRL) project.
 * Copyright (c) 2009 ESSCC, The University of Queensland
 *
 * Licensed under the terms of the GNU Lesser General Public License.
 */

Ext.namespace("ScriptBuilder");
Ext.ux.ComponentLoader.load({url: ScriptBuilder.componentPath+"RotFriction.json"});

RotFrictionNode = Ext.extend(ScriptBuilder.BaseComponent, {
  constructor: function(container) {
    RotFrictionNode.superclass.constructor.apply(this,
        [container, "Rotational Frictional Contact", "RotFriction", "i"]
    );
    var numInts = container.getInteractions().length;
    this.values.uniqueName = "interaction"+numInts;
  },

  getScript: function() {
    var ret="sim.createInteractionGroup (\n   RotFrictionPrms (\n";
    ret+="      name = \""+this.values.uniqueName+"\",\n";
    ret+="      normalK = "+this.values.normalK+",\n";
    ret+="      dynamicMu = "+this.values.dynamicMu+",\n";
    ret+="      staticMu = "+this.values.staticMu+",\n";
    ret+="      shearK = "+this.values.shearK+"\n   )\n)\n\n";

    return ret;
  }
});

