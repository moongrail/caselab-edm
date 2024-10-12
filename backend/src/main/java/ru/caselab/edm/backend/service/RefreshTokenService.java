package ru.caselab.edm.backend.service;

import ru.caselab.edm.backend.entity.RefreshToken;
import ru.caselab.edm.backend.entity.User;

public interface RefreshTokenService {
    RefreshToken createRefreshToken(String login);

    RefreshToken verifyExpirationDate(RefreshToken refreshToken);

    User getUserByToken(String token);
}
