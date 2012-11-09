package org.auscope.portal.server.web.controllers;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.jar.Attributes;
import java.util.jar.Manifest;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.auscope.portal.core.cloud.CloudFileInformation;
import org.auscope.portal.core.server.controllers.BasePortalController;
import org.auscope.portal.core.services.GeonetworkService;
import org.auscope.portal.core.services.cloud.CloudStorageService;
import org.auscope.portal.core.services.responses.csw.CSWContact;
import org.auscope.portal.core.services.responses.csw.CSWGeographicBoundingBox;
import org.auscope.portal.core.services.responses.csw.CSWGeographicElement;
import org.auscope.portal.core.services.responses.csw.CSWOnlineResourceImpl;
import org.auscope.portal.core.services.responses.csw.CSWRecord;
import org.auscope.portal.core.services.responses.csw.CSWResponsibleParty;
import org.auscope.portal.server.vegl.VEGLJob;
import org.auscope.portal.server.vegl.VEGLJobManager;
import org.auscope.portal.server.vegl.VEGLSeries;
import org.auscope.portal.server.vegl.VGLSignature;
import org.auscope.portal.server.vegl.VglDownload;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;

/**
 * A controller class for marshalling a users interactions with Geonetwork
 * @author Josh Vote
 * @author Richard Goh
 */
@Controller
public class GeonetworkController extends BasePortalController {
    protected final Log logger = LogFactory.getLog(getClass());

    private VEGLJobManager jobManager;
    private GeonetworkService gnService;
    private CloudStorageService cloudStorageService;

    @Autowired
    public GeonetworkController(VEGLJobManager jobManager, GeonetworkService gnService, CloudStorageService cloudStorageService) {
        this.jobManager = jobManager;
        this.gnService = gnService;
        this.cloudStorageService = cloudStorageService;
    }

    /**
     * Converts a job into a CSWRecord that can be stored in a registry.
     *
     * The record is mostly prefilled with Geoscience Australia contact information
     * @param job
     * @return
     * @throws MalformedURLException
     * @throws CloudStorageException
     */
    private CSWRecord jobToCSWRecord(HttpServletRequest request, VEGLJob job, VEGLSeries series) throws Exception {
        CloudFileInformation[] outputFiles = cloudStorageService.listJobFiles(job);
        List<CSWOnlineResourceImpl> onlineResources = new ArrayList<CSWOnlineResourceImpl>();

        //Add any output files to our online resources tab
        if (outputFiles != null) {
            for (CloudFileInformation obj : outputFiles) {
                onlineResources.add(new CSWOnlineResourceImpl(new URL(obj.getPublicUrl()), "WWW:DOWNLOAD-1.0-ftp--download", obj.getName(), obj.getName()));
            }
        }

        //Add our remote service downloads to the online resources tab
        List<VglDownload> jobDownloads = job.getJobDownloads();
        for (VglDownload dl : jobDownloads) {
            URL url = null;
            try {
                url = new URL(dl.getUrl());
            } catch (Exception ex) {
                log.warn(String.format("Unable to parse URL '%1$s' for job '%2$s'. It will be skipped", dl.getUrl(), job.getId()));
                continue;
            }

            onlineResources.add(new CSWOnlineResourceImpl(url, "WWW:LINK-1.0-http--link", dl.getName(), dl.getDescription()));
        }

        //Generate our contact details
        CSWContact contact = new CSWContact();
        contact.setAddressAdministrativeArea(request.getParameter("administrativeArea"));
        contact.setAddressCity(request.getParameter("city"));
        contact.setAddressDeliveryPoint(request.getParameter("deliveryPoint"));
        contact.setAddressPostalCode(request.getParameter("postalCode"));
        contact.setAddressCountry(request.getParameter("country"));
        contact.setTelephone(request.getParameter("telephone"));
        contact.setFacsimile(request.getParameter("facsimile"));

        String onlineContactURL = request.getParameter("onlineContactURL");
        String onlineContactName = request.getParameter("onlineContactName");
        String onlineContactDescription = request.getParameter("onlineContactDescription");

        if (StringUtils.isNotEmpty(onlineContactURL) && StringUtils.isNotEmpty(onlineContactDescription)) {
            contact.setOnlineResource(new CSWOnlineResourceImpl(new URL(onlineContactURL), "WWW:LINK-1.0-http--link", onlineContactName, onlineContactDescription));
        }

        CSWResponsibleParty rp = new CSWResponsibleParty();
        rp.setContactInfo(contact);
        rp.setIndividualName(request.getParameter("individualName"));
        rp.setOrganisationName(request.getParameter("organisationName"));
        rp.setPositionName(request.getParameter("positionName"));

        //Build our bounding boxes
        List<CSWGeographicElement> geoEls = new ArrayList<CSWGeographicElement>();;
        for (VglDownload dl : jobDownloads) {
            if ( dl.getEastBoundLongitude()!= null &&
                    dl.getWestBoundLongitude() != null &&
                    dl.getNorthBoundLatitude() != null &&
                    dl.getSouthBoundLatitude() != null) {
                geoEls.add(new CSWGeographicBoundingBox(dl.getWestBoundLongitude(),  dl.getEastBoundLongitude(), dl.getSouthBoundLatitude(), dl.getNorthBoundLatitude()));
            }
        }

        //Build our CSW Record
        CSWRecord rec = new CSWRecord(null);
        rec.setContact(rp);
        rec.setCSWGeographicElements(geoEls.toArray(new CSWGeographicElement[geoEls.size()]));
        String constraints = request.getParameter("constraints");
        if (StringUtils.isNotEmpty(constraints)) {
            rec.setConstraints(new String[] { constraints });
        }
        rec.setDataIdentificationAbstract(job.getDescription());
        rec.setDate(job.getSubmitDate());

        String descriptiveKeywords = request.getParameter("keywords");
        if (StringUtils.isNotEmpty(descriptiveKeywords)) {
            rec.setDescriptiveKeywords(descriptiveKeywords.split("[\\s,]+"));
        }

        rec.setOnlineResources(onlineResources.toArray(new CSWOnlineResourceImpl[onlineResources.size()]));
        rec.setResourceProvider(request.getParameter("organisationName"));
        rec.setServiceName(job.getName());

        // set by VGL
        rec.setSupplementalInformation(String.format("User: %1$s\nSeries: %2$s\nDescription: %3$s",job.getUser(), series.getName(), series.getDescription()));

        String appServerHome = request.getSession().getServletContext().getRealPath("/");
        File manifestFile = new File(appServerHome,"META-INF/MANIFEST.MF");
        Manifest mf = new Manifest();
        try {
            mf.read(new FileInputStream(manifestFile));
            Attributes atts = mf.getMainAttributes();
            rec.setDataQualityStatement(String.format(
                    "Workflow by %1$s Version %2$s.%3$s. \nProcessed by GRAV3D MPI - Version 4.0 20100108.",
                    atts.getValue("Specification-Title"),
                    atts.getValue("Implementation-Version"),
                    atts.getValue("Implementation-Build")));
        } catch (IOException e) {
            logger.error("Error reading manifest file.", e);
        }

        return rec;
    }

