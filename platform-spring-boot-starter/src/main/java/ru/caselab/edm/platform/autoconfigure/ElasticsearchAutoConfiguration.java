package ru.caselab.edm.platform.autoconfigure;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.data.elasticsearch.core.ElasticsearchOperations;
import ru.caselab.edm.platform.search.SearchProperties;
import ru.caselab.edm.platform.search.SearchReindexer;

import java.util.List;

/**
 * Elasticsearch integration hook. Spring Boot owns client/repository
 * auto-configuration; this starter adds a common startup reindex contract.
 */
@Slf4j
@AutoConfiguration
@ConditionalOnClass(ElasticsearchOperations.class)
@EnableConfigurationProperties(SearchProperties.class)
public class ElasticsearchAutoConfiguration {

    @Bean
    @ConditionalOnProperty(prefix = "platform.search", name = "reindex-on-startup",
            havingValue = "true")
    public ApplicationRunner searchReindexRunner(List<SearchReindexer> reindexers) {
        return args -> {
            log.info("Rebuilding {} search indexes", reindexers.size());
            reindexers.forEach(SearchReindexer::reindex);
        };
    }
}
