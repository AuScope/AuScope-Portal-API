package org.auscope.portal.server.web.controllers;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Arrays;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;
import java.nio.charset.StandardCharsets;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletResponse;

import net.sf.json.JSONArray;

import org.apache.commons.httpclient.HttpStatus;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.auscope.portal.core.server.controllers.BasePortalController;
import org.auscope.portal.core.services.CSWCacheService;
import org.auscope.portal.core.services.csw.CSWRecordsHostFilter;
import org.auscope.portal.core.services.methodmakers.filter.FilterBoundingBox;
import org.auscope.portal.core.services.responses.wfs.WFSResponse;
import org.auscope.portal.core.util.FileIOUtil;
import org.auscope.portal.server.domain.nvcldataservice.AbstractStreamResponse;
import org.auscope.portal.server.domain.nvcldataservice.AlgorithmOutputClassification;
import org.auscope.portal.server.domain.nvcldataservice.AlgorithmOutputResponse;
import org.auscope.portal.server.domain.nvcldataservice.AnalyticalJobResults;
import org.auscope.portal.server.domain.nvcldataservice.AnalyticalJobStatus;
import org.auscope.portal.server.domain.nvcldataservice.BinnedCSVResponse;
import org.auscope.portal.server.domain.nvcldataservice.CSVDownloadResponse;
import org.auscope.portal.server.domain.nvcldataservice.GetDatasetCollectionResponse;
import org.auscope.portal.server.domain.nvcldataservice.GetLogCollectionResponse;
import org.auscope.portal.server.domain.nvcldataservice.ImageTrayDepthResponse;
import org.auscope.portal.server.domain.nvcldataservice.MosaicResponse;
import org.auscope.portal.server.domain.nvcldataservice.TSGDownloadResponse;
import org.auscope.portal.server.domain.nvcldataservice.TSGStatusResponse;
import org.auscope.portal.server.domain.nvcldataservice.TrayThumbNailResponse;
import org.auscope.portal.server.web.service.BoreholeService;
import org.auscope.portal.server.web.service.NVCL2_0_DataService;
import org.auscope.portal.server.web.service.NVCLDataService;
import org.auscope.portal.server.web.service.SF0BoreholeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;

/**
 * Controller for handling requests for the NVCL boreholes
 *
 * @author Josh Vote
 *
 */
@Controller
public class NVCLController extends BasePortalController {

    private BoreholeService boreholeService;
    private NVCLDataService dataService;
    private NVCL2_0_DataService dataService2_0;
    private CSWCacheService cswService;
    private SF0BoreholeService sf0BoreholeService;

    private int BUFFERSIZE = 1024 * 1024;

    @Autowired
    public NVCLController(BoreholeService boreholeService,
            CSWCacheService cswService,
            NVCLDataService dataService,
            NVCL2_0_DataService dataService2_0,
            SF0BoreholeService sf0BoreholeService) {

        this.boreholeService = boreholeService;
        this.cswService = cswService;
        this.dataService = dataService;
        this.dataService2_0 = dataService2_0;
        this.sf0BoreholeService = sf0BoreholeService;
    }
 
    

