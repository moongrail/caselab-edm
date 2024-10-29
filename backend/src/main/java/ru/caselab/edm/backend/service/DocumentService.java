package ru.caselab.edm.backend.service;

import org.springframework.data.domain.Page;
import ru.caselab.edm.backend.dto.DocumentCreateDTO;
import ru.caselab.edm.backend.dto.DocumentUpdateDTO;
import ru.caselab.edm.backend.entity.Document;
import ru.caselab.edm.backend.entity.DocumentVersion;

import java.util.List;
import java.util.UUID;

public interface DocumentService {
    Page<DocumentVersion> getAllDocumentForUser(int page, int size, UUID userId);

    DocumentVersion getDocumentForUser(long id, UUID userId);

    Page<Document> getAllDocuments(int page, int size);

    Document getDocument(long id);

    DocumentVersion saveDocument(DocumentCreateDTO document);

    DocumentVersion updateDocument(long id, DocumentUpdateDTO document);

    void deleteDocument(long id);

    void sendForSign(List<UUID> userIds, Long documentVersionId);
}
