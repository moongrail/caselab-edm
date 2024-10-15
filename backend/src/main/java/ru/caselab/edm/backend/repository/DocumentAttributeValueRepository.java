package ru.caselab.edm.backend.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.caselab.edm.backend.entity.DocumentAttributeValue;

import java.util.List;
import java.util.Optional;

public interface DocumentAttributeValueRepository extends JpaRepository<DocumentAttributeValue, Long> {

    List<DocumentAttributeValue> findByDocumentVersionId(Long documentId);

    List<DocumentAttributeValue> findByAttributeId(Long attributeId);

    Optional<DocumentAttributeValue> findByDocumentVersionIdAndAttributeId(Long documentId, Long attributeId);
}
