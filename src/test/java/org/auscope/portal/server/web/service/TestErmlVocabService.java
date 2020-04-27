package org.auscope.portal.server.web.service;

import java.io.InputStream;
import java.util.Map;

import org.apache.http.client.methods.HttpRequestBase;
import org.auscope.portal.core.server.http.HttpClientInputStream;
import org.auscope.portal.core.server.http.HttpServiceCaller;
import org.auscope.portal.core.services.methodmakers.sissvoc.SISSVoc3MethodMaker.Format;
import org.auscope.portal.core.test.PortalTestClass;
import org.auscope.portal.core.util.ResourceUtil;
import org.auscope.portal.mineraloccurrence.CommodityVocabMethodMaker;
import org.jmock.Expectations;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

public class TestErmlVocabService extends PortalTestClass {
    private HttpRequestBase mockMethod = context.mock(HttpRequestBase.class, "mockMethod");
    private HttpRequestBase mockMethod2 = context.mock(HttpRequestBase.class, "mockMethod2");
    private HttpServiceCaller mockServiceCaller = context.mock(HttpServiceCaller.class);
    private CommodityVocabMethodMaker mockMethodMaker = context.mock(CommodityVocabMethodMaker.class);
    private String baseUrl = "http://example.org:8080/sissvoc/path";

    private ErmlVocabService service;

    @Before
    public void setup() {
        service = new ErmlVocabService(mockServiceCaller, mockMethodMaker, baseUrl);
        service.setPageSize(50);
    }

    /**
     * Tests that iterating a repository works as expected - returning all english GA concepts
     *
     * @throws Exception
     */
    @Test
    public void testGetEnglishCommodityConcepts() throws Exception {
        final InputStream rs1 = ResourceUtil
                .loadResourceAsStream("org/auscope/portal/erml/vocab/SISSVoc3_Concepts_MoreData.xml");
        final InputStream rs2 = ResourceUtil
                .loadResourceAsStream("org/auscope/portal/erml/vocab/SISSVoc3_Concepts_NoMoreData.xml");

        context.checking(new Expectations() {
            {
                oneOf(mockMethodMaker).getCommoditiesMatchingUrn(baseUrl, ErmlVocabService.REPOSITORY_NAME, Format.Rdf,
                        service.getPageSize(), 0, ErmlVocabService.GA_URN_PATTERN);
                will(returnValue(mockMethod));
                oneOf(mockMethodMaker).getCommoditiesMatchingUrn(baseUrl, ErmlVocabService.REPOSITORY_NAME, Format.Rdf,
                        service.getPageSize(), 1, ErmlVocabService.GA_URN_PATTERN);
                will(returnValue(mockMethod2));

                oneOf(mockServiceCaller).getMethodResponseAsStream(mockMethod);
                will(returnValue(new HttpClientInputStream(rs1, null)));
                oneOf(mockServiceCaller).getMethodResponseAsStream(mockMethod2);
                will(returnValue(new HttpClientInputStream(rs2, null)));

                oneOf(mockMethod).releaseConnection();
                oneOf(mockMethod2).releaseConnection();
            }
        });

        Map<String, String> result = service.getGaCommodityConcepts("en");
        Assert.assertNotNull(result);
        Assert.assertEquals(2, result.size());

        Assert.assertEquals("gold", result.get("urn:cgi:classifier:GA:commodity:Au"));
        Assert.assertEquals("uranium", result.get("urn:cgi:classifier:GA:commodity:U"));
    }

    /**
     * Tests that iterating a repository works as expected - returning only a single french term
     *
     * @throws Exception
     */
    @Test
    public void testGetFrenchCommodityConcepts() throws Exception {
        final InputStream rs1 = ResourceUtil
                .loadResourceAsStream("org/auscope/portal/erml/vocab/SISSVoc3_Concepts_MoreData.xml");
        final InputStream rs2 = ResourceUtil
                .loadResourceAsStream("org/auscope/portal/erml/vocab/SISSVoc3_Concepts_NoMoreData.xml");

        context.checking(new Expectations() {
            {
                oneOf(mockMethodMaker).getCommoditiesMatchingUrn(baseUrl, ErmlVocabService.REPOSITORY_NAME, Format.Rdf,
                        service.getPageSize(), 0, ErmlVocabService.GA_URN_PATTERN);
                will(returnValue(mockMethod));
                oneOf(mockMethodMaker).getCommoditiesMatchingUrn(baseUrl, ErmlVocabService.REPOSITORY_NAME, Format.Rdf,
                        service.getPageSize(), 1, ErmlVocabService.GA_URN_PATTERN);
                will(returnValue(mockMethod2));

                oneOf(mockServiceCaller).getMethodResponseAsStream(mockMethod);
                will(returnValue(new HttpClientInputStream(rs1, null)));
                oneOf(mockServiceCaller).getMethodResponseAsStream(mockMethod2);
                will(returnValue(new HttpClientInputStream(rs2, null)));

                oneOf(mockMethod).releaseConnection();
                oneOf(mockMethod2).releaseConnection();
            }
        });

        Map<String, String> result = service.getGaCommodityConcepts("fr");
        Assert.assertNotNull(result);
        Assert.assertEquals(1, result.size());

        Assert.assertEquals("or", result.get("urn:cgi:classifier:GA:commodity:Au"));
    }
}
