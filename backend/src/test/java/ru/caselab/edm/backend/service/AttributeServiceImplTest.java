package ru.caselab.edm.backend.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import ru.caselab.edm.backend.dto.attribute.AttributeCreateDTO;
import ru.caselab.edm.backend.dto.attribute.AttributeDTO;
import ru.caselab.edm.backend.dto.attribute.AttributeUpdateDTO;
import ru.caselab.edm.backend.dto.documentversion.DocumentVersionDtoWithAuthor;
import ru.caselab.edm.backend.entity.Attribute;
import ru.caselab.edm.backend.entity.AttributeSearch;
import ru.caselab.edm.backend.entity.Document;
import ru.caselab.edm.backend.entity.DocumentType;
import ru.caselab.edm.backend.exceptions.AttributeAlreadyExistsException;
import ru.caselab.edm.backend.exceptions.ResourceNotFoundException;
import ru.caselab.edm.backend.mapper.attribute.AttributeMapper;
import ru.caselab.edm.backend.mapper.attribute.AttributeSearchMapper;
import ru.caselab.edm.backend.mapper.documentversion.DocumentVersionMapper;
import ru.caselab.edm.backend.repository.AttributeRepository;
import ru.caselab.edm.backend.repository.DocumentRepository;
import ru.caselab.edm.backend.repository.DocumentTypeRepository;
import ru.caselab.edm.backend.repository.elastic.AttributeSearchRepository;
import ru.caselab.edm.backend.service.impl.AttributeServiceImpl;
import ru.caselab.edm.backend.state.DocumentStatus;

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
import static ru.caselab.edm.backend.testutils.fixtures.TestFixtures.attribute;
import static ru.caselab.edm.backend.testutils.fixtures.TestFixtures.document;
import static ru.caselab.edm.backend.testutils.fixtures.TestFixtures.documentType;
import static ru.caselab.edm.backend.testutils.fixtures.TestFixtures.user;
import static ru.caselab.edm.backend.testutils.fixtures.TestFixtures.version;

@ExtendWith(MockitoExtension.class)
class AttributeServiceImplTest {

    @Mock
    private AttributeRepository attributeRepository;
    @Mock
    private AttributeMapper attributeMapper;
    @Mock
    private AttributeSearchMapper attributeSearchMapper;
    @Mock
    private DocumentTypeRepository documentTypeRepository;
    @Mock
    private AttributeSearchRepository attributeSearchRepository;
    @Mock
    private DocumentRepository documentRepository;
    @Mock
    private DocumentVersionMapper documentVersionMapper;

    @InjectMocks
    private AttributeServiceImpl attributeService;

    private Attribute existingAttribute;
    private AttributeDTO attributeDTO;

    @BeforeEach
    void setUp() {
        existingAttribute = attribute(1L, false);
        attributeDTO = new AttributeDTO();
        attributeDTO.setId(1L);
        attributeDTO.setName("Attribute 1");
        attributeDTO.setDataType("STRING");
    }

    @Test
    void searchByName_delegatesToElasticsearch() {
        List<AttributeSearch> expected = List.of(new AttributeSearch());
        when(attributeSearchRepository.findByName("attr")).thenReturn(expected);

        assertThat(attributeService.searchByName("attr")).isSameAs(expected);
    }

    @Test
    void findByNameWithMinLength_returnsLatestVersionsOfFoundDocuments() {
        AttributeSearch search = AttributeSearch.builder()
                .id(1L)
                .name("attr")
                .documents(new ArrayList<>(List.of(DOCUMENT_ID, 2L)))
                .build();
        Document document = document(DOCUMENT_ID, user(OWNER_ID, "owner"), documentType(1L));
        version(10L, document, DocumentStatus.DRAFT);
        version(11L, document, DocumentStatus.APPROVED);
        DocumentVersionDtoWithAuthor dto = new DocumentVersionDtoWithAuthor();
        dto.setId(11L);
        when(attributeSearchRepository.findByNameWithMinLength("attr")).thenReturn(List.of(search));
        when(documentRepository.findById(DOCUMENT_ID)).thenReturn(Optional.of(document));
        when(documentRepository.findById(2L)).thenReturn(Optional.empty());
        when(documentVersionMapper.toDtoWithAuthor(document.getDocumentVersion().get(1))).thenReturn(dto);

        Set<DocumentVersionDtoWithAuthor> result = attributeService.findByNameWithMinLength("attr");

        assertThat(result).containsExactly(dto);
    }

    @Test
    void findByNameWithMinLength_noMatches_returnsEmptySet() {
        when(attributeSearchRepository.findByNameWithMinLength("missing")).thenReturn(List.of());

        assertThat(attributeService.findByNameWithMinLength("missing")).isEmpty();
    }

