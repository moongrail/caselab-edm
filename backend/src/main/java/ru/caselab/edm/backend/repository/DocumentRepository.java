package ru.caselab.edm.backend.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import ru.caselab.edm.backend.entity.Document;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface DocumentRepository extends JpaRepository<Document, Long> {
    @Query(value = """
            SELECT d.*
            FROM documents d
            LEFT JOIN document_versions dv
            ON d.id = dv.documents_id
            LEFT JOIN signatures s
            ON dv.id = s.document_version_id
            WHERE d.id = :documentId AND s.user_id = :userId OR d.id = :documentId AND d.user_id = :userId
            """,
            nativeQuery = true)
    Optional<Document> getDocumentForUser(Long documentId, UUID userId);

    @Query(value = """
            SELECT d.*
            FROM documents d
            LEFT JOIN document_versions dv
            ON d.id = dv.documents_id
            LEFT JOIN signatures s
            ON dv.id = s.document_version_id
            WHERE s.user_id = :userId OR d.user_id = :userId
            """,
            countQuery = """
                    SELECT COUNT(d.*)
                    FROM documents d
                    LEFT JOIN document_versions dv
                    ON d.id = dv.documents_id
                    LEFT JOIN signatures s
                    ON dv.id = s.document_version_id
                    WHERE s.user_id = :userId OR d.user_id = :userId""",
            nativeQuery = true)
    Page<Document> getAllDocumentForUser(UUID userId, Pageable pageable);
}