    /**
     * Handles the borehole filter queries.
     *
     * @param serviceUrl
     *            the url of the service to query
     * @param mineName
     *            the name of the mine to query for
     * @param request
     *            the HTTP client request
     * @return a WFS response converted into KML
     * @throws Exception
     */
    public ModelAndView doBoreholeFilter(String serviceUrl, String boreholeName, String custodian,
            String dateOfDrillingStart,String dateOfDrillingEnd, int maxFeatures, FilterBoundingBox bbox,
            boolean onlyHylogger, String outputFormat, boolean countOnly,String optionalFilters) throws Exception {
        List<String> hyloggerBoreholeIDs = null;
        if (onlyHylogger) {
            try {
                hyloggerBoreholeIDs = this.boreholeService.discoverHyloggerBoreholeIDs(this.cswService,
                        new CSWRecordsHostFilter(serviceUrl));
            } catch (Exception e) {
                log.warn(String
                        .format("Error requesting list of hylogger borehole ID's from %1$s: %2$s", serviceUrl, e));
                log.debug("Exception:", e);
                return generateJSONResponseMAV(false, null,
                        "Failure when identifying which boreholes have Hylogger data.");
            }

            if (hyloggerBoreholeIDs.size() == 0) {
                log.warn("No hylogger boreholes exist (or the services are missing)");
                return generateJSONResponseMAV(false, null, "Unable to identify any boreholes with Hylogger data.");
            }
        }

        try {
            if (countOnly) {
                int count = this.boreholeService.countAllBoreholes(serviceUrl, boreholeName, custodian, dateOfDrillingStart, dateOfDrillingEnd, maxFeatures, bbox, hyloggerBoreholeIDs);
                return generateJSONResponseMAV(true, count, "");
            } else {
                WFSResponse response = this.boreholeService.getAllBoreholes(serviceUrl, boreholeName, custodian,
                        dateOfDrillingStart,dateOfDrillingEnd, maxFeatures, bbox, hyloggerBoreholeIDs, outputFormat,optionalFilters);
                return generateNamedJSONResponseMAV(true, "gml", response.getData(), null);
            }
        } catch (Exception e) {
            log.info("Error performing borehole filter: ", e);
            return this.generateExceptionResponse(e, serviceUrl);
        }

    }

    /**
     * Gets the list of datasets for given borehole from the specified NVCL dataservice url.
     *
     * @param serviceUrl
     *            The URL of an NVCL Data service
     * @param holeIdentifier
     *            The unique ID of a borehole
     * @return
     */
    @RequestMapping("getNVCLDatasets.do")
    public ModelAndView getNVCLDatasets(@RequestParam("serviceUrl") String serviceUrl,
            @RequestParam("holeIdentifier") String holeIdentifier) {
        List<GetDatasetCollectionResponse> responseObjs = null;
        try {
            responseObjs = dataService.getDatasetCollection(serviceUrl, holeIdentifier);

            return generateJSONResponseMAV(true, responseObjs, "");
        } catch (Exception ex) {
            log.warn(String.format("Error requesting dataset collection for hole '%1$s' from %2$s: %3$s",
                    holeIdentifier, serviceUrl, ex));
            log.debug("Exception:", ex);
            return generateJSONResponseMAV(false);
        }
    }

    /**
     * Gets the list of logs for given NVCL dataset from the specified NVCL dataservice url.
     *
     * @param serviceUrl
     *            The URL of an NVCL Data service
     * @param datasetId
     *            The unique ID of a dataset
     * @return
     */
    @RequestMapping("getNVCLLogs.do")
    public ModelAndView getNVCLLogs(@RequestParam("serviceUrl") String serviceUrl,
            @RequestParam("datasetId") String datasetId,
            @RequestParam(required = false, value = "mosaicService") Boolean forMosaicService) {
        List<GetLogCollectionResponse> responseObjs = null;
        try {
            responseObjs = dataService.getLogCollection(serviceUrl, datasetId, forMosaicService);

            return generateJSONResponseMAV(true, responseObjs, "");
        } catch (Exception ex) {
            log.warn(String.format("Error requesting log collection for dataset '%1$s' from %2$s: %3$s", datasetId,
                    serviceUrl, ex));
            log.debug("Exception:", ex);
            return generateJSONResponseMAV(false);
        }
    }

    /**
     * Gets the list of logs for given NVCL dataset from the specified NVCL dataservice url.
     *
     * @param serviceUrl
     *            The URL of an NVCL Data service
     * @param datasetId
     *            The unique ID of a dataset
     * @return
     */
    @RequestMapping("getNVCL2_0_Logs.do")
    public ModelAndView getNVCL2_0_Logs(@RequestParam("serviceUrl") String serviceUrl,
            @RequestParam("datasetId") String datasetId,
            @RequestParam(required = false, value = "mosaicService") Boolean forMosaicService) {
        List<GetLogCollectionResponse> responseObjs = null;
        try {
            responseObjs = dataService2_0.getLogCollection(serviceUrl, datasetId, forMosaicService);

            return generateJSONResponseMAV(true, responseObjs, "");
        } catch (Exception ex) {
            log.warn(String.format("Error requesting log collection for dataset '%1$s' from %2$s: %3$s", datasetId,
                    serviceUrl, ex));
            log.debug("Exception:", ex);
            return generateJSONResponseMAV(false);
        }
    }

