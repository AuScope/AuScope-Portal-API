/*
 * This file is part of the AuScope Virtual Rock Lab (VRL) project.
 * Copyright (c) 2009 ESSCC, The University of Queensland
 *
 * Licensed under the terms of the GNU Lesser General Public License.
 */

Ext.namespace("ScriptBuilder");
Ext.ux.ComponentLoader.load({url: ScriptBuilder.componentPath+"BondedElastic.json"});

BondedElasticNode = Ext.extend(ScriptBuilder.BaseComponent, {
  constructor: function(container) {
    BondedElasticNode.superclass.constructor.apply(this,
        [container, "Bonded Elastic-Brittle", "BondedElastic", "i"]
    );
    var numInts = container.getInteractions().length;
    this.values.uniqueName = "interaction"+numInts;
  },

  getScript: function() {
    var ret=this.values.uniqueName+" = sim.createInteractionGroup (\n   NRotBondPrms (\n";
    ret+="      name = \""+this.values.uniqueName+"\",\n";
    ret+="      normalK = "+this.values.normalK+",\n";
    ret+="      breakDistance = "+this.values.breakDist+",\n";
    ret+="      tag = "+this.values.tag+"\n   )\n)\n";
    ret+=this.values.uniqueName+"_finder = SimpleSphereNeighbours (\n";
    ret+="   maxDist = "+this.values.maxDist+",\n";
    ret+="   bBox = geometry.getBBox(),\n";
    var circDimList=["False","False","False"];
    if (this.container.getValues().periodic>0) {
      circDimList[this.container.getValues().periodic-1]="True";
    }
    ret+="   circDimList = ["+circDimList[0]+", "+circDimList[1]+", "+circDimList[2]+"]\n)\n";
    ret+=this.values.uniqueName+"_idpairs = "+this.values.uniqueName+"_finder.getNeighbours ( geometry )\n";
    ret+=this.values.uniqueName+".createInteractions ( "+this.values.uniqueName+"_idpairs )\n\n";

    return ret;
  }
});

