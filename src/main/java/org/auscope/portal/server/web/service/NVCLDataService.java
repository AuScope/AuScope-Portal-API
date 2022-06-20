package org.auscope.portal.server.web.service;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;

import com.google.gson.Gson;
import com.opencsv.CSVParser;
import com.opencsv.CSVParserBuilder;
import com.opencsv.CSVReader;
import com.opencsv.CSVReaderBuilder;

import org.apache.http.Header;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpRequestBase;
import org.auscope.portal.core.server.http.HttpServiceCaller;
import org.auscope.portal.core.services.methodmakers.WFSGetFeatureMethodMaker;
import org.auscope.portal.core.util.DOMUtil;
import org.auscope.portal.server.domain.nvcldataservice.GetDatasetCollectionResponse;
import org.auscope.portal.server.domain.nvcldataservice.GetLogCollectionResponse;
import org.auscope.portal.server.domain.nvcldataservice.MosaicResponse;
import org.auscope.portal.server.domain.nvcldataservice.TSGDownloadResponse;
import org.auscope.portal.server.domain.nvcldataservice.TSGStatusResponse;
import org.auscope.portal.server.web.NVCLDataServiceMethodMaker;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.utils.URIBuilder;
/**
 * Service class for accessing an instance of a NVCLDataService web service.
 *
 * See https://twiki.auscope.org/wiki/CoreLibrary/WebServicesDevelopment for the full API this service class attempts to provide
 *
 * @author Josh Vote
 */
@Service
public class NVCLDataService {

    private HttpServiceCaller httpServiceCaller;
    private NVCLDataServiceMethodMaker methodMaker;
	class Dataset{
        String datasetID;
        String boreholeURI;
        String datasetName;
        String description;
        String spectralLogCollection;
        String imageLogCollection;
        String profLogCollection;
        String logCollection;
        String trayID;
        String sectionID;
        String domainID;
    }
    class DatasetCollection {
        Dataset[] datasetCollection;
    }
    private HashMap<String, HashMap> mapEndpoint = new HashMap<String, HashMap>();
    private HashMap<String, String> mapTsgCachePath = new HashMap<String,String>();
    private String nvclTsgFileCacheUrl;
    
    /**
     * Creates a new NVCLDataService with the specified dependencies
     */
    @Autowired
    public NVCLDataService(HttpServiceCaller httpServiceCaller, 
                            NVCLDataServiceMethodMaker methodMaker,
                            WFSGetFeatureMethodMaker wfsMethodMaker, 
                            @Value("${env.nvcl.tsgFileCacheUrl:#{null}}") String nvclTsgFileCacheUrl) {
        this.httpServiceCaller = httpServiceCaller;
        this.methodMaker = methodMaker;
        this.nvclTsgFileCacheUrl = nvclTsgFileCacheUrl;
        this.loadTsgDownloadMaps();
    }
    public String getTsgFileCacheUrl() {
        return this.nvclTsgFileCacheUrl;
    }
    /**
     * Makes and parses a getDatasetCollection request to a NVCLDataService
     * 
     * @param serviceUrl
     *            The NVCLDataService url
     * @param holeIdentifier
     *            The unique borehole ID to query
     * @throws Exception
     */
    public List<GetDatasetCollectionResponse> getDatasetCollection(String serviceUrl, String holeIdentifier)
            throws Exception {
        HttpRequestBase method = methodMaker.getDatasetCollectionMethod(serviceUrl, holeIdentifier);

        //Make our request, parse it into a DOM document
        InputStream responseStream = httpServiceCaller.getMethodResponseAsStream(method);
        Document responseDoc = DOMUtil.buildDomFromStream(responseStream);

        //Get our dataset nodes
        XPathExpression expr = DOMUtil.compileXPathExpr("DatasetCollection/Dataset");
        NodeList nodeList = (NodeList) expr.evaluate(responseDoc, XPathConstants.NODESET);

        //Parse our response objects
        List<GetDatasetCollectionResponse> responseObjs = new ArrayList<GetDatasetCollectionResponse>();
        XPathExpression exprDatasetId = DOMUtil.compileXPathExpr("DatasetID");
        XPathExpression exprDatasetName = DOMUtil.compileXPathExpr("DatasetName");
        XPathExpression exprOmUrl = DOMUtil.compileXPathExpr("OmUrl");
        for (int i = 0; i < nodeList.getLength(); i++) {
            Node node = nodeList.item(i);

            String datasetId = (String) exprDatasetId.evaluate(node, XPathConstants.STRING);
            String datasetName = (String) exprDatasetName.evaluate(node, XPathConstants.STRING);
            String omUrl = (String) exprOmUrl.evaluate(node, XPathConstants.STRING);
            responseObjs.add(new GetDatasetCollectionResponse(datasetId, datasetName, omUrl));
        }

        return responseObjs;
    }