    /**
     * Utility function for piping the contents of serviceResponse to servletResponse
     */
    private void writeStreamResponse(HttpServletResponse servletResponse, AbstractStreamResponse serviceResponse)
            throws IOException {
        InputStream serviceInputStream = serviceResponse.getResponse();
        OutputStream responseOutput = null;

        //write our response
        try {
            servletResponse.setContentType(serviceResponse.getContentType());
            responseOutput = servletResponse.getOutputStream();

            FileIOUtil.writeInputToOutputStream(serviceInputStream, responseOutput, BUFFERSIZE, true);
        } finally {
            if (responseOutput != null) {
                responseOutput.close();
            }
        }
    }

    /**
     * Proxies an NVCL Mosaic request for mosaic imagery. Writes directly to the HttpServletResponse
     *
     * @param serviceUrl
     *            The URL of an NVCL Data service
     * @param logId
     *            The unique ID of a log (from a getNVCLLogs.do request)
     * @return
     */
    @RequestMapping("getNVCLMosaic.do")
    public void getNVCLMosaic(@RequestParam("serviceUrl") String serviceUrl,
            @RequestParam("logId") String logId,
            @RequestParam(required = false, value = "width") Integer width,
            @RequestParam(required = false, value = "startSampleNo") Integer startSampleNo,
            @RequestParam(required = false, value = "endSampleNo") Integer endSampleNo,
            HttpServletResponse response) throws Exception {

        //Make our request
        MosaicResponse serviceResponse = null;
        try {
            serviceResponse = dataService.getMosaic(serviceUrl, logId, width, startSampleNo, endSampleNo);
        } catch (Exception ex) {
            log.warn(String.format("Error requesting mosaic for logid '%1$s' from %2$s: %3$s", logId, serviceUrl, ex));
            log.debug("Exception:", ex);
            response.sendError(HttpStatus.SC_INTERNAL_SERVER_ERROR);
            return;
        }

        writeStreamResponse(response, serviceResponse);
    }

    /**
     * Proxies an NVCL 2.0 Mosaic request for mosaic imagery. Writes directly to the HttpServletResponse
     *
     * @param serviceUrl
     *            The URL of an NVCL Data service
     * @param logId
     *            The unique ID of a log (from a getNVCLLogs.do request)
     * @return
     */
    @RequestMapping("getNVCL2_0_Thumbnail.do")
    public void getNVCL2_0_Thumbnail(@RequestParam("serviceUrl") String serviceUrl,
            @RequestParam("dataSetId") String dataSetId,
            @RequestParam("logId") String logId,
            @RequestParam(required = false, value = "width") Integer width,
            @RequestParam(required = false, value = "startSampleNo") Integer startSampleNo,
            @RequestParam(required = false, value = "endSampleNo") Integer endSampleNo,
            HttpServletResponse response) throws Exception {

        //Make our request
        TrayThumbNailResponse serviceResponse = null;
        try {
            serviceResponse = this.dataService2_0.getTrayThumbNail(dataSetId, serviceUrl, logId, width, startSampleNo,
                    endSampleNo);
        } catch (Exception ex) {
            log.warn(String.format("Error requesting mosaic for logid '%1$s' from %2$s: %3$s", logId, serviceUrl, ex));
            log.debug("Exception:", ex);
            response.sendError(HttpStatus.SC_INTERNAL_SERVER_ERROR);
            return;
        }

        response.setContentType(serviceResponse.getContentType());
        //vt:we have to hack the response because the html response has relative url and when
        //the result is proxied, the service url becomes portal's url.
        String stringResponse = IOUtils.toString(serviceResponse.getResponse(), StandardCharsets.UTF_8);
        stringResponse = stringResponse.replace("./Display_Tray_Thumb.html", serviceUrl + "Display_Tray_Thumb.html");
        if (!stringResponse.contains("style=\"max-width: 33%")) {
            stringResponse = stringResponse.replace("<img",
                    "<img style=\"max-width: 33%;height: auto;width: auto\\9;\" ");
        }

        FileIOUtil.writeInputToOutputStream(new ByteArrayInputStream(stringResponse.getBytes()),
                response.getOutputStream(), BUFFERSIZE, true);

    }


