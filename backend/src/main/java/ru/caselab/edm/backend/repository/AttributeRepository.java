package ru.caselab.edm.backend.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import ru.caselab.edm.backend.entity.Attribute;

@Repository
public interface AttributeRepository extends JpaRepository<Attribute, Long> {
}
