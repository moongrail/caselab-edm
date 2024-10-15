package ru.caselab.edm.backend.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.caselab.edm.backend.entity.DocumentVersion;

public interface DocumentVersionRepository extends JpaRepository<DocumentVersion, Long> {
}
