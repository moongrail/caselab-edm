package ru.caselab.edm.backend.service;

import org.springframework.data.domain.Page;
import ru.caselab.edm.backend.dto.DocumentCreateDTO;
import ru.caselab.edm.backend.dto.DocumentVersionCreateDTO;
import ru.caselab.edm.backend.entity.Document;
import ru.caselab.edm.backend.entity.DocumentType;
import ru.caselab.edm.backend.entity.DocumentVersion;

import java.util.UUID;

public interface DocumentVersionService {
    DocumentVersion getDocumentVersion(long id);

    Page<DocumentVersion> getAllDocumentVersions(int page, int size);

    DocumentVersion saveDocumentVersion(DocumentCreateDTO document, Document newDocument, UUID userId);
}
