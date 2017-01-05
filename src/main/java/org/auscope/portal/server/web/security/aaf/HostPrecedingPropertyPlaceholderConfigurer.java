package org.auscope.portal.server.web.security.aaf;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Properties;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.config.PropertyPlaceholderConfigurer;


public class HostPrecedingPropertyPlaceholderConfigurer extends PropertyPlaceholderConfigurer {

    protected final Log logger = LogFactory.getLog(getClass());
    
    public String resolvePlaceholder(String placeholder, Properties props) {

        try {
            if (placeholder.startsWith("HOST.")) {
                logger.debug("Host: "
                        + InetAddress.getLocalHost().getHostName()
                        + " for property " + placeholder);
                String replace = placeholder.replaceFirst("HOST",
                        InetAddress.getLocalHost().getHostName());

                String prop = props.getProperty(replace);
                if (prop == null) {
                    logger.warn("Please define property: " + replace);
                }
                return prop;

            } else {
                logger.debug("reg");
                return props.getProperty(placeholder);
            }
        } catch (UnknownHostException e) {
            logger.warn(e);
            return null;
        }
    }
}