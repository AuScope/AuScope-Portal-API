package org.auscope.portal.server.web.controllers;

import junit.framework.Assert;

import org.auscope.portal.core.test.PortalTestClass;
import org.junit.Test;
import org.springframework.ui.ModelMap;
import org.springframework.web.servlet.ModelAndView;

public class TestReprojectionController extends PortalTestClass {
    ReprojectionController cont = new ReprojectionController();

    /**
     * Simple test to ensure no errors (no validity of conversion is tested)
     * @throws Exception
     */
    @Test
    public void testBBoxReproject() throws Exception {
        double north = -30;
        double south = -32;
        double east = 125;
        double west = 110;

        ModelAndView mav = cont.projectBBoxToUtm(north, south, east, west, null);
        Assert.assertNotNull(mav);
        Assert.assertTrue((Boolean) mav.getModel().get("success"));
        Assert.assertNotNull(mav.getModel().get("data"));

        ModelMap data = (ModelMap) mav.getModel().get("data");
        data.containsKey("mgaZone");
        data.containsKey("minNorthing");
        data.containsKey("maxNorthing");
        data.containsKey("minEasting");
        data.containsKey("maxEasting");
    }
}
