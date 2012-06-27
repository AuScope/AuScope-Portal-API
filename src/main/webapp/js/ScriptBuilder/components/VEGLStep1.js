/*
 * This file is part of the AuScope Virtual Exploration Geophysics Lab (VEGL) project.
 * Copyright (c) 2011 CSIRO Earth Science and Resource Engineering
 *
 * Licensed under the terms of the GNU Lesser General Public License.
 */

Ext.define('ScriptBuilder.components.VEGLStep1', {
    extend : 'ScriptBuilder.components.BasePythonComponent',

    constructor: function(config) {
        Ext.apply(config, {
            bodyStyle: "padding:5px;",
            labelWidth: 150,
            defaults: { anchor: "100%" },
            items: [{
                xtype: "textfield",
                name: "paramsInstance",
                value: "VEGLParams",
                fieldLabel: "Python VEGL Parameters Instance",
                allowBlank: false
            }]
        });

        this.callParent(arguments);
    },

    getScript: function() {
        var text = '';
        var values = this.getValues();

        text += this._tab + '# ------------ VEGL - Step 1 ---------' + this._newLine;
        text += this._tab + 'f = file(' + values.paramsInstance + '.getVmSubsetFilePath(), "r")' + this._newLine;
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

