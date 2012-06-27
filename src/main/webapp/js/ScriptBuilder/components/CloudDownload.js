/*
 * This file is part of the AuScope Virtual Exploration Geophysics Lab (VEGL) project.
 * Copyright (c) 2011 CSIRO Earth Science and Resource Engineering
 *
 * Licensed under the terms of the GNU Lesser General Public License.
 */
Ext.define('ScriptBuilder.components.CloudDownload', {
    extend : 'ScriptBuilder.components.BaseComponent',

    constructor: function(config) {
        Ext.apply(config, {
            bodyStyle: "padding:5px;",
            labelWidth: 150,
            defaults: { anchor: "100%" },
            items: [{
              xtype: "textfield",
              name: "bucketName",
              value: "vegl-portal",
              fieldLabel: "S3 bucket",
              allowBlank: false
            },{
              xtype: "textfield",
              name: "keyPath",
              value: "",
              fieldLabel: "S3 Key Path",
              allowBlank: false
            },{
              xtype: "textfield",
              name: "outputFilePath",
              value: "",
              fieldLabel: "Output filepath",
              allowBlank: false
            }]
        });

        this.callParent(arguments);
    },

    getScript: function() {
        var values = this.getValues();

        var queryPath = values.bucketName + "/" + values.keyPath;
        queryPath = queryPath.replace(/\/\/*/g, " ");

        return 'cloud download "' + queryPath + '" > "' + values.outputFilePath + '"\n';
    }

});

