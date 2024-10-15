package ru.caselab.edm.backend.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import ru.caselab.edm.backend.entity.DocumentType;

import java.util.Optional;

@Repository
public interface DocumentTypeRepository extends JpaRepository<DocumentType, Long> {
    Optional<DocumentType> findByName(String name);

//    @Query("SELECT dt.name FROM DocumentType dt JOIN DocumentTypeAttribute da ON dt.id = da.documentType.id WHERE da.attribute.id IN :documentTypeIds")
//    String findDocumentTypeNameByDocumentTypeAttributeId(List<Long> documentTypeIds);
}
