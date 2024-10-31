package ru.caselab.edm.backend.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.caselab.edm.backend.dto.ApprovementProcessItemDTO;
import ru.caselab.edm.backend.dto.DocumentCreateDTO;
import ru.caselab.edm.backend.dto.DocumentUpdateDTO;
import ru.caselab.edm.backend.entity.ApprovementProcessItem;
import ru.caselab.edm.backend.entity.Document;
import ru.caselab.edm.backend.entity.DocumentVersion;
import ru.caselab.edm.backend.entity.User;
import ru.caselab.edm.backend.entity.UserInfoDetails;
import ru.caselab.edm.backend.enums.ApprovementProcessItemStatus;
import ru.caselab.edm.backend.event.DocumentSignRequestEvent;
import ru.caselab.edm.backend.exceptions.ApprovementProccessItemAlreadyExistsException;
import ru.caselab.edm.backend.exceptions.DocumentForbiddenAccess;
import ru.caselab.edm.backend.exceptions.ResourceNotFoundException;
import ru.caselab.edm.backend.exceptions.WrongDateException;
import ru.caselab.edm.backend.mapper.ApprovementProccessItemMapper;
import ru.caselab.edm.backend.repository.ApprovementItemRepository;
import ru.caselab.edm.backend.repository.DocumentRepository;
import ru.caselab.edm.backend.repository.DocumentTypeRepository;
import ru.caselab.edm.backend.repository.DocumentVersionRepository;
import ru.caselab.edm.backend.repository.UserRepository;
import ru.caselab.edm.backend.service.DocumentAttributeValueService;
import ru.caselab.edm.backend.service.DocumentService;
import ru.caselab.edm.backend.service.DocumentVersionService;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class DocumentServiceImpl implements DocumentService {

    private final DocumentRepository documentRepository;
    private final UserRepository userRepository;
    private final DocumentTypeRepository documentTypeRepository;
    private final DocumentVersionRepository documentVersionRepository;
    private final ApplicationEventPublisher eventPublisher;
    private final ApprovementItemRepository approvementItemRepository;
    private final ApprovementProccessItemMapper approvementProccessItemMapper;
    private final DocumentVersionService documentVersionService;
    private final DocumentAttributeValueService documentAttributeValueService;

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
    public Page<Document> getAllDocumentForUser(int page, int size, UUID userId) {
        Pageable pageable = PageRequest.of(page, size);


        return documentRepository.getAllDocumentForUser(userId, pageable);
    }

    @Override
    public DocumentVersion getDocumentForUser(long id, UUID userId) {
        Document document = documentRepository.getDocumentForUser(id, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Document not found"));

        Long documentId = document.getId();
        DocumentVersion documentVersion = documentVersionService.getDocumentVersion(documentId);

        return documentVersion;
    }

    @Transactional
    @Override
    public Document saveDocument(DocumentCreateDTO document, UUID userId) {
        Long documentTypeId = document.getDocumentTypeId();
        Document newDocument = new Document();
        newDocument.setDocumentType(
                documentTypeRepository.findById(documentTypeId)
                        .orElseThrow(() -> new ResourceNotFoundException("Document type not found"))
        );

        newDocument.setUser(
                userRepository.findById(userId)
                        .orElseThrow(() -> new ResourceNotFoundException("User not found"))
        );

        Document saved = documentRepository.save(newDocument);

        documentVersionService.saveDocumentVersion(document, saved, userId);

        return saved;
    }

    @Transactional
    @Override
    public Document updateDocument(long id, DocumentUpdateDTO document, UUID userId) {
        Document existingDocument = documentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Document not found"));

        if (userId != null) {
            existingDocument.setUser(
                    userRepository.findById(userId)
                            .orElseThrow(() -> new ResourceNotFoundException("User not found"))
            );
        }

        existingDocument = documentRepository.save(existingDocument);

        documentVersionService.updateDocumentVersion(document, existingDocument, userId);

        return existingDocument;
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
    public ApprovementProcessItemDTO sendForSign(UUID userId, Long documentVersionId, UserInfoDetails authenticatedUser) {
        Optional<DocumentVersion> documentVersionOptional = documentVersionRepository.findById(documentVersionId);
        if (documentVersionOptional.isEmpty()) {
            throw new ResourceNotFoundException("Document version not found with id = %d".formatted(documentVersionId));
        }
        Optional<User> userOptional = userRepository.findById(userId);
        if (userOptional.isEmpty()) {
            throw new ResourceNotFoundException("User not found with id = %s".formatted(userId));
        }
        User user = userOptional.get();
        DocumentVersion documentVersion = documentVersionOptional.get();
        if (!documentVersion.getDocument().getUser().getId().equals(authenticatedUser.getId())) {
            throw new DocumentForbiddenAccess("You don't have access to this document with id = %d".formatted(documentVersionId));
        }
        if (approvementItemRepository.existsByDocumentVersionIdAndUserId(documentVersion.getId(), user.getId())) {
            throw new ApprovementProccessItemAlreadyExistsException("Provided document already sent to user");
        }
        ApprovementProcessItem approvementProcessItem = new ApprovementProcessItem();
        approvementProcessItem.setUser(user);
        approvementProcessItem.setDocumentVersion(documentVersion);
        approvementProcessItem.setStatus(ApprovementProcessItemStatus.IN_PROGRESS);
        approvementProcessItem.setCreatedAt(LocalDateTime.now());
        approvementItemRepository.save(approvementProcessItem);
        eventPublisher.publishEvent(new DocumentSignRequestEvent(this, approvementProcessItem));
        return approvementProccessItemMapper.toDTO(approvementProcessItem);
    }

/*    private String saveToMinio(MinioSaveDto saveDto) {
        minioService.saveObject(saveDto);
        return saveDto.objectName();
    }*/
}
