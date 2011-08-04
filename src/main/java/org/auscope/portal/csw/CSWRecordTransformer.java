package org.auscope.portal.csw;

import java.io.FileInputStream;
import java.util.ArrayList;
import java.util.List;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathExpressionException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.core.io.ClassPathResource;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;



/**
 * A class for taking a MD_Metadata DOM document representation as a template and using it
 * as a means to transform between CSWRecord and DOM.
 *
 * This class can build its initial DOM representation from an existing XML template specified at TEMPLATE_FILE.
 * This file must exist on the classpath
 * @author Josh Vote
 */
public class CSWRecordTransformer {
    public static final String TEMPLATE_FILE = "MD_MetadataTemplate.xml";
    protected final Log logger = LogFactory.getLog(getClass());

    private Node template;
    private Document document;

    private static final CSWNamespaceContext nc = new CSWNamespaceContext();

    private static final String serviceTitleExpression = "gmd:identificationInfo/gmd:MD_DataIdentification/gmd:citation/gmd:CI_Citation/gmd:title/gco:CharacterString";
    private static final String dataIdentificationAbstractExpression = "gmd:identificationInfo/gmd:MD_DataIdentification/gmd:abstract/gco:CharacterString";
    private static final String contactOrganisationExpression = "gmd:contact/gmd:CI_ResponsibleParty/gmd:organisationName/gco:CharacterString";
    private static final String contactIndividualExpression = "gmd:contact/gmd:CI_ResponsibleParty/gmd:individualName/gco:CharacterString";
    private static final String contactEmailExpression = "gmd:contact/gmd:CI_ResponsibleParty/gmd:contactInfo/gmd:CI_Contact/gmd:address/gmd:CI_Address/gmd:electronicMailAddress/gco:CharacterString";
    private static final String contactResourceExpression = "gmd:contact/gmd:CI_ResponsibleParty/gmd:contactInfo/gmd:CI_Contact/gmd:onlineResource";
    private static final String resourceProviderExpression =  "gmd:identificationInfo/gmd:MD_DataIdentification/gmd:pointOfContact/gmd:CI_ResponsibleParty[./gmd:role[./gmd:CI_RoleCode[@codeListValue = 'resourceProvider']]]/gmd:organisationName/gco:CharacterString";
    private static final String fileIdentifierExpression = "gmd:fileIdentifier/gco:CharacterString";
    private static final String onlineTransfersExpression = "gmd:distributionInfo/gmd:MD_Distribution/gmd:transferOptions/gmd:MD_DigitalTransferOptions/gmd:onLine";
    private static final String bboxExpression = "gmd:identificationInfo/gmd:MD_DataIdentification/gmd:extent/gmd:EX_Extent/gmd:geographicElement/gmd:EX_GeographicBoundingBox";
    private static final String keywordListExpression = "gmd:identificationInfo/gmd:MD_DataIdentification/gmd:descriptiveKeywords/gmd:MD_Keywords/gmd:keyword/gco:CharacterString";
    private static final String supplementalInfoExpression = "gmd:identificationInfo/gmd:MD_DataIdentification/gmd:supplementalInformation/gco:CharacterString";
    private static final String languageExpression = "gmd:identificationInfo/gmd:MD_DataIdentification/gmd:language/gco:CharacterString";
    private static final String otherConstraintsExpression = "gmd:identificationInfo/gmd:MD_DataIdentification/gmd:resourceConstraints/gmd:MD_LegalConstraints/gmd:otherConstraints/gco:CharacterString";

    private static final String templateBboxExtentExpression = "gmd:identificationInfo/gmd:MD_DataIdentification/gmd:extent[gmd:EX_Extent/gmd:geographicElement]";
    private static final String templateSupplementalInfoExpression = "gmd:identificationInfo/gmd:MD_DataIdentification/gmd:supplementalInformation";
    private static final String templateDescriptiveKeywords = "gmd:identificationInfo/gmd:MD_DataIdentification/gmd:descriptiveKeywords";
    private static final String templateSpatialRepresentationExpression = "gmd:identificationInfo/gmd:MD_DataIdentification/gmd:spatialRepresentationType";
    private static final String templateContactInfoExpression = "gmd:contact/gmd:CI_ResponsibleParty/gmd:contactInfo/gmd:CI_Contact";
    private static final String templateOnlineResourcesExpression = onlineTransfersExpression;
    private static final String templateLanguageExpression = "gmd:identificationInfo/gmd:MD_DataIdentification/gmd:language";
    private static final String templateLegalConstraintsExpression = "gmd:identificationInfo/gmd:MD_DataIdentification/gmd:resourceConstraints/gmd:MD_LegalConstraints";


