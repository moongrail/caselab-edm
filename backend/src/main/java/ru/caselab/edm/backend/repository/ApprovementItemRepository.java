package ru.caselab.edm.backend.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import ru.caselab.edm.backend.entity.ApprovementProcess;
import ru.caselab.edm.backend.entity.ApprovementProcessItem;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface ApprovementItemRepository extends JpaRepository<ApprovementProcessItem, Long> {
    boolean existsByDocumentVersionIdAndUserId(Long documentVersionId, UUID userId);

    Optional<ApprovementProcessItem> findByDocumentVersionIdAndUserId(Long documentVersionId, UUID userId);

    List<ApprovementProcessItem> findAllByApprovementProcess(ApprovementProcess approvementProcess);

}
