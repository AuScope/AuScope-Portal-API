/*
 * This file is part of the AuScope Virtual Exploration Geophysics Lab (VEGL) project.
 * Copyright (c) 2011 CSIRO Earth Science and Resource Engineering
 *
 * Licensed under the terms of the GNU Lesser General Public License.
 */

Ext.namespace("ScriptBuilder");
Ext.ux.ComponentLoader.load({url: ScriptBuilder.componentPath+"VEGLStep3.json"});

VEGLStep3Node = Ext.extend(ScriptBuilder.BasePythonComponent, {
  constructor: function(container) {
    VEGLStep3Node.superclass.constructor.apply(this,
        [container, "VEGL - Step3", "VEGLStep3", "s"]
    );

    var numShells = container.getShellCommands().length;
    this.values.uniqueName = "VEGLStep3-i"+numShells;
  },

  getScript: function() {
    var text = '';

    text = this._tab + 'temp_data = []' + this._newLine;
    text += this._tab + 'for x, y, z in data:' + this._newLine;
    text += this._tab + this._tab + '# isPointInsideArea happens to read northings then eastings, and we store' + this._newLine;
    text += this._tab + this._tab + '# northings as y, and eastings as' + this._newLine;
    text += this._tab + this._tab + 'if VEGLPaddedBox.isPointInsideArea(y,x):' + this._newLine;
    text += this._tab + this._tab + this._tab + 'temp_data.append([x,y,z])' + this._newLine;
    text += this._tab + this._tab + 'data = temp_data' + this._newLine;
    text += this._newLine;
    return text;
  }
});

