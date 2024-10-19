package ru.caselab.edm.backend.repository;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;
import ru.caselab.edm.backend.entity.Document;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@ActiveProfiles("test")
@DataJpaTest
class DocumentRepositoryTest {
    @Autowired
    private JdbcTemplate jdbcTemplate;
    @Autowired
    DocumentRepository repository;

    @Test
    void getDocumentForUser() {
        initdb();

        Document actual1 = repository.getDocumentForUser(2L, UUID.fromString("c5df47fe-f4d2-45c2-8084-e646c85a7eba")).get();
        Assertions.assertThat(actual1)
                .matches(doc -> doc.getId() == 2L);
    }

    @Test
    void getDocumentForFailUser() {
        initdb();

        Optional<Document> actual2 = repository.getDocumentForUser(4L, UUID.fromString("48bbbd31-45c0-43c5-b989-c1c14a8c3b8b"));
        Assertions.assertThat(actual2).isEmpty();
    }


    @Test
    void getAllDocumentForUser() {
        initdb();

        UUID userId = UUID.fromString("48bbbd31-45c0-43c5-b989-c1c14a8c3b8b");

        int size = 10;
        int page = 0;
        Pageable pageable = PageRequest.of(page, size);

        Assertions.assertThat(repository.getAllDocumentForUser(userId, pageable)).extracting("id")
                .isEqualTo(List.of(1L, 2L, 3L));
    }

    private void initdb() {
        jdbcTemplate.execute("""
                INSERT INTO users (id, login, email, password, first_name, last_name, patronymic) VALUES 
                    ('48bbbd31-45c0-43c5-b989-c1c14a8c3b8b'::uuid, 'Test1', 'test1.gmail.com', 'test1password', 'Ivan', 'Ivanov', 'shskfckhvca'),
                    ('c5df47fe-f4d2-45c2-8084-e646c85a7eba'::uuid, 'Test2', 'test2.gmail.com', 'test2password', 'Mari', 'Ivanova', 'sdfvsfdev');
                INSERT INTO document_types (id, name, description, created_at, updated_at) VALUES 
                    (1, 'test_type', 'test', '2024-01-15', '2024-01-15');
                INSERT INTO documents (id, user_id, document_type_id) VALUES
                    (1,'48bbbd31-45c0-43c5-b989-c1c14a8c3b8b',1),
                    (2,'c5df47fe-f4d2-45c2-8084-e646c85a7eba',1),
                    (3,'48bbbd31-45c0-43c5-b989-c1c14a8c3b8b',1),
                    (4,'c5df47fe-f4d2-45c2-8084-e646c85a7eba',1);
                INSERT INTO document_versions (id, documents_id, document_name, created_at, updated_at, content_url) VALUES
                    (1, 1, 'document_name_test1', '2024-01-15', '2024-01-15', 'test_url'),
                    (2, 2, 'document_name_test2', '2024-01-15', '2024-01-15', 'test_url1'),
                    (3, 3, 'document_name_test3', '2024-01-15', '2024-01-15', 'test_url3'),
                    (4, 4, 'document_name_test4', '2024-01-15', '2024-01-15', 'test_url4');
                INSERT INTO signatures (id, user_id, created_at, hash, document_version_id) VALUES 
                     (1, 'c5df47fe-f4d2-45c2-8084-e646c85a7eba'::uuid, '2024-01-15', 'test_hash', 1),
                     (2, '48bbbd31-45c0-43c5-b989-c1c14a8c3b8b'::uuid, '2024-01-15', 'test_hash1', 2),
                     (3, '48bbbd31-45c0-43c5-b989-c1c14a8c3b8b'::uuid, '2024-01-15', 'test_hash2', 3),
                     (4, 'c5df47fe-f4d2-45c2-8084-e646c85a7eba'::uuid, '2024-01-15', 'test_hash3', 4);
                """);
    }
}