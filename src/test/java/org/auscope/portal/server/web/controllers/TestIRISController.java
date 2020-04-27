package org.auscope.portal.server.web.controllers;

import java.io.IOException;

import org.auscope.portal.core.test.PortalTestClass;
import org.auscope.portal.core.util.ResourceUtil;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.springframework.web.servlet.ModelAndView;

public class TestIRISController extends PortalTestClass {

    private IRISController controller;

    @Before
    public void startUp() {

        // Using extract and override pattern to control dependency.
        // This avoids external lookup by making the resource come
        // directly from a file instead of IRIS.
        this.controller = new IRISController(null) {
            @Override
            protected String getIrisResponseFromQuery(String queryUrl) throws IOException {

                return ResourceUtil.loadResourceAsString(queryUrl.substring(0, queryUrl.indexOf(".xml") + 4));
            }
        };
    }

    @After
    public void tearDown() {
        this.controller = null;
    }

    @Test
    public void getIRISStations_stationResponseXML_ResultantModelHasSuccessSetToTrue() {
        // Act
        ModelAndView result = this.controller.getIRISStations("org/auscope/portal/iris/stationResponse.xml", "");

        // Assert
        Assert.assertTrue((Boolean) result.getModel().get("success"));
    }

    @Test
    public void getIRISStations_stationResponseBadXML_ResultantModelHasSuccessSetToFalse() {
        // Act
        ModelAndView result = this.controller.getIRISStations("org/auscope/portal/iris/stationResponseBad.xml", "");

        // Assert
        Assert.assertFalse((Boolean) result.getModel().get("success"));
    }

    @Test
    public void getStationChannels_channelResponseXML_ResultantModelHasSuccessSetToTrue() {
        // Act
        ModelAndView result = this.controller.getIRISStations("org/auscope/portal/iris/channelResponse.xml", "");

        // Assert
        Assert.assertTrue((Boolean) result.getModel().get("success"));
    }

    @Test
    public void getStationChannels_channelResponseBadXML_ResultantModelHasSuccessSetToFalse() {
        // Act
        ModelAndView result = this.controller.getIRISStations("org/auscope/portal/iris/channelResponseBad.xml", "");

        // Assert
        Assert.assertFalse((Boolean) result.getModel().get("success"));
    }
}