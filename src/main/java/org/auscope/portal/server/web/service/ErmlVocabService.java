package org.auscope.portal.server.web.service;

import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.Map;

import org.apache.http.client.methods.HttpRequestBase;
import org.auscope.portal.core.server.http.HttpServiceCaller;
import org.auscope.portal.core.services.PortalServiceException;
import org.auscope.portal.core.services.SISSVoc3Service;
import org.auscope.portal.core.services.methodmakers.sissvoc.SISSVoc3MethodMaker.Format;
import org.auscope.portal.core.services.namespaces.VocabNamespaceContext;
import org.auscope.portal.mineraloccurrence.CommodityVocabMethodMaker;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.Property;
import org.apache.jena.rdf.model.ResIterator;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.rdf.model.Statement;
import org.apache.jena.rdf.model.StmtIterator;

/**
 * An specialisation of a SISSVoc3Service to better deal with an Earth Resource Markup Language commodity vocabulary.
 * 
 * @author Josh Vote
 *
 */
public class ErmlVocabService extends SISSVoc3Service {

    public static final String GA_URN_PATTERN = "urn:cgi:classifier:GA:commodity:.*";
    public static final String REPOSITORY_NAME = "commodity-vocab";

    public ErmlVocabService(HttpServiceCaller httpServiceCaller,
            CommodityVocabMethodMaker commodityVocabMethodMaker, String baseUrl) {
        super(httpServiceCaller, commodityVocabMethodMaker, baseUrl, REPOSITORY_NAME);
    }

    /**
     * Gets all RDF concepts at the specified repository as a single JENA Model. The results will be requested page by page until the entire repository has been
     * traversed.
     *
     * @return
     * @throws PortalServiceException
     * @throws URISyntaxException
     */
    public Model getAllCommodityConcepts() throws PortalServiceException, URISyntaxException {
        Model model = ModelFactory.createDefaultModel();
        int pageNumber = 0;
        int pageSize = this.getPageSize();

        //Request each page in turn - put the results into Model
        do {
            HttpRequestBase method = ((CommodityVocabMethodMaker) sissVocMethodMaker).getAllCommodities(getBaseUrl(),
                    getRepository(), Format.Rdf, pageSize, pageNumber);
            if (requestPageOfConcepts(method, model)) {
                pageNumber++;
            } else {
                break;
            }
        } while (true);

        return model;
    }

    /**
     * Gets a Map of all commodity names keyed by their commodity URI's. The commodity names will be restricted to a specific language.
     *
     * Only the original GA URNs will be returned
     *
     * @param language
     *            The language prefix (eg 'en') that the preferred names will be drawn from
     * @return
     * @throws URISyntaxException
     */
    public Map<String, String> getGaCommodityConcepts(String language) throws PortalServiceException,
            URISyntaxException {
        Map<String, String> result = new HashMap<String, String>();

        Model model = ModelFactory.createDefaultModel();
        int pageNumber = 0;
        int pageSize = this.getPageSize();

        //Request each of the GA commodity names
        do {
            HttpRequestBase method = ((CommodityVocabMethodMaker) sissVocMethodMaker).getCommoditiesMatchingUrn(
                    getBaseUrl(), getRepository(), Format.Rdf, pageSize, pageNumber, GA_URN_PATTERN);
            if (requestPageOfConcepts(method, model)) {
                pageNumber++;
            } else {
                break;
            }
        } while (true);

        //Iterate over all the resources with a preferred label
        Property prefLabelProperty = model.createProperty(VocabNamespaceContext.SKOS_NAMESPACE, "prefLabel");
        Property sameAsProperty = model.createProperty(VocabNamespaceContext.OWL_NAMESPACE, "sameAs");
        ResIterator iterator = model.listResourcesWithProperty(prefLabelProperty);
        while (iterator.hasNext()) {
            //Ensure we only include the preferred labels matching 'language'
            Resource res = iterator.next();
            StmtIterator prefLabelIt = res.listProperties(prefLabelProperty);
            while (prefLabelIt.hasNext()) {
                Statement prefLabelStatement = prefLabelIt.next();
                if (prefLabelStatement.getLanguage().equals(language)) {
                    String prefLabel = prefLabelStatement.getString();
                    Statement sameAsStatement = res.getProperty(sameAsProperty);
                    if (sameAsStatement != null) {
                        String urn = sameAsStatement.getResource().getURI();
                        if (urn != null) {
                            result.put(urn, prefLabel);
                        }
                    }
                }
            }
        }

        return result;
    }
}
