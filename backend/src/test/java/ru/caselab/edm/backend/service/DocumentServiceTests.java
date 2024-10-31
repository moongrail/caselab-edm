package ru.caselab.edm.backend.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import ru.caselab.edm.backend.dto.DocumentCreateDTO;
import ru.caselab.edm.backend.entity.Document;
import ru.caselab.edm.backend.entity.DocumentType;
import ru.caselab.edm.backend.entity.DocumentVersion;
import ru.caselab.edm.backend.entity.User;
import ru.caselab.edm.backend.exceptions.ResourceNotFoundException;
import ru.caselab.edm.backend.repository.DocumentRepository;
import ru.caselab.edm.backend.repository.DocumentTypeRepository;
import ru.caselab.edm.backend.repository.DocumentVersionRepository;
import ru.caselab.edm.backend.repository.UserRepository;
import ru.caselab.edm.backend.service.impl.DocumentServiceImpl;
import ru.caselab.edm.backend.service.impl.DocumentVersionServiceImpl;

import java.time.Instant;
import java.util.Collections;
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

/*    @Mock
    private MinioDocumentMapper minioDocumentMapper;
    @Mock
    private MinioService minioService;*/

    @InjectMocks
    private DocumentServiceImpl documentService;
    @Mock
    private DocumentVersionServiceImpl documentVersionService;

    private Document document;
    private DocumentVersion documentVersion;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);

        User user = new User();
        user.setId(UUID.fromString("48bbbd31-45c0-43c5-b989-c1c14a8c3b8b"));
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

        documentVersion = new DocumentVersion();
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
    @DisplayName("Get All Documents for User")
    void getAllDocumentForUser() {
        Page<Document> expectedPage = new PageImpl<>(Collections.singletonList(document));

        Mockito.when(documentRepository.getAllDocumentForUser(
                        UUID.fromString("48bbbd31-45c0-43c5-b989-c1c14a8c3b8b"),
                        PageRequest.of(0, 10)))
                .thenReturn(expectedPage);

        Page<Document> actualPage = documentService.getAllDocumentForUser(0,
                10,
                UUID.fromString("48bbbd31-45c0-43c5-b989-c1c14a8c3b8b"));

        assertEquals(expectedPage.getTotalElements(), actualPage.getTotalElements());
        assertEquals(expectedPage.getContent(), actualPage.getContent());
        verify(documentRepository).getAllDocumentForUser(
                UUID.fromString("48bbbd31-45c0-43c5-b989-c1c14a8c3b8b"),
                PageRequest.of(0, 10));
    }

    @Test
    @DisplayName("Get Document For User Success")
    void getDocumentForUserSuccess() {
        Mockito.when(documentRepository.getDocumentForUser(
                1L,
                UUID.fromString("48bbbd31-45c0-43c5-b989-c1c14a8c3b8b"))
        ).thenReturn(Optional.of(document));

        assertEquals(document.getId(), documentService.getDocumentForUser(
                1L,
                UUID.fromString("48bbbd31-45c0-43c5-b989-c1c14a8c3b8b")).getId());
        assertEquals(document.getUser(), documentService.getDocumentForUser(
                1L,
                UUID.fromString("48bbbd31-45c0-43c5-b989-c1c14a8c3b8b")).getUser());
        assertEquals(document.getDocumentType(), documentService.getDocumentForUser(
                1L,
                UUID.fromString("48bbbd31-45c0-43c5-b989-c1c14a8c3b8b")).getDocumentType());
    }

    @Test
    @DisplayName("Document for User not found")
    void getDocumentForUser_NotFound() {
        Mockito.when(documentRepository.getDocumentForUser(
                        1L,
                        UUID.fromString("48bbbd31-45c0-43c5-b989-c1c14a8c3b8b")))
                .thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> documentService.getDocumentForUser(
                1L,
                UUID.fromString("48bbbd31-45c0-43c5-b989-c1c14a8c3b8b")));

        verify(documentRepository).getDocumentForUser(
                1L,
                UUID.fromString("48bbbd31-45c0-43c5-b989-c1c14a8c3b8b"));
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

        assertThrows(ResourceNotFoundException.class, () -> documentService.getDocument(1L));

        verify(documentRepository).findById(1L);
    }

    @Test
    @DisplayName("Save correct Document")
    void saveDocument_Success() {
        User user1 = new User();

        UUID userId = UUID.fromString("48bbbd31-45c0-43c5-b989-c1c14a8c3b8b");

        DocumentType documentType1 = new DocumentType();
        Long documentTypeId = 1L;

        Document document1 = new Document();
        document1.setUser(user1);
        document1.setDocumentType(documentType1);

        Document savedDocument = new Document();
        savedDocument.setId(1L);
        savedDocument.setUser(user1);
        savedDocument.setDocumentType(documentType1);

        DocumentVersion documentVersion1 = new DocumentVersion();

        DocumentCreateDTO documentCreateDTO = new DocumentCreateDTO();
        documentCreateDTO.setDocumentTypeId(documentTypeId);

        Mockito.when(documentTypeRepository.findById(documentTypeId))
                .thenReturn(Optional.of(documentType1));

        Mockito.when(userRepository.findById(userId)).thenReturn(Optional.of(user1));
        // when(minioDocumentMapper.map(documentCreateDTO, userId)).thenReturn(new MinioSaveDto("test", new byte[0]));

        Mockito.when(documentRepository.save(document1)).thenReturn(savedDocument);

        Mockito.when(documentVersionService.saveDocumentVersion(documentCreateDTO,
                savedDocument, userId)).thenReturn(documentVersion1);

        Document saveDocument = documentService.saveDocument(documentCreateDTO, userId);

        assertEquals(saveDocument.getId(), 1L, "Saved Document ID should match");



    }

  /*  @Test
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

        Document updatedDoc = documentService.updateDocument(1L, updateDTO, user.getId()).getDocument();


        verify(documentRepository).findById(1L);
        verify(documentRepository).save(any(Document.class));
    }*/
/*
    @Test
    @DisplayName("Update Document not found")
    void updateDocument_NotFound() {
        when(documentRepository.findById(1L)).thenReturn(Optional.empty());


        assertThrows(ResourceNotFoundException.class, () ->
                documentService.updateDocument(1L, new DocumentUpdateDTO(), user.getId())
        );

        verify(documentRepository).findById(1L);
    }*/

    @Test
    @DisplayName("Delete Document")
    void deleteDocument_Success() {
        doNothing().when(documentRepository).deleteById(1L);

        documentService.deleteDocument(1L);

        verify(documentRepository).deleteById(1L);
    }
}