/*
 * This file is part of the AuScope Virtual Exploration Geophysics Lab (VEGL) project.
 * Copyright (c) 2011 CSIRO Earth Science and Resource Engineering
 *
 * Licensed under the terms of the GNU Lesser General Public License.
 */

Ext.define('ScriptBuilder.components.VEGLStep2', {
    extend : 'ScriptBuilder.components.BasePythonComponent',

    constructor: function(config) {
        Ext.apply(config, {
            bodyStyle: "padding:5px;",
            labelWidth: 150,
            defaults: { anchor: "100%" },
            items: [{
                xtype: "textfield",
                name: "paramsObj",
                value: "VEGLParams",
                fieldLabel: "Python VEGL Parameters Class",
                allowBlank: false
            }]
        });

        this.callParent(arguments);
    },

    getScript: function() {
        var text = '';
        var values = this.getValues();

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

