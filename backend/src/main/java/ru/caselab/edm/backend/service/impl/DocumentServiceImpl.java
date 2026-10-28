package ru.caselab.edm.backend.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.JpaSort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.caselab.edm.backend.dto.approvementprocessitem.ApprovementProcessItemDTO;
import ru.caselab.edm.backend.dto.document.DocumentCreateDTO;
import ru.caselab.edm.backend.dto.document.DocumentOutputAllDocumentsDTO;
import ru.caselab.edm.backend.dto.document.DocumentUpdateDTO;
import ru.caselab.edm.backend.entity.Attribute;
import ru.caselab.edm.backend.entity.AttributeSearch;
import ru.caselab.edm.backend.entity.Document;
import ru.caselab.edm.backend.entity.DocumentVersion;
import ru.caselab.edm.backend.entity.ApprovementProcessItem;
import ru.caselab.edm.backend.entity.ReplacementManager;
import ru.caselab.edm.backend.entity.User;
import ru.caselab.edm.backend.entity.UserInfoDetails;
import ru.caselab.edm.backend.enums.ApprovementProcessItemStatus;
import ru.caselab.edm.backend.enums.DocumentSortingType;
import ru.caselab.edm.backend.event.DocumentSignRequestEvent;
import ru.caselab.edm.backend.exceptions.ApprovementProccessItemAlreadyExistsException;
import ru.caselab.edm.backend.exceptions.ResourceNotFoundException;
import ru.caselab.edm.backend.mapper.approvementprocessitem.ApprovementProccessItemMapper;
import ru.caselab.edm.backend.repository.ApprovementItemRepository;
import ru.caselab.edm.backend.repository.DocumentRepository;
import ru.caselab.edm.backend.repository.DocumentTypeRepository;
import ru.caselab.edm.backend.repository.DocumentVersionRepository;
import ru.caselab.edm.backend.repository.ReplacementManagerRepository;
import ru.caselab.edm.backend.repository.UserRepository;
import ru.caselab.edm.backend.repository.elastic.AttributeSearchRepository;
import ru.caselab.edm.backend.service.AuthorizationService;
import ru.caselab.edm.backend.service.DocumentService;
import ru.caselab.edm.backend.service.DocumentVersionService;
import ru.caselab.edm.backend.state.DocumentStatus;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class DocumentServiceImpl implements DocumentService {

    private final DocumentRepository documentRepository;
    private final UserRepository userRepository;
    private final DocumentTypeRepository documentTypeRepository;
    private final DocumentVersionRepository documentVersionRepository;
    private final ApplicationEventPublisher eventPublisher;
    private final ApprovementItemRepository approvementItemRepository;
    private final ApprovementProccessItemMapper approvementProccessItemMapper;
    private final ReplacementManagerRepository replacementManagerRepository;
    private final DocumentVersionService documentVersionService;
    private final AttributeSearchRepository attributeSearchRepository;
    private final AuthorizationService authorizationService;

    @Override
    public Page<Document> getAllDocuments(int page, int size) {
        return documentRepository.findAll(PageRequest.of(page, size));
    }

    @Override
    public Document getDocument(long id) {
        return documentRepository.getDocumentById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Document not found with id = %d".formatted(id)));
    }

    @Override
    public Page<DocumentOutputAllDocumentsDTO> getAllDocumentForUser(int page,
                                                                     int size,
                                                                     UUID userId,
                                                                     DocumentSortingType sortingType) {
        PageRequest pageable = buildPageable(page, size, sortingType);
        Page<DocumentOutputAllDocumentsDTO> documents =
                documentRepository.getAllDocumentWithNameAndStatusProjectionWhereUserOwner(userId, pageable);
        log.info("Found {} documents for user {}", documents.getTotalElements(), userId);
        return documents;
    }

    @Override
    public Page<DocumentOutputAllDocumentsDTO> getAllDocumentWhereUserOwnerAfterSigner(int page,
                                                                                       int size,
                                                                                       UUID userId,
                                                                                       DocumentSortingType sortingType) {
        PageRequest pageable = buildPageable(page, size, sortingType);
        Page<DocumentOutputAllDocumentsDTO> documents =
                documentRepository.getAllDocumentWithNameAndStatusProjectionWhereUserOwnerAfterSigner(userId, pageable);
        log.info("Found {} signed documents for owner {}", documents.getTotalElements(), userId);
        return documents;
    }

    @Override
    public Page<DocumentOutputAllDocumentsDTO> getAllDocumentWhereUserOwnerBeforeSigner(int page,
                                                                                        int size,
                                                                                        UUID userId,
                                                                                        DocumentSortingType sortingType) {
        PageRequest pageable = buildPageable(page, size, sortingType);
        Page<DocumentOutputAllDocumentsDTO> documents =
                documentRepository.getAllDocumentWithNameAndStatusProjectionWhereUserOwnerBeforeSigner(userId, pageable);
        log.info("Found {} documents awaiting signature for owner {}", documents.getTotalElements(), userId);
        return documents;
    }

    @Override
    public Page<DocumentOutputAllDocumentsDTO> getAllDocumentWhereUserSignatories(int page,
                                                                                  int size,
                                                                                  UUID userId,
                                                                                  DocumentSortingType sortingType) {
        PageRequest pageable = buildPageable(page, size, sortingType);
        Page<DocumentOutputAllDocumentsDTO> documents =
                documentRepository.getAllDocumentWithNameAndStatusProjectionWhereUserSignatories(userId, pageable);
        log.info("Found {} documents for signatory {}", documents.getTotalElements(), userId);
        return documents;
    }

    @Override
    public Page<DocumentOutputAllDocumentsDTO> getAllDocumentWhereUserSignatoriesBeforeSigner(int page,
                                                                                              int size,
                                                                                              UUID userId,
                                                                                              DocumentSortingType sortingType) {
        PageRequest pageable = buildPageable(page, size, sortingType);
        Page<DocumentOutputAllDocumentsDTO> documents =
                documentRepository.getAllDocumentWithNameAndStatusProjectionWhereUserSignatoriesBeforeSigner(userId, pageable);
        log.info("Found {} documents awaiting signature for signatory {}", documents.getTotalElements(), userId);
        return documents;
    }

    @Override
    public Page<DocumentOutputAllDocumentsDTO> getAllDocumentWhereUserSignatoriesAfterSigner(int page,
                                                                                             int size,
                                                                                             UUID userId,
                                                                                             DocumentSortingType sortingType) {
        PageRequest pageable = buildPageable(page, size, sortingType);
        Page<DocumentOutputAllDocumentsDTO> documents =
                documentRepository.getAllDocumentWithNameAndStatusProjectionWhereUserSignatoriesAfterSigner(userId, pageable);
        log.info("Found {} processed documents for signatory {}", documents.getTotalElements(), userId);
        return documents;
    }

    @Override
    public DocumentVersion getLastVersionDocumentForUser(long id, UUID userId) {
        documentRepository.getDocumentForUser(id, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Document not found"));
        return latestVersionFromRepository(id);
    }

    @Override
    public DocumentVersion getLastVersionDocumentWhereUserSignatories(long id, UUID userId) {
        documentRepository.getDocumentWhereUserSignatories(id, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Document not found"));
        return latestVersionFromRepository(id);
    }

    @Override
    public Page<DocumentVersion> getAllVersionDocumentForUser(long id, UUID userId, int page, int size) {
        documentRepository.getDocumentForUser(id, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Document not found"));
        return getVersionPage(id, page, size);
    }

    @Override
    public Page<DocumentVersion> getAllVersionDocumentWhereUserSignatories(long id,
                                                                           UUID userId,
                                                                           int page,
                                                                           int size) {
        documentRepository.getDocumentWhereUserSignatories(id, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Document not found"));
        return getVersionPage(id, page, size);
    }

    @Transactional
    @Override
    public DocumentVersion saveDocument(DocumentCreateDTO document, UUID userId) {
        Long documentTypeId = document.getDocumentTypeId();
        Document newDocument = new Document();
        log.info("Creating document with name: {}", document.getDocumentName());
        newDocument.setDocumentType(
                documentTypeRepository.findById(documentTypeId)
                        .orElseThrow(() -> new ResourceNotFoundException("Document type not found"))
        );
        newDocument.setUser(userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found")));

        Document saved = documentRepository.save(newDocument);
        indexDocumentAttributes(saved);
        log.info("Document saved with id {}", saved.getId());

        return documentVersionService.saveDocumentVersion(document, saved, userId);
    }

    @Transactional
    @Override
    public DocumentVersion updateDocument(long id, DocumentUpdateDTO document, UUID userId) {
        log.info("Updating document with id: {}", id);
        Document existingDocument = authorizationService.requireOwnedDocument(id, userId);

        existingDocument = documentRepository.save(existingDocument);
        indexDocumentAttributes(existingDocument);

        DocumentVersion documentVersion = documentVersionService.updateDocumentVersion(
                document, existingDocument, userId);
        log.info("Document {} updated successfully", id);
        return documentVersion;
    }

    @Transactional
    @Override
    public void deleteDocument(long id, UUID userId) {
        Document document = authorizationService.requireOwnedDocument(id, userId);
        document.setArchived(true);

        DocumentVersion version = latestVersion(document, id);
        version.getStateMachine().delete(version);

        for (Attribute attribute : document.getDocumentType().getAttributes()) {
            Optional<AttributeSearch> attributeSearch = attributeSearchRepository.findById(attribute.getId());
            attributeSearch.ifPresent(search -> {
                search.getDocuments().remove(document.getId());
                attributeSearchRepository.save(search);
            });
        }

        documentRepository.save(document);
        log.info("Document {} archived by user {}", id, userId);
    }

    @Transactional
    @Override
    public ApprovementProcessItemDTO sendForSign(UUID userId, Long documentId, UserInfoDetails authenticatedUser) {
        DocumentVersion documentVersion = getLastVersionDocumentForUser(documentId, authenticatedUser.getId());
        authorizationService.checkDocumentOwner(documentVersion.getDocument(), authenticatedUser.getId());
        User user = getUserOrTempManagerById(userId);
        if (approvementItemRepository.existsByDocumentVersionIdAndUserId(documentVersion.getId(), user.getId())) {
            throw new ApprovementProccessItemAlreadyExistsException("Provided document already sent to user");
        }

        documentVersion.getStateMachine().sendForSign(documentVersion);

        ApprovementProcessItem item = new ApprovementProcessItem();
        item.setUser(user);
        item.setDocumentVersion(documentVersion);
        item.setStatus(documentVersion.getStatus() == DocumentStatus.PENDING_AUTHOR_SIGN
                ? ApprovementProcessItemStatus.PENDING_AUTHOR_SIGN
                : ApprovementProcessItemStatus.PENDING_CONTRACTOR_SIGN);
        item.setCreatedAt(LocalDateTime.now());
        approvementItemRepository.save(item);
        eventPublisher.publishEvent(new DocumentSignRequestEvent(this, item));
        return approvementProccessItemMapper.toDTO(item);
    }

    @Override
    public Page<DocumentOutputAllDocumentsDTO> getArchivedDocuments(int page, int size, UUID userId) {
        return documentRepository.getArchivedDocumentsForUser(userId, PageRequest.of(page, size));
    }

    private PageRequest buildPageable(int page, int size, DocumentSortingType sortingType) {
        PageRequest pageable = PageRequest.of(page, size);
        if (!DocumentSortingType.WITHOUT.equals(sortingType)) {
            pageable = pageable.withSort(JpaSort.unsafe(sortingType.getDirection(), sortingType.getFieldName()));
        }
        return pageable;
    }

    private Page<DocumentVersion> getVersionPage(long documentId, int page, int size) {
        PageRequest pageable = PageRequest.of(page, size,
                Sort.by(Sort.Direction.DESC, "createdAt").and(Sort.by(Sort.Direction.DESC, "id")));
        return documentVersionRepository.findPageByDocumentId(documentId, pageable);
    }

    private DocumentVersion latestVersion(Document document, long documentId) {
        return document.getDocumentVersion().stream()
                .max(Comparator.comparing(DocumentVersion::getCreatedAt)
                        .thenComparing(DocumentVersion::getId, Comparator.nullsFirst(Comparator.naturalOrder())))
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Document version not found for document id = %d".formatted(documentId)));
    }

    private DocumentVersion latestVersionFromRepository(long documentId) {
        return documentVersionRepository.findByDocumentId(documentId).stream()
                .max(Comparator.comparing(DocumentVersion::getCreatedAt)
                        .thenComparing(DocumentVersion::getId, Comparator.nullsFirst(Comparator.naturalOrder())))
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Document version not found for document id = %d".formatted(documentId)));
    }

    private void indexDocumentAttributes(Document document) {
        for (Attribute attribute : document.getDocumentType().getAttributes()) {
            attributeSearchRepository.findById(attribute.getId()).ifPresent(search -> {
                if (search.getDocuments() == null) {
                    search.setDocuments(new ArrayList<>());
                }
                if (!search.getDocuments().contains(document.getId())) {
                    search.getDocuments().add(document.getId());
                }
                attributeSearchRepository.save(search);
            });
        }
    }

    private User getUserOrTempManagerById(UUID userId) {
        return replacementManagerRepository.findActiveReplacementByManagerUserId(userId)
                .map(ReplacementManager::getTempManagerUser)
                .orElseGet(() -> getUserById(userId));
    }

    private User getUserById(UUID userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "User not found with id = %s".formatted(userId)));
    }
}
