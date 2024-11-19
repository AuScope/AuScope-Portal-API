package org.auscope.portal.server.web.controllers;


import java.util.HashMap;

import java.util.Map;
import java.util.ArrayList;

import org.auscope.portal.core.services.VocabularyFilterService;
import org.auscope.portal.core.test.PortalTestClass;
import org.auscope.portal.core.util.structure.RDFTriple;

import org.hamcrest.Matcher;
import org.jmock.Expectations;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import org.springframework.web.servlet.ModelAndView;
import org.apache.jena.vocabulary.SKOS;

import au.gov.geoscience.portal.services.vocabularies.VocabularyLookup;

/**
 * Test Vocabulary Controller
 *
 */
public class TestVocabController extends PortalTestClass {

    private VocabController vocabularyController;

    private VocabularyFilterService mockVocabularyFilterService = context.mock(VocabularyFilterService.class);

    @Before
    public void setUp() {
        this.vocabularyController = new VocabController(mockVocabularyFilterService);
    }

    /**
     * @throws Exception
     */
    @Test
    public void testGetScalarQuery() throws Exception {
        final String repository = "nvcl-scalars";
        final String label = "labelX";
        final String defn = "defnX";
        final ArrayList<String> serviceResult = new ArrayList<String>();
        serviceResult.add(defn);

        context.checking(new Expectations() {
            {
                oneOf(mockVocabularyFilterService).getVocabularyById(VocabController.NVCL_SCALARS_VOCABULARY_ID, label, SKOS.definition);
                will(returnValue(serviceResult));
            }
        });

        ModelAndView mav = vocabularyController.getScalarQuery(repository, label);
        Assert.assertNotNull(mav);
        Assert.assertTrue((Boolean) mav.getModel().get("success"));
        HashMap<String,String> data = (HashMap<String,String>) mav.getModel().get("data");
        Assert.assertNotNull(data);
        Assert.assertEquals(label, data.get("label"));
        Assert.assertEquals(defn, data.get("definition"));
        Assert.assertEquals(defn, data.get("scopeNote"));
    }

    /**
     * @throws Exception
     */
    @Test
    public void testGetAllCommodities() throws Exception {
        final Map<String, String> serviceResult = new HashMap<String, String>();

        serviceResult.put("http://uri.org/1", "label1");
        serviceResult.put("http://uri.org/2", "label2");

        context.checking(new Expectations() {
            {
                oneOf(mockVocabularyFilterService).getVocabularyById(VocabController.COMMODITY_VOCABULARY_ID);
                will(returnValue(serviceResult));
            }
        });

        ModelAndView mav = vocabularyController.getAllCommodities();
        Assert.assertNotNull(mav);
        Assert.assertTrue((Boolean) mav.getModel().get("success"));

        @SuppressWarnings("unchecked")
        ArrayList<String[]> data = ArrayList.class.cast(mav.getModel().get("data"));
        Assert.assertNotNull(data);
        Assert.assertEquals(serviceResult.size(), data.size());

        for (String[] obj : data) {
            String urn = obj[0];
            String label = obj[1];;

            Assert.assertEquals(serviceResult.get(urn), label);
            serviceResult.remove(urn);
        }

        Assert.assertEquals("Service result contains items that were NOT included in the JSON array response", 0,
                serviceResult.size());
    }

    /**
     * @throws Exception
     */
    @Test
    public void testGetAllMineStatuses() throws Exception {
        final Map<String, String> serviceResult = new HashMap<String, String>();

        serviceResult.put("http://uri.org/1", "label1");
        serviceResult.put("http://uri.org/2", "label2");

        context.checking(new Expectations() {
            {
                oneOf(mockVocabularyFilterService).getVocabularyById(VocabController.MINE_STATUS_VOCABULARY_ID);
                will(returnValue(serviceResult));
            }
        });

        ModelAndView mav = vocabularyController.getAllMineStatuses();
        Assert.assertNotNull(mav);
        Assert.assertTrue((Boolean) mav.getModel().get("success"));

        @SuppressWarnings("unchecked")
        ArrayList<String[]> data = ArrayList.class.cast(mav.getModel().get("data"));
        Assert.assertNotNull(data);
        Assert.assertEquals(serviceResult.size(), data.size());

        // We want to make sure each of our map items are included in the list
        // We do this by removing items from serviceResult as they appear in the
        // response
        // Success will be measured by an empty serviceResult
        for (String[] obj : data) {
            String urn = obj[0];
            String label = obj[1];

            Assert.assertEquals(serviceResult.get(urn), label);
            serviceResult.remove(urn);
        }

        Assert.assertEquals("Service result contains items that were NOT included in the JSON array response", 0,
                serviceResult.size());
    }

