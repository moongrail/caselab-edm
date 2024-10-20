package ru.caselab.edm.backend.service.Impl;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import ru.caselab.edm.backend.dto.AttributeDTO;
import ru.caselab.edm.backend.dto.DocumentTypeCreateDTO;
import ru.caselab.edm.backend.dto.DocumentTypeDTO;
import ru.caselab.edm.backend.dto.DocumentTypeUpdateDTO;
import ru.caselab.edm.backend.entity.Attribute;
import ru.caselab.edm.backend.entity.DocumentType;
import ru.caselab.edm.backend.mapper.DocumentTypeMapper;
import ru.caselab.edm.backend.repository.AttributeRepository;
import ru.caselab.edm.backend.repository.DocumentTypeRepository;
import ru.caselab.edm.backend.service.impl.DocumentTypeImpl;

import java.time.LocalDateTime;
import java.time.Month;
import java.util.*;

@ExtendWith(MockitoExtension.class)
class DocumentTypeImplTest {
    public static final LocalDateTime NOW = LocalDateTime
            .of(2024, Month.FEBRUARY, 22, 9, 49, 19, 275039200);
    @Mock
    private DocumentTypeMapper mapper;
    @Mock
    private DocumentTypeRepository documentTypeRepository;
    @Mock
    private AttributeRepository attributesRepository;
    @InjectMocks
    private DocumentTypeImpl documentTypeService;

    private static DocumentType getDocumentType() {

        Attribute attribute = new Attribute();
        attribute.setName("подписант");
        attribute.setDataType("текст");

        List<Attribute> documentAttributeList = new ArrayList<>();
        documentAttributeList.add(attribute);

        DocumentType documentType = new DocumentType();
        documentType.setId(1L);
        documentType.setName("договор");
        documentType.setDescription("какоей-то описание");
        return documentType;
    }

    private static AttributeDTO getDocumentsAttributesDTO(Long attributesId,
                                                          String attributesName) {
        AttributeDTO testAttributeDTO = new AttributeDTO();
        testAttributeDTO.setId(attributesId);
        testAttributeDTO.setName(attributesName);
        return testAttributeDTO;
    }


    @Test
    void createDocumentType() {

        AttributeDTO testAttributeDto1 = getDocumentsAttributesDTO(0L, "подписант");

        DocumentTypeCreateDTO testDto = new DocumentTypeCreateDTO();
        testDto.setName("договор");
        testDto.setDescription("какое-то описание");

        Set<Long> documentsAttributesDTOSet = new HashSet<>();
        documentsAttributesDTOSet.add(testAttributeDto1.getId());
        testDto.setAttributeIds(documentsAttributesDTOSet);

        DocumentTypeDTO expectedDocumentTypeDTO = getDocumentTypeDTO(
                documentsAttributesDTOSet,
                1L, "договор", "какое-то описание"
        );

        Mockito.when(attributesRepository.findAllById(documentsAttributesDTOSet))
                .thenReturn(new ArrayList<>(List.of(new Attribute() {{
                    setId(0L);
                    setName("подписант");
                }})));

        Mockito.when(mapper.toDto(Mockito.any(DocumentType.class))).thenReturn(expectedDocumentTypeDTO);
        DocumentTypeDTO result = documentTypeService.createDocumentType(testDto);

        Assertions.assertThat(result).isEqualTo(expectedDocumentTypeDTO);

        ArgumentCaptor<DocumentType> documentTypeCaptor = ArgumentCaptor.forClass(DocumentType.class);
        Mockito.verify(documentTypeRepository).save(documentTypeCaptor.capture());

        DocumentType savedDocumentType = documentTypeCaptor.getValue();
        Assertions.assertThat(savedDocumentType.getName()).isEqualTo("договор");
        Assertions.assertThat(savedDocumentType.getDescription()).isEqualTo("какое-то описание");
        Assertions.assertThat(savedDocumentType.getAttributes()).hasSize(1);
    }

