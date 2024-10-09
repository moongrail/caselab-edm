package ru.caselab.edm.backend.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.caselab.edm.backend.entity.AttributeValue;

import java.util.List;

public interface AttributeValueRepository extends JpaRepository<AttributeValue, Long> {

    List<AttributeValue> findByDocumentId(Long documentId);
    List<AttributeValue> findByAttributeId(Long attributeId);
}
