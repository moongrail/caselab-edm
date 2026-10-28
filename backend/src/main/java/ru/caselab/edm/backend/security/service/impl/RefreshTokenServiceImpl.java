package ru.caselab.edm.backend.security.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.caselab.edm.backend.configurations.props.JwtProperties;
import ru.caselab.edm.backend.entity.RefreshToken;
import ru.caselab.edm.backend.entity.User;
import ru.caselab.edm.backend.exceptions.ExpiredRefreshTokenException;
import ru.caselab.edm.backend.exceptions.ResourceNotFoundException;
import ru.caselab.edm.backend.repository.RefreshTokenRepository;
import ru.caselab.edm.backend.repository.UserRepository;
import ru.caselab.edm.backend.security.service.RefreshTokenService;

import java.time.Instant;
import java.util.UUID;

@Service
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
