package ru.caselab.edm.backend.entity;

import lombok.Getter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.UUID;

public class UserInfoDetails implements UserDetails {
    @Getter
    private final UUID id;
    private final String login;
    private final String password;
    Collection<? extends GrantedAuthority> authorities;


    public UserInfoDetails(User user) {
        this.id = user.getId();
        this.login = user.getLogin();
        this.password = user.getPassword();

        this.authorities = getUserAuthorities(user);
    }

    @Transactional
    public List<SimpleGrantedAuthority> getUserAuthorities(User user) {
        List<SimpleGrantedAuthority> auths = new ArrayList<>();

        for (Role role : user.getRoles()) {
            auths.add(new SimpleGrantedAuthority("ROLE_" + role.getName()));
        }

        return auths;
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
        return UserDetails.super.isAccountNonExpired();
    }

    @Override
    public boolean isAccountNonLocked() {
        return UserDetails.super.isAccountNonLocked();
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return UserDetails.super.isCredentialsNonExpired();
    }

    @Override
    public boolean isEnabled() {
        return UserDetails.super.isEnabled();
    }
}
