package ru.caselab.edm.backend.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.JpaSort;
import ru.caselab.edm.backend.dto.document.DocumentCreateDTO;
import ru.caselab.edm.backend.dto.document.DocumentOutputAllDocumentsDTO;
import ru.caselab.edm.backend.dto.document.DocumentUpdateDTO;
import ru.caselab.edm.backend.dto.file.FileDTO;
import ru.caselab.edm.backend.entity.Document;
import ru.caselab.edm.backend.entity.DocumentType;
import ru.caselab.edm.backend.entity.DocumentVersion;
import ru.caselab.edm.backend.entity.User;
import ru.caselab.edm.backend.enums.DocumentSortingType;
import ru.caselab.edm.backend.exceptions.ResourceNotFoundException;
import ru.caselab.edm.backend.repository.DocumentRepository;
import ru.caselab.edm.backend.repository.DocumentTypeRepository;
import ru.caselab.edm.backend.repository.DocumentVersionRepository;
import ru.caselab.edm.backend.repository.UserRepository;
import ru.caselab.edm.backend.service.impl.DocumentServiceImpl;
import ru.caselab.edm.backend.service.impl.DocumentVersionServiceImpl;
import ru.caselab.edm.backend.state.DocumentBaseState;
import ru.caselab.edm.backend.state.DocumentState;
import ru.caselab.edm.backend.state.DocumentStatus;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.Assert.assertTrue;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;

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
    @Mock
    private DocumentVersionServiceImpl documentVersionService;

    private Document document;
    private DocumentVersion documentVersion;

    private final UUID userId = UUID.fromString("48bbbd31-45c0-43c5-b989-c1c14a8c3b8b");
    private final long documentId = 123L;
    private final int page = 0;
    private final int size = 10;

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
    void getAllDocumentForUserWithoutSorting() {
        PageRequest pageable = PageRequest.of(page, size);

        List<DocumentOutputAllDocumentsDTO> content = List.of(new DocumentOutputAllDocumentsDTO());
        Page<DocumentOutputAllDocumentsDTO> expectedPage = new PageImpl<>(content);

        DocumentSortingType sortingType = DocumentSortingType.WITHOUT;

        when(documentRepository.getAllDocumentWithNameAndStatusProjectionWhereUserOwner(userId, pageable))
                .thenReturn(expectedPage);
        documentService.getAllDocumentForUser(page, size, userId, sortingType);

        verify(documentRepository).getAllDocumentWithNameAndStatusProjectionWhereUserOwner(
                userId,
                pageable);
    }

    @Test
    void getAllDocumentForUserWithSorting() {
        DocumentSortingType sortingType = DocumentSortingType.DOCUMENT_NAME_ASC;

        PageRequest pageable = PageRequest.of(page, size);
        pageable = pageable.withSort(Sort.by(sortingType.getDirection(), sortingType.getFieldName()));

        List<DocumentOutputAllDocumentsDTO> content = List.of(new DocumentOutputAllDocumentsDTO());
        Page<DocumentOutputAllDocumentsDTO> expectedPage = new PageImpl<>(content);

        when(documentRepository.getAllDocumentWithNameAndStatusProjectionWhereUserOwner(userId, pageable))
                .thenReturn(expectedPage);

        documentService.getAllDocumentForUser(page, size, userId, sortingType);

        verify(documentRepository).getAllDocumentWithNameAndStatusProjectionWhereUserOwner(
                userId,
                pageable);
    }

    @Test
    void getLastVersionDocumentForUser_Success() {
        Document document = new Document();
        DocumentVersion version1 = new DocumentVersion();
        version1.setId(1L);
        version1.setDocumentName("version1");
        version1.setCreatedAt(Instant.now());

        DocumentVersion version2 = new DocumentVersion();
        version2.setId(2L);
        version2.setDocumentName("version2");
        version2.setCreatedAt(Instant.now());

        List<DocumentVersion> documentVersionList = new ArrayList<>();
        documentVersionList.add(version1);
        documentVersionList.add(version2);
        document.setDocumentVersion(documentVersionList);

        when(documentRepository.getDocumentForUser(documentId, userId)).thenReturn(Optional.of(document));

        DocumentVersion result = documentService.getLastVersionDocumentForUser(documentId, userId);

        assertEquals(result, version2);
    }

    @Test
    void getLastVersionDocumentForUser_DocumentNotFound() {
        when(documentRepository.getDocumentForUser(documentId, userId)).thenReturn(Optional.empty());

        Exception exception = assertThrows(ResourceNotFoundException.class,
                () -> documentService.getLastVersionDocumentForUser(documentId, userId));

        String expectedMessage = "Document not found";
        String actualMessage = exception.getMessage();

        assertTrue(actualMessage.contains(expectedMessage));
    }

    @Test
    void getAllVersionDocumentForUserSuccess() {
        Document document = new Document();
        DocumentVersion version1 = new DocumentVersion();
        version1.setId(1L);
        version1.setDocumentName("version1");
        version1.setCreatedAt(Instant.now());

        DocumentVersion version2 = new DocumentVersion();
        version2.setId(2L);
        version2.setDocumentName("version2");
        version2.setCreatedAt(Instant.now());

        List<DocumentVersion> documentVersionList = new ArrayList<>();
        documentVersionList.add(version1);
        documentVersionList.add(version2);

        Page<DocumentVersion> documentVersionPage = new PageImpl<>(documentVersionList);

        document.setDocumentVersion(documentVersionList);

        when(documentRepository.getDocumentForUser(documentId, userId)).thenReturn(Optional.of(document));

        Page<DocumentVersion> result = documentService.getAllVersionDocumentForUser(documentId, userId, 0, 5);

        assertEquals(documentVersionPage.getTotalElements(), result.getTotalElements());
    }

    @Test
    void getAllVersionDocumentForUserDocumentNotFound() {
        when(documentRepository.getDocumentForUser(documentId, userId)).thenReturn(Optional.empty());

        ResourceNotFoundException exception = assertThrows(ResourceNotFoundException.class,
                () -> documentService.getAllVersionDocumentForUser(documentId, userId, 0, 5));

        assertEquals("Document not found", exception.getMessage());
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
        FileDTO fileDTO = new FileDTO("Data", "test_object_name");
        documentCreateDTO.setFile(fileDTO);
        documentCreateDTO.setDocumentTypeId(documentTypeId);

        Mockito.when(documentTypeRepository.findById(documentTypeId))
                .thenReturn(Optional.of(documentType1));

        Mockito.when(userRepository.findById(userId)).thenReturn(Optional.of(user1));

        Mockito.when(documentRepository.save(document1)).thenReturn(savedDocument);

        Mockito.when(documentVersionService.saveDocumentVersion(documentCreateDTO,
                savedDocument, userId)).thenReturn(documentVersion1);

        Long result = documentService.saveDocument(documentCreateDTO, userId).getId();

        assertEquals(documentVersion1.getId(), result);
    }

    @Test
    void updateDocumentSuccess() {
        DocumentUpdateDTO documentUpdateDTO = new DocumentUpdateDTO();
        FileDTO fileDTO = new FileDTO("Data", "test_object_name");
        documentUpdateDTO.setFile(fileDTO);

        Document existingDocument = new Document();
        User user = new User();

        DocumentVersion documentVersion = new DocumentVersion();


        when(documentRepository.findById(documentId)).thenReturn(Optional.of(existingDocument));
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));

        when(documentRepository.save(existingDocument)).thenReturn(existingDocument);

        when(documentVersionService.updateDocumentVersion(any(), any(), any())).thenReturn(documentVersion);

        DocumentVersion updatedDocument = documentService.updateDocument(documentId, documentUpdateDTO, userId);

        verify(documentRepository).findById(documentId);
        verify(userRepository).findById(userId);
        verify(documentRepository).save(any());
        verify(documentVersionService).updateDocumentVersion(documentUpdateDTO, existingDocument, userId);

        assertEquals(updatedDocument, documentVersion);
    }

    @Test
    @DisplayName("Delete Document")
    void deleteDocument_Success() {
        long documentId = 1L;
        Document mockDocument = new Document();
        mockDocument.setId(documentId);
        DocumentVersion mockVersion = new DocumentVersion();
        mockVersion.setId(1L);
        mockVersion.setDocument(mockDocument);
        DocumentBaseState mockState = mock(DocumentBaseState.class);
        mockVersion.setState(DocumentStatus.DRAFT);
        mockDocument.setDocumentVersion(List.of(mockVersion));

        when(documentRepository.findById(documentId)).thenReturn(Optional.of(mockDocument));

        when(documentRepository.save(any(Document.class))).thenReturn(mockDocument);

        when(documentVersionService.getDocumentVersion(documentId)).thenReturn(mockVersion);

        doNothing().when(mockState).delete(any(DocumentVersion.class));

        documentService.deleteDocument(documentId);

    }

    @Test
    void testGetAllDocumentWhereUserSignatoriesWithoutSorting() {
        DocumentSortingType sortingType = DocumentSortingType.WITHOUT;

        PageRequest pageable = PageRequest.of(page, size);

        List<DocumentOutputAllDocumentsDTO> content = List.of(new DocumentOutputAllDocumentsDTO());
        Page<DocumentOutputAllDocumentsDTO> expectedPage = new PageImpl<>(content);

        when(documentRepository.getAllDocumentWithNameAndStatusProjectionWhereUserSignatories(userId,
                pageable)).thenReturn(expectedPage);

        Page<DocumentOutputAllDocumentsDTO> result = documentService.getAllDocumentWhereUserSignatories(page, size,
                userId, sortingType);

        verify(documentRepository).getAllDocumentWithNameAndStatusProjectionWhereUserSignatories(
                userId,
                pageable);
        assertEquals(1, result.getTotalElements());
    }

    @Test
    void testGetAllDocumentWhereUserSignatoriesWithSorting() {
        DocumentSortingType sortingType = DocumentSortingType.DOCUMENT_NAME_ASC;

        PageRequest pageable = PageRequest.of(page, size);
        pageable = pageable.withSort(JpaSort.unsafe(sortingType.getDirection(), sortingType.getFieldName()));

        List<DocumentOutputAllDocumentsDTO> content = List.of(new DocumentOutputAllDocumentsDTO());
        Page<DocumentOutputAllDocumentsDTO> expectedPage = new PageImpl<>(content);

        when(documentRepository.getAllDocumentWithNameAndStatusProjectionWhereUserSignatories(userId,
                pageable)).thenReturn(expectedPage);

        Page<DocumentOutputAllDocumentsDTO> result = documentService.getAllDocumentWhereUserSignatories(page, size,
                userId, sortingType);

        verify(documentRepository).getAllDocumentWithNameAndStatusProjectionWhereUserSignatories(
                userId, pageable);

        assertEquals(1, result.getTotalElements());
    }
}