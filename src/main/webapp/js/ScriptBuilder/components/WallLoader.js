/*
 * This file is part of the AuScope Virtual Rock Lab (VRL) project.
 * Copyright (c) 2009 ESSCC, The University of Queensland
 *
 * Licensed under the terms of the GNU Lesser General Public License.
 */

Ext.namespace("ScriptBuilder");
Ext.ux.ComponentLoader.load({url: ScriptBuilder.componentPath+"WallLoader.json"});

WallLoaderNode = Ext.extend(ScriptBuilder.BaseComponent, {
  constructor: function(container) {
    WallLoaderNode.superclass.constructor.apply(this,
        [container, "Wall Loader", "WallLoader", "r"]
    );
  },

  fillFormValues: function(form) {
    form.setValues(this.values);
    var wallList = this.container.getWalls();
    var store = form.findField('wallName').getStore();
    store.removeAll();
    for (var i=0; i<wallList.length; i++) {
        store.add(new store.recordType({'text': wallList[i].getUniqueName()}));
    }
  },

  getScript: function() {
    var ret = "from WallLoader import WallLoaderRunnable\n\n";
    ret+=this.values.uniqueName+" = WallLoaderRunnable (\n";
    ret+="   LsmMpi = sim,\n   wallName = \""+this.values.wallName+"\",\n";
    ret+="   vPlate = Vec3("+this.values.plateX+", "+this.values.plateY+", "+this.values.plateZ+"),\n";
    ret+="   dt = "+this.container.getValues().timeIncrement+"\n)\n";
    if (this.values.snapTiming == 0) {
      ret+="sim.addPreTimeStepRunnable ( "+this.values.uniqueName+" )\n\n";
    } else {
      ret+="sim.addPostTimeStepRunnable ( "+this.values.uniqueName+" )\n\n";
    }
    return ret;
  }
});

