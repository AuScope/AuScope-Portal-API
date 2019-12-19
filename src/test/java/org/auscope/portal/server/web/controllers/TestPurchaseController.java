package org.auscope.portal.server.web.controllers;

import org.auscope.portal.core.services.CSWFilterService;
import org.auscope.portal.core.test.PortalTestClass;
import org.auscope.portal.server.web.service.SimpleWfsService;
import org.junit.Before;

/**
 * Unit test for PurchaseController
 * @author Bo Yan
 *
 */
public class TestPurchaseController extends PortalTestClass {

    private SimpleWfsService mockWfsService = context.mock(SimpleWfsService.class);
    private CSWFilterService mockCSWFilterService = context.mock(CSWFilterService.class);
    
    private PurchaseController controller;
    
    final String serviceUrl = "http://example.org/service";

    /**
     * Setup controller before all the tests
     */
    @Before
    public void setup() {
        controller = new PurchaseController(mockWfsService, mockCSWFilterService);
    }
    
}
