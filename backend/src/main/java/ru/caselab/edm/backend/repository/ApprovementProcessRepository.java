package ru.caselab.edm.backend.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import ru.caselab.edm.backend.entity.ApprovementProcess;
import ru.caselab.edm.backend.entity.DocumentVersion;

import java.util.Optional;

@Repository
public interface ApprovementProcessRepository extends JpaRepository<ApprovementProcess, Long> {
    Optional<ApprovementProcess> getApprovementProcessByDocumentVersion(DocumentVersion documentVersion);


}
