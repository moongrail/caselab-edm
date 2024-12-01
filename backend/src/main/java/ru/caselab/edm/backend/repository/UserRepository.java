package ru.caselab.edm.backend.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import ru.caselab.edm.backend.entity.User;
import ru.caselab.edm.backend.repository.projection.TopUsersByReplacementProjection;
import ru.caselab.edm.backend.repository.projection.TopUsersByDocumentCreationProjection;
import ru.caselab.edm.backend.repository.projection.TopUsersByDocumentSigningProjection;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
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

    @Query(value = """
            SELECT u
            FROM User u
            JOIN u.department d
            WHERE d.id = :departmentId
            AND u.id <> :userId
            """)
    List<User> getDepartmentMembersForReplacement(UUID userId, Long departmentId);

    @Query(value = """
            SELECT u
            FROM User u
            WHERE u.leadDepartment.id = :departmentId
            """)
    List<User> getDepartmentManagersForReplacementDepartmentMember(Long departmentId);

    @Query(value = """
            SELECT u
            FROM User u
            JOIN u.department d
            WHERE d.id = :departmentId
            """)
    List<User> getDepartmentMembersForReplacementManager(Long departmentId);

    @Query("""
                SELECT u
                FROM User u
                JOIN u.department d
                WHERE 
                  d.id = :departmentId
                  AND u.id NOT IN :notAvailableUsers
            """)
    Page<User> getAvailableUsersForDelegation(@Param("departmentId") Long departmentId, @Param("notAvailableUsers") List<UUID> notAvailableUsers, Pageable pageable);

    @Query("""
                SELECT u
                FROM User u
                JOIN u.department d
                WHERE u.id = :userId
                  AND d.id = :departmentId
                  AND u.id NOT IN :notAvailableUsers
            """)
    Optional<User> findUserByIdAndDepartmentAndNotInNotAvailableList(UUID userId, @Param("departmentId") Long departmentId, List<UUID> notAvailableUsers);

    @Query("""
    SELECT 
        u.id as userId,
        COUNT(d) as documentCount
    FROM User u
    JOIN u.documents d
    WHERE d.createdAt BETWEEN :startDate AND :endDate
    GROUP BY u.id
    ORDER BY COUNT(d) DESC
    """)
    Page<TopUsersByDocumentCreationProjection> findTopUserByDocumentCreation(@Param("startDate") Instant startDate, @Param("endDate") Instant endDate, Pageable pageable);


    @Query("""
    SELECT s.approvementProcessItem.user.id as userId, COUNT(s) as signatureCount
    FROM Signature s
    JOIN s.approvementProcessItem.user u
    WHERE s.createdAt BETWEEN :startDate AND :endDate
    GROUP BY u.id
    ORDER BY COUNT(s) DESC
    """)
    Page<TopUsersByDocumentSigningProjection> findTopUserByDocumentSigning(@Param("startDate") LocalDateTime startDate, @Param("endDate") LocalDateTime endDate, Pageable pageable);


    @Query("""
    SELECT rm.tempManagerUser.id as userId, COUNT(rm) as replacementCount
    FROM ReplacementManager rm
    WHERE rm.startDate <= :endDate AND rm.endDate >= :startDate
    Group by rm.tempManagerUser.id
    ORDER BY COUNT(rm) DESC
    """)
    Page<TopUsersByReplacementProjection> findTopUserByReplacement(@Param("startDate") Instant startDate, @Param("endDate") Instant endDate, Pageable pageable);

}


