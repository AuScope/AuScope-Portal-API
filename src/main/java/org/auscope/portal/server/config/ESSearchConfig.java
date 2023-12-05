package org.auscope.portal.server.config;

import java.net.InetSocketAddress;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.elasticsearch.client.RestClient;
import org.elasticsearch.client.RestClientBuilder;
//import org.elasticsearch.client.RestHighLevelClient;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.elasticsearch.client.ClientConfiguration;
import org.springframework.data.elasticsearch.client.elc.ElasticsearchConfiguration;
//import org.springframework.data.elasticsearch.config.AbstractElasticsearchConfiguration;
import org.springframework.data.elasticsearch.repository.config.EnableElasticsearchRepositories;
import org.springframework.data.elasticsearch.support.HttpHeaders;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@Configuration
@EnableElasticsearchRepositories(basePackages = "org.auscope.portal.core.repositories")
//@ComponentScan(basePackages = { "org.auscope.portal.core" })
//public class SearchConfig extends ElasticsearchConfiguration {
//public class SearchConfig extends AbstractElasticsearchConfiguration {
//@EnableJpaRepositories(basePackages = "org.auscope.portal.core.repositories")
public class ESSearchConfig extends ElasticsearchConfiguration {
	
	//private final Log log = LogFactory.getLog(getClass());
	
	@Override
	public ClientConfiguration clientConfiguration() {
		return ClientConfiguration.builder()
			//.connectedTo(new InetSocketAddress("elastic.dev.easi-eo.solutions", 443))
			//.connectedTo("elastic.dev.easi-eo.solutions:443")
				
			.connectedTo("auportalsearch-dev.es.australiaeast.azure.elastic-cloud.com:443")
			
			.usingSsl()
			
			.withBasicAuth("elastic", "nVot4UU9nprvY7daTc8Df5WC")
			
			//.withConnectTimeout(15000)
			.withSocketTimeout(15000)
			
			//.withDefaultHeaders(compatibilityHeaders())
			.build();
	}
	
	/*
	@Override
	public ClientConfiguration clientConfiguration() {
		return ClientConfiguration.builder()     
				//.connectedTo("localhost:9200")
				.connectedTo(new InetSocketAddress("elastic.dev.easi-eo.solutions", 443))
				.withDefaultHeaders(compatibilityHeaders())
				.build();
	}
	
	private Supplier<org.springframework.http.HttpHeaders> compatibilityHeaders() {
		org.springframework.http.HttpHeaders headers = new org.springframework.http.HttpHeaders();
		headers.add("Accept", "application/vnd.elasticsearch+json;compatible-with=7");
		headers.add("Content-Type", "application/vnd.elasticsearch+json;compatible-with=7");
		return headers;
	}
	*/
	
	/* XXX LAST CODE
	@Bean
    @Override
    public RestHighLevelClient elasticsearchClient() {
		// XXX headers only needed if connecting to version 8
		//RestClientBuilder builder = RestClient.builder(new HttpHost("elastic.dev.exploration.tools", 80, "http")).setDefaultHeaders(compatibilityHeaders());
		RestClientBuilder builder = RestClient.builder(new HttpHost("elastic.dev.easi-eo.solutions", 443, "https"));//.setDefaultHeaders(compatibilityHeaders());
        RestHighLevelClient client = new RestHighLevelClient(builder);
        return client;
    }
    */
	
	/**
	 * spring-boot-data-elasticsearch using ES7 so make sure any ES8.X servers respond as such
	 * @return headers to ensure ES7 responses
	 */
	/*
	private Header[] compatibilityHeaders() {
	    return new Header[] {
    		new BasicHeader(HttpHeaders.ACCEPT, "application/vnd.elasticsearch+json;compatible-with=7"),
    		new BasicHeader(HttpHeaders.CONTENT_TYPE, "application/vnd.elasticsearch+json;compatible-with=7")
	    };
	}
	*/

	/*
	private HttpHeaders compatibilityHeaders() {
		HttpHeaders headers = new HttpHeaders();
		
		//headers.add("Accept", "application/json");
		//headers.add("Content-Type", "application/json;charset=UTF-8");
		
		return headers;
	}
	*/
	
	
	/*
	@Bean
    @Override
    public ElasticsearchCustomConversions elasticsearchCustomConversions() {
        return new ElasticsearchCustomConversions(Arrays.asList(new URLToElasticStringFormat(), new ElasticStringFormatToURL()));
    }

    @WritingConverter
    static class URLToElasticStringFormat implements Converter<URL, String> {

        @Override
        public String convert(URL source) {
        	
        	System.out.println("");
        	System.out.println("*********************** URLToElasticStringFormat ***********************");
        	System.out.println("url: " + source.toString());
        	System.out.println("");
        	
            return source.toString();
        }
    }
    
    @ReadingConverter
    static class ElasticStringFormatToURL implements Converter<String, URL> {
    	
    	@Override
    	public URL convert(String source) {
    		
    		System.out.println("");
        	System.out.println("*********************** ElasticStringFormatToURL ***********************");
        	System.out.println("url: " + source);
        	System.out.println("");
        	
    		return new URL(source);
    	}
    }
    */
	
}
