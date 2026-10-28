package ru.caselab.edm.backend.security;

import io.jsonwebtoken.JwtException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import ru.caselab.edm.backend.configurations.props.JwtProperties;
import ru.caselab.edm.backend.entity.RefreshToken;
import ru.caselab.edm.backend.entity.User;
import ru.caselab.edm.backend.exceptions.ExpiredJwtTokenException;
import ru.caselab.edm.backend.exceptions.ExpiredRefreshTokenException;
import ru.caselab.edm.backend.exceptions.ResourceNotFoundException;
import ru.caselab.edm.backend.repository.RefreshTokenRepository;
import ru.caselab.edm.backend.repository.UserRepository;
import ru.caselab.edm.backend.security.details.UserDetailsServiceImpl;
import ru.caselab.edm.backend.security.service.impl.JwtServiceImpl;
import ru.caselab.edm.backend.security.service.impl.RefreshTokenServiceImpl;

import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.Instant;
import java.util.Base64;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static ru.caselab.edm.backend.testutils.fixtures.TestFixtures.OWNER_ID;
import static ru.caselab.edm.backend.testutils.fixtures.TestFixtures.user;

@ExtendWith(MockitoExtension.class)
class SecurityServicesTest {

    private JwtProperties jwtProperties;

    @Mock
    private UserDetails userDetails;

    @Mock
    private UserRepository userRepository;
    @Mock
    private RefreshTokenRepository refreshTokenRepository;

    private JwtServiceImpl jwtService;
    private RefreshTokenServiceImpl refreshTokenService;

    @BeforeEach
    void setUp() {
        jwtProperties = new JwtProperties();
        jwtProperties.setSecret(Base64.getEncoder().encodeToString(
                "0123456789abcdef0123456789abcdef".getBytes(StandardCharsets.UTF_8)));
        jwtProperties.setAccessTokenTtl(Duration.ofHours(1));
        jwtProperties.setRefreshTokenTtl(Duration.ofDays(30));
        jwtService = new JwtServiceImpl(jwtProperties);
        refreshTokenService = new RefreshTokenServiceImpl(
                userRepository, refreshTokenRepository, jwtProperties);

        lenient().when(userDetails.getUsername()).thenReturn("owner");
        lenient().doReturn(List.of(new SimpleGrantedAuthority("ROLE_USER")))
                .when(userDetails).getAuthorities();
    }

    @Test
    void jwtGenerateAndReadClaims() {
        String token = jwtService.generateToken(userDetails);

        assertThat(jwtService.getLogin(token)).isEqualTo("owner");
        assertThat(jwtService.getExpiration(token)).isAfter(new java.util.Date());
        org.junit.jupiter.api.Assertions.assertFalse(jwtService.isExpired(token));
        org.junit.jupiter.api.Assertions.assertTrue(jwtService.validateToken(token, userDetails));
        @SuppressWarnings("unchecked")
        List<Object> authorities = (List<Object>) jwtService.getClaims(token, claims -> claims.get("authorities", List.class));
        assertThat(authorities).containsExactly("ROLE_USER");
    }

    @Test
    void jwtValidateToken_otherUser_returnsFalse() {
        String token = jwtService.generateToken(userDetails);
        UserDetails other = org.mockito.Mockito.mock(UserDetails.class);
        when(other.getUsername()).thenReturn("other");

        assertThat(jwtService.validateToken(token, other)).isFalse();
    }

    @Test
    void jwtExpiredToken_throwsOnValidation() {
        jwtProperties.setAccessTokenTtl(Duration.ofMillis(-1000));
        String token = new JwtServiceImpl(jwtProperties).generateToken(userDetails);

        assertThat(jwtService.isExpired(token)).isTrue();
        assertThatThrownBy(() -> jwtService.validateToken(token, userDetails))
                .isInstanceOf(ExpiredJwtTokenException.class);
    }

    @Test
    void jwtMalformedToken_throwsJwtException() {
        assertThatThrownBy(() -> jwtService.getLogin("not-a-token"))
                .isInstanceOf(JwtException.class);
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

        UserDetailsServiceImpl service = new UserDetailsServiceImpl(userRepository);
        org.springframework.security.core.userdetails.UserDetails details =
                service.loadUserByUsername("owner");

        assertThat(details.getUsername()).isEqualTo("owner");
    }

    @Test
    void userDetailsService_missingUser_throws() {
        when(userRepository.findUserByLogin("missing")).thenReturn(Optional.empty());
        UserDetailsServiceImpl service = new UserDetailsServiceImpl(userRepository);

        assertThatThrownBy(() -> service.loadUserByUsername("missing"))
                .isInstanceOf(org.springframework.security.core.userdetails.UsernameNotFoundException.class);
    }
}