    /**
     * Creates a new instance of this class uses the underlying metadata template. An exception will
     * be thrown if the required template cannot be loaded or parsed.
     * @throws Exception
     */
    public CSWRecordTransformer() throws Exception {
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        factory.setNamespaceAware(true); // never forget this!
        DocumentBuilder builder = factory.newDocumentBuilder();

        ClassPathResource r = new ClassPathResource(TEMPLATE_FILE);
        InputSource inputSource = new InputSource(r.getInputStream());
        this.document = builder.parse(inputSource);
        this.template = this.document.getDocumentElement();

    }

    /**
     * Creates a new instance of this class which will draw from the specified
     * gmd:MD_Metadata Node representation as a template
     * @param rootNode
     */
    public CSWRecordTransformer(Node mdMetadataNode) {
        this.document = mdMetadataNode.getOwnerDocument();
        this.template = mdMetadataNode;
    }

    /**
     * Given a root node and expression update the specified node's text content with newContents
     *
     * If then node DNE then this function will have no effect
     * @param root
     * @param expr
     * @param newContents
     * @throws XPathExpressionException
     */
    private void updateSingleNodeTextContents(Node root, String exprString, String newContents) throws XPathExpressionException {
        XPathExpression expr = CSWXPathUtil.attemptCompileXpathExpr(exprString);
        Node target = (Node)expr.evaluate(root, XPathConstants.NODE);

        //If it exists, update it
        //We can't just go creating nodes in the template (well not easily anyway).
        if (target != null) {
            target.setTextContent(newContents);
        }
    }

    /**
     * Helper method for creating child XML elements using the template document
     * @param parent The node who will be the parent of this node
     * @param namespaceUri The URI of this node
     * @param name The name of this node
     * @return
     */
    private Element createChildNode(Node parent, String namespaceUri, String name) {
        Element child = this.document.createElementNS(namespaceUri, name);

        parent.appendChild(child);

        return child;
    }

    /**
     * Helper method for appending a child element containing a single gco:Decimal element
     * @param parent
     * @param namespaceUri
     * @param name
     * @param value
     */
    private void appendChildDecimal(Node parent, String namespaceUri, String name, double value) {
        Element child = createChildNode(parent, namespaceUri, name);
        Node characterStr = createChildNode(child, nc.getNamespaceURI("gco"), "Decimal");
        characterStr.setTextContent(Double.toString(value));
    }

    /**
     * Helper method for appending a child element containing a single gco:CharacterString element
     * @param parent where it will be appended
     * @param namespaceUri
     * @param name
     * @param value
     */
    private void appendChildCharacterString(Node parent, String namespaceUri, String name, String value) {
        Element child = createChildNode(parent, namespaceUri, name);
        Node characterStr = createChildNode(child, nc.getNamespaceURI("gco"), "CharacterString");
        if (value == null || value.isEmpty()) {
            child.setAttributeNS(nc.getNamespaceURI("gco"), "nilReason", "missing");
        } else {
            characterStr.setTextContent(value);
        }
    }

    /**
     * Helper method, deletes all child nodes from node
     * @param root
     * @param expr
     * @throws XPathExpressionException
     */
    private void deleteChildNodes(Node node) throws XPathExpressionException {

        NodeList children = node.getChildNodes();

        for (int i = children.getLength() - 1; i >= 0; i--) {
            Node child = children.item(i);

            node.removeChild(child);
        }
    }

    /**
     * Helper method, deletes all nodes (and their children) from root that match the specified expression
     * @param root
     * @param expr
     * @throws XPathExpressionException
     */
    private void deleteMatchingNodes(Node root, String exprStr) throws XPathExpressionException {
        XPathExpression expr = CSWXPathUtil.attemptCompileXpathExpr(exprStr);
        NodeList matching = (NodeList) expr.evaluate(root, XPathConstants.NODESET);

        for (int i = 0; i < matching.getLength(); i++) {
            Node node = matching.item(i);

            Node parent = node.getParentNode();
            if (parent != null) {
                parent.removeChild(node);
            }
        }
    }

