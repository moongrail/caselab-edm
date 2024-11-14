package ru.caselab.edm.backend.entity;

import jakarta.persistence.*;
import lombok.*;

import java.util.List;
import java.util.Set;
import java.util.UUID;

@Entity
@Table(name = "users")
@Builder
@Getter
@Setter
@EqualsAndHashCode
@NoArgsConstructor
@AllArgsConstructor
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id")
    private UUID id;

    @ManyToOne
    @JoinColumn(name = "department_id", nullable = false)
    private Department departmentId;

    @Column(name = "login", nullable = false, unique = true)
    private String login;

    @Column(name = "email", nullable = false, unique = true)
    private String email;

    @Column(name = "password", nullable = false)
    private String password;

    @Column(name = "first_name", nullable = false)
    private String firstName;

    @Column(name = "last_name", nullable = false)
    private String lastName;

    @Column(name = "patronymic")
    private String patronymic;

    @Column(name = "position")
    private String position;

    @OneToMany(mappedBy = "user")
    private Set<RefreshToken> refreshTokens;

    @OneToMany(mappedBy = "user")
    private Set<Document> documents;

    @OneToMany(mappedBy = "managerUser")
    private Set<ReplacementManager> replacementManagers;

    @OneToMany(mappedBy = "tempManagerUser")
    private Set<ReplacementManager> tempReplacementManagers;


/*    @OneToMany(mappedBy = "user")
    private Set<Signature> signatures;*/

    @ManyToMany
    @JoinTable(
            name = "user_roles",
            joinColumns = @JoinColumn(name = "user_id"),
            inverseJoinColumns = @JoinColumn(name = "role_id")
    )
    private Set<Role> roles;

    @ManyToMany(mappedBy = "managers")
    private Set<Department> leadsDepartments;

    @ManyToMany(mappedBy = "members")
    private Set<Department> departments;

    @OneToMany(mappedBy = "user")
    private List<ApprovementProcessItem> approvementProcessItems;
}
