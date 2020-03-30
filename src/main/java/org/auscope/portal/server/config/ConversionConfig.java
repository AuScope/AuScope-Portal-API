package org.auscope.portal.server.config;

import java.util.HashSet;
import java.util.Set;

import org.auscope.portal.server.web.StringToArrayConversionBlocker;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.context.support.ConversionServiceFactoryBean;
import org.springframework.core.convert.ConversionService;
import org.springframework.core.convert.converter.Converter;


/**
 * Override Spring converters.
 * 
 * String to array conversion blocker required so faceted search doesn't read
 * coordinates string as an array.
 * 
 * @author woo392
 *
 */
@Configuration
public class ConversionConfig {
	
	@Autowired
	private StringToArrayConversionBlocker cBlocker;
	
	@Bean(name="conversionService")
	@Primary
	public ConversionService getConversionService() {
		ConversionServiceFactoryBean bean = new ConversionServiceFactoryBean();
        bean.setConverters(getConverters());
        bean.afterPropertiesSet();
        ConversionService object = bean.getObject();
        return object;
	}
	
	// Add any more custom converters here if/when necessary
	private Set<Converter<?,?>> getConverters() {
        Set<Converter<?,?>> converters = new HashSet<Converter<?,?>>();
        converters.add(cBlocker);
        return converters;
    }

}