    /**
     * Helper method for appending a child element containing a single gmd:CI_Citation element
     * @param parent
     * @param namespaceUri
     * @param name
     * @param resource
     */
    private void appendChildOnlineResource(Node parent, String namespaceUri, String name, CSWOnlineResource onlineResource) {
        Node child = createChildNode(parent, namespaceUri, name);
        Node ciOnlineResource = createChildNode(child, nc.getNamespaceURI("gmd"), "CI_OnlineResource");

        //Add linkage
        Node linkage = createChildNode(ciOnlineResource, nc.getNamespaceURI("gmd"), "linkage");
        Node url = createChildNode(linkage, nc.getNamespaceURI("gmd"), "URL");
        url.setTextContent(onlineResource.getLinkage().toString());

        //Add protocol
        appendChildCharacterString(ciOnlineResource, nc.getNamespaceURI("gmd"), "protocol", onlineResource.getProtocol());

        //Add application profile
        appendChildCharacterString(ciOnlineResource, nc.getNamespaceURI("gmd"), "applicationProfile", onlineResource.getApplicationProfile());

        //Add name
        appendChildCharacterString(ciOnlineResource, nc.getNamespaceURI("gmd"), "name", onlineResource.getName());

        //Add description
        appendChildCharacterString(ciOnlineResource, nc.getNamespaceURI("gmd"), "description", onlineResource.getDescription());

        parent.appendChild(child);
    }

    /**
     * Transforms the specified CSWRecord back into a MD_Metadata element represented by Node.
     *
     * The transformation is built from the internal template specified at construction time.
     *
     * The internal template will NOT be modified by this function
     * @param record
     * @return
     * @throws XPathExpressionException
     */
    public Node transformToNode(CSWRecord record) throws XPathExpressionException {
        Node root = template.cloneNode(true);


        //The single fields we all assume exist
        updateSingleNodeTextContents(root, serviceTitleExpression, record.getServiceName());
        updateSingleNodeTextContents(root, dataIdentificationAbstractExpression, record.getDataIdentificationAbstract());
        updateSingleNodeTextContents(root, contactOrganisationExpression, record.getContactOrganisation());
        updateSingleNodeTextContents(root, contactEmailExpression, record.getContactEmail());
        updateSingleNodeTextContents(root, contactIndividualExpression, record.getContactIndividual());
        updateSingleNodeTextContents(root, fileIdentifierExpression, record.getFileIdentifier());
        updateSingleNodeTextContents(root, resourceProviderExpression, record.getResourceProvider());
        updateSingleNodeTextContents(root, supplementalInfoExpression, record.getSupplementalInformation());
        updateSingleNodeTextContents(root, languageExpression, record.getLanguage());

        //The 'array' fields will be somewhat trickier and CSW record template specific

        //Add our contact resource
        Node ciContact = (Node) CSWXPathUtil.attemptCompileXpathExpr(templateContactInfoExpression).evaluate(root, XPathConstants.NODE);
        deleteMatchingNodes(root, contactResourceExpression);
        if (record.getContactResource() != null) {
            appendChildOnlineResource(ciContact, nc.getNamespaceURI("gmd"), "onlineResource", record.getContactResource());
        }

        //Choose specifically our extent that has a geographic element
        CSWGeographicElement[] recordGeoEls = record.getCSWGeographicElements();
        deleteMatchingNodes(root, templateBboxExtentExpression);//firstly start by removing any geographic el's. We will replace them
        Node supplInfo = (Node) CSWXPathUtil.attemptCompileXpathExpr(templateSupplementalInfoExpression).evaluate(root, XPathConstants.NODE);//We will be inserting before this node
        for (CSWGeographicElement recordGeoEl : recordGeoEls) {

            //Create our extent nodes and insert them before supplInfo
            if (recordGeoEl instanceof CSWGeographicBoundingBox) {
                CSWGeographicBoundingBox bbox = (CSWGeographicBoundingBox) recordGeoEl;
                Node newExtent = this.document.createElementNS(nc.getNamespaceURI("gmd"), "extent");
                Node exExtent = createChildNode(newExtent, nc.getNamespaceURI("gmd"), "EX_Extent");

                Node geoEl = createChildNode(exExtent, nc.getNamespaceURI("gmd"), "geographicElement");
                Node geoBbox = createChildNode(geoEl, nc.getNamespaceURI("gmd"), "EX_GeographicBoundingBox");

                appendChildDecimal(geoBbox, nc.getNamespaceURI("gmd"), "westBoundLongitude", bbox.getWestBoundLongitude());
                appendChildDecimal(geoBbox, nc.getNamespaceURI("gmd"), "eastBoundLongitude", bbox.getEastBoundLongitude());
                appendChildDecimal(geoBbox, nc.getNamespaceURI("gmd"), "southBoundLatitude", bbox.getSouthBoundLatitude());
                appendChildDecimal(geoBbox, nc.getNamespaceURI("gmd"), "northBoundLatitude", bbox.getNorthBoundLatitude());

                supplInfo.getParentNode().insertBefore(newExtent, supplInfo);
            }
        }

        //Put all of our keywords under the "theme" category
        String[] keywords = record.getDescriptiveKeywords();
        deleteMatchingNodes(root, templateDescriptiveKeywords);//remove existing keywords
        Node gmdLanguage = (Node) CSWXPathUtil.attemptCompileXpathExpr(templateLanguageExpression).evaluate(root, XPathConstants.NODE);
        Node descriptiveKeywords = this.document.createElementNS(nc.getNamespaceURI("gmd"), "descriptiveKeywords");
        Node mdKeywords = createChildNode(descriptiveKeywords, nc.getNamespaceURI("gmd"), "MD_Keywords");
        for (String keyword : keywords) {
            appendChildCharacterString(mdKeywords, nc.getNamespaceURI("gmd"), "keyword", keyword);
        }
        //Finally add the theme category
        Node keywordType = createChildNode(mdKeywords, nc.getNamespaceURI("gmd"), "type");
        Element keywordTypeCode = createChildNode(keywordType, nc.getNamespaceURI("gmd"), "MD_KeywordTypeCode");
        keywordTypeCode.setAttributeNS("", "codeListValue", "theme");
        keywordTypeCode.setAttributeNS("", "codeList", "http://www.isotc211.org/2005/resources/Codelist/gmxCodelists.xml#MD_KeywordTypeCode");
        //And then add the new keyword node +children to our document
        gmdLanguage.getParentNode().insertBefore(descriptiveKeywords, gmdLanguage);

        //Next get our online resources into the document
        CSWOnlineResource[] onlineResources = record.getOnlineResources();
        Node mdDigitalTransferOpts = (Node) CSWXPathUtil.attemptCompileXpathExpr(templateOnlineResourcesExpression).evaluate(root, XPathConstants.NODE);//We will be inserting into the parent of the online resources
        mdDigitalTransferOpts = mdDigitalTransferOpts.getParentNode();
        deleteMatchingNodes(root, templateOnlineResourcesExpression);//remove existing keywords
        for (CSWOnlineResource onlineResource : onlineResources) {
            appendChildOnlineResource(mdDigitalTransferOpts, nc.getNamespaceURI("gmd"), "onLine", onlineResource);
        }

        //Write any legal constraints out
        String[] legalConstraints = record.getConstraints();
        if (legalConstraints != null) {
            Node mdLegalConstraints = (Node) CSWXPathUtil.attemptCompileXpathExpr(templateLegalConstraintsExpression).evaluate(root, XPathConstants.NODE);//We will be inserting into the parent of the online resources
            deleteChildNodes(mdLegalConstraints); //clear any existing constraints before writing
            for (String constraint : legalConstraints) {
                appendChildCharacterString(mdLegalConstraints, nc.getNamespaceURI("gmd"), "otherConstraints", constraint);
            }
        }

        return root;
    }

