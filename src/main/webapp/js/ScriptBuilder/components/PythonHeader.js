/*
 * This file is part of the AuScope Virtual Exploration Geophysics Lab (VEGL) project.
 * Copyright (c) 2011 CSIRO Earth Science and Resource Engineering
 *
 * Licensed under the terms of the GNU Lesser General Public License.
 */
Ext.define('ScriptBuilder.components.PythonHeader', {
    extend : 'ScriptBuilder.components.BasePythonComponent',

    constructor: function(config) {
        Ext.apply(config, {
            bodyStyle: "padding:5px;",
            labelWidth: 150,
            defaults: { anchor: "100%" },
            items: [{
                xtype: "label",
                text: "Press OK to load the script header."
            }]
        });

        this.callParent(arguments);
    },

    /**
     * We generate a static def main() function
     */
    getScript : function() {
        var scriptHeader = "\
#!/usr/bin/env python\n\n\
# VEGL processing script.\n\
# Please load the Job Object before you load other components\n\n\
import subprocess, csv, math, os, sys, urllib, glob;\n\n";

        return scriptHeader;
    }
});
