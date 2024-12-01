package ru.caselab.edm.backend.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.jpa.domain.JpaSort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.caselab.edm.backend.dto.approvementprocessitem.ApprovementProcessItemDTO;
import ru.caselab.edm.backend.dto.document.DocumentCreateDTO;
import ru.caselab.edm.backend.dto.document.DocumentOutputAllDocumentsDTO;
import ru.caselab.edm.backend.dto.document.DocumentUpdateDTO;
import ru.caselab.edm.backend.entity.*;
import ru.caselab.edm.backend.enums.ApprovementProcessItemStatus;
import ru.caselab.edm.backend.enums.DocumentSortingType;
import ru.caselab.edm.backend.event.DocumentSignRequestEvent;
import ru.caselab.edm.backend.exceptions.ApprovementProccessItemAlreadyExistsException;
import ru.caselab.edm.backend.exceptions.DocumentForbiddenAccess;
import ru.caselab.edm.backend.exceptions.ResourceNotFoundException;
import ru.caselab.edm.backend.exceptions.WrongDateException;
import ru.caselab.edm.backend.mapper.approvementprocessitem.ApprovementProccessItemMapper;
import ru.caselab.edm.backend.repository.*;
import ru.caselab.edm.backend.repository.elastic.AttributeSearchRepository;
import ru.caselab.edm.backend.service.DocumentService;
import ru.caselab.edm.backend.service.DocumentVersionService;
import ru.caselab.edm.backend.state.DocumentStatus;

