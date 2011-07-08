/*
 * This file is part of the AuScope Virtual Exploration Geophysics Lab (VEGL) project.
 * Copyright (c) 2011 CSIRO Earth Science and Resource Engineering
 *
 * Licensed under the terms of the GNU Lesser General Public License.
 */

Ext.namespace("ScriptBuilder");
Ext.ux.ComponentLoader.load({url: ScriptBuilder.componentPath+"VEGLStep4.json"});

VEGLStep4Node = Ext.extend(ScriptBuilder.BaseComponent, {
  constructor: function(container) {
	  VEGLStep4Node.superclass.constructor.apply(this,
        [container, "VEGLStep4", "VEGLStep4", "s"]
    );

	var numShells = container.getShellCommands().length;
    this.values.uniqueName = "VEGLStep4-i"+numShells;
  },

  getScript: function() {
	  if (this.values.invType == "grav") {
			  return "" +
			  		"# If we have a gravity inversion, we need to correct the units of the supplied gravity data.\n" +
			  		"# National gravity coverages are in units of micrometres per second squared.\n" +
			  		"# UBC-GIF gravity inversion expects milliGals, which means we divide supplied properties by 10.\n" +
			  		"#\n" +
			  		"    i = 0\n" +
			  		"    for east,north,prop in data:\n" +
			  		"        data[i] = east,north,prop/10\n" +
			  		"        i = i + 1\n\n";
	  }
	  if (this.values.invType == "mag") {
			  return "" +
			  		"    # --- Scientific description below ---\n" +
			  		"    # If we have a magnetic inversion, we need to define the magnetic field properties.\n" +
			  		"    # General convention is to assign the magnetic properties associated with the\n" +
			  		"    # middle of the inversion area.\n" +
			  		"    #\n" +
			  		"    central_lat = (VEGLSelectedBox.getMaxNorthing() - VEGLSelectedBox.getMinNorthing()) / 2\n" +
			  		"    central_lon = (VEGLSelectedBox.getMaxEasting() - VEGLSelectedBox.getMinEasting()) / 2\n" +
			  		"    declination,inclination,intensity = get_mag_field_data(central_lat, central_lon,2010,01,01)\n";
			  	  }
	return "";
  }
});

