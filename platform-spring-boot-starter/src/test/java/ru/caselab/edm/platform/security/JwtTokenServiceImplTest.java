package ru.caselab.edm.platform.security;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.Instant;
import java.util.Base64;
import java.util.Date;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class JwtTokenServiceImplTest {

    private static final String SECRET = Base64.getEncoder().encodeToString(
            "0123456789abcdef0123456789abcdef".getBytes(StandardCharsets.UTF_8));

    private JwtProperties properties;
    private JwtTokenServiceImpl service;
    private UUID userId;

    @BeforeEach
    void setUp() {
        userId = UUID.randomUUID();
        properties = new JwtProperties();
        properties.setSecret(SECRET);
        properties.setAccessTokenTtl(Duration.ofHours(1));
        service = new JwtTokenServiceImpl(properties);
    }

    @Test
    void createAndParseAccessToken_preservesClaims() {
        JwtClaims claims = new JwtClaims(userId, "owner", Set.of("USER", "ADMIN"), 2L, 3L);

        JwtClaims parsed = service.parseAccessToken(service.createAccessToken(claims));

        assertThat(parsed).isEqualTo(claims);
        assertThat(service.isExpired(service.createAccessToken(claims))).isFalse();
    }

    @Test
    void createAccessToken_nullableDepartments_arePreserved() {
        JwtClaims claims = new JwtClaims(userId, "owner", null, null, null);

        JwtClaims parsed = service.parseAccessToken(service.createAccessToken(claims));

        assertThat(parsed.departmentId()).isNull();
        assertThat(parsed.managedDepartmentId()).isNull();
        assertThat(parsed.roles()).isEmpty();
    }

    @Test
    void expiredToken_isReportedAndRejected() {
        properties.setAccessTokenTtl(Duration.ofMillis(-1000));
        JwtTokenServiceImpl expiredService = new JwtTokenServiceImpl(properties);
        String token = expiredService.createAccessToken(
                new JwtClaims(userId, "owner", Set.of(), null, null));

        assertThat(expiredService.isExpired(token)).isTrue();
        assertThatThrownBy(() -> expiredService.parseAccessToken(token))
                .isInstanceOf(JwtAuthenticationException.class)
                .hasMessageContaining("expired");
    }

    @Test
    void malformedToken_isRejected() {
        assertThat(service.isExpired("not-a-token")).isTrue();
        assertThatThrownBy(() -> service.parseAccessToken("not-a-token"))
                .isInstanceOf(JwtAuthenticationException.class)
                .hasMessageContaining("Invalid");
    }

    @Test
    void tokenWithWrongType_isRejected() {
        String token = Jwts.builder()
                .subject("owner")
                .claim("uid", userId.toString())
                .claim("type", "refresh")
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + 60_000))
                .signWith(Keys.hmacShaKeyFor(Decoders.BASE64.decode(SECRET)))
                .compact();

        assertThatThrownBy(() -> service.parseAccessToken(token))
                .isInstanceOf(JwtAuthenticationException.class)
                .hasMessageContaining("not an access token");
    }

    @Test
    void tokenWithoutUserId_isRejected() {
        String token = signedToken(builder -> builder.subject("owner")
                .claim("type", "access"));

        assertThatThrownBy(() -> service.parseAccessToken(token))
                .isInstanceOf(JwtAuthenticationException.class)
                .hasMessageContaining("uid");
    }

    @Test
    void tokenWithoutSubject_isRejected() {
        String token = signedToken(builder -> builder.claim("uid", userId.toString())
                .claim("type", "access"));

        assertThatThrownBy(() -> service.parseAccessToken(token))
                .isInstanceOf(JwtAuthenticationException.class)
                .hasMessageContaining("sub");
    }

    @Test
    void rolesPayloadThatIsNotList_becomesEmpty() {
        String token = signedToken(builder -> builder
                .subject("owner")
                .claim("uid", userId.toString())
                .claim("type", "access")
                .claim("roles", "USER"));

        assertThat(service.parseAccessToken(token).roles()).isEmpty();
    }

    @Test
    void authenticatedUserImplementsUserDetailsContract() {
        AuthenticatedUser principal = new AuthenticatedUser(
                userId, "owner", Set.of("USER", "ROLE_ADMIN"), 1L, 2L);

        assertThat(principal.getUsername()).isEqualTo("owner");
        assertThat(principal.getPassword()).isNull();
        assertThat(principal.getAuthorities()).extracting("authority")
                .containsExactlyInAnyOrder("ROLE_USER", "ROLE_ADMIN");
        assertThat(principal.isAccountNonExpired()).isTrue();
        assertThat(principal.isAccountNonLocked()).isTrue();
        assertThat(principal.isCredentialsNonExpired()).isTrue();
        assertThat(principal.isEnabled()).isTrue();
    }

    @Test
    void authenticatedUser_nullRoles_areEmpty() {
        assertThat(new AuthenticatedUser(userId, "owner", null, null, null).roles())
                .isEmpty();
    }

    @Test
    void jwtAndSecurityPropertiesExposeConfiguration() {
        properties.setRefreshTokenTtl(Duration.ofDays(2));
        SecurityProperties security = new SecurityProperties();
        security.getCors().setAllowedOrigins(List.of("https://example.test"));
        security.getCors().setAllowedMethods(List.of("GET"));
        security.getCors().setAllowedHeaders(List.of("Authorization"));
        security.getCors().setExposedHeaders(List.of("Location"));
        security.getCors().setAllowCredentials(false);
        security.getCors().setMaxAge(10);
        security.setPublicPaths(List.of("/health"));

        assertThat(properties.getSecret()).isEqualTo(SECRET);
        assertThat(properties.getAccessTokenTtl()).isEqualTo(Duration.ofHours(1));
        assertThat(properties.getRefreshTokenTtl()).isEqualTo(Duration.ofDays(2));
        assertThat(security.getPublicPaths()).containsExactly("/health");
        assertThat(security.getCors().getAllowedOrigins()).containsExactly("https://example.test");
        assertThat(security.getCors().getAllowedMethods()).containsExactly("GET");
        assertThat(security.getCors().getAllowedHeaders()).containsExactly("Authorization");
        assertThat(security.getCors().getExposedHeaders()).containsExactly("Location");
        assertThat(security.getCors().isAllowCredentials()).isFalse();
        assertThat(security.getCors().getMaxAge()).isEqualTo(10);
    }

    private String signedToken(java.util.function.Consumer<io.jsonwebtoken.JwtBuilder> customizer) {
        var builder = Jwts.builder()
                .issuedAt(new Date())
                .expiration(Date.from(Instant.now().plusSeconds(60)));
        customizer.accept(builder);
        return builder.signWith(Keys.hmacShaKeyFor(Decoders.BASE64.decode(SECRET))).compact();
    }
}
