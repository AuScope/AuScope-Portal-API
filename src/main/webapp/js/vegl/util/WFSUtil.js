/**
 * Utility functions for Web Feature Services
 */
Ext.define('vegl.util.WFSUtil', {
    singleton: true
}, function() {
    /**
     * Estimates the amount of data points the specified bbox will return from the specified WCS.
     *
     * Returns a JSON object via a callback with the following fields:
     *
     * {
     *  total : The total number of data features in the selection area
     *  roundedTotal : total rounded to a nice 'well rounded' number.
     * }
     *
     * @param bbox a portal.util.BBox
     * @param wfsUrl The wfs URL as a string
     * @param featureType the feature type name as a String
     * @param cbOpts Passed along to cb
     * @param cb function(success, errorMsg, responseObj, cbOpts) Called when estimation returns a result
     */
    vegl.util.WFSUtil.estimateFeatureCount = function(bbox, wfsUrl, featureType, cbOpts, cb) {
        Ext.Ajax.request({
            url : 'getFeatureCount.do',
            params : {
                crs : bbox.crs,
                northBoundLatitude : bbox.northBoundLatitude,
                southBoundLatitude : bbox.southBoundLatitude,
                eastBoundLongitude : bbox.eastBoundLongitude,
                westBoundLongitude : bbox.westBoundLongitude,
                serviceUrl : wfsUrl,
                typeName : featureType
            },
            timeout : 1000 * 60 * 5, //5 minutes
            scope : this,
            callback : function(options, success, response) {
                if (!success) {
                    cb(false, 'Error estimating size: Couldn\'t contact VGL server.', null, cbOpts);
                    return;
                }

                var responseObj = Ext.JSON.decode(response.responseText);
                if (!responseObj.success) {
                    cb(false, 'Error estimating size: ' + responseObj.msg, null, cbOpts);
                    return;
                }

                cb(true, '', {
                    total : responseObj.data,
                    roundedTotal : vegl.util.WCSUtil.roundToApproximation(responseObj.data)
                }, cbOpts);
            }
        });
    };
});

