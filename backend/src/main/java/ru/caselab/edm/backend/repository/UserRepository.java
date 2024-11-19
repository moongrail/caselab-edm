package ru.caselab.edm.backend.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import ru.caselab.edm.backend.entity.User;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface UserRepository extends JpaRepository<User, UUID> {
    boolean existsByEmail(String email);

    boolean existsByLogin(String login);

    Optional<User> findUserByLogin(String username);

    @Query(value = """
            SELECT u.id, u.login, u.email, u.first_name, u.last_name, u.patronymic, u.password, u.position
            FROM users u
            JOIN department_managers dm
            ON u.id = dm.user_id
            WHERE dm.department_id = :departmentId
            """,
            nativeQuery = true)
    Page<User> getDepartmentManagers(@Param("departmentId") Long departmentId, Pageable pageable);

    @Query(value = """
            SELECT u.id, u.login, u.email, u.first_name, u.last_name, u.patronymic, u.password, u.position
            FROM users u
            JOIN department_members dm
            ON u.id = dm.member_id
            WHERE dm.department_id = :departmentId
            """,
            nativeQuery = true)
    Page<User> getDepartmentMembers(@Param("departmentId") Long departmentId, Pageable pageable);

    @Query(value = """
            select u.* from users u
            join department_members dm on u.id = dm.member_id
            where dm.department_id =:departmentId
            and u.id !=:userId
            """,
            nativeQuery = true)
    List<User> getDepartmentMembersForReplacement(UUID userId, Long departmentId);

    @Query(value = """
            select u.* from users u
            join department_managers dm on u.id =dm.user_id
            where dm.department_id =:departmentId
            """,
            nativeQuery = true)
    List<User> getDepartmentManagersForReplacementDepartmentMember(Long departmentId);

    @Query(value = """
            select u.* from users u
            join department_members dm on u.id = dm.member_id
            where dm.department_id = :departmentId
            """,
            nativeQuery = true)
    List<User> getDepartmentMembersForReplacementManager(Long departmentId);
}
