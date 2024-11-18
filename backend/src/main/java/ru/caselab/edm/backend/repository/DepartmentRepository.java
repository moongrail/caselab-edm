package ru.caselab.edm.backend.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import ru.caselab.edm.backend.entity.Department;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface DepartmentRepository extends JpaRepository<Department, Long> {

    @Query(value = """
            SELECT d.id, d.name, d.description, d.parent_id
            FROM departments d
            JOIN department_members dm
            ON d.id = dm.department_id
            WHERE dm.member_id = :userId
            ORDER BY d.name ASC
            """,
            nativeQuery = true)
    Page<Department> getDepartmentsWithUser(UUID userId, Pageable pageable);

    @Query(value = """
            SELECT d.id, d.name, d.description, d.parent_id
            FROM departments d
            JOIN department_managers dm
            ON d.id = dm.department_id
            WHERE dm.user_id = :userId
            ORDER BY d.name ASC
            """,
            nativeQuery = true)
    Page<Department> getSubordinateDepartments(UUID userId, Pageable pageable);

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