    /**
     * Creates a new CSWRecord instance parsed from the internal template of this class
     *
     * Throws an exception if the internal template cannot be parsed correctly
     *
     * @return
     * @throws XPathExpressionException
     */
    public CSWRecord transformToCSWRecord() throws XPathExpressionException {
        CSWRecord record = new CSWRecord("", "", "", "", "", new CSWOnlineResource[0], new CSWGeographicElement[0]);

        NodeList tempNodeList1 = null;

        record.setServiceName((String)CSWXPathUtil.attemptCompileXpathExpr(serviceTitleExpression).evaluate(this.template, XPathConstants.STRING));
        record.setDataIdentificationAbstract((String) CSWXPathUtil.attemptCompileXpathExpr(dataIdentificationAbstractExpression).evaluate(this.template, XPathConstants.STRING));
        record.setContactOrganisation((String) CSWXPathUtil.attemptCompileXpathExpr(contactOrganisationExpression).evaluate(this.template, XPathConstants.STRING));
        record.setContactEmail((String) CSWXPathUtil.attemptCompileXpathExpr(contactEmailExpression).evaluate(this.template, XPathConstants.STRING));
        record.setContactIndividual((String) CSWXPathUtil.attemptCompileXpathExpr(contactIndividualExpression).evaluate(this.template, XPathConstants.STRING));
        record.setFileIdentifier((String) CSWXPathUtil.attemptCompileXpathExpr(fileIdentifierExpression).evaluate(this.template, XPathConstants.STRING));
        record.setSupplementalInformation((String) CSWXPathUtil.attemptCompileXpathExpr(supplementalInfoExpression).evaluate(this.template, XPathConstants.STRING));
        record.setLanguage((String) CSWXPathUtil.attemptCompileXpathExpr(languageExpression).evaluate(this.template, XPathConstants.STRING));

        String resourceProvider = (String) CSWXPathUtil.attemptCompileXpathExpr(resourceProviderExpression).evaluate(this.template, XPathConstants.STRING);
        if (resourceProvider.equals("")) {
            resourceProvider = "Unknown";
        }
        record.setResourceProvider(resourceProvider);

        //There can be multiple gmd:onLine elements (which contain a number of fields we want)
        tempNodeList1 = (NodeList)CSWXPathUtil.attemptCompileXpathExpr(onlineTransfersExpression).evaluate(this.template, XPathConstants.NODESET);
        List<CSWOnlineResource> resources = new ArrayList<CSWOnlineResource>();
        for (int i = 0; i < tempNodeList1.getLength(); i++) {
            try {
                Node onlineNode = tempNodeList1.item(i);
                resources.add(CSWOnlineResourceFactory.parseFromNode(onlineNode));
            } catch (IllegalArgumentException ex) {
                logger.debug(String.format("Unable to parse online resource for serviceName='%1$s' %2$s",record.getServiceName(), ex));
            }
        }
        record.setOnlineResources(resources.toArray(new CSWOnlineResource[resources.size()]));

        //Parse our bounding boxes (if they exist). If any are unparsable, don't worry and just continue
        tempNodeList1 = (NodeList)CSWXPathUtil.attemptCompileXpathExpr(bboxExpression).evaluate(this.template, XPathConstants.NODESET);
        if (tempNodeList1 != null && tempNodeList1.getLength() > 0) {
            List<CSWGeographicElement> elList = new ArrayList<CSWGeographicElement>();
            for (int i = 0; i < tempNodeList1.getLength(); i++) {
                try {
                    Node geographyNode = tempNodeList1.item(i);
                    elList.add(CSWGeographicBoundingBox.fromGeographicBoundingBoxNode(geographyNode));
                } catch (Exception ex) {
                    logger.debug(String.format("Unable to parse CSWGeographicBoundingBox resource for serviceName='%1$s' %2$s",record.getServiceName(), ex));
                }
            }
            record.setCSWGeographicElements(elList.toArray(new CSWGeographicElement[elList.size()]));
        }

        //Parse the descriptive keywords
        tempNodeList1 = (NodeList) CSWXPathUtil.attemptCompileXpathExpr(keywordListExpression).evaluate(this.template, XPathConstants.NODESET);
        if (tempNodeList1 != null && tempNodeList1.getLength() > 0 ) {
            List<String> keywords = new ArrayList<String>();
            Node keyword;
            for (int j=0; j<tempNodeList1.getLength(); j++) {
                keyword = tempNodeList1.item(j);
                keywords.add(keyword.getTextContent());
            }
            record.setDescriptiveKeywords(keywords.toArray(new String[keywords.size()]));
        }

        //Parse our contact online resource
        Node tempNode = (Node) CSWXPathUtil.attemptCompileXpathExpr(contactResourceExpression).evaluate(this.template, XPathConstants.NODE);
            if (tempNode != null) {
            try {
                record.setContactResource(CSWOnlineResourceFactory.parseFromNode(tempNode));
            } catch (Exception ex) {
                logger.debug(String.format("Unable to parse contact resource for serviceName='%1$s' %2$s",record.getServiceName(), ex));
            }
        }

        //Parse any legal constraints
        tempNodeList1 = (NodeList) CSWXPathUtil.attemptCompileXpathExpr(otherConstraintsExpression).evaluate(this.template,XPathConstants.NODESET);
        if (tempNodeList1 != null && tempNodeList1.getLength() > 0) {
            List<String> constraintsList = new ArrayList<String>();
            Node constraint;
            for (int j = 0; j < tempNodeList1.getLength(); j++) {
                constraint = tempNodeList1.item(j);
                constraintsList.add(constraint.getTextContent());
            }
            record.setConstraints(constraintsList.toArray(new String[constraintsList.size()]));
        }

        return record;
    }
}
