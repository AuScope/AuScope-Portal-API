/*
 * This file is part of the AuScope Virtual Rock Lab (VRL) project.
 * Copyright (c) 2009 ESSCC, The University of Queensland
 *
 * Licensed under the terms of the GNU Lesser General Public License.
 */

Ext.namespace("ScriptBuilder");
Ext.ux.ComponentLoader.load({url: ScriptBuilder.componentPath+"BondedRotElastic.json"});

BondedRotElasticNode = Ext.extend(ScriptBuilder.BaseComponent, {
  constructor: function(container) {
    BondedRotElasticNode.superclass.constructor.apply(this,
        [container, "Rotational Bonded Elastic-Brittle", "BondedRotElastic", "i"]
    );
    var numInts = container.getInteractions().length;
    this.values.uniqueName = "interaction"+numInts;
  },

  getScript: function() {
    var ret=this.values.uniqueName+" = sim.createInteractionGroup (\n   RotBondPrms (\n";
    ret+="      name = \""+this.values.uniqueName+"\",\n";
    ret+="      normalK = "+this.values.normalK+",\n";
    ret+="      shearK = "+this.values.shearK+",\n";
    ret+="      torsionK = "+this.values.torsionK+",\n";
    ret+="      bendingK = "+this.values.bendingK+",\n";
    ret+="      normalBrkForce = "+this.values.normalF+",\n";
    ret+="      shearBrkForce = "+this.values.shearF+",\n";
    ret+="      torsionBrkForce = "+this.values.torsionF+",\n";
    ret+="      bendingBrkForce = "+this.values.bendingF+",\n";
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

