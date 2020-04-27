package org.auscope.portal.server.web.controllers;

import java.io.IOException;
import java.net.URI;
import java.nio.charset.StandardCharsets;

import javax.servlet.http.HttpServletResponse;

import org.apache.commons.io.IOUtils;
import org.auscope.portal.core.server.controllers.BasePortalController;
import org.auscope.portal.core.server.http.download.DownloadResponse;
import org.auscope.portal.core.server.http.download.FileDownloadService;
import org.auscope.portal.core.server.http.download.FileUploadBean;
import org.auscope.portal.core.util.MimeUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.commons.CommonsMultipartFile;
import org.springframework.web.servlet.ModelAndView;

import com.google.gson.Gson;

/*
 * Controller enables loading of KML layers onto map
 */

@Controller
public class CustomKMLController extends BasePortalController {
    FileDownloadService fileDownloadService;

    @Autowired
    public CustomKMLController(FileDownloadService fileDownloadService) {
        this.fileDownloadService = fileDownloadService;
    }
    
    /**
     * Parses the given KML file content
     *
     * @return KML data which can be loaded as a layer
     * @throws Exception
     */
    @RequestMapping("/addKMLLayer.do")
    public @ResponseBody String addKMLLayer(FileUploadBean uploadItem, BindingResult result,
            HttpServletResponse response) {
        try {
            CommonsMultipartFile file = uploadItem.getFile();
            ModelMap model = new ModelMap();
            model.put("success", true);
            model.put("file", IOUtils.toString(file.getInputStream(), StandardCharsets.UTF_8));
            model.put("name", file.getOriginalFilename());
            return new Gson().toJson(model);
        } catch (Exception e) {
            ModelMap model = new ModelMap();
            model.put("success", false);
            return new Gson().toJson(model);
        }
    }

    /**
     * Retrieves KML from the given URL
     *
     * @param url
     *       URL of KML file to be retrieved
     * @return KML data which can be loaded as a layer
     * @throws Exception
     */
    @RequestMapping("/addKMLUrl.do")
    public ModelAndView addKMLUrl(
            @RequestParam("url") String url,
            HttpServletResponse response) throws IOException {

        try {

            DownloadResponse dlRes = this.fileDownloadService.singleFileDownloadFromURL(url);

            URI uri = new URI(url);
            String fileExtension = MimeUtil.mimeToFileExtension(dlRes.getContentType());
            if (fileExtension != null && !fileExtension.isEmpty()) {
                fileExtension = "." + fileExtension;
            }

            ModelMap model = new ModelMap();
            model.put("success", true);
            model.put("file", IOUtils.toString(dlRes.getResponseAsStream(), StandardCharsets.UTF_8));
            model.put("name", uri.getHost() + fileExtension);

            return generateJSONResponseMAV(true, model, "success");

        } catch (Exception e) {
            log.warn(String.format("Error performing filter for '%1$s': %2$s", url, e));
            log.debug("Exception: ", e);
            return generateJSONResponseMAV(false, null, e.getMessage());

        }

    }
}
