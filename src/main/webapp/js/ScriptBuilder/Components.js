Ext.ns('ScriptBuilder.Components');

/**
 * The raw configuration for building the scriptbuilder tree
 */
ScriptBuilder.Components = {
    text : "Script Builder Components",
    expanded : true,
    children : [{
        type : "category",
        text : "Shell Components",
        children : [{
            text : "Amazon Web Service Commands",
            children : [{
                id   : "ScriptBuilder.components.CloudDownload",
                type : "s",
                text : "Cloud Download",
                qtip : "Download a file from the Amazon web service S3 and store it on the local file system.",
                leaf : true
            },{
                id   : "ScriptBuilder.components.CloudUpload",
                type : "s",
                text : "Cloud Upload",
                qtip : "Upload a file from the local file system to the Amazon S3.",
                leaf : true
            }]
        },{
            text : "Open MPI Commands",
            children : [{
                id   : "ScriptBuilder.components.MPIRun",
                type : "s",
                text : "MPI Run",
                qtip : "Execute serial and parallel jobs in Open MPI.",
                leaf : true
            }]
        }]
    },{
        text : "Python Components",
        expanded : true,
        children : [{
            text : "VGL Workflow Steps",
            children : [{
                id : "ScriptBuilder.components.DefinePythonFunc",
                type : "s",
                text : "Define Function Name",
                qtip : "Starts the definition of a Python function.",
                leaf : true
            },{
                id : "ScriptBuilder.components.VEGLStep1",
                type : "s",
                text : "VGL Step 1",
                qtip : "Reads in a 3 column CSV file, assumes the data are floating point numbers, and places these columns into the variable 'data'.",
                leaf : true
            },{
                id : "ScriptBuilder.components.VEGLStep2",
                type : "s",
                text : "VGL Step 2",
                qtip : "Takes the variable 'data' and transforms the coordinates to a different coordinate system. This is because the national-scale data are geodetic (unprojected) and we need a projected coordinate system for a UBC-GIF inversion.",
                leaf : true
            },{
                id : "ScriptBuilder.components.VEGLStep3",
                type : "s",
                text : "VGL Step 3",
                qtip : "Takes the variable 'data' and removes any data points which do not lie within a given bounding box. Assumes the coordinate systems of 'data' and the bounding box are the same.",
                leaf : true
            },{
                id : "ScriptBuilder.components.VEGLStep4",
                type : "s",
                text : "VGL Step 4",
                qtip : "We need to determine the type of inversion. There are some corrections to be made to each type of data. The parameters for the inversions also vary slightly, as do the control files.",
                leaf : true
            },{
                id : "ScriptBuilder.components.VEGLStep5",
                type : "s",
                text : "VGL Step 5",
                qtip : "UBC-GIF needs a data file in a specific format.  We need to define a filename ('obs_filename'). This includes writing out expected errors in the data, number of data points etc.",
                leaf : true
            },{
                id : "ScriptBuilder.components.VEGLStep6",
                type : "s",
                text : "VGL Step 6",
                qtip : "Defines the mesh parameters and writes out a UBC-GIF mesh file. Mesh is defined by the minimum and maximum eastings and northings, inversion depth, and respective cell sizes. Mesh file name: 'mesh'",
                leaf : true
            },{
                id : "ScriptBuilder.components.VEGLStep7",
                type : "s",
                text : "VGL Step 7",
                qtip : "There are two parts to running a UBC-GIF inversion. The first involves a sensitivity analysis; here we write out the appropriate control files for this analysis. File names for things defined outside this method are defined at the top",
                leaf : true
            },{
                id : "ScriptBuilder.components.VEGLStep8",
                type : "s",
                text : "VGL Step 8",
                qtip : "In the second part to running a UBC-GIF inversion, we need to write out the control file for the actual inversion.",
                leaf : true
            },{
                id : "ScriptBuilder.components.VEGLStep9",
                type : "s",
                text : "VGL Step 9",
                qtip : "Finalise things.",
                leaf : true
            },{
                id : "ScriptBuilder.components.DefineMainFunc",
                type : "s",
                text : "Specify 'Main' Function",
                qtip : "Defines a particular function as the main function. It will be run first if this script is executed.",
                leaf : true
            }]
        },{
            id : "ScriptBuilder.components.PythonHeader",
            type : "s",
            text : "Python Script File Header",
            qtip : "Generates the script file header for identifying a python script.",
            leaf : true
        },{
            id : "ScriptBuilder.components.VEGLJobObject",
            type : "s",
            text : "Job Details",
            qtip : "Place Job specifics inside a python dictionary.",
            leaf : true
        },{
            id : "ScriptBuilder.components.CloudUtils",
            type : "s",
            text : "Cloud Util Functions",
            qtip : "Utility functions enabling python to communicate with the cloud storage.",
            leaf : true
        },{
            id : "ScriptBuilder.components.VEGLUtils",
            type : "s",
            text : "VGL Util Functions",
            qtip : "Utility functions enabling the standard VGL workflow.",
            leaf : true
        }]
    }]
};