/*
 * This file is part of the AuScope Virtual Rock Lab (VRL) project.
 * Copyright (c) 2009 ESSCC, The University of Queensland
 *
 * Licensed under the terms of the GNU Lesser General Public License.
 */

Ext.namespace("ScriptBuilder");
Ext.ux.ComponentLoader.load({url: ScriptBuilder.componentPath+"IVRunnable.json"});

IVRunnableNode = Ext.extend(ScriptBuilder.BaseComponent, {
  constructor: function(container) {
    IVRunnableNode.superclass.constructor.apply(this,
        [container, "OpenInventor Camera", "IVRunnable", "r"]
    );
  },

  getScript: function() {
    var ret = "from IVsnap import IVsnaps\n\n";
    ret+=this.values.uniqueName+" = IVsnaps (\n";
    ret+="   LsmMpi = sim,\n   interval = "+this.values.interval+"\n)\n";
    ret+=this.values.uniqueName+".configure (\n";
    ret+="   lookAt = ["+this.values.lookAtX+", "+this.values.lookAtY+", "+this.values.lookAtZ+"],\n";
    ret+="   camPosn = ["+this.values.camPosX+", "+this.values.camPosY+", "+this.values.camPosZ+"],\n";
    ret+="   zoomFactor = "+this.values.zoomFactor+",\n";
    ret+="   imageSize = ["+this.values.resX+", "+this.values.resY+"]\n)\n\n";
    if (this.values.snapTiming == 0) {
      ret+="sim.addPreTimeStepRunnable ( "+this.values.uniqueName+" )\n\n";
    } else {
      ret+="sim.addPostTimeStepRunnable ( "+this.values.uniqueName+" )\n\n";
    }
    return ret;
  }
});

