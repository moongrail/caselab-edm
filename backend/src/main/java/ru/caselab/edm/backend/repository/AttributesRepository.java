package ru.caselab.edm.backend.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import ru.caselab.edm.backend.entity.DocumentAttribute;

@Repository
public interface AttributesRepository extends JpaRepository<DocumentAttribute, Long> {

}
