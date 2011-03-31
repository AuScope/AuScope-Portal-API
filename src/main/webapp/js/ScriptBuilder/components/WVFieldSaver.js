/*
 * This file is part of the AuScope Virtual Rock Lab (VRL) project.
 * Copyright (c) 2009 ESSCC, The University of Queensland
 *
 * Licensed under the terms of the GNU Lesser General Public License.
 */

Ext.namespace("ScriptBuilder");
Ext.ux.ComponentLoader.load({url: ScriptBuilder.componentPath+"WVFieldSaver.json"});

WVFieldSaverNode = Ext.extend(ScriptBuilder.BaseComponent, {
  constructor: function(container) {
    WVFieldSaverNode.superclass.constructor.apply(this,
        [container, "Wall Vector Field Saver", "WVFieldSaver", "f"]
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
    var ret = this.values.uniqueName+" = WallVectorFieldSaverPrms (\n";
    ret+="   wallName = \""+this.values.wallName+"\",\n";
    ret+="   fieldName = \""+this.values.fieldName+"\",\n";
    ret+="   fileName = \""+this.values.fileName+"\",\n";
    ret+="   fileFormat = \""+this.values.fileFormat+"\",\n";
    ret+="   beginTimeStep = "+this.values.beginTS+",\n";
    ret+="   endTimeStep = "+this.values.endTS+",\n";
    ret+="   timeStepIncr = "+this.values.timeIncrement+"\n)\n";
    ret+="sim.createFieldSaver ( "+this.values.uniqueName+" )\n\n";
    return ret;
  }
});

