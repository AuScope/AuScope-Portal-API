package org.auscope.portal.server.web.controllers;

import org.auscope.portal.core.test.PortalTestClass;
import org.junit.Assert;
import org.junit.Test;
import org.springframework.ui.ModelMap;
import org.springframework.web.servlet.ModelAndView;

public class TestReprojectionController extends PortalTestClass {
    ReprojectionController cont = new ReprojectionController();

    /**
     * Simple test with dataset that will return MGA Zone of 49.
     */
    @Test
    public void testCalculateMgaZoneForBBox_MGAZone_49() {
        double north = -30;
        double south = -32;
        double east = 125;
        double west = 100;

        ModelAndView mav = cont.calculateMgaZoneForBBox(north, south, east, west);
        Assert.assertNotNull(mav);
        Assert.assertTrue((Boolean) mav.getModel().get("success"));
        int result = (Integer)mav.getModel().get("data");
        Assert.assertEquals(49, result);
    }

    /**
     * Simple test with dataset that will return MGA Zone of 50.
     */
    @Test
    public void testCalculateMgaZoneForBBox_MGAZone_50() {
        double north = -30;
        double south = -32;
        double east = 125;
        double west = 110;

        ModelAndView mav = cont.calculateMgaZoneForBBox(north, south, east, west);
        Assert.assertNotNull(mav);
        Assert.assertTrue((Boolean) mav.getModel().get("success"));
        int result = (Integer)mav.getModel().get("data");
        Assert.assertEquals(50, result);
    }

    /**
     * Simple test with dataset that will return MGA Zone of 51.
     */
    @Test
    public void testCalculateMgaZoneForBBox_MGAZone_51() {
        double north = -30;
        double south = -32;
        double east = 150;
        double west = 100;

        ModelAndView mav = cont.calculateMgaZoneForBBox(north, south, east, west);
        Assert.assertNotNull(mav);
        Assert.assertTrue((Boolean) mav.getModel().get("success"));
        int result = (Integer)mav.getModel().get("data");
        Assert.assertEquals(51, result);
    }

    /**
     * Simple test with dataset that will return MGA Zone of 52.
     */
    @Test
    public void testCalculateMgaZoneForBBox_MGAZone_52() {
        double north = -30;
        double south = -32;
        double east = 150;
        double west = 110;

        ModelAndView mav = cont.calculateMgaZoneForBBox(north, south, east, west);
        Assert.assertNotNull(mav);
        Assert.assertTrue((Boolean) mav.getModel().get("success"));
        int result = (Integer)mav.getModel().get("data");
        Assert.assertEquals(52, result);
    }

    /**
     * Simple test with dataset that will return MGA Zone of 53.
     */
    @Test
    public void testCalculateMgaZoneForBBox_MGAZone_53() {
        double north = -30;
        double south = -32;
        double east = 170;
        double west = 100;

        ModelAndView mav = cont.calculateMgaZoneForBBox(north, south, east, west);
        Assert.assertNotNull(mav);
        Assert.assertTrue((Boolean) mav.getModel().get("success"));
        int result = (Integer)mav.getModel().get("data");
        Assert.assertEquals(53, result);
    }

    /**
     * Simple test with dataset that will return MGA Zone of 54.
     */
    @Test
    public void testCalculateMgaZoneForBBox_MGAZone_54() {
        double north = -30;
        double south = -32;
        double east = 180;
        double west = 100;

        ModelAndView mav = cont.calculateMgaZoneForBBox(north, south, east, west);
        Assert.assertNotNull(mav);
        Assert.assertTrue((Boolean) mav.getModel().get("success"));
        int result = (Integer)mav.getModel().get("data");
        Assert.assertEquals(54, result);
    }

    /**
     * Simple test with dataset that will return MGA Zone of 55.
     */
    @Test
    public void testCalculateMgaZoneForBBox_MGAZone_55() {
        double north = -30;
        double south = -32;
        double east = 190;
        double west = 100;

        ModelAndView mav = cont.calculateMgaZoneForBBox(north, south, east, west);
        Assert.assertNotNull(mav);
        Assert.assertTrue((Boolean) mav.getModel().get("success"));
        int result = (Integer)mav.getModel().get("data");
        Assert.assertEquals(55, result);
    }

    /**
     * Simple test with dataset that will return MGA Zone of 56.
     */
    @Test
    public void testCalculateMgaZoneForBBox_MGAZone_56() {
        double north = -30;
        double south = -32;
        double east = 200;
        double west = 100;

        ModelAndView mav = cont.calculateMgaZoneForBBox(north, south, east, west);
        Assert.assertNotNull(mav);
        Assert.assertTrue((Boolean) mav.getModel().get("success"));
        int result = (Integer)mav.getModel().get("data");
        Assert.assertEquals(56, result);
    }

    /**
     * Simple test with dataset that will fail.
     */
    @Test
    public void testCalculateMgaZoneForBBox_MGAZoneSmallerThanZero() {
        // The following dataset will yield -1 MGA zone
        double north = -30;
        double south = -32;
        double east = 100;
        double west = 50;

        ModelAndView mav = cont.calculateMgaZoneForBBox(north, south, east, west);
        Assert.assertNotNull(mav);
        Assert.assertFalse((Boolean) mav.getModel().get("success"));
        Assert.assertNull(mav.getModel().get("data"));
    }

    /**
     * Simple test to ensure no errors (no validity of conversion is tested)
     */
    @Test
    public void testBBoxReproject() {
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

    /**
     * Simple test with dataset that will fail.
     */
    @Test
    public void testBBoxReproject_MGAZoneSmallerThanZero() {
        // The following dataset will yield -1 MGA zone
        double north = -30;
        double south = -32;
        double east = 100;
        double west = 50;

        ModelAndView mav = cont.projectBBoxToUtm(north, south, east, west, null);
        Assert.assertNotNull(mav);
        Assert.assertFalse((Boolean) mav.getModel().get("success"));
        Assert.assertNull(mav.getModel().get("data"));
    }
}