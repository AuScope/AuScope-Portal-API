/*
 * This file is part of the AuScope Virtual Exploration Geophysics Lab (VEGL) project.
 * Copyright (c) 2011 CSIRO Earth Science and Resource Engineering
 *
 * Licensed under the terms of the GNU Lesser General Public License.
 */

Ext.namespace("ScriptBuilder");
Ext.ux.ComponentLoader.load({url: ScriptBuilder.componentPath+"VEGLStep9.json"});

VEGLStep9Node = Ext.extend(ScriptBuilder.BasePythonComponent, {
  constructor: function(container) {
      VEGLStep9Node.superclass.constructor.apply(this,
        [container, "VEGL - Step9", "VEGLStep9", "s"]
    );

    var numShells = container.getShellCommands().length;
    this.values.uniqueName = "VEGLStep9-i"+numShells;
  },

  getScript: function() {
    var text = '';

    text += this._tab + "# step 9: finalise stuff - I guess this is where we execute two commands" + this._newLine;
    text += this._tab + "# At a guess, they are the two commented-out lines below?" + this._newLine;
    text += this._tab + "# Control files, defined elsewhere" + this._newLine;
    text += this._tab + "sns_inp = 'sens.inp'" + this._newLine;
    text += this._tab + "sns_out = 'sens.out'" + this._newLine;
    text += this._tab + "inv_inp = 'inv.inp'" + this._newLine;
    text += this._tab + "inv_out = 'inv.out'" + this._newLine;
    text += this._tab + "sensitivity_command = 'gzsen3d_MPI ' + sns_inp + ' > ' + sns_out" + this._newLine;
    text += this._tab + "inversion_command = 'gzinv3d_MPI ' + inv_inp + ' > ' + inv_out" + this._newLine;
    text += this._tab + "# exec(sensitivity_command)" + this._newLine;
    text += this._tab + "# exec(inversion_command)" + this._newLine;
    text += this._tab + "print 'Sensitivity command: ' + sensitivity_command" + this._newLine;
    text += this._tab + "print 'Inversion command: ' + inversion_command" + this._newLine;

    return text;
  }
});

