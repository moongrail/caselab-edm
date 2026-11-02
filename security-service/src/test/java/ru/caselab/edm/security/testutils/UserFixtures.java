package ru.caselab.edm.security.testutils;

import ru.caselab.edm.security.domain.User;

import java.util.UUID;

/**
 * Minimal object mother shared by security-module service tests.
 */
public final class UserFixtures {

    public static final UUID OWNER_ID = UUID.fromString("11111111-1111-1111-1111-111111111111");
    public static final UUID SIGNER_ID = UUID.fromString("22222222-2222-2222-2222-222222222222");
    public static final UUID THIRD_ID = UUID.fromString("33333333-3333-3333-3333-333333333333");

    private UserFixtures() {
    }

    public static User user(UUID id, String login) {
        return User.builder()
                .id(id)
                .login(login)
                .email(login + "@example.com")
                .password("encoded-password")
                .firstName("First")
                .lastName("Last")
                .position("Engineer")
                .build();
    }
}
