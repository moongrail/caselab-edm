package ru.caselab.edm.backend.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import ru.caselab.edm.backend.entity.User;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface UserRepository extends JpaRepository<User, UUID> {
    boolean existsByEmail(String email);

    boolean existsByLogin(String login);

    Optional<User> findUserByLogin(String username);

    @Query(value = """
            SELECT u
            FROM User u
            WHERE u.leadDepartment.id = :departmentId
            """)
    Optional<User> getDepartmentManager(@Param("departmentId") Long departmentId);

    @Query(value = """
            SELECT u
            FROM User u
            JOIN u.department d
            WHERE d.id = :departmentId
            """)
    Page<User> getDepartmentMembers(@Param("departmentId") Long departmentId, Pageable pageable);

    @Query(value = """
        SELECT EXISTS (
            SELECT 1
            FROM department_members dm
            WHERE member_id = :userId
        ) AS result;
    """, nativeQuery = true)
    boolean existsUserInOtherDepartmentsAsMember(@Param("userId") UUID userId);

    @Query(value = """
        SELECT EXISTS (
            SELECT 1
            FROM department_managers dm
            WHERE dm.user_id = :userId
        ) AS result;
    """, nativeQuery = true)
    boolean existsUserAsManager(@Param("userId") UUID userId);
}
