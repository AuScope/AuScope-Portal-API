/*
 * This file is part of the AuScope Virtual Rock Lab (VRL) project.
 * Copyright (c) 2009 ESSCC, The University of Queensland
 *
 * Licensed under the terms of the GNU Lesser General Public License.
 */

Ext.namespace("ScriptBuilder");
Ext.ux.ComponentLoader.load({url: ScriptBuilder.componentPath+"Exclusion.json"});

ExclusionNode = Ext.extend(ScriptBuilder.BaseComponent, {
  constructor: function(container) {
    ExclusionNode.superclass.constructor.apply(this,
        [container, "Interaction Exclusion Principle", "Exclusion", "xx"]
    );
  },

  fillFormValues: function(form) {
    form.setValues(this.values);
    var intList = this.container.getInteractions();
    var store1 = form.findField('intName1').getStore();
    var store2 = form.findField('intName2').getStore();
    store1.removeAll();
    store2.removeAll();
    for (var i=0; i<intList.length; i++) {
        store1.add(new store1.recordType({'text': intList[i].getUniqueName()}));
        store2.add(new store1.recordType({'text': intList[i].getUniqueName()}));
    }
  },

  getUniqueName: function() {
    return "pp_exclusion";
  },

  getScript: function() {
    var ret="sim.createExclusion (\n";
    ret+="   interactionName1 = \""+this.values.intName1+"\",\n";
    ret+="   interactionName2 = \""+this.values.intName2+"\"\n)\n\n";

    return ret;
  }
});

