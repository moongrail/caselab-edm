package ru.caselab.edm.backend.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import ru.caselab.edm.backend.entity.Department;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface DepartmentRepository extends JpaRepository<Department, Long> {

    @Query("""
            SELECT d
            FROM Department d 
            LEFT JOIN d.members m 
            LEFT JOIN d.manager mn 
            WHERE m.id = :userId OR mn.id = :userId
            """)
    List<Department> getDepartmentsWithUser(@Param("userId") UUID userId);

    @Query(value = """
            SELECT d
            FROM Department d
            JOIN d.manager m
            WHERE m.id = :userId
            """)
    Page<Department> getSubordinateDepartments(@Param("userId") UUID userId, Pageable pageable);

    @Query(value = """
            select dm.department_id from department_members dm
            join users u on u.id = dm.member_id
            where u.id = :userId
            """,
            nativeQuery = true)
    Optional<Long> findDepartmentByMemberUuid(UUID userId);

    @Query(value = """
            select dm.department_id from department_managers dm
            join users u on u.id = dm.user_id
            where u.id = :userId
            """,
            nativeQuery = true)
    Optional<Long> findDepartmentByManagerUuid(UUID userId);
}
