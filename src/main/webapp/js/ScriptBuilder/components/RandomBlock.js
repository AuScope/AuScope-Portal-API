/*
 * This file is part of the AuScope Virtual Rock Lab (VRL) project.
 * Copyright (c) 2009 ESSCC, The University of Queensland
 *
 * Licensed under the terms of the GNU Lesser General Public License.
 */

Ext.namespace("ScriptBuilder");
Ext.ux.ComponentLoader.load({url: ScriptBuilder.componentPath+"RandomBlock.json"});

RandomBlockNode = Ext.extend(ScriptBuilder.BaseComponent, {
  constructor: function(container) {
    RandomBlockNode.superclass.constructor.apply(this,
        [container, "Random Block", "RandomBlock", "g"]
    );
  },

  getUniqueName: function() {
    return "geometry";
  },

  getScript: function() {
    var ret = "geoRandomBlock = RandomBoxPacker (\n   minRadius = "+this.values.minRadius+",\n";
    ret+="   maxRadius = "+this.values.maxRadius+",\n";
    ret+="   cubicPackRadius = "+this.values.cubicRadius+",\n";
    ret+="   maxInsertFails = "+this.values.maxFailures+",\n";
    ret+="   bBox = BoundingBox( Vec3(";
    ret+=this.values.llX+", "+this.values.llY+", "+this.values.llZ+"), Vec3(";
    ret+=this.values.urX+", "+this.values.urY+", "+this.values.urZ+") ),\n";
    var circDimList=["False","False","False"];
    if (this.container.getValues().periodic>0) {
      circDimList[this.container.getValues().periodic-1]="True";
    }
    ret+="   circDimList = ["+circDimList[0]+", "+circDimList[1]+", "+circDimList[2]+"],\n";
    ret+="   tolerance = "+this.values.tolerance+"\n)\n";
    ret+="geoRandomBlock.generate ()\n";
    ret+="geometry = geoRandomBlock.getSimpleSphereCollection ()\n";
    ret+="sim.createParticles ( geometry )\n\n";
    return ret;
  }
});

