/*
 * This file is part of the AuScope Virtual Rock Lab (VRL) project.
 * Copyright (c) 2009 ESSCC, The University of Queensland
 *
 * Licensed under the terms of the GNU Lesser General Public License.
 */

Ext.namespace("ScriptBuilder");
Ext.ux.ComponentLoader.load({url: ScriptBuilder.componentPath+"RotationalVisc.json"});

RotationalViscNode = Ext.extend(ScriptBuilder.BaseComponent, {
  constructor: function(container) {
    RotationalViscNode.superclass.constructor.apply(this,
        [container, "Rotational Viscosity", "RotationalVisc", "i"]
    );
    var numInts = container.getInteractions().length;
    this.values.uniqueName = "interaction"+numInts;
  },

  getScript: function() {
    var ret="sim.createInteractionGroup (\n   RotDampingPrms (\n";
    ret+="      name = \""+this.values.uniqueName+"\",\n";
    ret+="      viscosity = "+this.values.viscosity+",\n";
    ret+="      maxIterations = "+this.values.maxIter+"\n   )\n)\n\n";
    return ret;
  }
});

