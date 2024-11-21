package ru.caselab.edm.backend.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import ru.caselab.edm.backend.entity.ReplacementManager;

import java.util.*;

@Repository
public interface ReplacementManagerRepository extends JpaRepository<ReplacementManager, Long> {

    @Query("""
        SELECT rm
        FROM ReplacementManager rm
        WHERE rm.managerUser.id = :userId
            AND (rm.startDate < CURRENT_TIMESTAMP AND rm.endDate > CURRENT_TIMESTAMP)
        ORDER BY rm.startDate DESC
        """)
    Optional<ReplacementManager> findActiveReplacementByManagerUserId(@Param("userId") UUID userId);

    @Query("""
    SELECT rm
    FROM ReplacementManager rm
    WHERE rm.managerUser.id IN :userIds
            AND rm.startDate = (
                SELECT MAX(rm2.startDate)
                FROM ReplacementManager rm2
                WHERE rm2.managerUser.id = rm.managerUser.id
                AND rm2.startDate < CURRENT_TIMESTAMP
                AND rm2.endDate > CURRENT_TIMESTAMP
            )
    """)
    List<ReplacementManager> findActiveReplacementsByManagerUserIds(@Param("userIds") Collection<UUID> userIds);

}