import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class DocumentServiceImpl implements DocumentService {

    private final DocumentRepository documentRepository;
    private final UserRepository userRepository;
    private final DocumentTypeRepository documentTypeRepository;
    private final ApplicationEventPublisher eventPublisher;
    private final ApprovementItemRepository approvementItemRepository;
    private final ApprovementProccessItemMapper approvementProccessItemMapper;
    private final ReplacementManagerRepository replacementManagerRepository;
    private final DocumentVersionService documentVersionService;
    private final AttributeSearchRepository attributeSearchRepository;


    @Override
    public Page<Document> getAllDocuments(int page, int size) {
        return documentRepository.findAll(PageRequest.of(page, size));
    }

    @Override
    public Document getDocument(long id) {
        return documentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Document not found"));
    }

    @Override
    public Page<DocumentOutputAllDocumentsDTO> getAllDocumentForUser(int page,
                                                                     int size,
                                                                     UUID userId,
                                                                     DocumentSortingType sortingType) {
        log.info("Get all document for user with sorting- page: {}, size: {}, userId: {}, sortingType: {}",
                page, size, userId, sortingType);
        PageRequest pageable = PageRequest.of(page, size);
        if (!DocumentSortingType.WITHOUT.equals(sortingType)) {
            pageable = pageable.withSort(JpaSort.unsafe(sortingType.getDirection(), sortingType.getFieldName()));
        }
        Page<DocumentOutputAllDocumentsDTO> allDocumentWithNameAndStatusProjectionForUser =
                documentRepository.getAllDocumentWithNameAndStatusProjectionWhereUserOwner(userId, pageable);

        log.info("Get {} document", allDocumentWithNameAndStatusProjectionForUser.getTotalElements());
        return allDocumentWithNameAndStatusProjectionForUser;
    }

    @Override
    public Page<DocumentOutputAllDocumentsDTO> getAllDocumentWhereUserOwnerAfterSigner(int page,
                                                                                       int size,
                                                                                       UUID userId,
                                                                                       DocumentSortingType sortingType) {
        log.info("Get all document for user with sorting- page: {}, size: {}, userId: {}, sortingType: {}",
                page, size, userId, sortingType);
        PageRequest pageable = PageRequest.of(page, size);
        if (!DocumentSortingType.WITHOUT.equals(sortingType)) {
            pageable = pageable.withSort(JpaSort.unsafe(sortingType.getDirection(), sortingType.getFieldName()));
        }
        Page<DocumentOutputAllDocumentsDTO> allDocumentWithNameAndStatusProjectionForUser =
                documentRepository.getAllDocumentWithNameAndStatusProjectionWhereUserOwnerAfterSigner(userId, pageable);

        log.info("Get {} document", allDocumentWithNameAndStatusProjectionForUser.getTotalElements());
        return allDocumentWithNameAndStatusProjectionForUser;
    }

    @Override
    public Page<DocumentOutputAllDocumentsDTO> getAllDocumentWhereUserOwnerBeforeSigner(int page,
                                                                                        int size,
                                                                                        UUID userId,
                                                                                        DocumentSortingType sortingType) {
        log.info("Get all document for user with sorting- page: {}, size: {}, userId: {}, sortingType: {}",
                page, size, userId, sortingType);
        PageRequest pageable = PageRequest.of(page, size);
        if (!DocumentSortingType.WITHOUT.equals(sortingType)) {
            pageable = pageable.withSort(JpaSort.unsafe(sortingType.getDirection(), sortingType.getFieldName()));
        }
        Page<DocumentOutputAllDocumentsDTO> allDocumentWithNameAndStatusProjectionForUser =
                documentRepository.getAllDocumentWithNameAndStatusProjectionWhereUserOwnerBeforeSigner(userId, pageable);

        log.info("Get {} document", allDocumentWithNameAndStatusProjectionForUser.getTotalElements());
        return allDocumentWithNameAndStatusProjectionForUser;
    }

    @Override
    public Page<DocumentOutputAllDocumentsDTO> getAllDocumentWhereUserSignatories(int page,
                                                                                  int size,
                                                                                  UUID userId,
                                                                                  DocumentSortingType sortingType) {
        log.info("Get all document for user with sorting- page: {}, size: {}, userId: {}, sortingType: {}",
                page, size, userId, sortingType);
        PageRequest pageable = PageRequest.of(page, size);
        if (!DocumentSortingType.WITHOUT.equals(sortingType)) {
            pageable = pageable.withSort(JpaSort.unsafe(sortingType.getDirection(), sortingType.getFieldName()));
        }
        Page<DocumentOutputAllDocumentsDTO> allDocumentWithNameAndStatusProjectionWhereUserSignatories =
                documentRepository.getAllDocumentWithNameAndStatusProjectionWhereUserSignatories(userId, pageable);

        log.info("Get {} document", allDocumentWithNameAndStatusProjectionWhereUserSignatories.getTotalElements());
        return allDocumentWithNameAndStatusProjectionWhereUserSignatories;
    }


    @Override
    public Page<DocumentOutputAllDocumentsDTO> getAllDocumentWhereUserSignatoriesBeforeSigner(int page,
                                                                                              int size,
                                                                                              UUID userId,
                                                                                              DocumentSortingType sortingType) {
        log.info("Get all document for user with sorting- page: {}, size: {}, userId: {}, sortingType: {}",
                page, size, userId, sortingType);
        PageRequest pageable = PageRequest.of(page, size);
        if (!DocumentSortingType.WITHOUT.equals(sortingType)) {
            pageable = pageable.withSort(JpaSort.unsafe(sortingType.getDirection(), sortingType.getFieldName()));
        }
        Page<DocumentOutputAllDocumentsDTO> allDocumentWithNameAndStatusProjectionWhereUserSignatories =
                documentRepository.getAllDocumentWithNameAndStatusProjectionWhereUserSignatoriesBeforeSigner(userId, pageable);

        log.info("Get {} document", allDocumentWithNameAndStatusProjectionWhereUserSignatories.getTotalElements());
        return allDocumentWithNameAndStatusProjectionWhereUserSignatories;
    }

    @Override
    public Page<DocumentOutputAllDocumentsDTO> getAllDocumentWhereUserSignatoriesAfterSigner(int page,
                                                                                             int size,
                                                                                             UUID userId,
                                                                                             DocumentSortingType sortingType) {
        log.info("Get all document for user with sorting- page: {}, size: {}, userId: {}, sortingType: {}",
                page, size, userId, sortingType);
        PageRequest pageable = PageRequest.of(page, size);
        if (!DocumentSortingType.WITHOUT.equals(sortingType)) {
            pageable = pageable.withSort(JpaSort.unsafe(sortingType.getDirection(), sortingType.getFieldName()));
        }
        Page<DocumentOutputAllDocumentsDTO> allDocumentWithNameAndStatusProjectionWhereUserSignatories =
                documentRepository.getAllDocumentWithNameAndStatusProjectionWhereUserSignatoriesAfterSigner(userId, pageable);

        log.info("Get {} document", allDocumentWithNameAndStatusProjectionWhereUserSignatories.getTotalElements());
        return allDocumentWithNameAndStatusProjectionWhereUserSignatories;
    }


    @Override
    public DocumentVersion getLastVersionDocumentForUser(long id, UUID userId) {
        log.info("Get document with id: {} for user: {}", id, userId);
        Document document = documentRepository.getDocumentForUser(id, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Document not found"));
        log.info("Get last version document");
        DocumentVersion lastDocumentVersion = document.getDocumentVersion()
                .stream()
                .max(Comparator.comparing(DocumentVersion::getCreatedAt))
                .orElseThrow();

        return lastDocumentVersion;
    }

    @Override
    public DocumentVersion getLastVersionDocumentWhereUserSignatories(long id, UUID userId) {
        log.info("Get document with id: {} for user: {}", id, userId);
        Document document = documentRepository.getDocumentWhereUserSignatories(id, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Document not found"));
        log.info("Get last version document");
        DocumentVersion lastDocumentVersion = document.getDocumentVersion()
                .stream()
                .max(Comparator.comparing(DocumentVersion::getCreatedAt))
                .orElseThrow();

        return lastDocumentVersion;
    }

    @Override
    public Page<DocumentVersion> getAllVersionDocumentForUser(long id,
                                                              UUID userId,
                                                              int page,
                                                              int size) {
        log.info("Get document with id: {} for user: {}", id, userId);
        Document document = documentRepository.getDocumentForUser(id, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Document not found"));

        PageRequest pageable = PageRequest.of(page, size);

        log.info("Get all version document");
        List<DocumentVersion> allDocumentVersion = document.getDocumentVersion();
        allDocumentVersion.sort((dv1, dv2) -> Math.toIntExact(dv2.getId() - dv1.getId()));

        int start = (int) pageable.getOffset();
        int end = Math.min((start + pageable.getPageSize()), allDocumentVersion.size());

        List<DocumentVersion> pageContent = allDocumentVersion.subList(start, end);
        return new PageImpl<>(pageContent, pageable, allDocumentVersion.size());
    }

    @Override
    public Page<DocumentVersion> getAllVersionDocumentWhereUserSignatories(long id,
                                                                           UUID userId,
                                                                           int page,
                                                                           int size) {
        log.info("Get document with id: {} for user: {}", id, userId);
        Document document = documentRepository.getDocumentWhereUserSignatories(id, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Document not found"));

        PageRequest pageable = PageRequest.of(page, size);

        log.info("Get all version document");
        List<DocumentVersion> allDocumentVersion = document.getDocumentVersion();
        allDocumentVersion.sort((dv1, dv2) -> Math.toIntExact(dv2.getId() - dv1.getId()));

        int start = (int) pageable.getOffset();
        int end = Math.min((start + pageable.getPageSize()), allDocumentVersion.size());

        List<DocumentVersion> pageContent = allDocumentVersion.subList(start, end);
        return new PageImpl<>(pageContent, pageable, allDocumentVersion.size());
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
        log.info("Creating document for User: {}", userId);
        newDocument.setUser(
                userRepository.findById(userId)
                        .orElseThrow(() -> new ResourceNotFoundException("User not found"))
        );
        log.info("Save new document with id {}", newDocument.getId());
        Document saved = documentRepository.save(newDocument);

        for (Attribute attribute : saved.getDocumentType().getAttributes()) {
            Optional<AttributeSearch> attributeSearch = attributeSearchRepository.findById(attribute.getId());
            if (attributeSearch.isPresent()) {
                AttributeSearch existingAttributeSearch = attributeSearch.get();
                existingAttributeSearch.getDocuments().add(saved.getId());
                attributeSearchRepository.save(existingAttributeSearch);
                log.info("Save AttributeSearch with id {}", existingAttributeSearch.getId());
            }
        }

        log.info("Save document version document {}", document.getDocumentName());
        DocumentVersion documentVersion = documentVersionService.saveDocumentVersion(document, saved, userId);
        return documentVersion;
    }

    @Transactional
    @Override
    public DocumentVersion updateDocument(long id, DocumentUpdateDTO document, UUID userId) {
        log.info("Updating document with id: {}", id);
        Document existingDocument = documentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Document not found"));

        if (userId != null) {
            existingDocument.setUser(
                    userRepository.findById(userId)
                            .orElseThrow(() -> new ResourceNotFoundException("User not found"))
            );
        }

        existingDocument = documentRepository.save(existingDocument);

        for (Attribute attribute : existingDocument.getDocumentType().getAttributes()) {
            Optional<AttributeSearch> attributeSearch = attributeSearchRepository.findById(attribute.getId());

            if (attributeSearch.isPresent()) {
                AttributeSearch existingAttributeSearch = attributeSearch.get();
                existingAttributeSearch.getDocuments().add(existingDocument.getId());
                attributeSearchRepository.save(existingAttributeSearch);
            }
        }

        DocumentVersion documentVersion = documentVersionService.updateDocumentVersion(document, existingDocument, userId);
        log.info("Document update successfully");
        return documentVersion;
    }

    private void validateDate(DocumentVersion documentVersion) {
        if (documentVersion.getCreatedAt().isAfter(documentVersion.getUpdatedAt())) {
            throw new WrongDateException("The creation date cannot be later than the update date");
        }
    }

    @Transactional
    @Override
    public void deleteDocument(long id, UUID userId) {
        Document document = documentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Document not found"));
        document.setArchived(true);
        DocumentVersion version = document.getDocumentVersion()
                .stream()
                .max(Comparator.comparing(DocumentVersion::getCreatedAt))
                .orElseThrow();
        version.getState().delete(version);

        for (Attribute attribute : document.getDocumentType().getAttributes()) {
            Optional<AttributeSearch> attributeSearch = attributeSearchRepository.findById(attribute.getId());

            if (attributeSearch.isPresent()) {
                AttributeSearch existingAttributeSearch = attributeSearch.get();
                existingAttributeSearch.getDocuments().remove(document.getId());

                attributeSearchRepository.save(existingAttributeSearch);
            }

            documentRepository.save(document);
        }
    }


    @Transactional
    @Override
    public ApprovementProcessItemDTO sendForSign(UUID userId, Long documentId, UserInfoDetails authenticatedUser) {
        User user = getUserOrTempManagerById(userId);
        DocumentVersion documentVersion = getLastVersionDocumentForUser(documentId, authenticatedUser.getId());
        if (!documentVersion.getDocument().getUser().getId().equals(authenticatedUser.getId())) {
            throw new DocumentForbiddenAccess("You don't have access to this document with id = %d".formatted(documentId));
        }
        if (approvementItemRepository.existsByDocumentVersionIdAndUserId(documentVersion.getId(), user.getId())) {
            throw new ApprovementProccessItemAlreadyExistsException("Provided document already sent to user");
        }
        //проверка можно ли такой документ отправить на подпись
        documentVersion.getState().sendForSign(documentVersion);

        ApprovementProcessItem approvementProcessItem = new ApprovementProcessItem();
        approvementProcessItem.setUser(user);
        approvementProcessItem.setDocumentVersion(documentVersion);

        approvementProcessItem.setStatus(documentVersion.getStatus() == DocumentStatus.PENDING_AUTHOR_SIGN ? ApprovementProcessItemStatus.PENDING_AUTHOR_SIGN : ApprovementProcessItemStatus.PENDING_CONTRACTOR_SIGN);
        approvementProcessItem.setCreatedAt(LocalDateTime.now());
        approvementItemRepository.save(approvementProcessItem);
        eventPublisher.publishEvent(new DocumentSignRequestEvent(this, approvementProcessItem));
        return approvementProccessItemMapper.toDTO(approvementProcessItem);
    }

    @Override
    public Page<DocumentOutputAllDocumentsDTO> getArchivedDocuments(int page, int size, UUID userId) {
        return documentRepository.getArchivedDocumentsForUser(userId, PageRequest.of(page, size));
    }

    private User getUserOrTempManagerById(UUID userId) {
        return replacementManagerRepository.findActiveReplacementByManagerUserId(userId)
                .map(ReplacementManager::getTempManagerUser)
                .orElseGet(() -> getUserById(userId));
    }

    private User getUserById(UUID userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id = %s".formatted(userId)));
    }

}
