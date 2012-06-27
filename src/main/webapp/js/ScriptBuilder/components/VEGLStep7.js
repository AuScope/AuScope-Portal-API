/*
 * This file is part of the AuScope Virtual Exploration Geophysics Lab (VEGL) project.
 * Copyright (c) 2011 CSIRO Earth Science and Resource Engineering
 *
 * Licensed under the terms of the GNU Lesser General Public License.
 */

Ext.define('ScriptBuilder.components.VEGLStep7', {
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

        text += this._tab + "# Step 7: Write out sensitivity analsysis control file" + this._newLine;
        text += this._tab + "# --- Scientific description below ---" + this._newLine;
        text += this._tab + "# There are two parts to running a UBC-GIF inversion. The first involves a sensitivity analysis;" + this._newLine;
        text += this._tab + "# here we write out the appropriate control files for this analysis." + this._newLine;
        text += this._tab + "# File names for things defined outside this method are defined at the top" + this._newLine;
        text += this._tab + "obs_file = 'temp_ubc_obs.asc'" + this._newLine;
        text += this._tab + "mesh = 'mesh.msh'" + this._newLine;
        text += this._tab + "# Sensitivity analysis (*sen3d_MPI) input file" + this._newLine;
        text += this._tab + "sns_inp = 'sens.inp'" + this._newLine + this._newLine;

        text += this._tab + "# Write some files" + this._newLine;
        text += this._tab + "try:" + this._newLine;
        text += this._tab + this._tab + "f = file(sns_inp, 'w')" + this._newLine;
        text += this._tab + this._tab + "f.write(mesh + ' ! mesh\\n')" + this._newLine;
        text += this._tab + this._tab + "f.write(obs_file + ' ! observations file\\n')" + this._newLine;
        text += this._tab + this._tab + "f.write('null ! topography\\n')" + this._newLine;
        text += this._tab + this._tab + "f.write('2 ! iwt\\n')" + this._newLine;
        text += this._tab + this._tab + "f.write('null ! beta, znot\\n')" + this._newLine;
        text += this._tab + this._tab + "f.write('daub2 ! wavelet\\n')" + this._newLine;
        text += this._tab + this._tab + "f.write('2 1e-4 ! itol eps\\n')" + this._newLine;
        text += this._tab + this._tab + "f.close()" + this._newLine + this._newLine;
        text += this._tab + "except IOError, e:" + this._newLine;
        text += this._tab + this._tab + "print e" + this._newLine;
        text += this._tab + this._tab + "sys.exit(1)" + this._newLine  + this._newLine;

        return text;
    }
});

