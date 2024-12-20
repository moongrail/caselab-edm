package ru.caselab.edm.backend.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import ru.caselab.edm.backend.dto.document.DocumentOutputAllDocumentsDTO;
import ru.caselab.edm.backend.entity.Document;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface DocumentRepository extends JpaRepository<Document, Long> {
    @Query(value = """
            SELECT DISTINCT ON (d.id) d.id, d.user_id, d.document_type_id, d.created_at, d.is_archive
            FROM documents d
            JOIN document_versions dv
            ON d.id = dv.documents_id
            LEFT JOIN approvment_process_item api
            ON dv.id = api.document_version_id
            WHERE d.id = :documentId AND (d.user_id = :userId) and d.is_archive = false
            """,
            nativeQuery = true)
    Optional<Document> getDocumentForUser(Long documentId, UUID userId);

    @Query(value = """
            SELECT DISTINCT ON (d.id) d.id, d.user_id, d.document_type_id, d.created_at, d.is_archive
            FROM documents d
            LEFT JOIN document_versions dv
            ON d.id = dv.documents_id
            LEFT JOIN approvment_process_item api
            ON dv.id = api.document_version_id
            WHERE d.id = :documentId AND api.user_id = :userId and d.is_archive = false
            """,
            nativeQuery = true)
    Optional<Document> getDocumentWhereUserSignatories(Long documentId, UUID userId);

    @Query(value = """
            SELECT new ru.caselab.edm.backend.dto.document.DocumentOutputAllDocumentsDTO (d.id as id,
                                                                                            u.login as login,
                                                                                            d.createdAt as createdAt,
                                                                                            dv.documentName as documentName, 
                                                                                            dv.contentUrl as contentUrl, 
                                                                                            dv.state as state)
                        FROM Document d 
                        left join d.documentVersion dv 
                        left join dv.approvementProcessItems api 
                        left join User u1 
                        ON (u1.id = api.user.id)
                        left join User u 
                        ON (u.id = d.user.id)
             			WHERE dv.id = (select dv1.id
             		                               from DocumentVersion dv1
             		                               where dv1.document.id = d.id
                                                   order by dv1.createdAt DESC
             		                               LIMIT 1)
             		    AND api.user.id = :userId and d.isArchived = false
            """, countQuery = """
            SELECT count(d.id)
             			FROM Document d
                        left join d.documentVersion dv
                        left join dv.approvementProcessItems api
                        left join User u
                        ON (u.id = api.user.id)
             			WHERE dv.id = (select dv1.id
             		                               from DocumentVersion dv1
             		                               where dv1.document.id = d.id
                                                   order by dv1.createdAt DESC
             		                               LIMIT 1)
             		    AND api.user.id = :userId and d.isArchived = false
            """)
    Page<DocumentOutputAllDocumentsDTO> getAllDocumentWithNameAndStatusProjectionWhereUserSignatories(UUID userId, Pageable pageable);

    @Query(value = """
            SELECT new ru.caselab.edm.backend.dto.document.DocumentOutputAllDocumentsDTO (d.id as id,
                                                                                            u.login as login,
                                                                                            d.createdAt as createdAt,
                                                                                            dv.documentName as documentName, 
                                                                                            dv.contentUrl as contentUrl, 
                                                                                            dv.state as state)
                        FROM Document d 
                        left join d.documentVersion dv 
                        left join dv.approvementProcessItems api 
                        left join User u1 
                        ON (u1.id = api.user.id)
                        left join User u 
                        ON (u.id = d.user.id)
             			WHERE dv.id = (select dv1.id
             		                               from DocumentVersion dv1
             		                               where dv1.document.id = d.id
                                                   order by dv1.createdAt DESC
             		                               LIMIT 1)
             		    AND api.status in ( 'PENDING_CONTRACTOR_SIGN', 'PENDING_AUTHOR_SIGN')
             		    AND api.user.id = :userId and d.isArchived = false
            """, countQuery = """
            SELECT count(d.id)
             			FROM Document d
                        left join d.documentVersion dv
                        left join dv.approvementProcessItems api
                        left join User u
                        ON (u.id = api.user.id)
             			WHERE dv.id = (select dv1.id
             		                               from DocumentVersion dv1
             		                               where dv1.document.id = d.id
                                                   order by dv1.createdAt DESC
             		                               LIMIT 1)
             		    AND api.status in ( 'PENDING_CONTRACTOR_SIGN', 'PENDING_AUTHOR_SIGN')
             		    AND api.user.id = :userId and d.isArchived = false
            """)
    Page<DocumentOutputAllDocumentsDTO> getAllDocumentWithNameAndStatusProjectionWhereUserSignatoriesBeforeSigner(UUID userId, Pageable pageable);

    @Query(value = """
            SELECT new ru.caselab.edm.backend.dto.document.DocumentOutputAllDocumentsDTO (d.id as id,
                                                                                            u.login as login,
                                                                                            d.createdAt as createdAt,
                                                                                            dv.documentName as documentName, 
                                                                                            dv.contentUrl as contentUrl, 
                                                                                            dv.state as state)
                        FROM Document d 
                        left join d.documentVersion dv 
                        left join dv.approvementProcessItems api 
                        left join User u1 
                        ON (u1.id = api.user.id)
                        left join User u 
                        ON (u.id = d.user.id)
             			WHERE dv.id = (select dv1.id
             		                               from DocumentVersion dv1
             		                               where dv1.document.id = d.id
                                                   order by dv1.createdAt DESC
             		                               LIMIT 1)
             		    AND api.status in ( 'APPROVED', 'REJECTED', 'REWORK_REQUIRED')
             		    AND api.user.id = :userId and d.isArchived = false
            """, countQuery = """
            SELECT count(d.id)
             			FROM Document d
                        left join d.documentVersion dv
                        left join dv.approvementProcessItems api
                        left join User u
                        ON (u.id = api.user.id)
             			WHERE dv.id = (select dv1.id
             		                               from DocumentVersion dv1
             		                               where dv1.document.id = d.id
                                                   order by dv1.createdAt DESC
             		                               LIMIT 1)
             		    AND api.status in ('APPROVED', 'REJECTED', 'REWORK_REQUIRED')
             		    AND api.user.id = :userId and d.isArchived = false
            """)
    Page<DocumentOutputAllDocumentsDTO> getAllDocumentWithNameAndStatusProjectionWhereUserSignatoriesAfterSigner(UUID userId, Pageable pageable);

    @Query(value = """
            SELECT new ru.caselab.edm.backend.dto.document.DocumentOutputAllDocumentsDTO(d.id as id,
                                                                                            u.login as login,
                                                                                            d.createdAt as createdAt, 
                                                                                            dv.documentName as documentName, 
                                                                                            dv.contentUrl as contentUrl, 
                                                                                            dv.state as state)
                        FROM Document d left join d.documentVersion dv left join User u ON (u.id = d.user.id)
             			WHERE dv.id = (select dv1.id
             		                               from DocumentVersion dv1
             		                               where dv1.document.id = d.id
                                                   order by dv1.createdAt DESC
             		                               LIMIT 1)
             		    AND (u.id = :userId) and d.isArchived = false
            """, countQuery = """
            SELECT count(d.id)
             			FROM Document d join d.documentVersion dv join User u ON (u.id = d.user.id)
             			WHERE dv.id = (select dv1.id
             		                               from DocumentVersion dv1
             		                               where dv1.document.id = d.id
                                                   order by dv1.createdAt DESC
             		                               LIMIT 1)
             		    AND (u.id = :userId) and d.isArchived = false
            """)
    Page<DocumentOutputAllDocumentsDTO> getAllDocumentWithNameAndStatusProjectionWhereUserOwner(UUID userId, Pageable pageable);

    @Query(value = """
            SELECT new ru.caselab.edm.backend.dto.document.DocumentOutputAllDocumentsDTO(d.id as id,
                                                                                            u.login as login,
                                                                                            d.createdAt as createdAt, 
                                                                                            dv.documentName as documentName, 
                                                                                            dv.contentUrl as contentUrl, 
                                                                                            dv.state as state)
                        FROM Document d 
                        left join d.documentVersion dv 
                        left join User u ON (u.id = d.user.id)
                        left join ApprovementProcess ap ON (ap.documentVersion.document.id = d.id)
             			WHERE dv.id = (select dv1.id
             		                               from DocumentVersion dv1
             		                               where dv1.document.id = d.id
                                                   order by dv1.createdAt DESC
             		                               LIMIT 1)
             		    AND (u.id = :userId)
             		    AND (state IN ('PENDING_AUTHOR_SIGN', 'PENDING_CONTRACTOR_SIGN')
             		    OR ap.status IN('PUBLISHED_FOR_VOTING'))
             		    
            """, countQuery = """
            SELECT count(d.id)
             			FROM Document d
             			left join d.documentVersion dv
             			left join User u ON (u.id = d.user.id)
             			left join ApprovementProcess ap ON (ap.documentVersion.document.id = d.id)
             			WHERE dv.id = (select dv1.id
             		                               from DocumentVersion dv1
             		                               where dv1.document.id = d.id
                                                   order by dv1.createdAt DESC
             		                               LIMIT 1)
             		    AND (u.id = :userId)
             		    AND (state IN ('PENDING_AUTHOR_SIGN', 'PENDING_CONTRACTOR_SIGN')
             		    OR ap.status IN('PUBLISHED_FOR_VOTING'))
            """)
    Page<DocumentOutputAllDocumentsDTO> getAllDocumentWithNameAndStatusProjectionWhereUserOwnerBeforeSigner(UUID userId, Pageable pageable);

    @Query(value = """
            SELECT new ru.caselab.edm.backend.dto.document.DocumentOutputAllDocumentsDTO(d.id as id,
                                                                                            u.login as login,
                                                                                            d.createdAt as createdAt,
                                                                                            dv.documentName as documentName,
                                                                                            dv.contentUrl as contentUrl,
                                                                                            dv.state as state)
                        FROM Document d 
                        left join d.documentVersion dv 
                        left join User u ON (u.id = d.user.id)
                        left join ApprovementProcess ap ON (ap.documentVersion.document.id = d.id)
             			WHERE dv.id = (select dv1.id
             		                               from DocumentVersion dv1
             		                               where dv1.document.id = d.id
                                                   order by dv1.createdAt DESC
             		                               LIMIT 1)
             		    AND (u.id = :userId)
             		    AND (state IN ('APPROVED', 'REJECTED', 'REWORK_REQUIRED', 'DELETED')
             		    OR ap.status IN ('VOTING_COMPLETED', 'VOTING_APPROVED', 'VOTING_REJECTED'))
            """, countQuery = """
            SELECT count(d.id)
             			FROM Document d
             			left join d.documentVersion dv 
             			left join User u ON (u.id = d.user.id)
             			left join ApprovementProcess ap ON (ap.documentVersion.document.id = d.id)
             			WHERE dv.id = (select dv1.id
             		                               from DocumentVersion dv1
             		                               where dv1.document.id = d.id
                                                   order by dv1.createdAt DESC
             		                               LIMIT 1)
             		    AND (u.id = :userId)
             		    AND (state IN ('APPROVED', 'REJECTED', 'REWORK_REQUIRED', 'DELETED')
             		    OR ap.status IN ('VOTING_COMPLETED', 'VOTING_APPROVED', 'VOTING_REJECTED'))
            """)
    Page<DocumentOutputAllDocumentsDTO> getAllDocumentWithNameAndStatusProjectionWhereUserOwnerAfterSigner(UUID userId, Pageable pageable);

    @Query(value = """
            SELECT DISTINCT ON (d.id) d.id, d.user_id, d.document_type_id, d.created_at
            FROM documents d
            LEFT JOIN document_versions dv
            ON d.id = dv.documents_id
            LEFT JOIN approvment_process_item api
            ON dv.id = api.document_version_id
            WHERE api.user_id = :userId OR d.user_id = :userId and d.is_archive = false
            """, countQuery = """
            SELECT DISTINCT ON (d.id) d.id, d.user_id, d.document_type_id, d.created_at
            FROM documents d
            LEFT JOIN document_versions dv
            ON d.id = dv.documents_id
            LEFT JOIN approvment_process_item api
            ON dv.id = api.document_version_id
            WHERE api.user_id = :userId OR d.user_id = :userId and d.is_archive = false
            """,
            nativeQuery = true)
    Page<Document> getAllDocumentForUser(UUID userId, Pageable pageable);

    @Query(value = """
           SELECT d.id FROM documents d
           JOIN document_types dt
           ON dt.id = d.document_type_id
           JOIN document_type_attributes dta
           ON dta.doc_type_id = dt.id
           JOIN attributes a
           ON dta.attribute_id = a.id
           WHERE a.id = :attributeId
            """,
            nativeQuery = true)
    List<Long> getDocumentsWithAttribute(@Param("attributeId") Long attributeId);

    @Query("SELECT d FROM Document d WHERE d.id = :id AND d.isArchived = false")
    Optional<Document> getDocumentById(@Param("id") Long id);


    @Query(value = """
            SELECT new ru.caselab.edm.backend.dto.document.DocumentOutputAllDocumentsDTO(d.id as id,
                                                                                            u.login as login,
                                                                                            d.createdAt as createdAt, 
                                                                                            dv.documentName as documentName, 
                                                                                            dv.contentUrl as contentUrl, 
                                                                                            dv.state as state)
                        FROM Document d left join d.documentVersion dv left join User u ON (u.id = d.user.id)
             			WHERE dv.id = (select dv1.id
             		                               from DocumentVersion dv1
             		                               where dv1.document.id = d.id
                                                   order by dv1.createdAt DESC
             		                               LIMIT 1)
             		    AND (u.id = :userId) and d.isArchived = true
            """, countQuery = """
            SELECT count(d.id)
             			FROM Document d join d.documentVersion dv join User u ON (u.id = d.user.id)
             			WHERE dv.id = (select dv1.id
             		                               from DocumentVersion dv1
             		                               where dv1.document.id = d.id
                                                   order by dv1.createdAt DESC
             		                               LIMIT 1)
             		    AND (u.id = :userId) and d.isArchived = true 
            """)
    Page<DocumentOutputAllDocumentsDTO> getArchivedDocumentsForUser(UUID userId, Pageable pageable);

    Optional<Document> getDocumentByUserIdAndId(UUID user_id, Long id);


}
