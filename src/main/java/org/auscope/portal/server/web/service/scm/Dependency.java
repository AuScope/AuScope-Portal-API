package org.auscope.portal.server.web.service.scm;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonValue;

@JsonIgnoreProperties(ignoreUnknown = true)
public class Dependency {

    public static enum Type {
        PUPPET,
        REQUIREMENTS,
        PYTHON,
        TOOLBOX
    }

    public Type type;
    public String identifier;
    public String version;
    public String repository;
}
