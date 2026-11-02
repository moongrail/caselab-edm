package ru.caselab.edm.backend.document.application;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import ru.caselab.edm.backend.document.api.dto.attributevalue.AttributeValuetoCreateDocumentDTO;
import ru.caselab.edm.backend.document.api.dto.document.DocumentCreateDTO;
import ru.caselab.edm.backend.document.api.dto.document.DocumentUpdateDTO;
import ru.caselab.edm.backend.document.api.dto.file.FileDTO;
import ru.caselab.edm.platform.storage.ObjectStorage;
import ru.caselab.edm.platform.storage.StorageObject;
import ru.caselab.edm.backend.document.domain.Attribute;
import ru.caselab.edm.backend.document.domain.Document;
import ru.caselab.edm.backend.document.domain.DocumentAttributeValue;
import ru.caselab.edm.backend.document.domain.DocumentVersion;
import ru.caselab.edm.backend.shared.exception.ResourceNotFoundException;
import ru.caselab.edm.backend.document.api.mapper.MinioDocumentMapper;
import ru.caselab.edm.backend.document.infrastructure.DocumentVersionRepository;
import ru.caselab.edm.backend.document.application.impl.DocumentVersionServiceImpl;
import ru.caselab.edm.backend.document.domain.state.DocumentStatus;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static ru.caselab.edm.backend.shared.testutils.fixtures.TestFixtures.DOCUMENT_ID;
import static ru.caselab.edm.backend.shared.testutils.fixtures.TestFixtures.OWNER_ID;
import static ru.caselab.edm.backend.shared.testutils.fixtures.TestFixtures.VERSION_ID;
import static ru.caselab.edm.backend.shared.testutils.fixtures.TestFixtures.attribute;
import static ru.caselab.edm.backend.shared.testutils.fixtures.TestFixtures.document;
import static ru.caselab.edm.backend.shared.testutils.fixtures.TestFixtures.documentType;
import static ru.caselab.edm.backend.shared.testutils.fixtures.TestFixtures.version;

@ExtendWith(MockitoExtension.class)
class DocumentVersionServiceImplTest {

    @Mock
    private DocumentVersionRepository repository;
    @Mock
    private ObjectStorage objectStorage;
    @Mock
    private DocumentAttributeValueService attributeValueService;
    @Mock
    private MinioDocumentMapper minioDocumentMapper;

    @InjectMocks
    private DocumentVersionServiceImpl service;

    private Document document;
    private DocumentVersion version;

    @BeforeEach
    void setUp() {
        document = document(DOCUMENT_ID, OWNER_ID, documentType(1L));
        version = version(VERSION_ID, document, DocumentStatus.DRAFT);
    }

    @Test
    void getDocumentVersion_returnsLatestVersion() {
        DocumentVersion older = version(9L, document, DocumentStatus.APPROVED);
        older.setCreatedAt(Instant.parse("2025-01-01T00:00:00Z"));
        version.setCreatedAt(Instant.parse("2026-01-01T00:00:00Z"));
        when(repository.findByDocumentId(DOCUMENT_ID)).thenReturn(List.of(older, version));

        assertThat(service.getDocumentVersion(DOCUMENT_ID)).isSameAs(version);
    }

    @Test
    void getDocumentVersion_withoutVersions_throws() {
        when(repository.findByDocumentId(DOCUMENT_ID)).thenReturn(List.of());

        assertThatThrownBy(() -> service.getDocumentVersion(DOCUMENT_ID))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Document version not found");
    }

    @Test
    void getAllDocumentVersions_byDocument_delegates() {
        when(repository.findByDocumentId(DOCUMENT_ID)).thenReturn(List.of(version));

        assertThat(service.getAllDocumentVersions(DOCUMENT_ID)).containsExactly(version);
    }

    @Test
    void getAllDocumentVersions_byPage_delegates() {
        Page<DocumentVersion> page = new PageImpl<>(List.of(version));
        when(repository.findAll(PageRequest.of(0, 10))).thenReturn(page);

        assertThat(service.getAllDocumentVersions(0, 10)).isSameAs(page);
    }

