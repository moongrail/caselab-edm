package ru.caselab.edm.backend.repository;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;
import ru.caselab.edm.backend.dto.document.DocumentOutputAllDocumentsDTO;
import ru.caselab.edm.backend.entity.Document;
import ru.caselab.edm.backend.repository.elastic.AttributeSearchRepository;
import ru.caselab.edm.backend.state.DocumentStatus;

import java.time.LocalDateTime;
import java.time.ZoneId;
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
    @MockBean
    AttributeSearchRepository attributeSearchRepository;
    private void initdb() {
        jdbcTemplate.execute("""
                INSERT INTO users (id, department_id, login, email, password, first_name, last_name, patronymic, "position") VALUES
                ('48bbbd31-45c0-43c5-b989-c1c14a8c3b8b', 1, 'Test1', 'test1@gmail.com', 'test1password', 'Ivan', 'Ivanov', 'shskfckhvca', 'sfsadfa'),
                ('c5df47fe-f4d2-45c2-8084-e646c85a7eba', 1, 'Test2', 'test2@gmail.com', 'test2password', 'Mari', 'Ivanova', 'sdfvsfdev', 'dfgdsfgdsfgdsf'),
                ('c5df47fe-f4d2-45c2-8084-e546c85a7eba', 1, 'Test3', 'test3@gmail.com', 'test3password', 'Test', 'Test', 'test', 'dsgdfgdf');
                INSERT INTO document_types (id, name, description, created_at, updated_at) VALUES
                    (1, 'test_type', 'test', '2024-01-15', '2024-01-15');
                INSERT INTO documents (id, user_id, document_type_id, created_at) VALUES
                    (1,'48bbbd31-45c0-43c5-b989-c1c14a8c3b8b',1, '2024-01-15'::TIMESTAMP AT TIME ZONE 'UTC'),
                    (2,'c5df47fe-f4d2-45c2-8084-e646c85a7eba',1, '2024-01-15'::TIMESTAMP AT TIME ZONE 'UTC'),
                    (3,'48bbbd31-45c0-43c5-b989-c1c14a8c3b8b',1, '2024-01-15'::TIMESTAMP AT TIME ZONE 'UTC'),
                    (4,'c5df47fe-f4d2-45c2-8084-e646c85a7eba',1, '2024-01-15'::TIMESTAMP AT TIME ZONE 'UTC');
                INSERT INTO document_versions (id, documents_id, document_name, created_at, updated_at, content_url, state) VALUES
                    (1, 1, 'document_name_test1', '2024-01-18'::TIMESTAMP AT TIME ZONE 'UTC', '2024-01-15', 'test_url', 'PENDING_AUTHOR_SIGN'),
                    (2, 2, 'document_name_test2', '2024-01-15'::TIMESTAMP AT TIME ZONE 'UTC', '2024-01-15', 'test_url1', 'IN_VOTING'),
                    (3, 3, 'document_name_test3', '2024-01-15'::TIMESTAMP AT TIME ZONE 'UTC', '2024-01-15', 'test_url3', 'IN_VOTING'),
                    (4, 4, 'document_name_test4', '2024-01-15'::TIMESTAMP AT TIME ZONE 'UTC', '2024-01-15', 'test_url4', 'IN_VOTING'),
                    (5, 1, 'document_name_test1', '2024-01-16'::TIMESTAMP AT TIME ZONE 'UTC', '2024-01-15', 'test_url', 'PENDING_AUTHOR_SIGN');
                INSERT INTO approvement_process(id, document_version_id, status, deadline, agreement_procent) VALUES
                    (1, 1, 'VOTING_REJECTED', '2024-01-15', 1.00),
                    (2, 3, 'VOTING_REJECTED', '2024-01-15', 1.00),
                    (3, 2, 'VOTING_APPROVED', '2024-01-15', 1.00);
                INSERT INTO approvment_process_item (id, app_procc_id, user_id, created_at, status, document_version_id) VALUES
                    (1, 1, 'c5df47fe-f4d2-45c2-8084-e546c85a7eba', '2024-01-15', 'test', 3),
                    (2, 2, 'c5df47fe-f4d2-45c2-8084-e646c85a7eba', '2024-01-15', 'test1', 3),
                    (3, 3, '48bbbd31-45c0-43c5-b989-c1c14a8c3b8b', '2024-01-15', 'test1', 2);
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
    void getAllDocumentWithNameAndStatusProjectionForUser() {
        initdb();
        Pageable pageable = PageRequest.of(0, 5);

        List<DocumentOutputAllDocumentsDTO> expected = List.of(
                new DocumentOutputAllDocumentsDTO(1L, "Test1",
                        LocalDateTime.parse("2024-01-15T00:00:00").atZone(ZoneId.systemDefault()).toInstant(),
                        "document_name_test1",
                        "test_url",
                        DocumentStatus.PENDING_AUTHOR_SIGN),
                new DocumentOutputAllDocumentsDTO(3L, "Test1",
                        LocalDateTime.parse("2024-01-15T00:00:00").atZone(ZoneId.systemDefault()).toInstant(),
                        "document_name_test3",
                        "test_url3",
                        DocumentStatus.IN_VOTING)
        );

        Page<DocumentOutputAllDocumentsDTO> actual2 = repository
                .getAllDocumentWithNameAndStatusProjectionForUser(UUID.fromString("48bbbd31-45c0-43c5-b989-c1c14a8c3b8b"), pageable);
        Assertions.assertThat(actual2.get().toList()).hasSize(2).usingRecursiveComparison().isEqualTo(expected);
    }


    @Test
    void getAllDocumentWithNameAndStatusProjectionWhereUserSignatories() {
        initdb();
        Pageable pageable = PageRequest.of(0, 5);

        List<DocumentOutputAllDocumentsDTO> expected = List.of(
                new DocumentOutputAllDocumentsDTO(2L, "Test1",
                        LocalDateTime.parse("2024-01-15T00:00:00").atZone(ZoneId.systemDefault()).toInstant(),
                        "document_name_test2",
                        "test_url1",
                        DocumentStatus.IN_VOTING)
        );

        Page<DocumentOutputAllDocumentsDTO> actual2 = repository
                .getAllDocumentWithNameAndStatusProjectionWhereUserSignatories(UUID.fromString("48bbbd31-45c0-43c5-b989-c1c14a8c3b8b"), pageable);
        Assertions.assertThat(actual2.get().toList()).hasSize(1).usingRecursiveComparison().isEqualTo(expected);
    }
}