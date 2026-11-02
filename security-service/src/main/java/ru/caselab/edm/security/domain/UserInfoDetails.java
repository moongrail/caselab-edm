package ru.caselab.edm.security.domain;

import lombok.Getter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.UUID;

@Getter
public class UserInfoDetails implements UserDetails {

    private final UUID id;
    private final String login;
    private final String password;
    private final Set<String> roleNames;
    private final Long departmentId;
    private final Long managedDepartmentId;
    private final Collection<? extends GrantedAuthority> authorities;

    public UserInfoDetails(User user) {
        this.id = user.getId();
        this.login = user.getLogin();
        this.password = user.getPassword();
        this.roleNames = user.getRoles() == null
                ? Set.of()
                : user.getRoles().stream()
                .map(role -> role.getName().name())
                .collect(java.util.stream.Collectors.toCollection(LinkedHashSet::new));
        this.departmentId = user.getDepartment() == null ? null : user.getDepartment().getId();
        this.managedDepartmentId = user.getLeadDepartment() == null
                ? null
                : user.getLeadDepartment().getId();
        this.authorities = roleNames.stream()
                .map(role -> new SimpleGrantedAuthority("ROLE_" + role))
                .toList();
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return authorities;
    }

    @Override
    public String getPassword() {
        return password;
    }

    @Override
    public String getUsername() {
        return login;
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return true;
    }
}
