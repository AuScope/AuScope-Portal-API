/*
 * This file is part of the AuScope Virtual Exploration Geophysics Lab (VEGL) project.
 * Copyright (c) 2011 CSIRO Earth Science and Resource Engineering
 *
 * Licensed under the terms of the GNU Lesser General Public License.
 */

Ext.namespace("ScriptBuilder");
Ext.ux.ComponentLoader.load({url: ScriptBuilder.componentPath+"VEGLStep2.json"});

VEGLStep2Node = Ext.extend(ScriptBuilder.BasePythonComponent, {
  constructor: function(container) {
    VEGLStep2Node.superclass.constructor.apply(this,
      [container, "VEGL - Step2", "VEGLStep2", "s"]
    );

    var numShells = container.getShellCommands().length;
    this.values.uniqueName = "VEGLStep2-i"+numShells;
  },

  getScript: function() {
    var text = '';

    text = this._tab + 'VEGLPaddedBox = VEGLParams.getPaddedBounds()' + this._newLine;
    text += this._tab + 'zone = int(VEGLPaddedBox.getSrs())' + this._newLine;
    text += this._tab + 'temp_data = []' + this._newLine;
    text += this._tab + 'for x, y, z in data:' + this._newLine;
    text += this._tab + this._tab +'newX, newY = project(x, y, zone)' + this._newLine;
    text += this._tab + this._tab +'temp_data.append([newX, newY, z])' + this._newLine;
    text += this._tab + 'data = temp_data' + this._newLine;
    text += this._newLine;

    return text;
  }
});

