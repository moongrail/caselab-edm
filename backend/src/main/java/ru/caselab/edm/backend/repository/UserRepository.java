package ru.caselab.edm.backend.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import ru.caselab.edm.backend.entity.User;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface UserRepository extends JpaRepository<User, UUID> {
    boolean existsByEmail(String email);

    boolean existsByLogin(String login);

    Optional<User> findUserByLogin(String username);

    @Query(value = """
            select u.* from users u
            join department_members dm on u.id = dm.member_id
            where dm.department_id =:departmentId
            and u.id != userId
            """,
            nativeQuery = true)
    List<User> getDepartmentMembersForReplacement(UUID userId, Long departmentId);

    @Query(value = """
            select u.* from users u
            join department_managers dm on u.id =dm.user_id
            where dm.department_id =:departmentId
            """,
            nativeQuery = true)
    List<User> getDepartmentManagersForReplacementDepartmentMember(Long departmentId);

    @Query(value = """
            select u.* from users u
            join department_members dm on u.id = dm.member_id
            where dm.department_id = :departmentId
            """,
            nativeQuery = true)
    List<User> getDepartmentMembersForReplacementManager(Long departmentId);
}
