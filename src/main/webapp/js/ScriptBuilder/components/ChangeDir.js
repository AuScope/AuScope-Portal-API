/*
 * This file is part of the AuScope Virtual Exploration Geophysics Lab (VEGL) project.
 * Copyright (c) 2011 CSIRO Earth Science and Resource Engineering
 *
 * Licensed under the terms of the GNU Lesser General Public License.
 */

Ext.define('ScriptBuilder.components.ChangeDir', {
    extend : 'ScriptBuilder.components.BaseComponent',

    constructor: function(config) {
        Ext.apply(config, {
            bodyStyle: "padding:5px;",
            labelWidth: 150,
            defaults: { anchor: "100%" },
            items: [{
                xtype: "textfield",
                name: "directory",
                fieldLabel: "Directory",
                allowBlank: false
            }]
        });

        this.callParent(arguments);
    },

    getScript: function() {
        return 'cd "' + this.getValues().directory + '"\n';
    }
});

