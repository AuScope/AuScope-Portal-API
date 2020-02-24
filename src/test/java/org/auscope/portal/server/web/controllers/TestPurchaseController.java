package org.auscope.portal.server.web.controllers;

import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.nio.charset.Charset;
import java.util.Collection;
import java.util.LinkedList;
import java.util.Locale;

import javax.servlet.ReadListener;
import javax.servlet.ServletInputStream;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.io.IOUtils;
import org.auscope.portal.core.services.CSWFilterService;
import org.auscope.portal.core.services.PortalServiceException;
import org.auscope.portal.core.test.PortalTestClass;
import org.auscope.portal.server.vegl.VGLDataPurchase;
import org.auscope.portal.server.web.security.ANVGLUser;
import org.auscope.portal.server.web.service.ANVGLUserService;
import org.auscope.portal.server.web.service.SimpleWfsService;
import org.auscope.portal.server.web.service.VGLPurchaseService;

import org.jmock.Expectations;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import org.powermock.reflect.Whitebox;
import org.springframework.web.servlet.ModelAndView;
/**
 * Unit test for PurchaseController
 * @author Bo Yan
 *
 */
public class TestPurchaseController extends PortalTestClass {

    private SimpleWfsService mockWfsService = context.mock(SimpleWfsService.class);
    private CSWFilterService mockCSWFilterService = context.mock(CSWFilterService.class);
    private HttpServletRequest mockRequest = context.mock(HttpServletRequest.class);
    private ANVGLUserService mockUserService = context.mock(ANVGLUserService.class);
    private VGLPurchaseService mockPurchaseService = context.mock(VGLPurchaseService.class);
    
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
    							"{\"amount\": 0.0," + 
    							"\"tokenId\": \"1234\"," +
    							"\"email\": \"test@123456789mail.com\"," +
    							"\"dataToPurchase\": ["+
    							"{\"cswRecord\": \"csw\"}]}" 
    							,Charset.forName("UTF-8"));
        final DelegatingServletInputStream input = new DelegatingServletInputStream(strStream);

        context.checking(new Expectations() {        	
            {
            	allowing(mockRequest).getInputStream(); will(returnValue(input));
                allowing(mockUserService).getLoggedInUser(); will(returnValue(null)); // anonymous user!
            }
        });
        Whitebox.setInternalState(controller, "userService", mockUserService);
        
        HttpServletResponseImpl response = new HttpServletResponseImpl();
    	controller.processDataPayment(mockRequest, response);
    	// Unable to process payment for anonymous user.
    	Assert.assertTrue(response.sw.toString().contains("anonymous user"));
    }
    
    /**
     * test processJobPayment
     * @throws Exception 
     */
    @Test
    public void testProcessJobPayment() throws Exception {
    	final InputStream strStream = IOUtils.toInputStream(
				"{\"amount\": 0.0," + 
				"\"tokenId\": \"1234\"," +
				"\"email\": \"test@123456789mail.com\"," +
				"\"jobId\":1234," +
				"\"jobName\":\"dummy\"}" 
				,Charset.forName("UTF-8"));
    	final DelegatingServletInputStream input = new DelegatingServletInputStream(strStream);

    	context.checking(new Expectations() {        	
    		{
    			allowing(mockRequest).getInputStream(); will(returnValue(input));
    			allowing(mockUserService).getLoggedInUser(); will(returnValue(null)); // anonymous user!
    		}
    	});
    	Whitebox.setInternalState(controller, "userService", mockUserService);

    	HttpServletResponseImpl response = new HttpServletResponseImpl();
    	controller.processJobPayment(mockRequest, response);
    	// Unable to process payment for anonymous user.
    	Assert.assertTrue(response.sw.toString().contains("anonymous user"));
    }
    
    /**
     * test getPurchases
     * @throws PortalServiceException 
     */
    @Test
    public void testGetPurchases() throws PortalServiceException {
        final ANVGLUser user = new ANVGLUser();
        context.checking(new Expectations() {        	
            {
                allowing(mockUserService).getLoggedInUser(); will(returnValue(user));
                allowing(mockPurchaseService).getDataPurchasesByUser(user); will(returnValue(new LinkedList<VGLDataPurchase>())); 
            }
        });
        Whitebox.setInternalState(controller, "userService", mockUserService);
        Whitebox.setInternalState(controller, "purchaseService", mockPurchaseService);
        ModelAndView mav = controller.getPurchases();
        Assert.assertTrue(mav.getModel().containsKey("data"));
    }
    
    private class HttpServletResponseImpl implements HttpServletResponse {
        private final StringWriter sw = new StringWriter();
        private final PrintWriter pWriter = new PrintWriter(sw);

		@Override
		public String getCharacterEncoding() {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public String getContentType() {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public ServletOutputStream getOutputStream() throws IOException {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public PrintWriter getWriter() throws IOException {
			return pWriter;
		}

		@Override
		public void setCharacterEncoding(String charset) {
			// TODO Auto-generated method stub
			
		}

		@Override
		public void setContentLength(int len) {
			// TODO Auto-generated method stub
			
		}

		@Override
		public void setContentLengthLong(long length) {
			// TODO Auto-generated method stub
			
		}

		@Override
		public void setContentType(String type) {
			// TODO Auto-generated method stub
			
		}

		@Override
		public void setBufferSize(int size) {
			// TODO Auto-generated method stub
			
		}

		@Override
		public int getBufferSize() {
			// TODO Auto-generated method stub
			return 0;
		}

		@Override
		public void flushBuffer() throws IOException {
			// TODO Auto-generated method stub
			
		}

		@Override
		public void resetBuffer() {
			// TODO Auto-generated method stub
			
		}

		@Override
		public boolean isCommitted() {
			// TODO Auto-generated method stub
			return false;
		}

		@Override
		public void reset() {
			// TODO Auto-generated method stub
			
		}

		@Override
		public void setLocale(Locale loc) {
			// TODO Auto-generated method stub
			
		}

		@Override
		public Locale getLocale() {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public void addCookie(Cookie cookie) {
			// TODO Auto-generated method stub
			
		}

		@Override
		public boolean containsHeader(String name) {
			// TODO Auto-generated method stub
			return false;
		}

		@Override
		public String encodeURL(String url) {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public String encodeRedirectURL(String url) {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public String encodeUrl(String url) {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public String encodeRedirectUrl(String url) {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public void sendError(int sc, String msg) throws IOException {
			// TODO Auto-generated method stub
			
		}

		@Override
		public void sendError(int sc) throws IOException {
			// TODO Auto-generated method stub
			
		}

		@Override
		public void sendRedirect(String location) throws IOException {
			// TODO Auto-generated method stub
			
		}

		@Override
		public void setDateHeader(String name, long date) {
			// TODO Auto-generated method stub
			
		}

		@Override
		public void addDateHeader(String name, long date) {
			// TODO Auto-generated method stub
			
		}

		@Override
		public void setHeader(String name, String value) {
			// TODO Auto-generated method stub
			
		}

		@Override
		public void addHeader(String name, String value) {
			// TODO Auto-generated method stub
			
		}

		@Override
		public void setIntHeader(String name, int value) {
			// TODO Auto-generated method stub
			
		}

		@Override
		public void addIntHeader(String name, int value) {
			// TODO Auto-generated method stub
			
		}

		@Override
		public void setStatus(int sc) {
			// TODO Auto-generated method stub
			
		}

		@Override
		public void setStatus(int sc, String sm) {
			// TODO Auto-generated method stub
			
		}

		@Override
		public int getStatus() {
			// TODO Auto-generated method stub
			return 0;
		}

		@Override
		public String getHeader(String name) {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public Collection<String> getHeaders(String name) {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public Collection<String> getHeaderNames() {
			// TODO Auto-generated method stub
			return null;
		}
    	
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