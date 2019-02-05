package org.auscope.portal.server.velocity;

import java.util.Properties;

import org.apache.velocity.app.VelocityEngine;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * VelocityEngine Bean for templating (pre-Spring Boot holdover)
 *  
 * TOD: This should probably go in a utils class, will do that when we have
 * more like this from migration
 * 
 * @author woo392
 *
 */
@Configuration
public class Velocity {

	@Bean
	public VelocityEngine velocityEngine() throws Exception {
	    Properties properties = new Properties();
	    properties.setProperty("input.encoding", "UTF-8");
	    properties.setProperty("output.encoding", "UTF-8");
	    properties.setProperty("resource.loader", "class");
	    properties.setProperty("class.resource.loader.class",
	    					   "org.apache.velocity.runtime.resource.loader.ClasspathResourceLoader");
	    VelocityEngine velocityEngine = new VelocityEngine(properties);
	    return velocityEngine;
	}
}