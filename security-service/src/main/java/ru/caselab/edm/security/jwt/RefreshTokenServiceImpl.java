package ru.caselab.edm.security.jwt;

import lombok.RequiredArgsConstructor;
import org.springframework.transaction.annotation.Transactional;
import ru.caselab.edm.platform.security.JwtProperties;
import ru.caselab.edm.security.domain.RefreshToken;
import ru.caselab.edm.security.domain.User;
import ru.caselab.edm.security.exception.ExpiredRefreshTokenException;
import ru.caselab.edm.security.exception.ResourceNotFoundException;
import ru.caselab.edm.security.persistence.RefreshTokenRepository;
import ru.caselab.edm.security.persistence.UserRepository;
import ru.caselab.edm.security.jwt.RefreshTokenService;

import java.time.Instant;
import java.util.UUID;

@RequiredArgsConstructor
@Transactional
public class RefreshTokenServiceImpl implements RefreshTokenService {

    private final UserRepository userRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final JwtProperties jwtProperties;

    @Override
    public RefreshToken createRefreshToken(String login) {
        User user = userRepository.findUserByLogin(login)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "User not found with this login = %s".formatted(login)));

        RefreshToken refreshToken = RefreshToken.builder()
                .token(UUID.randomUUID().toString())
                .expirationDate(Instant.now().plus(jwtProperties.getRefreshTokenTtl()))
                .user(user)
                .build();

        return refreshTokenRepository.save(refreshToken);
    }

    @Override
    public RefreshToken verifyExpirationDate(RefreshToken refreshToken) {
        if (refreshToken.getExpirationDate().isBefore(Instant.now())) {
            refreshTokenRepository.delete(refreshToken);
            throw new ExpiredRefreshTokenException("Refresh token is expired");
        }
        return refreshToken;
    }

    @Override
    public User getUserByToken(String token) {
        RefreshToken refreshToken = refreshTokenRepository.findRefreshTokenByToken(token)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Token not found with this token = %s".formatted(token)));
        return verifyExpirationDate(refreshToken).getUser();
    }
}
