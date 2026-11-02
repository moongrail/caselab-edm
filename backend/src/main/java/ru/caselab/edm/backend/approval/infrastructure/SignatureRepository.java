package ru.caselab.edm.backend.approval.infrastructure;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import ru.caselab.edm.backend.approval.domain.Signature;

@Repository
public interface SignatureRepository extends JpaRepository<Signature, Long> {
    /**
     * Проверяет, что по заявке на подписание уже сформирована подпись.
     *
     * @param approvementProcessItemId идентификатор заявки на подписание
     * @return true, если подпись уже существует
     */
    boolean existsByApprovementProcessItemId(Long approvementProcessItemId);
}
