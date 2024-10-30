package ru.caselab.edm.backend.mapper;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;
import ru.caselab.edm.backend.dto.AttributeDTO;
import ru.caselab.edm.backend.entity.Attribute;
import ru.caselab.edm.backend.entity.DocumentType;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class AttributeMapperTest {
    AttributeMapper mapper = Mappers.getMapper(AttributeMapper.class);

    @Test
    void mapAttributeToAttributeDTO() {

        DocumentType testDocumentType = new DocumentType();
        testDocumentType.setId(1L);
        testDocumentType.setName("договор");
        testDocumentType.setDescription("какое-то описание");

        Attribute testAttribute = new Attribute();
        testAttribute.setId(1L);
        testAttribute.setName("подпись");
        testAttribute.setDocumentTypes(new HashSet<>(List.of(testDocumentType)));

        AttributeDTO testAttributeDTO = new AttributeDTO();
        testAttributeDTO.setId(1L);
        testAttributeDTO.setName("подпись");


        Set<Long> documentsTypesId = new HashSet<>();
        documentsTypesId.add(testDocumentType.getId());
        testAttributeDTO.setDocumentTypeIds(documentsTypesId);


        AttributeDTO map = mapper.toDTO(testAttribute);
        System.out.println(map);
        Assertions.assertThat(mapper.toDTO(testAttribute)).isEqualTo(testAttributeDTO);
    }

    @Test
    void mapAttributeDTOToAttribute() {

        DocumentType testDocumentType = new DocumentType();
        testDocumentType.setId(1L);

        Attribute testAttribute = new Attribute();
        testAttribute.setId(1L);
        testAttribute.setName("подпись");
        testAttribute.setDocumentTypes(new HashSet<>(List.of(testDocumentType)));

        AttributeDTO testAttributeDTO = new AttributeDTO();
        testAttributeDTO.setId(1L);
        testAttributeDTO.setName("подпись");


        Set<Long> documentsTypesId = new HashSet<>();
        documentsTypesId.add(testDocumentType.getId());
        testAttributeDTO.setDocumentTypeIds(documentsTypesId);


        Attribute map = mapper.toEntity(testAttributeDTO);
        Assertions.assertThat(map).usingRecursiveComparison().isEqualTo(testAttribute);
    }
}
