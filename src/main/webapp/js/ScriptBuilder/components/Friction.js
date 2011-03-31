/*
 * This file is part of the AuScope Virtual Rock Lab (VRL) project.
 * Copyright (c) 2009 ESSCC, The University of Queensland
 *
 * Licensed under the terms of the GNU Lesser General Public License.
 */

Ext.namespace("ScriptBuilder");
Ext.ux.ComponentLoader.load({url: ScriptBuilder.componentPath+"Friction.json"});

FrictionNode = Ext.extend(ScriptBuilder.BaseComponent, {
  constructor: function(container) {
    FrictionNode.superclass.constructor.apply(this,
        [container, "Frictional Contact", "Friction", "i"]
    );
    var numInts = container.getInteractions().length;
    this.values.uniqueName = "interaction"+numInts;
  },

  getScript: function() {
    var ret="sim.createInteractionGroup (\n   NRotFrictionPrms (\n";
    ret+="      name = \""+this.values.uniqueName+"\",\n";
    ret+="      normalK = "+this.values.normalK+",\n";
    ret+="      dynamicMu = "+this.values.dynamicMu+",\n";
    ret+="      shearK = "+this.values.shearK+"\n   )\n)\n\n";

    return ret;
  }
});

