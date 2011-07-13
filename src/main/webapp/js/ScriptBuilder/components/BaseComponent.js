/*
 * This file is part of the AuScope Virtual Rock Lab (VRL) project.
 * Copyright (c) 2009 ESSCC, The University of Queensland
 *
 * Licensed under the terms of the GNU Lesser General Public License.
 */

Ext.namespace("ScriptBuilder");

ScriptBuilder.componentPath = "js/ScriptBuilder/components/";

ScriptBuilder.BaseComponent = Ext.extend(Ext.tree.TreeNode, {

  //
  // Constructor with simulation container (will be the parent node after
  // adding to the tree), title (will be displayed in the tree), component ID
  // (will be used to call correct form etc) and type.
  //
  constructor: function(container, compTitle, compId, compType) {
    ScriptBuilder.BaseComponent.superclass.constructor.apply(this);
    this.container = container;
    this.compId = compId;
    this.compTitle = compTitle;
    this.type = compType;
    this.values = { uniqueName: "" };
    this.setText(this.getUniqueName()+" ("+this.compTitle+")");
  },

  //
  // Returns stored values
  //
  getValues: function() {
    return this.values;
  },

  setValuesObject: function(obj) {
	// if the name has changed check for an existing node with the same name
    // and force changing
    if (this.container && obj.uniqueName &&
        this.getUniqueName() != obj.uniqueName &&
        this.container.findByName(obj.uniqueName) != null) {
      Ext.Msg.alert("Name not unique", "Please use a unique name for this component.");
      return false;
    } else {
      this.setText(this.getUniqueName()+" ("+this.compTitle+")");

      // THis should be a merge operation - but its not....
      this.values = obj;
      return true;
    }
  },

  //
  // Retrieves values from the given form after checking uniqueness of the
  // component name. Returns false if the name is not unique.
  //
  setValues: function(form) {
    var tmpVals = form.getValues(false);
    return this.setValuesObject(tmpVals);
  },

  //
  // Uses current values to fill given form
  //
  fillFormValues: function(form) {
    form.setValues(this.values);
  },

  //
  // Returns a unique ID for this instance
  //
  getUniqueName: function() {
    return this.values.uniqueName;
  },

  //
  // Returns the script fragment for this component
  //
  getScript: function() {
    return "";
  }

});

