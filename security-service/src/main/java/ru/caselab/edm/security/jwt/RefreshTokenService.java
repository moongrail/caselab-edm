package ru.caselab.edm.security.jwt;

import ru.caselab.edm.security.domain.RefreshToken;
import ru.caselab.edm.security.domain.User;

public interface RefreshTokenService {
    RefreshToken createRefreshToken(String login);

    RefreshToken verifyExpirationDate(RefreshToken refreshToken);

    User getUserByToken(String token);
}