    /**
     * Makes and parses a getLogCollection request to a NVCLDataService
     * 
     * @param serviceUrl
     *            The NVCLDataService url
     * @param datasetId
     *            The unique dataset ID to query
     * @param forMosaicService
     *            [Optional] indicates if the getLogCollection service should generate a result specifically for the use of a Mosaic Service
     * @throws Exception
     */
    public List<GetLogCollectionResponse> getLogCollection(String serviceUrl, String datasetId, Boolean forMosaicService)
            throws Exception {
        HttpRequestBase method = methodMaker.getLogCollectionMethod(serviceUrl, datasetId, forMosaicService);

        //Make our request, parse it into a DOM document
        InputStream responseStream = httpServiceCaller.getMethodResponseAsStream(method);
        Document responseDoc = DOMUtil.buildDomFromStream(responseStream);

        //Get our dataset nodes
        XPathExpression expr = DOMUtil.compileXPathExpr("LogCollection/Log");
        NodeList nodeList = (NodeList) expr.evaluate(responseDoc, XPathConstants.NODESET);

        //Parse our response objects
        List<GetLogCollectionResponse> responseObjs = new ArrayList<GetLogCollectionResponse>();
        XPathExpression exprLogId = DOMUtil.compileXPathExpr("LogID");
        XPathExpression exprLogName = null; //both logName and LogName get returned according to the value of forMosaicService
        if (forMosaicService != null && forMosaicService.booleanValue()) {
            exprLogName = DOMUtil.compileXPathExpr("LogName");
        } else {
            exprLogName = DOMUtil.compileXPathExpr("logName");
        }
        XPathExpression exprispublic = DOMUtil.compileXPathExpr("ispublic");
        XPathExpression exprSampleCount = DOMUtil.compileXPathExpr("SampleCount");
        for (int i = 0; i < nodeList.getLength(); i++) {
            Node node = nodeList.item(i);

            String logId = (String) exprLogId.evaluate(node, XPathConstants.STRING);
            String logName = (String) exprLogName.evaluate(node, XPathConstants.STRING);
            String sampleCountString = (String) exprSampleCount.evaluate(node, XPathConstants.STRING);
            String ispub = (String) exprispublic.evaluate(node, XPathConstants.STRING);

            int sampleCount = 0;
            if (sampleCountString != null && !sampleCountString.isEmpty()) {
                sampleCount = Integer.parseInt(sampleCountString);
            }
            if(ispub==null || ispub.isEmpty() || ispub.equals("true")) {
                responseObjs.add(new GetLogCollectionResponse(logId, logName, sampleCount));
            }
        }

        return responseObjs;
    }

    /**
     * Makes a mosaic request and returns the resulting data in a MosaicResponse object.
     *
     * @param serviceUrl
     *            The URL of the NVCLDataService
     * @param logId
     *            The logID (from a getLogCollection request) to query
     * @param width
     *            [Optional] specify the number of column the images are to be displayed
     * @param startSampleNo
     *            [Optional] the first sample image to be displayed
     * @param endSampleNo
     *            [Optional] the last sample image to be displayed
     * @return
     */
    public MosaicResponse getMosaic(String serviceUrl, String logId, Integer width, Integer startSampleNo,
            Integer endSampleNo) throws Exception {
        HttpRequestBase method = methodMaker.getMosaicMethod(serviceUrl, logId, width, startSampleNo, endSampleNo);

        HttpResponse httpResponse = httpServiceCaller.getMethodResponseAsHttpResponse(method);
        InputStream responseStream = httpResponse.getEntity().getContent();
        Header contentHeader = httpResponse.getEntity().getContentType();

        return new MosaicResponse(responseStream, contentHeader == null ? null : contentHeader.getValue());
    }
    

