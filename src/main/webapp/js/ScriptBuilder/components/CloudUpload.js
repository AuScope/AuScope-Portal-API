/*
 * This file is part of the AuScope Virtual Exploration Geophysics Lab (VEGL) project.
 * Copyright (c) 2011 CSIRO Earth Science and Resource Engineering
 *
 * Licensed under the terms of the GNU Lesser General Public License.
 */
Ext.define('ScriptBuilder.components.CloudUpload', {
    extend : 'ScriptBuilder.components.BaseComponent',

    constructor: function(config) {
        Ext.apply(config, {
            bodyStyle: "padding:5px;",
            labelWidth: 150,
            defaults: { anchor: "100%" },
            items: [{
                xtype: "textfield",
                name: "inputFilePath",
                value: "",
                fieldLabel: "Input filepath",
                allowBlank: false
              },{
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
              }]
        });

        this.callParent(arguments);
    },

    getScript: function() {
        var values = this.getValues();
        var queryPath = values.bucketName + "/" + values.keyPath;
        queryPath = queryPath.replace(/\/\/*/g, " ");

        var ret = 'QUERY_PATH=`echo "' + queryPath + '" | sed "s/\\/\\/*/\\//g"`\n';
        ret += 'cloud upload "${QUERY_PATH}" "' + values.inputFilePath + '" --set-acl=public-read\n';
        return ret;
    }

});
