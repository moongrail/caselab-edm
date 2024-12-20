package ru.caselab.edm.backend.repository.elastic;

import org.springframework.data.elasticsearch.annotations.Query;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;
import ru.caselab.edm.backend.entity.AttributeSearch;

import java.util.List;

public interface AttributeSearchRepository extends ElasticsearchRepository<AttributeSearch, Long> {
    List<AttributeSearch> findByName(String name);

    @Query("{\"wildcard\": {\"name\": \"*?0*\"}}")
    List<AttributeSearch> findByNameWithMinLength(String name);
}
