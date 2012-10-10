Ext.ns('ScriptBuilder.Components');

/**
 * The raw configuration for building the scriptbuilder tree
 */
ScriptBuilder.Components.getComponents = function() {
    return {
        text : "Script Builder Components",
        expanded : true,
        children : [{
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
        },{
            text : "eScript Examples",
            type : "category",
            children : [{
                id   : "ScriptBuilder.templates.EScriptGravityTemplate",
                type : "s",
                text : "Gravity Inversion",
                qtip : "Perform a gravity inversion using eScript. Expects data in the form of a NetCDF file.",
                leaf : true
            }]
        },{
            text : "Underworld Examples",
            type : "category",
            children : [{
                id   : "ScriptBuilder.templates.UnderworldGocadTemplate",
                type : "s",
                text : "Gocad Simulation",
                qtip : "Perform an Underworld simulation using a Gocad Voxelset. Expects data in the form of a Gocad voxel set.",
                leaf : true
            }]
        }]
    }
};