    /**
     * @throws Exception
     */
    @Test
    public void testGetAllJorcCategories() throws Exception {

        final Map<String, String> serviceResults = new HashMap<String, String>();
        final Map<String, String> serviceResult1 = new HashMap<String, String>();
        final Map<String, String> serviceResult2 = new HashMap<String, String>();

        serviceResult1.put("http://uri.org/1", "label1");

        serviceResult2.put("http://uri.org/2", "label2");

        serviceResults.putAll(serviceResult1);
        serviceResults.putAll(serviceResult2);

        serviceResults.put(VocabularyLookup.RESERVE_CATEGORY.uri(), "any reserves");
        serviceResults.put(VocabularyLookup.RESOURCE_CATEGORY.uri(), "any resources");



        context.checking(new Expectations() {
            {
                Matcher<RDFTriple[]> anyTripleArray = anything();
                oneOf(mockVocabularyFilterService).getVocabularyById(with(same(VocabController.RESOURCE_VOCABULARY_ID)),with(anyTripleArray));
                will(returnValue(serviceResult1));
                oneOf(mockVocabularyFilterService).getVocabularyById(with(same(VocabController.RESERVE_VOCABULARY_ID)),with(anyTripleArray));
                will(returnValue(serviceResult2));
            }
        });

        ModelAndView mav = vocabularyController.getAllJorcCategories();
        Assert.assertNotNull(mav);
        Assert.assertTrue((Boolean) mav.getModel().get("success"));

        @SuppressWarnings("unchecked")
        ArrayList<String[]> data = ArrayList.class.cast(mav.getModel().get("data"));
        Assert.assertNotNull(data);
        Assert.assertEquals(serviceResults.size(), data.size());

        // We want to make sure each of our map items are included in the list
        // We do this by removing items from serviceResult as they appear in the
        // response
        // Success will be measured by an empty serviceResult
        for (String[] obj : data) {
            String urn = obj[0];
            String label = obj[1];

            Assert.assertEquals(serviceResults.get(urn), label);
            serviceResults.remove(urn);
        }

        Assert.assertEquals("Service result contains items that were NOT included in the JSON array response", 0,
                serviceResults.size());
    }

    /**
     * @throws Exception
     */
    @Test
    public void testGetAllTimescales() throws Exception {
        final Map<String, String> serviceResult = new HashMap<String, String>();

        serviceResult.put("http://uri.org/1", "label1");
        serviceResult.put("http://uri.org/2", "label2");

        context.checking(new Expectations() {
            {
                Matcher<RDFTriple[]> anyRDFTripleArray = anything();
                oneOf(mockVocabularyFilterService).getVocabularyById(with(same(VocabController.TIMESCALE_VOCABULARY_ID)),with(anyRDFTripleArray));
                will(returnValue(serviceResult));
            }
        });

        ModelAndView mav = vocabularyController.getAllTimescales();
        Assert.assertNotNull(mav);
        Assert.assertTrue((Boolean) mav.getModel().get("success"));

        @SuppressWarnings("unchecked")
        ArrayList<String[]> data = ArrayList.class.cast(mav.getModel().get("data"));
        Assert.assertNotNull(data);
        Assert.assertEquals(serviceResult.size(), data.size());

        // We want to make sure each of our map items are included in the list
        // We do this by removing items from serviceResult as they appear in the
        // response
        // Success will be measured by an empty serviceResult
        for (String[] obj : data) {
            String urn = obj[0];
            String label = obj[1];

            Assert.assertEquals(serviceResult.get(urn), label);
            serviceResult.remove(urn);
        }

        Assert.assertEquals("Service result contains items that were NOT included in the JSON array response", 0,
                serviceResult.size());
    }

}
