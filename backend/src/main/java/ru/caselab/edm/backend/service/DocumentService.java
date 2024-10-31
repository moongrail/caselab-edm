package ru.caselab.edm.backend.service;

import org.springframework.data.domain.Page;
import ru.caselab.edm.backend.dto.ApprovementProcessItemDTO;
import org.springframework.transaction.annotation.Transactional;
import ru.caselab.edm.backend.dto.ApprovementProcessItemDTO;
import ru.caselab.edm.backend.dto.DocumentCreateDTO;
import ru.caselab.edm.backend.dto.DocumentUpdateDTO;
import ru.caselab.edm.backend.entity.Document;
import ru.caselab.edm.backend.entity.DocumentVersion;
import ru.caselab.edm.backend.entity.UserInfoDetails;

import java.util.UUID;

public interface DocumentService {
    Page<Document> getAllDocumentForUser(int page, int size, UUID userId);

    Document getDocumentForUser(long id, UUID userId);

    Page<Document> getAllDocuments(int page, int size);

    Document getDocument(long id);

    Document saveDocument(DocumentCreateDTO document, UUID userId);

    Document updateDocument(long id, DocumentUpdateDTO document, UUID userId);

    void deleteDocument(long id);

    ApprovementProcessItemDTO sendForSign(UUID userId, Long documentVersionId, UserInfoDetails authenticatedUser);
}
