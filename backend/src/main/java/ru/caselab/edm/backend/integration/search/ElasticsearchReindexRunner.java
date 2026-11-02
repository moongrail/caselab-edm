package ru.caselab.edm.backend.integration.search;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import ru.caselab.edm.backend.document.domain.Attribute;
import ru.caselab.edm.backend.document.infrastructure.AttributeRepository;
import ru.caselab.edm.backend.document.infrastructure.DocumentRepository;
import ru.caselab.edm.platform.search.SearchReindexer;

import java.util.List;

/**
 * Rebuilds the document attribute index. The platform starter decides when the
 * reindex runs through {@code platform.search.reindex-on-startup}.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class ElasticsearchReindexRunner implements SearchReindexer {

    private final AttributeRepository attributeRepository;
    private final DocumentRepository documentRepository;
    private final AttributeSearchRepository attributeSearchRepository;

    @Override
    public void reindex() {
        try {
            List<Attribute> attributes = attributeRepository.findAll();
            for (Attribute attribute : attributes) {
                List<Long> documents = documentRepository.getDocumentsWithAttribute(
                        attribute.getId());
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
