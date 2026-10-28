package ru.caselab.edm.backend.service;

import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;
import ru.caselab.edm.backend.entity.Attribute;
import ru.caselab.edm.backend.entity.DocumentType;
import ru.caselab.edm.backend.mapper.attribute.AttributeMapper;
import ru.caselab.edm.backend.mapper.attributevalue.DocumentAttributeValueMapper;

import static org.assertj.core.api.Assertions.assertThat;
import static ru.caselab.edm.backend.testutils.fixtures.TestFixtures.attribute;
import static ru.caselab.edm.backend.testutils.fixtures.TestFixtures.documentType;

class MapperDefaultMethodsTest {

    @Test
    void attributeMapper_mapsNullAndPresentDocumentTypes() {
        AttributeMapper mapper = Mappers.getMapper(AttributeMapper.class);
        DocumentType type = documentType(5L);

        assertThat(mapper.mapDocumentTypeToId(null)).isNull();
        assertThat(mapper.mapDocumentTypeToId(type)).isEqualTo(5L);
        assertThat(mapper.mapIdToDocumentType(null)).isNull();
        assertThat(mapper.mapIdToDocumentType(7L).getId()).isEqualTo(7L);
    }

    @Test
    void documentAttributeValueMapper_mapsNullAndPresentReferences() {
        DocumentAttributeValueMapper mapper = Mappers.getMapper(DocumentAttributeValueMapper.class);
        Attribute attribute = attribute(3L, false);

        assertThat(mapper.mapAttributeToId(null)).isNull();
        assertThat(mapper.mapAttributeToId(attribute)).isEqualTo(3L);
        assertThat(mapper.mapDocumentVersionToId(null)).isNull();
    }
}
