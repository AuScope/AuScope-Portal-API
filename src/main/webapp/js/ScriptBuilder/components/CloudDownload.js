/*
 * This file is part of the AuScope Virtual Exploration Geophysics Lab (VEGL) project.
 * Copyright (c) 2011 CSIRO Earth Science and Resource Engineering
 *
 * Licensed under the terms of the GNU Lesser General Public License.
 */

Ext.namespace("ScriptBuilder");
Ext.ux.ComponentLoader.load({url: ScriptBuilder.componentPath+"CloudDownload.json"});

CloudDownloadNode = Ext.extend(ScriptBuilder.BaseComponent, {
  constructor: function(container) {
    CloudDownloadNode.superclass.constructor.apply(this,
        [container, "Cloud - download", "CloudDownload", "s"]
    );

    var numShells = container.getShellCommands().length;
    this.values.uniqueName = "CloudDownload"+numShells;
  },

  getScript: function() {
    var queryPath = this.values.bucketName + "/" + this.values.keyPath;
    queryPath = queryPath.replace(/\/\/*/g, " ");

    return 'cloud download "' + queryPath + '" > "' + this.values.outputFilePath + '"\n';
  }
});

