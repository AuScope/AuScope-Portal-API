/*
 * This file is part of the AuScope Virtual Rock Lab (VRL) project.
 * Copyright (c) 2009 ESSCC, The University of Queensland
 *
 * Licensed under the terms of the GNU Lesser General Public License.
 */

Ext.namespace("ScriptBuilder");
Ext.ux.ComponentLoader.load({url: ScriptBuilder.componentPath+"SimpleWall.json"});

SimpleWallNode = Ext.extend(ScriptBuilder.BaseComponent, {
  constructor: function(container) {
    SimpleWallNode.superclass.constructor.apply(this,
        [container, "Simple Wall", "SimpleWall", "w"]
    );
    var numWalls = container.getWalls().length;
    this.values.uniqueName = "wall"+numWalls;
  },

  getScript: function() {
    var ret="sim.createWall (\n   name = \""+this.values.uniqueName+"\",\n";
    ret+="   posn = Vec3("+this.values.originX+", "+this.values.originY+", "+this.values.originZ+"),\n";
    ret+="   normal = Vec3("+this.values.normalX+", "+this.values.normalY+", "+this.values.normalZ+")\n)\n\n";
    return ret;
  }
});

