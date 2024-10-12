package ru.caselab.edm.backend.mapper;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;
import ru.caselab.edm.backend.dto.DocumentTypeDTO;
import ru.caselab.edm.backend.dto.DocumentsAttributesDTO;
import ru.caselab.edm.backend.entity.DocumentType;

import java.time.LocalDateTime;
import java.time.Month;
import java.util.ArrayList;
import java.util.List;

class DocumentTypeMapperTest {
    DocumentTypeMapper mapper = Mappers.getMapper(DocumentTypeMapper.class);

    @Test
    void mapDocumentTypeToDocumentTypeDTO() {
        LocalDateTime now = LocalDateTime
                .of(2024, Month.FEBRUARY, 22, 9, 49, 19, 275039200);


        DocumentTypeDTO testDocumentTypeDTO = new DocumentTypeDTO();


        DocumentsAttributesDTO testDocumentsAttributesDTO = new DocumentsAttributesDTO();
        testDocumentsAttributesDTO.setName("подписант");

        List<DocumentsAttributesDTO> documentsAttributesDTOList = new ArrayList<>();
        documentsAttributesDTOList.add(testDocumentsAttributesDTO);

        DocumentType testDocumenttype = new DocumentType();

        testDocumenttype.setId(1L);
        testDocumenttype.setName("договор");
        testDocumenttype.setDescription("какоей-то описание");

        testDocumentTypeDTO.setId(1L);
        testDocumentTypeDTO.setName("договор");
        testDocumentTypeDTO.setDescription("какоей-то описание");
        DocumentTypeDTO map = mapper.map(testDocumenttype);
        System.out.println(map);
        Assertions.assertThat(mapper.map(testDocumenttype)).isEqualTo(testDocumentTypeDTO);

    }
}