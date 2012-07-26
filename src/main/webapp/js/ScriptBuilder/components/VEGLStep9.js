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
                xtype: 'radiogroup',
                columns : 1,
                fieldLabel: 'Inversion Type',
                items : [{
                    xtype      : 'radio',
                    boxLabel   : 'Gravity',
                    name       : 'invType',
                    inputValue : 'grav'
                },{
                    xtype          : 'radio',
                    boxLabel       : 'Magnetic',
                    name           : 'invType',
                    inputValue     : 'mag'
                }]
            },{
                xtype: 'radiogroup',
                columns : 1,
                fieldLabel: 'Results Upload',
                items : [{
                    xtype      : 'radio',
                    boxLabel   : 'Upload all model files',
                    name       : 'uploadType',
                    inputValue : 'model'
                },{
                    xtype      : 'radio',
                    boxLabel   : 'Upload all model and predicted files',
                    name       : 'uploadType',
                    inputValue : 'all'
                },{
                    xtype      : 'radio',
                    boxLabel   : 'Upload final model file',
                    name       : 'uploadType',
                    inputValue : 'finalModel'
                },{
                    xtype      : 'radio',
                    boxLabel   : 'Upload final model and predicted files',
                    name       : 'uploadType',
                    inputValue : 'finalAll'
                }]
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

        //The final file upload is a little tricky and depends on user selection
        //There will be a ton of files matching - gzinv3d_XXX.den, gzinv3d_XXX.pre where XXX is a number from [0-999]
        //We only upload a subset of the files depending on configuration
        text += this._tab + "# Upload gravity or magnetic data file" + this._newLine;
        text += this._tab + "denFiles = glob.glob('*zinv3d*.den')" + this._newLine;
        text += this._tab + "preFiles = glob.glob('*zinv3d*.pre')" + this._newLine;

        switch(values.uploadType) {
        case 'model':
            text += this._tab + "# Upload All Models" + this._newLine;
            text += this._tab + "print 'Uploading all model files'" + this._newLine;
            text += this._tab + "invFilesToUpload = denFiles" + this._newLine;
            break;
        case 'all':
            text += this._tab + "# Upload everything" + this._newLine;
            text += this._tab + "print 'Uploading all model and prediction files'" + this._newLine;
            text += this._tab + "denFiles.extend(preFiles)" + this._newLine;
            text += this._tab + "invFilesToUpload = denFiles" + this._newLine;
            break;
        case 'finalModel':
            text += this._tab + "# Upload Final Model" + this._newLine;
            text += this._tab + "print 'Uploading final model file'" + this._newLine;
            text += this._tab + "invFilesToUpload = []" + this._newLine;
            text += this._tab + "if len(denFiles) > 0:" + this._newLine;
            text += this._tab + this._tab + "denFiles.sort()" + this._newLine;
            text += this._tab + this._tab + "invFilesToUpload.append(denFiles[len(denFiles) - 1])" + this._newLine;
            break;
        case 'finalAll':
            text += this._tab + "# Upload Final Model + Prediction" + this._newLine;
            text += this._tab + "print 'Uploading final model and prediction'" + this._newLine;
            text += this._tab + "invFilesToUpload = []" + this._newLine;
            text += this._tab + "if len(denFiles) > 0:" + this._newLine;
            text += this._tab + this._tab + "denFiles.sort()" + this._newLine;
            text += this._tab + this._tab + "invFilesToUpload.append(denFiles[len(denFiles) - 1])" + this._newLine;
            text += this._tab + "if len(preFiles) > 0:" + this._newLine;
            text += this._tab + this._tab + "preFiles.sort()" + this._newLine;
            text += this._tab + this._tab + "invFilesToUpload.append(preFiles[len(preFiles) - 1])" + this._newLine;
            break;
        }

        text += this._tab + "print 'About to upload the following files:'" + this._newLine;
        text += this._tab + "print invFilesToUpload" + this._newLine;
        text += this._tab + "for invFile in invFilesToUpload:" + this._newLine;
        text += this._tab + this._tab + "cloudUpload(invFile, " + values.paramsInstance + ".getStorageBucket(), " + values.paramsInstance + ".getStorageBaseKey(), invFile)" + this._newLine;
        text += this._newLine;
        return text;
    }
});

