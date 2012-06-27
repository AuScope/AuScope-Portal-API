/*
 * This file is part of the AuScope Virtual Exploration Geophysics Lab (VEGL) project.
 * Copyright (c) 2011 CSIRO Earth Science and Resource Engineering
 *
 * Licensed under the terms of the GNU Lesser General Public License.
 */

Ext.define('ScriptBuilder.components.VEGLStep3', {
    extend : 'ScriptBuilder.components.BasePythonComponent',

    constructor: function(config) {
        Ext.apply(config, {
            bodyStyle: "padding:5px;",
            labelWidth: 150,
            defaults: { anchor: "100%" },
            items: [{
                xtype: "label",
                text: "Press OK to load."
            }]
        });

        this.callParent(arguments);
    },

    getScript: function() {
        var text = '';

        text = this._tab + 'temp_data = []' + this._newLine;
        text += this._tab + 'for x, y, z in data:' + this._newLine;
        text += this._tab + this._tab + '# isPointInsideArea happens to read northings then eastings, and we store' + this._newLine;
        text += this._tab + this._tab + '# northings as y, and eastings as' + this._newLine;
        text += this._tab + this._tab + 'if VEGLPaddedBox.isPointInsideArea(y,x):' + this._newLine;
        text += this._tab + this._tab + this._tab + 'temp_data.append([x,y,z])' + this._newLine;
        text += this._tab + 'data = temp_data' + this._newLine;
        text += this._newLine;
        return text;
    }
});

