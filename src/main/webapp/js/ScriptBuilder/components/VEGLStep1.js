/*
 * This file is part of the AuScope Virtual Exploration Geophysics Lab (VEGL) project.
 * Copyright (c) 2011 CSIRO Earth Science and Resource Engineering
 *
 * Licensed under the terms of the GNU Lesser General Public License.
 */

Ext.namespace("ScriptBuilder");
Ext.ux.ComponentLoader.load({url: ScriptBuilder.componentPath+"VEGLStep1.json"});

VEGLStep1Node = Ext.extend(ScriptBuilder.BaseComponent, {
  constructor: function(container) {
      VEGLStep1Node.superclass.constructor.apply(this,
        [container, "VEGL - Step1", "VEGLStep1", "s"]
    );

    var numShells = container.getShellCommands().length;
    this.values.uniqueName = "VEGLStep1-i"+numShells;
  },

  getScript: function() {
    return "    # Step 1: Read in data file\n\
    f = file(VEGLParams.getCSVName(), \"r\")\n\
    input_csv = csv.reader(f)\n\
    data = []\n\
    for strX, strY, strZ in input_csv:\n\
        x = float(strX)\n\
        y = float(strY)\n\
        z = float(strZ)\n\
        data.append([x,y,z]);\n\n\
";
  }
});

