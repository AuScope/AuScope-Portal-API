package org.auscope.portal.server.config;

import java.net.InetSocketAddress;
import java.net.URI;
import java.net.URISyntaxException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.elasticsearch.client.ClientConfiguration;
import org.springframework.data.elasticsearch.client.elc.ElasticsearchConfiguration;
import org.springframework.data.elasticsearch.repository.config.EnableElasticsearchRepositories;
import org.springframework.data.elasticsearch.support.HttpHeaders;

import com.nimbusds.oauth2.sdk.util.StringUtils;

import jakarta.annotation.PostConstruct;

@Configuration
@EnableElasticsearchRepositories(basePackages = "org.auscope.portal.core.repositories")
public class ElasticsearchConfig extends ElasticsearchConfiguration {
	
	private final Log log = LogFactory.getLog(getClass());
	
	@Value("${spring.data.elasticsearch.cluster-nodes}")
    private String clusterNodes;
	private String clusterNodesUrl;
	
	@Value("${spring.data.elasticsearch.apiKey:null}")
    private String apiKey;
	
	@Value("${spring.data.elasticsearch.port}")
    private Integer port;
	
	@PostConstruct
	private void createClusterNodesUrl() {
		try {
			URI clusterNodesUri = new URI(clusterNodes);
			clusterNodesUrl = clusterNodesUri.getHost() + clusterNodesUri.getPath();
		} catch(URISyntaxException e) {
			log.error("Unable to set cluster-nodes address from spring.data.elasticsearch.cluster-nodes property: " + e.getLocalizedMessage());
		}
	}
	
	@Override
	public ClientConfiguration clientConfiguration() {
		
		HttpHeaders headers = new HttpHeaders();
		if (StringUtils.isNotBlank(apiKey)) {
			headers.add("Authorization", "ApiKey " + apiKey);
		}
		headers.add("Content-Type", "application/json");
		
		return ClientConfiguration.builder()
			.connectedTo(new InetSocketAddress(clusterNodesUrl, port))
			.usingSsl()
			.withDefaultHeaders(headers)
			.withSocketTimeout(15000)
			.build();
	}
	
}
