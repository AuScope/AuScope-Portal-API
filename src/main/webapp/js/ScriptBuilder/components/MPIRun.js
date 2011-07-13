/*
 * This file is part of the AuScope Virtual Exploration Geophysics Lab (VEGL) project.
 * Copyright (c) 2011 CSIRO Earth Science and Resource Engineering
 *
 * Licensed under the terms of the GNU Lesser General Public License.
 */

Ext.namespace("ScriptBuilder");
Ext.ux.ComponentLoader.load({url: ScriptBuilder.componentPath+"MPIRun.json"});

MPIRunNode = Ext.extend(ScriptBuilder.BaseComponent, {
  constructor: function(container) {
	  MPIRunNode.superclass.constructor.apply(this,
        [container, "MPI - Run", "MPIRun", "s"]
    );

	var numShells = container.getShellCommands().length;
    this.values.uniqueName = "mpirun"+numShells;
  },

  getScript: function() {
	return "mpirun -np " + this.values.numProcessors +
		   " --mca " + this.values.mcaArgs +
		   " \"" + this.values.executable + "\"" +
		   " \"" + this.values.programArgs + "\"\n";
  }
});

