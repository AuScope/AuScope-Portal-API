/**
 * Utility functions for Web Coverage Services
 */
Ext.define('vegl.util.WCSUtil', {
    singleton: true
}, function() {
    /**
     * Estimates the amount of data points the specified bbox will return from the specified WCS.
     *
     * Returns a JSON object via a callback with the following fields:
     *
     * {
     *  width : the returned number of data points horizontally (no guarantee on accuracy)
     *  height : the returned number of data points vertically (no guarantee on accuracy)
     *  roundedTotal : width * height rounded to a nice 'well rounded' number.
     * }
     *
     * @param bbox a portal.util.BBox
     * @param wcsUrl The wcs URL as a string
     * @param coverageName the coverage name as a String
     * @param cbOpts Passed along to cb
     * @param cb function(success, errorMsg, responseObj, cbOpts) Called when estimation returns a result
     */
    vegl.util.WCSUtil.estimateCoverageSize = function(bbox, wcsUrl, coverageName, cbOpts, cb) {
        Ext.Ajax.request({
            url : 'estimateCoverageSize.do',
            params : {
                northBoundLatitude : bbox.northBoundLatitude,
                southBoundLatitude : bbox.southBoundLatitude,
                eastBoundLongitude : bbox.eastBoundLongitude,
                westBoundLongitude : bbox.westBoundLongitude,
                serviceUrl : wcsUrl,
                coverageName : coverageName
            },
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
                    width : responseObj.data.width,
                    height : responseObj.data.height,
                    roundedTotal : vegl.util.WCSUtil.roundToApproximation(responseObj.data.width * responseObj.data.height)
                }, cbOpts);
            }
        });
    };

    /**
     * Given a number with N digits, disregard most of the least significant digits and round
     * to a nice even number. This is to round off approximate values (eg 12456) to a more rounded
     * value so it looks more 'approximate' to people (eg 12000).
     */
    vegl.util.WCSUtil.roundToApproximation = function(number) {
        var totalDigits = number.toString().length;
        var digitsToKeep = Math.floor(totalDigits / 2);
        var divisor = Math.pow(10, totalDigits - digitsToKeep);
        return Math.round((number / divisor)) * divisor;
    };
});

