/*
 * This file is part of the AuScope Virtual Exploration Geophysics Lab (VEGL) project.
 * Copyright (c) 2011 CSIRO Earth Science and Resource Engineering
 *
 * Licensed under the terms of the GNU Lesser General Public License.
 */

Ext.namespace("ScriptBuilder");
Ext.ux.ComponentLoader.load({url: ScriptBuilder.componentPath+"VEGLStep8.json"});

VEGLStep8Node = Ext.extend(ScriptBuilder.BasePythonComponent, {
  constructor: function(container) {
      VEGLStep8Node.superclass.constructor.apply(this,
        [container, "VEGL - Step8", "VEGLStep8", "s"]
    );

    var numShells = container.getShellCommands().length;
    this.values.uniqueName = "VEGLStep8-i"+numShells;
  },

  getScript: function() {
    var text = '';
    var filename = '';

    if (this.values.invType === "grav") {
        filename = 'gzinv3d.mtx';
    } else {
        filename = 'mzinv3d.mtx';
    }

    text += this._tab + "# Step 8: Write out inversion control file" + this._newLine;
    text += this._tab + "# --- Scientific description below ---" + this._newLine;
    text += this._tab + "# In the second part to running a UBC-GIF inversion, we need to write out" + this._newLine;
    text += this._tab + "# the control file for the actual inversion." + this._newLine;
    text += this._tab + "# File names for things defined outside this method are defined at the top" + this._newLine;
    text += this._tab + "obs_file = 'temp_ubc_obs.asc'" + this._newLine;
    text += this._tab + "inv_inp = 'inv.inp'" + this._newLine;
    text += this._tab + "try:" + this._newLine;
    text += this._tab + this._tab + "f = file(inv_inp, 'w')" + this._newLine;
    text += this._tab + this._tab + "f.write('0\t!irest\n')" + this._newLine;
    text += this._tab + this._tab + "f.write('1\t!mode\n')" + this._newLine;
    text += this._tab + this._tab + "f.write('1  0.02\t!par tolc\n')" + this._newLine;
    text += this._tab + this._tab + "f.write(obs_file + '\t! observations file\n')" + this._newLine;
    text += this._tab + this._tab + "# file name dependant on type set in JS" + this._newLine;
    text += this._tab + this._tab + "f.write('" + this.filename + "\n')" + this._newLine;
    text += this._tab + this._tab + "f.write('null\t!initial model\n')" + this._newLine;
    text += this._tab + this._tab + "f.write('null\t!reference model\n')" + this._newLine;
    text += this._tab + this._tab + "f.write('null\t!active cell file\n')" + this._newLine;
    text += this._tab + this._tab + "f.write('null\t!lower, upper bounds\n')" + this._newLine;
    text += this._tab + this._tab + "f.write('null\tLe, Ln, Lz\n')" + this._newLine;
    text += this._tab + this._tab + "f.write('SMOOTH_MOD_DIF\n')" + this._newLine;
    text += this._tab + this._tab + "f.write('null\t!weighting file\n')" + this._newLine;
    text += this._tab + this._tab + "f.write('0\n')" + this._newLine;
    text += this._tab + this._tab + "f.close()" + this._newLine;
    text += this._tab + "except IOError, e:" + this._newLine;
    text += this._tab + this._tab + "print e" + this._newLine;
    text += this._tab + this._tab + "sys.exit(1)" + this._newLine + this._newLine;

    return text;
  }
});

