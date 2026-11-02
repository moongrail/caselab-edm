package ru.caselab.edm.backend.analytics.infrastructure;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import ru.caselab.edm.backend.document.domain.Document;
import ru.caselab.edm.backend.analytics.infrastructure.projection.TopUsersByDocumentCreationProjection;
import ru.caselab.edm.backend.analytics.infrastructure.projection.TopUsersByDocumentSigningProjection;
import ru.caselab.edm.backend.analytics.infrastructure.projection.TopUsersByReplacementProjection;

import java.time.Instant;
import java.time.LocalDateTime;

/**
 * Cross-context analytics queries that intentionally live in the application
 * module: they aggregate document, signature and delegation identifiers.
 * aggregates.
 */
@Repository
public interface AnalyticsRepository extends JpaRepository<Document, Long> {

    @Query("""
            SELECT d.ownerId as userId, COUNT(d) as documentCount
            FROM Document d
            WHERE d.createdAt BETWEEN :startDate AND :endDate
            GROUP BY d.ownerId
            ORDER BY COUNT(d) DESC
            """)
    Page<TopUsersByDocumentCreationProjection> findTopUserByDocumentCreation(
            @Param("startDate") Instant startDate,
            @Param("endDate") Instant endDate,
            Pageable pageable);

    @Query("""
            SELECT s.approvementProcessItem.signerId as userId, COUNT(s) as signatureCount
            FROM Signature s
            WHERE s.createdAt BETWEEN :startDate AND :endDate
            GROUP BY s.approvementProcessItem.signerId
            ORDER BY COUNT(s) DESC
            """)
    Page<TopUsersByDocumentSigningProjection> findTopUserByDocumentSigning(
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate,
            Pageable pageable);

    @Query("""
            SELECT rm.tempManagerUserId as userId, COUNT(rm) as replacementCount
            FROM ReplacementManager rm
            WHERE rm.startDate <= :endDate AND rm.endDate >= :startDate
            GROUP BY rm.tempManagerUserId
            ORDER BY COUNT(rm) DESC
            """)
    Page<TopUsersByReplacementProjection> findTopUserByReplacement(
            @Param("startDate") Instant startDate,
            @Param("endDate") Instant endDate,
            Pageable pageable);
}
