package ru.caselab.edm.backend.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import ru.caselab.edm.backend.entity.Document;

import java.util.UUID;

@Repository
public interface DocumentRepository extends JpaRepository<Document, Long> {
    @Query(value = """
            SELECT *
            FROM documents
            WHERE id = :documentId AND user_id = :userId
            """,
            nativeQuery = true)
    Document getDocumentForUser(Long documentId, UUID userId);

    @Query(value = """
            SELECT d.*
            FROM documents d
            JOIN document_versions dv
            ON d.id = dv.documents_id
            JOIN signatures s
            ON dv.id = s.document_version_id
            WHERE s.user_id = :userId OR d.user_id = :userId
            ORDER BY d.id
            LIMIT :limit
            OFFSET :offset""",
            nativeQuery = true)
    Page<Document> getAllDocumentForUser(UUID userId, int limit, int offset);
}
