package org.auscope.portal.server.web.controllers;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;

import javax.servlet.ReadListener;
import javax.servlet.ServletInputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.io.IOUtils;
import org.auscope.portal.core.services.CSWFilterService;
import org.auscope.portal.core.test.PortalTestClass;
import org.auscope.portal.server.web.security.ANVGLUser;
import org.auscope.portal.server.web.service.ANVGLUserService;
import org.auscope.portal.server.web.service.SimpleWfsService;
import org.jmock.Expectations;
import org.junit.Before;
import org.junit.Test;

import org.powermock.reflect.Whitebox;

/**
 * Unit test for PurchaseController
 * @author Bo Yan
 *
 */
public class TestPurchaseController extends PortalTestClass {

    private SimpleWfsService mockWfsService = context.mock(SimpleWfsService.class);
    private CSWFilterService mockCSWFilterService = context.mock(CSWFilterService.class);
    private HttpServletRequest mockRequest = context.mock(HttpServletRequest.class);
    private HttpServletResponse mockResponse = context.mock(HttpServletResponse.class);
    private ANVGLUserService mockUserService = context.mock(ANVGLUserService.class);
    
    private PurchaseController controller;
    
    final String serviceUrl = "http://example.org/service";

    /**
     * Setup controller before all the tests
     */
    @Before
    public void setup() {
        controller = new PurchaseController(mockWfsService, mockCSWFilterService);
    }
 
    /**
     * test processDataPayment
     * @throws Exception 
     */
    @Test
    public void testProcessDataPayment() throws Exception {
    	
    	final InputStream strStream = IOUtils.toInputStream(
    							"{\"amount\": 123.45," + 
    							"\"tokenId\": \"1234\"," +
    							"\"email\": \"test@123456789mail.com\"," +
    							"\"dataToPurchase\": ["+
    							"{\"cswRecord\": \"csw\"}]}" 
    							,Charset.forName("UTF-8"));
        final DelegatingServletInputStream input = new DelegatingServletInputStream(strStream);

        final ANVGLUser user = new ANVGLUser();
        
        context.checking(new Expectations() {        	
            {
            	allowing(mockRequest).getInputStream(); will(returnValue(input));
                allowing(mockUserService).getLoggedInUser(); will(returnValue(user));
            }
        });
        Whitebox.setInternalState(controller, "userService", mockUserService);
        
    	controller.processDataPayment(mockRequest, mockResponse);
    }
    
    /**
     * test processJobPayment
     */
    @Test
    public void testProcessJobPayment() {
    	
    }
    
    /**
     * test getPurchases
     */
    @Test
    public void testGetPurchases() {
    	
    }
    
    /**
     * test getServiceType
     */
    @Test
    public void testGetServiceType() {
    	
    }
    
    private class DelegatingServletInputStream extends ServletInputStream {

        private final InputStream sourceStream;

        /**
         * Create a DelegatingServletInputStream for the given source stream.
         * @param sourceStream the source stream (never <code>null</code>)
         */
        public DelegatingServletInputStream(InputStream sourceStream) {
            this.sourceStream = sourceStream;
        }

        /**
         * Return the underlying source stream (never <code>null</code>).
         */
        public final InputStream getSourceStream() {
            return this.sourceStream;
        }


        public int read() throws IOException {
            return this.sourceStream.read();
        }

        public void close() throws IOException {
            super.close();
            this.sourceStream.close();
        }

		@Override
		public boolean isFinished() {
			// TODO Auto-generated method stub
			return false;
		}

		@Override
		public boolean isReady() {
			// TODO Auto-generated method stub
			return false;
		}

		@Override
		public void setReadListener(ReadListener listener) {
			// TODO Auto-generated method stub
			
		}

    }
}