    @RequestMapping("/getUserSignature.do")
    public ModelAndView getUserSignature(HttpServletRequest request) {
        //Get user email from session
        String user = (String) request.getSession().getAttribute("openID-Email");
        if (user == null) {
            return generateJSONResponseMAV(false, null,
                    "Your session has timed out.",
                    "Please refresh this page and login again to complete the job registration.");
        }

        //Retrieve user signature from database. If the operation fails or user
        //signature doesn't exist, return a blank one.
        VGLSignature userSignature = null;

        try {
            userSignature = jobManager.getSignatureByUser(user);
        } catch (Exception ex) {
            logger.warn("Failed to retrieve signature for " + user, ex);
        }

        if (userSignature == null) {
            logger.debug("Return an empty signature as the user signature couldn't be found or retrieved.");
            userSignature = new VGLSignature();
        }

        return generateJSONResponseMAV(true, Arrays.asList(userSignature), "");
    }

    /**
     * Requests that the portal should insert details of a job into GeoNetwork
     * @param jobId
     * @param request
     * @return A generic VEGL JSON response with the data element populated with the geonetwork URL string (on success)
     * @throws Exception
     */
    @RequestMapping("/insertRecord.do")
    public ModelAndView insertRecord(@RequestParam("jobId") final Integer jobId, HttpServletRequest request) throws Exception {
        logger.debug("in insertRecord...");

        //Lookup our appropriate job
        VEGLJob job = jobManager.getJobById(jobId);
        if (job == null) {
            return generateJSONResponseMAV(false, null, "The specified job does not exist.");
        }

        //Lookup our series
        VEGLSeries jobSeries = jobManager.getSeriesById(job.getSeriesId());
        if (jobSeries == null) {
            return generateJSONResponseMAV(false, null, "The specified job does not belong to a series.");
        }

        //Get user email from session
        String user = (String) request.getSession().getAttribute("openID-Email");
        if (user == null) {
            logger.debug("Unable to get user email as user session has expired.");
            return generateJSONResponseMAV(false, null,
                    "Your session has timed out.",
                    "Please refresh this page and login again to complete the job registration.");
        }

        try {
            //Store or update user signature so that the details can be re-used
            //in subsequent registration process
            VGLSignature userSignature = jobManager.getSignatureByUser(user);
            if (userSignature == null) {
                logger.debug("Create a new signature as the user doesn't have one.");
                userSignature = new VGLSignature();
            }
            userSignature.setUser(user);
            userSignature.setIndividualName(request.getParameter("individualName"));
            userSignature.setOrganisationName(request.getParameter("organisationName"));
            userSignature.setPositionName(request.getParameter("positionName"));
            userSignature.setTelephone(request.getParameter("telephone"));
            userSignature.setFacsimile(request.getParameter("facsimile"));
            userSignature.setDeliveryPoint(request.getParameter("deliveryPoint"));
            userSignature.setCity(request.getParameter("city"));
            userSignature.setAdministrativeArea(request.getParameter("administrativeArea"));
            userSignature.setPostalCode(request.getParameter("postalCode"));
            userSignature.setCountry(request.getParameter("country"));
            userSignature.setOnlineContactName(request.getParameter("onlineContactName"));
            userSignature.setOnlineContactDescription(request.getParameter("onlineContactDescription"));
            userSignature.setOnlineContactURL(request.getParameter("onlineContactURL"));
            userSignature.setKeywords(request.getParameter("keywords"));
            userSignature.setConstraints(request.getParameter("constraints"));
            jobManager.saveSignature(userSignature);

            //Create an instance of our CSWRecord and transform it to a <gmd:MD_Metadata> record
            CSWRecord record = jobToCSWRecord(request, job, jobSeries);

            //Lets connect to geonetwork and then send our new record
            String metadataRecordUrl = gnService.makeCSWRecordInsertion(record);
            job.setRegisteredUrl(metadataRecordUrl);
            jobManager.saveJob(job);

            return generateJSONResponseMAV(true, metadataRecordUrl, "");
        } catch (Exception ex) {
            logger.warn("Error registering job to Geonetwork for jobId=" + jobId, ex);
            return generateJSONResponseMAV(false, null, "Internal error");
        }
    }
}