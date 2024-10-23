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

    private void initdb() {
        jdbcTemplate.execute("""
                INSERT INTO users (id, login, email, password, first_name, last_name, patronymic) VALUES 
                    ('48bbbd31-45c0-43c5-b989-c1c14a8c3b8b'::uuid, 'Test1', 'test1.gmail.com', 'test1password', 'Ivan', 'Ivanov', 'shskfckhvca'),
                    ('c5df47fe-f4d2-45c2-8084-e646c85a7eba'::uuid, 'Test2', 'test2.gmail.com', 'test2password', 'Mari', 'Ivanova', 'sdfvsfdev'),
                    ('c5df47fe-f4d2-45c2-8084-e546c85a7eba'::uuid, 'Test3', 'test3.gmail.com', 'test3password', 'Test', 'Test', 'test');
                INSERT INTO document_types (id, name, description, created_at, updated_at) VALUES 
                    (1, 'test_type', 'test', '2024-01-15', '2024-01-15');
                INSERT INTO documents (id, user_id, document_type_id) VALUES
                    (1,'48bbbd31-45c0-43c5-b989-c1c14a8c3b8b'::uuid,1),
                    (2,'c5df47fe-f4d2-45c2-8084-e646c85a7eba'::uuid,1),
                    (3,'48bbbd31-45c0-43c5-b989-c1c14a8c3b8b'::uuid,1),
                    (4,'c5df47fe-f4d2-45c2-8084-e646c85a7eba'::uuid,1);
                INSERT INTO document_versions (id, documents_id, document_name, created_at, updated_at, content_url) VALUES
                    (1, 1, 'document_name_test1', '2024-01-15', '2024-01-15', 'test_url'),
                    (2, 2, 'document_name_test2', '2024-01-15', '2024-01-15', 'test_url1'),
                    (3, 3, 'document_name_test3', '2024-01-15', '2024-01-15', 'test_url3'),
                    (4, 4, 'document_name_test4', '2024-01-15', '2024-01-15', 'test_url4');
                INSERT INTO approvement_process(id, document_version_id, status, deadline, agreement_procent) VALUES 
                    (1, 1, 'Test1', '2024-01-15', 1.00),
                    (2, 3, 'Test2', '2024-01-15', 1.00),
                    (3, 2, 'Test3', '2024-01-15', 1.00);
                INSERT INTO approvment_process_item (id, app_procc_id, user_id, created_at, status, document_version_id) VALUES
                    (1, 1, 'c5df47fe-f4d2-45c2-8084-e546c85a7eba'::uuid, '2024-01-15', 'test', 3),
                    (2, 2, 'c5df47fe-f4d2-45c2-8084-e646c85a7eba'::uuid, '2024-01-15', 'test1', 3),
                    (3, 3, 'c5df47fe-f4d2-45c2-8084-e646c85a7eba'::uuid, '2024-01-15', 'test1', 2);
                """);
    }

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
    void getAllDocumentForUserWhoCreateDocument() {
        initdb();

        UUID userId = UUID.fromString("48bbbd31-45c0-43c5-b989-c1c14a8c3b8b");

        int size = 10;
        int page = 0;
        Pageable pageable = PageRequest.of(page, size);

        Assertions.assertThat(repository.getAllDocumentForUser(userId, pageable)).extracting("id")
                .isEqualTo(List.of(1L, 3L));
    }

    @Test
    void getAllDocumentForUserWhoApprovement() {
        initdb();

        UUID userId = UUID.fromString("c5df47fe-f4d2-45c2-8084-e546c85a7eba");

        int size = 10;
        int page = 0;
        Pageable pageable = PageRequest.of(page, size);

        Assertions.assertThat(repository.getAllDocumentForUser(userId, pageable)).extracting("id")
                .isEqualTo(List.of(3L));
    }

    @Test
    void getAllDocumentForUserWhoCreateAndApprovement() {
        initdb();

        UUID userId = UUID.fromString("c5df47fe-f4d2-45c2-8084-e646c85a7eba");

        int size = 10;
        int page = 0;
        Pageable pageable = PageRequest.of(page, size);

        Assertions.assertThat(repository.getAllDocumentForUser(userId, pageable)).extracting("id")
                .isEqualTo(List.of(2L, 3L, 4L));
    }


}