package ru.caselab.edm.backend.service.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ru.caselab.edm.backend.dto.JwtDto;
import ru.caselab.edm.backend.dto.LoginUserDto;
import ru.caselab.edm.backend.entity.User;
import ru.caselab.edm.backend.entity.UserInfoDetails;
import ru.caselab.edm.backend.repository.RoleRepository;
import ru.caselab.edm.backend.repository.UserRepository;
import ru.caselab.edm.backend.service.JwtService;
import ru.caselab.edm.backend.service.RefreshTokenService;

import java.util.ArrayList;
import java.util.List;

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

    public JwtDto testJwt(LoginUserDto dto) {
        User user = userRepository.findUserByLogin(dto.login()).orElseThrow(() -> new RuntimeException("Not found"));

        UserInfoDetails userInfoDetails = new UserInfoDetails(user);

        JwtDto jwtDto = new JwtDto(refreshTokenService.createRefreshToken(dto.login()).getToken()
                , jwtService.generateToken(userInfoDetails));

        return jwtDto;
    }
}
