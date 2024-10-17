package ru.caselab.edm.backend.service;

import org.springframework.data.domain.Page;
import ru.caselab.edm.backend.dto.DocumentCreateDTO;
import ru.caselab.edm.backend.dto.DocumentUpdateDTO;
import ru.caselab.edm.backend.entity.Document;

import java.util.List;
import java.util.UUID;

public interface DocumentService {
    Page<Document> getAllDocuments(int page, int size);

    Document getDocument(long id);

    Document saveDocument(DocumentCreateDTO document);

    Document updateDocument(long id, DocumentUpdateDTO document);

    void deleteDocument(long id);

    void sendForSign(List<UUID> userIds, Long documentVersionId);
}
