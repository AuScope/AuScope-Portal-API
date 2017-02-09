Ext.ns('ScriptBuilder.Components');

/**
 * The raw configuration for building the scriptbuilder tree
 *
 * Retrieve available templates from the marketplace then populate the
 * panel with the resulting tree.
 */
ScriptBuilder.Components.getComponents = function(tree, fn, filterFacets) {

    var params = {
        field: [],
        value: [],
        type: [],
        comparison: [],
    };

    if (filterFacets) {
        Ext.each(filterFacets, function(facet) {
            params.field.push(facet.get('field'));
            params.value.push(facet.get('value'));
            params.type.push(facet.get('type'));
            params.comparison.push(facet.get('comparison'));
        });
    }

    portal.util.Ajax.request({
    	url : 'secure/getProblems.do',
        scope : this,
        params: params,
        headers: {
            Accept: 'application/json'
        },

        success: function(data, message, debugInfo) {
            var prob_id, children;

            var root = tree.getRootNode();
            root.removeAll();

            for (var i in data) {
                var problem = data[i];

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
        },
        failure: function(message, debugInfo) {
            var errorObj = {
                title : 'Script Loading Error',
                message : "There was an error loading your script.",
                info : "Please try again in a few minutes or report this error to cg_admin@csiro.au."
            };
            var errorWin = Ext.create('portal.widgets.window.ErrorWindow', {
                errorObj : errorObj
            });
            errorWin.show();
        }
    });

};
