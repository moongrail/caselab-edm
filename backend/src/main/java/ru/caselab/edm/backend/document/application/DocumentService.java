package ru.caselab.edm.backend.document.application;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import org.springframework.data.domain.Page;
import ru.caselab.edm.backend.approval.api.dto.approvementprocessitem.ApprovementProcessItemDTO;
import ru.caselab.edm.backend.document.api.dto.document.DocumentCreateDTO;
import ru.caselab.edm.backend.document.api.dto.document.DocumentOutputAllDocumentsDTO;
import ru.caselab.edm.backend.document.api.dto.document.DocumentUpdateDTO;
import ru.caselab.edm.backend.document.domain.Document;
import ru.caselab.edm.backend.document.domain.DocumentVersion;
import ru.caselab.edm.platform.security.AuthenticatedUser;
import ru.caselab.edm.backend.document.domain.DocumentSortingType;

import java.util.UUID;

public interface DocumentService {
    Page<DocumentOutputAllDocumentsDTO> getAllDocumentForUser(int page,
                                                              int size,
                                                              UUID userId,
                                                              DocumentSortingType sortingType);

    Page<DocumentOutputAllDocumentsDTO> getAllDocumentWhereUserSignatories(int page,
                                                                           int size,
                                                                           UUID userId,
                                                                           DocumentSortingType sortingType);

    Page<DocumentOutputAllDocumentsDTO> getAllDocumentWhereUserSignatoriesBeforeSigner(int page,
                                                                                       int size,
                                                                                       UUID userId,
                                                                                       DocumentSortingType sortingType);

    Page<DocumentOutputAllDocumentsDTO> getAllDocumentWhereUserSignatoriesAfterSigner(int page,
                                                                                      int size,
                                                                                      UUID userId,
                                                                                      DocumentSortingType sortingType);

    DocumentVersion getLastVersionDocumentForUser(long id, UUID userId);

    DocumentVersion getLastVersionDocumentWhereUserSignatories(long id, UUID userId);

    Page<DocumentVersion> getAllVersionDocumentForUser(long id, UUID userId, int page, int size);

    Page<DocumentVersion> getAllVersionDocumentWhereUserSignatories(long id, UUID userId, int page, int size);

    Page<Document> getAllDocuments(int page, int size);
    Page<DocumentOutputAllDocumentsDTO> getArchivedDocuments(int page, int size, UUID userId);

    Document getDocument(long id);

    DocumentVersion saveDocument(DocumentCreateDTO document, UUID userId);

    DocumentVersion updateDocument(long id, DocumentUpdateDTO document, UUID userId);

    void deleteDocument(long id, UUID userId);

    ApprovementProcessItemDTO sendForSign(UUID userId, Long documentVersionId, AuthenticatedUser authenticatedUser);

    Page<DocumentOutputAllDocumentsDTO> getAllDocumentWhereUserOwnerAfterSigner(int page, int size, UUID userId, DocumentSortingType sortingType);

    Page<DocumentOutputAllDocumentsDTO> getAllDocumentWhereUserOwnerBeforeSigner(int page, int size, UUID userId, DocumentSortingType sortingType);
}
