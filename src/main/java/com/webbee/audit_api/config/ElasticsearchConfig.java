package com.webbee.audit_api.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.webbee.audit_api.converter.LongToLocalDateTimeConverter;
import org.apache.http.conn.ssl.TrustAllStrategy;
import org.apache.http.ssl.SSLContextBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.data.elasticsearch.client.ClientConfiguration;
import org.springframework.data.elasticsearch.client.elc.ElasticsearchConfiguration;
import org.springframework.data.elasticsearch.core.convert.ElasticsearchCustomConversions;

import javax.net.ssl.SSLContext;

@Configuration
@Profile("!test")
public class ElasticsearchConfig extends ElasticsearchConfiguration {

    @Value("${elasticsearch.username}")
    private String username;

    @Value("${elasticsearch.password}")
    private String password;

    @Autowired
    private ObjectMapper objectMapper;

    private static SSLContext buildSSSLContext() {
        try {
            return new SSLContextBuilder().loadTrustMaterial(null, TrustAllStrategy.INSTANCE).build();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public ClientConfiguration clientConfiguration() {
        return ClientConfiguration.builder()
                .connectedToLocalhost()
                .usingSsl(buildSSSLContext())
                .withBasicAuth(username, password)
                .build();
    }

    @Bean
    public ElasticsearchCustomConversions elasticsearchCustomConversions() {
        return new ElasticsearchCustomConversions(
                java.util.List.of(new LongToLocalDateTimeConverter())
        );
    }

}
