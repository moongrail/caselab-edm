package ru.caselab.edm.backend.mapper;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;
import ru.caselab.edm.backend.dto.documenttype.DocumentTypeDTO;
import ru.caselab.edm.backend.entity.Attribute;
import ru.caselab.edm.backend.entity.DocumentType;
import ru.caselab.edm.backend.mapper.documenttype.DocumentTypeMapper;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

class DocumentTypeMapperTest {
    DocumentTypeMapper mapper = Mappers.getMapper(DocumentTypeMapper.class);

    @Test
    void mapDocumentTypeToDocumentTypeDTO() {


        DocumentTypeDTO testDocumentTypeDTO = new DocumentTypeDTO();
        testDocumentTypeDTO.setId(1L);
        testDocumentTypeDTO.setName("договор");
        testDocumentTypeDTO.setDescription("какоей-то описание");

        Attribute testAttribute = new Attribute();
        testAttribute.setId(1L);
        testAttribute.setName("подпись");


        Set<Long> documentsAttributesId = new HashSet<>();
        documentsAttributesId.add(testDocumentTypeDTO.getId());
        testDocumentTypeDTO.setAttributeIds(documentsAttributesId);

        DocumentType testDocumentType = new DocumentType();

        testDocumentType.setId(1L);
        testDocumentType.setName("договор");
        testDocumentType.setDescription("какоей-то описание");
        testDocumentType.setAttributes(new HashSet<>(List.of(testAttribute)));


        DocumentTypeDTO map = mapper.toDto(testDocumentType);
        System.out.println(map);
        Assertions.assertThat(mapper.toDto(testDocumentType)).isEqualTo(testDocumentTypeDTO);

    }
}