    @Test
    void saveDocumentVersion_savesFileAndAttributeValues() {
        DocumentCreateDTO request = new DocumentCreateDTO();
        request.setDocumentName("Name");
        request.setFile(new FileDTO("data", "file.pdf"));
        request.setAttributeValues(List.of());
        StorageObject saveDto = new StorageObject("object", new byte[0], "application/pdf");
        List<DocumentAttributeValue> attributeValues = List.of(
                DocumentAttributeValue.builder().attribute(attribute(1L, false)).value("v").build());
        when(minioDocumentMapper.map(any(DocumentCreateDTO.class), eq(OWNER_ID))).thenReturn(saveDto);
        when(attributeValueService.createDocumentAttributeValues(
                eq(request.getAttributeValues()), eq(document), any(DocumentVersion.class)))
                .thenReturn(attributeValues);
        when(repository.save(any(DocumentVersion.class))).thenAnswer(invocation -> invocation.getArgument(0));

        DocumentVersion result = service.saveDocumentVersion(request, document, OWNER_ID);

        assertThat(result.getDocumentName()).isEqualTo("Name");
        assertThat(result.getContentUrl()).isEqualTo("object");
        assertThat(result.getStatus()).isEqualTo(DocumentStatus.DRAFT);
        assertThat(result.getDocument()).isSameAs(document);
        assertThat(result.getDocumentAttributeValue()).isSameAs(attributeValues);
        verify(objectStorage).put(saveDto);
        verify(repository).save(any(DocumentVersion.class));
    }

    @Test
    void updateDocumentVersion_withNewFileAndName_createsNewDraftAndModifiesOldVersion() {
        DocumentUpdateDTO request = new DocumentUpdateDTO();
        request.setDocumentName("Updated");
        request.setFile(new FileDTO("new data", "new.pdf"));
        request.setAttributeValues(List.of());
        StorageObject saveDto = new StorageObject("new-object", new byte[0], "application/pdf");
        List<DocumentAttributeValue> attributeValues = List.of();
        when(minioDocumentMapper.map("new.pdf", "new data", OWNER_ID)).thenReturn(saveDto);
        when(repository.saveAndFlush(any(DocumentVersion.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));
        when(attributeValueService.updateDocumentAttributeValue(
                eq(request.getAttributeValues()), eq(version), any(DocumentVersion.class)))
                .thenReturn(attributeValues);
        when(repository.save(any(DocumentVersion.class))).thenAnswer(invocation -> invocation.getArgument(0));

        DocumentVersion result = service.updateDocumentVersion(request, document, OWNER_ID);

        assertThat(version.getStatus()).isEqualTo(DocumentStatus.REWORK_REQUIRED);
        assertThat(result.getDocumentName()).isEqualTo("Updated");
        assertThat(result.getContentUrl()).isEqualTo("new-object");
        assertThat(result.getStatus()).isEqualTo(DocumentStatus.DRAFT);
        assertThat(result.getDocument()).isSameAs(document);
        assertThat(result.getDocumentAttributeValue()).isSameAs(attributeValues);
        verify(objectStorage).put(saveDto);
        verify(repository).saveAndFlush(result);
    }

    @Test
    void updateDocumentVersion_withoutFileOrName_preservesExistingValues() {
        DocumentUpdateDTO request = new DocumentUpdateDTO();
        request.setAttributeValues(List.of());
        version.setContentUrl("old-object");
        when(repository.saveAndFlush(any(DocumentVersion.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));
        when(attributeValueService.updateDocumentAttributeValue(
                eq(request.getAttributeValues()), eq(version), any(DocumentVersion.class)))
                .thenReturn(List.of());
        when(repository.save(any(DocumentVersion.class))).thenAnswer(invocation -> invocation.getArgument(0));

        DocumentVersion result = service.updateDocumentVersion(request, document, OWNER_ID);

        assertThat(result.getDocumentName()).isEqualTo("Document name");
        assertThat(result.getContentUrl()).isEqualTo("old-object");
        verify(objectStorage, never()).put(any());
    }

    @Test
    void updateDocumentVersion_withoutExistingVersion_throws() {
        Document emptyDocument = document(DOCUMENT_ID, OWNER_ID, documentType(1L));

        assertThatThrownBy(() -> service.updateDocumentVersion(
                new DocumentUpdateDTO(), emptyDocument, OWNER_ID))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Document version not found");
        verify(repository, never()).save(any());
    }
}
