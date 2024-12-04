package ru.caselab.edm.backend.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import ru.caselab.edm.backend.entity.ApprovementProcess;
import ru.caselab.edm.backend.entity.DocumentVersion;
import ru.caselab.edm.backend.repository.projection.TopVotesByParticipants;

import java.time.LocalDateTime;
import java.util.Optional;

@Repository
public interface ApprovementProcessRepository extends JpaRepository<ApprovementProcess, Long> {

    Optional<ApprovementProcess> getApprovementProcessByDocumentVersion(DocumentVersion documentVersion);

    @Query("""
    SELECT 
        p.id as processId,
        d.id as documentId,
        COUNT(i) as participantsCount
    FROM ApprovementProcess p
    JOIN p.documentVersion dv
    JOIN p.approvementProcessItems i
    JOIN dv.document d
    WHERE p.deadline BETWEEN :startDate AND :endDate
    GROUP BY p.id, d.id
    ORDER BY participantsCount DESC
    """)
    Page<TopVotesByParticipants> findTopVotesByParticipants(@Param("startDate") LocalDateTime startDate, @Param("endDate") LocalDateTime endDate, Pageable pageable);

}
