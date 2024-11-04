package ru.caselab.edm.backend.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.caselab.edm.backend.dto.approvementprocessitem.ApprovementProcessItemDTO;
import ru.caselab.edm.backend.dto.document.DocumentCreateDTO;
import ru.caselab.edm.backend.dto.document.DocumentOutputAllDocumentsDTO;
import ru.caselab.edm.backend.dto.document.DocumentUpdateDTO;
import ru.caselab.edm.backend.entity.ApprovementProcessItem;
import ru.caselab.edm.backend.entity.Document;
import ru.caselab.edm.backend.entity.DocumentVersion;
import ru.caselab.edm.backend.entity.User;
import ru.caselab.edm.backend.entity.UserInfoDetails;
import ru.caselab.edm.backend.enums.ApprovementProcessItemStatus;
import ru.caselab.edm.backend.enums.DocumentSortingType;
import ru.caselab.edm.backend.event.DocumentSignRequestEvent;
import ru.caselab.edm.backend.exceptions.ApprovementProccessItemAlreadyExistsException;
import ru.caselab.edm.backend.exceptions.DocumentForbiddenAccess;
import ru.caselab.edm.backend.exceptions.ResourceNotFoundException;
import ru.caselab.edm.backend.exceptions.WrongDateException;
import ru.caselab.edm.backend.mapper.approvementprocessitem.ApprovementProccessItemMapper;
import ru.caselab.edm.backend.repository.ApprovementItemRepository;
import ru.caselab.edm.backend.repository.DocumentRepository;
import ru.caselab.edm.backend.repository.DocumentTypeRepository;
import ru.caselab.edm.backend.repository.DocumentVersionRepository;
import ru.caselab.edm.backend.repository.UserRepository;
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
    private final DocumentVersionRepository documentVersionRepository;
    private final ApplicationEventPublisher eventPublisher;
    private final ApprovementItemRepository approvementItemRepository;
    private final ApprovementProccessItemMapper approvementProccessItemMapper;
    private final DocumentVersionService documentVersionService;

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
            pageable = pageable.withSort(Sort.by(sortingType.getDirection(), sortingType.getFieldName()));
        }
        Page<DocumentOutputAllDocumentsDTO> allDocumentWithNameAndStatusProjectionForUser =
                documentRepository.getAllDocumentWithNameAndStatusProjectionForUser(userId, pageable);

        log.info("Get {} document", allDocumentWithNameAndStatusProjectionForUser.getTotalElements());
        return allDocumentWithNameAndStatusProjectionForUser;
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
    public List<DocumentVersion> getAllVersionDocumentForUser(long id, UUID userId) {
        log.info("Get document with id: {} for user: {}", id, userId);
        Document document = documentRepository.getDocumentForUser(id, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Document not found"));
        log.info("Get all version document");
        List<DocumentVersion> allDocumentVersion = document.getDocumentVersion();

        return allDocumentVersion;
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
    public void deleteDocument(long id) {
        documentRepository.deleteById(id);
    }

    @Transactional
    @Override
    public ApprovementProcessItemDTO sendForSign(UUID userId, Long documentId, UserInfoDetails authenticatedUser) {
        Optional<User> userOptional = userRepository.findById(userId);
        if (userOptional.isEmpty()) {
            throw new ResourceNotFoundException("User not found with id = %s".formatted(userId));
        }
        User user = userOptional.get();

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
}
