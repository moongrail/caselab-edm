package ru.caselab.edm.backend.security.service.impl;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtParser;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.stereotype.Service;
import ru.caselab.edm.backend.configurations.props.JwtProperties;
import ru.caselab.edm.backend.exceptions.ExpiredJwtTokenException;
import ru.caselab.edm.backend.security.service.JwtService;

import javax.crypto.SecretKey;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

@Service
@EnableConfigurationProperties(JwtProperties.class)
public class JwtServiceImpl implements JwtService {

    private final SecretKey signingKey;
    private final long accessTokenLifetimeMillis;
    private final JwtParser jwtParser;

    public JwtServiceImpl(JwtProperties jwtProperties) {
        this.signingKey = Keys.hmacShaKeyFor(Decoders.BASE64.decode(jwtProperties.getSecret()));
        this.accessTokenLifetimeMillis = jwtProperties.getAccessTokenTtl().toMillis();
        this.jwtParser = Jwts.parser()
                .verifyWith(signingKey)
                .build();
    }

    @Override
    public String getLogin(String token) {
        return getClaims(token, Claims::getSubject);
    }

    @Override
    public Date getExpiration(String token) {
        return getClaims(token, Claims::getExpiration);
    }

    @Override
    public boolean validateToken(String token, UserDetails user) {
        if (isExpired(token)) {
            throw new ExpiredJwtTokenException("JWT token expired");
        }
        return getLogin(token).equals(user.getUsername());
    }

    @Override
    public <T> T getClaims(String token, Function<Claims, T> function) {
        Claims claims = jwtParser.parseSignedClaims(token).getPayload();
        return function.apply(claims);
    }

    @Override
    public String generateToken(UserDetails user) {
        return createToken(new HashMap<>(), user);
    }

    @Override
    public boolean isExpired(String token) {
        try {
            return getExpiration(token).before(new Date());
        } catch (io.jsonwebtoken.ExpiredJwtException ex) {
            return true;
        }
    }

    private String createToken(Map<String, Object> claims, UserDetails user) {
        List<String> roles = user.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .toList();
        claims.put("authorities", roles);

        Date now = new Date();
        return Jwts.builder()
                .claims(claims)
                .subject(user.getUsername())
                .issuedAt(now)
                .expiration(new Date(now.getTime() + accessTokenLifetimeMillis))
                .signWith(signingKey, Jwts.SIG.HS256)
                .compact();
    }
}
