Ext.ns('ScriptBuilder.Components');

/**
 * The raw configuration for building the scriptbuilder tree
 */
ScriptBuilder.Components.getComponents = function(selectedComputeVmId) {
    var comps = {
        text : "Script Builder Components",
        expanded : true,
        children : []
    };

    switch (selectedComputeVmId) {
        case "ami-0000000b": // UBC
            comps.children.push(ScriptBuilder.Components.getUBCExamples());
            break;
        case "ami-00000022": // escript
            comps.children.push(ScriptBuilder.Components.getEscriptExamples());
            break;
        case "ami-00000025": // Underworld
            comps.children.push(ScriptBuilder.Components.getUnderworldExamples());
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
            qtip : "Perform a gravity inversion using UBC GIF. Expects data in the form of a CSV file.",
            leaf : true
        },{
            id   : "ScriptBuilder.templates.UbcMagneticTemplate",
            type : "s",
            text : "Magnetic Inversion",
            qtip : "Perform a magnetic inversion using UBC GIF. Expects data in the form of a CSV file.",
            leaf : true
        }]
    };
};

ScriptBuilder.Components.getEscriptExamples = function() {
    return {
        text : "eScript Examples",
        type : "category",
        expanded : true,
        children : [{
            id   : "ScriptBuilder.templates.EScriptGravityTemplate",
            type : "s",
            text : "Gravity Inversion",
            qtip : "Perform a gravity inversion using eScript. Expects data in the form of a NetCDF file.",
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
            qtip : "Perform an Underworld simulation using a Gocad Voxelset. Expects data in the form of a Gocad voxel set.",
            leaf : true
        }]
    };
};