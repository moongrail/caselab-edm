package ru.caselab.edm.backend.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import ru.caselab.edm.backend.entity.Signature;

@Repository
public interface SignatureRepository extends JpaRepository<Signature, Long> {
    /**
     * Checks if a Signature exists with the given user ID and version ID.
     *
     * @param userId the UUID of the user
     * @param versionId the Long version ID
     * @return true if exists, false otherwise
     */
    //boolean existsByUserIdAndDocumentVersionId(UUID userId, Long versionId);
}