    @Test
    void createAttribute_withoutDocumentTypes_savesAttributeAndSearchDocument() {
        AttributeCreateDTO request = createRequest(Set.of());
        when(attributeRepository.findByName("New attribute")).thenReturn(Optional.empty());
        when(attributeRepository.save(any(Attribute.class))).thenAnswer(invocation -> {
            Attribute saved = invocation.getArgument(0);
            saved.setId(2L);
            return saved;
        });
        AttributeSearch search = AttributeSearch.builder().id(2L).documents(new ArrayList<>()).build();
        when(attributeSearchMapper.toDTO(any(Attribute.class))).thenReturn(search);
        AttributeDTO expected = new AttributeDTO();
        when(attributeMapper.toDTO(any(Attribute.class))).thenReturn(expected);

        AttributeDTO result = attributeService.createAttribute(request);

        assertThat(result).isSameAs(expected);
        verify(attributeSearchRepository).save(search);
        verify(attributeRepository).save(any(Attribute.class));
    }

    @Test
    void createAttribute_withDocumentTypes_mapsEntities() {
        DocumentType type = documentType(5L);
        AttributeCreateDTO request = createRequest(Set.of(5L));
        when(attributeRepository.findByName("New attribute")).thenReturn(Optional.empty());
        when(documentTypeRepository.findById(5L)).thenReturn(Optional.of(type));
        when(attributeRepository.save(any(Attribute.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(attributeSearchMapper.toDTO(any(Attribute.class)))
                .thenReturn(AttributeSearch.builder().documents(new ArrayList<>()).build());
        when(attributeMapper.toDTO(any(Attribute.class))).thenReturn(new AttributeDTO());

        attributeService.createAttribute(request);

        org.mockito.ArgumentCaptor<Attribute> captor = org.mockito.ArgumentCaptor.forClass(Attribute.class);
        verify(attributeRepository).save(captor.capture());
        assertThat(captor.getValue().getDocumentTypes()).containsExactly(type);
    }

    @Test
    void createAttribute_existingName_throws() {
        AttributeCreateDTO request = createRequest(Set.of());
        when(attributeRepository.findByName("New attribute")).thenReturn(Optional.of(existingAttribute));

        assertThatThrownBy(() -> attributeService.createAttribute(request))
                .isInstanceOf(AttributeAlreadyExistsException.class);
        verify(attributeRepository, never()).save(any());
    }

    @Test
    void createAttribute_missingDocumentType_throws() {
        AttributeCreateDTO request = createRequest(Set.of(99L));
        when(attributeRepository.findByName("New attribute")).thenReturn(Optional.empty());
        when(documentTypeRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> attributeService.createAttribute(request))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("DocumentType not found");
    }

    @Test
    void getAttributeEntityById_found_returnsEntity() {
        when(attributeRepository.findById(1L)).thenReturn(Optional.of(existingAttribute));

        assertThat(attributeService.getAttributeEntityById(1L)).isSameAs(existingAttribute);
    }

    @Test
    void getAttributeEntityById_missing_throws() {
        when(attributeRepository.findById(1L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> attributeService.getAttributeEntityById(1L))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("1");
    }

    @Test
    void getAttributeById_mapsEntity() {
        when(attributeRepository.findById(1L)).thenReturn(Optional.of(existingAttribute));
        when(attributeMapper.toDTO(existingAttribute)).thenReturn(attributeDTO);

        assertThat(attributeService.getAttributeById(1L)).isSameAs(attributeDTO);
    }

    @Test
    void getAllAttributes_mapsPage() {
        Page<Attribute> page = new PageImpl<>(List.of(existingAttribute));
        when(attributeRepository.findAll(PageRequest.of(0, 10))).thenReturn(page);
        when(attributeMapper.toDTO(existingAttribute)).thenReturn(attributeDTO);

        Page<AttributeDTO> result = attributeService.getAllAttributes(0, 10);

        assertThat(result.getContent()).containsExactly(attributeDTO);
    }

    @Test
    void updateAttribute_updatesFieldsAndRefreshesSearchIndex() {
        AttributeUpdateDTO request = new AttributeUpdateDTO();
        request.setName("Updated");
        request.setDataType("NUMBER");
        request.setRequired(true);
        request.setDocumentTypeIds(Set.of(5L));
        DocumentType type = documentType(5L);
        AttributeSearch search = AttributeSearch.builder().documents(new ArrayList<>()).build();
        when(attributeRepository.findById(1L)).thenReturn(Optional.of(existingAttribute));
        when(attributeRepository.findByName("Updated")).thenReturn(Optional.empty());
        when(documentTypeRepository.findById(5L)).thenReturn(Optional.of(type));
        when(attributeSearchMapper.toDTO(existingAttribute)).thenReturn(search);
        when(documentRepository.getDocumentsWithAttribute(1L)).thenReturn(List.of(DOCUMENT_ID));
        when(attributeMapper.toDTO(existingAttribute)).thenReturn(attributeDTO);

        AttributeDTO result = attributeService.updateAttribute(1L, request);

        assertThat(result).isSameAs(attributeDTO);
        assertThat(existingAttribute.getName()).isEqualTo("Updated");
        assertThat(existingAttribute.getDataType()).isEqualTo("NUMBER");
        assertThat(existingAttribute.isRequired()).isTrue();
        assertThat(existingAttribute.getDocumentTypes()).containsExactly(type);
        assertThat(search.getDocuments()).containsExactly(DOCUMENT_ID);
        verify(attributeSearchRepository).save(search);
    }

    @Test
    void updateAttribute_onlyDataTypeAndRequiredChanges() {
        AttributeUpdateDTO request = new AttributeUpdateDTO();
        request.setName(" ");
        request.setDataType("BOOLEAN");
        request.setRequired(false);
        when(attributeRepository.findById(1L)).thenReturn(Optional.of(existingAttribute));
        AttributeSearch search = AttributeSearch.builder().documents(new ArrayList<>()).build();
        when(attributeSearchMapper.toDTO(existingAttribute)).thenReturn(search);
        when(documentRepository.getDocumentsWithAttribute(1L)).thenReturn(List.of());
        when(attributeMapper.toDTO(existingAttribute)).thenReturn(attributeDTO);

        attributeService.updateAttribute(1L, request);

        assertThat(existingAttribute.getName()).isEqualTo("Attribute 1");
        assertThat(existingAttribute.getDataType()).isEqualTo("BOOLEAN");
        assertThat(existingAttribute.isRequired()).isFalse();
        verify(attributeRepository, never()).findByName(any());
    }

    @Test
    void updateAttribute_sameName_doesNotCheckDuplicates() {
        AttributeUpdateDTO request = new AttributeUpdateDTO();
        request.setName("Attribute 1");
        when(attributeRepository.findById(1L)).thenReturn(Optional.of(existingAttribute));
        AttributeSearch search = AttributeSearch.builder().documents(new ArrayList<>()).build();
        when(attributeSearchMapper.toDTO(existingAttribute)).thenReturn(search);
        when(documentRepository.getDocumentsWithAttribute(1L)).thenReturn(List.of());
        when(attributeMapper.toDTO(existingAttribute)).thenReturn(attributeDTO);

        attributeService.updateAttribute(1L, request);

        verify(attributeRepository, never()).findByName(any());
    }

    @Test
    void updateAttribute_duplicateName_throws() {
        AttributeUpdateDTO request = new AttributeUpdateDTO();
        request.setName("Duplicate");
        when(attributeRepository.findById(1L)).thenReturn(Optional.of(existingAttribute));
        when(attributeRepository.findByName("Duplicate")).thenReturn(Optional.of(attribute(2L, false)));

        assertThatThrownBy(() -> attributeService.updateAttribute(1L, request))
                .isInstanceOf(AttributeAlreadyExistsException.class);
        verify(attributeRepository, never()).save(any());
    }

    @Test
    void updateAttribute_missingAttribute_throws() {
        when(attributeRepository.findById(1L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> attributeService.updateAttribute(1L, new AttributeUpdateDTO()))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Attribute not found");
    }

    @Test
    void updateAttribute_missingDocumentType_throws() {
        AttributeUpdateDTO request = new AttributeUpdateDTO();
        request.setDocumentTypeIds(Set.of(99L));
        when(attributeRepository.findById(1L)).thenReturn(Optional.of(existingAttribute));
        when(documentTypeRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> attributeService.updateAttribute(1L, request))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void deleteAttribute_existing_deletesEntityAndSearchDocument() {
        AttributeSearch search = AttributeSearch.builder().documents(new ArrayList<>()).build();
        when(attributeRepository.findById(1L)).thenReturn(Optional.of(existingAttribute));
        when(attributeSearchMapper.toDTO(existingAttribute)).thenReturn(search);
        when(documentRepository.getDocumentsWithAttribute(1L)).thenReturn(List.of(DOCUMENT_ID));

        attributeService.deleteAttribute(1L);

        assertThat(search.getDocuments()).containsExactly(DOCUMENT_ID);
        verify(attributeRepository).delete(existingAttribute);
        verify(attributeSearchRepository).delete(search);
    }

    @Test
    void deleteAttribute_missing_throws() {
        when(attributeRepository.findById(1L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> attributeService.deleteAttribute(1L))
                .isInstanceOf(ResourceNotFoundException.class);
        verify(attributeRepository, never()).delete(any());
    }

    private AttributeCreateDTO createRequest(Set<Long> documentTypeIds) {
        AttributeCreateDTO request = new AttributeCreateDTO();
        request.setName("New attribute");
        request.setDataType("STRING");
        request.setRequired(true);
        request.setDocumentTypeIds(documentTypeIds == null ? null : new HashSet<>(documentTypeIds));
        return request;
    }
}
