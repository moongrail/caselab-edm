package ru.caselab.edm.backend.service;

import ru.caselab.edm.backend.entity.DocumentVersion;

public interface DocumentVersionService {
    DocumentVersion getDocumentVersion(long id);
}
