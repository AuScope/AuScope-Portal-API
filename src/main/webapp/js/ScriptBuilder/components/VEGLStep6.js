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

    text += this._tab + "# Step 6: calculate some meshy stuff" + this._newLine;
    text += this._tab + "# --- Scientific description below ---" + this._newLine;
    text += this._tab + "# Defines the mesh parameters and writes out a UBC-GIF mesh file." + this._newLine;
    text += this._tab + "# Mesh is defined by the minimum and maximum eastings and northings, inversion depth, and respective cell sizes." + this._newLine;
    text += this._tab + "# Mesh file name: 'mesh'" + this._newLine;
    text += this._tab + "VEGLParams = VEGLParameters()" + this._newLine;
    text += this._tab + "VEGLPaddedBox = VEGLParams.getPaddedBounds()" + this._newLine;
    text += this._tab + "minEasting = VEGLPaddedBox.getMinEasting()" + this._newLine;
    text += this._tab + "maxEasting = VEGLPaddedBox.getMaxEasting()" + this._newLine;
    text += this._tab + "minNorthing = VEGLPaddedBox.getMinNorthing()" + this._newLine;
    text += this._tab + "maxNorthing = VEGLPaddedBox.getMaxNorthing()" + this._newLine;
    text += this._tab + "invDepth = VEGLParams.getInversionDepth()" + this._newLine;
    text += this._tab + "cell_sizes = VEGLParams.getCellSize()" + this._newLine;
    text += this._tab + "cell_x = cell_sizes['x']" + this._newLine;
    text += this._tab + "cell_y = cell_sizes['y']" + this._newLine;
    text += this._tab + "cell_z = cell_sizes['z']" + this._newLine;
    text += this._tab + "num_x_cells = int((maxEasting - minEasting) / cell_x)" + this._newLine;
    text += this._tab + "num_y_cells = int((maxNorthing - minNorthing) / cell_y)" + this._newLine;
    text += this._tab + "num_z_cells = int(invDepth / cell_z)" + this._newLine;
    text += this._tab + "print 'Number of cells in x dimension: ' + str(num_x_cells) + ', number of cells in y dimension: ' + str(num_y_cells) + ' and number of cells in z dimension: ' + str(num_z_cells)" + this._newLine;
    text += this._tab + "# Define mesh file name here" + this._newLine;
    text += this._tab + "mesh = 'mesh.msh'" + this._newLine;
    text += this._tab + "try:" + this._newLine;
    text += this._tab + this._tab + "f = file(mesh, 'w')" + this._newLine;
    text += this._tab + this._tab + "f.write(str(num_x_cells) + ' ' + str(num_y_cells) + ' ' + str(num_z_cells) + '\n')" + this._newLine;
    text += this._tab + this._tab + "f.write(str(minEasting) + ' ' + str(minNorthing) + ' 0\n')" + this._newLine;
    text += this._tab + this._tab + "f.write(str(num_x_cells) + '*' + str(cell_x) + '\n')" + this._newLine;
    text += this._tab + this._tab + "f.write(str(num_y_cells) + '*' + str(cell_y) + '\n')" + this._newLine;
    text += this._tab + this._tab + "f.write(str(num_z_cells) + '*' + str(cell_z))" + this._newLine;
    text += this._tab + this._tab + "f.close()" + this._newLine;
    text += this._tab + "except IOError, e:" + this._newLine;
    text += this._tab + this._tab + "print e" + this._newLine;
    text += this._tab + this._tab + "sys.exit(1)" + this._newLine  + this._newLine;

    return text;
  }
});

