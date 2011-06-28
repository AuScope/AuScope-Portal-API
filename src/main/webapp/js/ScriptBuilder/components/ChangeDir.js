/*
 * This file is part of the AuScope Virtual Exploration Geophysics Lab (VEGL) project.
 * Copyright (c) 2011 CSIRO Earth Science and Resource Engineering
 *
 * Licensed under the terms of the GNU Lesser General Public License.
 */

Ext.namespace("ScriptBuilder");
Ext.ux.ComponentLoader.load({url: ScriptBuilder.componentPath+"ChangeDir.json"});

ChangeDirNode = Ext.extend(ScriptBuilder.BaseComponent, {
  constructor: function(container) {
	ChangeDirNode.superclass.constructor.apply(this,
        [container, "Shell - cd", "ChangeDir", "s"]
    );
  },
  
  getScript: function() {
	return 'cd "' + this.values.directory + '"\n';
  }
});

