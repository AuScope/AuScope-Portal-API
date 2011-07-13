/*
 * This file is part of the AuScope Virtual Exploration Geophysics Lab (VEGL) project.
 * Copyright (c) 2011 CSIRO Earth Science and Resource Engineering
 *
 * Licensed under the terms of the GNU Lesser General Public License.
 */

Ext.namespace("ScriptBuilder");
Ext.ux.ComponentLoader.load({url: ScriptBuilder.componentPath+"VEGLStep1.json"});

VEGLStep1Node = Ext.extend(ScriptBuilder.BasePythonComponent, {
  constructor: function(container) {
      VEGLStep1Node.superclass.constructor.apply(this,
        [container, "VEGL - Step1", "VEGLStep1", "s"]
    );

    var numShells = container.getShellCommands().length;
    this.values.uniqueName = "VEGLStep1-i"+numShells;
  },

  getScript: function() {
    var text = '';
    
    text += this._tab + '# ------------ VEGL - Step 1 ---------' + this._newLine;
    text += this._tab + 'f = file(' + this.values.paramsInstance + '.getVmSubsetFilePath(), "r")' + this._newLine; 
    text += this._tab + 'input_csv = csv.reader(f)' + this._newLine;
    text += this._tab + 'data = []' + this._newLine;
    text += this._tab + 'lineCount = 0 # The first 2 lines contain text and must be skipped' + this._newLine;
    text += this._tab + 'for strX, strY, strZ in input_csv:' + this._newLine;
    text += this._tab + this._tab + 'if lineCount > 1:' + this._newLine;
    text += this._tab + this._tab + this._tab + 'x = float(strX)' + this._newLine;
    text += this._tab + this._tab + this._tab + 'y = float(strY)' + this._newLine;
    text += this._tab + this._tab + this._tab + 'z = float(strZ)' + this._newLine;
    text += this._tab + this._tab + this._tab + 'data.append([x,y,z])' + this._newLine;
    text += this._tab + this._tab + 'lineCount = lineCount + 1' + this._newLine;
    text += this._newLine;
    text += this._tab + '# ------------------------------------' + this._newLine;;
    text += this._newLine;
    
    return text;
  }
});

