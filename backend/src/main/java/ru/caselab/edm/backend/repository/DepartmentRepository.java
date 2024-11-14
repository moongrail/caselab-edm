package ru.caselab.edm.backend.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import ru.caselab.edm.backend.entity.Department;

import java.util.List;
import java.util.UUID;

@Repository
public interface DepartmentRepository extends JpaRepository<Department, Long> {
    @Query(value = """
            select dm.department_id from department_members dm
            join users u on u.id = dm.member_id
            where u.id = :userId
            """,
            nativeQuery = true)
    List<Department> findDepartmentByMemberUuid(UUID userId);

    @Query(value = """
            select dm.department_id from department_managers dm
            join users u on u.id = dm.member_id
            where u.id = :userId
            """,
            nativeQuery = true)
    List<Department> findDepartmentByManagerUuid(UUID userId);
}