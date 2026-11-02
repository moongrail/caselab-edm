package ru.caselab.edm.backend.approval.infrastructure;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import ru.caselab.edm.backend.approval.domain.ApprovementProcess;
import ru.caselab.edm.backend.approval.domain.ApprovementProcessItem;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface ApprovementItemRepository extends JpaRepository<ApprovementProcessItem, Long> {
    boolean existsByDocumentVersionIdAndSignerId(Long documentVersionId, UUID userId);

    Optional<ApprovementProcessItem> findByDocumentVersionIdAndSignerId(Long documentVersionId, UUID userId);

    List<ApprovementProcessItem> findAllByApprovementProcess(ApprovementProcess approvementProcess);

}
