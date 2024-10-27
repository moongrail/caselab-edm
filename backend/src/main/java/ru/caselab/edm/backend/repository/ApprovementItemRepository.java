package ru.caselab.edm.backend.repository;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;
import ru.caselab.edm.backend.entity.ApprovementProcessItem;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface ApprovementItemRepository extends CrudRepository<ApprovementProcessItem, Long> {
    boolean existsByDocumentVersionIdAndUserId(Long documentVersionId, UUID userId);

    Optional<ApprovementProcessItem> findByDocumentVersionIdAndUserId(Long documentVersionId, UUID userId);

}
