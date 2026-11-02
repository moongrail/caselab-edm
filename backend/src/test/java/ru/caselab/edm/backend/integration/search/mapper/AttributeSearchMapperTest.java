package ru.caselab.edm.backend.integration.search.mapper;

import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;
import ru.caselab.edm.backend.document.domain.Attribute;
import ru.caselab.edm.backend.integration.search.AttributeSearch;
import ru.caselab.edm.backend.integration.search.mapper.AttributeSearchMapper;

import static org.assertj.core.api.Assertions.assertThat;

class AttributeSearchMapperTest {

    private final AttributeSearchMapper mapper = Mappers.getMapper(AttributeSearchMapper.class);

    @Test
    void mapsAttributeToSearchDocument() {
        Attribute attribute = Attribute.builder()
                .id(7L)
                .name("amount")
                .dataType("number")
                .isRequired(true)
                .build();

        AttributeSearch result = mapper.toDTO(attribute);

        assertThat(result.getId()).isEqualTo(7L);
        assertThat(result.getName()).isEqualTo("amount");
        assertThat(result.getDataType()).isEqualTo("number");
        assertThat(result.isRequired()).isTrue();
        assertThat(result.getDocuments()).isEmpty();
    }

    @Test
    void mapsSearchDocumentToAttribute() {
        AttributeSearch searchDocument = AttributeSearch.builder()
                .id(9L)
                .name("currency")
                .dataType("string")
                .isRequired(false)
                .build();

        Attribute result = mapper.toEntity(searchDocument);

        assertThat(result.getId()).isEqualTo(9L);
        assertThat(result.getName()).isEqualTo("currency");
        assertThat(result.getDataType()).isEqualTo("string");
        assertThat(result.isRequired()).isFalse();
    }

    @Test
    void returnsNullForNullArguments() {
        assertThat(mapper.toDTO(null)).isNull();
        assertThat(mapper.toEntity(null)).isNull();
    }
}
