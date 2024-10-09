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
import ru.caselab.edm.backend.entity.Document;
import ru.caselab.edm.backend.entity.DocumentType;
import ru.caselab.edm.backend.entity.User;
import ru.caselab.edm.backend.exceptions.WrongDateException;
import ru.caselab.edm.backend.repository.DocumentRepository;
import ru.caselab.edm.backend.service.impl.DocumentServiceImpl;

import java.time.LocalDateTime;
import java.util.*;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;


class DocumentServiceTests {

    @Mock
    private DocumentRepository documentRepository;

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
        user.setEmail("email");
        user.setPatronymic("patronymic");
        user.setLogin("login");

        DocumentType documentType = new DocumentType();
        documentType.setId(1L);
        documentType.setName("name");
        documentType.setDescription("description");

        document = new Document();
        document.setId(1L);
        document.setCreationDate(LocalDateTime.now().minusDays(1));
        document.setUpdateDate(LocalDateTime.now());
        document.setName("Test Document");
        document.setUser(user);
        document.setDocumentType(documentType);
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
        assertEquals(document.getName(), foundDocument.getName(), "Document name should match");
        assertEquals(document.getCreationDate(), foundDocument.getCreationDate(), "Creation date should match");
        assertEquals(document.getUpdateDate(), foundDocument.getUpdateDate(), "Update date should match");
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
        when(documentRepository.save(any(Document.class))).thenReturn(document);

        Document foundDocument = documentService.saveDocument(document);

        assertEquals(document.getId(), foundDocument.getId(), "Document ID should match");
        assertEquals(document.getName(), foundDocument.getName(), "Document name should match");
        assertEquals(document.getCreationDate(), foundDocument.getCreationDate(), "Creation date should match");
        assertEquals(document.getUpdateDate(), foundDocument.getUpdateDate(), "Update date should match");
        assertEquals(document.getUser(), foundDocument.getUser(), "User should match");
        assertEquals(document.getDocumentType(), foundDocument.getDocumentType(), "Document type should match");
        verify(documentRepository).save(document);
    }

    @Test
    @DisplayName("Save invalid Document with wrong date")
    void saveDocument_InvalidDate() {
        document.setUpdateDate(LocalDateTime.now().minusDays(2));

        assertThrows(WrongDateException.class, () -> documentService.saveDocument(document));
    }

    @Test
    @DisplayName("Update correct Document")
    void updateDocument_Success() {
        when(documentRepository.findById(1L)).thenReturn(Optional.of(document));
        when(documentRepository.save(any(Document.class))).thenReturn(document);

        LocalDateTime dateTime = LocalDateTime.now().plusDays(2);
        document.setUpdateDate(dateTime);

        Document result = documentService.updateDocument(1L, document);

        assertEquals(document.getUpdateDate(), result.getUpdateDate());
        verify(documentRepository).findById(1L);
        verify(documentRepository).save(any(Document.class));
    }

    @Test
    @DisplayName("Update Document with wrong date")
    void updateDocument_WrongDate() {
        when(documentRepository.findById(1L)).thenReturn(Optional.of(document));

        LocalDateTime invalidDateTime = document.getCreationDate().minusDays(2);
        document.setUpdateDate(invalidDateTime);

        assertThrows(WrongDateException.class, () -> documentService.updateDocument(document.getId(), document));

        verify(documentRepository).findById(1L);
        verify(documentRepository, never()).save(any(Document.class));
    }

    @Test
    @DisplayName("Update Document not found")
    void updateDocument_NotFound() {
        when(documentRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(NoSuchElementException.class, () -> documentService.updateDocument(1L, new Document()));
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