package ru.caselab.edm.backend.analytics.infrastructure;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import ru.caselab.edm.backend.analytics.infrastructure.projection.TopUsersByDocumentCreationProjection;
import ru.caselab.edm.backend.document.domain.Document;
import ru.caselab.edm.backend.document.domain.DocumentType;
import ru.caselab.edm.backend.integration.search.AttributeSearchRepository;
import ru.caselab.edm.backend.shared.testutils.annotations.DatabaseTest;
import ru.caselab.edm.backend.shared.testutils.builder.document.DocumentEntityBuilder;
import ru.caselab.edm.backend.shared.testutils.builder.documenttype.DocumentTypeEntityBuilder;
import ru.caselab.edm.backend.shared.testutils.facade.TestDatabaseFacade;

import java.time.Instant;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@DatabaseTest
public class AnalyticsRepositoryTest {

    @Autowired
    private AnalyticsRepository analyticsRepository;

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

        Page<TopUsersByDocumentCreationProjection> result =
                analyticsRepository.findTopUserByDocumentCreation(startDate, endDate, pageable);

        assertThat(result).isNotEmpty();

        Long firstUserDocumentCount = result.getContent().get(0).getDocumentCount();
        Long secondUserDocumentCount = result.getContent().get(1).getDocumentCount();

        assertThat(firstUserDocumentCount).isGreaterThanOrEqualTo(secondUserDocumentCount);
    }

    private void insertTestData() {
        UUID firstOwner = UUID.randomUUID();
        UUID secondOwner = UUID.randomUUID();

        DocumentType documentType = createDocumentType("Type1");
        testDatabaseFacade.saveAll(documentType);

        Document document1 = createDocument(firstOwner, documentType, Instant.now().minusSeconds(500_000));
        Document document2 = createDocument(firstOwner, documentType, Instant.now().minusSeconds(400_000));
        Document document3 = createDocument(secondOwner, documentType, Instant.now().minusSeconds(300_000));

        testDatabaseFacade.saveAll(document1, document2, document3);
    }

    private DocumentType createDocumentType(String name) {
        return DocumentTypeEntityBuilder.builder()
                .withName(name)
                .build();
    }

    private Document createDocument(UUID ownerId, DocumentType documentType, Instant createdAt) {
        return DocumentEntityBuilder.builder()
                .withOwnerId(ownerId)
                .withDocumentType(documentType)
                .withCreatedAt(createdAt)
                .build();
    }

    private void deleteTestData() {
        testDatabaseFacade.cleanDatabase();
    }
}
