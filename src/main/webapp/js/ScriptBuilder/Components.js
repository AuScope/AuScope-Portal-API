Ext.ns('ScriptBuilder.Components');

/**
 * The raw configuration for building the scriptbuilder tree
 */
ScriptBuilder.Components.getComponents = function(selectedToolbox) {
    var comps = {
        text : "Script Builder Components",
        expanded : true,
        children : []
    };

    switch (selectedToolbox.toLowerCase()) {
        case "ubc-gif":
            comps.children.push(ScriptBuilder.Components.getUBCExamples());
            break;
        case "escript":
            comps.children.push(ScriptBuilder.Components.getEscriptExamples());
            break;
        case "underworld":
            comps.children.push(ScriptBuilder.Components.getUnderworldExamples());
            break;
        case "aem-inversion":
            comps.children.push(ScriptBuilder.Components.getAEMExamples());
            break;
        default:
            comps.children.push(ScriptBuilder.Components.getUBCExamples());
            comps.children.push(ScriptBuilder.Components.getEscriptExamples());
            comps.children.push(ScriptBuilder.Components.getUnderworldExamples());
    }

    return comps;
};

ScriptBuilder.Components.getUBCExamples = function() {
    return {
        type : "category",
        text : "UBC GIF Examples",
        expanded : true,
        children : [{
            id   : "ScriptBuilder.templates.UbcGravityTemplate",
            type : "s",
            text : "Gravity Inversion",
            qtip : "Perform a gravity inversion using UBC GIF. Expects data in the form of a CSV file. Double click to use this example.",
            leaf : true
        },{
            id   : "ScriptBuilder.templates.UbcMagneticTemplate",
            type : "s",
            text : "Magnetic Inversion",
            qtip : "Perform a magnetic inversion using UBC GIF. Expects data in the form of a CSV file. Double click to use this example.",
            leaf : true
        }]
    };
};

ScriptBuilder.Components.getEscriptExamples = function() {
    return {
        text : "escript Examples",
        type : "category",
        expanded : true,
        children : [{
            id   : "ScriptBuilder.templates.EScriptGravityTemplate",
            type : "s",
            text : "Gravity Inversion",
            qtip : "Perform a gravity inversion using escript. Expects data in the form of a NetCDF file. Double click to use this example.",
            leaf : true
        },{
            id   : "ScriptBuilder.templates.EScriptMagneticTemplate",
            type : "s",
            text : "Magnetic Inversion",
            qtip : "Perform a magnetic inversion using escript. Expects data in the form of a NetCDF file. Double click to use this example.",
            leaf : true
        },{
            id   : "ScriptBuilder.templates.EScriptJointTemplate",
            type : "s",
            text : "Joint Inversion",
            qtip : "Perform a joint gravity/magnetic inversion using escript. Expects both datasets to be in the form of seperate NetCDF files. Double click to use this example.",
            leaf : true
        },{
            id   : "ScriptBuilder.templates.EScriptGravityPointTemplate",
            type : "s",
            text : "Gravity Inversion",
            qtip : "Perform a gravity inversion using escript. Expects data in the form of a simple wfs file. Double click to use this example.",
            leaf : true
        }]
    };
};

ScriptBuilder.Components.getAEMExamples = function() {
    return {
        text : "AEM Inversion Examples",
        type : "category",
        expanded : true,
        children : [{
            id   : "ScriptBuilder.templates.AEMInversionTemplate",
            type : "s",
            text : "AEM Inversion",
            qtip : "Perform a AEM inversion using some script. To be completed",
            leaf : true
        }]
    };
};

ScriptBuilder.Components.getUnderworldExamples = function() {
    return {
        text : "Underworld Examples",
        type : "category",
        expanded : true,
        children : [{
            id   : "ScriptBuilder.templates.UnderworldGocadTemplate",
            type : "s",
            text : "Gocad Simulation",
            qtip : "Perform an Underworld simulation using a Gocad Voxelset. Expects data in the form of a Gocad voxel set. Double click to use this example.",
            leaf : true
        }]
    };
};