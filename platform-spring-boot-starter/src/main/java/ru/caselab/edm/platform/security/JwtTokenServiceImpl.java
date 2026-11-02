package ru.caselab.edm.platform.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.JwtParser;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;

import javax.crypto.SecretKey;
import java.util.Date;
import java.util.List;
import java.util.Set;
import java.util.UUID;

/**
 * HMAC-SHA256 access-token codec shared by the identity service and resource
 * servers.
 */
public class JwtTokenServiceImpl implements JwtTokenService {

    static final String ACCESS_TOKEN_TYPE = "access";
    private static final String CLAIM_USER_ID = "uid";
    private static final String CLAIM_ROLES = "roles";
    private static final String CLAIM_DEPARTMENT_ID = "department_id";
    private static final String CLAIM_MANAGED_DEPARTMENT_ID = "managed_department_id";
    private static final String CLAIM_TYPE = "type";

    private final SecretKey signingKey;
    private final long accessTokenLifetimeMillis;
    private final JwtParser jwtParser;

    public JwtTokenServiceImpl(JwtProperties properties) {
        this.signingKey = Keys.hmacShaKeyFor(Decoders.BASE64.decode(properties.getSecret()));
        this.accessTokenLifetimeMillis = properties.getAccessTokenTtl().toMillis();
        this.jwtParser = Jwts.parser().verifyWith(signingKey).build();
    }

    @Override
    public String createAccessToken(JwtClaims claims) {
        Date now = new Date();
        return Jwts.builder()
                .subject(claims.username())
                .claim(CLAIM_USER_ID, claims.userId().toString())
                .claim(CLAIM_ROLES, List.copyOf(claims.roles()))
                .claim(CLAIM_DEPARTMENT_ID, claims.departmentId())
                .claim(CLAIM_MANAGED_DEPARTMENT_ID, claims.managedDepartmentId())
                .claim(CLAIM_TYPE, ACCESS_TOKEN_TYPE)
                .issuedAt(now)
                .expiration(new Date(now.getTime() + accessTokenLifetimeMillis))
                .signWith(signingKey, Jwts.SIG.HS256)
                .compact();
    }

    @Override
    public JwtClaims parseAccessToken(String token) {
        try {
            Claims claims = jwtParser.parseSignedClaims(token).getPayload();
            if (!ACCESS_TOKEN_TYPE.equals(claims.get(CLAIM_TYPE, String.class))) {
                throw new JwtAuthenticationException("Token is not an access token");
            }
            return new JwtClaims(
                    UUID.fromString(required(claims, CLAIM_USER_ID, String.class)),
                    required(claims, Claims.SUBJECT, String.class),
                    Set.copyOf(roles(claims)),
                    claims.get(CLAIM_DEPARTMENT_ID, Number.class) == null
                            ? null
                            : claims.get(CLAIM_DEPARTMENT_ID, Number.class).longValue(),
                    claims.get(CLAIM_MANAGED_DEPARTMENT_ID, Number.class) == null
                            ? null
                            : claims.get(CLAIM_MANAGED_DEPARTMENT_ID, Number.class).longValue()
            );
        } catch (JwtAuthenticationException ex) {
            throw ex;
        } catch (ExpiredJwtException ex) {
            throw new JwtAuthenticationException("Access token expired", ex);
        } catch (JwtException | IllegalArgumentException ex) {
            throw new JwtAuthenticationException("Invalid access token", ex);
        }
    }

    @Override
    public boolean isExpired(String token) {
        try {
            return jwtParser.parseSignedClaims(token).getPayload().getExpiration().before(new Date());
        } catch (ExpiredJwtException ex) {
            return true;
        } catch (JwtException | IllegalArgumentException ex) {
            return true;
        }
    }

    private static <T> T required(Claims claims, String name, Class<T> type) {
        T value = claims.get(name, type);
        if (value == null || (value instanceof String text && text.isBlank())) {
            throw new JwtAuthenticationException("Token claim is missing: " + name);
        }
        return value;
    }

    private static List<String> roles(Claims claims) {
        Object value = claims.get(CLAIM_ROLES);
        if (!(value instanceof List<?> list)) {
            return List.of();
        }
        return list.stream().map(String::valueOf).toList();
    }
}
