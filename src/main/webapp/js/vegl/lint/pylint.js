/**
 * Run a code checker over the text and pass any issues to callback.
 *
 * Does not report errors from the server on the client.
 */
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
                  var found = [];

                  if (success) {
                    var responseObj = Ext.JSON.decode(response.responseText);
                    if (responseObj.success) {
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
                    }
                  }

                  // Client ignores server side errors
                  callback(found);
                  return;
                }
            });
        } catch (exception) {
          //Create an error object and pass it to custom error window
          var errorObj = {
            title : 'Script Checking Error',
            message : "Failed to connect to server for template checking.",
            info : "Please try again in a few minutes or report this error to cg_admin@csiro.au."
          };

          var errorWin = Ext.create('portal.widgets.window.ErrorWindow', {
            errorObj : errorObj
          });
          errorWin.show();
        }
    }
}

lintPython.async = true;
