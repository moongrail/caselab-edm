package ru.caselab.edm.backend.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.caselab.edm.backend.dto.attributevalue.AttributeValuetoCreateDocumentDTO;
import ru.caselab.edm.backend.dto.attributevalue.DocumentAttributeValueDTO;
import ru.caselab.edm.backend.dto.attributevalue.DocumentAttributeValueUpdateDTO;
import ru.caselab.edm.backend.entity.Attribute;
import ru.caselab.edm.backend.entity.Document;
import ru.caselab.edm.backend.entity.DocumentAttributeValue;
import ru.caselab.edm.backend.entity.DocumentType;
import ru.caselab.edm.backend.entity.DocumentVersion;
import ru.caselab.edm.backend.exceptions.RequieredAttributeEmptyValueException;
import ru.caselab.edm.backend.exceptions.ResourceNotFoundException;
import ru.caselab.edm.backend.mapper.attributevalue.DocumentAttributeValueMapper;
import ru.caselab.edm.backend.repository.DocumentAttributeValueRepository;
import ru.caselab.edm.backend.service.impl.DocumentAttributeValueServiceImpl;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static ru.caselab.edm.backend.testutils.fixtures.TestFixtures.DOCUMENT_ID;
import static ru.caselab.edm.backend.testutils.fixtures.TestFixtures.OWNER_ID;
import static ru.caselab.edm.backend.testutils.fixtures.TestFixtures.VERSION_ID;
import static ru.caselab.edm.backend.testutils.fixtures.TestFixtures.attribute;
import static ru.caselab.edm.backend.testutils.fixtures.TestFixtures.document;
import static ru.caselab.edm.backend.testutils.fixtures.TestFixtures.documentType;
import static ru.caselab.edm.backend.testutils.fixtures.TestFixtures.user;
import static ru.caselab.edm.backend.testutils.fixtures.TestFixtures.version;

@ExtendWith(MockitoExtension.class)
class DocumentAttributeValueServiceImplTest {

    @Mock
    private DocumentAttributeValueRepository repository;
    @Mock
    private DocumentAttributeValueMapper mapper;
    @Mock
    private DocumentVersionService documentVersionService;

    @InjectMocks
    private DocumentAttributeValueServiceImpl service;

    @Test
    void getByDocumentAndAttribute_found_mapsValue() {
        DocumentAttributeValue value = DocumentAttributeValue.builder().id(1L).value("v").build();
        DocumentAttributeValueDTO expected = new DocumentAttributeValueDTO();
        when(repository.findByDocumentVersionIdAndAttributeId(DOCUMENT_ID, 2L))
                .thenReturn(Optional.of(value));
        when(mapper.toDTO(value)).thenReturn(expected);

        assertThat(service.getDocumentAttributeValueByDocumentAndAttribute(DOCUMENT_ID, 2L))
                .isSameAs(expected);
    }

