package org.auscope.portal.server.web.controllers;

import java.io.File;
import java.io.IOException;

import org.apache.commons.io.FileUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.util.ResourceUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

/**
 * Class for serving preview images. Layer previews are defined in layers.yaml using the previewImg property.
 */
@Controller
@RequestMapping(value = "/preview")
public class PreviewImageController {
	
	private final String IMAGE_PATH = "classpath:images/preview/";
	private final Log logger = LogFactory.getLog(getClass());

	/**
	 * Retrieve a preview from the image folder defined in RESOURCE_ROOT
	 *
	 * @param filename the name of the preview file
	 * @return preview image
	 */
    @GetMapping("/{image}")
    public ResponseEntity<byte[]> getImage(@PathVariable("image") String filename) {
        byte[] image = new byte[0];
        try {
        	File f = ResourceUtils.getFile(IMAGE_PATH + filename);
        	image = FileUtils.readFileToByteArray(f);
        } catch (IOException e) {
            logger.warn("Unable to find preview: " + filename);
        }
        return ResponseEntity.ok().contentType(MediaType.IMAGE_PNG).body(image);
    }

}
