package org.auscope.portal.server.web;

import org.springframework.core.convert.converter.Converter;

/**
 * Stops Spring Request Mapping conversion of String -> String[] for strings with an arbitrary
 * "seperator" character.
 * @author Josh Vote (CSIRO)
 *
 */
public class StringToArrayConversionBlocker implements Converter<String, String[]> {

    @Override
    public String[] convert(String source) {
        return new String[] {source};
    }

}
