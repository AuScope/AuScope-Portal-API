/*
 * This file is part of the AuScope Virtual Exploration Geophysics Lab (VEGL) project.
 * Copyright (c) 2011 CSIRO Earth Science and Resource Engineering
 *
 * Licensed under the terms of the GNU Lesser General Public License.
 */

Ext.define('ScriptBuilder.components.VEGLStep9', {
    extend : 'ScriptBuilder.components.BasePythonComponent',

    constructor: function(config) {
        Ext.apply(config, {
            bodyStyle: "padding:5px;",
            labelWidth: 150,
            defaults: { anchor: "100%" },
            items: [{
                xtype: "label",
                text: "This step is dependent on the AWS Util Functions step. Please ensure that the AWS Utils component is included.",
            },{
                xtype: "textfield",
                name: "paramsInstance",
                value: "VEGLParams",
                fieldLabel: "Python VEGL Parameters Instance",
                allowBlank: false
            },{
                xtype: "numberfield",
                name: "numProcessors",
                value: "4",
                fieldLabel: "Number of processors",
                decimalPrecision: 0,
                allowNegative: false,
                allowBlank: false
            },{
                xtype: "textfield",
                name: "mcaArgs",
                value: "btl self,sm",
                fieldLabel: "MCA Arguments (key value)",
                allowBlank: false
            },{
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

    getMPIRunCommand: function(numProcessors, mcaArgs, executable, programArgs, outputFile) {
        return "'mpirun -np " + numProcessors + " --mca " + mcaArgs + " \"" + executable + "\"" + " \"" + programArgs + "\" > \"" + outputFile + "\"'";
    },

    getScript: function() {
        var text = '';
        var filename = '';
        var values = this.getValues();

        if (values.invType === "grav") {
            filename = 'gzinv3d.den';
        } else {
            filename = 'mzinv3d.den';
        }

        text += this._tab + "# step 9: finalise stuff - I guess this is where we execute two commands" + this._newLine;
        text += this._tab + "# At a guess, they are the two commented-out lines below?" + this._newLine;
        text += this._tab + "# Control files, defined elsewhere" + this._newLine;
        text += this._tab + "sns_inp = 'sens.inp'" + this._newLine;
        text += this._tab + "sns_out = 'sens.out'" + this._newLine;
        text += this._tab + "inv_inp = 'inv.inp'" + this._newLine;
        text += this._tab + "inv_out = 'inv.out'" + this._newLine;
        text += this._tab + "sensitivity_command = " + "'mpirun -np " + values.numProcessors + " --mca " + values.mcaArgs + " /opt/ubc/gzsen3d_MPI ' + sns_inp + ' > ' + sns_out" + this._newLine;
        text += this._tab + "inversion_command = " + "'mpirun -np " + values.numProcessors + " --mca " + values.mcaArgs + " /opt/ubc/gzinv3d_MPI ' + inv_inp + ' > ' + inv_out" + this._newLine;
        text += this._tab + "print 'Sensitivity command: ' + sensitivity_command" + this._newLine;
        text += this._tab + "print 'Inversion command: ' + inversion_command" + this._newLine;
        text += this._tab + "sys.stdout.flush()" + this._newLine;
        text += this._tab + "retcode = subprocess.call(sensitivity_command, shell=True)" + this._newLine;
        text += this._tab + "print 'sensitivity returned: ' + str(retcode)" + this._newLine;
        text += this._tab + "sys.stdout.flush()" + this._newLine;
        text += this._tab + "retcode = subprocess.call(inversion_command, shell=True)" + this._newLine;
        text += this._tab + "print 'inversion returned: ' + str(retcode)" + this._newLine;
        text += this._tab + "sys.stdout.flush()" + this._newLine;
        text += this._tab + "# Upload our logging outs" + this._newLine;
        text += this._tab + "cloudUpload(sns_out, " + values.paramsInstance + ".getStorageBucket(), " + values.paramsInstance + ".getStorageBaseKey(), sns_out)" + this._newLine;
        text += this._tab + "cloudUpload(inv_out, " + values.paramsInstance + ".getStorageBucket(), " + values.paramsInstance + ".getStorageBaseKey(), inv_out)" + this._newLine;
        text += this._tab + "# Upload the mesh file" + this._newLine;
        text += this._tab + "cloudUpload(mesh, " + values.paramsInstance + ".getStorageBucket(), " + values.paramsInstance + ".getStorageBaseKey(), mesh)" + this._newLine;
        text += this._tab + "# Upload gravity or magnetic data file" + this._newLine;
        text += this._tab + "cloudUpload('" + filename + "', " + values.paramsInstance + ".getStorageBucket(), " + values.paramsInstance + ".getStorageBaseKey(), '" + filename + "')" + this._newLine;
        text += this._newLine;
        return text;
    }
});

