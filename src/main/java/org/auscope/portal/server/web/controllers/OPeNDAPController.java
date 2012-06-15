package org.auscope.portal.server.web.controllers;

import java.io.InputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import javax.servlet.http.HttpServletResponse;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.auscope.portal.core.server.controllers.BasePortalController;
import org.auscope.portal.core.server.http.HttpServiceCaller;
import org.auscope.portal.core.services.OpendapService;
import org.auscope.portal.core.services.methodmakers.OPeNDAPGetDataMethodMaker.OPeNDAPFormat;
import org.auscope.portal.core.services.responses.opendap.AbstractViewVariable;
import org.auscope.portal.core.services.responses.opendap.ViewVariableFactory;
import org.auscope.portal.core.view.JSONModelAndView;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;


/**
 * A controller for marshaling requests for an arbitrary OPeNDAP resource.
 * @author vot002
 *
 */
@Controller
public class OPeNDAPController extends BasePortalController {

    /** The log. */
    private final Log log = LogFactory.getLog(getClass());

    /** The opendap service. */
    private OpendapService opendapService;

    /**
     * Instantiates a new opendap controller.
     *
     * @param serviceCaller the service caller
     * @param getDataMethodMaker the get data method maker
     */
    @Autowired
    public OPeNDAPController(OpendapService opendapService) {
        super();
        this.opendapService = opendapService;
    }

    /**
     * Downloads the list of supported download formats (one of these values
     * should be passed to the opendapMakeRequest.do handler).
     *
     * @return the supported formats
     */
    @RequestMapping("/opendapGetSupportedFormats.do")
    public ModelAndView getSupportedFormats() {
        JSONArray items = new JSONArray();

        items.add(new String[] {"ascii"});
        items.add(new String[] {"dods"});

        return new JSONModelAndView(items);
    }

    /**
     * Downloads the list of queryable variables from the given OPeNDAP Service.
     *
     * JSON ResponseFormat = [ViewVariable]
     *
     * @param opendapUrl The remote service URL to query
     * @param variableName the variable name
     * @return the variables
     * @throws Exception the exception
     */
    @RequestMapping("/opendapGetVariables.do")
    public ModelAndView getVariables(@RequestParam("opendapUrl") final String opendapUrl,
                                     @RequestParam(required=false, value="variableName") final String variableName) throws Exception {

        //Attempt to parse our response
        try {
            AbstractViewVariable[] vars = opendapService.getVariables(opendapUrl, variableName);
            return generateJSONResponseMAV(true, vars, "");
        } catch (Exception ex) {
            log.error(String.format("Error parsing from '%1$s'", opendapUrl), ex);
            return generateJSONResponseMAV(false, null, String.format("An error has occured whilst reading data from '%1$s'", opendapUrl));
        }
    }

    /**
     * Makes a request to an OPeNDAP service for data within given constraints.
     *
     * @param opendapUrl The remote service URL to query
     * @param downloadFormat How the response data should be formatted
     * @param constraintsJson [Optional] Must be an object with an element 'constraints' set to a list variable/griddedVariable (See getVariables for more info on JSON schema)
     * @param response the response
     * @throws Exception the exception
     */
    @RequestMapping("/opendapMakeRequest")
    public void makeRequest(@RequestParam("opendapUrl") final String opendapUrl,
            @RequestParam("downloadFormat") final String downloadFormat,
            @RequestParam(required=false, value="constraints") final String constraintsJson,
            HttpServletResponse response) throws Exception {

        log.trace(String.format("opendapUrl='%1$s'", opendapUrl));
        log.trace(String.format("downloadFormat='%1$s'", downloadFormat));
        log.trace(String.format("constraintsJson='%1$s'", constraintsJson));

        OPeNDAPFormat format;
        String outputFileName;
        if (downloadFormat.equals("ascii")) {
            format = OPeNDAPFormat.ASCII;
            outputFileName = "data.txt";
        } else if (downloadFormat.equals("dods")) {
            format = OPeNDAPFormat.DODS;
            outputFileName = "data.bin";
        } else {
            throw new IllegalArgumentException("Unsupported format " + downloadFormat);
        }

        //Parse our constraint list (can be null)
        AbstractViewVariable[] constraints = new AbstractViewVariable[0];
        if (constraintsJson != null && !constraintsJson.isEmpty()) {
            JSONObject obj = JSONObject.fromObject(constraintsJson);
            constraints = ViewVariableFactory.fromJSONArray(obj.getJSONArray("constraints"));
        }



        //Make our request, push the contents to the outputstream (as a zipfile)
        response.setContentType("application/zip");
        response.setHeader("Content-Disposition", "inline; filename=OPeNDAPDownload.zip;");
        ZipOutputStream zout = new ZipOutputStream(response.getOutputStream());
        InputStream dataStream = null;
        try {
            zout.putNextEntry(new ZipEntry(outputFileName));

            dataStream = opendapService.getData(opendapUrl, format, constraints);

            writeInputToOutputStream(dataStream, zout, 1024 * 1024, false);
        } catch (Exception ex) {
            log.info(String.format("Error requesting data from '%1$s'", opendapUrl));
            log.debug("Exception...", ex);
            writeErrorToZip(zout, String.format("Error connecting to '%1$s'", opendapUrl), ex, "error.txt");
        } finally {
            if (dataStream != null) {
                dataStream.close();
            }
            if (zout != null) {
                zout.close();
            }
        }
    }
}
