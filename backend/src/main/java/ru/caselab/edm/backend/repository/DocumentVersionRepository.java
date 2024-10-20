package ru.caselab.edm.backend.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import ru.caselab.edm.backend.entity.DocumentVersion;

@Repository
public interface DocumentVersionRepository extends JpaRepository<DocumentVersion, Long> {
}
