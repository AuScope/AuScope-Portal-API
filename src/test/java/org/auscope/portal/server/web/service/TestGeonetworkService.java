package org.auscope.portal.server.web.service;

import java.io.ByteArrayInputStream;
import java.io.InputStream;


import junit.framework.Assert;

import org.apache.commons.httpclient.Header;
import org.apache.commons.httpclient.URI;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpMethodBase;
import org.auscope.portal.Util;
import org.auscope.portal.csw.CSWGeographicElement;
import org.auscope.portal.csw.CSWOnlineResource;
import org.auscope.portal.csw.CSWRecord;
import org.auscope.portal.server.web.GeonetworkDetails;
import org.auscope.portal.server.web.GeonetworkMethodMaker;
import org.jmock.Expectations;
import org.jmock.Mockery;
import org.jmock.lib.legacy.ClassImposteriser;
import org.junit.Before;
import org.junit.Test;

public class TestGeonetworkService {
	
	private Mockery context = new Mockery() {{
        setImposteriser(ClassImposteriser.INSTANCE);
    }};
	
	private HttpServiceCaller serviceCaller;
	private GeonetworkMethodMaker gnMethodMaker;
    private GeonetworkDetails gnDetails;
    private GeonetworkService service;
	
	@Before
	public void setup() {
		serviceCaller = context.mock(HttpServiceCaller.class);
		gnMethodMaker = context.mock(GeonetworkMethodMaker.class);
		gnDetails = new GeonetworkDetails("http://foo.bar", "user-name", "pass-word");
		service = new GeonetworkService(serviceCaller, gnMethodMaker, gnDetails);
	}
	
	@Test
	public void testSuccessfulRequest() throws Exception {
		final String sessionCookie = "sessionCookie";
		final HttpMethodBase insertRecordMethod = context.mock(HttpMethodBase.class, "insertRecordMethod");
		final HttpMethodBase recordMetadataMethod = context.mock(HttpMethodBase.class, "recordMetadataMethod");
		final HttpMethodBase recordPublicMethod = context.mock(HttpMethodBase.class, "recordPublicMethod");
		final HttpMethodBase loginMethod = context.mock(HttpMethodBase.class, "loginMethod");
		final HttpMethodBase logoutMethod = context.mock(HttpMethodBase.class, "logoutMethod");
		final HttpClient mockClient = context.mock(HttpClient.class);
		
		final String uuid = "4cda9dc3-9a0e-40cd-a3a9-64db5ce3c031";
		final String recordId = "19144";
		final String insertResponse = Util.loadXML("src/test/resources/GNCSWInsertResponse.xml");
		final String loginResponse = Util.loadXML("src/test/resources/GNLoginLogoutSuccessResponse.xml");
		final String logoutResponse = Util.loadXML("src/test/resources/GNLoginLogoutSuccessResponse.xml");
		final String recordPublicResponse = Util.loadXML("src/test/resources/GNRecordPublicResponse.xml");
		final String recordMetadata = Util.loadXML("src/test/resources/GNMetadataShowResponse.html");
		final InputStream recordMetadataStream =  new ByteArrayInputStream(recordMetadata.getBytes());
		
		final CSWRecord record = new CSWRecord("a", "b", "c", "", "", new CSWOnlineResource[0], new CSWGeographicElement[0]);
		final URI responseUri = new URI("http://foo.bar.baz", false);
		
		context.checking(new Expectations() {{
			allowing(gnMethodMaker).makeInsertRecordMethod(with(any(String.class)), with(any(String.class)), with(any(String.class)));will(returnValue(insertRecordMethod));
			allowing(gnMethodMaker).makeRecordMetadataShowMethod(gnDetails.getUrl(), uuid, sessionCookie);will(returnValue(recordMetadataMethod));
			allowing(gnMethodMaker).makeRecordPublicMethod(gnDetails.getUrl(), recordId, sessionCookie);will(returnValue(recordPublicMethod));
			allowing(gnMethodMaker).makeUserLoginMethod(gnDetails.getUrl(), gnDetails.getUser(), gnDetails.getPassword());will(returnValue(loginMethod));
			allowing(gnMethodMaker).makeUserLogoutMethod(gnDetails.getUrl(), sessionCookie);will(returnValue(logoutMethod));
			
			allowing(serviceCaller).getHttpClient();will(returnValue(mockClient));
			
			allowing(loginMethod).getResponseHeader("Set-Cookie");will(returnValue(new Header("Set-Cookie", sessionCookie)));
			
			oneOf(serviceCaller).getMethodResponseAsString(insertRecordMethod, mockClient);will(returnValue(insertResponse));
			oneOf(serviceCaller).getMethodResponseAsStream(recordMetadataMethod, mockClient);will(returnValue(recordMetadataStream));
			oneOf(serviceCaller).getMethodResponseAsString(recordPublicMethod, mockClient);will(returnValue(recordPublicResponse));
			oneOf(serviceCaller).getMethodResponseAsString(loginMethod, mockClient);will(returnValue(loginResponse));
			oneOf(serviceCaller).getMethodResponseAsString(logoutMethod, mockClient);will(returnValue(logoutResponse));
			
			allowing(recordMetadataMethod).getURI();will(returnValue(responseUri));
		}});
		
		Assert.assertEquals(responseUri.toString(), service.makeCSWRecordInsertion(record));
	}
	
	@Test(expected=Exception.class)
	public void testBadLoginRequest() throws Exception {
		final HttpMethodBase loginMethod = context.mock(HttpMethodBase.class, "loginMethod");
		final HttpClient mockClient = context.mock(HttpClient.class);
		final String loginResponse = "<html>The contents doesn't matter as a failed GN login returns a static page</html>";
		
		final CSWRecord record = new CSWRecord("a", "b", "c", "", "", new CSWOnlineResource[0], new CSWGeographicElement[0]);
		
		context.checking(new Expectations() {{
			allowing(gnMethodMaker).makeUserLoginMethod(gnDetails.getUrl(), gnDetails.getUser(), gnDetails.getPassword());will(returnValue(loginMethod));
			
			allowing(serviceCaller).getHttpClient();will(returnValue(mockClient));
			
			oneOf(serviceCaller).getMethodResponseAsString(loginMethod, mockClient);will(returnValue(loginResponse));
		}});
		
		service.makeCSWRecordInsertion(record);
	}
}
