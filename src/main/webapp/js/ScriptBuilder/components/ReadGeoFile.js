/*
 * This file is part of the AuScope Virtual Rock Lab (VRL) project.
 * Copyright (c) 2009 ESSCC, The University of Queensland
 *
 * Licensed under the terms of the GNU Lesser General Public License.
 */

Ext.namespace("ScriptBuilder");
Ext.ux.ComponentLoader.load({url: ScriptBuilder.componentPath+"ReadGeoFile.json"});

ReadGeoFileNode = Ext.extend(ScriptBuilder.BaseComponent, {
  constructor: function(container) {
    ReadGeoFileNode.superclass.constructor.apply(this,
        [container, "Read GEO File", "ReadGeoFile", "g"]
    );
  },

  getUniqueName: function() {
    return "geometry";
  },

  getScript: function() {
    var ret="sim.readGeometry ( fileName = \""+this.values.fileName+"\" )\n";
    return ret;
  }
});

