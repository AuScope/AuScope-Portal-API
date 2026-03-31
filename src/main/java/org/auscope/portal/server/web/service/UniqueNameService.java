package org.auscope.portal.server.web.service;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.utils.URIBuilder;
import org.auscope.portal.core.server.http.HttpServiceCaller;
import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

/**
 * A utility class which provides methods for a unique name service based on the
 * generation of two random five letter words appended together used by:
 * shorturl controller portal state controller
 *
 * @author pet22a
 *
 */

@Service
public class UniqueNameService {

    @Value("${cloud.aws.portalS3Bucket}")
    private String portalS3Bucket;

    private List<String> words = new ArrayList<>();; // list of words from the S3 bucket, filtered by length
    private Boolean setup = false; // flag indicating if "words" have been loaded

    /*
     * creates a list of words of "minLength"
     */
    private void setupWordList(int minLength) {

        try {
            URL wordsUrl = new URI(portalS3Bucket + "/words.json").toURL();
            String wordsJson = IOUtils.toString(wordsUrl, Charset.forName("UTF-8"));
            JSONArray wordsObj = new JSONArray(wordsJson);

            Random rnd = new Random();

            words = wordsObj.toList().stream().map(Object::toString) // Ensure all elements are treated as strings
                    .filter(s -> s.length() == minLength) // Filter based on length
                    .collect(Collectors.toList());

            setup = true;
        } catch (IOException | URISyntaxException e) {
            System.out.println("Exception: UniqueNameService.setupWordList() - " + e.getMessage());
        }
    }

    /*
     * picks two randowm words from "words" and appends then to make a "unique" id
     */
    private String getWords() {

        String uniqueId = "";

        if (setup) {
            Random rnd = new Random();

            String w1 = (String) words.get(rnd.nextInt(words.size() + 1));
            String w2 = (String) words.get(rnd.nextInt(words.size() + 1));
            uniqueId = StringUtils.capitalize(w1) + StringUtils.capitalize(w2);
        }

        return uniqueId;
    }

    /**
     * getUniqueName to get a unique combination of two five letter words
     *
     * @return
     * @throws Exception
     */
    public String get() {

        if (!setup) {
            setupWordList(5);
        }

        return getWords();
    }

}
