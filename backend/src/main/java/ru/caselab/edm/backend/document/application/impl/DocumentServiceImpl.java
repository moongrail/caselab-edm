package ru.caselab.edm.backend.document.application.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.JpaSort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.caselab.edm.backend.approval.api.dto.approvementprocessitem.ApprovementProcessItemDTO;
import ru.caselab.edm.backend.document.api.dto.document.DocumentCreateDTO;
import ru.caselab.edm.backend.document.api.dto.document.DocumentOutputAllDocumentsDTO;
import ru.caselab.edm.backend.document.api.dto.document.DocumentUpdateDTO;
import ru.caselab.edm.backend.document.domain.Attribute;
import ru.caselab.edm.backend.integration.search.AttributeSearch;
import ru.caselab.edm.backend.document.domain.Document;
import ru.caselab.edm.backend.document.domain.DocumentVersion;
import ru.caselab.edm.backend.approval.domain.ApprovementProcessItem;
import ru.caselab.edm.backend.delegation.domain.ReplacementManager;
import ru.caselab.edm.backend.shared.directory.UserDirectory;
import ru.caselab.edm.backend.shared.directory.UserSummary;
import ru.caselab.edm.platform.security.AuthenticatedUser;
import ru.caselab.edm.backend.approval.domain.ApprovementProcessItemStatus;
import ru.caselab.edm.backend.document.domain.DocumentSortingType;
import ru.caselab.edm.backend.approval.domain.event.DocumentSignRequestEvent;
import ru.caselab.edm.backend.approval.exception.ApprovementProccessItemAlreadyExistsException;
import ru.caselab.edm.backend.shared.exception.ResourceNotFoundException;
import ru.caselab.edm.backend.approval.api.mapper.ApprovementProccessItemMapper;
import ru.caselab.edm.backend.approval.infrastructure.ApprovementItemRepository;
import ru.caselab.edm.backend.document.infrastructure.DocumentRepository;
import ru.caselab.edm.backend.document.infrastructure.DocumentTypeRepository;
import ru.caselab.edm.backend.document.infrastructure.DocumentVersionRepository;
import ru.caselab.edm.backend.delegation.infrastructure.ReplacementManagerRepository;
import ru.caselab.edm.backend.integration.search.AttributeSearchRepository;
import ru.caselab.edm.backend.document.application.AuthorizationService;
import ru.caselab.edm.backend.document.application.DocumentService;
import ru.caselab.edm.backend.document.application.DocumentVersionService;
import ru.caselab.edm.backend.document.domain.state.DocumentStatus;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class DocumentServiceImpl implements DocumentService {

    private final DocumentRepository documentRepository;
    private final DocumentTypeRepository documentTypeRepository;
    private final DocumentVersionRepository documentVersionRepository;
    private final ApplicationEventPublisher eventPublisher;
    private final ApprovementItemRepository approvementItemRepository;
    private final ApprovementProccessItemMapper approvementProccessItemMapper;
    private final ReplacementManagerRepository replacementManagerRepository;
    private final DocumentVersionService documentVersionService;
    private final AttributeSearchRepository attributeSearchRepository;
    private final AuthorizationService authorizationService;
    private final UserDirectory userDirectory;

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
        return enrichAuthors(documents);
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
        return enrichAuthors(documents);
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
        return enrichAuthors(documents);
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
        return enrichAuthors(documents);
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
        return enrichAuthors(documents);
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
        return enrichAuthors(documents);
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
        newDocument.setOwnerId(userId);
        userDirectory.requireUser(userId);

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
    public ApprovementProcessItemDTO sendForSign(UUID userId, Long documentId, AuthenticatedUser authenticatedUser) {
        DocumentVersion documentVersion = getLastVersionDocumentForUser(documentId, authenticatedUser.id());
        authorizationService.checkDocumentOwner(documentVersion.getDocument(), authenticatedUser.id());
        UUID signerId = getUserOrTempManagerById(userId);
        if (approvementItemRepository.existsByDocumentVersionIdAndSignerId(documentVersion.getId(), signerId)) {
            throw new ApprovementProccessItemAlreadyExistsException("Provided document already sent to user");
        }

        documentVersion.getStateMachine().sendForSign(documentVersion);

        ApprovementProcessItem item = new ApprovementProcessItem();
        item.setSignerId(signerId);
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
        return enrichAuthors(documentRepository.getArchivedDocumentsForUser(userId, PageRequest.of(page, size)));
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

    private UUID getUserOrTempManagerById(UUID userId) {
        UUID signerId = replacementManagerRepository.findActiveReplacementByManagerUserId(userId)
                .map(ReplacementManager::getTempManagerUserId)
                .orElse(userId);
        userDirectory.requireUser(signerId);
        return signerId;
    }

    private Page<DocumentOutputAllDocumentsDTO> enrichAuthors(
            Page<DocumentOutputAllDocumentsDTO> page) {
        if (page.isEmpty()) {
            return page;
        }
        Set<UUID> ownerIds = page.getContent().stream()
                .map(DocumentOutputAllDocumentsDTO::getOwnerId)
                .filter(java.util.Objects::nonNull)
                .collect(Collectors.toSet());
        Map<UUID, String> logins = ownerIds.isEmpty()
                ? Map.of()
                : userDirectory.findUsers(ownerIds).stream()
                .collect(Collectors.toMap(UserSummary::id, UserSummary::login));
        page.getContent().forEach(document -> {
            if (document.getOwnerId() != null) {
                document.setAuthor(logins.get(document.getOwnerId()));
            }
        });
        return page;
    }
}
