package ru.caselab.edm.security.jwt;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import ru.caselab.edm.platform.security.JwtProperties;
import ru.caselab.edm.security.domain.RefreshToken;
import ru.caselab.edm.security.domain.User;
import ru.caselab.edm.security.exception.ExpiredRefreshTokenException;
import ru.caselab.edm.security.exception.ResourceNotFoundException;
import ru.caselab.edm.security.persistence.RefreshTokenRepository;
import ru.caselab.edm.security.persistence.UserRepository;

import java.time.Duration;
import java.time.Instant;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static ru.caselab.edm.security.testutils.UserFixtures.OWNER_ID;
import static ru.caselab.edm.security.testutils.UserFixtures.user;

@ExtendWith(MockitoExtension.class)
class SecurityJwtServicesTest {

    @Mock
    private UserRepository userRepository;
    @Mock
    private RefreshTokenRepository refreshTokenRepository;

    private RefreshTokenServiceImpl refreshTokenService;

    @BeforeEach
    void setUp() {
        JwtProperties properties = new JwtProperties();
        properties.setRefreshTokenTtl(Duration.ofDays(30));
        refreshTokenService = new RefreshTokenServiceImpl(
                userRepository, refreshTokenRepository, properties);
    }

    @Test
    void createRefreshToken_savesTokenForExistingUser() {
        User user = user(OWNER_ID, "owner");
        when(userRepository.findUserByLogin("owner")).thenReturn(Optional.of(user));
        when(refreshTokenRepository.save(any(RefreshToken.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        RefreshToken token = refreshTokenService.createRefreshToken("owner");

        assertThat(token.getUser()).isSameAs(user);
        assertThat(token.getToken()).isNotBlank();
        assertThat(token.getExpirationDate()).isAfter(Instant.now());
    }

    @Test
    void createRefreshToken_missingUser_throws() {
        when(userRepository.findUserByLogin("missing")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> refreshTokenService.createRefreshToken("missing"))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void verifyExpirationDate_validToken_returnsToken() {
        RefreshToken token = RefreshToken.builder()
                .expirationDate(Instant.now().plusSeconds(60))
                .build();

        assertThat(refreshTokenService.verifyExpirationDate(token)).isSameAs(token);
    }

    @Test
    void verifyExpirationDate_expiredToken_deletesAndThrows() {
        RefreshToken token = RefreshToken.builder()
                .expirationDate(Instant.now().minusSeconds(60))
                .build();

        assertThatThrownBy(() -> refreshTokenService.verifyExpirationDate(token))
                .isInstanceOf(ExpiredRefreshTokenException.class);
        verify(refreshTokenRepository).delete(token);
    }

    @Test
    void getUserByToken_found_returnsUser() {
        User user = user(OWNER_ID, "owner");
        RefreshToken token = RefreshToken.builder()
                .token("token")
                .user(user)
                .expirationDate(Instant.now().plusSeconds(60))
                .build();
        when(refreshTokenRepository.findRefreshTokenByToken("token")).thenReturn(Optional.of(token));

        assertThat(refreshTokenService.getUserByToken("token")).isSameAs(user);
    }

    @Test
    void getUserByToken_missing_throws() {
        when(refreshTokenRepository.findRefreshTokenByToken("token")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> refreshTokenService.getUserByToken("token"))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void userDetailsService_loadsExistingUser() {
        User user = user(OWNER_ID, "owner");
        user.setRoles(java.util.Set.of());
        when(userRepository.findUserByLogin("owner")).thenReturn(Optional.of(user));

        UserDetails details = new UserDetailsServiceImpl(userRepository)
                .loadUserByUsername("owner");

        assertThat(details.getUsername()).isEqualTo("owner");
    }

    @Test
    void userDetailsService_missingUser_throws() {
        when(userRepository.findUserByLogin("missing")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> new UserDetailsServiceImpl(userRepository)
                .loadUserByUsername("missing"))
                .isInstanceOf(UsernameNotFoundException.class);
    }
}
