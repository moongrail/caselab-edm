package ru.caselab.edm.backend.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import ru.caselab.edm.backend.entity.ReplacementManager;
import java.util.Optional;
import java.util.UUID;

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

}
