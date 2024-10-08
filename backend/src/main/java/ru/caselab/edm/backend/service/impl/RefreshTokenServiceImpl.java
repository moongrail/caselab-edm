package ru.caselab.edm.backend.service.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ru.caselab.edm.backend.entity.RefreshToken;
import ru.caselab.edm.backend.entity.User;
import ru.caselab.edm.backend.exceptions.ExpiredRefreshTokenException;
import ru.caselab.edm.backend.exceptions.ResourceNotFoundException;
import ru.caselab.edm.backend.repository.RefreshTokenRepository;
import ru.caselab.edm.backend.repository.UserRepository;
import ru.caselab.edm.backend.service.RefreshTokenService;

import java.time.Instant;
import java.util.UUID;

@Service
public class RefreshTokenServiceImpl implements RefreshTokenService {

    private final UserRepository userRepository;
    private final RefreshTokenRepository refreshTokenRepository;

    public RefreshTokenServiceImpl(UserRepository userRepository, RefreshTokenRepository refreshTokenRepository) {
        this.userRepository = userRepository;
        this.refreshTokenRepository = refreshTokenRepository;
    }

    @Override
    public RefreshToken createRefreshToken(String login) {
        RefreshToken refreshToken = RefreshToken.builder()
                .token(UUID.randomUUID().toString())
                .expirationDate(Instant.now().plusMillis(30L * 24 * 60 * 60 * 1000))
                .user(userRepository.findUserByLogin(login)
                        .orElseThrow(() -> new ResourceNotFoundException("User not found"))).build();

        refreshTokenRepository.save(refreshToken);

        return refreshToken;
    }

    @Override
    public RefreshToken verifyExpirationDate(RefreshToken refreshToken) {
        if (refreshToken.getExpirationDate().compareTo(Instant.now()) < 0) {
            refreshTokenRepository.delete(refreshToken);
            throw new ExpiredRefreshTokenException("Refresh token is expired");
        }

        return refreshToken;
    }

    @Override
    public User getUserByToken(String token) {
        RefreshToken refreshToken = refreshTokenRepository.findRefreshTokenByToken(token)
                .orElseThrow(() -> new ResourceNotFoundException("Token not found"));

        return refreshToken.getUser();
    }


}
