/*
 * This file is part of the AuScope Virtual Exploration Geophysics Lab (VEGL) project.
 * Copyright (c) 2011 CSIRO Earth Science and Resource Engineering
 *
 * Licensed under the terms of the GNU Lesser General Public License.
 */

Ext.define('ScriptBuilder.components.MPIRun', {
    extend : 'ScriptBuilder.components.BaseComponent',

    constructor: function(config) {
        Ext.apply(config, {
            bodyStyle: "padding:5px;",
            labelWidth: 150,
            defaults: { anchor: "100%" },
            items: [{
                bodyStyle: "padding:5px;",
                labelWidth: 150,
                defaults: { anchor: "100%" },
                items: [{
                    xtype: "numberfield",
                    name: "numProcessors",
                    value: "4",
                    fieldLabel: "Number of processors",
                    decimalPrecision: 0,
                    allowNegative: false,
                    allowBlank: false
                },{
                    xtype: "textfield",
                    name: "executable",
                    value: "/opt/ubc/gzsen3d_MPI",
                    fieldLabel: "Program Executable",
                    allowBlank: false
                },{
                    xtype: "textfield",
                    name: "mcaArgs",
                    value: "btl self,sm",
                    fieldLabel: "MCA Arguments (key value)",
                    allowBlank: false
                },{
                    xtype: "textfield",
                    name: "programArgs",
                    value: "${EXAMPLE_DATA_DIR}/grav_sns.inp",
                    fieldLabel: "Program Arguments",
                    allowBlank: false
                }]
            }]
        });

        this.callParent(arguments);
    },

    getScript: function() {
        var values = this.getValues();
        return "mpirun -np " + values.numProcessors +
               " --mca " + values.mcaArgs +
               " \"" + values.executable + "\"" +
               " \"" + values.programArgs + "\"\n";
    }
});

