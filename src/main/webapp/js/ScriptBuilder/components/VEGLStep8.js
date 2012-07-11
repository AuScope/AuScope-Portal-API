/*
 * This file is part of the AuScope Virtual Exploration Geophysics Lab (VEGL) project.
 * Copyright (c) 2011 CSIRO Earth Science and Resource Engineering
 *
 * Licensed under the terms of the GNU Lesser General Public License.
 */

Ext.define('ScriptBuilder.components.VEGLStep8', {
    extend : 'ScriptBuilder.components.BasePythonComponent',

    constructor: function(config) {
        Ext.apply(config, {
            bodyStyle: "padding:5px;",
            labelWidth: 150,
            defaults: { anchor: "100%" },
            items: [{
                xtype      : 'radio',
                fieldLabel : "",
                boxLabel   : 'Gravity',
                name       : 'invType',
                inputValue : 'grav'
            },{
                xtype          : 'radio',
                fieldLabel     : "",
                labelSeparator : ' ',
                boxLabel       : 'Magnetic',
                name           : 'invType',
                inputValue     : 'mag'
            }]
        });

        this.callParent(arguments);
    },

    getScript: function() {
        var text = '';
        var filename = '';
        var values = this.getValues();

        if (values.invType === "grav") {
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
        text += this._tab + this._tab + "f.write('0 !irest\\n')" + this._newLine;
        text += this._tab + this._tab + "f.write('1 !mode\\n')" + this._newLine;
        text += this._tab + this._tab + "f.write('1  0.02 !par tolc\\n')" + this._newLine;
        text += this._tab + this._tab + "f.write(obs_file + ' ! observations file\\n')" + this._newLine;
        text += this._tab + this._tab + "# file name dependant on type set in JS" + this._newLine;
        text += this._tab + this._tab + "f.write('" + filename + "\\n')" + this._newLine;
        text += this._tab + this._tab + "f.write('null !initial model\\n')" + this._newLine;
        text += this._tab + this._tab + "f.write('null !reference model\\n')" + this._newLine;
        text += this._tab + this._tab + "f.write('null !active cell file\\n')" + this._newLine;
        text += this._tab + this._tab + "f.write('null !lower, upper bounds\\n')" + this._newLine;
        text += this._tab + this._tab + "f.write('null Le, Ln, Lz\\n')" + this._newLine;
        text += this._tab + this._tab + "f.write('SMOOTH_MOD\\n')" + this._newLine;
        text += this._tab + this._tab + "f.write('null !weighting file\\n')" + this._newLine;
        text += this._tab + this._tab + "f.write('0\\n')" + this._newLine;
        text += this._tab + this._tab + "f.close()" + this._newLine;
        text += this._tab + "except IOError, e:" + this._newLine;
        text += this._tab + this._tab + "print e" + this._newLine;
        text += this._tab + this._tab + "sys.exit(1)" + this._newLine + this._newLine;

        return text;
    }
});

