package ru.caselab.edm.platform.security;

/**
 * Issues and parses signed access tokens.
 */
public interface JwtTokenService {

    String createAccessToken(JwtClaims claims);

    JwtClaims parseAccessToken(String token);

    boolean isExpired(String token);
}
