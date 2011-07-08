/*
 * This file is part of the AuScope Virtual Exploration Geophysics Lab (VEGL) project.
 * Copyright (c) 2011 CSIRO Earth Science and Resource Engineering
 *
 * Licensed under the terms of the GNU Lesser General Public License.
 */

Ext.namespace("ScriptBuilder");
Ext.ux.ComponentLoader.load({url: ScriptBuilder.componentPath+"AWSDownload.json"});

AWSDownloadNode = Ext.extend(ScriptBuilder.BaseComponent, {
  constructor: function(container) {
	AWSDownloadNode.superclass.constructor.apply(this,
        [container, "AWS - download", "AWSDownload", "s"]
    );

	var numShells = container.getShellCommands().length;
    this.values.uniqueName = "shell"+numShells;
  },

  getScript: function() {
	var queryPath = this.values.bucketName + "/" + this.values.keyPath;
	queryPath = queryPath.replace(/\/\/*/g, "/");

    return 'aws get "' + queryPath + '" > "' + this.values.outputFilePath + '"\n';
  }
});

