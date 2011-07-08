/*
 * This file is part of the AuScope Virtual Exploration Geophysics Lab (VEGL) project.
 * Copyright (c) 2011 CSIRO Earth Science and Resource Engineering
 *
 * Licensed under the terms of the GNU Lesser General Public License.
 */

Ext.namespace("ScriptBuilder");
Ext.ux.ComponentLoader.load({url: ScriptBuilder.componentPath+"VEGLStep2.json"});

VEGLStep2Node = Ext.extend(ScriptBuilder.BaseComponent, {
  constructor: function(container) {
    VEGLStep2Node.superclass.constructor.apply(this,
      [container, "VEGL - Step2", "VEGLStep2", "s"]
    );

    var numShells = container.getShellCommands().length;
    this.values.uniqueName = "VEGLStep2-i"+numShells;
  },

  getScript: function() {
    return "	VEGLPaddedBox = VEGLParams.getPaddedBounds()\n\
    zone = int(VEGLPaddedBox.getSpatialReferenceSystem())\n\
    temp_data = []\n\
    for x, y, z in data:\n\
        newX, newY = project(x, y, zone)\n\
        temp_data.append([newX, newY, z])\n\
        data = temp_data\n";
  }
});