    /**
     * Proxies a CSV download request to a WFS from an NVCL 2.0 service. Writes directly to the HttpServletResponse
     *
     * @param serviceUrl
     *            The URL of an observation and measurements URL (obtained from a getDatasetCollection response)
     * @param datasetId
     *            The dataset to download
     * @return
     */
    @RequestMapping("getNVCL2_0_CSVDownload.do")
    public void getNVCL2_0_CSVDownload(@RequestParam("serviceUrl") String serviceUrl,
            @RequestParam("logIds") String[] logIds,
            HttpServletResponse response) throws Exception {

        //Make our request
        CSVDownloadResponse serviceResponse = null;
        try {
            serviceResponse = dataService2_0.getNVCL2_0_CSVDownload(serviceUrl, logIds);
        } catch (Exception ex) {
            log.warn(String.format("Error requesting csw download for logId '%1$s' from %2$s: %3$s", logIds,
                    serviceUrl, ex));
            log.debug("Exception:", ex);
            if (ex.getMessage().contains("404")) {
                String htmlMessage = "<html><head><title>Error 404</title></head>"
                        +
                        "<body><h1>HTTP Status 404 - </h1><p>You could be seeing this error because the service does not support this operation</p>"
                        +
                        "</body></html>";

                FileIOUtil.writeInputToOutputStream(new ByteArrayInputStream(htmlMessage.getBytes()),
                        response.getOutputStream(), BUFFERSIZE, true);
                return;
            } else {
                throw new ServletException(ex);
            }
        }

        response.setHeader("Content-Disposition", "attachment; filename=downloadScalar.csv");
        writeStreamResponse(response, serviceResponse);
    }

    /**
     * Proxies a CSV download request to a WFS from an NVCL 2.0 service. Parses the response into a series of 1m averaged bins.
     *
     * @param serviceUrl
     *            The URL of an observation and measurements URL (obtained from a getDatasetCollection response)
     * @param datasetId
     *            The dataset to download
     * @return
     */
    @RequestMapping("getNVCL2_0_JSONDataBinned.do")
    public ModelAndView getNVCL2_0_JSONDataBinned(@RequestParam("serviceUrl") String serviceUrl,
            @RequestParam("logIds") String[] logIds) throws Exception {

        //Make our request
        try {
            String responseStr = dataService2_0.getNVCL2_0_JSONDownsampledData(serviceUrl, logIds);
            return generateJSONResponseMAV(true, responseStr, "");

        } catch (Exception ex) {
            log.warn(String.format("Error requesting json download for logId '%1$s' from %2$s: %3$s", logIds,serviceUrl, ex));
            log.debug("Exception:", ex);
            return generateJSONResponseMAV(false);
        }
    }
    
    /**
     * Fetches NVCL TSG Jobs data
     * @param jobId
     *          job id of data to be downloaded
     * @param boreholeId
     *          borehole id of data to be downloaded
     */
    @RequestMapping("getNVCL2_0_JobsScalarBinned.do")
    public ModelAndView getNVCL2_0_JobsScalarBinned(@RequestParam("jobIds") String[] jobIds, @RequestParam("boreholeId") String boreholeId) {
        
        //Make our request
        try {
            BinnedCSVResponse response = dataService2_0.getNVCL2_0_JobsScalarBinned(jobIds, boreholeId, 1.0);

            return generateJSONResponseMAV(true, Arrays.asList(response), "");

        } catch (Exception ex) {
            log.warn(String.format("Error requesting scalar csv download from NVCL job for boreholeId '%1$s': %2$s", boreholeId, ex));
            log.debug("Exception:", ex);
            return generateJSONResponseMAV(false);
        }  
    }

