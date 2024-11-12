package ru.caselab.edm.backend.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import ru.caselab.edm.backend.entity.User;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface UserRepository extends JpaRepository<User, UUID> {
    boolean existsByEmail(String email);

    boolean existsByLogin(String login);

    Optional<User> findUserByLogin(String username);

    @Query(value = """
            SELECT * FROM  users u
            WHERE u.department_id IN (select department_id from users u2 where id = :userId)
            AND id !=:userId
            """, countQuery = """
            SELECT * FROM  users u
            WHERE u.department_id IN (select department_id from users u2 where id = :userId)
            AND id !=:userId
            """,
            nativeQuery = true)
    Page<User> getAllUsersForReplacement(UUID userId, Pageable pageable);
}
