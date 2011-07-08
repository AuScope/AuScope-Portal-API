/*
 * This file is part of the AuScope Virtual Exploration Geophysics Lab (VEGL) project.
 * Copyright (c) 2011 CSIRO Earth Science and Resource Engineering
 *
 * Licensed under the terms of the GNU Lesser General Public License.
 */

Ext.namespace("ScriptBuilder");
Ext.ux.ComponentLoader.load({url: ScriptBuilder.componentPath+"VEGLStep3.json"});

VEGLStep3Node = Ext.extend(ScriptBuilder.BaseComponent, {
  constructor: function(container) {
    VEGLStep3Node.superclass.constructor.apply(this,
        [container, "VEGL - Step3", "VEGLStep3", "s"]
    );

    var numShells = container.getShellCommands().length;
    this.values.uniqueName = "VEGLStep3-i"+numShells;
  },

  getScript: function() {
    return "   temp_data = []\n\
    for x, y, z in data:\n\
        # isPointInsideArea happens to read northings then eastings, and we store\n\
        # northings as y, and eastings as x\n\
        if VEGLPaddedBox.isPointInsideArea(y,x):\n\
            temp_data.append([x,y,z])\n\
        data = temp_data\n\n";
  }
});

