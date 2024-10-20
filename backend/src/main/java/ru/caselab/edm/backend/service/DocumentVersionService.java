package ru.caselab.edm.backend.service;

import org.springframework.data.domain.Page;
import ru.caselab.edm.backend.entity.DocumentVersion;

public interface DocumentVersionService {
    DocumentVersion getDocumentVersion(long id);
    Page<DocumentVersion> getAllDocumentVersions(int page, int size);
}
