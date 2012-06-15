package org.auscope.portal.server.web.controllers;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.jar.Attributes;
import java.util.jar.Manifest;

import javax.servlet.http.HttpServletRequest;

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
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;

/**
 * A controller class for marshalling a users interactions with Geonetwork
 * @author Josh Vote
 *
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

        //Add our source WCS to the online resources tab
        String dataUrl = job.getVmSubsetUrl();
        if (dataUrl != null && !dataUrl.isEmpty()) {
            onlineResources.add(new CSWOnlineResourceImpl(new URL(dataUrl), "WWW:LINK-1.0-http--link", "ERDAP Subset Data", "ERDAP Subset Data"));
        }

        //Generate our contact details
        CSWContact contact = new CSWContact();
        contact.setAddressAdministrativeArea("ACT");
        contact.setAddressCity("Canberra");
        contact.setAddressDeliveryPoint("GPO Box 378");
        contact.setAddressPostalCode("2601");
        contact.setAddressCountry("Australia");
        contact.setTelephone("+61 2 62499111");
        contact.setFacsimile("+61 2 62499999");
        contact.setOnlineResource(new CSWOnlineResourceImpl(new URL("http://www.ga.gov.au/"), "WWW:LINK-1.0-http--link", "Geoscience Australia", "Geoscience Australia Homepage"));

        CSWResponsibleParty rp = new CSWResponsibleParty();
        rp.setContactInfo(contact);
        rp.setIndividualName("Web Operations Manager");
        rp.setOrganisationName("Geoscience Australia");
        rp.setPositionName("Web Operations Manager");

        //Build our bounding box
        CSWGeographicElement[] geoEls = new CSWGeographicElement[] {new CSWGeographicBoundingBox(job.getSelectionMinEasting(), job.getSelectionMaxEasting(), job.getSelectionMinNorthing(), job.getSelectionMaxNorthing())};

        //Build our CSW Record
        CSWRecord rec = new CSWRecord(null);
        rec.setContact(rp);
        rec.setCSWGeographicElements(geoEls);
        rec.setConstraints(new String[] {
                "Copyright Commonwealth of Australia. This work is copyright. Unless otherwise specified on this website, you may display, print and reproduce this material in unaltered form only (retaining this notice) for your personal, non-commercial use, use within your organisation or, if you are an educational institution, use for educational purposes. Apart from any use as permitted under the Copyright Act 1968 or as otherwise specified on this website, all other rights are reserved. Requests and enquiries concerning copyright in the work should be addressed to the Information Services Branch, Geoscience Australia, GPO Box 378, CANBERRA, ACT, 2601 or email: copyright@ga.gov.au. See also http://www.ga.gov.au/about/copyright.jsp, and http://www.osdm.gov.au/OSDM/Policies+and+Guidelines/Spatial+Data+Access+and+Pricing/OSDM+Licence+Internet+-+no+registration/default.aspx"
        });
        rec.setDataIdentificationAbstract(job.getDescription());
        rec.setDate(job.getSubmitDate());
        rec.setDescriptiveKeywords(new String[] {
           "VEGL",
           "GA",
           "NCI"
        });
        rec.setOnlineResources(onlineResources.toArray(new CSWOnlineResourceImpl[onlineResources.size()]));
        rec.setResourceProvider("Geoscience Australia");
        rec.setServiceName(job.getName());
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

    /**
     * Requests that the portal should insert details of a job into GeoNetwork
     * @param jobId
     * @param request
     * @return A generic VEGL JSON response with the data element populated with the geonetwork URL string (on success)
     * @throws Exception
     */
    @RequestMapping("/insertRecord.do")
    public ModelAndView insertRecord(@RequestParam("jobId") final Integer jobId, HttpServletRequest request) throws Exception {

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

        try {
            //Create an instance of our CSWRecord and transform it to a <gmd:MD_Metadata> record
            CSWRecord record = jobToCSWRecord(request, job, jobSeries);

            //Lets connect to geonetwork and then send our new record
            String metadataRecordUrl = gnService.makeCSWRecordInsertion(record);
            job.setRegisteredUrl(metadataRecordUrl);
            jobManager.saveJob(job);

            return generateJSONResponseMAV(true, metadataRecordUrl, "");
        } catch (Exception ex) {
            logger.warn("Error sending job to Geonetwork for jobId=" + jobId, ex);
            return generateJSONResponseMAV(false, null, "Internal error");
        }
    }



}
