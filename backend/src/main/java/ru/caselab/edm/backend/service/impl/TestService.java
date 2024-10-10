package ru.caselab.edm.backend.service.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ru.caselab.edm.backend.dto.JwtDTO;
import ru.caselab.edm.backend.dto.LoginUserDTO;
import ru.caselab.edm.backend.entity.User;
import ru.caselab.edm.backend.entity.UserInfoDetails;
import ru.caselab.edm.backend.repository.UserRepository;
import ru.caselab.edm.backend.service.JwtService;
import ru.caselab.edm.backend.service.RefreshTokenService;

@Service
public class TestService {
    // здесь вставляем все роли юзера и т.д крч тестовый сервис

    private final JwtService jwtService;
    private final UserRepository userRepository;
    private final RefreshTokenService refreshTokenService;

    @Autowired
    public TestService(JwtService jwtService, UserRepository userRepository,
                        RefreshTokenService refreshTokenService) {
        this.jwtService = jwtService;
        this.userRepository = userRepository;
        this.refreshTokenService = refreshTokenService;
    }

    public JwtDTO testJwt(LoginUserDTO dto) {
        User user = userRepository.findUserByLogin(dto.login()).orElseThrow(() -> new RuntimeException("Not found"));

        UserInfoDetails userInfoDetails = new UserInfoDetails(user);

        JwtDTO jwtDto = new JwtDTO(refreshTokenService.createRefreshToken(dto.login()).getToken()
                , jwtService.generateToken(userInfoDetails));

        return jwtDto;
    }
}
