package ru.caselab.edm.backend.repository;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import ru.caselab.edm.backend.testutils.builder.document.DocumentEntityBuilder;
import ru.caselab.edm.backend.testutils.builder.documenttype.DocumentTypeEntityBuilder;
import ru.caselab.edm.backend.testutils.builder.user.UserEntityBuilder;
import ru.caselab.edm.backend.testutils.annotations.DatabaseTest;
import ru.caselab.edm.backend.entity.Document;
import ru.caselab.edm.backend.entity.DocumentType;
import ru.caselab.edm.backend.entity.User;
import ru.caselab.edm.backend.testutils.facade.TestDatabaseFacade;
import ru.caselab.edm.backend.repository.elastic.AttributeSearchRepository;
import ru.caselab.edm.backend.repository.projection.TopUsersByDocumentCreationProjection;

import java.time.Instant;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DatabaseTest
public class UserRepositoryTest {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private TestDatabaseFacade testDatabaseFacade;

    @MockBean
    AttributeSearchRepository attributeSearchRepository;

    @BeforeEach
    void setUp() {
        deleteTestData();
        insertTestData();
    }

    @Test
    void findTopUsersByDocumentCreation_withValidDateRange_shouldReturnSortedUsersByDocumentCount() {
        Pageable pageable = PageRequest.of(0, 10);
        Instant startDate = Instant.now().minusSeconds(2_000_000);
        Instant endDate = Instant.now();

        List<TopUsersByDocumentCreationProjection> result = userRepository.findTopUserByDocumentCreation(startDate, endDate, pageable);

        assertThat(result).isNotEmpty();

        Long firstUserDocumentCount = result.get(0).getDocumentCount();
        Long secondUserDocumentCount = result.get(1).getDocumentCount();

        assertThat(firstUserDocumentCount).isGreaterThanOrEqualTo(secondUserDocumentCount);
    }

    private void insertTestData() {
        // Users
        User first = createUser("first@gmail.com", "first");
        User second = createUser("second@gmail.com", "second");
        testDatabaseFacade.save(first, second);

        // DocumentType
        DocumentType documentType = createDocumentType("Type1");
        testDatabaseFacade.save(documentType);

        // Documents
        Document document1 = createDocument(first, documentType, Instant.now().minusSeconds(500_000));
        Document document2 = createDocument(first, documentType, Instant.now().minusSeconds(400_000));
        Document document3 = createDocument(second, documentType, Instant.now().minusSeconds(300_000));

        testDatabaseFacade.save(document1, document2, document3);
    }

    private User createUser(String email, String login) {
        return UserEntityBuilder.builder()
                .withEmail(email)
                .withLogin(login)
                .build();
    }

    private DocumentType createDocumentType(String name) {
        return DocumentTypeEntityBuilder.builder()
                .withName(name)
                .build();
    }

    private Document createDocument(User user, DocumentType documentType, Instant createdAt) {
        return DocumentEntityBuilder.builder()
                .withUser(user)
                .withDocumentType(documentType)
                .withCreatedAt(createdAt)
                .build();
    }

    private void deleteTestData() {
        testDatabaseFacade.cleanDatabase();
    }
}
