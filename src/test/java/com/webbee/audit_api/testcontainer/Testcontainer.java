package com.webbee.audit_api.testcontainer;

import com.webbee.audit_api.converter.LongToLocalDateTimeConverter;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.elasticsearch.core.convert.ElasticsearchCustomConversions;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.elasticsearch.ElasticsearchContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

@Testcontainers
@SpringBootTest
@ActiveProfiles("test")
@Configuration
public abstract class Testcontainer {

    @Container
    private static final ElasticsearchContainer elasticsearchContainer =
            new ElasticsearchContainer("elasticsearch:9.1.2")
                    .withExposedPorts(9200)
                    .withEnv("xpack.security.enabled", "false");

    @DynamicPropertySource
    static void setProp(DynamicPropertyRegistry registry) {
        registry.add("spring.elasticsearch.uris", elasticsearchContainer::getHttpHostAddress);
    }

    @Configuration
    public static class TestElasticsearchConfig {
        @Bean
        public ElasticsearchCustomConversions elasticsearchCustomConversions() {
            return new ElasticsearchCustomConversions(
                    java.util.List.of(new LongToLocalDateTimeConverter())
            );
        }
    }
}
