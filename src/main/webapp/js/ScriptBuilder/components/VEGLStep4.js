/*
 * This file is part of the AuScope Virtual Exploration Geophysics Lab (VEGL) project.
 * Copyright (c) 2011 CSIRO Earth Science and Resource Engineering
 *
 * Licensed under the terms of the GNU Lesser General Public License.
 */

Ext.namespace("ScriptBuilder");
Ext.ux.ComponentLoader.load({url: ScriptBuilder.componentPath+"VEGLStep4.json"});

VEGLStep4Node = Ext.extend(ScriptBuilder.BasePythonComponent, {
  constructor: function(container) {
      VEGLStep4Node.superclass.constructor.apply(this,
        [container, "VEGL - Step4", "VEGLStep4", "s"]
    );

    var numShells = container.getShellCommands().length;
    this.values.uniqueName = "VEGLStep4-i" + numShells;
  },

  getScript: function() {
      var text = '';

      if (this.values.invType === "grav") {
        text += this._tab + "# If we have a gravity inversion, we need to correct the units of the supplied gravity data." + this._newLine;
        text += this._tab + "# National gravity coverages are in units of micrometres per second squared." + this._newLine;
        text += this._tab + "# UBC-GIF gravity inversion expects milliGals, which means we divide supplied properties by 10." + this._newLine;
        text += this._tab + "#" + this._newLine;
        text += this._tab + "i = 0" + this._newLine;
        text += this._tab + "for east,north,prop in data:" + this._newLine;
        text += this._tab + this._tab + "data[i] = east,north,prop/10" + this._newLine;
        text += this._tab + this._tab + "i = i + 1" + this._newLine + this._newLine;
      } else if (this.values.invType === "mag") {
        text += this._tab + "# If we have a magnetic inversion, we need to define the magnetic field properties." + this._newLine;
        text += this._tab + "# General convention is to assign the magnetic properties associated with the" + this._newLine;
        text += this._tab + "# middle of the inversion area." + this._newLine;
        text += this._tab + "#" + this._newLine;
        text += this._tab + "central_lat = (VEGLSelectedBox.getMaxNorthing() - VEGLSelectedBox.getMinNorthing()) / 2" + this._newLine;
        text += this._tab + "central_lon = (VEGLSelectedBox.getMaxEasting() - VEGLSelectedBox.getMinEasting()) / 2" + this._newLine;
        text += this._tab + "declination,inclination,intensity = get_mag_field_data(central_lat, central_lon,2010,01,01)" + this._newLine +  this._newLine;
      }

      return text;
    }
});

