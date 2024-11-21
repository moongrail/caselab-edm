package ru.caselab.edm.backend.repository;

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

    @Query("SELECT dv FROM DocumentVersion dv JOIN dv.document d WHERE d.id = :documentId AND d.isArchived = false")
    List<DocumentVersion> findByDocumentId(@Param("documentId")Long documentId);

}