    @Test
    void updateDocumentType() {
        // Входные данные
        DocumentTypeUpdateDTO documentTypeUpdateDTO = new DocumentTypeUpdateDTO();
        documentTypeUpdateDTO.setAttributeIds(new HashSet<>(List.of(0L)));
        documentTypeUpdateDTO.setName("Новый документ");
        documentTypeUpdateDTO.setDescription("Такого вы еще не видели");

        AttributeDTO testAttributeDto1 = getDocumentsAttributesDTO(0L, "подписант");
        Set<Long> documentsAttributesDTOSet1 = new HashSet<>();
        documentsAttributesDTOSet1.add(testAttributeDto1.getId());

        DocumentTypeDTO testDocumentTypeDTO = getDocumentTypeDTO(
                documentsAttributesDTOSet1,
                1L, "Новый документ",
                "Такого вы еще не видели"
        );

        Long id = 1L;

        DocumentType documentType = getDocumentType();
        documentType.setId(id);
        documentType.setName("договор");
        documentType.setDescription("какое-то описание");

        List<Attribute> attributes = new ArrayList<>();
        attributes.add(new Attribute() {{
            setId(0L);
            setName("подписант");
        }});

        Mockito.when(documentTypeRepository.findById(id)).thenReturn(Optional.of(documentType));
        Mockito.when(attributesRepository.findAllById(documentTypeUpdateDTO.getAttributeIds())).thenReturn(attributes);
        Mockito.when(mapper.toDto(documentType)).thenReturn(testDocumentTypeDTO);

        DocumentTypeDTO result = documentTypeService.updateDocumentType(id, documentTypeUpdateDTO);

        Assertions.assertThat(result).isEqualTo(testDocumentTypeDTO);

        Mockito.verify(documentTypeRepository).save(documentType);

        Assertions.assertThat(documentType.getName()).isEqualTo("Новый документ");
        Assertions.assertThat(documentType.getDescription()).isEqualTo("Такого вы еще не видели");
        Assertions.assertThat(documentType.getAttributes()).containsExactlyInAnyOrderElementsOf(attributes);
    }


    private static DocumentTypeDTO getDocumentTypeDTO(Set<Long> documentsAttributesIdsSet,
                                                      Long documentTypeId,
                                                      String nameDocumentType,
                                                      String descriptionDocumentType) {

        DocumentTypeDTO testDocumentTypeDTO = new DocumentTypeDTO();
        testDocumentTypeDTO.setId(documentTypeId);
        testDocumentTypeDTO.setName(nameDocumentType);
        testDocumentTypeDTO.setDescription(descriptionDocumentType);
        testDocumentTypeDTO.setAttributeIds(documentsAttributesIdsSet);
        return testDocumentTypeDTO;
    }

    @Test
    void getAllDocumentType() {
        DocumentType documentType = getDocumentType();

        AttributeDTO testAttributeDto1 = getDocumentsAttributesDTO(0L, "подписант");


        Set<Long> documentsAttributesDTOSet1 = new HashSet<>();
        documentsAttributesDTOSet1.add(testAttributeDto1.getId());

        DocumentTypeDTO testDocumentTypeDTO1 = getDocumentTypeDTO(documentsAttributesDTOSet1,
                0L, "договор",
                "какое-то описание");

        int page = 0;
        int size = 2;
        PageRequest pageable = PageRequest.of(page, size);

        List<DocumentType> documentTypeList = List.of(documentType);

        Page<DocumentType> documentTypes = new PageImpl<>(documentTypeList, pageable, 1);

        Mockito.when(documentTypeRepository.findAll(pageable)).thenReturn(documentTypes);

        List<DocumentTypeDTO> documentTypeDTOList = List.of(testDocumentTypeDTO1);
        Page<DocumentTypeDTO> documentTypeDTOPage = new PageImpl<>(documentTypeDTOList, pageable, 1);

        Mockito.when(mapper.toDto(documentType)).thenReturn(testDocumentTypeDTO1);

        Assertions.assertThat(documentTypeService.getAllDocumentType(page, size))
                .isEqualTo(documentTypeDTOPage);
    }

    @Test
    void getDocumentTypeById() {
        Long id = 1L;

        DocumentType documentType = getDocumentType();

        AttributeDTO testAttributeDto1 = getDocumentsAttributesDTO(0L, "подписант");


        Set<Long> documentsAttributesDTOSet2 = new HashSet<>();
        documentsAttributesDTOSet2.add(testAttributeDto1.getId());

        DocumentTypeDTO testDocumentTypeDTO = getDocumentTypeDTO(documentsAttributesDTOSet2,
                1L, "договор",
                "какое-то описание");

        Mockito.when(documentTypeRepository.findById(id))
                .thenReturn(Optional.of(documentType));

        Mockito.when(mapper.toDto(documentType)).thenReturn(testDocumentTypeDTO);

        Assertions.assertThat(documentTypeService.getDocumentTypeById(id))
                .isEqualTo(testDocumentTypeDTO);
    }

    @Test
    void deleteDocumentType() {
        Long id = 1L;

        DocumentType documentType = getDocumentType();

        Mockito.when(documentTypeRepository.findById(id))
                .thenReturn(Optional.of(documentType));

        documentTypeService.deleteDocumentType(id);

        Mockito.verify(documentTypeRepository).deleteById(id);
    }
}