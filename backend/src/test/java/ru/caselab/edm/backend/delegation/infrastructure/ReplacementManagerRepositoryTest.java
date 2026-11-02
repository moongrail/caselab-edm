package ru.caselab.edm.backend.delegation.infrastructure;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import ru.caselab.edm.backend.delegation.domain.ReplacementManager;
import ru.caselab.edm.backend.integration.search.AttributeSearchRepository;
import ru.caselab.edm.backend.shared.testutils.annotations.DatabaseTest;
import ru.caselab.edm.backend.shared.testutils.facade.TestDatabaseFacade;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@DatabaseTest
public class ReplacementManagerRepositoryTest {

    @Autowired
    private ReplacementManagerRepository replacementManagerRepository;

    @Autowired
    private TestDatabaseFacade testDatabaseFacade;

    @MockBean
    AttributeSearchRepository attributeSearchRepository;

    private UUID managerUserId;
    private UUID tempManagerUserId;

    @BeforeEach
    void setUp() {
        deleteTestData();
        insertTestData();
    }

    @Test
    void findActiveReplacementByManagerUser_existsReplacementForUserId_shouldReturnReplacementManager() {
        Optional<ReplacementManager> result = replacementManagerRepository
                .findActiveReplacementByManagerUserId(managerUserId);

        assertThat(result)
                .isPresent()
                .get()
                .satisfies(rm -> assertThat(rm.getTempManagerUserId()).isEqualTo(tempManagerUserId));
    }

    @Test
    void findActiveReplacementByManagerUser_nonExistsReplacementForUserId_shouldReturnEmptyOptional() {
        Optional<ReplacementManager> replacementManagerOptional = replacementManagerRepository
                .findActiveReplacementByManagerUserId(tempManagerUserId);

        assertThat(replacementManagerOptional).isEmpty();
    }

    @Test
    void findActiveReplacementByManagerUser_replacementIsExpired_shouldReturnEmptyOptional() {
        UUID expiredManager = UUID.randomUUID();

        saveReplacementManager(expiredManager, tempManagerUserId,
                Instant.now().minusSeconds(100000), Instant.now().minusSeconds(50000));

        Optional<ReplacementManager> replacementManagerOptional = replacementManagerRepository
                .findActiveReplacementByManagerUserId(expiredManager);

        assertThat(replacementManagerOptional).isEmpty();
    }

    @Test
    void findActiveReplacementByManagerUserIds_noActiveReplacementForUser_shouldReturnEmptyList() {
        UUID newManager = UUID.randomUUID();

        List<ReplacementManager> result = replacementManagerRepository
                .findActiveReplacementsByManagerUserIds(Set.of(newManager));

        assertThat(result).isEmpty();
    }

    @Test
    void findActiveReplacementByManagerUserIds_existsReplacementForMultipleUserIds_shouldReturnLastReplacementForEachUser() {
        UUID secondManager = UUID.randomUUID();
        UUID thirdManager = UUID.randomUUID();

        saveReplacementManager(managerUserId, tempManagerUserId,
                Instant.now().minusSeconds(3600), Instant.now().plusSeconds(3600));
        saveReplacementManager(secondManager, tempManagerUserId,
                Instant.now().minusSeconds(7200), Instant.now().plusSeconds(7200));
        saveReplacementManager(thirdManager, tempManagerUserId,
                Instant.now().minusSeconds(1800), Instant.now().plusSeconds(1800));

        List<ReplacementManager> result = replacementManagerRepository
                .findActiveReplacementsByManagerUserIds(
                        Set.of(managerUserId, secondManager, thirdManager));

        assertThat(result)
                .hasSize(3)
                .extracting(ReplacementManager::getManagerUserId)
                .containsExactlyInAnyOrder(managerUserId, secondManager, thirdManager);
    }

    @Test
    void findActiveReplacementByManagerUserIds_replacementIsExpired_shouldReturnEmptyList() {
        UUID expiredManager = UUID.randomUUID();
        saveReplacementManager(expiredManager, tempManagerUserId,
                Instant.now().minusSeconds(100000), Instant.now().minusSeconds(50000));

        List<ReplacementManager> result = replacementManagerRepository
                .findActiveReplacementsByManagerUserIds(Set.of(expiredManager));

        assertThat(result).isEmpty();
    }

    private void deleteTestData() {
        testDatabaseFacade.cleanDatabase();
    }

    private void insertTestData() {
        managerUserId = UUID.randomUUID();
        tempManagerUserId = UUID.randomUUID();

        saveReplacementManager(managerUserId, tempManagerUserId,
                Instant.now().minusSeconds(3600), Instant.now().plusSeconds(3600));
    }

    private ReplacementManager saveReplacementManager(UUID manager, UUID tempManager,
                                                      Instant startDate, Instant endDate) {
        ReplacementManager replacement = ReplacementManager.builder()
                .managerUserId(manager)
                .tempManagerUserId(tempManager)
                .startDate(startDate)
                .endDate(endDate)
                .build();
        return testDatabaseFacade.save(replacement);
    }
}