    /**
     * Makes a TSG download request and returns the resulting data in a TSGDownloadResponse object.
     *
     * One of (but not both) datasetId and matchString must be specified
     *
     * @param serviceUrl
     *            The URL of the NVCLDataService
     * @param email
     *            The user's email address
     * @param datasetId
     *            [Optional] a dataset id chosen by user (list of dataset id can be obtained thru calling the get log collection service)
     * @param matchString
     *            [Optional] Its value is part or all of a proper drillhole name. The first dataset found to match in the database is downloaded
     * @param lineScan
     *            [Optional] yes or no. If no then the main image component is not downloaded. The default is yes.
     * @param spectra
     *            [Optional] yes or no. If no then the spectral component is not downloaded. The default is yes.
     * @param profilometer
     *            [Optional] yes or no. If no then the profilometer component is not downloaded. The default is yes.
     * @param trayPics
     *            [Optional] yes or no. If no then the individual tray pictures are not downloaded. The default is yes.
     * @param mosaicPics
     *            [Optional] yes or no. If no then the hole mosaic picture is not downloaded. The default is yes.
     * @param mapPics
     *            [Optional] yes or no. If no then the map pictures are not downloaded. The default is yes.
     * @return
     */
    public TSGDownloadResponse getTSGDownload(String serviceUrl, String email, String datasetId, String matchString,
            Boolean lineScan, Boolean spectra, Boolean profilometer, Boolean trayPics, Boolean mosaicPics,
            Boolean mapPics) throws Exception {
        HttpRequestBase method = methodMaker.getDownloadTSGMethod(serviceUrl, email, datasetId, matchString, lineScan,
                spectra, profilometer, trayPics, mosaicPics, mapPics);

        HttpResponse httpResponse = httpServiceCaller.getMethodResponseAsHttpResponse(method);
        InputStream responseStream = httpResponse.getEntity().getContent();
        Header contentHeader = httpResponse.getEntity().getContentType();

        return new TSGDownloadResponse(responseStream, contentHeader == null ? null : contentHeader.getValue());
    }

    /**
     * Checks a user's TSG download status
     *
     * This method will return a HTML stream
     *
     * @param serviceUrl
     *            The URL of the NVCLDataService
     * @param email
     *            The user's email address
     * @return
     * @throws Exception
     */
    public TSGStatusResponse checkTSGStatus(String serviceUrl, String email) throws Exception {
        HttpRequestBase method = methodMaker.getCheckTSGStatusMethod(serviceUrl, email);

        HttpResponse httpResponse = httpServiceCaller.getMethodResponseAsHttpResponse(method);
        InputStream responseStream = httpResponse.getEntity().getContent();
        Header contentHeader = httpResponse.getEntity().getContentType();

        return new TSGStatusResponse(responseStream, contentHeader == null ? null : contentHeader.getValue());
    }

    /**
     * getTsgFileUrls to get TSG File download Urls.
     *
     * @param serviceUrl
     *            The URL of the NVCLDataService
     * @param endpoint
     * @param csv 
     * @param email
     *            The user's email address
     * @return
     * @throws Exception
     */
    public String  getTsgFileUrls(String endpoint, String  csv) throws Exception {
        HashMap<String, String> mapDatasetCollection = this.mapEndpoint.get(endpoint);
        String cacheUrlPath  = this.mapTsgCachePath.get(endpoint);
        if (mapDatasetCollection == null) {
            return null;
        }
        InputStream inputstreamCSV = new ByteArrayInputStream(csv.getBytes());

        StringBuilder sb = new StringBuilder();

        CSVParser parser = new CSVParserBuilder().withSeparator(',').withQuoteChar('"').build();
        CSVReader reader = new CSVReaderBuilder(new InputStreamReader(inputstreamCSV)).withCSVParser(parser).build();
        String[] headerLine = reader.readNext();
        int indexOfIdentifier = Arrays.asList(headerLine).indexOf("gsmlp:identifier");
        if (headerLine == null || headerLine.length <= 2 || indexOfIdentifier < 0) {
            throw new Exception("No or malformed CSV header sent");
        }


        //Start parsing our data - loading it into bins
        String[] dataLine = null;
        ArrayList<String> tsgFileCacheUrlList =new ArrayList<String> ();
        while ((dataLine = reader.readNext()) != null) {
            if (dataLine.length != headerLine.length) {
                continue; //skip malformed lines
            }
            //example: http://geossdi.dmp.wa.gov.au/resource/feature/gswa/borehole/ABDP1
            String boreholeURI = dataLine[indexOfIdentifier];
            //Filter out bad boreholeURI
            if (!boreholeURI.startsWith("http")){
                continue;
            }
            //remove starter of http or https.
            int index = boreholeURI.indexOf("//");
            if (index > 0 ) {
                boreholeURI = boreholeURI.substring( index +2);
            }            
            String bhDatasetName = mapDatasetCollection.get(boreholeURI);
            String tsgFileCacheURL="";

            if (bhDatasetName == null) {
                tsgFileCacheURL = "NoMatchedDatasetName-https://" + boreholeURI + "\n";
                System.out.println(tsgFileCacheURL);
            } else {
                tsgFileCacheURL = cacheUrlPath + bhDatasetName + ".zip\n"; 
                //LINGBO https://nvclanalyticscache.z8.web.core.windows.net/WA/PDD446.zip
            }
            sb.append(tsgFileCacheURL);
            tsgFileCacheUrlList.add(tsgFileCacheURL);
        }
        return sb.toString();
    }    

