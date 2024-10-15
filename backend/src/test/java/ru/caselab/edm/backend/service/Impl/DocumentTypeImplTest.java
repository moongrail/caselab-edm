package ru.caselab.edm.backend.service.Impl;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import ru.caselab.edm.backend.dto.DocumentTypeDTO;
import ru.caselab.edm.backend.dto.DocumentsAttributesDTO;
import ru.caselab.edm.backend.entity.Attribute;
import ru.caselab.edm.backend.entity.DocumentType;
import ru.caselab.edm.backend.mapper.DocumentTypeMapper;
import ru.caselab.edm.backend.repository.AttributeRepository;
import ru.caselab.edm.backend.repository.DocumentTypeRepository;
import ru.caselab.edm.backend.service.impl.DocumentTypeImpl;

import java.time.LocalDateTime;
import java.time.Month;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

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

    private static DocumentsAttributesDTO getDocumentsAttributesDTO(Long attributesId,
                                                                    String attributesName) {
        DocumentsAttributesDTO testDocumentsAttributesDTO = new DocumentsAttributesDTO();
        testDocumentsAttributesDTO.setId(attributesId);
        testDocumentsAttributesDTO.setName(attributesName);
        return testDocumentsAttributesDTO;
    }
//TODO: ПЕРЕДЕЛАТЬ ПОД НОВУЮ ЛОГИКУ

//    @Test
//    void createDocumentType() {
//        //Входные данные
//        List<Long> attributesDocumentTypeId = new ArrayList<Long>(List.of(0L));
//
//        DocumentTypeCreateDTO testDto = new DocumentTypeCreateDTO();
//        testDto.setName("договор");
//        testDto.setDescription("какое-то описание");
//        testDto.setAttributesDocumentTypeId(attributesDocumentTypeId);
//
//        //выходные данные
//        DocumentsAttributesDTO testDocumentsAttributesDTO = getDocumentsAttributesDTO(0L,
//                "подписант");
//
//        List<DocumentsAttributesDTO> documentsAttributesDTOList = new ArrayList<>();
//        documentsAttributesDTOList.add(testDocumentsAttributesDTO);
//
//        DocumentTypeDTO testDocumentTypeDTO = getDocumentTypeDTO(documentsAttributesDTOList,
//                1L, "договор",
//                "какое-то описание");
//
//        DocumentType documentType = new DocumentType();
//        documentType.setName("договор");
//        documentType.setDescription("какое-то описание");
//
//        List<Attribute> attributes = new ArrayList<>();
//
//        Mockito.when(attributesRepository.findAllById(attributesDocumentTypeId)).thenReturn(attributes);
//
//        Mockito.when(mapper.map(documentType)).thenReturn(testDocumentTypeDTO);
//
//        Assertions.assertThat(documentTypeService.createDocumentType(testDto))
//                .isEqualTo(testDocumentTypeDTO);
//
//        Mockito.verify(documentTypeRepository).save(documentType);
//    }

//    @Test
//    void updateDocumentType() {
//        //DocumentTypeUpdateDTO
//        DocumentTypeUpdateDTO documentTypeUpdateDTO = new DocumentTypeUpdateDTO();
//        documentTypeUpdateDTO.setAttributesDocumentTypeId(List.of(1L));
//        documentTypeUpdateDTO.setName("Новый документ");
//        documentTypeUpdateDTO.setDescription("Такого вы еще не видели");
//
//        //DocumentTypeDTO
//        DocumentsAttributesDTO testDocumentsAttributesDTO = getDocumentsAttributesDTO(0L,
//                "подписант");
//
//        List<DocumentsAttributesDTO> documentsAttributesDTOList = new ArrayList<>();
//        documentsAttributesDTOList.add(testDocumentsAttributesDTO);
//
//        DocumentTypeDTO testDocumentTypeDTO = getDocumentTypeDTO(documentsAttributesDTOList,
//                1L, "договор",
//                "какое-то описание");
//
//        //Long id
//        Long id = 1L;
//
//        //DocumentType
//        DocumentType documentType = getDocumentType();
//
//        List<Attribute> attributes = new ArrayList<>();
//        List<Long> attributesDocumentTypeId = new ArrayList<>(List.of(1L));
//
//        Mockito.when(documentTypeRepository.findById(id)).thenReturn(Optional.of(documentType));
//
//        Mockito.when(attributesRepository.findAllById(attributesDocumentTypeId)).thenReturn(attributes);
//
//        Mockito.when(mapper.map(documentType)).thenReturn(testDocumentTypeDTO);
//
//        Assertions.assertThat(documentTypeService.updateDocumentType(id, documentTypeUpdateDTO))
//                .isEqualTo(testDocumentTypeDTO);
//
//        Mockito.verify(documentTypeRepository).save(documentType);
//    }

    private static DocumentTypeDTO getDocumentTypeDTO(List<DocumentsAttributesDTO> documentsAttributesDTOList,
                                                      Long documentTypeId,
                                                      String nameDocumentType,
                                                      String descriptionDocumentType) {

        DocumentTypeDTO testDocumentTypeDTO = new DocumentTypeDTO();
        testDocumentTypeDTO.setId(documentTypeId);
        testDocumentTypeDTO.setName(nameDocumentType);
        testDocumentTypeDTO.setDescription(descriptionDocumentType);
        testDocumentTypeDTO.setAttributes(documentsAttributesDTOList);
        return testDocumentTypeDTO;
    }

    @Test
    void getAllDocumentType() {
        DocumentType documentType = getDocumentType();

        DocumentsAttributesDTO testDocumentsAttributesDTO = getDocumentsAttributesDTO(0L,
                "подписант");

        List<DocumentsAttributesDTO> documentsAttributesDTOList = new ArrayList<>();
        documentsAttributesDTOList.add(testDocumentsAttributesDTO);

        DocumentTypeDTO testDocumentTypeDTO = getDocumentTypeDTO(documentsAttributesDTOList,
                1L, "договор",
                "какое-то описание");

        int page = 0;
        int size = 2;
        PageRequest pageable = PageRequest.of(page, size);

        List<DocumentType> documentTypeList = List.of(documentType);

        Page<DocumentType> documentTypes = new PageImpl<>(documentTypeList, pageable, 1);

        Mockito.when(documentTypeRepository.findAll(pageable)).thenReturn(documentTypes);

        List<DocumentTypeDTO> documentTypeDTOList = List.of(testDocumentTypeDTO);
        Page<DocumentTypeDTO> documentTypeDTOPage = new PageImpl<>(documentTypeDTOList, pageable, 1);

        Mockito.when(mapper.map(documentType)).thenReturn(testDocumentTypeDTO);

        Assertions.assertThat(documentTypeService.getAllDocumentType(page, size))
                .isEqualTo(documentTypeDTOPage);
    }

    @Test
    void getDocumentTypeById() {
        Long id = 1L;

        DocumentType documentType = getDocumentType();

        DocumentsAttributesDTO testDocumentsAttributesDTO = getDocumentsAttributesDTO(0L,
                "подписант");

        List<DocumentsAttributesDTO> documentsAttributesDTOList = new ArrayList<>();
        documentsAttributesDTOList.add(testDocumentsAttributesDTO);

        DocumentTypeDTO testDocumentTypeDTO = getDocumentTypeDTO(documentsAttributesDTOList,
                1L, "договор",
                "какое-то описание");

        Mockito.when(documentTypeRepository.findById(id))
                .thenReturn(Optional.of(documentType));

        Mockito.when(mapper.map(documentType)).thenReturn(testDocumentTypeDTO);

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