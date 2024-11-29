package ru.caselab.edm.backend.service;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import org.springframework.data.domain.Page;
import ru.caselab.edm.backend.dto.approvementprocessitem.ApprovementProcessItemDTO;
import ru.caselab.edm.backend.dto.document.DocumentCreateDTO;
import ru.caselab.edm.backend.dto.document.DocumentOutputAllDocumentsDTO;
import ru.caselab.edm.backend.dto.document.DocumentUpdateDTO;
import ru.caselab.edm.backend.entity.Document;
import ru.caselab.edm.backend.entity.DocumentVersion;
import ru.caselab.edm.backend.entity.UserInfoDetails;
import ru.caselab.edm.backend.enums.DocumentSortingType;

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

    Document getDocument(long id);

    DocumentVersion saveDocument(DocumentCreateDTO document, UUID userId);

    DocumentVersion updateDocument(long id, DocumentUpdateDTO document, UUID userId);

    void deleteDocument(long id);

    ApprovementProcessItemDTO sendForSign(UUID userId, Long documentVersionId, UserInfoDetails authenticatedUser);

    Page<DocumentOutputAllDocumentsDTO> getAllDocumentWhereUserOwnerAfterSigner(int page, int size, UUID userId, DocumentSortingType sortingType);

    Page<DocumentOutputAllDocumentsDTO> getAllDocumentWhereUserOwnerBeforeSigner(int page, int size, UUID userId, DocumentSortingType sortingType);
}
