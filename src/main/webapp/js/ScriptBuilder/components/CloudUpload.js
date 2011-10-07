/*
 * This file is part of the AuScope Virtual Exploration Geophysics Lab (VEGL) project.
 * Copyright (c) 2011 CSIRO Earth Science and Resource Engineering
 *
 * Licensed under the terms of the GNU Lesser General Public License.
 */

Ext.namespace("ScriptBuilder");
Ext.ux.ComponentLoader.load({url: ScriptBuilder.componentPath+"CloudUpload.json"});

CloudUploadNode = Ext.extend(ScriptBuilder.BaseComponent, {
  constructor: function(container) {
    CloudUploadNode.superclass.constructor.apply(this,
        [container, "Cloud - upload", "CloudUpload", "s"]
    );

    var numShells = container.getShellCommands().length;
    this.values.uniqueName = "CloudUpload"+numShells;
  },

  getScript: function() {
    var queryPath = this.values.bucketName + "/" + this.values.keyPath;
    queryPath = queryPath.replace(/\/\/*/g, " ");

    var ret = 'QUERY_PATH=`echo "' + queryPath + '" | sed "s/\\/\\/*/\\//g"`\n';
    ret += 'cloud upload "${QUERY_PATH}" "' + this.values.inputFilePath + '" --set-acl=public-read\n';
    //`echo "${s3Bucket}/${s3BaseKeyPath}/subset_request.sh" | sed "s/\/\/*/\//g"`
    return ret;
  }
});

