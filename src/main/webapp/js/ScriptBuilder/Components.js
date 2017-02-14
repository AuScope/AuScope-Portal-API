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

            //Reassemble our configured vs unconfigured problem/solutions (and tag configured/unconfigured solutions)
            var problemMap = {};
            for (var i in data.configuredProblems) {
                var problem = data.configuredProblems[i];
                for (var j in problem.solutions) {
                    problem.solutions[j].configured = true;
                }

                if (problemMap[problem.id]) {
                    problemMap[problem.id].solutions = problemMap[problem.id].solutions.concat(problem.solutions);
                } else {
                    problemMap[problem.id] = problem;
                }
            }
            for (var i in data.unconfiguredProblems) {
                var problem = data.unconfiguredProblems[i];
                for (var j in problem.solutions) {
                    problem.solutions[j].configured = false;
                }

                if (problemMap[problem.id]) {
                    problemMap[problem.id].solutions = problemMap[problem.id].solutions.concat(problem.solutions);
                } else {
                    problemMap[problem.id] = problem;
                }
            }

            //Build our tree of nodes
            for (var i in problemMap) {
                var problem = problemMap[i];

                children = [];

                for (var j in problem.solutions) {
                    var solution = problem.solutions[j];

                    children.push({
                        id: solution.uri,
                        type: "s",
                        text: solution.name,
                        cls: solution.configured ? '' : 'vl-disabled-treenode',
                        qtip: solution.configured ? solution.description : 'This solution can\'t run in any of your configured compute locations. You will need to configure additional compute locations in your user profile.',
                        disabled: !solution.configured,
                        iconCls: solution.configured ? '' : 'vl-disabled-treenode-icon',
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
