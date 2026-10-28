package ru.caselab.edm.backend.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import ru.caselab.edm.backend.entity.DocumentVersion;

import java.util.List;
import java.util.Optional;

@Repository
public interface DocumentVersionRepository extends JpaRepository<DocumentVersion, Long> {

    Optional<DocumentVersion> findByDocumentName(String name);

    @EntityGraph(attributePaths = {
            "document",
            "document.user",
            "documentAttributeValue",
            "documentAttributeValue.attribute"
    })
    @Query("SELECT DISTINCT dv FROM DocumentVersion dv JOIN dv.document d WHERE d.id = :documentId AND d.isArchived = false")
    List<DocumentVersion> findByDocumentId(@Param("documentId") Long documentId);

    @EntityGraph(attributePaths = {
            "document",
            "document.user",
            "documentAttributeValue",
            "documentAttributeValue.attribute"
    })
    @Query("SELECT DISTINCT dv FROM DocumentVersion dv WHERE dv.document.id = :documentId")
    Page<DocumentVersion> findPageByDocumentId(@Param("documentId") Long documentId, Pageable pageable);
}
