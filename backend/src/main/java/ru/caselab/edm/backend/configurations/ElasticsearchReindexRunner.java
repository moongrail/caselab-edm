package ru.caselab.edm.backend.configurations;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;
import ru.caselab.edm.backend.entity.Attribute;
import ru.caselab.edm.backend.entity.AttributeSearch;
import ru.caselab.edm.backend.repository.AttributeRepository;
import ru.caselab.edm.backend.repository.DocumentRepository;
import ru.caselab.edm.backend.repository.elastic.AttributeSearchRepository;

import java.util.List;

/**
 * Полная переиндексация атрибутов выполняется только при явно включённом флаге.
 * Это убирает сетевой вызов Elasticsearch из жизненного цикла бинов и не мешает
 * запуску приложения, если поиск временно недоступен.
 */
@Component
@RequiredArgsConstructor
@Slf4j
@ConditionalOnProperty(name = "app.elasticsearch.reindex-on-startup", havingValue = "true")
public class ElasticsearchReindexRunner implements ApplicationRunner {

    private final AttributeRepository attributeRepository;
    private final DocumentRepository documentRepository;
    private final AttributeSearchRepository attributeSearchRepository;

    @Override
    public void run(ApplicationArguments args) {
        try {
            List<Attribute> attributes = attributeRepository.findAll();
            for (Attribute attribute : attributes) {
                List<Long> documents = documentRepository.getDocumentsWithAttribute(attribute.getId());
                AttributeSearch search = new AttributeSearch(
                        attribute.getId(),
                        attribute.getName(),
                        attribute.getDataType(),
                        attribute.isRequired(),
                        documents);
                attributeSearchRepository.save(search);
            }
            log.info("Elasticsearch reindex completed for {} attributes", attributes.size());
        } catch (Exception ex) {
            log.warn("Elasticsearch reindex skipped: {}", ex.getMessage());
        }
    }
}
