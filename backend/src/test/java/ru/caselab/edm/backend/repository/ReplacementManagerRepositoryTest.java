package ru.caselab.edm.backend.repository;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ActiveProfiles;
import ru.caselab.edm.backend.entity.Department;
import ru.caselab.edm.backend.entity.ReplacementManager;
import ru.caselab.edm.backend.entity.User;
import ru.caselab.edm.backend.repository.elastic.AttributeSearchRepository;

import java.time.Instant;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

@ActiveProfiles("test")
@DataJpaTest
public class ReplacementManagerRepositoryTest {

    @Autowired
    private DepartmentRepository departmentRepository;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private ReplacementManagerRepository replacementManagerRepository;

    @MockBean
    AttributeSearchRepository attributeSearchRepository;

    private Department department;
    private User managerUser;
    private User tempManagerUser;
    private ReplacementManager replacementManager;

    @BeforeEach
    void setUp() {
        deleteTestData();
        insertTestData();
    }

    @Test
    void findActiveReplacementByManagerUser_existsReplacementForUserId_shouldReturnReplacementManager() {
        Optional<ReplacementManager> replacementManagerOptional = replacementManagerRepository.findActiveReplacementByManagerUserId(managerUser.getId());

        assertThat(replacementManagerOptional).isPresent();
        ReplacementManager replacementManager = replacementManagerOptional.get();
        assertThat(replacementManager.getTempManagerUser()).isEqualTo(tempManagerUser);
    }

    @Test
    void findActiveReplacementByManagerUser_nonExistsReplacementForUserId_shouldReturnEmptyOptional() {
        Optional<ReplacementManager> replacementManagerOptional = replacementManagerRepository.findActiveReplacementByManagerUserId(tempManagerUser .getId());

        assertThat(replacementManagerOptional).isEmpty();
    }

    @Test
    void findActiveReplacementByManagerUser_replacementIsExpired_shouldReturnEmptyOptional() {
        Instant startDate = Instant.now().minusSeconds(100000);
        Instant endDate = startDate.minusSeconds(50000);
        User anotherManager = saveTestManager("anotherManager", "anotherManager@gmail.com", department);
        saveTestReplacementManager(anotherManager, tempManagerUser, startDate, endDate);

        Optional<ReplacementManager> replacementManagerOptional = replacementManagerRepository.findActiveReplacementByManagerUserId(anotherManager.getId());

        assertThat(replacementManagerOptional).isEmpty();

    }

    @Test
    void findActiveReplacementByManagerUserIds_noActiveReplacementForUser_shouldReturnEmptyList() {
        User newUser = saveTestManager("newManagerUser", "newManagerUser@example.com", department);

        List<ReplacementManager> replacementManagers =
                replacementManagerRepository.findActiveReplacementsByManagerUserIds(Set.of(newUser.getId()));

        assertThat(replacementManagers).isEmpty();
    }

    @Test
    void findActiveReplacementByManagerUserIds_existsReplacementForMultipleUserIds_shouldReturnLastReplacementForEachUser() {
        //Additional users
        User secondManagerUser = saveTestManager("secondManagerUser", "secondManagerUser@example.com", department);
        User thirdManagerUser = saveTestManager("thirdManagerUser", "thirdManagerUser@example.com", department);

        //Replacements
        Instant startDate1 = Instant.now().minusSeconds(3600);
        Instant endDate1 = Instant.now().plusSeconds(3600);
        saveTestReplacementManager(managerUser, tempManagerUser, startDate1, endDate1);

        Instant startDate2 = Instant.now().minusSeconds(7200);
        Instant endDate2 = Instant.now().plusSeconds(7200);
        saveTestReplacementManager(secondManagerUser, tempManagerUser, startDate2, endDate2);

        Instant startDate3 = Instant.now().minusSeconds(1800);
        Instant endDate3 = Instant.now().plusSeconds(1800);
        saveTestReplacementManager(thirdManagerUser, tempManagerUser, startDate3, endDate3);

        //Act
        List<ReplacementManager> replacementManagers = replacementManagerRepository.findActiveReplacementsByManagerUserIds
                        (Set.of(managerUser.getId(), secondManagerUser.getId(), thirdManagerUser.getId()));

        //Asserts
        assertThat(replacementManagers).hasSize(3);
        assertThat(replacementManagers.stream().anyMatch(rm -> rm.getManagerUser().equals(managerUser))).isTrue();
        assertThat(replacementManagers.stream().anyMatch(rm -> rm.getManagerUser().equals(secondManagerUser))).isTrue();
        assertThat(replacementManagers.stream().anyMatch(rm -> rm.getManagerUser().equals(thirdManagerUser))).isTrue();
    }

    @Test
    void findActiveReplacementByManagerUserIds_replacementIsExpired_shouldReturnEmptyList() {
        Instant startDate = Instant.now().minusSeconds(100000);
        Instant endDate = startDate.minusSeconds(50000);
        User anotherManager = saveTestManager("anotherManager", "anotherManager@gmail.com", department);
        saveTestReplacementManager(anotherManager, tempManagerUser, startDate, endDate);

        List<ReplacementManager> replacementManagers = replacementManagerRepository.findActiveReplacementsByManagerUserIds(
                Set.of(anotherManager.getId()));

        assertThat(replacementManagers).isEmpty();
    }

    private void deleteTestData() {
        replacementManagerRepository.deleteAll();
        replacementManagerRepository.flush();

        userRepository.deleteAll();
        userRepository.flush();

        departmentRepository.deleteAll();
        departmentRepository.flush();
    }

    private void insertTestData() {
        department = saveTestDepartment();
        managerUser = saveTestManager("managerUser", "managerUser@example.com", department);
        tempManagerUser = saveTestMember("memberUser", "memberUser@example.com", department);

        replacementManager = saveTestReplacementManager(managerUser, tempManagerUser,
                Instant.now().minusSeconds(3600), Instant.now().plusSeconds(3600));
    }

    private Department saveTestDepartment() {
        return departmentRepository.save(Department.builder()
                .name("test-department")
                .build());
    }

    private User saveTestManager(String login, String email, Department department) {
        User manager = buildTestUserWithoutDepartments(login, email);
        Set<Department> departments = new HashSet<>();
        departments.add(department);
        manager.setLeadsDepartments(departments);

        return userRepository.save(manager);
    }

    private User saveTestMember(String login, String email, Department department) {
        User member = buildTestUserWithoutDepartments(login, email);
        Set<Department> departments = new HashSet<>();
        departments.add(department);
        member.setDepartments(departments);

        return userRepository.save(member);
    }

    private User buildTestUserWithoutDepartments(String login, String email) {
        return userRepository.save(User.builder()
                .login(login)
                .email(email)
                .password("test-password")
                .firstName("test-name")
                .lastName("test-lastname")
                .position("test-position")
                .build());
    }
    private ReplacementManager saveTestReplacementManager(User managerUser, User tempManagerUser, Instant startDate, Instant endDate) {
        return replacementManagerRepository.save(ReplacementManager.builder()
                .managerUser(managerUser)
                .tempManagerUser(tempManagerUser)
                .startDate(startDate)
                .endDate(endDate)
                .build());
    }
}
