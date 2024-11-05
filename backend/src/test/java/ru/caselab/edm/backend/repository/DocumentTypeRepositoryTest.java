package ru.caselab.edm.backend.repository;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;
import ru.caselab.edm.backend.entity.Attribute;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;

@ActiveProfiles("test")
@DataJpaTest
class DocumentTypeRepositoryTest {
    @Autowired
    DocumentTypeRepository documentTypeRepository;
    @Autowired
    AttributeRepository attributesRepository;
    @Autowired
    private JdbcTemplate jdbcTemplate;

    private void initdb() {
        jdbcTemplate.execute("""
                INSERT INTO document_types (id, name, description, created_at, updated_at) VALUES 
                    (1L, 'test_type', 'test', '2024-01-15', '2024-01-15'),
                    (2L, 'test_type1', 'test1', '2024-01-15', '2024-01-15');
                INSERT INTO attributes (id, name, data_type, is_required) VALUES
                    (1L, 'подписант', 'текст', true),
                    (2L, 'сумма', 'число', false),
                    (3L, 'предмет договора', 'текст', true);
                INSERT INTO document_type_attributes (id, doc_type_id, attribute_id) VALUES
                    (1, 2, 2),
                    (2, 2, 3),
                    (3, 2, 1),
                    (4, 1, null);
                """);
    }

    @Test
    void findByName() {
        initdb();
        Set<Attribute> actualSet = documentTypeRepository.findByName("test_type1").get().getAttributes();
        List<Long> actual = new ArrayList<>();
        for (Attribute a : actualSet) {
            actual.add(a.getId());
        }
        Collections.sort(actual);
        Assertions.assertThat(actual)
                .isEqualTo(List.of(1L, 2L, 3L));
    }
}