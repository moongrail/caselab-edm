package ru.caselab.edm.backend.service;

import org.springframework.data.domain.Page;
import ru.caselab.edm.backend.dto.DocumentCreateDTO;
import ru.caselab.edm.backend.dto.DocumentUpdateDTO;
import ru.caselab.edm.backend.entity.Document;
import ru.caselab.edm.backend.entity.DocumentVersion;

public interface DocumentService {
    Page<Document> getAllDocuments(int page, int size);

    Page<DocumentVersion> getAllDocumentVersions(int page, int size);

    Document getDocument(long id);

    DocumentVersion getDocumentVersion(long id);

    DocumentVersion saveDocument(DocumentCreateDTO document);

    DocumentVersion updateDocument(long id, DocumentUpdateDTO document);

    void deleteDocument(long id);
}
