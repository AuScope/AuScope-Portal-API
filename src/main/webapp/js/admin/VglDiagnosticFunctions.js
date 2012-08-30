Ext.ns('admin.VglDiagnosticFunctions');

admin.VglDiagnosticFunctions.ClearCswCache = Ext.create('admin.DiagnosticFunction', {
    name : 'Clear CS/W Cache',
    description : 'This function clears the internal CS/W cache which will force an immediate update request to be sent to each registry.',
    group : 'General',
    executeFn : function(callback) {
        Ext.Ajax.request({
            url : 'updateCSWCache.do',
            callback : function(options, success, response) {
                if (!success) {
                    callback(false, 'Unable to connect to VGL server.');
                    return;
                }

                var responseObj = Ext.JSON.decode(response.responseText);
                callback(responseObj.success, responseObj.msg);
            }
        });
    }
});