    /**
     * Request for mineral colours for NVCL graphs
     *
     * @param serviceUrl
     *          The URL of colour table request
     * @param logId
                logId of the dataset
     *
     * @return
     */
    @RequestMapping("getNVCL2_0_MineralColourTable.do")
    public ModelAndView getNVCL2_0_MineralColourTable(@RequestParam("serviceUrl") String serviceUrl,
            @RequestParam("logIds") String[] logIds) throws Exception {
        //Make our request
        try {
            String responseStr = dataService2_0.getNVCL2_0_MineralColourTable(serviceUrl, logIds);
            return generateJSONResponseMAV(true, responseStr, "");

        } catch (Exception ex) {
            log.warn(String.format("Error requesting colour table for logId '%1$s' from %2$s: %3$s", logIds, serviceUrl, ex));
            log.debug("Exception:", ex);
            return generateJSONResponseMAV(false);
        }
    }
    
    /**
     * Request for TSG job id and job name for a borehole id
     *
     * @param serviceUrl
     *          The URL of job id request
     * @param boreholeId
                boreholeId of the job
     *
     * @return
     */
    @RequestMapping("getNVCL2_0_TsgJobsByBoreholeId.do")
    public ModelAndView getNVCL2_0_TsgJobsByBoreholeId(@RequestParam("boreholeId") String boreholeId,
            @RequestParam(required = false, value = "email") String email) throws Exception {
        //Make our request
        try {
            JSONArray jsonResponse = dataService2_0.getNVCL2_0_getTsgJobsByBoreholeId(boreholeId, email);
            return generateJSONResponseMAV(true, jsonResponse, "");

        } catch (Exception ex) {
            log.warn(String.format("Error requesting TSG job id for borehole id '%1$s': %2$s", boreholeId, ex));
            log.debug("Exception:", ex);
            return generateJSONResponseMAV(false);
        }
    }
    

    /**
     * Proxies a NVCL TSG download request. Writes directly to the HttpServletResponse
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
    @RequestMapping("getNVCLTSGDownload.do")
    public void getNVCLTSGDownload(@RequestParam("serviceUrl") String serviceUrl,
            @RequestParam("email") String email,
            @RequestParam(required = false, value = "datasetId") String datasetId,
            @RequestParam(required = false, value = "matchString") String matchString,
            @RequestParam(required = false, value = "lineScan") Boolean lineScan,
            @RequestParam(required = false, value = "spectra") Boolean spectra,
            @RequestParam(required = false, value = "profilometer") Boolean profilometer,
            @RequestParam(required = false, value = "trayPics") Boolean trayPics,
            @RequestParam(required = false, value = "mosaicPics") Boolean mosaicPics,
            @RequestParam(required = false, value = "mapPics") Boolean mapPics,
            HttpServletResponse response) throws Exception {

        //It's likely that the GUI (due to its construction) may send multiple email parameters
        //Spring condenses this into a single CSV string (which is bad)
        email = email.split(",")[0];

        //Make our request
        TSGDownloadResponse serviceResponse = null;
        try {
            serviceResponse = dataService.getTSGDownload(serviceUrl, email, datasetId, matchString, lineScan, spectra,
                    profilometer, trayPics, mosaicPics, mapPics);
        } catch (Exception ex) {
            if (ex.getMessage().contains("404 Not Found")) {
                FileIOUtil.writeInputToOutputStream(NVCLController.get404HTMLError(), response.getOutputStream(),
                        BUFFERSIZE, true);
                return;
            }
            log.warn(String.format("Error requesting tsg download from %1$s: %2$s", serviceUrl, ex));
            log.debug("Exception:", ex);
            response.sendError(HttpStatus.SC_INTERNAL_SERVER_ERROR);
            return;
        }

        response.setContentType(serviceResponse.getContentType());
        String stringResponse = IOUtils.toString(serviceResponse.getResponse(), StandardCharsets.UTF_8);
        stringResponse = stringResponse.replace("downloadtsg.html", serviceUrl + "downloadtsg.html");

        FileIOUtil.writeInputToOutputStream(new ByteArrayInputStream(stringResponse.getBytes()),
                response.getOutputStream(), BUFFERSIZE, true);

    }

    /**
     * Proxies a NVCL TSG status request. Writes directly to the HttpServletResponse
     *
     * @param serviceUrl
     *            The URL of the NVCLDataService
     * @param email
     *            The user's email address
     * @return
     */
    @RequestMapping("getNVCLTSGDownloadStatus.do")
    public void getNVCLTSGDownloadStatus(@RequestParam("serviceUrl") String serviceUrl,
            @RequestParam("email") String email,
            HttpServletResponse response) throws Exception {

        //Make our request
        TSGStatusResponse serviceResponse = null;
        try {
            serviceResponse = dataService.checkTSGStatus(serviceUrl, email);
        } catch (Exception ex) {
            if (ex.getMessage().contains("404 Not Found")) {
                FileIOUtil.writeInputToOutputStream(NVCLController.get404HTMLError(), response.getOutputStream(),
                        BUFFERSIZE, true);
                return;
            }
            log.warn(String.format("Error requesting tsg status from %1$s: %2$s", serviceUrl, ex));
            log.debug("Exception:", ex);
            response.sendError(HttpStatus.SC_INTERNAL_SERVER_ERROR);
            return;
        }

        response.setContentType(serviceResponse.getContentType());
        String stringResponse = IOUtils.toString(serviceResponse.getResponse(), StandardCharsets.UTF_8);
        stringResponse = stringResponse.replace("downloadtsg.html", serviceUrl + "downloadtsg.html");
        stringResponse = stringResponse.replace("href", " target='_blank' href");

        FileIOUtil.writeInputToOutputStream(new ByteArrayInputStream(stringResponse.getBytes()),
                response.getOutputStream(), BUFFERSIZE, true);

        //writeStreamResponse(response, serviceResponse);
    }

