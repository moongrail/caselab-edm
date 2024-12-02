package ru.caselab.edm.backend.repository;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import ru.caselab.edm.backend.entity.Department;
import ru.caselab.edm.backend.entity.ReplacementManager;
import ru.caselab.edm.backend.entity.User;
import ru.caselab.edm.backend.repository.elastic.AttributeSearchRepository;
import ru.caselab.edm.backend.testutils.annotations.DatabaseTest;
import ru.caselab.edm.backend.testutils.builder.department.DepartmentEntityBuilder;
import ru.caselab.edm.backend.testutils.builder.user.UserEntityBuilder;
import ru.caselab.edm.backend.testutils.facade.TestDatabaseFacade;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

@DatabaseTest
public class ReplacementManagerRepositoryTest {

    @Autowired
    private ReplacementManagerRepository replacementManagerRepository;

    @Autowired
    private TestDatabaseFacade testDatabaseFacade;

    @MockBean
    AttributeSearchRepository attributeSearchRepository;

    private Department department;
    private User managerUser;
    private User tempManagerUser;

    @BeforeEach
    void setUp() {
        deleteTestData();
        insertTestData();
    }

    @Test
    void findActiveReplacementByManagerUser_existsReplacementForUserId_shouldReturnReplacementManager() {
        Optional<ReplacementManager> result = replacementManagerRepository
                .findActiveReplacementByManagerUserId(managerUser.getId());

        assertThat(result)
                .isPresent()
                .get()
                .satisfies(rm -> assertThat(rm.getTempManagerUser()).isEqualTo(tempManagerUser));
    }

    @Test
    void findActiveReplacementByManagerUser_nonExistsReplacementForUserId_shouldReturnEmptyOptional() {
        Optional<ReplacementManager> replacementManagerOptional = replacementManagerRepository
                .findActiveReplacementByManagerUserId(tempManagerUser.getId());

        assertThat(replacementManagerOptional).isEmpty();
    }

    @Test
    void findActiveReplacementByManagerUser_replacementIsExpired_shouldReturnEmptyOptional() {
        User expiredManager = saveManager("anotherManager", "anotherManager@gmail.com");

        saveReplacementManager(expiredManager, tempManagerUser,
                Instant.now().minusSeconds(100000), Instant.now().minusSeconds(50000));

        Optional<ReplacementManager> replacementManagerOptional = replacementManagerRepository
                .findActiveReplacementByManagerUserId(expiredManager.getId());

        assertThat(replacementManagerOptional).isEmpty();

    }

    @Test
    void findActiveReplacementByManagerUserIds_noActiveReplacementForUser_shouldReturnEmptyList() {
        User newManager = saveManager("newManager", "newManager@example.com");

        List<ReplacementManager> result = replacementManagerRepository
                .findActiveReplacementsByManagerUserIds(Set.of(newManager.getId()));

        assertThat(result).isEmpty();
    }

    @Test
    void findActiveReplacementByManagerUserIds_existsReplacementForMultipleUserIds_shouldReturnLastReplacementForEachUser() {
        //Prepare
        User secondManager = saveManager("secondManager", "second@example.com");
        User thirdManager = saveManager("thirdManager", "third@example.com");

        saveReplacementManager(managerUser, tempManagerUser, Instant.now().minusSeconds(3600), Instant.now().plusSeconds(3600));
        saveReplacementManager(secondManager, tempManagerUser, Instant.now().minusSeconds(7200), Instant.now().plusSeconds(7200));
        saveReplacementManager(thirdManager, tempManagerUser, Instant.now().minusSeconds(1800), Instant.now().plusSeconds(1800));


        //Act
        List<ReplacementManager> result = replacementManagerRepository
                .findActiveReplacementsByManagerUserIds(Set.of(managerUser.getId(), secondManager.getId(), thirdManager.getId()));

        //Asserts
        assertThat(result)
                .hasSize(3)
                .extracting(ReplacementManager::getManagerUser)
                .containsExactlyInAnyOrder(managerUser, secondManager, thirdManager);
    }

    @Test
    void findActiveReplacementByManagerUserIds_replacementIsExpired_shouldReturnEmptyList() {
        User expiredManager = saveManager("expiredManager", "expired@example.com");
        saveReplacementManager(expiredManager, tempManagerUser,
                Instant.now().minusSeconds(100000), Instant.now().minusSeconds(50000));

        List<ReplacementManager> result = replacementManagerRepository
                .findActiveReplacementsByManagerUserIds(Set.of(expiredManager.getId()));

        assertThat(result).isEmpty();
    }

    private void deleteTestData() {
        testDatabaseFacade.cleanDatabase();
    }

    private void insertTestData() {
        department = saveDepartment();
        managerUser = saveManager("managerUser", "manager@example.com");
        tempManagerUser = saveUser("tempManager", "temp@example.com");

        saveReplacementManager(managerUser, tempManagerUser,
                Instant.now().minusSeconds(3600), Instant.now().plusSeconds(3600));
    }

    private Department saveDepartment() {
        return testDatabaseFacade.save(DepartmentEntityBuilder.builder().build());
    }

    private User saveManager(String login, String email) {
        User manager = UserEntityBuilder.builder()
                .withLogin(login)
                .withEmail(email)
                .withLeadDepartment(department)
                .build();
        return testDatabaseFacade.save(manager);
    }

    private User saveUser(String login, String email) {
        User user = UserEntityBuilder.builder()
                .withLogin(login)
                .withEmail(email)
                .withDepartment(department)
                .build();
        return testDatabaseFacade.save(user);
    }

    private ReplacementManager saveReplacementManager(User manager, User tempManager, Instant startDate, Instant endDate) {
        ReplacementManager replacement = ReplacementManager.builder()
                .managerUser(manager)
                .tempManagerUser(tempManager)
                .startDate(startDate)
                .endDate(endDate)
                .build();
        return testDatabaseFacade.save(replacement);
    }

}

