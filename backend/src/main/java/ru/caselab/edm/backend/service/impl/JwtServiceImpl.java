package ru.caselab.edm.backend.service.impl;

import io.jsonwebtoken.*;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;
import ru.caselab.edm.backend.service.JwtService;

import java.security.Key;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

@Service
public class JwtServiceImpl implements JwtService {

    private static String SECRET_KEY;
    private static final int JWT_TOKEN_LIFETIME = 60 * 60 * 1000; // 1 час

    private final JwtParser jwtParser;

    public JwtServiceImpl(@Value("${secret.key}") String key) {
        SECRET_KEY = key;
        jwtParser = Jwts.parser().setSigningKey(SECRET_KEY).build();
    }

    @Override
    public String getLogin(String token) {
        return getClaims(token, Claims::getSubject);
    }

    @Override
    public Date getExpiration(String token) {
        return  getClaims(token, Claims::getExpiration);
    }

    @Override
    public boolean validateToken(String token, UserDetails user) {
        final String username = getLogin(token);
        return (username.equals(user.getUsername()) && !isExpired(token));
    }

    @Override
    public <T> T getClaims(String token, Function<Claims, T> function) {
        Claims claims = jwtParser.parseClaimsJws(token).getBody();

        return function.apply(claims);
    }

    @Override
    public String generateToken(UserDetails user) {

        return createToken(new HashMap<String, Object>(), user);
    }


    @Override
    public boolean isExpired(String token) {
        return getExpiration(token).before(new Date());
    }

    public Key getSignKey() {
        return Keys.hmacShaKeyFor(Decoders.BASE64.decode(SECRET_KEY));
    }


    private String createToken(Map<String, Object> claims, UserDetails user) {
        List<String> roles = user.getAuthorities().stream().map(GrantedAuthority::getAuthority).toList();
        claims.put("authorities", roles);

        return Jwts.builder()
                .setClaims(claims)
                .setSubject(user.getUsername())
                .setIssuedAt(new Date(System.currentTimeMillis()))
                .setExpiration(new Date(System.currentTimeMillis() + (JWT_TOKEN_LIFETIME)))
                .signWith(getSignKey(), SignatureAlgorithm.HS256).compact();
    }



}