    private static InputStream get404HTMLError() throws IOException {
        InputStream input = NVCLController.class.getResourceAsStream("/htmlerror/NVCL404Response.htm");
        return input;
    }

    /**
     * Proxies an NVCL getTsgAlgorithms request. Returns a JSON response
     *
     * @param serviceUrl
     *            The URL of the NVCLDataService
     * @return
     */
    @RequestMapping("getTsgAlgorithms.do")
    public ModelAndView getTsgAlgorithms(@RequestParam("tsgAlgName") String tsgAlgName) throws Exception {
        try {
            String algorithms = dataService2_0.getTsgAlgorithms(tsgAlgName);
            return generateJSONResponseMAV(true, algorithms, "");
        } catch (Exception ex) {
            log.warn("Unable to fetch Tsg algorithms for " + tsgAlgName + ex);
            return generateJSONResponseMAV(false);
        }
    }        
        
    /**
     * Proxies an NVCL getAlgorithms request. Returns a JSON response
     *
     * @param serviceUrl
     *            The URL of the NVCLDataService
     * @return
     */
    @RequestMapping("getNVCLAlgorithms.do")
    public ModelAndView getNVCLWFSDownloadStatus(@RequestParam("serviceUrl") String serviceUrl) throws Exception {
        try {
            List<AlgorithmOutputResponse> algorithms = dataService2_0.getAlgorithms(serviceUrl);
            return generateJSONResponseMAV(true, algorithms, "");
        } catch (Exception ex) {
            log.warn("Unable to fetch NVCL algorithms for " + serviceUrl, ex);
            return generateJSONResponseMAV(false);
        }
    }

    /**
     * Proxies an NVCL getClassifications request. Returns a JSON response
     *
     * @param serviceUrl
     *            The URL of the NVCLDataService
     * @return
     */
    @RequestMapping("getNVCLClassifications.do")
    public ModelAndView getNVCLWFSDownloadStatus(
            @RequestParam("serviceUrl") String serviceUrl,
            @RequestParam("algorithmOutputId") String[] algorithmOutputIdStrings) throws Exception {

        int[] algorithmOutputIds = new int[algorithmOutputIdStrings.length];
        for (int i = 0; i < algorithmOutputIds.length; i++) {
            algorithmOutputIds[i] = Integer.parseInt(algorithmOutputIdStrings[i]);
        }

        try {
            List<AlgorithmOutputClassification> classifications = dataService2_0.getClassifications(serviceUrl, algorithmOutputIds);
            return generateJSONResponseMAV(true, classifications, "");
        } catch (Exception ex) {
            log.warn("Unable to fetch NVCL classifications for " + serviceUrl + " and algorithmOutputId " + algorithmOutputIdStrings, ex);
            return generateJSONResponseMAV(false);
        }
    }