    @Test
    void getByDocumentAndAttribute_missing_throws() {
        when(repository.findByDocumentVersionIdAndAttributeId(DOCUMENT_ID, 2L))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.getDocumentAttributeValueByDocumentAndAttribute(DOCUMENT_ID, 2L))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Value doesn't exist");
    }

    @Test
    void getById_found_mapsValue() {
        DocumentAttributeValue value = DocumentAttributeValue.builder().id(1L).value("v").build();
        DocumentAttributeValueDTO expected = new DocumentAttributeValueDTO();
        when(repository.findById(1L)).thenReturn(Optional.of(value));
        when(mapper.toDTO(value)).thenReturn(expected);

        assertThat(service.getDocumentAttributeValueById(1L)).isSameAs(expected);
    }

    @Test
    void getById_missing_throws() {
        when(repository.findById(1L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.getDocumentAttributeValueById(1L))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void getByDocumentId_verifiesDocumentAndMapsValues() {
        List<DocumentAttributeValue> values = List.of(DocumentAttributeValue.builder().id(1L).build());
        List<DocumentAttributeValueDTO> expected = List.of(new DocumentAttributeValueDTO());
        when(repository.findByDocumentVersionId(DOCUMENT_ID)).thenReturn(values);
        when(mapper.toDto(values)).thenReturn(expected);

        assertThat(service.getDocumentAttributeValuesByDocumentId(DOCUMENT_ID)).isSameAs(expected);
        verify(documentVersionService).getDocumentVersion(DOCUMENT_ID);
    }

    @Test
    void updateDocumentAttributeValue_replacesProvidedValueAndKeepsMissingValue() {
        Attribute firstAttribute = attribute(1L, false);
        Attribute secondAttribute = attribute(2L, false);
        DocumentVersion oldVersion = version(VERSION_ID, document(DOCUMENT_ID, user(OWNER_ID, "owner"), documentType(1L)), ru.caselab.edm.backend.state.DocumentStatus.DRAFT);
        DocumentVersion newVersion = version(11L, oldVersion.getDocument(), ru.caselab.edm.backend.state.DocumentStatus.DRAFT);
        List<DocumentAttributeValue> oldValues = List.of(
                DocumentAttributeValue.builder().id(1L).attribute(firstAttribute).value("old").build(),
                DocumentAttributeValue.builder().id(2L).attribute(secondAttribute).value("keep").build());
        DocumentAttributeValueUpdateDTO update = new DocumentAttributeValueUpdateDTO();
        update.setAttributeId(1L);
        update.setValue("new");
        when(repository.findByDocumentVersionId(VERSION_ID)).thenReturn(oldValues);
        when(repository.saveAll(any())).thenAnswer(invocation -> invocation.getArgument(0));

        List<DocumentAttributeValue> result = service.updateDocumentAttributeValue(
                List.of(update), oldVersion, newVersion);

        assertThat(result).hasSize(2);
        assertThat(result.get(0).getValue()).isEqualTo("new");
        assertThat(result.get(1).getValue()).isEqualTo("keep");
        assertThat(result).allMatch(value -> value.getDocumentVersion() == newVersion);
        assertThat(newVersion.getDocumentAttributeValue()).isSameAs(result);
    }

    @Test
    void createDocumentAttributeValues_optionalMissing_savesBlankAndProvidedValues() {
        Attribute required = attribute(1L, false);
        Attribute optional = attribute(2L, false);
        DocumentType type = documentType(1L, required, optional);
        Document document = document(DOCUMENT_ID, user(OWNER_ID, "owner"), type);
        DocumentVersion version = version(VERSION_ID, document, ru.caselab.edm.backend.state.DocumentStatus.DRAFT);
        AttributeValuetoCreateDocumentDTO provided = new AttributeValuetoCreateDocumentDTO();
        provided.setAttributeId(1L);
        provided.setValue("provided");
        when(repository.saveAll(any())).thenAnswer(invocation -> invocation.getArgument(0));

        List<DocumentAttributeValue> result = service.createDocumentAttributeValues(
                List.of(provided), document, version);

        assertThat(result).hasSize(2);
        assertThat(result).anyMatch(value -> "provided".equals(value.getValue()));
        assertThat(result).anyMatch(value -> "".equals(value.getValue()));
    }

    @Test
    void createDocumentAttributeValues_requiredMissing_throws() {
        Attribute required = attribute(1L, true);
        DocumentType type = documentType(1L, required);
        Document document = document(DOCUMENT_ID, user(OWNER_ID, "owner"), type);
        DocumentVersion version = version(VERSION_ID, document, ru.caselab.edm.backend.state.DocumentStatus.DRAFT);

        assertThatThrownBy(() -> service.createDocumentAttributeValues(
                List.of(), document, version))
                .isInstanceOf(RequieredAttributeEmptyValueException.class)
                .hasMessageContaining("required");
        verify(repository, never()).saveAll(any());
    }

    @Test
    void createDocumentAttributeValues_blankRequiredValue_throws() {
        Attribute required = attribute(1L, true);
        DocumentType type = documentType(1L, required);
        Document document = document(DOCUMENT_ID, user(OWNER_ID, "owner"), type);
        DocumentVersion version = version(VERSION_ID, document, ru.caselab.edm.backend.state.DocumentStatus.DRAFT);
        AttributeValuetoCreateDocumentDTO provided = new AttributeValuetoCreateDocumentDTO();
        provided.setAttributeId(1L);
        provided.setValue(" ");

        assertThatThrownBy(() -> service.createDocumentAttributeValues(
                List.of(provided), document, version))
                .isInstanceOf(RequieredAttributeEmptyValueException.class);
    }

    @Test
    void deleteAttributeValue_existing_deletes() {
        when(repository.existsById(1L)).thenReturn(true);

        service.deleteAttributeValue(1L);

        verify(repository).deleteById(1L);
    }

    @Test
    void deleteAttributeValue_missing_throws() {
        when(repository.existsById(1L)).thenReturn(false);

        assertThatThrownBy(() -> service.deleteAttributeValue(1L))
                .isInstanceOf(ResourceNotFoundException.class);
        verify(repository, never()).deleteById(any());
    }
}
