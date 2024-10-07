package ru.caselab.edm.backend.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.caselab.edm.backend.entity.Document;

public interface DocumentRepository extends JpaRepository<Document, Long> {
}