    /**
     * Submits an NVCL processing job to the remote analytical services
     *
     * @return
     * @throws Exception
     */
    @RequestMapping("/submitSF0NVCLProcessingJob.do")
    public ModelAndView submitSF0NVCLProcessingJob(
            @RequestParam("email") String email,
            @RequestParam("jobName") String jobName,

            @RequestParam("wfsUrl") String[] wfsUrls,

            @RequestParam(required = false, value = "boreholeName", defaultValue = "") String boreholeName,
            @RequestParam(required = false, value = "dateOfDrillingStart", defaultValue = "") String dateOfDrillingStart,
            @RequestParam(required = false, value = "dateOfDrillingEnd", defaultValue = "") String dateOfDrillingEnd,
            @RequestParam(required = false, value = "bbox") String bboxJson,

            @RequestParam(required = false, value = "algorithmOutputId") String[] algorithmOutputIds,
            @RequestParam(required = false, value = "logName") String logName,

            @RequestParam(required = false, value = "classification") String classification,
            @RequestParam(required = false, value = "ogcFilter") String ogcFilter,  
            
            @RequestParam("startDepth") int startDepth,
            @RequestParam("endDepth") int endDepth,
            @RequestParam("operator") String operator,
            @RequestParam("value") String value,
            @RequestParam("units") String units,
            @RequestParam("span") int span)
                    throws Exception {


        if ((ArrayUtils.isEmpty(algorithmOutputIds) && StringUtils.isEmpty(logName)) ||
                ArrayUtils.isNotEmpty(algorithmOutputIds) && StringUtils.isNotEmpty(logName)) {
            return generateJSONResponseMAV(false, null, "Must define exactly one of algorithmOutputId or logName");
        }

        FilterBoundingBox bbox = FilterBoundingBox.attemptParseFromJSON(bboxJson);
        if (StringUtils.isEmpty(ogcFilter)) {
            ogcFilter = sf0BoreholeService.getFilter(boreholeName, "", dateOfDrillingStart, dateOfDrillingEnd, -1, bbox, null, true,null);
        }

        try {
            boolean result = this.dataService2_0.submitProcessingJob(email, jobName, wfsUrls, ogcFilter, algorithmOutputIds, logName, classification, startDepth, endDepth, operator, value, units, span);
            return generateJSONResponseMAV(result);
        } catch (Exception ex) {
            log.error("Unable to submit processing job: " + ex.getMessage());
            log.debug("Exception: ", ex);
            return generateJSONResponseMAV(false);
        }
    }

    /**
     * Submits an NVCL processing TsgJob to the remote analytical services
     *
     * @return
     * @throws Exception
     */
    @RequestMapping("/submitSF0NVCLProcessingTsgJob.do")
    public ModelAndView submitSF0NVCLProcessingTsgJob(
            @RequestParam("email") String email,
            @RequestParam("jobName") String jobName,

            @RequestParam("wfsUrl") String[] wfsUrls,

            @RequestParam(required = false, value = "boreholeName", defaultValue = "") String boreholeName,
            @RequestParam(required = false, value = "dateOfDrillingStart", defaultValue = "") String dateOfDrillingStart,
            @RequestParam(required = false, value = "dateOfDrillingEnd", defaultValue = "") String dateOfDrillingEnd,
            @RequestParam(required = false, value = "bbox") String bboxJson,
            @RequestParam(required = false, value = "tsgAlgName") String tsgAlgName,
            @RequestParam(required = false, value = "tsgAlgorithm") String tsgAlgorithm,  
            @RequestParam(required = false, value = "ogcFilter") String ogcFilter,  

            @RequestParam("startDepth") int startDepth,
            @RequestParam("endDepth") int endDepth,
            @RequestParam("operator") String operator,
            @RequestParam("value") String value,
            @RequestParam("units") String units,
            @RequestParam("span") int span)
                    throws Exception {


        if ((StringUtils.isEmpty(tsgAlgorithm))) {
            return generateJSONResponseMAV(false, null, "Must define tsgAlgorithm");
        }

        FilterBoundingBox bbox = FilterBoundingBox.attemptParseFromJSON(bboxJson);
        if (StringUtils.isEmpty(ogcFilter)) {
            ogcFilter = sf0BoreholeService.getFilter(boreholeName, "", dateOfDrillingStart, dateOfDrillingEnd, -1, bbox, null, true,null);
        }
        try {
            boolean result = this.dataService2_0.submitProcessingTsgJob(email, jobName, wfsUrls, ogcFilter, tsgAlgName, tsgAlgorithm, startDepth, endDepth, operator, value, units, span);
            return generateJSONResponseMAV(result);
        } catch (Exception ex) {
            log.error("Unable to submit processing job: " + ex.getMessage());
            log.debug("Exception: ", ex);
            return generateJSONResponseMAV(false);
        }
    }

