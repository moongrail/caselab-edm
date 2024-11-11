package ru.caselab.edm.backend.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import ru.caselab.edm.backend.entity.Department;

@Repository
public interface DepartmentRepository extends JpaRepository<Department, Long> {
}
