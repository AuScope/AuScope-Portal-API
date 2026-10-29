package org.auscope.portal.server.config;

import javax.sql.DataSource;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.jdbc.autoconfigure.DataSourceProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;

@Configuration
public class GraceDataSourceConfiguration {

    @Bean
    @ConfigurationProperties("grace.datasource")
    public DataSourceProperties graceDataSourceProperties() {
        return new DataSourceProperties();
    }

    @Bean(name = "graceDataSource")
    public DataSource graceDataSource() {
        return graceDataSourceProperties().initializeDataSourceBuilder().build();
    }

    @Bean(name = "graceJdbcTemplate")
    public NamedParameterJdbcTemplate graceJdbcTemplate(@Qualifier("graceDataSource") DataSource dataSource) {

        return new NamedParameterJdbcTemplate(dataSource);
    }
}