    /**
     * Returns an array of JSON AnalyticalJobStatus objects describing job status responses for a given email
     * @param email
     * @return
     */
    @RequestMapping("/checkNVCLProcessingJob.do")
    public ModelAndView checkNVCLProcessingJob(@RequestParam("email") String email) {
        try {
            List<AnalyticalJobStatus> statuses = this.dataService2_0.checkProcessingJobs(email);
            return generateJSONResponseMAV(true, statuses, "");
        } catch (Exception ex) {
            log.error("Unable to check NVCL processing jobs: " + ex.getMessage());
            log.debug("Exception: ", ex);
            return generateJSONResponseMAV(false);
        }
    }

    /**
     * Returns an object containing passing, failing and erroring borehole ID's for a given processing job
     * @param jobid
     *            requested job id
     * @return
     */
    @RequestMapping("/getNVCLProcessingResults.do")
    public ModelAndView getNVCLProcessingResults(@RequestParam("jobId") String jobId) {
        try {
            AnalyticalJobResults results = this.dataService2_0.getProcessingResults(jobId);
            return generateJSONResponseMAV(true, Arrays.asList(results), "");
        } catch (Exception ex) {
            log.error("Unable to check NVCL processing jobs: " + ex.getMessage());
            log.debug("Exception: ", ex);
            return generateJSONResponseMAV(false);
        }
    }


    /**
     * Downloads results of NVCL processing job
     * @param jobId
     *            job id NVCL processing job
     * @param returns results as a byte stream encoded in zip format, containing csv files
     * @throws Exception
     */     
    @RequestMapping("/downloadNVCLProcessingResults.do")
    public void downloadNVCLProcessingResults(@RequestParam("jobId") String jobId, HttpServletResponse response) throws Exception {
        AnalyticalJobResults results = this.dataService2_0.getProcessingResults(jobId);

        response.setContentType("application/zip");
        response.setHeader("Content-Disposition","inline; filename=nvclanalytics-" + jobId + ".zip;");
        ZipOutputStream zout = new ZipOutputStream(response.getOutputStream());

        try{
            zout.putNextEntry(new ZipEntry("passIds.csv"));
            for (String id : results.getPassBoreholes()) {
                zout.write((id + '\n').getBytes());
            }
            zout.closeEntry();

            zout.putNextEntry(new ZipEntry("failIds.csv"));
            for (String id : results.getFailBoreholes()) {
                zout.write((id + '\n').getBytes());
            }
            zout.closeEntry();

            zout.putNextEntry(new ZipEntry("errorIds.csv"));
            for (String id : results.getErrorBoreholes()) {
                zout.write((id + '\n').getBytes());
            }
            zout.closeEntry();

            zout.finish();
            zout.flush();
        } finally {
            zout.close();
        }
    }


    /**
     * Requests the image tray depth from NVCL services
     *
     * @param serviceUrl
     *            URL of NVCL service
     * @param logid
     *            requested log id
     * @return JSON struct of image tray depths
     * @throws Exception
     */
    @RequestMapping("/getNVCLImageTrayDepth.do")
    public ModelAndView getNVCLImageTrayDepth(@RequestParam("serviceUrl") String serviceUrl, @RequestParam("logid") String logId) throws Exception {
        try {
            List<ImageTrayDepthResponse> results = this.dataService2_0.getImageTrayDepths(serviceUrl,logId);
            return generateJSONResponseMAV(true, results, "");
        } catch (Exception ex) {
            log.error("Unable to get image tray depths: " + ex.getMessage());
            log.debug("Exception: ", ex);
            return generateJSONResponseMAV(false);
        }
    }    
}
