Ext.ns('ScriptBuilder.Components');

/**
 * The raw configuration for building the scriptbuilder tree
 *
 * Retrieve available templates from the marketplace then populate the
 * panel with the resulting tree.
 */
ScriptBuilder.Components.getComponents = function(tree, fn) {
    Ext.Ajax.request({
    	url : "getSolutions.do",
        scope : this,
        headers: {
            Accept: 'application/json'
        },
        
        callback : function(options, success, response) {
            var errorMsg, errorInfo;

            if (success) {
                var responseObj = Ext.JSON.decode(response.responseText);
                
                if (responseObj) {
                    var data, prob_id, children;
                   
                    var root = tree.getRootNode();
                    root.removeAll();
                    
                    for (var i in responseObj.data) {
                        var problem = responseObj.data[i];
                        
                        children = [];

                        for (var j in problem.solutions) {
                            var solution = problem.solutions[j];

                            children.push({
                                id: solution.uri,
                                type: "s",
                                text: solution.name,
                                qtip: solution.description,
                                leaf: true
                            });
                        }
                        
                        root.appendChild({
                            text: problem.name,
                            type: "category",
                            qtip: problem.description,
                            expanded: true,
                            children: children
                        });
                    }

                    // Call the callback function fn
                    if (fn) { fn(); }
                } else {
                    errorMsg = responseObj.msg;
                    errorInfo = responseObj.debugInfo;
                }
            } else {
                errorMsg = "There was an error loading your script.";
                errorInfo = "Please try again in a few minutes or report this error to cg_admin@csiro.au.";
            }

            if (errorMsg) {
                //Create an error object and pass it to custom error window
                var errorObj = {
                    title : 'Script Loading Error',
                    message : errorMsg,
                    info : errorInfo
                };

                var errorWin = Ext.create('portal.widgets.window.ErrorWindow', {
                    errorObj : errorObj
                });
                
                errorWin.show();
            }
        }
    });
    
};