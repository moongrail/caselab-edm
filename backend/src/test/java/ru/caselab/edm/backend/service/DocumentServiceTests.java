package ru.caselab.edm.backend.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import ru.caselab.edm.backend.dto.DocumentCreateDTO;
import ru.caselab.edm.backend.dto.DocumentUpdateDTO;
import ru.caselab.edm.backend.entity.Document;
import ru.caselab.edm.backend.entity.DocumentType;
import ru.caselab.edm.backend.entity.DocumentVersion;
import ru.caselab.edm.backend.entity.User;
import ru.caselab.edm.backend.repository.DocumentRepository;
import ru.caselab.edm.backend.repository.DocumentTypeRepository;
import ru.caselab.edm.backend.repository.DocumentVersionRepository;
import ru.caselab.edm.backend.repository.UserRepository;
import ru.caselab.edm.backend.service.impl.DocumentServiceImpl;

import java.time.Instant;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class DocumentServiceTests {

    @Mock
    private DocumentRepository documentRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private DocumentTypeRepository documentTypeRepository;
    @Mock
    private DocumentVersionRepository documentVersionRepository;

    @InjectMocks
    private DocumentServiceImpl documentService;

    private Document document;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);

        User user = new User();
        user.setId(UUID.randomUUID());
        user.setPassword("password");
        user.setFirstName("firstName");
        user.setLastName("lastName");
        user.setEmail("email@example.com");
        user.setPatronymic("patronymic");
        user.setLogin("login");

        DocumentType documentType = new DocumentType();
        documentType.setId(1L);
        documentType.setName("name");
        documentType.setDescription("description");

        document = new Document();
        document.setId(100L);
        document.setUser(user);
        document.setDocumentType(documentType);

        DocumentVersion documentVersion = new DocumentVersion();
        documentVersion.setId(1L);
        documentVersion.setDocumentName("New Document");
        documentVersion.setDocument(document);
        documentVersion.setCreatedAt(Instant.now());
        documentVersion.setUpdatedAt(Instant.now());
        documentVersion.setContentUrl("ContentUrl");

        when(documentVersionRepository.save(any(DocumentVersion.class))).thenReturn(documentVersion);
        when(userRepository.findById(any(UUID.class))).thenReturn(Optional.of(user));
        when(documentTypeRepository.findById(anyLong())).thenReturn(Optional.of(documentType));

    }

    @Test
    @DisplayName("Get All Documents")
    void getAllDocuments() {
        Page<Document> expectedPage = new PageImpl<>(Collections.singletonList(document));
        when(documentRepository.findAll(PageRequest.of(0, 10))).thenReturn(expectedPage);

        Page<Document> actualPage = documentService.getAllDocuments(0, 10);

        assertEquals(expectedPage.getTotalElements(), actualPage.getTotalElements());
        assertEquals(expectedPage.getContent(), actualPage.getContent());
        verify(documentRepository).findAll(PageRequest.of(0, 10));
    }

    @Test
    @DisplayName("Get Document success")
    void getDocument_Success() {
        when(documentRepository.findById(1L)).thenReturn(Optional.of(document));
        Document foundDocument = documentService.getDocument(1L);

        assertEquals(document.getId(), foundDocument.getId(), "Document ID should match");
        assertEquals(document.getUser(), foundDocument.getUser(), "User should match");
        assertEquals(document.getDocumentType(), foundDocument.getDocumentType(), "Document type should match");

        verify(documentRepository).findById(1L);
    }

    @Test
    @DisplayName("Document not found")
    void getDocument_NotFound() {
        when(documentRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(NoSuchElementException.class, () -> documentService.getDocument(1L));

        verify(documentRepository).findById(1L);
    }

    @Test
    @DisplayName("Save correct Document")
    void saveDocument_Success() {
        DocumentCreateDTO documentCreateDTO = new DocumentCreateDTO();
        documentCreateDTO.setName("New Document");
        documentCreateDTO.setUserId(UUID.randomUUID());
        documentCreateDTO.setDocumentTypeId(1L);

        when(userRepository.findById(any())).thenReturn(Optional.of(document.getUser()));
        when(documentTypeRepository.findById(any())).thenReturn(Optional.of(document.getDocumentType()));

        when(documentRepository.save(any(Document.class))).thenReturn(document);

        Document savedDocument = documentService.saveDocument(documentCreateDTO).getDocument();

        assertEquals(document.getId(), savedDocument.getId(), "Saved Document ID should match");

        verify(documentRepository).save(any(Document.class));
    }


    @Test
    @DisplayName("Update correct Document")
    void updateDocument_Success() {
        when(documentRepository.findById(1L)).thenReturn(Optional.of(document));

        DocumentUpdateDTO updateDTO = new DocumentUpdateDTO();
        updateDTO.setCreationDate(LocalDateTime.now().minusDays(1));
        updateDTO.setUserId(UUID.randomUUID());
        updateDTO.setDocumentTypeId(1L);

        when(documentVersionRepository.findById(any())).thenReturn(Optional.of(DocumentVersion.builder()
                .id(1L)
                .documentName("name")
                .build()));
        when(userRepository.findById(any())).thenReturn(Optional.of(document.getUser()));
        when(documentTypeRepository.findById(any())).thenReturn(Optional.of(document.getDocumentType()));

        when(documentRepository.save(any(Document.class))).thenReturn(document);

        Document updatedDoc = documentService.updateDocument(1L, updateDTO).getDocument();


        verify(documentRepository).findById(1L);
        verify(documentRepository).save(any(Document.class));
    }

    @Test
    @DisplayName("Update Document not found")
    void updateDocument_NotFound() {
        when(documentRepository.findById(1L)).thenReturn(Optional.empty());


        assertThrows(NoSuchElementException.class, () ->
                documentService.updateDocument(1L, new DocumentUpdateDTO())
        );

        verify(documentRepository).findById(1L);
    }

    @Test
    @DisplayName("Delete Document")
    void deleteDocument_Success() {
        doNothing().when(documentRepository).deleteById(1L);

        documentService.deleteDocument(1L);

        verify(documentRepository).deleteById(1L);
    }
}