    /**
     * getDatasetCollectionMap to get a map for <boreholeURI, datasetName> 
     *
     * @param serviceUrl
     *            The URL of the NVCLDataService
     * @param holeIdentifier
     * @return
     * @throws Exception
     */
    public HashMap<String, String> getDatasetCollectionMap(String serviceUrl, String holeIdentifier) throws Exception {
        //https://geology.data.nt.gov.au/NVCLDataServices/getDatasetCollection.html?holeidentifier=all&headersonly=yes&outputformat=json
        HashMap<String, String> mapDatasetCollection = new HashMap<String, String>();
        HttpGet method = new HttpGet();
        URIBuilder builder = new URIBuilder(serviceUrl + "getDatasetCollection.html");
        System.out.println("getDatasetCollectionMap:" + serviceUrl);
        //set all of the parameters.
        builder.setParameter("holeidentifier", holeIdentifier);
        builder.setParameter("headersonly", "yes");
        builder.setParameter("outputformat", "json");
        method.setURI(builder.build());

        HttpServiceCaller httpServiceCaller = new HttpServiceCaller(90000);
        //Make our request, parse it into a DOM document
        String response = httpServiceCaller.getMethodResponseAsString(method);
        DatasetCollection datasetCollection = new Gson().fromJson(response, DatasetCollection.class);

        for (Dataset dataset : datasetCollection.datasetCollection) {
            String boreholeURI =dataset.boreholeURI;
            //remove starter of http or https.
            int index = dataset.boreholeURI.indexOf("//");
            if (index > 0 ) {
                boreholeURI = boreholeURI.substring( index +2);
            }  
            mapDatasetCollection.put(boreholeURI, dataset.datasetName);
            //System.out.println(boreholeURI +  ',' + dataset.datasetName);
        }
        System.out.println("getDatasetCollectionMap:" + mapDatasetCollection.size());
        return mapDatasetCollection;
    }

    /**
     * loadTsgDownloadMaps to prepare map for DatasetCollection. 
     * @return
     */
    private void loadTsgDownloadMaps() {
        if (this.mapEndpoint.size() > 0) {
            return;
        }
        this.mapEndpoint.clear();
        this.mapTsgCachePath.clear();
        String endpoint;

        try {
            endpoint = "https://www.mrt.tas.gov.au/";
            this.mapEndpoint.put(endpoint,this.getDatasetCollectionMap(endpoint + "NVCLDataServices/", "all"));
            this.mapTsgCachePath.put(endpoint,this.nvclTsgFileCacheUrl+"Tas/");

            endpoint = "https://geossdi.dmp.wa.gov.au/";
            this.mapEndpoint.put(endpoint,this.getDatasetCollectionMap(endpoint + "NVCLDataServices/", "all"));
            this.mapTsgCachePath.put(endpoint,this.nvclTsgFileCacheUrl+"WA/");

            endpoint = "https://geology.data.nt.gov.au/";
            this.mapEndpoint.put(endpoint,this.getDatasetCollectionMap(endpoint + "NVCLDataServices/", "all"));
            this.mapTsgCachePath.put(endpoint,this.nvclTsgFileCacheUrl+"NT/");

            endpoint = "https://gs.geoscience.nsw.gov.au/";
            this.mapEndpoint.put(endpoint,this.getDatasetCollectionMap(endpoint + "NVCLDataServices/", "all"));
            this.mapTsgCachePath.put(endpoint,this.nvclTsgFileCacheUrl+"NSW/");

            endpoint = "https://sarigdata.pir.sa.gov.au/";
            this.mapEndpoint.put(endpoint,this.getDatasetCollectionMap(endpoint + "NVCLDataServices/", "all"));
            this.mapTsgCachePath.put(endpoint,this.nvclTsgFileCacheUrl+"SA/");
/* getDatasetCollection headless  doesnot works on QLD and VIC yet.
        endpoint = "https://geology-uat.information.qld.gov.au/";
        this.mapEndpoint.put(endpoint,this.getDatasetCollectionMap(endpoint + "NVCLDataServices/", "all"));
        this.mapTsgCachePath.put(endpoint,this.nvclTsgFileCacheUrl+"Qld/");

        endpoint = "https://geology.information.qld.gov.au/";
        this.mapEndpoint.put(endpoint,this.getDatasetCollectionMap(endpoint + "NVCLDataServices/", "all"));
        this.mapTsgCachePath.put(endpoint,this.nvclTsgFileCacheUrl+"Qld/");

        endpoint = "https://geology.data.vic.gov.au/";
        this.mapEndpoint.put(endpoint,this.getDatasetCollectionMap(endpoint + "NVCLDataServices/", "all"));
        this.mapTsgCachePath.put(endpoint,this.nvclTsgFileCacheUrl+"Vic/");
        logger.trace("downloadTsgFiles.do: Loaded maps: " + this.mapEndpoint.size());
*/      } catch (Exception e) {
            e.printStackTrace();
        }
        return;
    }        
}
