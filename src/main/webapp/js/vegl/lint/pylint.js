function lintPython(text, callback) {
    // Don't check an empty string
    if (Ext.isEmpty(text)) {
        callback([]);
    }
    else {
        // Send the text to the server to check
        try {
            Ext.Ajax.request({
                url : 'lintTemplate.do',
                params: { template: text },
                callback : function(options, success, response) {
                    var errorMsg, errorInfo;
                    if (success) {
                        var responseObj = Ext.JSON.decode(response.responseText);
                        if (responseObj.success) {
                            var found = [], issue;

                            responseObj.data.forEach(function(it) {
                                var from = CodeMirror.Pos(it.from.line, it.from.column);
                                var to;
                                if (it.to) {
                                    to = CodeMirror.Pos(it.to.line, it.to.column);
                                }
                                else {
                                    to = CodeMirror.Pos(it.from.line, it.from.column + 1);
                                }
                                found.push({
                                    severity: it.severity,
                                    message: it.message,
                                    from: from,
                                    to: to
                                });
                            });

                            callback(found);
                            return;
                        } else {
                            errorMsg = responseObj.msg;
                            errorInfo = responseObj.debugInfo;
                        }
                    } else {
                        errorMsg = "There was an error checking the template.";
                        errorInfo = "Please try again in a few minutes or report this error to cg_admin@csiro.au.";
                    }

                    //Create an error object and pass it to custom error window
                    var errorObj = {
                        title : 'Script Checking Error',
                        message : errorMsg,
                        info : errorInfo
                    };

                    var errorWin = Ext.create('portal.widgets.window.ErrorWindow', {
                        errorObj : errorObj
                    });
                    errorWin.show();
                }
            });
        } catch (exception) {
            console.log("Exception: ScriptBuilder template checking, details below - ");
            console.log(exception);
        }
    }
}

lintPython.async = true;
