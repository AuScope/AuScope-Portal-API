/*
 * This file is part of the AuScope Virtual Exploration Geophysics Lab (VEGL) project.
 * Copyright (c) 2011 CSIRO Earth Science and Resource Engineering
 *
 * Licensed under the terms of the GNU Lesser General Public License.
 */

Ext.namespace("ScriptBuilder");
Ext.ux.ComponentLoader.load({url: ScriptBuilder.componentPath+"VEGLStep5.json"});

VEGLStep5Node = Ext.extend(ScriptBuilder.BaseComponent, {
  constructor: function(container) {
	  VEGLStep5Node.superclass.constructor.apply(this,
        [container, "VEGL - Step5", "VEGLStep5", "s"]
    );

	var numShells = container.getShellCommands().length;
    this.values.uniqueName = "VEGLStep5-i"+numShells;
  },

  getScript: function() {
	return "    # UBC-GIF needs a data file in a specific format.\n\
    # We need to define a filename ('obs_filename').\n\
    # This includes writing out expected errors in the data, number of data points etc.\n\
    print 'Time to write out a data file'\n\
    obs_file = 'temp_ubc_obs.asc'\n\
    f = file(obs_file, 'w')\n\
    f.write(str(len(data)) + '\t! Number of points\n')\n\
    # For each data point, write out: Easting, Northing, Elevation, Data, Error\n\
    # In this simple example, we assume elevation is 1 m, and error are 2 mGal / nT\n\
    for east,north,prop in data:\n\
        elevation = 1.0\n\
        error = 2.0\n\
        f.write(str(east) + ' ' + str(north) + ' ' + str(elevation) + ' ' + str(prop) + ' ' + str(error) + '\\n')\n\
    f.close()";
  }
});

