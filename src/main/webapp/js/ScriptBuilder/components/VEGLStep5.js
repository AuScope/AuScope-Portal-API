/*
 * This file is part of the AuScope Virtual Exploration Geophysics Lab (VEGL) project.
 * Copyright (c) 2011 CSIRO Earth Science and Resource Engineering
 *
 * Licensed under the terms of the GNU Lesser General Public License.
 */

Ext.namespace("ScriptBuilder");
Ext.ux.ComponentLoader.load({url: ScriptBuilder.componentPath+"VEGLStep5.json"});

VEGLStep5Node = Ext.extend(ScriptBuilder.BasePythonComponent, {
  constructor: function(container) {
	  VEGLStep5Node.superclass.constructor.apply(this,
        [container, "VEGL - Step5", "VEGLStep5", "s"]
    );

	var numShells = container.getShellCommands().length;
    this.values.uniqueName = "VEGLStep5-i"+numShells;
  },

  getScript: function() {
	var text = '';

	text += this._tab + "# UBC-GIF needs a data file in a specific format." + this._newLine;
    text += this._tab + "# We need to define a filename ('obs_filename')." + this._newLine;
    text += this._tab + "# This includes writing out expected errors in the data, number of data points etc." + this._newLine;
    text += this._tab + "print 'Time to write out a data file'" + this._newLine;
    text += this._tab + "obs_file = 'temp_ubc_obs.asc'" + this._newLine;
    text += this._tab + "f = file(obs_file, 'w')" + this._newLine;
    text += this._tab + "f.write(str(len(data)) + '\\t! Number of points\\n')" + this._newLine;
    text += this._tab + "# For each data point, write out: Easting, Northing, Elevation, Data, Error" + this._newLine;
    text += this._tab + "# In this simple example, we assume elevation is 1 m, and error are 2 mGal / nT" + this._newLine;
    text += this._tab + "for east,north,prop in data:" + this._newLine;
    text += this._tab + this._tab + "elevation = 1.0" + this._newLine;
    text += this._tab + this._tab + "error = 2.0" + this._newLine;
    text += this._tab + this._tab + "f.write(str(east) + ' ' + str(north) + ' ' + str(elevation) + ' ' + str(prop) + ' ' + str(error) + '\\n')" + this._newLine;
    text += this._tab + "f.close()" + this._newLine;

    return text;
  }
});

